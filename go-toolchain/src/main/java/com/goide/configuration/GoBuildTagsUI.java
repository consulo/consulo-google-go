/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide.configuration;

import com.goide.project.GoBuildTargetSettings;
import consulo.content.bundle.Sdk;
import consulo.go.GoConstants;
import consulo.go.GoEnvironmentUtil;
import consulo.go.localize.GoLocalize;
import consulo.go.module.extension.GoMutableModuleExtension;
import consulo.localize.LocalizeValue;
import consulo.platform.Platform;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.HtmlLabel;
import consulo.ui.Hyperlink;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.LabeledLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.model.FlatDataModel;
import consulo.ui.model.MutableFlatDataModel;
import consulo.ui.util.FormBuilder;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ThreeState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class GoBuildTagsUI {
    private static final String BUILD_CONSTRAINTS_URL = "https://golang.org/pkg/go/build/#hdr-Build_Constraints";

    private final GoMutableModuleExtension myExtension;
    private final GoBuildTargetSettings mySettings;

    private final MutableFlatDataModel<String> myGoVersionModel;
    private final MutableFlatDataModel<ThreeState> myCgoModel;

    private final Component myComponent;

    @RequiredUIAccess
    public GoBuildTagsUI(GoMutableModuleExtension extension) {
        myExtension = extension;
        mySettings = extension.getBuildTargetSettings();

        ComboBox<String> osBox = createBox(createModel(GoConstants.KNOWN_OS, GoBuildTargetSettings.DEFAULT, mySettings.os),
            () -> GoLocalize.goModuleExtensionDefaultValue(GoEnvironmentUtil.systemOS()));
        osBox.setValue(mySettings.os);

        ComboBox<String> archBox = createBox(createModel(GoConstants.KNOWN_ARCH, GoBuildTargetSettings.DEFAULT, mySettings.arch),
            () -> GoLocalize.goModuleExtensionDefaultValue(GoEnvironmentUtil.systemArch()));
        archBox.setValue(mySettings.arch);

        myGoVersionModel = createModel(GoConstants.KNOWN_VERSIONS, GoBuildTargetSettings.DEFAULT, mySettings.goVersion);
        ComboBox<String> goVersionBox = createBox(myGoVersionModel, this::getDefaultGoVersion);
        goVersionBox.setValue(mySettings.goVersion);

        MutableFlatDataModel<String> compilerModel =
            createModel(GoConstants.KNOWN_COMPILERS, GoBuildTargetSettings.ANY_COMPILER, mySettings.compiler);
        ComboBox<String> compilerBox = ComboBox.create(compilerModel);
        compilerBox.setTextRenderer(value -> value == null ? LocalizeValue.empty() : LocalizeValue.of(value));
        compilerBox.setValue(mySettings.compiler);

        myCgoModel = FlatDataModel.of(List.of(ThreeState.UNSURE, ThreeState.YES, ThreeState.NO));
        ComboBox<ThreeState> cgoBox = ComboBox.create(myCgoModel);
        cgoBox.setTextRenderer(value -> {
            if (value == null) {
                return LocalizeValue.empty();
            }
            if (value == ThreeState.UNSURE) {
                String os = expandDefault(mySettings.os, GoEnvironmentUtil.systemOS());
                String arch = expandDefault(mySettings.arch, GoEnvironmentUtil.systemArch());
                return GoLocalize.goModuleExtensionDefaultValue(cgo(GoEnvironmentUtil.systemCgo(os, arch)));
            }
            return cgo(value);
        });
        cgoBox.setValue(mySettings.cgo);

        TextBoxWithExpandAction customTagsBox = TextBoxWithExpandAction.create(
            PlatformIconGroup.actionsShow(),
            GoLocalize.goModuleExtensionCustomTagsDialogTitle().get(),
            text -> StringUtil.split(text, " "),
            tags -> StringUtil.join(tags, " ")
        );
        customTagsBox.setValue(StringUtil.join(mySettings.customFlags, " "));

        osBox.addValueListener(event -> {
            mySettings.os = StringUtil.notNullize(event.getValue(), GoBuildTargetSettings.DEFAULT);
            myCgoModel.update(ThreeState.UNSURE);
        });
        archBox.addValueListener(event -> {
            mySettings.arch = StringUtil.notNullize(event.getValue(), GoBuildTargetSettings.DEFAULT);
            myCgoModel.update(ThreeState.UNSURE);
        });
        goVersionBox.addValueListener(event ->
            mySettings.goVersion = StringUtil.notNullize(event.getValue(), GoBuildTargetSettings.DEFAULT));
        compilerBox.addValueListener(event ->
            mySettings.compiler = StringUtil.notNullize(event.getValue(), GoBuildTargetSettings.ANY_COMPILER));
        cgoBox.addValueListener(event -> {
            ThreeState value = event.getValue();
            mySettings.cgo = value == null ? ThreeState.UNSURE : value;
        });
        customTagsBox.addValueListener(event ->
            mySettings.customFlags = ArrayUtil.toStringArray(StringUtil.split(StringUtil.notNullize(event.getValue()), " ")));

        FormBuilder formBuilder = FormBuilder.create();
        formBuilder.addLabeled(GoLocalize.goModuleExtensionOsLabel(), osBox);
        formBuilder.addLabeled(GoLocalize.goModuleExtensionArchLabel(), archBox);
        formBuilder.addLabeled(GoLocalize.goModuleExtensionGoVersionLabel(), goVersionBox);
        formBuilder.addLabeled(GoLocalize.goModuleExtensionCompilerLabel(), compilerBox);
        formBuilder.addLabeled(GoLocalize.goModuleExtensionCgoLabel(), cgoBox);
        formBuilder.addLabeled(GoLocalize.goModuleExtensionCustomTagsLabel(), customTagsBox);

        VerticalLayout layout = VerticalLayout.create();
        layout.add(HtmlLabel.create(GoLocalize.goModuleExtensionBuildTagsDescription()));
        layout.add(Hyperlink.create(
            GoLocalize.goModuleExtensionBuildTagsDocumentationLink(),
            event -> Platform.current().openInBrowser(BUILD_CONSTRAINTS_URL)
        ));
        layout.add(formBuilder.build());

        myComponent = LabeledLayout.create(GoLocalize.goModuleExtensionBuildTagsGroup(), layout);
    }

    @RequiredUIAccess
    public void sdkChanged() {
        myGoVersionModel.update(GoBuildTargetSettings.DEFAULT);
    }

    public Component getComponent() {
        return myComponent;
    }

    private LocalizeValue getDefaultGoVersion() {
        Sdk sdk = myExtension.getSdk();
        String sdkName = sdk == null ? null : myExtension.getSdkName();
        if (sdkName == null) {
            return GoLocalize.goModuleExtensionModuleSdkValue(GoLocalize.goModuleExtensionAnySdk());
        }
        return GoLocalize.goModuleExtensionModuleSdkValue(sdkName);
    }

    @RequiredUIAccess
    private static ComboBox<String> createBox(MutableFlatDataModel<String> model, Supplier<LocalizeValue> defaultPresentation) {
        ComboBox<String> box = ComboBox.create(model);
        box.setTextRenderer(value -> {
            if (value == null) {
                return LocalizeValue.empty();
            }
            return GoBuildTargetSettings.DEFAULT.equals(value) ? defaultPresentation.get() : LocalizeValue.of(value);
        });
        return box;
    }

    private static MutableFlatDataModel<String> createModel(Collection<String> values, String defaultValue, @Nullable String currentValue) {
        List<String> items = new ArrayList<>();
        items.add(defaultValue);
        items.addAll(ContainerUtil.sorted(values));
        if (currentValue != null && !items.contains(currentValue)) {
            items.add(currentValue);
        }
        return FlatDataModel.of(items);
    }

    private static String expandDefault(String value, String defaultValue) {
        return GoBuildTargetSettings.DEFAULT.equals(value) ? defaultValue : value;
    }

    private static LocalizeValue cgo(ThreeState threeState) {
        if (threeState == ThreeState.YES) {
            return GoLocalize.goModuleExtensionEnabled();
        }
        if (threeState == ThreeState.NO) {
            return GoLocalize.goModuleExtensionDisabled();
        }
        return LocalizeValue.of(GoBuildTargetSettings.DEFAULT);
    }
}

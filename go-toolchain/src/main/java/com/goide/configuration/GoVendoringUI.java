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

import com.goide.project.GoVendoringUtil;
import consulo.content.bundle.Sdk;
import consulo.go.localize.GoLocalize;
import consulo.go.module.extension.GoMutableModuleExtension;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.LabeledLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.model.FlatDataModel;
import consulo.ui.model.MutableFlatDataModel;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.ThreeState;

import java.util.List;

public class GoVendoringUI {
    private final GoMutableModuleExtension myModuleExtension;

    private final MutableFlatDataModel<ThreeState> myVendoringModel;
    private final ComboBox<ThreeState> myVendoringBox;
    private final Label myErrorMessageLabel;

    private final Component myComponent;

    private boolean myVendoringByDefault;

    @RequiredUIAccess
    public GoVendoringUI(GoMutableModuleExtension moduleExtension) {
        myModuleExtension = moduleExtension;

        myVendoringModel = FlatDataModel.of(List.of(ThreeState.UNSURE, ThreeState.YES, ThreeState.NO));
        myVendoringBox = ComboBox.create(myVendoringModel);
        myVendoringBox.setTextRenderer(value -> {
            if (value == null) {
                return LocalizeValue.empty();
            }
            switch (value) {
                case YES:
                    return GoLocalize.goModuleExtensionEnabled();
                case NO:
                    return GoLocalize.goModuleExtensionDisabled();
                default:
                    return GoLocalize.goModuleExtensionVendoringDefault(
                        myVendoringByDefault ? GoLocalize.goModuleExtensionEnabled() : GoLocalize.goModuleExtensionDisabled()
                    );
            }
        });

        myErrorMessageLabel = Label.create(LocalizeValue.empty());
        myErrorMessageLabel.setImage(PlatformIconGroup.generalBalloonwarning());

        sdkChanged();

        myVendoringBox.addValueListener(event -> {
            ThreeState value = event.getValue();
            myModuleExtension.setVendoringEnabled(value == null ? ThreeState.UNSURE : value);
        });

        FormBuilder formBuilder = FormBuilder.create();
        formBuilder.addLabeled(GoLocalize.goModuleExtensionVendoringLabel(), myVendoringBox);

        VerticalLayout layout = VerticalLayout.create();
        layout.add(formBuilder.build());
        layout.add(myErrorMessageLabel);

        myComponent = LabeledLayout.create(GoLocalize.goModuleExtensionVendoringGroup(), layout);
    }

    @RequiredUIAccess
    public void sdkChanged() {
        Sdk sdk = myModuleExtension.getSdk();
        String sdkVersion = sdk == null ? null : sdk.getVersionString();
        if (!GoVendoringUtil.vendoringCanBeDisabled(sdkVersion)) {
            myErrorMessageLabel.setText(GoLocalize.goModuleExtensionVendoringCannotBeDisabled(String.valueOf(sdkVersion)));
            myErrorMessageLabel.setVisible(true);
            myVendoringBox.setEnabled(false);
        }
        else if (!GoVendoringUtil.supportsVendoring(sdkVersion) && sdkVersion != null) {
            myErrorMessageLabel.setText(GoLocalize.goModuleExtensionVendoringNotSupported(sdkVersion));
            myErrorMessageLabel.setVisible(true);
            myVendoringBox.setEnabled(true);
        }
        else {
            myErrorMessageLabel.setVisible(false);
            myVendoringBox.setEnabled(true);
        }

        myVendoringByDefault = GoVendoringUtil.supportsVendoringByDefault(sdkVersion);
        myVendoringModel.update(ThreeState.UNSURE);

        myVendoringBox.setValue(myModuleExtension.getVendoringEnabled(), false);
    }

    public Component getComponent() {
        return myComponent;
    }
}

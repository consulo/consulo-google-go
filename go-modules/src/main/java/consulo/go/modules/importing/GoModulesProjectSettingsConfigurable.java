/*
 * Copyright 2013-2026 consulo.io
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

package consulo.go.modules.importing;

import com.goide.sdk.GoSdkType;
import consulo.disposer.Disposable;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.go.localize.GoLocalize;
import consulo.go.modules.setting.GoModulesProjectSettings;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * The SDK of a linked project, empty for "the Go SDK matching the go on PATH", and whether the import downloads
 * missing modules.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModulesProjectSettingsConfigurable extends AbstractExternalProjectSettingsConfigurable<GoModulesProjectSettings> {
    private @Nullable BundleBox mySdkBox;
    private @Nullable CheckBox myDownloadBox;

    public GoModulesProjectSettingsConfigurable(GoModulesProjectSettings settings, ExternalSystemSettingsPlace place) {
        super(settings, place);
    }

    @RequiredUIAccess
    @Override
    protected Component createExtraUIComponent(Disposable uiDisposable) {
        GoModulesProjectSettings settings = getSettings();

        BundleBox sdkBox = BundleBoxBuilder.create(uiDisposable)
            .withSdkTypeFilter(sdkType -> sdkType instanceof GoSdkType)
            .withNoneItem(GoLocalize.goModulesSettingsSdkAuto())
            .build();
        String sdkName = settings.getSdkName();
        if (sdkName != null) {
            sdkBox.setSelectedBundle(sdkName);
        }
        else {
            sdkBox.setSelectedNoneBundle();
        }
        mySdkBox = sdkBox;

        CheckBox downloadBox = CheckBox.create(GoLocalize.goModulesSettingsDownloadDependencies(), settings.isDownloadDependencies());
        myDownloadBox = downloadBox;

        FormBuilder builder = FormBuilder.create().addLabeled(GoLocalize.goModulesSettingsSdkLabel(), sdkBox.getComponent());
        builder.addBottom(downloadBox);
        return builder.build();
    }

    @RequiredUIAccess
    @Override
    protected boolean isExtraModified() {
        GoModulesProjectSettings settings = getSettings();
        return !Objects.equals(selectedSdkName(), settings.getSdkName()) || isDownloadSelected() != settings.isDownloadDependencies();
    }

    @RequiredUIAccess
    @Override
    protected void applyExtra() {
        GoModulesProjectSettings settings = getSettings();
        settings.setSdkName(selectedSdkName());
        settings.setDownloadDependencies(isDownloadSelected());
    }

    @RequiredUIAccess
    private @Nullable String selectedSdkName() {
        BundleBox sdkBox = mySdkBox;
        return sdkBox != null ? sdkBox.getSelectedBundleName() : getSettings().getSdkName();
    }

    @RequiredUIAccess
    private boolean isDownloadSelected() {
        CheckBox downloadBox = myDownloadBox;
        return downloadBox != null ? downloadBox.getValueOrError() : getSettings().isDownloadDependencies();
    }
}

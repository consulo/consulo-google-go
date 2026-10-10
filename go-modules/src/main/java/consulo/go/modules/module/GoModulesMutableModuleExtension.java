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

package consulo.go.modules.module;

import consulo.disposer.Disposable;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemMutableModuleExtension;
import consulo.go.localize.GoLocalize;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModulesMutableModuleExtension extends GoModulesModuleExtension
    implements ExternalSystemMutableModuleExtension<ExternalSystemModuleExtensionImpl> {
    public GoModulesMutableModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer);
    }

    public void setModulePath(@Nullable String modulePath) {
        setOption(MODULE_PATH_KEY, modulePath);
    }

    public void setGoModFile(@Nullable String goModFile) {
        setOption(GO_MOD_KEY, goModFile);
    }

    public void setGoVersion(@Nullable String goVersion) {
        setOption(GO_VERSION_KEY, goVersion);
    }

    public void setWorkFile(@Nullable String workFile) {
        setOption(WORK_FILE_KEY, workFile);
    }

    @Override
    public void setOption(String key, @Nullable String value) {
        if (value == null) {
            myOptions.remove(key);
        }
        else {
            myOptions.put(key, value);
        }
    }

    @Override
    public void removeOption(String key) {
        myOptions.remove(key);
    }

    @Override
    public void removeAllOptions() {
        myOptions.clear();
    }

    @Override
    public void setEnabled(boolean enabled) {
        myIsEnabled = enabled;
    }

    @Override
    public boolean isModified(ExternalSystemModuleExtensionImpl originalExtension) {
        return myIsEnabled != originalExtension.isEnabled()
            || !(originalExtension instanceof GoModulesModuleExtension other)
            || !hasSameOptions(other);
    }

    @RequiredUIAccess
    @Override
    public @Nullable Component createConfigurationComponent(Disposable uiDisposable, Runnable updateOnCheck) {
        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(GoLocalize.goModulesExtensionModulePathLabel(), Label.create(text(getModulePath())));
        builder.addLabeled(GoLocalize.goModulesExtensionGoModLabel(), Label.create(text(getGoModFile())));
        String workFile = getWorkFile();
        if (workFile != null) {
            builder.addLabeled(GoLocalize.goModulesExtensionGoWorkLabel(), Label.create(LocalizeValue.of(workFile)));
        }
        return builder.build();
    }

    private static LocalizeValue text(@Nullable String value) {
        return value != null ? LocalizeValue.of(value) : LocalizeValue.empty();
    }
}

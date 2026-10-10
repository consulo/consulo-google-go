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

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsConfigurableFactory;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.setting.GoModulesProjectSettings;
import consulo.go.modules.setting.GoModulesSettings;
import consulo.project.ProjectManager;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModulesSettingsConfigurableFactory implements ExternalSystemSettingsConfigurableFactory<GoModulesProjectSettings, GoModulesSettings> {
    @Override
    public ProjectSystemId getSystemId() {
        return GoModulesConstants.SYSTEM_ID;
    }

    @Override
    public GoModulesProjectSettings createProjectSettings() {
        return new GoModulesProjectSettings();
    }

    @Override
    public GoModulesSettings createSystemSettings() {
        return new GoModulesSettings(ProjectManager.getInstance().getDefaultProject());
    }

    @Override
    @RequiredUIAccess
    public AbstractExternalProjectSettingsConfigurable<GoModulesProjectSettings> createProjectSettingsConfigurable(GoModulesProjectSettings settings,
                                                                                                                ExternalSystemSettingsPlace place) {
        return new GoModulesProjectSettingsConfigurable(settings, place);
    }
}

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

package consulo.go.modules.setting;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ProjectConfigurable;
import consulo.configurable.StandardConfigurableIds;
import consulo.externalSystem.service.setting.AbstractExternalSystemConfigurable;
import consulo.go.modules.GoModulesConstants;
import consulo.project.Project;
import jakarta.inject.Inject;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModulesConfigurable extends AbstractExternalSystemConfigurable<GoModulesProjectSettings, GoModulesSettingsListener, GoModulesSettings>
    implements ProjectConfigurable {
    @Inject
    public GoModulesConfigurable(Project project) {
        super(project, GoModulesConstants.SYSTEM_ID);
    }

    @Override
    public String getId() {
        return "execution.go.modules";
    }

    @Override
    public String getParentId() {
        return StandardConfigurableIds.EXECUTION_GROUP;
    }
}

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
import consulo.externalSystem.importing.AbstractExternalModuleImportProvider;
import consulo.externalSystem.importing.ExternalModuleImportContext;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.go.icon.GoIconGroup;
import consulo.go.modules.GoModulesConstants;
import consulo.project.Project;
import consulo.ui.image.Image;
import jakarta.inject.Inject;

import java.io.File;

/**
 * Opens a directory holding a {@code go.work} or a {@code go.mod} as a project.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModulesModuleImportProvider extends AbstractExternalModuleImportProvider {
    @Inject
    public GoModulesModuleImportProvider(ProjectDataManager dataManager) {
        super(dataManager, GoModulesConstants.SYSTEM_ID);
    }

    @Override
    public Image getIcon() {
        return GoIconGroup.gomod();
    }

    @Override
    public boolean canImport(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            return new File(fileOrDirectory, GoModulesConstants.GO_WORK).isFile() || new File(fileOrDirectory, GoModulesConstants.GO_MOD).isFile();
        }
        String name = fileOrDirectory.getName();
        return GoModulesConstants.GO_WORK.equals(name) || GoModulesConstants.GO_MOD.equals(name);
    }

    @Override
    protected void doPrepare(ExternalModuleImportContext context) {
    }

    @Override
    protected void beforeCommit(DataNode<ProjectData> dataNode, Project project) {
    }

    @Override
    protected File getExternalProjectConfigToUse(File file) {
        File directory = file.isDirectory() ? file : file.getParentFile();
        return directory != null ? directory : file;
    }

    @Override
    protected void applyExtraSettings(ExternalModuleImportContext context) {
    }
}

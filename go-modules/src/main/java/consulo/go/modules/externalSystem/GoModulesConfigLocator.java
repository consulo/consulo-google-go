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

package consulo.go.modules.externalSystem;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.setting.ExternalSystemConfigLocator;
import consulo.externalSystem.setting.ExternalProjectSettings;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.GoModulesProjects;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Tells the platform which files make a linked Go project, so a refresh is skipped while none of them changed.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModulesConfigLocator implements ExternalSystemConfigLocator {
    @Override
    public ProjectSystemId getTargetExternalSystemId() {
        return GoModulesConstants.SYSTEM_ID;
    }

    @Override
    public @Nullable VirtualFile adjust(VirtualFile configPath) {
        if (!configPath.isDirectory()) {
            return GoModulesConstants.SETTINGS_FILE_NAMES.contains(configPath.getName()) ? configPath : null;
        }
        VirtualFile work = configPath.findChild(GoModulesConstants.GO_WORK);
        return work != null ? work : configPath.findChild(GoModulesConstants.GO_MOD);
    }

    @Override
    public List<VirtualFile> findAll(ExternalProjectSettings externalProjectSettings) {
        List<VirtualFile> files = new ArrayList<>();
        String projectPath = externalProjectSettings.getExternalProjectPath();
        if (projectPath == null) {
            return files;
        }
        // no project here: the go.mod files of the modules a go.work uses are tracked by GoModulesAutoImportAware
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        Path root = GoModulesProjects.toDirectory(Path.of(projectPath));
        for (String name : GoModulesConstants.SETTINGS_FILE_NAMES) {
            VirtualFile file = fileSystem.findFileByNioFile(root.resolve(name));
            if (file != null && !file.isDirectory()) {
                files.add(file);
            }
        }
        return files;
    }
}

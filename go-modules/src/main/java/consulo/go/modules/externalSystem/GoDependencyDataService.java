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
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.service.project.manage.ProjectDataService;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.go.modules.orderEntry.GoDependencyEntries;
import consulo.go.modules.orderEntry.GoDependencyOrderEntryModel;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Puts the build list into the modules as {@link GoDependencyOrderEntryModel} entries.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoDependencyDataService implements ProjectDataService<GoDependencyData, Module> {
    @Override
    public Key<GoDependencyData> getTargetDataKey() {
        return GoDependencyData.KEY;
    }

    @Override
    @RequiredUIAccess
    public void importData(Collection<DataNode<GoDependencyData>> toImport, Project project, boolean synchronous) {
        Map<String, List<GoDependencyOrderEntryModel>> byModule = new LinkedHashMap<>();
        List<Path> directories = new ArrayList<>();
        for (DataNode<GoDependencyData> node : toImport) {
            ModuleData moduleData = node.getData(ProjectKeys.MODULE);
            if (moduleData == null) {
                continue;
            }
            GoDependencyData data = node.getData();
            String directory = data.getDirectory();
            if (directory != null) {
                directories.add(Path.of(directory));
            }
            byModule.computeIfAbsent(moduleData.getInternalName(), name -> new ArrayList<>()).add(new GoDependencyOrderEntryModel(
                data.getModulePath(),
                data.getVersion(),
                directory != null ? VirtualFileUtil.pathToUrl(directory) : null,
                data.getReplacement(),
                data.isIndirect()));
        }
        if (byModule.isEmpty()) {
            return;
        }

        // freshly downloaded modules are not in the VFS yet; their roots must resolve before the root models change
        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        for (Path directory : directories) {
            fileSystem.refreshAndFindFileByNioFile(directory);
        }

        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            @RequiredUIAccess
            public void execute() {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                for (Map.Entry<String, List<GoDependencyOrderEntryModel>> entry : byModule.entrySet()) {
                    Module module = moduleManager.findModuleByName(entry.getKey());
                    if (module == null || module.isDisposed()) {
                        continue;
                    }
                    ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
                    GoDependencyEntries.replace(rootModel, entry.getValue());
                    if (rootModel.isChanged()) {
                        rootModel.commit();
                    }
                    else {
                        rootModel.dispose();
                    }
                }
            }
        });
    }

    @Override
    public void removeData(Collection<? extends Module> toRemove, Project project, boolean synchronous) {
    }
}

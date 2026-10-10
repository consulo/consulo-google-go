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
import consulo.externalSystem.service.project.manage.ProjectDataService;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.go.modules.GoModulesProjects;
import consulo.module.ModifiableModuleModel;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Drops the modules a linked project imported before but no longer has, e.g. a {@code use} removed from the {@code go.work}.
 * Only modules this system imported are touched; anything the user made stays.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoWorkspaceDataService implements ProjectDataService<GoWorkspaceData, Void> {
    @Override
    public Key<GoWorkspaceData> getTargetDataKey() {
        return GoWorkspaceData.KEY;
    }

    @Override
    @RequiredUIAccess
    public void importData(Collection<DataNode<GoWorkspaceData>> toImport, Project project, boolean synchronous) {
        if (toImport.isEmpty()) {
            return;
        }
        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            @RequiredUIAccess
            public void execute() {
                List<Module> stale = new ArrayList<>();
                for (DataNode<GoWorkspaceData> node : toImport) {
                    GoWorkspaceData data = node.getData();
                    Set<String> names = new HashSet<>(data.getModuleNames());
                    for (Module module : GoModulesProjects.getImportedModules(project, data.getLinkedProjectPath())) {
                        if (!names.contains(module.getName())) {
                            stale.add(module);
                        }
                    }
                }
                if (stale.isEmpty()) {
                    return;
                }
                ModifiableModuleModel moduleModel = ModuleManager.getInstance(project).getModifiableModel();
                for (Module module : stale) {
                    moduleModel.disposeModule(module);
                }
                moduleModel.commit();
            }
        });
    }

    @Override
    public void removeData(Collection<? extends Void> toRemove, Project project, boolean synchronous) {
    }
}

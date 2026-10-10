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

import com.goide.sdk.GoSdkType;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.content.bundle.SdkUtil;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.service.project.manage.ProjectDataService;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.go.module.extension.GoMutableModuleExtension;
import consulo.go.modules.module.GoModulesMutableModuleExtension;
import consulo.go.modules.orderEntry.GoDependencyEntries;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.project.Project;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Turns an imported module into a Go module: switches on the Go extension with an SDK, and records the Go module
 * it was imported from.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModuleDataService implements ProjectDataService<GoModuleData, Module> {
    private static final Logger LOG = Logger.getInstance(GoModuleDataService.class);

    @Override
    public Key<GoModuleData> getTargetDataKey() {
        return GoModuleData.KEY;
    }

    @Override
    @RequiredUIAccess
    public void importData(Collection<DataNode<GoModuleData>> toImport, Project project, boolean synchronous) {
        if (toImport.isEmpty()) {
            return;
        }
        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            @RequiredUIAccess
            public void execute() {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                for (DataNode<GoModuleData> node : toImport) {
                    ModuleData moduleData = node.getData(ProjectKeys.MODULE);
                    Module module = moduleData != null ? moduleManager.findModuleByName(moduleData.getInternalName()) : null;
                    if (module == null || module.isDisposed()) {
                        continue;
                    }
                    DataNode<?> moduleNode = node.getParent();
                    boolean hasDependencies = moduleNode != null && ExternalSystemApiUtil.find(moduleNode, GoDependencyData.KEY) != null;
                    apply(module, node.getData(), hasDependencies);
                }
            }
        });
    }

    @RequiredUIAccess
    private static void apply(Module module, GoModuleData data, boolean hasDependencies) {
        ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
        GoMutableModuleExtension goExtension = rootModel.getExtensionWithoutCheck(GoMutableModuleExtension.class);
        GoModulesMutableModuleExtension modulesExtension = rootModel.getExtensionWithoutCheck(GoModulesMutableModuleExtension.class);
        if (goExtension == null || modulesExtension == null) {
            rootModel.dispose();
            return;
        }

        modulesExtension.setEnabled(true);
        modulesExtension.setModulePath(data.getModulePath());
        modulesExtension.setGoModFile(data.getGoModFile());
        modulesExtension.setGoVersion(data.getGoVersion());
        modulesExtension.setWorkFile(data.getWorkFile());

        goExtension.setEnabled(true);
        // the SDK picked for the linked project wins; without one a module keeps the SDK it has
        if (data.getSdkName() != null || goExtension.getInheritableSdk().isNull()) {
            Sdk sdk = chooseSdk(data);
            if (sdk != null && !sdk.getName().equals(goExtension.getInheritableSdk().getName())) {
                goExtension.getInheritableSdk().set(null, sdk);
            }
        }
        if (!goExtension.getInheritableSdk().isNull() && rootModel.findModuleExtensionSdkEntry(goExtension) == null) {
            rootModel.addModuleExtensionSdkEntry(goExtension);
        }
        if (!hasDependencies) {
            GoDependencyEntries.removeAll(rootModel);
        }

        if (rootModel.isChanged()) {
            rootModel.commit();
        }
        else {
            rootModel.dispose();
        }
    }

    /**
     * @return the SDK named in the settings, else the SDK at the GOROOT of the {@code go} that read the project,
     * created when there is none yet
     */
    @RequiredUIAccess
    private static @Nullable Sdk chooseSdk(GoModuleData data) {
        SdkTable sdkTable = SdkTable.getInstance();
        String sdkName = data.getSdkName();
        if (sdkName != null) {
            Sdk sdk = sdkTable.findSdk(sdkName);
            if (sdk != null) {
                return sdk;
            }
        }

        String goRoot = data.getGoRoot();
        if (StringUtil.isEmpty(goRoot)) {
            return null;
        }
        GoSdkType sdkType = GoSdkType.getInstance();
        Path goRootPath = realPath(goRoot);
        for (Sdk sdk : sdkTable.getSdksOfType(sdkType)) {
            String homePath = sdk.getHomePath();
            if (homePath != null && goRootPath != null && goRootPath.equals(realPath(homePath))) {
                return sdk;
            }
        }
        if (!sdkType.isValidSdkHome(goRoot)) {
            LOG.warn("GOROOT reported by go is not a valid Go SDK home: " + goRoot);
            return null;
        }
        return SdkUtil.createAndAddSDK(goRoot, sdkType, UIAccess.current());
    }

    private static @Nullable Path realPath(String path) {
        try {
            Path nioPath = Path.of(path);
            return Files.exists(nioPath) ? nioPath.toRealPath() : nioPath.toAbsolutePath().normalize();
        }
        catch (IOException | InvalidPathException e) {
            return null;
        }
    }

    @Override
    public void removeData(Collection<? extends Module> toRemove, Project project, boolean synchronous) {
    }
}

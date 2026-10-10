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
import consulo.application.ReadAction;
import consulo.configurable.Configurable;
import consulo.externalSystem.ExternalSystemConfigurableAware;
import consulo.externalSystem.ExternalSystemManager;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.autoimport.CachingExternalSystemAutoImportAware;
import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.externalSystem.ui.ExternalSystemUiAware;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.go.modules.GoCommandRunner;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.GoToolchain;
import consulo.go.modules.setting.GoModulesConfigurable;
import consulo.go.modules.setting.GoModulesExecutionSettings;
import consulo.go.modules.setting.GoModulesLocalSettings;
import consulo.go.modules.setting.GoModulesProjectSettings;
import consulo.go.modules.setting.GoModulesSettings;
import consulo.go.modules.setting.GoModulesSettingsListener;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Go modules as an external system: a linked project is a directory with a {@code go.work} or a {@code go.mod},
 * read and built by the {@code go} binary.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModulesManager implements ExternalSystemAutoImportAware, ExternalSystemConfigurableAware, ExternalSystemUiAware,
    ExternalSystemManager<GoModulesProjectSettings, GoModulesSettingsListener, GoModulesSettings, GoModulesLocalSettings, GoModulesExecutionSettings> {

    private final ProcessHandlerBuilderFactory myProcessHandlerBuilderFactory;
    private final GoModulesAutoImportAware myAutoImportAware = new GoModulesAutoImportAware();
    private final ExternalSystemAutoImportAware myAutoImportDelegate = new CachingExternalSystemAutoImportAware(myAutoImportAware);

    @Inject
    public GoModulesManager(ProcessHandlerBuilderFactory processHandlerBuilderFactory) {
        myProcessHandlerBuilderFactory = processHandlerBuilderFactory;
    }

    @Override
    public ProjectSystemId getSystemId() {
        return GoModulesConstants.SYSTEM_ID;
    }

    @Override
    public Function<Project, GoModulesSettings> getSettingsProvider() {
        return GoModulesSettings::getInstance;
    }

    @Override
    public Function<Project, GoModulesLocalSettings> getLocalSettingsProvider() {
        return GoModulesLocalSettings::getInstance;
    }

    @Override
    public Function<Pair<Project, String>, GoModulesExecutionSettings> getExecutionSettingsProvider() {
        return pair -> {
            Project project = pair.getFirst();
            GoModulesProjectSettings settings = GoModulesSettings.getInstance(project).findLinkedProjectSettingsFor(pair.getSecond());
            String sdkName = settings != null ? settings.getSdkName() : null;
            return new GoModulesExecutionSettings(GoToolchain.find(sdkName),
                sdkName,
                settings == null || settings.isDownloadDependencies(),
                ReadAction.compute(() -> moduleNamesByDirectory(project)));
        };
    }

    private static Map<String, String> moduleNamesByDirectory(Project project) {
        Map<String, String> names = new HashMap<>();
        if (project.isDefault()) {
            return names;
        }
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
                names.putIfAbsent(GoModulesProjectResolver.key(root.getPath()), module.getName());
            }
        }
        return names;
    }

    @Override
    public Supplier<? extends ExternalSystemProjectResolver<GoModulesExecutionSettings>> getProjectResolverFactory() {
        return () -> new GoModulesProjectResolver(new GoCommandRunner(myProcessHandlerBuilderFactory));
    }

    @Override
    public Supplier<? extends ExternalSystemTaskManager<GoModulesExecutionSettings>> getTaskManagerFactory() {
        return () -> new GoModulesTaskManager(new GoCommandRunner(myProcessHandlerBuilderFactory));
    }

    @Override
    public FileChooserDescriptor getExternalProjectDescriptor() {
        return createProjectDescriptor();
    }

    public static FileChooserDescriptor createProjectDescriptor() {
        return new FileChooserDescriptor(true, true, false, false, false, false) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return file.isDirectory() || GoModulesConstants.GO_MOD.equals(file.getName()) || GoModulesConstants.GO_WORK.equals(file.getName());
            }
        };
    }

    @Override
    public Configurable getConfigurable(Project project) {
        return new GoModulesConfigurable(project);
    }

    @Override
    public String getProjectRepresentationName(String targetProjectPath, @Nullable String rootProjectPath) {
        return ExternalSystemApiUtil.getProjectRepresentationName(targetProjectPath, rootProjectPath);
    }

    @Override
    public @Nullable FileChooserDescriptor getExternalProjectConfigDescriptor() {
        return createProjectDescriptor();
    }

    @Override
    public @Nullable String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        return myAutoImportDelegate.getAffectedExternalProjectPath(changedFileOrDirPath, project);
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        return myAutoImportAware.getAffectedExternalProjectFilePaths(projectPath, project);
    }
}

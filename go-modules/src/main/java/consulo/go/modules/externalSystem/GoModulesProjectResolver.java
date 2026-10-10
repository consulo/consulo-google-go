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

import com.goide.sdk.GoSdkUtil;
import com.google.gson.JsonParseException;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ContentRootData;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.project.ModuleDependencyData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationEvent;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.rt.model.ExternalSystemSourceType;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.ProjectData;
import consulo.go.localize.GoLocalize;
import consulo.go.modules.GoCommandRunner;
import consulo.go.modules.GoJson;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.GoModulesProjects;
import consulo.go.modules.GoToolchain;
import consulo.go.modules.setting.GoModulesExecutionSettings;
import consulo.localize.LocalizeValue;
import consulo.process.ExecutionException;
import consulo.util.io.FileUtil;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads a Go modules project with the {@code go} command only:
 * <ul>
 * <li>{@code go env -json} - GOROOT, and whether a {@code go.work} or a {@code go.mod} drives the directory;</li>
 * <li>{@code go list -m -json} - the main modules, which are all the {@code use}s in workspace mode;</li>
 * <li>{@code go mod edit -json} - what each main module requires;</li>
 * <li>{@code go list -m -json all} - the build list, with the directory of every module;</li>
 * <li>{@code go mod download -json} - the directories of the modules the module cache misses.</li>
 * </ul>
 * Every main module becomes an IDE module; every other module of the build list becomes a dependency of the main
 * modules requiring it.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModulesProjectResolver implements ExternalSystemProjectResolver<GoModulesExecutionSettings> {
    /**
     * From this {@code go} directive on, a {@code go.mod} lists every module its packages need, indirect ones included.
     */
    private static final String COMPLETE_REQUIREMENTS_VERSION = "1.17";
    private static final int DOWNLOAD_BATCH_SIZE = 50;

    private final GoCommandRunner myRunner;
    private final Set<ExternalSystemTaskId> myCancelled = ConcurrentHashMap.newKeySet();

    public GoModulesProjectResolver(GoCommandRunner runner) {
        myRunner = runner;
    }

    @Override
    public @Nullable DataNode<ProjectData> resolveProjectInfo(ExternalSystemTaskId id,
                                                            String projectPath,
                                                            boolean isPreviewMode,
                                                            @Nullable GoModulesExecutionSettings settings,
                                                            ExternalSystemTaskNotificationListener listener)
        throws ExternalSystemException, IllegalArgumentException, IllegalStateException {
        try {
            return resolve(id, projectPath, isPreviewMode, settings, listener);
        }
        finally {
            myCancelled.remove(id);
        }
    }

    private DataNode<ProjectData> resolve(ExternalSystemTaskId id,
                                          String projectPath,
                                          boolean isPreviewMode,
                                          @Nullable GoModulesExecutionSettings settings,
                                          ExternalSystemTaskNotificationListener listener) {
        Path root = GoModulesProjects.toDirectory(Path.of(projectPath));
        GoToolchain toolchain = settings != null ? settings.getToolchain() : GoToolchain.fromPath();
        if (toolchain == null) {
            throw new ExternalSystemException(GoLocalize.goModulesGoNotFound().get());
        }

        status(id, listener, GoLocalize.goModulesResolveEnvironment(root.toString()));
        List<String> envArguments = new ArrayList<>(List.of("env", "-json"));
        envArguments.addAll(GoJson.Env.NAMES);
        GoJson.Env env = parseEnv(runChecked(id, toolchain, root, envArguments, listener).stdout());
        String workFile = env.workFile();
        if (workFile == null && env.modFile() == null) {
            throw new ExternalSystemException(GoLocalize.goModulesNoModule(root.toString()).get());
        }

        status(id, listener, GoLocalize.goModulesResolveMainModules());
        List<GoJson.Module> mains = new ArrayList<>();
        for (GoJson.Module module : parseModules(runChecked(id, toolchain, root, List.of("list", "-m", "-e", "-json"), listener).stdout())) {
            if (module.main() && module.dir() != null) {
                mains.add(module);
            }
            else if (module.error() != null) {
                listener.onTaskOutput(id, module.path() + ": " + module.error() + "\n", false);
            }
        }
        if (mains.isEmpty()) {
            throw new ExternalSystemException(GoLocalize.goModulesNoModule(root.toString()).get());
        }

        Map<String, GoJson.ModFile> modFiles = new LinkedHashMap<>();
        for (GoJson.Module main : mains) {
            Path directory = Path.of(main.dir());
            GoCommandRunner.Result result = run(id, toolchain, directory, List.of("mod", "edit", "-json", goModOf(main)), listener);
            modFiles.put(main.path(), result.isSuccess() ? parseModFile(result.stdout()) : new GoJson.ModFile(main.path(), null, null, List.of()));
        }

        List<GoJson.Module> dependencies = List.of();
        if (!isPreviewMode) {
            status(id, listener, GoLocalize.goModulesResolveBuildList());
            dependencies = listBuildList(id, toolchain, root, listener);
            if (settings == null || settings.isDownloadDependencies()) {
                dependencies = download(id, toolchain, root, dependencies, listener);
            }
        }

        return createProjectNode(projectPath, root, env, workFile, mains, modFiles, dependencies, settings);
    }

    private List<GoJson.Module> listBuildList(ExternalSystemTaskId id, GoToolchain toolchain, Path root, ExternalSystemTaskNotificationListener listener) {
        GoCommandRunner.Result result = run(id, toolchain, root, List.of("list", "-m", "-e", "-json", "all"), listener);
        List<GoJson.Module> dependencies = nonMain(parseModules(result.stdout()));
        if (dependencies.isEmpty() && !result.isSuccess() && result.stderr().contains(GoModulesConstants.VENDOR)) {
            // a vendored module cannot compute 'all' from vendor/modules.txt, the module graph can
            result = run(id, toolchain, root, List.of("list", "-mod=readonly", "-m", "-e", "-json", "all"), listener);
            dependencies = nonMain(parseModules(result.stdout()));
        }
        return dependencies;
    }

    private static List<GoJson.Module> nonMain(List<GoJson.Module> modules) {
        List<GoJson.Module> result = new ArrayList<>();
        for (GoJson.Module module : modules) {
            if (!module.main()) {
                result.add(module);
            }
        }
        return result;
    }

    /**
     * {@code go list} gives no directory to a module that is not extracted in the module cache, or whose hash is not in
     * {@code go.sum} yet; {@code go mod download} fetches it when needed and prints where it lives.
     */
    private List<GoJson.Module> download(ExternalSystemTaskId id,
                                         GoToolchain toolchain,
                                         Path root,
                                         List<GoJson.Module> dependencies,
                                         ExternalSystemTaskNotificationListener listener) {
        List<String> keys = new ArrayList<>();
        for (GoJson.Module dependency : dependencies) {
            GoJson.Module target = downloadTarget(dependency);
            if (target != null) {
                keys.add(target.key());
            }
        }
        if (keys.isEmpty()) {
            return dependencies;
        }

        status(id, listener, GoLocalize.goModulesResolveDownload(keys.size()));
        Map<String, String> directories = new HashMap<>();
        for (int start = 0; start < keys.size(); start += DOWNLOAD_BATCH_SIZE) {
            List<String> arguments = new ArrayList<>(List.of("mod", "download", "-json"));
            arguments.addAll(keys.subList(start, Math.min(keys.size(), start + DOWNLOAD_BATCH_SIZE)));
            for (GoJson.Module downloaded : parseModules(run(id, toolchain, root, arguments, listener).stdout())) {
                if (downloaded.dir() != null) {
                    directories.put(downloaded.key(), downloaded.dir());
                }
                else if (downloaded.error() != null) {
                    listener.onTaskOutput(id, downloaded.key() + ": " + downloaded.error() + "\n", false);
                }
            }
        }

        List<GoJson.Module> result = new ArrayList<>(dependencies.size());
        for (GoJson.Module dependency : dependencies) {
            GoJson.Module target = downloadTarget(dependency);
            String directory = target != null ? directories.get(target.key()) : null;
            result.add(directory != null ? dependency.withDir(directory) : dependency);
        }
        return result;
    }

    private static GoJson.@Nullable Module downloadTarget(GoJson.Module dependency) {
        if (dependency.sourceDir() != null || dependency.error() != null) {
            return null;
        }
        GoJson.Module target = dependency.replace() != null ? dependency.replace() : dependency;
        // a replacement by a directory has no version and nothing to download
        return target.version() != null ? target : null;
    }

    private static DataNode<ProjectData> createProjectNode(String projectPath,
                                                           Path root,
                                                           GoJson.Env env,
                                                           @Nullable String workFile,
                                                           List<GoJson.Module> mains,
                                                           Map<String, GoJson.ModFile> modFiles,
                                                           List<GoJson.Module> dependencies,
                                                           @Nullable GoModulesExecutionSettings settings) {
        String projectName = root.getFileName() != null ? root.getFileName().toString() : root.toString();
        ProjectData projectData = new ProjectData(GoModulesConstants.SYSTEM_ID, projectName, root.toString(), projectPath);
        DataNode<ProjectData> projectNode = new DataNode<>(ProjectKeys.PROJECT, projectData, null);

        Map<String, String> names = moduleNames(mains, root, settings != null ? settings.getModuleNames() : Map.of());
        Map<String, DataNode<ModuleData>> moduleNodes = new LinkedHashMap<>();
        Map<String, String> mainByDirectory = new HashMap<>();
        Path workDirectory = workFile != null ? Path.of(workFile).getParent() : null;
        boolean workTaskAdded = false;
        for (GoJson.Module main : mains) {
            Path directory = Path.of(main.dir()).normalize();
            String goMod = goModOf(main);

            ModuleData moduleData = new ModuleData(main.path(), GoModulesConstants.SYSTEM_ID, names.get(main.path()), directory.toString(), goMod);
            DataNode<ModuleData> moduleNode = projectNode.createChild(ProjectKeys.MODULE, moduleData);
            moduleNodes.put(main.path(), moduleNode);
            mainByDirectory.put(key(directory.toString()), main.path());

            ContentRootData contentRoot = new ContentRootData(GoModulesConstants.SYSTEM_ID, directory.toString());
            contentRoot.storePath(ExternalSystemSourceType.SOURCE, directory.toString());
            moduleNode.createChild(ProjectKeys.CONTENT_ROOT, contentRoot);

            GoJson.ModFile modFile = modFiles.get(main.path());
            String goVersion = modFile != null && modFile.goVersion() != null ? modFile.goVersion() : main.goVersion();
            moduleNode.createChild(GoModuleData.KEY, new GoModuleData(main.path(),
                goMod,
                goVersion,
                workFile,
                env.goRoot(),
                settings != null ? settings.getSdkName() : null,
                env.goVersion()));

            for (String taskName : GoModulesTasks.MODULE_TASKS) {
                addTask(moduleNode, taskName, directory);
            }
            if (workDirectory != null && directory.equals(workDirectory)) {
                addTask(moduleNode, GoModulesTasks.WORK_SYNC, workDirectory);
                workTaskAdded = true;
            }
        }
        if (workDirectory != null && !workTaskAdded) {
            addTask(moduleNodes.values().iterator().next(), GoModulesTasks.WORK_SYNC, workDirectory);
        }

        List<GoJson.Module> sortedDependencies = new ArrayList<>(dependencies);
        sortedDependencies.sort(Comparator.comparing(GoJson.Module::path));
        for (GoJson.Module main : mains) {
            DataNode<ModuleData> moduleNode = moduleNodes.get(main.path());
            if (moduleNode == null) {
                continue;
            }
            ModuleData owner = moduleNode.getData();
            Set<String> dependsOn = new HashSet<>();

            GoJson.ModFile modFile = modFiles.get(main.path());
            if (modFile != null) {
                for (GoJson.Require require : modFile.requires()) {
                    addModuleDependency(moduleNode, owner, moduleNodes.get(require.path()), dependsOn);
                }
            }

            Set<String> required = requiredModules(main.path(), modFiles);
            for (GoJson.Module dependency : sortedDependencies) {
                if (required != null && !required.contains(dependency.path())) {
                    continue;
                }
                String directory = dependency.sourceDir();
                String mainOnDirectory = directory != null ? mainByDirectory.get(key(Path.of(directory).normalize().toString())) : null;
                if (mainOnDirectory != null) {
                    // replaced by a module of the project itself
                    addModuleDependency(moduleNode, owner, moduleNodes.get(mainOnDirectory), dependsOn);
                    continue;
                }
                moduleNode.createChild(GoDependencyData.KEY, new GoDependencyData(dependency.path(),
                    dependency.version(),
                    directory,
                    replacement(dependency),
                    dependency.indirect(),
                    dependency.error()));
            }
        }

        projectNode.createChild(GoWorkspaceData.KEY, new GoWorkspaceData(projectPath, workFile, names.values()));
        return projectNode;
    }

    private static void addModuleDependency(DataNode<ModuleData> moduleNode,
                                            ModuleData owner,
                                            @Nullable DataNode<ModuleData> target,
                                            Set<String> dependsOn) {
        if (target == null || target.getData() == owner || !dependsOn.add(target.getData().getId())) {
            return;
        }
        moduleNode.createChild(ProjectKeys.MODULE_DEPENDENCY, new ModuleDependencyData(owner, target.getData()));
    }

    /**
     * @return the module paths {@code mainPath} needs, following the main modules it requires;
     * {@code null} when a {@code go.mod} on the way is too old to list them all and the whole build list has to do
     */
    private static @Nullable Set<String> requiredModules(String mainPath, Map<String, GoJson.ModFile> modFiles) {
        Set<String> result = new LinkedHashSet<>();
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(mainPath);
        while (!queue.isEmpty()) {
            String path = queue.poll();
            GoJson.ModFile modFile = modFiles.get(path);
            if (modFile == null || !visited.add(path)) {
                continue;
            }
            String goVersion = modFile.goVersion();
            if (goVersion == null || GoSdkUtil.compareVersions(goVersion, COMPLETE_REQUIREMENTS_VERSION) < 0) {
                return null;
            }
            for (GoJson.Require require : modFile.requires()) {
                result.add(require.path());
                if (modFiles.containsKey(require.path())) {
                    queue.add(require.path());
                }
            }
        }
        return result;
    }

    private static @Nullable String replacement(GoJson.Module dependency) {
        GoJson.Module replace = dependency.replace();
        if (replace == null) {
            return null;
        }
        return replace.version() != null ? replace.path() + " " + replace.version() : replace.path();
    }

    /**
     * Names every main module after its directory. A module the user already has on that directory keeps its name, so
     * the import adopts it instead of adding a second module on the same content root.
     */
    private static Map<String, String> moduleNames(List<GoJson.Module> mains, Path root, Map<String, String> existingNames) {
        Map<String, String> assigned = new HashMap<>();
        Set<String> used = new HashSet<>();
        for (GoJson.Module main : mains) {
            String existing = existingNames.get(key(Path.of(main.dir()).normalize().toString()));
            if (existing != null && used.add(existing)) {
                assigned.put(main.path(), existing);
            }
        }
        for (GoJson.Module main : mains) {
            if (assigned.containsKey(main.path())) {
                continue;
            }
            Path directory = Path.of(main.dir()).normalize();
            String candidate = directory.getFileName() != null ? directory.getFileName().toString() : main.path();
            if (used.contains(candidate) && directory.startsWith(root) && !directory.equals(root)) {
                candidate = FileUtil.toSystemIndependentName(root.relativize(directory).toString()).replace('/', '.');
            }
            String unique = candidate;
            for (int i = 2; !used.add(unique); i++) {
                unique = candidate + "~" + i;
            }
            assigned.put(main.path(), unique);
        }

        Map<String, String> names = new LinkedHashMap<>();
        for (GoJson.Module main : mains) {
            names.put(main.path(), assigned.get(main.path()));
        }
        return names;
    }

    private static void addTask(DataNode<ModuleData> moduleNode, String taskName, Path directory) {
        TaskData task = new TaskData(GoModulesConstants.SYSTEM_ID, taskName, directory.toString(), GoModulesTasks.description(taskName).get());
        task.setGroup(GoModulesTasks.group(taskName).get());
        moduleNode.createChild(ProjectKeys.TASK, task);
    }

    private static String goModOf(GoJson.Module main) {
        return main.goMod() != null ? main.goMod() : Path.of(main.dir()).resolve(GoModulesConstants.GO_MOD).toString();
    }

    /**
     * @return a path as the key of directories compared across {@code go} output and the IDE
     */
    public static String key(String path) {
        return FileUtil.toSystemIndependentName(path);
    }

    private GoCommandRunner.Result runChecked(ExternalSystemTaskId id,
                                              GoToolchain toolchain,
                                              Path directory,
                                              List<String> arguments,
                                              ExternalSystemTaskNotificationListener listener) {
        GoCommandRunner.Result result = run(id, toolchain, directory, arguments, listener);
        // with -e, go reports broken modules in the output and still exits with an error
        if (!result.isSuccess() && result.stdout().isBlank()) {
            throw new ExternalSystemException(GoLocalize.goModulesCommandFailed("go " + String.join(" ", arguments), result.stderr().trim()).get());
        }
        return result;
    }

    private GoCommandRunner.Result run(ExternalSystemTaskId id,
                                       GoToolchain toolchain,
                                       Path directory,
                                       List<String> arguments,
                                       ExternalSystemTaskNotificationListener listener) {
        if (myCancelled.contains(id)) {
            throw new ExternalSystemException(GoLocalize.goModulesResolveCancelled().get());
        }
        listener.onTaskOutput(id, "go " + String.join(" ", arguments) + "\n", true);
        try {
            GoCommandRunner.Result result = myRunner.run(id, toolchain.createCommandLine(directory, arguments),
                (text, stdout) -> listener.onTaskOutput(id, text, stdout));
            if (myCancelled.contains(id)) {
                throw new ExternalSystemException(GoLocalize.goModulesResolveCancelled().get());
            }
            return result;
        }
        catch (ExecutionException e) {
            throw new ExternalSystemException(e);
        }
    }

    private static void status(ExternalSystemTaskId id, ExternalSystemTaskNotificationListener listener, LocalizeValue text) {
        listener.onStatusChange(new ExternalSystemTaskNotificationEvent(id, text.get()));
    }

    private static GoJson.Env parseEnv(String text) {
        try {
            return GoJson.parseEnv(text);
        }
        catch (JsonParseException e) {
            throw new ExternalSystemException(e);
        }
    }

    private static List<GoJson.Module> parseModules(String text) {
        try {
            return GoJson.parseModules(text);
        }
        catch (JsonParseException e) {
            throw new ExternalSystemException(e);
        }
    }

    private static GoJson.ModFile parseModFile(String text) {
        try {
            return GoJson.parseModFile(text);
        }
        catch (JsonParseException e) {
            throw new ExternalSystemException(e);
        }
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId taskId, ExternalSystemTaskNotificationListener listener) {
        myCancelled.add(taskId);
        myRunner.cancel(taskId);
        return true;
    }
}

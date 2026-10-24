package com.goide.compiler;

import com.goide.runconfig.application.GoApplicationConfiguration;
import com.goide.util.GoConsoleFilter;
import com.goide.util.GoExecutor;
import com.goide.util.GoHistoryProcessListener;
import com.goide.util.GoPackageUtil;
import com.goide.util.GoPathResolveScope;
import com.goide.util.GoUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.application.progress.ProgressIndicator;
import consulo.build.ui.FilePosition;
import consulo.build.ui.event.MessageEvent;
import consulo.build.ui.progress.BuildProgress;
import consulo.build.ui.progress.BuildProgressDescriptor;
import consulo.compiler.*;
import consulo.compiler.scope.CompileScope;
import consulo.content.bundle.Sdk;
import consulo.dataContext.DataContext;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.go.GoEnvironmentUtil;
import consulo.go.localize.GoLocalize;
import consulo.go.module.extension.GoModuleExtension;
import consulo.go.modules.GoToolchain;
import consulo.go.modules.externalSystem.GoModulesTasks;
import consulo.go.modules.module.GoModulesModuleExtension;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FilenameIndex;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.ProcessOutputType;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;
import consulo.util.io.FileUtil;
import consulo.util.io.PathUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * @author VISTALL
 * @since 2026-02-24
 */
@ExtensionImpl
public class GoCompilerRunner implements CompilerRunner {
    public static final Key<File> OUTPUT_FILE = Key.create("GoCompilerRunner#OUTPUT_FILE");

    private static final YesResult YES = new YesResult(PlatformIconGroup.actionsCompile());
    private static final long WAIT_INTERVAL = 100L;

    private final ModuleExtensionHelper myModuleExtensionHelper;
    private final ProcessHandlerBuilderFactory myProcessHandlerBuilderFactory;

    @Inject
    public GoCompilerRunner(ModuleExtensionHelper moduleExtensionHelper, ProcessHandlerBuilderFactory processHandlerBuilderFactory) {
        myModuleExtensionHelper = moduleExtensionHelper;
        myProcessHandlerBuilderFactory = processHandlerBuilderFactory;
    }

    @Override
    public LocalizeValue getName() {
        return GoLocalize.goCompilerName();
    }

    /**
     * Builds the binary of a Go application configuration; without a run configuration, every imported Go module.
     */
    @RequiredReadAction
    @Override
    public Result checkAvailable(DataContext dataContext) {
        RunConfiguration runConfiguration = dataContext.getData(RunConfiguration.KEY);
        if (runConfiguration != null) {
            return runConfiguration instanceof GoApplicationConfiguration ? YES : NO;
        }
        return myModuleExtensionHelper.hasModuleExtension(GoModulesModuleExtension.class) ? YES : NO;
    }

    @Override
    public boolean build(
        CompileDriver compileDriver,
        CompileContextEx context,
        BuildProgress<BuildProgressDescriptor> buildProgress,
        boolean isRebuild,
        boolean forceCompile,
        boolean onlyCheckStatus
    ) throws ExitException {
        CompileScope compileScope = context.getCompileScope();

        RunConfiguration configuration = compileScope.getUserData(RunConfiguration.KEY);
        if (!(configuration instanceof GoApplicationConfiguration goRunConfiguration)) {
            return !onlyCheckStatus && buildGoModules(context, isRebuild);
        }

        ExecutionEnvironment env = Objects.requireNonNull(compileScope.getUserData(ExecutionEnvironment.KEY));

        File outputFile;
        try {
            outputFile = getOutputFile(goRunConfiguration);
        }
        catch (ExecutionException e) {
            context.newError(LocalizeValue.ofNullable(e.getMessage())).add();
            return false;
        }

        Module module = goRunConfiguration.getConfigurationModule().getModule();
        if (module == null) {
            context.newError(LocalizeValue.localizeTODO("Module for " + goRunConfiguration.getName() + " is not set")).add();
            return false;
        }

        context.getProgressIndicator().setText("go build");

        GoHistoryProcessListener historyProcessListener = new GoHistoryProcessListener();

        boolean isDebug = DefaultDebugExecutor.EXECUTOR_ID.equals(env.getExecutor().getId());
        boolean result = GoExecutor.in(module).withWorkDirectory(goRunConfiguration.getWorkingDirectory())
            .withExtraEnvironment(goRunConfiguration.getCustomEnvironment())
            .withPassParentEnvironment(goRunConfiguration.isPassParentEnvironment())
            .withParameters("build")
            .withParameterString(goRunConfiguration.getGoToolParams())
            .withParameters("-o", outputFile.getAbsolutePath())
            .withParameters(isDebug ? new String[]{"-gcflags", "-N -l"} : ArrayUtil.EMPTY_STRING_ARRAY)
            .withParameters(goRunConfiguration.getTarget())
            .withPresentableName("go build")
            .withProcessListener(historyProcessListener)
            .withProcessListener(new ProcessListener() {
                @Override
                public void onTextAvailable(ProcessEvent event, Key outputType) {
                    String text = event.getText();

                    if (!tryParse(text, goRunConfiguration.getConfigurationModule().getModule(), buildProgress)) {
                        if (outputType == ProcessOutputTypes.STDERR) {
                            buildProgress.output(text, false);
                        }
                        else {
                            buildProgress.output(text, true);
                        }
                    }
                }

            })
            .execute();

        if (result) {
            env.putUserData(OUTPUT_FILE, outputFile);
        }
        return result;
    }

    /**
     * Runs {@code go build ./...} in the directory of every Go module of the compile scope. With several packages
     * {@code go build} writes no binaries, it only compiles: exactly what building a module means.
     */
    private boolean buildGoModules(CompileContextEx context, boolean isRebuild) throws ExitException {
        Map<Path, GoToolchain> modules = ReadAction.compute(() -> {
            Map<Path, GoToolchain> result = new LinkedHashMap<>();
            for (Module module : context.getCompileScope().getAffectedModules()) {
                GoModulesModuleExtension extension = GoModulesModuleExtension.find(module);
                Path directory = extension != null ? extension.getModuleDirectory() : null;
                if (directory == null || result.containsKey(directory)) {
                    continue;
                }
                Sdk sdk = ModuleUtilCore.getSdk(module, GoModuleExtension.class);
                GoToolchain toolchain = sdk != null ? GoToolchain.fromSdk(sdk) : null;
                result.put(directory, toolchain != null ? toolchain : GoToolchain.fromPath());
            }
            return result;
        });

        boolean didSomething = false;
        for (Map.Entry<Path, GoToolchain> entry : modules.entrySet()) {
            Path directory = entry.getKey();
            GoToolchain toolchain = entry.getValue();
            if (toolchain == null) {
                context.newError(GoLocalize.goModulesGoNotFound()).add();
                continue;
            }
            List<String> arguments = isRebuild
                ? List.of(GoModulesTasks.BUILD, "-a", GoModulesTasks.ALL_PACKAGES)
                : List.of(GoModulesTasks.BUILD, GoModulesTasks.ALL_PACKAGES);
            runGoBuild(context, toolchain.createCommandLine(directory, arguments), directory);
            didSomething = true;
        }
        return didSomething;
    }

    private void runGoBuild(CompileContextEx context, GeneralCommandLine commandLine, Path directory) throws ExitException {
        ProgressIndicator indicator = context.getProgressIndicator();
        indicator.setText(GoLocalize.goCompilerProgress(commandLine.getCommandLineString(), directory.toString()));

        ProcessHandler handler;
        try {
            handler = myProcessHandlerBuilderFactory.newBuilder(commandLine).killable().build();
        }
        catch (ExecutionException e) {
            context.newError(LocalizeValue.ofNullable(e.getMessage())).add();
            return;
        }

        int errorsBefore = context.getMessageCount(CompilerMessageCategory.ERROR);
        StringBuilder output = new StringBuilder();
        handler.addProcessListener(new ProcessListener() {
            @Override
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if (ProcessOutputType.isStdout(outputType) || ProcessOutputType.isStderr(outputType)) {
                    output.append(event.getText());
                    for (String line : StringUtil.splitByLines(event.getText())) {
                        addMessage(context, line, directory);
                    }
                }
            }
        });
        handler.startNotify();
        while (!handler.waitFor(WAIT_INTERVAL)) {
            if (indicator.isCanceled()) {
                handler.destroyProcess();
                handler.waitFor();
                throw new ExitException(ExitStatus.CANCELLED);
            }
        }

        Integer exitCode = handler.getExitCode();
        if (exitCode != null && exitCode != 0 && context.getMessageCount(CompilerMessageCategory.ERROR) == errorsBefore) {
            context.newError(GoLocalize.goCompilerFailed(commandLine.getCommandLineString(), exitCode, output.toString().trim())).add();
        }
    }

    /**
     * Reports a {@code file.go:line:column: message} line of {@code go build}; paths are relative to the module directory.
     */
    private static void addMessage(CompileContextEx context, String line, Path directory) {
        Matcher matcher = GoConsoleFilter.MESSAGE_PATTERN.matcher(line);
        if (!matcher.find()) {
            return;
        }
        String fileName = matcher.group(1);
        int lineNumber = StringUtil.parseInt(matcher.group(2), -1);
        if (lineNumber <= 0) {
            return;
        }
        int columnNumber = matcher.group(4) != null ? StringUtil.parseInt(matcher.group(4), -1) : -1;
        int messageStart = Math.max(matcher.end(2), matcher.group(4) != null ? matcher.end(4) : -1);
        String message = line.substring(Math.min(line.length(), messageStart + 1)).trim();

        Path file = directory.resolve(fileName).normalize();
        context.newError(LocalizeValue.of(message.isEmpty() ? line.trim() : message))
            .url(VirtualFileUtil.pathToUrl(FileUtil.toSystemIndependentName(file.toString())))
            .position(lineNumber, columnNumber)
            .add();
    }

    private static File getOutputFile(GoApplicationConfiguration goRunConfiguration) throws ExecutionException {
        File outputFile;
        String outputDirectoryPath = goRunConfiguration.getOutputDirectory();
        String configurationName = goRunConfiguration.getName();
        if (StringUtil.isEmpty(outputDirectoryPath)) {
            try {
                outputFile = FileUtil.createTempFile(configurationName, "go", true);
            }
            catch (IOException e) {
                throw new ExecutionException("Cannot create temporary output file", e);
            }
        }
        else {
            File outputDirectory = new File(outputDirectoryPath);
            if (outputDirectory.isDirectory() || !outputDirectory.exists() && outputDirectory.mkdirs()) {
                outputFile = new File(outputDirectoryPath, GoEnvironmentUtil.getBinaryFileNameForPath(configurationName));
                try {
                    if (!outputFile.exists() && !outputFile.createNewFile()) {
                        throw new ExecutionException("Cannot create output file " + outputFile.getAbsolutePath());
                    }
                }
                catch (IOException e) {
                    throw new ExecutionException("Cannot create output file " + outputFile.getAbsolutePath());
                }
            }
            else {
                throw new ExecutionException("Cannot create output file in " + outputDirectory.getAbsolutePath());
            }
        }
        if (!prepareFile(outputFile)) {
            throw new ExecutionException("Cannot make temporary file executable " + outputFile.getAbsolutePath());
        }
        return outputFile;
    }

    private static boolean prepareFile(File file) {
        try {
            FileUtil.writeToFile(file, new byte[]{0x7F, 'E', 'L', 'F'});
        }
        catch (IOException e) {
            return false;
        }
        return file.setExecutable(true);
    }

    private static boolean tryParse(String line, Module module, BuildProgress<BuildProgressDescriptor> buildProgress) {
        String moduleDirUrl = module.getModuleDirUrl();

        Matcher matcher = GoConsoleFilter.MESSAGE_PATTERN.matcher(line);
        if (!matcher.find()) {
            return false;
        }

        int startOffset = matcher.start(1);
        int endOffset = matcher.end(2);

        String fileName = matcher.group(1);
        int lineNumber = StringUtil.parseInt(matcher.group(2), 1) - 1;
        if (lineNumber < 0) {
            return false;
        }

        int columnNumber = -1;
        if (matcher.groupCount() > 3) {
            columnNumber = StringUtil.parseInt(matcher.group(4), 1) - 1;
            endOffset = Math.max(endOffset, matcher.end(4));
        }

        VirtualFile virtualFile = null;
        if (FileUtil.isAbsolutePlatformIndependent(fileName)) {
            virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByUrl(VirtualFileUtil.pathToUrl(fileName));
        }
        else {
            if (moduleDirUrl != null) {
                virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByUrl(moduleDirUrl + "/" + fileName);
            }
            if (virtualFile == null && module != null) {
                virtualFile = findInGoPath(fileName, module);
                if (virtualFile == null && fileName.startsWith("src/")) {
                    virtualFile = findInGoPath(StringUtil.trimStart(fileName, "src/"), module);
                }
            }
            if (virtualFile == null) {
                VirtualFile baseDir = module.getProject().getBaseDir();
                if (baseDir != null) {
                    virtualFile = baseDir.findFileByRelativePath(fileName);
                    if (virtualFile == null && fileName.startsWith("src/")) {
                        // exclude src
                        virtualFile = baseDir.findFileByRelativePath(StringUtil.trimStart(fileName, "src/"));
                    }
                }
            }
        }
        if (virtualFile == null) {
            virtualFile = findSingleFile(fileName, module);
        }

        if (virtualFile == null) {
            return false;
        }

        String message = line.substring(endOffset + 1, line.length()).trim();

        buildProgress.fileMessage(
            LocalizeValue.localizeTODO("Error"),
            LocalizeValue.of(message),
            MessageEvent.Kind.ERROR,
            new FilePosition(virtualFile.toNioPath().toFile(), lineNumber, columnNumber)
        );

        return true;
    }

    @Nullable
    private static VirtualFile findInGoPath(String fileName, Module module) {
        return GoPackageUtil.findByImportPath(fileName, module.getProject(), module);
    }

    @Nullable
    private static VirtualFile findSingleFile(String fileName, Module module) {
        Project project = module.getProject();
        if (PathUtil.isValidFileName(fileName)) {
            Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(project, fileName, GlobalSearchScope.allScope(project));
            if (files.size() == 1) {
                return ContainerUtil.getFirstItem(files);
            }
            if (!files.isEmpty()) {
                GlobalSearchScope goPathScope = GoPathResolveScope.create(project, module, null);
                files = ContainerUtil.filter(files, goPathScope::accept);
                if (files.size() == 1) {
                    return ContainerUtil.getFirstItem(files);
                }
            }
            if (!files.isEmpty()) {
                GlobalSearchScope smallerScope = GoUtil.moduleScopeWithoutLibraries(project, module);
                files = ContainerUtil.filter(files, smallerScope::accept);
                if (files.size() == 1) {
                    return ContainerUtil.getFirstItem(files);
                }
            }
        }
        return null;
    }
}

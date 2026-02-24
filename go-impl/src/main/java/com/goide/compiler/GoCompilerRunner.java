package com.goide.compiler;

import com.goide.GoEnvironmentUtil;
import com.goide.runconfig.GoConsoleFilter;
import com.goide.runconfig.GoRunConfigurationBase;
import com.goide.runconfig.application.GoApplicationConfiguration;
import com.goide.sdk.GoPackageUtil;
import com.goide.util.GoExecutor;
import com.goide.util.GoHistoryProcessListener;
import com.goide.util.GoPathResolveScope;
import com.goide.util.GoUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.build.ui.FilePosition;
import consulo.build.ui.event.MessageEvent;
import consulo.build.ui.progress.BuildProgress;
import consulo.build.ui.progress.BuildProgressDescriptor;
import consulo.compiler.*;
import consulo.compiler.scope.CompileScope;
import consulo.dataContext.DataContext;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FilenameIndex;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.ExecutionException;
import consulo.process.ProcessOutputTypes;
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
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

/**
 * @author VISTALL
 * @since 2026-02-24
 */
@ExtensionImpl
public class GoCompilerRunner implements CompilerRunner {
    public static final Key<File> OUTPUT_FILE = Key.create("GoCompilerRunner#OUTPUT_FILE");

    private static final YesResult YES = new YesResult(PlatformIconGroup.actionsCompile());

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return LocalizeValue.localizeTODO("Go Build");
    }

    @Nonnull
    @Override
    public Result checkAvailable(@Nonnull DataContext dataContext) {
        RunConfiguration runConfiguration = dataContext.getData(RunConfiguration.KEY);
        if (runConfiguration instanceof GoApplicationConfiguration) {
            return YES;
        }
        return NO;
    }

    @Override
    public boolean build(CompileDriver compileDriver,
                         CompileContextEx context,
                         BuildProgress<BuildProgressDescriptor> buildProgress,
                         boolean isRebuild,
                         boolean forceCompile,
                         boolean onlyCheckStatus) throws ExitException {
        CompileScope compileScope = context.getCompileScope();

        RunConfiguration configuration = compileScope.getUserData(RunConfiguration.KEY);
        if (!(configuration instanceof GoApplicationConfiguration goRunConfiguration)) {
            return false;
        }

        ExecutionEnvironment env = Objects.requireNonNull(compileScope.getUserData(ExecutionEnvironment.KEY));

        File outputFile;
        try {
            outputFile = getOutputFile(goRunConfiguration);
        }
        catch (ExecutionException e) {
            context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, 0, 0);
            return false;
        }

        Module module = goRunConfiguration.getConfigurationModule().getModule();
        if (module == null) {
            context.addMessage(CompilerMessageCategory.ERROR, "Module for " + goRunConfiguration.getName() + " is not set", null, 0, 0);
            return false;
        }

        context.getProgressIndicator().setText("go build");

        CompletableFuture<Boolean> future = new CompletableFuture<>();

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

                @Override
                public void processTerminated(ProcessEvent event) {
                    boolean compilationFailed = event.getExitCode() != 0;

                    future.complete(!compilationFailed);
                }
            })
            .execute();

        if (result) {
            env.putUserData(OUTPUT_FILE, outputFile);
        }
        return result;
    }

    @Nonnull
    private static File getOutputFile(@Nonnull GoRunConfigurationBase goRunConfiguration) throws ExecutionException {
        File outputFile;
        String outputDirectoryPath = goRunConfiguration.getOutputFilePath();
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

    private static boolean prepareFile(@Nonnull File file) {
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

        buildProgress.fileMessage("Error", message, MessageEvent.Kind.ERROR, new FilePosition(virtualFile.toNioPath().toFile(), lineNumber, columnNumber));

        return true;
    }

    @Nullable
    private static VirtualFile findInGoPath(@Nonnull String fileName, @Nonnull Module module) {
        return GoPackageUtil.findByImportPath(fileName, module.getProject(), module);
    }

    @Nullable
    private static VirtualFile findSingleFile(@Nonnull String fileName, @Nonnull Module module) {
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

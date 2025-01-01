package com.goide.compiler;

import com.goide.GoFileType;
import com.goide.runconfig.GoConsoleFilter;
import com.goide.sdk.GoPackageUtil;
import com.goide.util.GoExecutor;
import com.goide.util.GoPathResolveScope;
import com.goide.util.GoUtil;
import consulo.compiler.CompileContext;
import consulo.compiler.CompilerMessageCategory;
import consulo.compiler.ModuleCompilerPathsManager;
import consulo.compiler.TranslatingCompiler;
import consulo.compiler.scope.CompileScope;
import consulo.execution.configuration.RunConfiguration;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FilenameIndex;
import consulo.module.Module;
import consulo.platform.Platform;
import consulo.platform.PlatformOperatingSystem;
import consulo.process.ProcessOutputTypes;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.project.Project;
import consulo.util.collection.Chunk;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;
import consulo.util.io.FileUtil;
import consulo.util.io.PathUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author VISTALL
 * @since 2025-01-01
 */
//@ExtensionImpl
public class GoCompiler implements TranslatingCompiler {
    public static boolean ENABLE_COMPILER = false;

    private static record Output(String output, VirtualFile sourceFile) implements OutputItem {

        @Override
        public String getOutputPath() {
            return output;
        }

        @Override
        public VirtualFile getSourceFile() {
            return sourceFile;
        }
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Goo Compiler";
    }

    @Override
    public boolean validateConfiguration(CompileScope compileScope) {
        return true;
    }

    @Override
    public boolean isCompilableFile(VirtualFile virtualFile, CompileContext compileContext) {
        return virtualFile.getFileType() == GoFileType.INSTANCE;
    }

    @Override
    public void compile(CompileContext compileContext, Chunk<Module> chunk, VirtualFile[] virtualFiles, OutputSink outputSink) {
        CompileScope compileScope = compileContext.getCompileScope();

        PlatformOperatingSystem os = Platform.current().os();

        RunConfiguration data = compileContext.getUserData(RunConfiguration.DATA_KEY);
        if (data != null) {

        }
        else {
            for (Module module : chunk.getNodes()) {
                String compilerOutputUrl = ModuleCompilerPathsManager.getInstance(module).getCompilerOutputUrl(ProductionContentFolderTypeProvider.getInstance());

                String path = VirtualFileUtil.urlToPath(compilerOutputUrl);

                String fileName = os.isWindows() ? module.getName() + ".exe" : module.getName();

                String outputFile = path + "/" + fileName;

                GoExecutor.in(module)
                    .withWorkDirectory(module.getModuleDirPath())
                    .withParameters("build")
                    .withParameters("-o")
                    .withParameters(path + "/")
                    .withParameters("-gcflags", "-N -l")
                    .withProcessListener(new ProcessListener() {
                        @Override
                        public void onTextAvailable(ProcessEvent event, Key outputType) {
                            if (outputType == ProcessOutputTypes.STDERR) {
                                tryParse(event.getText(), module, compileContext);
                            }
                        }
                    })
                    .execute();

                outputSink.add(path, List.of(new Output(outputFile, module.getModuleDir())), new VirtualFile[0]);
            }
        }
    }

    private static void tryParse(String line, Module module, CompileContext context) {
        String moduleDirUrl = module.getModuleDirUrl();

        Matcher matcher = GoConsoleFilter.MESSAGE_PATTERN.matcher(line);
        if (!matcher.find()) {
            return;
        }

        int startOffset = matcher.start(1);
        int endOffset = matcher.end(2);

        String fileName = matcher.group(1);
        int lineNumber = StringUtil.parseInt(matcher.group(2), 1) - 1;
        if (lineNumber < 0) {
            return;
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
            return;
        }

        String message = line.substring(endOffset + 1, line.length()).trim();

        context.addMessage(CompilerMessageCategory.ERROR, message, virtualFile.getUrl(), lineNumber + 1, columnNumber + 1);
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

    @Nonnull
    @Override
    public FileType[] getInputFileTypes() {
        return new FileType[]{GoFileType.INSTANCE};
    }

    @Nonnull
    @Override
    public FileType[] getOutputFileTypes() {
        return new FileType[]{GoFileType.INSTANCE};
    }
}

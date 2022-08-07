/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.sdk;

import com.goide.GoConstants;
import com.goide.GoEnvironmentUtil;
import com.goide.appengine.YamlFilesModificationTracker;
import com.goide.psi.GoFile;
import consulo.application.ApplicationManager;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.application.util.SystemInfo;
import consulo.google.go.module.extension.GoModuleExtension;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ProjectRootManager;
import consulo.process.PathEnvironmentVariableUtil;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.dataholder.Key;
import consulo.util.dataholder.UserDataHolder;
import consulo.util.io.FileUtil;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.VersionComparatorUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoSdkUtil {
  private static final Pattern GO_VERSION_PATTERN = Pattern.compile("[tT]heVersion\\s*=\\s*`go([\\d.]+\\w+(\\d+)?)`");
  private static final Pattern GAE_VERSION_PATTERN = Pattern.compile("[tT]heVersion\\s*=\\s*`go([\\d.]+)( \\(appengine-[\\d.]+\\))?`");
  private static final Pattern GO_DEVEL_VERSION_PATTERN = Pattern.compile("[tT]heVersion\\s*=\\s*`(devel.*)`");
  private static final Key<String> ZVERSION_DATA_KEY = Key.create("GO_ZVERSION_KEY");

  private GoSdkUtil() {
  }

  @Nullable
  public static VirtualFile getSdkSrcDir(@Nonnull Project project, @Nullable Module module) {
    if (module != null) {
      return CachedValuesManager.getManager(project).getCachedValue(module, () -> {
        GoSdkService sdkService = GoSdkService.getInstance(module.getProject());
        return CachedValueProvider.Result.create(getInnerSdkSrcDir(sdkService, module), ProjectRootManager.getInstance(project));
      });
    }
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      GoSdkService sdkService = GoSdkService.getInstance(project);
      return CachedValueProvider.Result.create(getInnerSdkSrcDir(sdkService, null), ProjectRootManager.getInstance(project));
    });
  }

  @Nullable
  private static VirtualFile getInnerSdkSrcDir(@Nonnull GoSdkService sdkService, @Nullable Module module) {
    String sdkHomePath = sdkService.getSdkHomePath(module);
    String sdkVersionString = sdkService.getSdkVersion(module);
    return sdkHomePath != null && sdkVersionString != null ? getSdkSrcDir(sdkHomePath, sdkVersionString) : null;
  }

  @Nullable
  private static VirtualFile getSdkSrcDir(@Nonnull String sdkPath, @Nonnull String sdkVersion) {
    String srcPath = getSrcLocation(sdkVersion);
    VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(VirtualFileUtil.pathToUrl(FileUtil.join(sdkPath, srcPath)));
    return file != null && file.isDirectory() ? file : null;
  }

  @Nullable
  public static GoFile findBuiltinFile(@Nonnull PsiElement context) {
    Project project = context.getProject();
    // it's important to ask module on file, otherwise module won't be found for elements in libraries files [zolotov]
    Module moduleFromContext = ModuleUtilCore.findModuleForPsiElement(context.getContainingFile());
    if (moduleFromContext == null) {
      for (Module module : ModuleManager.getInstance(project).getModules()) {
        if (GoSdkService.getInstance(project).isGoModule(module)) {
          moduleFromContext = module;
          break;
        }
      }
    }

    Module module = moduleFromContext;
    UserDataHolder holder = ObjectUtil.notNull(module, project);
    VirtualFile file = CachedValuesManager.getManager(context.getProject()).getCachedValue(holder, () -> {
      VirtualFile sdkSrcDir = getSdkSrcDir(project, module);
      VirtualFile result = sdkSrcDir != null ? sdkSrcDir.findFileByRelativePath(GoConstants.BUILTIN_FILE_PATH) : null;
      return CachedValueProvider.Result.create(result, getSdkAndLibrariesCacheDependencies(project, module, result));
    });

    if (file == null) return null;
    PsiFile psiBuiltin = context.getManager().findFile(file);
    return psiBuiltin instanceof GoFile ? (GoFile)psiBuiltin : null;
  }

  @Nullable
  public static VirtualFile findExecutableInGoPath(@Nonnull String executableName, @Nonnull Project project, @Nullable Module module) {
    executableName = GoEnvironmentUtil.getBinaryFileNameForPath(executableName);
    Collection<VirtualFile> roots = getGoPathRoots(project, module);
    for (VirtualFile file : roots) {
      VirtualFile child = VirtualFileUtil.findRelativeFile(file, "bin", executableName);
      if (child != null) return child;
    }
    File fromPath = PathEnvironmentVariableUtil.findInPath(executableName);
    return fromPath != null ? VirtualFileUtil.findFileByIoFile(fromPath, true) : null;
  }

  /**
   * @return concatenation of {@link this#getSdkSrcDir(Project, Module)} and {@link this#getGoPathSources(Project, Module)}
   */
  @Nonnull
  public static LinkedHashSet<VirtualFile> getSourcesPathsToLookup(@Nonnull Project project, @Nullable Module module) {
    LinkedHashSet<VirtualFile> sdkAndGoPath = new LinkedHashSet<>();
    ContainerUtil.addIfNotNull(sdkAndGoPath, getSdkSrcDir(project, module));
    ContainerUtil.addAllNotNull(sdkAndGoPath, getGoPathSources(project, module));
    return sdkAndGoPath;
  }

  @Nonnull
  public static LinkedHashSet<VirtualFile> getVendoringAwareSourcesPathsToLookup(@Nonnull Project project,
                                                                                 @Nullable Module module,
                                                                                 @Nullable VirtualFile contextFile) {
    LinkedHashSet<VirtualFile> sdkAndGoPath = getSourcesPathsToLookup(project, module);
    if (contextFile != null) {
      Collection<VirtualFile> vendorDirectories = collectVendorDirectories(contextFile, sdkAndGoPath);
      if (!vendorDirectories.isEmpty()) {
        LinkedHashSet<VirtualFile> result = new LinkedHashSet<>(vendorDirectories);
        result.addAll(sdkAndGoPath);
        return result;
      }
    }
    return sdkAndGoPath;
  }

  @Nonnull
  private static Collection<VirtualFile> collectVendorDirectories(@Nullable VirtualFile contextDirectory, @Nonnull Set<VirtualFile> sourceRoots) {
    if (contextDirectory == null) {
      return Collections.emptyList();
    }
    Collection<VirtualFile> vendorDirectories = ContainerUtil.newArrayList();
    VirtualFile directory = contextDirectory;
    while (directory != null) {
      VirtualFile vendorDirectory = directory.findChild(GoConstants.VENDOR);
      if (vendorDirectory != null && vendorDirectory.isDirectory()) {
        vendorDirectories.add(vendorDirectory);
      }
      if (sourceRoots.contains(directory)) {
        break;
      }
      directory = directory.getParent();
    }
    return vendorDirectories;
  }

  @Nonnull
  private static Collection<VirtualFile> getGoPathRoots(@Nonnull Project project, @Nullable Module module) {
    Collection<VirtualFile> roots = ContainerUtil.newArrayList();
    GoModuleExtension extension = module == null ? null : ModuleUtilCore.getExtension(module, GoModuleExtension.class);
    //TODO [VISTALL] use roots from Module
    /*if (extension != null && extension.isUseGoPath()) {
      roots.addAll(getGoPathsRootsFromEnvironment());
    }
    roots.addAll(module != null ? GoLibrariesService.getUserDefinedLibraries(module) : GoLibrariesService.getUserDefinedLibraries(project));   */
    return roots;
  }

  @Nonnull
  public static Collection<VirtualFile> getGoPathSources(@Nonnull Project project, @Nullable Module module) {
    if (module != null) {
      return CachedValuesManager.getManager(project).getCachedValue(module, () -> {
        Collection<VirtualFile> result = new LinkedHashSet<>();
        Project project1 = module.getProject();
        GoSdkService sdkService = GoSdkService.getInstance(project1);
        if (sdkService.isAppEngineSdk(module)) {
          ContainerUtil.addAllNotNull(result, ContainerUtil.mapNotNull(YamlFilesModificationTracker.getYamlFiles(project1, module), VirtualFile::getParent));
        }
        result.addAll(getInnerGoPathSources(project1, module));
        return CachedValueProvider.Result
                .create(result, getSdkAndLibrariesCacheDependencies(project1, module, YamlFilesModificationTracker.getInstance(project1)));
      });
    }
    return CachedValuesManager.getManager(project).getCachedValue(project, (CachedValueProvider<Collection<VirtualFile>>)() -> CachedValueProvider.Result
            .create(getInnerGoPathSources(project, null),
                    getSdkAndLibrariesCacheDependencies(project, null, YamlFilesModificationTracker.getInstance(project))));
  }

  @Nonnull
  private static List<VirtualFile> getInnerGoPathSources(@Nonnull Project project, @Nullable Module module) {
    return ContainerUtil.mapNotNull(getGoPathRoots(project, module), new RetrieveSubDirectoryOrSelfFunction("src"));
  }

  @Nonnull
  private static Collection<VirtualFile> getGoPathBins(@Nonnull Project project, @Nullable Module module) {
    Collection<VirtualFile> result = new LinkedHashSet<>(ContainerUtil.mapNotNull(getGoPathRoots(project, module), new RetrieveSubDirectoryOrSelfFunction("bin")));
    String executableGoPath = GoSdkService.getInstance(project).getGoExecutablePath(module);
    if (executableGoPath != null) {
      VirtualFile executable = VirtualFileManager.getInstance().findFileByUrl(VirtualFileUtil.pathToUrl(executableGoPath));
      if (executable != null) ContainerUtil.addIfNotNull(result, executable.getParent());
    }
    return result;
  }

  /**
   * Retrieves root directories from GOPATH env-variable.
   * This method doesn't consider user defined libraries,
   * for that case use {@link {@link this#getGoPathRoots(Project, Module)}
   */
  @Nonnull
  public static Collection<VirtualFile> getGoPathsRootsFromEnvironment() {
    return GoEnvironmentGoPathModificationTracker.getGoEnvironmentGoPathRoots();
  }

  @Nonnull
  public static String retrieveGoPath(@Nonnull Project project, @Nullable Module module) {
    return StringUtil.join(ContainerUtil.map(getGoPathRoots(project, module), VirtualFile::getPath), File.pathSeparator);
  }

  @Nonnull
  public static String retrieveEnvironmentPathForGo(@Nonnull Project project, @Nullable Module module) {
    return StringUtil.join(ContainerUtil.map(getGoPathBins(project, module), VirtualFile::getPath), File.pathSeparator);
  }

  @Nonnull
  private static String getSrcLocation(@Nonnull String version) {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return "src/pkg";
    }
    if (version.startsWith("devel")) {
      return "src";
    }
    if (version.length() > 2 && StringUtil.parseDouble(version.substring(0, 3), 1.4) < 1.4) {
      return "src/pkg";
    }
    return "src";
  }

  public static int compareVersions(@Nonnull String lhs, @Nonnull String rhs) {
    return VersionComparatorUtil.compare(lhs, rhs);
  }

  @Nullable
  @Contract("null, _ -> null")
  public static String getImportPath(@Nullable PsiDirectory psiDirectory, boolean withVendoring) {
    if (psiDirectory == null) {
      return null;
    }
    return LanguageCachedValueUtil
            .getCachedValue(psiDirectory, withVendoring ? new CachedVendoredImportPathProvider(psiDirectory) : new CachedImportPathProviderImpl(psiDirectory));
  }

  @Nullable
  private static String getPathRelativeToSdkAndLibrariesAndVendor(@Nonnull PsiDirectory psiDirectory, boolean withVendoring) {
    VirtualFile file = psiDirectory.getVirtualFile();
    Project project = psiDirectory.getProject();
    Module module = ModuleUtilCore.findModuleForPsiElement(psiDirectory);
    LinkedHashSet<VirtualFile> sourceRoots =
            withVendoring ? getVendoringAwareSourcesPathsToLookup(project, module, file) : getSourcesPathsToLookup(project, module);

    String relativePath = getRelativePathToRoots(file, sourceRoots);
    if (relativePath != null) {
      return relativePath;
    }

    String filePath = file.getPath();
    int src = filePath.lastIndexOf("/src/");
    if (src > -1) {
      return filePath.substring(src + 5);
    }
    return null;
  }

  @Nullable
  public static String getRelativePathToRoots(@Nonnull VirtualFile file, @Nonnull Collection<VirtualFile> sourceRoots) {
    for (VirtualFile root : sourceRoots) {
      String relativePath = VirtualFileUtil.getRelativePath(file, root, '/');
      if (StringUtil.isNotEmpty(relativePath)) {
        return relativePath;
      }
    }
    return null;
  }

  @Nonnull
  public static List<String> suggestSdkDirectory() {
    List<String> list = new SmartList<>();
    String goRoot = System.getenv(GoConstants.GO_ROOT);
    if (goRoot != null) {
      list.add(goRoot);
    }

    if (SystemInfo.isWindows) {
      list.add("C:\\Go");
      list.add("C:\\cygwin");
    }

    if (SystemInfo.isMac || SystemInfo.isLinux) {
      String fromEnv = suggestSdkDirectoryPathFromEnv();
      if (fromEnv != null) {
        list.add(fromEnv);
      }
      list.add("/usr/local/go");
    }
    if (SystemInfo.isMac) {
      list.add("/opt/local/lib/go");
      list.add("/usr/local/Cellar/go");
    }
    return list;
  }

  @Nullable
  private static String suggestSdkDirectoryPathFromEnv() {
    File fileFromPath = PathEnvironmentVariableUtil.findInPath("go");
    if (fileFromPath != null) {
      File canonicalFile;
      try {
        canonicalFile = fileFromPath.getCanonicalFile();
        String path = canonicalFile.getPath();
        if (path.endsWith("bin/go")) {
          return StringUtil.trimEnd(path, "bin/go");
        }
      }
      catch (IOException ignore) {
      }
    }
    return null;
  }

  @Nullable
  public static String parseGoVersion(@Nonnull String text) {
    Matcher matcher = GO_VERSION_PATTERN.matcher(text);
    if (matcher.find()) {
      return matcher.group(1);
    }
    matcher = GAE_VERSION_PATTERN.matcher(text);
    if (matcher.find()) {
      return matcher.group(1) + matcher.group(2);
    }
    matcher = GO_DEVEL_VERSION_PATTERN.matcher(text);
    if (matcher.find()) {
      return matcher.group(1);
    }

    return null;
  }

  @Nullable
  public static String retrieveGoVersion(@Nonnull String sdkPath) {
    try {
      VirtualFile sdkRoot = VirtualFileManager.getInstance().findFileByUrl(VirtualFileUtil.pathToUrl(sdkPath));
      if (sdkRoot != null) {
        String cachedVersion = sdkRoot.getUserData(ZVERSION_DATA_KEY);
        if (cachedVersion != null) {
          return !cachedVersion.isEmpty() ? cachedVersion : null;
        }

        VirtualFile versionFile = sdkRoot.findFileByRelativePath("src/" + GoConstants.GO_VERSION_NEW_FILE_PATH);
        if (versionFile == null) {
          versionFile = sdkRoot.findFileByRelativePath("src/" + GoConstants.GO_VERSION_FILE_PATH);
        }
        if (versionFile == null) {
          versionFile = sdkRoot.findFileByRelativePath("src/pkg/" + GoConstants.GO_VERSION_FILE_PATH);
        }
        if (versionFile != null) {
          String text = Files.readString(VirtualFileUtil.virtualToIoFile(versionFile).toPath());
          String version = parseGoVersion(text);
          if (version == null) {
            GoSdkService.LOG.debug("Cannot retrieve go version from zVersion file: " + text);
          }
          sdkRoot.putUserData(ZVERSION_DATA_KEY, StringUtil.notNullize(version));
          return version;
        }
        else {
          GoSdkService.LOG.debug("Cannot find go version file in sdk path: " + sdkPath);
        }
      }
    }
    catch (IOException e) {
      GoSdkService.LOG.debug("Cannot retrieve go version from sdk path: " + sdkPath, e);
    }
    return null;
  }

  @Nonnull
  public static String adjustSdkPath(@Nonnull String path) {
    if (new File(path, GoConstants.LIB_EXEC_DIRECTORY).exists()) {
      path += File.separatorChar + GoConstants.LIB_EXEC_DIRECTORY;
    }

    File possibleGCloudSdk = new File(path, GoConstants.GCLOUD_APP_ENGINE_DIRECTORY_PATH);
    if (possibleGCloudSdk.exists() && possibleGCloudSdk.isDirectory()) {
      if (isAppEngine(possibleGCloudSdk.getAbsolutePath())) {
        return possibleGCloudSdk.getAbsolutePath() + GoConstants.APP_ENGINE_GO_ROOT_DIRECTORY_PATH;
      }
    }

    return isAppEngine(path) ? path + GoConstants.APP_ENGINE_GO_ROOT_DIRECTORY_PATH : path;
  }


  private static boolean isAppEngine(@Nonnull String path) {
    return new File(path, GoConstants.APP_ENGINE_MARKER_FILE).exists();
  }

  @Nonnull
  public static Collection<VirtualFile> getSdkDirectoriesToAttach(@Nonnull String sdkPath, @Nonnull String versionString) {
    // scr is enough at the moment, possible process binaries from pkg
    return ContainerUtil.createMaybeSingletonList(getSdkSrcDir(sdkPath, versionString));
  }

  @Nonnull
  private static Collection<Object> getSdkAndLibrariesCacheDependencies(@Nonnull Project project, @Nullable Module module, Object... extra) {
    Collection<Object> dependencies = ContainerUtil.newArrayList();
    ContainerUtil.addAllNotNull(dependencies, ProjectRootManager.getInstance(project));
    ContainerUtil.addAllNotNull(dependencies, extra);
    return dependencies;
  }

  @Nonnull
  public static Collection<Module> getGoModules(@Nonnull Project project) {
    if (project.isDefault()) return Collections.emptyList();
    GoSdkService sdkService = GoSdkService.getInstance(project);
    return ContainerUtil.filter(ModuleManager.getInstance(project).getModules(), sdkService::isGoModule);
  }

  public static boolean isUnreachableInternalPackage(@Nonnull VirtualFile targetDirectory,
                                                     @Nonnull VirtualFile referenceContextFile,
                                                     @Nonnull Set<VirtualFile> sourceRoots) {
    return isUnreachablePackage(GoConstants.INTERNAL, targetDirectory, referenceContextFile, sourceRoots);
  }

  public static boolean isUnreachableVendoredPackage(@Nonnull VirtualFile targetDirectory,
                                                     @Nonnull VirtualFile referenceContextFile,
                                                     @Nonnull Set<VirtualFile> sourceRoots) {
    return isUnreachablePackage(GoConstants.VENDOR, targetDirectory, referenceContextFile, sourceRoots);
  }

  @Nullable
  public static VirtualFile findParentDirectory(@Nullable VirtualFile file, @Nonnull Set<VirtualFile> sourceRoots, @Nonnull String name) {
    if (file == null) {
      return null;
    }
    VirtualFile currentFile = file.isDirectory() ? file : file.getParent();
    while (currentFile != null && !sourceRoots.contains(currentFile)) {
      if (currentFile.isDirectory() && name.equals(currentFile.getName())) {
        return currentFile;
      }
      currentFile = currentFile.getParent();
    }
    return null;
  }

  private static boolean isUnreachablePackage(@Nonnull String unreachableDirectoryName,
                                              @Nonnull VirtualFile targetDirectory,
                                              @Nonnull VirtualFile referenceContextFile,
                                              @Nonnull Set<VirtualFile> sourceRoots) {
    VirtualFile directory = findParentDirectory(targetDirectory, sourceRoots, unreachableDirectoryName);
    VirtualFile parent = directory != null ? directory.getParent() : null;
    return directory != null && !VirtualFileUtil.isAncestor(parent, referenceContextFile, false);
  }

  private static class RetrieveSubDirectoryOrSelfFunction implements Function<VirtualFile, VirtualFile> {
    @Nonnull
    private final String mySubdirName;

    public RetrieveSubDirectoryOrSelfFunction(@Nonnull String subdirName) {
      mySubdirName = subdirName;
    }

    @Override
    public VirtualFile apply(VirtualFile file) {
      return file == null || FileUtil.namesEqual(mySubdirName, file.getName()) ? file : file.findChild(mySubdirName);
    }
  }

  private abstract static class CachedImportPathProvider implements CachedValueProvider<String> {
    private final PsiDirectory myPsiDirectory;
    private final boolean myWithVendoring;

    public CachedImportPathProvider(@Nonnull PsiDirectory psiDirectory, boolean vendoring) {
      myPsiDirectory = psiDirectory;
      myWithVendoring = vendoring;
    }

    @Nullable
    @Override
    public Result<String> compute() {
      String path = getPathRelativeToSdkAndLibrariesAndVendor(myPsiDirectory, myWithVendoring);
      Module module = ModuleUtilCore.findModuleForPsiElement(myPsiDirectory);
      return Result.create(path, getSdkAndLibrariesCacheDependencies(myPsiDirectory.getProject(), module, myPsiDirectory));
    }
  }

  private static class CachedImportPathProviderImpl extends CachedImportPathProvider {
    public CachedImportPathProviderImpl(@Nonnull PsiDirectory psiDirectory) {
      super(psiDirectory, false);
    }
  }

  private static class CachedVendoredImportPathProvider extends CachedImportPathProvider {
    public CachedVendoredImportPathProvider(@Nonnull PsiDirectory psiDirectory) {
      super(psiDirectory, true);
    }
  }
}

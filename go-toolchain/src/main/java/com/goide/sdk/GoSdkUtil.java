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

import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.application.util.SystemInfo;
import consulo.go.GoConstants;
import consulo.go.GoEnvironmentUtil;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ProjectRootManager;
import consulo.platform.Platform;
import consulo.process.PathEnvironmentVariableUtil;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.VersionComparatorUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import org.jspecify.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoSdkUtil {
  private static final Pattern GO_VERSION_PATTERN = Pattern.compile("[tT]heVersion\\s*=\\s*`go([\\d.]+\\w+(\\d+)?)`");
  private static final Pattern GO_TEXT_VERSION_PATTERN = Pattern.compile("go([\\d.]+\\w+(\\d+)?)");
  private static final Pattern GAE_VERSION_PATTERN = Pattern.compile("[tT]heVersion\\s*=\\s*`go([\\d.]+)( \\(appengine-[\\d.]+\\))?`");
  private static final Pattern GO_DEVEL_VERSION_PATTERN = Pattern.compile("[tT]heVersion\\s*=\\s*`(devel.*)`");

  private GoSdkUtil() {
  }

  @Nullable
  public static VirtualFile getSdkSrcDir(Project project, @Nullable Module module) {
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
  private static VirtualFile getInnerSdkSrcDir(GoSdkService sdkService, @Nullable Module module) {
    String sdkHomePath = sdkService.getSdkHomePath(module);
    String sdkVersionString = sdkService.getSdkVersion(module);
    return sdkHomePath != null && sdkVersionString != null ? getSdkSrcDir(sdkHomePath, sdkVersionString) : null;
  }

  @Nullable
  private static VirtualFile getSdkSrcDir(String sdkPath, String sdkVersion) {
    String srcPath = getSrcLocation(sdkVersion);
    VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(VirtualFileUtil.pathToUrl(FileUtil.join(sdkPath, srcPath)));
    return file != null && file.isDirectory() ? file : null;
  }

  /**
   * Looks for a tool installed by {@code go install}: in GOBIN, in the {@code bin} of GOPATH (by default {@code ~/go}),
   * then on PATH.
   */
  @Nullable
  public static VirtualFile findExecutableInGoPath(String executableName, Project project, @Nullable Module module) {
    String binaryName = GoEnvironmentUtil.getBinaryFileNameForPath(executableName);
    for (Path binDirectory : GoEnvironmentUtil.getGoInstallDirectories()) {
      Path executable = binDirectory.resolve(binaryName);
      if (Files.isExecutable(executable)) {
        VirtualFile file = VirtualFileUtil.findFileByIoFile(executable.toFile(), true);
        if (file != null) return file;
      }
    }
    File fromPath = PathEnvironmentVariableUtil.findInPath(binaryName);
    return fromPath != null ? VirtualFileUtil.findFileByIoFile(fromPath, true) : null;
  }

  /**
   * @return {@link this#getSdkSrcDir(Project, Module)}: packages outside the standard library resolve through the Go modules
   * of the module
   */
  public static LinkedHashSet<VirtualFile> getSourcesPathsToLookup(Project project, @Nullable Module module) {
    LinkedHashSet<VirtualFile> sdkSources = new LinkedHashSet<>();
    ContainerUtil.addIfNotNull(sdkSources, getSdkSrcDir(project, module));
    return sdkSources;
  }

  public static LinkedHashSet<VirtualFile> getVendoringAwareSourcesPathsToLookup(Project project,
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

  private static Collection<VirtualFile> collectVendorDirectories(@Nullable VirtualFile contextDirectory, Set<VirtualFile> sourceRoots) {
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

  /**
   * @return the directory of the {@code go} binary, to put on PATH for the processes it starts
   */
  public static String retrieveEnvironmentPathForGo(Project project, @Nullable Module module) {
    String executableGoPath = GoSdkService.getInstance(project).getGoExecutablePath(module);
    Path binDirectory = executableGoPath != null ? Path.of(executableGoPath).getParent() : null;
    return binDirectory != null ? binDirectory.toString() : "";
  }

  private static String getSrcLocation(String version) {
    return "src";
  }

  public static int compareVersions(String lhs, String rhs) {
    return VersionComparatorUtil.compare(lhs, rhs);
  }

  @Nullable
  public static String getRelativePathToRoots(VirtualFile file, Collection<VirtualFile> sourceRoots) {
    for (VirtualFile root : sourceRoots) {
      String relativePath = VirtualFileUtil.getRelativePath(file, root, '/');
      if (StringUtil.isNotEmpty(relativePath)) {
        return relativePath;
      }
    }
    return null;
  }

  public static List<String> suggestSdkDirectory() {
    Platform platform = Platform.current();

    List<String> list = new SmartList<>();
    String goRoot = System.getenv(GoConstants.GO_ROOT);
    if (goRoot != null) {
      list.add(goRoot);
    }

    if (platform.os().isWindows()) {
      list.add("C:\\Go");
      list.add("C:\\cygwin");
      list.add("C:\\cygwin");

      String programFiles = platform.os().getEnvironmentVariable("ProgramFiles");
      if (programFiles != null) {
        list.add(programFiles + "\\Go");
      }
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
  public static String parseGoVersion(String text) {
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
    matcher = GO_TEXT_VERSION_PATTERN.matcher(text);
    if (matcher.matches()) {
      return matcher.group(1);
    }

    return null;
  }

  @Nullable
  public static String retrieveGoVersion(String sdkPath) {
    try {
      Path sdkRoot = Path.of(sdkPath);
      if (sdkRoot != null) {
        Path versionFile = sdkRoot.resolve("VERSION");
        if (versionFile == null) {
          versionFile = sdkRoot.resolve("src/" + GoConstants.GO_VERSION_NEW_FILE_PATH);
        }
        if (versionFile == null) {
          versionFile = sdkRoot.resolve("src/" + GoConstants.GO_VERSION_FILE_PATH);
        }
        if (versionFile == null) {
          versionFile = sdkRoot.resolve("src/pkg/" + GoConstants.GO_VERSION_FILE_PATH);
        }

        if (versionFile != null) {
          List<String> lines = Files.readAllLines(versionFile);
          String version = lines.isEmpty() ? null : parseGoVersion(lines.get(0));
          if (version == null) {
            GoSdkService.LOG.debug("Cannot retrieve go version from zVersion file: " + String.join("\n", lines));
          }
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

  public static String adjustSdkPath(String path) {
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


  private static boolean isAppEngine(String path) {
    return new File(path, GoConstants.APP_ENGINE_MARKER_FILE).exists();
  }

  public static Collection<VirtualFile> getSdkDirectoriesToAttach(String sdkPath, String versionString) {
    // scr is enough at the moment, possible process binaries from pkg
    return ContainerUtil.createMaybeSingletonList(getSdkSrcDir(sdkPath, versionString));
  }

  public static Collection<Object> getSdkAndLibrariesCacheDependencies(Project project, @Nullable Module module, Object... extra) {
    Collection<Object> dependencies = ContainerUtil.newArrayList();
    ContainerUtil.addAllNotNull(dependencies, ProjectRootManager.getInstance(project));
    ContainerUtil.addAllNotNull(dependencies, extra);
    return dependencies;
  }

  public static Collection<Module> getGoModules(Project project) {
    if (project.isDefault()) return Collections.emptyList();
    GoSdkService sdkService = GoSdkService.getInstance(project);
    return ContainerUtil.filter(ModuleManager.getInstance(project).getModules(), sdkService::isGoModule);
  }

  public static boolean isUnreachableInternalPackage(VirtualFile targetDirectory,
                                                     VirtualFile referenceContextFile,
                                                     Set<VirtualFile> sourceRoots) {
    return isUnreachablePackage(GoConstants.INTERNAL, targetDirectory, referenceContextFile, sourceRoots);
  }

  public static boolean isUnreachableVendoredPackage(VirtualFile targetDirectory,
                                                     VirtualFile referenceContextFile,
                                                     Set<VirtualFile> sourceRoots) {
    return isUnreachablePackage(GoConstants.VENDOR, targetDirectory, referenceContextFile, sourceRoots);
  }

  @Nullable
  public static VirtualFile findParentDirectory(@Nullable VirtualFile file, Set<VirtualFile> sourceRoots, String name) {
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

  private static boolean isUnreachablePackage(String unreachableDirectoryName,
                                              VirtualFile targetDirectory,
                                              VirtualFile referenceContextFile,
                                              Set<VirtualFile> sourceRoots) {
    VirtualFile directory = findParentDirectory(targetDirectory, sourceRoots, unreachableDirectoryName);
    VirtualFile parent = directory != null ? directory.getParent() : null;
    return directory != null && !VirtualFileUtil.isAncestor(parent, referenceContextFile, false);
  }
}

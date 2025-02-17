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
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.application.util.SystemInfo;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.ide.ServiceManager;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.process.PathEnvironmentVariableUtil;
import consulo.project.Project;
import consulo.util.io.FileUtil;
import consulo.util.io.PathUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.TestOnly;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;
import java.util.Set;

@ServiceAPI(ComponentScope.PROJECT)
public abstract class GoSdkService {
  public static final String LIBRARY_NAME = "Go SDK";

  public static final Logger LOG = Logger.getInstance(GoSdkService.class);
  private static final Set<String> FEDORA_SUBDIRECTORIES = Set.of("linux_amd64", "linux_386", "linux_arm");
  private static String ourTestSdkVersion;

  @Nonnull
  protected final Project myProject;

  protected GoSdkService(@Nonnull Project project) {
    myProject = project;
  }

  public static GoSdkService getInstance(@Nonnull Project project) {
    return ServiceManager.getService(project, GoSdkService.class);
  }

  @Nullable
  public abstract String getSdkHomePath(@Nullable Module module);

  @Nonnull
  public static String libraryRootToSdkPath(@Nonnull VirtualFile root) {
    return VirtualFileUtil.urlToPath(StringUtil.trimEnd(StringUtil.trimEnd(StringUtil.trimEnd(root.getUrl(), "src/pkg"), "src"), "/"));
  }

  @Nullable
  public String getSdkVersion(@Nullable Module module) {
    return ourTestSdkVersion;
  }

  public boolean isAppEngineSdk(@Nullable Module module) {
    return isAppEngineSdkPath(getSdkHomePath(module));
  }

  public static boolean isAppEngineSdkPath(@Nullable String path) {
    return isLooksLikeAppEngineSdkPath(path) && getGaeExecutablePath(path) != null;
  }

  private static boolean isLooksLikeAppEngineSdkPath(@Nullable String path) {
    return path != null && path.endsWith(GoConstants.APP_ENGINE_GO_ROOT_DIRECTORY_PATH);
  }

  /**
   * Use this method in order to check whether the method is appropriate for providing Go-specific code insight
   */
  @Contract("null -> false")
  public boolean isGoModule(@Nullable Module module) {
    return module != null && !module.isDisposed();
  }

  @Nullable
  public String getGoExecutablePath(@Nullable Module module) {
    return getGoExecutablePath(getSdkHomePath(module));
  }

  public static String getGoExecutablePath(@Nullable String sdkHomePath) {
    if (sdkHomePath != null) {
      if (isLooksLikeAppEngineSdkPath(sdkHomePath)) {
        LOG.debug("Looks like GAE sdk at " + sdkHomePath);
        String executablePath = getGaeExecutablePath(sdkHomePath);
        if (executablePath != null) return executablePath;
      }

      File binDirectory = new File(sdkHomePath, "bin");
      if (!binDirectory.exists() && SystemInfo.isLinux) {
        LOG.debug(sdkHomePath + "/bin doesn't exist, checking linux-specific paths");
        // failed to define executable path in old linux and old go
        File goFromPath = PathEnvironmentVariableUtil.findInPath(GoConstants.GO_EXECUTABLE_NAME);
        if (goFromPath != null && goFromPath.exists()) {
          LOG.debug("Go executable found at " + goFromPath.getAbsolutePath());
          return goFromPath.getAbsolutePath();
        }
      }

      String executableName = GoEnvironmentUtil.getBinaryFileNameForPath(GoConstants.GO_EXECUTABLE_NAME);
      String executable = FileUtil.join(sdkHomePath, "bin", executableName);

      if (!new File(executable).exists() && SystemInfo.isLinux) {
        LOG.debug(executable + " doesn't exists. Looking for binaries in fedora-specific directories");
        // fedora
        for (String directory : FEDORA_SUBDIRECTORIES) {
          File file = new File(binDirectory, directory);
          if (file.exists() && file.isDirectory()) {
            LOG.debug("Go executable found at " + file.getAbsolutePath());
            return FileUtil.join(file.getAbsolutePath(), executableName);
          }
        }
      }
      LOG.debug("Go executable found at " + executable);
      return executable;
    }
    return null;
  }

  @Nullable
  private static String getGaeExecutablePath(@Nonnull String sdkHomePath) {
    String goExecutablePath = PathUtil.toSystemIndependentName(sdkHomePath);
    goExecutablePath = StringUtil.trimEnd(goExecutablePath, GoConstants.APP_ENGINE_GO_ROOT_DIRECTORY_PATH);

    boolean gcloudInstallation = goExecutablePath.endsWith(GoConstants.GCLOUD_APP_ENGINE_DIRECTORY_PATH);
    if (gcloudInstallation) {
      LOG.debug("Detected gcloud GAE installation at " + goExecutablePath);
      goExecutablePath = FileUtil.join(StringUtil.trimEnd(goExecutablePath, GoConstants.GCLOUD_APP_ENGINE_DIRECTORY_PATH), "bin");
    }
    String executablePath = FileUtil.join(goExecutablePath, GoEnvironmentUtil.getGaeExecutableFileName(gcloudInstallation));
    return new File(executablePath).exists() ? executablePath : null;
  }

  @TestOnly
  public static void setTestingSdkVersion(@Nullable String version, @Nonnull Disposable disposable) {
    ourTestSdkVersion = version;
    Disposer.register(disposable, () -> {
      //noinspection AssignmentToStaticFieldFromInstanceMethod
      ourTestSdkVersion = null;
    });
  }

  public static boolean isGoSdkLibRoot(@Nonnull VirtualFile root) {
    return root.isInLocalFileSystem() &&
           root.isDirectory() &&
           (VirtualFileUtil.findRelativeFile(GoConstants.GO_VERSION_FILE_PATH, root) != null ||
               VirtualFileUtil.findRelativeFile(GoConstants.GO_VERSION_NEW_FILE_PATH, root) != null
           );
  }
}

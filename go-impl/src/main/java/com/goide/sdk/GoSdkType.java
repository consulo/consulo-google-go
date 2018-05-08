/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

import java.io.File;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;

import javax.annotation.Nullable;
import com.goide.GoIcons;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.roots.types.BinariesOrderRootType;
import consulo.roots.types.SourcesOrderRootType;
import consulo.ui.image.Image;

public class GoSdkType extends SdkType {
  @Nonnull
  public static GoSdkType getInstance() {
    return SdkType.EP_NAME.findExtension(GoSdkType.class);
  }

  public GoSdkType() {
    super("Google Go SDK");
  }

  @Nonnull
  @Override
  public Image getIcon() {
    return GoIcons.ICON;
  }

  @Nonnull
  @Override
  public Collection<String> suggestHomePaths() {
    return GoSdkUtil.suggestSdkDirectory();
  }

  @Override
  public boolean canCreatePredefinedSdks() {
    return true;
  }

  @Override
  public boolean isValidSdkHome(@Nonnull String path) {
    GoSdkService.LOG.debug("Validating sdk path: " + path);
    String executablePath = GoSdkService.getGoExecutablePath(path);
    if (executablePath == null) {
      GoSdkService.LOG.debug("Go executable is not found: ");
      return false;
    }
    if (!new File(executablePath).canExecute()) {
      GoSdkService.LOG.debug("Go binary cannot be executed: " + path);
      return false;
    }
    if (getVersionString(path) != null) {
      GoSdkService.LOG.debug("Cannot retrieve version for sdk: " + path);
      return true;
    }
    return false;
  }

  @Nonnull
  @Override
  public String adjustSelectedSdkHome(@Nonnull String homePath) {
    return GoSdkUtil.adjustSdkPath(homePath);
  }

  @Nonnull
  @Override
  public String suggestSdkName(@Nullable String currentSdkName, @Nonnull String sdkHome) {
    String version = getVersionString(sdkHome);
    if (version == null) {
      return "Unknown Go version at " + sdkHome;
    }
    return "Go " + version;
  }

  @Nullable
  @Override
  public String getVersionString(@Nonnull String sdkHome) {
    return GoSdkUtil.retrieveGoVersion(sdkHome);
  }

  @Override
  public boolean isRootTypeApplicable(OrderRootType type) {
    return type == SourcesOrderRootType.getInstance() || type == BinariesOrderRootType.getInstance();
  }

  @Nonnull
  @NonNls
  @Override
  public String getPresentableName() {
    return "Go SDK";
  }

  @Override
  public void setupSdkPaths(@Nonnull Sdk sdk) {
    String versionString = sdk.getVersionString();
    if (versionString == null) throw new RuntimeException("SDK version is not defined");
    SdkModificator modificator = sdk.getSdkModificator();
    String path = sdk.getHomePath();
    if (path == null) return;
    modificator.setHomePath(path);

    for (VirtualFile file : GoSdkUtil.getSdkDirectoriesToAttach(path, versionString)) {
      modificator.addRoot(file, BinariesOrderRootType.getInstance());
      modificator.addRoot(file, SourcesOrderRootType.getInstance());
    }
    modificator.commitChanges();
  }
}

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

package com.goide;

import consulo.application.ApplicationManager;
import consulo.application.macro.PathMacros;
import consulo.application.util.SystemInfo;
import consulo.process.local.EnvironmentUtil;
import consulo.util.io.FileUtil;
import consulo.util.io.PathUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GoEnvironmentUtil {
  private GoEnvironmentUtil() {}

  @Nonnull
  public static String getBinaryFileNameForPath(@Nonnull String path) {
    String resultBinaryName = FileUtil.getNameWithoutExtension(PathUtil.getFileName(path));
    return SystemInfo.isWindows ? resultBinaryName + ".exe" : resultBinaryName;
  }

  @Nonnull
  public static String getGaeExecutableFileName(boolean gcloudInstallation) {
    if (SystemInfo.isWindows) {
      return gcloudInstallation ? GoConstants.GAE_CMD_EXECUTABLE_NAME : GoConstants.GAE_BAT_EXECUTABLE_NAME;
    }
    return GoConstants.GAE_EXECUTABLE_NAME;
  }

  @Nullable
  public static String retrieveGoPathFromEnvironment() {
    if (ApplicationManager.getApplication().isUnitTestMode()) return null;
    
    String path = EnvironmentUtil.getValue(GoConstants.GO_PATH);
    return path != null ? path : PathMacros.getInstance().getValue(GoConstants.GO_PATH);
  }
}

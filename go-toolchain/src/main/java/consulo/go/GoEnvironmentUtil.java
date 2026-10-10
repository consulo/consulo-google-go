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

package consulo.go;

import consulo.application.util.SystemInfo;
import consulo.process.local.EnvironmentUtil;
import consulo.util.io.FileUtil;
import consulo.util.io.PathUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ThreeState;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GoEnvironmentUtil {
  private GoEnvironmentUtil() {}

  public static String getBinaryFileNameForPath(String path) {
    String resultBinaryName = FileUtil.getNameWithoutExtension(PathUtil.getFileName(path));
    return SystemInfo.isWindows ? resultBinaryName + ".exe" : resultBinaryName;
  }

  public static String getGaeExecutableFileName(boolean gcloudInstallation) {
    if (SystemInfo.isWindows) {
      return gcloudInstallation ? GoConstants.GAE_CMD_EXECUTABLE_NAME : GoConstants.GAE_BAT_EXECUTABLE_NAME;
    }
    return GoConstants.GAE_EXECUTABLE_NAME;
  }

  /**
   * @return where {@code go install} puts binaries: GOBIN, else the {@code bin} of every GOPATH entry, else {@code ~/go/bin}
   */
  public static List<Path> getGoInstallDirectories() {
    List<Path> directories = new ArrayList<>();
    String goBin = EnvironmentUtil.getValue(GoConstants.GO_BIN);
    if (StringUtil.isNotEmpty(goBin)) {
      directories.add(Path.of(goBin));
      return directories;
    }
    String goPath = EnvironmentUtil.getValue(GoConstants.GO_PATH);
    if (StringUtil.isNotEmpty(goPath)) {
      for (String entry : StringUtil.split(goPath, File.pathSeparator)) {
        directories.add(Path.of(entry, "bin"));
      }
      return directories;
    }
    String home = System.getProperty("user.home");
    if (home != null) {
      directories.add(Path.of(home, "go", "bin"));
    }
    return directories;
  }

  public static String systemOS() {
    // TODO android? dragonfly nacl? netbsd openbsd plan9
    if (SystemInfo.isMac) {
      return "darwin";
    }
    if (SystemInfo.isFreeBSD) {
      return "freebsd";
    }
    if (SystemInfo.isLinux) {
      return GoConstants.LINUX_OS;
    }
    if (SystemInfo.isWindows) {
      return "windows";
    }
    return "unknown";
  }

  public static String systemArch() {
    if (SystemInfo.is64Bit) {
      return GoConstants.AMD64;
    }
    if (SystemInfo.isWindows) {
      String arch = System.getenv("PROCESSOR_ARCHITECTURE");
      String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
      return arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? GoConstants.AMD64 : "386";
    }
    if (SystemInfo.is32Bit) {
      return "386";
    }
    return "unknown";
  }

  public static ThreeState systemCgo(String os, String arch) {
    return GoConstants.KNOWN_CGO.contains(os + "/" + arch) ? ThreeState.YES : ThreeState.NO;
  }
}

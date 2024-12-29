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

package com.goide.runconfig;

import com.goide.codeInsight.imports.GoGetPackageFix;
import com.goide.sdk.GoPackageUtil;
import com.goide.util.GoPathResolveScope;
import com.goide.util.GoUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.execution.ui.console.Filter;
import consulo.execution.ui.console.HyperlinkInfo;
import consulo.execution.ui.console.OpenFileHyperlinkInfo;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FilenameIndex;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.io.FileUtil;
import consulo.util.io.PathUtil;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.TempFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//todo @ExtensionImpl
public class GoConsoleFilter implements Filter {
  private static final Pattern MESSAGE_PATTERN = Pattern.compile("(?:^|\\s)(\\S+\\.\\w+):(\\d+)(:(\\d+))?(?=[:\\s]|$).*");
  private static final Pattern GO_GET_MESSAGE_PATTERN = Pattern.compile("^[ \t]*(go get (.*))\n?$");
  private static final Pattern APP_ENGINE_PATH_PATTERN = Pattern.compile("/tmp[A-z0-9]+appengine-go-bin/");
  private static final Pattern GO_FILE_PATTERN = Pattern.compile("\\((\\w+\\.go)\\)");

  @Nonnull
  private final Project myProject;
  @Nullable
  private final Module myModule;
  @Nullable
  private final String myWorkingDirectoryUrl;

  @Inject
  public GoConsoleFilter(@Nonnull Project project) {
    this(project, null, null);
  }

  public GoConsoleFilter(@Nonnull Project project, @Nullable Module module, @Nullable String workingDirectoryUrl) {
    myProject = project;
    myModule = module;
    myWorkingDirectoryUrl = ObjectUtil.chooseNotNull(workingDirectoryUrl, VirtualFileUtil.pathToUrl(System.getProperty("user.dir")));
  }

  @Override
  public Result applyFilter(@Nonnull String line, int entireLength) {
    Matcher goGetMatcher = GO_GET_MESSAGE_PATTERN.matcher(line);
    if (goGetMatcher.find() && myModule != null) {
      String packageName = goGetMatcher.group(2).trim();
      HyperlinkInfo hyperlinkInfo = new GoGetHyperlinkInfo(packageName, myModule);
      int lineStart = entireLength - line.length();
      return new Result(lineStart + goGetMatcher.start(1), lineStart + goGetMatcher.end(2), hyperlinkInfo);
    }
    Matcher matcher = MESSAGE_PATTERN.matcher(line);
    if (!matcher.find()) {
      Matcher fileMatcher = GO_FILE_PATTERN.matcher(line);
      List<ResultItem> resultItems = ContainerUtil.newArrayList();
      while (fileMatcher.find()) {
        VirtualFile file = findSingleFile(fileMatcher.group(1));
        if (file != null) {
          resultItems.add(createResult(line, entireLength, fileMatcher.start(1), fileMatcher.end(1), 0, 0, file));
        }
      }
      return !resultItems.isEmpty() ? new Result(resultItems) : null;
    }

    int startOffset = matcher.start(1);
    int endOffset = matcher.end(2);

    String fileName = matcher.group(1);
    int lineNumber = StringUtil.parseInt(matcher.group(2), 1) - 1;
    if (lineNumber < 0) {
      return null;
    }

    int columnNumber = -1;
    if (matcher.groupCount() > 3) {
      columnNumber = StringUtil.parseInt(matcher.group(4), 1) - 1;
      endOffset = Math.max(endOffset, matcher.end(4));
    }

    Matcher appEnginePathMatcher = APP_ENGINE_PATH_PATTERN.matcher(fileName);
    if (appEnginePathMatcher.find()) {
      fileName = fileName.substring(appEnginePathMatcher.end());
    }

    VirtualFile virtualFile = null;
    if (FileUtil.isAbsolutePlatformIndependent(fileName)) {
      virtualFile = ApplicationManager.getApplication().isUnitTestMode()
                    ? TempFileSystem.getInstance().refreshAndFindFileByPath(fileName)
                    : VirtualFileManager.getInstance().refreshAndFindFileByUrl(VirtualFileUtil.pathToUrl(fileName));
    }
    else {
      if (myWorkingDirectoryUrl != null) {
        virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByUrl(myWorkingDirectoryUrl + "/" + fileName);
      }
      if (virtualFile == null && myModule != null) {
        virtualFile = findInGoPath(fileName);
        if (virtualFile == null && fileName.startsWith("src/")) {
          virtualFile = findInGoPath(StringUtil.trimStart(fileName, "src/"));
        }
      }
      if (virtualFile == null) {
        VirtualFile baseDir = myProject.getBaseDir();
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
      virtualFile = findSingleFile(fileName);
    }
    if (virtualFile == null) {
      return null;
    }
    return createResult(line, entireLength, startOffset, endOffset, lineNumber, columnNumber, virtualFile);
  }

  @Nonnull
  private Result createResult(@Nonnull String line,
                              int entireLength,
                              int startOffset,
                              int endOffset,
                              int lineNumber,
                              int columnNumber,
                              @Nonnull VirtualFile virtualFile) {
    HyperlinkInfo hyperlinkInfo = new OpenFileHyperlinkInfo(myProject, virtualFile, lineNumber, columnNumber);
    int lineStart = entireLength - line.length();
    return new Result(lineStart + startOffset, lineStart + endOffset, hyperlinkInfo);
  }

  @Nullable
  private VirtualFile findSingleFile(@Nonnull String fileName) {
    if (PathUtil.isValidFileName(fileName)) {
      Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(myProject, fileName, GlobalSearchScope.allScope(myProject));
      if (files.size() == 1) {
        return ContainerUtil.getFirstItem(files);
      }
      if (!files.isEmpty()) {
        GlobalSearchScope goPathScope = GoPathResolveScope.create(myProject, myModule, null);
        files = ContainerUtil.filter(files, goPathScope::accept);
        if (files.size() == 1) {
          return ContainerUtil.getFirstItem(files);
        }
      }
      if (!files.isEmpty()) {
        GlobalSearchScope smallerScope = GoUtil.moduleScopeWithoutLibraries(myProject, myModule);
        files = ContainerUtil.filter(files, smallerScope::accept);
        if (files.size() == 1) {
          return ContainerUtil.getFirstItem(files);
        }
      }
    }
    return null;
  }

  @Nullable
  private VirtualFile findInGoPath(@Nonnull String fileName) {
    return GoPackageUtil.findByImportPath(fileName, myProject, myModule);
  }

  public static class GoGetHyperlinkInfo implements HyperlinkInfo {
    private final String myPackageName;
    private final Module myModule;

    public GoGetHyperlinkInfo(@Nonnull String packageName, @Nonnull Module module) {
      myPackageName = packageName;
      myModule = module;
    }

    @Nonnull
    public String getPackageName() {
      return myPackageName;
    }

    @Override
    public void navigate(Project project) {
      GoGetPackageFix.applyFix(project, myModule, myPackageName, false);
    }
  }
}

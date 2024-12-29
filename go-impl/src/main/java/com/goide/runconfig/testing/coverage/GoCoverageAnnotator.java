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

package com.goide.runconfig.testing.coverage;

import com.intellij.rt.coverage.data.ProjectData;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.progress.ProgressIndicatorProvider;
import consulo.execution.coverage.BaseCoverageAnnotator;
import consulo.execution.coverage.CoverageDataManager;
import consulo.execution.coverage.CoverageSuite;
import consulo.execution.coverage.CoverageSuitesBundle;
import consulo.ide.ServiceManager;
import consulo.language.content.FileIndexFacade;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiFile;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import jakarta.inject.Inject;
import org.jetbrains.annotations.TestOnly;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class GoCoverageAnnotator extends BaseCoverageAnnotator {
  private static final String STATEMENTS_SUFFIX = "% statements";
  private static final String FILES_SUFFIX = "% files";

  private final Map<String, FileCoverageInfo> myFileCoverageInfos = new HashMap<>();
  private final Map<String, DirCoverageInfo> myDirCoverageInfos = new HashMap<>();

  @Inject
  public GoCoverageAnnotator(@Nonnull Project project) {
    super(project);
  }

  public static GoCoverageAnnotator getInstance(Project project) {
    return ServiceManager.getService(project, GoCoverageAnnotator.class);
  }

  @Nullable
  @Override
  public String getDirCoverageInformationString(@Nonnull PsiDirectory directory,
                                                @Nonnull CoverageSuitesBundle bundle,
                                                @Nonnull CoverageDataManager manager) {
    DirCoverageInfo dirCoverageInfo = myDirCoverageInfos.get(directory.getVirtualFile().getPath());
    if (dirCoverageInfo == null) {
      return null;
    }

    if (manager.isSubCoverageActive()) {
      return dirCoverageInfo.coveredLineCount > 0 ? "covered" : null;
    }

    return getDirCoverageString(dirCoverageInfo);
  }

  @Nullable
  private static String getDirCoverageString(@Nonnull DirCoverageInfo dirCoverageInfo) {
    String filesCoverageInfo = getFilesCoverageString(dirCoverageInfo);
    if (filesCoverageInfo != null) {
      StringBuilder builder = new StringBuilder();
      builder.append(filesCoverageInfo);
      String statementsCoverageInfo = getStatementsCoverageString(dirCoverageInfo);
      if (statementsCoverageInfo != null) {
        builder.append(", ").append(statementsCoverageInfo);
      }
      return builder.toString();
    }
    return null;
  }

  @Nullable
  @TestOnly
  public String getDirCoverageInformationString(@Nonnull VirtualFile file) {
    DirCoverageInfo coverageInfo = myDirCoverageInfos.get(file.getPath());
    return coverageInfo != null ? getDirCoverageString(coverageInfo) : null;
  }

  @Nullable
  @Override
  public String getFileCoverageInformationString(@Nonnull PsiFile file,
                                                 @Nonnull CoverageSuitesBundle bundle,
                                                 @Nonnull CoverageDataManager manager) {
    FileCoverageInfo coverageInfo = myFileCoverageInfos.get(file.getVirtualFile().getPath());
    if (coverageInfo == null) {
      return null;
    }

    if (manager.isSubCoverageActive()) {
      return coverageInfo.coveredLineCount > 0 ? "covered" : null;
    }

    return getStatementsCoverageString(coverageInfo);
  }

  @Nullable
  @TestOnly
  public String getFileCoverageInformationString(@Nonnull VirtualFile file) {
    FileCoverageInfo coverageInfo = myFileCoverageInfos.get(file.getPath());
    return coverageInfo != null ? getStatementsCoverageString(coverageInfo) : null;
  }

  @Override
  public void onSuiteChosen(CoverageSuitesBundle newSuite) {
    super.onSuiteChosen(newSuite);
    myFileCoverageInfos.clear();
    myDirCoverageInfos.clear();
  }

  @Nullable
  @Override
  protected Runnable createRenewRequest(@Nonnull CoverageSuitesBundle bundle, @Nonnull CoverageDataManager manager) {
    GoCoverageProjectData data = new GoCoverageProjectData();
    for (CoverageSuite suite : bundle.getSuites()) {
      ProjectData toMerge = suite.getCoverageData(manager);
      if (toMerge != null) {
        data.merge(toMerge);
      }
    }

    return () -> {
      annotateAllFiles(data, manager.doInReadActionIfProjectOpen(() -> ProjectRootManager.getInstance(getProject()).getContentRoots()));
      manager.triggerPresentationUpdate();
    };
  }

  @Nonnull
  private DirCoverageInfo getOrCreateDirectoryInfo(VirtualFile file) {
    return myDirCoverageInfos.computeIfAbsent(file.getPath(), s -> new DirCoverageInfo());
  }

  @Nonnull
  private FileCoverageInfo getOrCreateFileInfo(VirtualFile file) {
    return myFileCoverageInfos.computeIfAbsent(file.getPath(), s -> new FileCoverageInfo());
  }

  @Nullable
  private static String getStatementsCoverageString(@Nonnull FileCoverageInfo info) {
    double percent = calcPercent(info.coveredLineCount, info.totalLineCount);
    return info.totalLineCount > 0 ? new DecimalFormat("##.#" + STATEMENTS_SUFFIX, DecimalFormatSymbols.getInstance(Locale.US))
      .format(percent) : null;
  }

  @Nullable
  private static String getFilesCoverageString(@Nonnull DirCoverageInfo info) {
    double percent = calcPercent(info.coveredFilesCount, info.totalFilesCount);
    return info.totalFilesCount > 0
           ? new DecimalFormat("##.#" + FILES_SUFFIX, DecimalFormatSymbols.getInstance(Locale.US)).format(percent)
           : null;
  }

  private static double calcPercent(int covered, int total) {
    return total != 0 ? (double)covered / total : 0;
  }

  public void annotateAllFiles(@Nonnull GoCoverageProjectData data,
                               @Nullable VirtualFile... contentRoots) {
    if (contentRoots != null) {
      for (VirtualFile root : contentRoots) {
        VirtualFileUtil.visitChildrenRecursively(root, new VirtualFileVisitor() {
          @Nonnull
          @Override
          public Result visitFileEx(@Nonnull VirtualFile file) {
            ProgressIndicatorProvider.checkCanceled();

            if (file.isDirectory() && !FileIndexFacade.getInstance(getProject()).isInContent(file)) {
              return SKIP_CHILDREN;
            }
            if (!file.isDirectory() && GoCoverageEngine.INSTANCE.coverageProjectViewStatisticsApplicableTo(file)) {
              DirCoverageInfo dirCoverageInfo = getOrCreateDirectoryInfo(file.getParent());
              FileCoverageInfo fileCoverageInfo = getOrCreateFileInfo(file);
              data.processFile(file.getPath(), rangeData -> {
                if (rangeData.hits > 0) {
                  fileCoverageInfo.coveredLineCount += rangeData.statements;
                }
                fileCoverageInfo.totalLineCount += rangeData.statements;
                return true;
              });

              if (fileCoverageInfo.totalLineCount > 0) {
                dirCoverageInfo.totalLineCount += fileCoverageInfo.totalLineCount;
                dirCoverageInfo.totalFilesCount++;
              }
              if (fileCoverageInfo.coveredLineCount > 0) {
                dirCoverageInfo.coveredLineCount += fileCoverageInfo.coveredLineCount;
                dirCoverageInfo.coveredFilesCount++;
              }
            }
            return CONTINUE;
          }

          @Override
          public void afterChildrenVisited(@Nonnull VirtualFile file) {
            if (file.isDirectory()) {
              DirCoverageInfo currentCoverageInfo = getOrCreateDirectoryInfo(file);
              DirCoverageInfo parentCoverageInfo = getOrCreateDirectoryInfo(file.getParent());
              parentCoverageInfo.totalFilesCount += currentCoverageInfo.totalFilesCount;
              parentCoverageInfo.coveredFilesCount += currentCoverageInfo.coveredFilesCount;
              parentCoverageInfo.totalLineCount += currentCoverageInfo.totalLineCount;
              parentCoverageInfo.coveredLineCount += currentCoverageInfo.coveredLineCount;
            }
          }
        });
      }
    }
  }
}

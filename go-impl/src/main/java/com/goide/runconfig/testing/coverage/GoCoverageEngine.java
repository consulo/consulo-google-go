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

import com.goide.GoFileType;
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.runconfig.testing.GoTestRunConfiguration;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.configuration.RunConfigurationBase;
import consulo.execution.coverage.*;
import consulo.execution.coverage.view.CoverageViewExtension;
import consulo.execution.coverage.view.DirectoryCoverageViewExtension;
import consulo.execution.test.AbstractTestProxy;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.module.Module;
import consulo.project.Project;
import consulo.project.ui.view.tree.AbstractTreeNode;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.Condition;
import consulo.virtualFileSystem.VirtualFile;

import org.jspecify.annotations.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

@ExtensionImpl
public class GoCoverageEngine extends CoverageEngine {
  private static final Condition<AbstractTreeNode> NODE_TO_COVERAGE = node -> {
    Object value = node.getValue();
    if (value instanceof PsiFile) {
      return isProductionGoFile((PsiFile)value);
    }
    return !StringUtil.equals(node.getName(), Project.DIRECTORY_STORE_FOLDER);
  };

  public static final GoCoverageEngine INSTANCE = new GoCoverageEngine();
  private static final String PRESENTABLE_TEXT = "Go Coverage";

  @Override
  public boolean isApplicableTo(@Nullable RunConfigurationBase conf) {
    return conf instanceof GoTestRunConfiguration;
  }

  @Override
  public boolean canHavePerTestCoverage(@Nullable RunConfigurationBase conf) {
    return false;
  }

  @Override
  public CoverageEnabledConfiguration createCoverageEnabledConfiguration(@Nullable RunConfigurationBase conf) {
    return new GoCoverageEnabledConfiguration((GoTestRunConfiguration)conf);
  }

  @Override
  public CoverageSuite createCoverageSuite(CoverageRunner runner,
                                           String name,
                                           CoverageFileProvider coverageDataFileProvider,
                                           @Nullable String[] filters,
                                           long lastCoverageTimeStamp,
                                           @Nullable String suiteToMerge,
                                           boolean coverageByTestEnabled,
                                           boolean tracingEnabled,
                                           boolean trackTestFolders,
                                           Project project) {
    return new GoCoverageSuite(name, coverageDataFileProvider, lastCoverageTimeStamp, runner, project);
  }

  @Override
  public CoverageSuite createCoverageSuite(CoverageRunner runner,
                                           String name,
                                           CoverageFileProvider coverageDataFileProvider,
                                           CoverageEnabledConfiguration config) {
    if (config instanceof GoCoverageEnabledConfiguration) {
      return new GoCoverageSuite(name, coverageDataFileProvider, new Date().getTime(), runner, config.getConfiguration().getProject());
    }
    return null;
  }

  @Override
  public CoverageSuite createEmptyCoverageSuite(CoverageRunner coverageRunner) {
    return new GoCoverageSuite();
  }

  @Override
  public CoverageAnnotator getCoverageAnnotator(Project project) {
    return GoCoverageAnnotator.getInstance(project);
  }

  @Override
  public boolean coverageEditorHighlightingApplicableTo(PsiFile psiFile) {
    return isProductionGoFile(psiFile);
  }

  @Override
  public boolean acceptedByFilters(PsiFile psiFile, CoverageSuitesBundle suite) {
    return isProductionGoFile(psiFile);
  }

  private static boolean isProductionGoFile(PsiFile psiFile) {
    return psiFile.getFileType() == GoFileType.INSTANCE && !GoTestFinder.isTestFile(psiFile);
  }

  @Override
  public boolean recompileProjectAndRerunAction(Module module,
                                                CoverageSuitesBundle suite,
                                                Runnable chooseSuiteAction) {
    return false;
  }

  @Override
  public String getQualifiedName(File outputFile, PsiFile sourceFile) {
    return sourceFile.getVirtualFile().getPath();
  }

  @Override
  public Set<String> getQualifiedNames(PsiFile sourceFile) {
    return Collections.singleton(sourceFile.getVirtualFile().getPath());
  }

  @Override
  public boolean includeUntouchedFileInCoverage(String qualifiedName,
                                                File outputFile,
                                                PsiFile sourceFile,
                                                CoverageSuitesBundle suite) {
    return false;
  }

  @Override
  public List<Integer> collectSrcLinesForUntouchedFile(File classFile, CoverageSuitesBundle suite) {
    return null;
  }

  @Override
  public List<PsiElement> findTestsByNames(String[] testNames, Project project) {
    return Collections.emptyList();
  }

  @Override
  public String getTestMethodName(PsiElement element, AbstractTestProxy testProxy) {
    return null;
  }

  @Override
  public String getPresentableText() {
    return PRESENTABLE_TEXT;
  }

  @Override
  public boolean coverageProjectViewStatisticsApplicableTo(VirtualFile fileOrDir) {
    return !fileOrDir.isDirectory() && fileOrDir.getFileType() == GoFileType.INSTANCE && !GoTestFinder.isTestFile(fileOrDir);
  }

  @Override
  public CoverageViewExtension createCoverageViewExtension(Project project,
                                                           CoverageSuitesBundle suiteBundle,
                                                           CoverageViewManager.StateBean stateBean) {
    return new DirectoryCoverageViewExtension(project, getCoverageAnnotator(project), suiteBundle, stateBean) {
      @Override
      public List<AbstractTreeNode> getChildrenNodes(AbstractTreeNode node) {
        return ContainerUtil.filter(super.getChildrenNodes(node), NODE_TO_COVERAGE);
      }
    };
  }
}

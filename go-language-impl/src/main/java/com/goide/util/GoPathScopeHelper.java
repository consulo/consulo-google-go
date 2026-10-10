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

package com.goide.util;

import com.goide.project.GoVendoringUtil;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.sdk.GoSdkService;
import com.goide.sdk.GoSdkUtil;
import consulo.application.ApplicationManager;
import consulo.go.GoConstants;
import consulo.go.modules.GoImportPathMap;
import consulo.module.Module;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import java.util.LinkedHashSet;
import org.jspecify.annotations.Nullable;
import java.util.Set;

public class GoPathScopeHelper {
  private final Set<VirtualFile> myRoots;
  @Nullable
  private final GoImportPathMap myImportPathMap;
  @Nullable
  private final VirtualFile mySdkHome;
  private final boolean mySupportsInternalPackages;
  private final boolean mySupportsSdkInternalPackages;
  private final boolean myVendoringEnabled;

  public static GoPathScopeHelper fromReferenceFile(Project project,
                                                    @Nullable Module module,
                                                    @Nullable VirtualFile referenceFile) {
    VirtualFile sdkHome = GoSdkUtil.getSdkSrcDir(project, module);
    String sdkVersion = GoSdkService.getInstance(project).getSdkVersion(module);
    boolean supportsInternalPackages = GoVendoringUtil.supportsInternalPackages(sdkVersion);
    boolean supportsSdkInternalPackages = GoVendoringUtil.supportsSdkInternalPackages(sdkVersion);
    boolean vendoringEnabled = GoVendoringUtil.isVendoringEnabled(module);
    Set<VirtualFile> sourceRoots = vendoringEnabled ? GoSdkUtil.getVendoringAwareSourcesPathsToLookup(project, module, referenceFile)
                                                    : GoSdkUtil.getSourcesPathsToLookup(project, module);
    GoImportPathMap importPathMap = GoImportPathMap.forFile(project, module, referenceFile);
    if (importPathMap != null) {
      sourceRoots = new LinkedHashSet<>(sourceRoots);
      sourceRoots.addAll(importPathMap.getRootDirectories());
    }
    return new GoPathScopeHelper(sourceRoots, importPathMap, sdkHome, supportsInternalPackages, supportsSdkInternalPackages,
                                 vendoringEnabled);
  }

  private GoPathScopeHelper(Set<VirtualFile> roots,
                            @Nullable GoImportPathMap importPathMap,
                            @Nullable VirtualFile sdkHome,
                            boolean supportsInternalPackages,
                            boolean supportsSdkInternalPackages,
                            boolean vendoringEnabled) {
    myRoots = roots;
    myImportPathMap = importPathMap;
    mySdkHome = sdkHome;
    mySupportsInternalPackages = supportsInternalPackages;
    mySupportsSdkInternalPackages = supportsSdkInternalPackages;
    myVendoringEnabled = vendoringEnabled;
  }

  public boolean couldBeReferenced(VirtualFile declarationFile, @Nullable VirtualFile referenceFile) {
    VirtualFile declarationDirectory = declarationFile.getParent();
    if (declarationDirectory == null) {
      return true;
    }

    if (ApplicationManager.getApplication().isUnitTestMode() && myRoots.contains(declarationDirectory)) {
      return true;
    }

    String importPath = myImportPathMap != null ? myImportPathMap.getImportPath(declarationDirectory) : null;
    if (importPath == null) {
      importPath = GoSdkUtil.getRelativePathToRoots(declarationDirectory, myRoots);
    }
    if (importPath == null) {
      return false;
    }
    if (importPath.isEmpty()) {
      return true;
    }

    VirtualFile referenceDirectory = referenceFile != null ? referenceFile.getParent() : null;
    if (referenceDirectory != null) {
      if (myVendoringEnabled && GoSdkUtil.isUnreachableVendoredPackage(declarationDirectory, referenceDirectory, myRoots)) {
        return false;
      }
      boolean declarationIsInSdk = mySdkHome != null && VirtualFileUtil.isAncestor(mySdkHome, declarationDirectory, false);
      if (mySupportsInternalPackages || mySupportsSdkInternalPackages && declarationIsInSdk) {
        if (GoSdkUtil.isUnreachableInternalPackage(declarationDirectory, referenceDirectory, myRoots)) {
          return false;
        }
      }
      if (declarationIsInSdk && GoSdkUtil.findParentDirectory(declarationDirectory, myRoots, GoConstants.TESTDATA_NAME) != null) {
        return false;
      }
      else {
        boolean referenceIsInSdk = mySdkHome != null && VirtualFileUtil.isAncestor(mySdkHome, referenceDirectory, false);
        if (referenceIsInSdk) {
          return false;
        }
      }
    }
    return GoPsiImplUtil.allowed(declarationFile, referenceFile) && !isShadowedImportPath(declarationDirectory, importPath);
  }

  private boolean isShadowedImportPath(VirtualFile targetDirectory, String targetImportPath) {
    assert targetDirectory.isDirectory();
    VirtualFile moduleDirectory = myImportPathMap != null ? myImportPathMap.findDirectory(targetImportPath) : null;
    if (moduleDirectory != null) {
      return !targetDirectory.equals(moduleDirectory);
    }
    for (VirtualFile root : myRoots) {
      VirtualFile realDirectoryToResolve = root.findFileByRelativePath(targetImportPath);
      if (realDirectoryToResolve != null) {
        return !targetDirectory.equals(realDirectoryToResolve);
      }
    }
    return false;
  }

  public Set<VirtualFile> getRoots() {
    return myRoots;
  }

  public boolean isVendoringEnabled() {
    return myVendoringEnabled;
  }
}

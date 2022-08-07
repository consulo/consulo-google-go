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

package com.goide.inspections;

import com.goide.GoConstants;
import com.goide.project.GoVendoringUtil;
import com.goide.psi.GoFile;
import com.goide.psi.GoImportSpec;
import com.goide.psi.impl.imports.GoImportReference;
import com.goide.quickfix.GoDeleteImportQuickFix;
import com.goide.quickfix.GoDisableVendoringInModuleQuickFix;
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.sdk.GoPackageUtil;
import com.goide.sdk.GoSdkService;
import com.goide.sdk.GoSdkUtil;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.intention.HighPriorityAction;
import consulo.language.psi.ElementManipulators;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

public abstract class GoInvalidPackageImportInspection extends GoInspectionBase {
  public static final String DELETE_ALIAS_QUICK_FIX_NAME = "Delete alias";

  @Override
  protected void checkFile(@Nonnull GoFile file, @Nonnull ProblemsHolder problemsHolder) {
    Module module = ModuleUtilCore.findModuleForPsiElement(file);
    VirtualFile sdkHome = GoSdkUtil.getSdkSrcDir(file.getProject(), module);
    boolean supportsVendoring = GoVendoringUtil.isVendoringEnabled(module);
    String sdkVersion = GoSdkService.getInstance(file.getProject()).getSdkVersion(module);
    boolean supportsInternalPackages = GoVendoringUtil.supportsInternalPackages(sdkVersion);
    boolean supportsInternalPackagesInSdk = sdkHome != null && GoVendoringUtil.supportsSdkInternalPackages(sdkVersion);

    for (GoImportSpec importSpec : file.getImports()) {
      if (importSpec.isCImport()) {
        PsiElement dot = importSpec.getDot();
        if (dot != null) {
          problemsHolder.registerProblem(importSpec, "Cannot rename import `C`", new GoDeleteImportSpecAlias(),
                                         new GoDeleteImportQuickFix());
        }
        PsiElement identifier = importSpec.getIdentifier();
        if (identifier != null) {
          problemsHolder.registerProblem(importSpec, "Cannot import 'builtin' package", new GoDeleteImportSpecAlias(),
                                         new GoDeleteImportQuickFix());
        }
        continue;
      }

      PsiDirectory resolve = importSpec.getImportString().resolve();
      if (resolve != null) {
        if (GoPackageUtil.isBuiltinPackage(resolve)) {
          problemsHolder.registerProblem(importSpec, "Cannot import 'builtin' package", new GoDeleteImportQuickFix());
        }
        Collection<String> packagesInDirectory = GoPackageUtil.getAllPackagesInDirectory(resolve, module, true);
        if (packagesInDirectory.isEmpty()) {
          problemsHolder.registerProblem(importSpec, "'" + resolve.getVirtualFile().getPath() + "' has no buildable Go source files",
                                         new GoDeleteImportQuickFix());
          continue;
        }
        if (!GoTestFinder.isTestFile(file) && packagesInDirectory.size() == 1 && packagesInDirectory.contains(GoConstants.MAIN)) {
          problemsHolder.registerProblem(importSpec, "'" + importSpec.getPath() + "' is a program, not an importable package",
                                         new GoDeleteImportQuickFix());
          continue;
        }
        if (packagesInDirectory.size() > 1) {
          problemsHolder.registerProblem(importSpec, "Found several packages [" + StringUtil.join(packagesInDirectory, ", ") + "] in '" +
                                                     resolve.getVirtualFile().getPath() + "'", new GoDeleteImportQuickFix());
          continue;
        }

        VirtualFile contextFile = file.getVirtualFile();
        VirtualFile resolvedFile = resolve.getVirtualFile();

        boolean resolvedToSdk = sdkHome != null && VirtualFileUtil.isAncestor(sdkHome, resolvedFile, false);
        boolean validateInternal = supportsInternalPackages || supportsInternalPackagesInSdk && resolvedToSdk;
        if (supportsVendoring || validateInternal || resolvedToSdk) {
          Set<VirtualFile> sourceRoots = GoSdkUtil.getSourcesPathsToLookup(file.getProject(), module);
          for (PsiReference reference : importSpec.getImportString().getReferences()) {
            if (reference instanceof GoImportReference) {
              String canonicalText = reference.getCanonicalText();
              if (resolvedToSdk && GoConstants.TESTDATA_NAME.equals(canonicalText)) {
                problemsHolder.registerProblem(importSpec, "Use of testdata package from SDK is not allowed", new GoDeleteImportQuickFix());
                break;
              }
              else if (validateInternal && GoConstants.INTERNAL.equals(canonicalText)) {
                if (GoSdkUtil.isUnreachableInternalPackage(resolvedFile, contextFile, sourceRoots)) {
                  problemsHolder.registerProblem(importSpec, "Use of internal package is not allowed", new GoDeleteImportQuickFix());
                  break;
                }
              }
              else if (supportsVendoring && GoConstants.VENDOR.equals(canonicalText)) {
                if (GoSdkUtil.isUnreachableVendoredPackage(resolvedFile, contextFile, sourceRoots)) {
                  problemsHolder.registerProblem(importSpec, "Use of vendored package is not allowed",
                                                 new GoDeleteImportQuickFix(), GoDisableVendoringInModuleQuickFix.create(module));
                  break;
                }
                else {
                  String vendoredImportPath = GoSdkUtil.getImportPath(resolve, true);
                  if (vendoredImportPath != null) {
                    problemsHolder.registerProblem(importSpec, "Must be imported as '" + vendoredImportPath + "'",
                                                   new GoReplaceImportPath(vendoredImportPath),
                                                   new GoDeleteImportQuickFix(), GoDisableVendoringInModuleQuickFix.create(module));
                    break;
                  }
                }
              }
            }
          }
        }
      }
      else {
        for (PsiReference reference : importSpec.getImportString().getReferences()) {
          if (reference instanceof GoImportReference) {
            if (((GoImportReference)reference).getFileReferenceSet().isAbsolutePathReference()) {
              problemsHolder.registerProblem(importSpec, "Cannot import absolute path", new GoDeleteImportQuickFix());
              break;
            }
          }
        }
      }
    }
  }

  private static class GoDeleteImportSpecAlias extends LocalQuickFixBase {
    protected GoDeleteImportSpecAlias() {
      super(DELETE_ALIAS_QUICK_FIX_NAME);
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      GoImportSpec element = ObjectUtil.tryCast(descriptor.getPsiElement(), GoImportSpec.class);
      if (element != null) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
          PsiElement dot = element.getDot();
          if (dot != null) {
            dot.delete();
            return;
          }

          PsiElement identifier = element.getIdentifier();
          if (identifier != null) {
            identifier.delete();
          }
        });
      }
    }
  }

  private static class GoReplaceImportPath extends LocalQuickFixBase implements HighPriorityAction {
    @Nonnull
	private final String myNewImportPath;

    protected GoReplaceImportPath(@Nonnull String newImportPath) {
      super("Replace with '" + newImportPath + "'");
      myNewImportPath = newImportPath;
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      GoImportSpec element = ObjectUtil.tryCast(descriptor.getPsiElement(), GoImportSpec.class);
      if (element != null) {
        ElementManipulators.handleContentChange(element.getImportString(), myNewImportPath);
      }
    }
  }
}

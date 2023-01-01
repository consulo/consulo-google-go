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

package com.goide.quickfix;

import com.goide.GoConstants;
import com.goide.psi.GoFile;
import com.goide.psi.GoPackageClause;
import com.goide.psi.impl.GoElementFactory;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.runconfig.testing.GoTestFinder;
import consulo.codeEditor.Editor;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalQuickFixAndIntentionActionOnPsiElement;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.ex.awt.IdeBorderFactory;
import consulo.ui.ex.awt.JBLabel;
import consulo.ui.ex.awt.JBList;
import consulo.ui.ex.popup.JBPopup;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class GoMultiplePackagesQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
  private static String myTestingPackageName;
  private final Collection<String> myPackages;
  private final String myPackageName;
  private final boolean myIsOneTheFly;

  public GoMultiplePackagesQuickFix(@Nonnull PsiElement element,
                                    @Nonnull String packageName,
                                    @Nonnull Collection<String> packages,
                                    boolean isOnTheFly) {
    super(element);
    myPackages = packages;
    myPackageName = packageName;
    myIsOneTheFly = isOnTheFly;
  }

  private static void renamePackagesInDirectory(@Nonnull Project project,
                                                @Nonnull PsiDirectory dir,
                                                @Nonnull String newName) {
    WriteCommandAction.runWriteCommandAction(project, () -> {
      Module module = ModuleUtilCore.findModuleForPsiElement(dir);
      for (PsiFile file : dir.getFiles()) {
        if (file instanceof GoFile && GoPsiImplUtil.allowed(file, null, module)) {
          GoPackageClause packageClause = ((GoFile)file).getPackage();
          String oldName = ((GoFile)file).getPackageName();
          if (packageClause != null && oldName != null) {
            String fullName = GoTestFinder.isTestFile(file) && StringUtil.endsWith(oldName, GoConstants.TEST_SUFFIX)
                              ? newName + GoConstants.TEST_SUFFIX
                              : newName;
            packageClause.replace(GoElementFactory.createPackageClause(project, fullName));
          }
        }
      }
    });
  }

  @TestOnly
  public static void setTestingPackageName(@Nonnull String packageName, @Nonnull Disposable disposable) {
    myTestingPackageName = packageName;
    Disposer.register(disposable, () -> {
      //noinspection AssignmentToStaticFieldFromInstanceMethod
      myTestingPackageName = null;
    });
  }

  @Override
  public void invoke(@Nonnull Project project,
                     @Nonnull PsiFile file,
                     @Nullable Editor editor,
                     @Nonnull PsiElement startElement,
                     @Nonnull PsiElement endElement) {
    if (editor == null || myTestingPackageName != null) {
      renamePackagesInDirectory(project, file.getContainingDirectory(),
                                myTestingPackageName != null ? myTestingPackageName : myPackageName);
      return;
    }

    JBPopup popup = JBPopupFactory.getInstance().createPopupChooserBuilder(new ArrayList<>(myPackages)).setTitle("Choose package name").setItemChosenCallback((name) -> {
      if (name != null) {
        renamePackagesInDirectory(project, file.getContainingDirectory(), name);
      }
    }).setRenderer((list1, value, index, isSelected, cellHasFocus) -> {
      JBLabel label = new JBLabel(value.toString());
      label.setBorder(IdeBorderFactory.createEmptyBorder(2, 4, 2, 4));
      return label;
    }).createPopup();

    editor.showPopupInBestPositionFor(popup);
  }

  @Nonnull
  @Override
  public String getText() {
    return "Rename packages" + (myIsOneTheFly ? "" : " to " + myPackageName);
  }

  @Nonnull
  @Override
  public String getFamilyName() {
    return "Rename packages";
  }
}

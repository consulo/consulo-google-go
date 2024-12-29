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

package com.goide.codeInsight.imports;

import com.goide.sdk.GoSdkService;
import com.goide.util.GoExecutor;
import consulo.language.editor.inspection.LocalQuickFixBase;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.intention.HighPriorityAction;
import consulo.language.psi.PsiElement;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.undoRedo.CommandProcessor;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFileManager;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.function.Consumer;

public class GoGetPackageFix extends LocalQuickFixBase implements HighPriorityAction {
  @Nonnull
  private final String myPackage;

  public GoGetPackageFix(@Nonnull String packageName) {
    super("go get -t " + packageName + "/...", "go get");
    myPackage = packageName;
  }

  public static void applyFix(@Nonnull Project project,
                              @Nullable Module module,
                              @Nonnull String packageName,
                              boolean startInBackground) {
    String sdkPath = GoSdkService.getInstance(project).getSdkHomePath(module);
    if (StringUtil.isEmpty(sdkPath)) return;
    CommandProcessor.getInstance().runUndoTransparentAction(() -> {
      Consumer<Boolean> consumer = aBoolean -> VirtualFileManager.getInstance().asyncRefresh(null);
      GoExecutor.in(project, module).withPresentableName("go get -t " + packageName + "/...")
        .withParameters("get", "-t", packageName+"/...").showNotifications(false, true).showOutputOnError()
        .executeWithProgress(!startInBackground, consumer);
    });
  }

  @Override
  public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    if (element != null) {
      applyFix(project, ModuleUtilCore.findModuleForPsiElement(element.getContainingFile()), myPackage, true);
    }
  }
}

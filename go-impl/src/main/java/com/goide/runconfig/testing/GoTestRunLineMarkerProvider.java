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

package com.goide.runconfig.testing;

import com.goide.GoLanguage;
import com.goide.GoTypes;
import com.goide.psi.GoFunctionDeclaration;
import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.GoMethodDeclaration;
import com.goide.psi.GoReceiver;
import com.goide.runconfig.GoRunUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.icon.ExecutionIconGroup;
import consulo.execution.lineMarker.ExecutorAction;
import consulo.execution.lineMarker.RunLineMarkerContributor;
import consulo.execution.test.TestIconMapper;
import consulo.execution.test.TestStateInfo;
import consulo.execution.test.TestStateStorage;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.ui.image.Image;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.function.Function;

@ExtensionImpl
public class GoTestRunLineMarkerProvider extends RunLineMarkerContributor {
  private static final Function<PsiElement, String> TOOLTIP_PROVIDER = element -> "Run Test";

  @RequiredReadAction
  @Nullable
  @Override
  public Info getInfo(PsiElement e) {
    if (e != null && e.getNode().getElementType() == GoTypes.IDENTIFIER) {
      PsiElement parent = e.getParent();
      PsiFile file = e.getContainingFile();
      if (!GoTestFinder.isTestFile(file)) {
        return null;
      }
      if (GoRunUtil.isPackageContext(e)) {
        return new Info(ExecutionIconGroup.gutterRerun(), TOOLTIP_PROVIDER, ExecutorAction.getActions(0));
      }
      else if (parent instanceof GoFunctionOrMethodDeclaration) {
        GoTestFunctionType functionType = GoTestFunctionType.fromName(((GoFunctionOrMethodDeclaration)parent).getName());
        if (functionType != null) {
          if (parent instanceof GoFunctionDeclaration) {
            return getInfo(GoTestLocator.PROTOCOL + "://" + ((GoFunctionDeclaration)parent).getName(), e.getProject());
          }
          else if (parent instanceof GoMethodDeclaration) {
            GoReceiver receiver = ((GoMethodDeclaration)parent).getReceiver();
            PsiElement receiverIdentifier = receiver != null ? receiver.getIdentifier() : null;
            String receiverText = receiverIdentifier != null ? receiverIdentifier.getText() + "." : "";
            return getInfo(GoTestLocator.PROTOCOL + "://" + receiverText + ((GoMethodDeclaration)parent).getName(), e.getProject());
          }
        }
      }
    }
    return null;
  }

  @Nonnull
  private static Info getInfo(String url, Project project) {
    Image icon = getTestStateIcon(url, project);
    return new Info(icon, TOOLTIP_PROVIDER, ExecutorAction.getActions(0));
  }

  private static Image getTestStateIcon(@Nonnull String url, @Nonnull Project project) {
    TestStateStorage.Record state = TestStateStorage.getInstance(project).getState(url);
    if (state != null) {
      TestStateInfo.Magnitude magnitude = TestIconMapper.getMagnitude(state.magnitude);
      if (magnitude != null) {
        switch (magnitude) {
          case ERROR_INDEX:
          case FAILED_INDEX:
            return ExecutionIconGroup.gutterRunerror();
          case PASSED_INDEX:
          case COMPLETE_INDEX:
            return ExecutionIconGroup.gutterRunsuccess();
          default:
        }
      }
    }
    return ExecutionIconGroup.gutterRun();
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}

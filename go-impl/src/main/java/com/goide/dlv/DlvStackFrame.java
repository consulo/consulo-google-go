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

package com.goide.dlv;

import com.goide.GoIcons;
import com.goide.dlv.protocol.DlvApi;
import com.goide.dlv.protocol.DlvRequest;
import com.goide.psi.*;
import com.goide.sdk.GoSdkService;
import consulo.application.dumb.IndexNotReadyException;
import consulo.application.util.SystemInfo;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunProfile;
import consulo.execution.debug.XDebuggerUtil;
import consulo.execution.debug.XSourcePosition;
import consulo.execution.debug.evaluation.XDebuggerEvaluator;
import consulo.execution.debug.frame.XCompositeNode;
import consulo.execution.debug.frame.XStackFrame;
import consulo.execution.debug.frame.XValue;
import consulo.execution.debug.frame.XValueChildrenList;
import consulo.execution.debug.icon.ExecutionDebugIconGroup;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.ex.ColoredTextContainer;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.image.Image;
import consulo.util.concurrent.AsyncResult;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.Ref;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

public class DlvStackFrame extends XStackFrame {
  private final DlvDebugProcess myProcess;
  private final DlvApi.Location myLocation;
  private final DlvCommandProcessor myProcessor;
  private final int myId;
  private final int myGoroutineId;

  public DlvStackFrame(@Nonnull DlvDebugProcess process, @Nonnull DlvApi.Location location, @Nonnull DlvCommandProcessor processor, int id, int goroutineId) {
    myProcess = process;
    myLocation = location;
    myProcessor = processor;
    myId = id;
    myGoroutineId = goroutineId;
  }

  @Nullable
  @Override
  public XDebuggerEvaluator getEvaluator() {
    return new XDebuggerEvaluator() {
      @Override
      public void evaluate(@Nonnull String expression, @Nonnull XEvaluationCallback callback, @Nullable XSourcePosition expressionPosition) {
        myProcessor.send(new DlvRequest.Eval(expression, myId, myGoroutineId))
                .doWhenDone(variable -> callback.evaluated(createXValue(variable.Variable, ExecutionDebugIconGroup.nodeWatch())))
                .doWhenRejectedWithThrowable(throwable -> callback.errorOccurred(throwable.getMessage()));
      }

      @Nullable
      private PsiElement findElementAt(@Nullable PsiFile file, int offset) {
        return file != null ? file.findElementAt(offset) : null;
      }

      @Nullable
      @Override
      public TextRange getExpressionRangeAtOffset(@Nonnull Project project, @Nonnull Document document, int offset, boolean sideEffectsAllowed) {
        Ref<TextRange> currentRange = Ref.create(null);
        PsiDocumentManager.getInstance(project).commitAndRunReadAction(() -> {
          try {
            PsiElement elementAtCursor = findElementAt(PsiDocumentManager.getInstance(project).getPsiFile(document), offset);
            GoTypeOwner e =
                    PsiTreeUtil.getParentOfType(elementAtCursor, GoExpression.class, GoVarDefinition.class, GoConstDefinition.class, GoParamDefinition.class);
            if (e != null) {
              currentRange.set(e.getTextRange());
            }
          }
          catch (IndexNotReadyException ignored) {
          }
        });
        return currentRange.get();
      }
    };
  }

  @Nonnull
  private XValue createXValue(@Nonnull DlvApi.Variable variable, @Nullable Image icon) {
    return new DlvXValue(myProcess, variable, myProcessor, myId, myGoroutineId, icon);
  }

  @Nullable
  @Override
  public XSourcePosition getSourcePosition() {
    VirtualFile file = findFile();
    return file == null ? null : XDebuggerUtil.getInstance().createPosition(file, myLocation.line - 1);
  }

  @Nullable
  private VirtualFile findFile() {
    String url = myLocation.file;
    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(url);
    if (file == null && SystemInfo.isWindows) {
      Project project = myProcess.getSession().getProject();
      RunProfile profile = myProcess.getSession().getRunProfile();
      Module module = profile instanceof ModuleBasedConfiguration ? ((ModuleBasedConfiguration)profile).getConfigurationModule().getModule() : null;
      String sdkHomePath = GoSdkService.getInstance(project).getSdkHomePath(module);
      if (sdkHomePath == null) return null;
      String newUrl = StringUtil.replaceIgnoreCase(url, "c:/go", sdkHomePath);
      return LocalFileSystem.getInstance().findFileByPath(newUrl);
    }
    return file;
  }

  @Override
  public void customizePresentation(@Nonnull ColoredTextContainer component) {
    super.customizePresentation(component);
    component.append(" at " + myLocation.function.name, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    component.setIcon(ExecutionDebugIconGroup.nodeFrame());
  }

  @Nonnull
  private <T> AsyncResult<T> send(@Nonnull DlvRequest<T> request) {
    return DlvDebugProcess.send(request, myProcessor);
  }

  @Override
  public void computeChildren(@Nonnull XCompositeNode node) {
    send(new DlvRequest.ListLocalVars(myId, myGoroutineId)).doWhenDone(variablesOut -> {
      List<DlvApi.Variable> variables = variablesOut.Variables;
      XValueChildrenList xVars = new XValueChildrenList(variables.size());
      for (DlvApi.Variable v : variables) xVars.add(v.name, createXValue(v, GoIcons.VARIABLE));
      send(new DlvRequest.ListFunctionArgs(myId, myGoroutineId)).doWhenDone(vars -> {
        List<DlvApi.Variable> args = vars.Args;
        for (DlvApi.Variable v : args) xVars.add(v.name, createXValue(v, GoIcons.PARAMETER));
        node.addChildren(xVars, true);
      });
    });
  }
}
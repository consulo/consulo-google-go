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

import com.goide.dlv.protocol.DlvApi;
import com.goide.dlv.protocol.DlvRequest;
import com.goide.psi.GoNamedElement;
import com.goide.psi.GoTopLevelDeclaration;
import com.goide.psi.GoTypeSpec;
import com.goide.stubs.index.GoTypesIndex;
import consulo.application.AllIcons;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.XDebuggerUtil;
import consulo.execution.debug.XSourcePosition;
import consulo.execution.debug.frame.*;
import consulo.execution.debug.frame.presentation.XNumericValuePresentation;
import consulo.execution.debug.frame.presentation.XRegularValuePresentation;
import consulo.execution.debug.frame.presentation.XStringValuePresentation;
import consulo.execution.debug.frame.presentation.XValuePresentation;
import consulo.fileEditor.FileEditorManager;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SyntaxTraverser;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ThreeState;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

class DlvXValue extends XNamedValue {
  @Nonnull
  private final DlvApi.Variable myVariable;
  private final Image myIcon;
  private final DlvDebugProcess myProcess;
  private final DlvCommandProcessor myProcessor;
  private final int myFrameId;
  private final int myGoroutineId;

  public DlvXValue(@Nonnull DlvDebugProcess process,
                   @Nonnull DlvApi.Variable variable,
                   @Nonnull DlvCommandProcessor processor,
                   int frameId,
                   int goroutineId,
                   @Nullable Image icon) {
    super(variable.name);
    myProcess = process;
    myVariable = variable;
    myIcon = icon;
    myProcessor = processor;
    myFrameId = frameId;
    myGoroutineId = goroutineId;
  }

  @Override
  public void computePresentation(@Nonnull XValueNode node, @Nonnull XValuePlace place) {
    XValuePresentation presentation = getPresentation();
    boolean hasChildren = myVariable.children.length > 0;
    node.setPresentation(myIcon, presentation, hasChildren);
  }

  @Override
  public void computeChildren(@Nonnull XCompositeNode node) {
    DlvApi.Variable[] children = myVariable.children;
    if (children.length == 0) {
      super.computeChildren(node);
    }
    else {
      XValueChildrenList list = new XValueChildrenList();
      for (DlvApi.Variable child : children) {
        list.add(child.name, new DlvXValue(myProcess, child, myProcessor, myFrameId, myGoroutineId, AllIcons.Nodes.Field));
      }
      node.addChildren(list, true);
    }
  }

  @Nullable
  @Override
  public XValueModifier getModifier() {
    return new XValueModifier() {
      @Override
      public void setValue(@Nonnull String newValue, @Nonnull final XModificationCallback callback) {
        myProcessor.send(new DlvRequest.Set(myVariable.name, newValue, myFrameId, myGoroutineId)).doWhenDone(o -> {
          if (o != null) {
            callback.valueModified();
          }
        }).doWhenRejectedWithThrowable(throwable -> callback.errorOccurred(throwable.getMessage()));
      }
    };
  }

  @Nonnull
  private XValuePresentation getPresentation() {
    String value = myVariable.value;
    if (myVariable.isNumber()) return new XNumericValuePresentation(value);
    if (myVariable.isString()) return new XStringValuePresentation(value);
    if (myVariable.isBool()) {
      return new XValuePresentation() {
        @Override
        public void renderValue(@Nonnull XValueTextRenderer renderer) {
          renderer.renderValue(value);
        }
      };
    }
    String type = myVariable.type;
    boolean isSlice = myVariable.isSlice();
    boolean isArray = myVariable.isArray();
    if (isSlice || isArray) {
      return new XRegularValuePresentation("len:" + myVariable.len + (isSlice ? ", cap:" + myVariable.cap : ""), type.replaceFirst("struct ", ""));
    }
    String prefix = myVariable.type + " ";
    return new XRegularValuePresentation(StringUtil.startsWith(value, prefix) ? value.replaceFirst(Pattern.quote(prefix), "") : value, type);
  }

  @Nullable
  private static PsiElement findTargetElement(@Nonnull Project project, @Nonnull XSourcePosition position, @Nonnull Editor editor, @Nonnull String name) {
    PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    if (file == null || !file.getVirtualFile().equals(position.getFile())) return null;
    ASTNode leafElement = file.getNode().findLeafElementAt(position.getOffset());
    if (leafElement == null) return null;
    GoTopLevelDeclaration topLevel = PsiTreeUtil.getTopmostParentOfType(leafElement.getPsi(), GoTopLevelDeclaration.class);
    SyntaxTraverser<PsiElement> traverser =
            SyntaxTraverser.psiTraverser(topLevel).filter(e -> e instanceof GoNamedElement && Comparing.equal(name, ((GoNamedElement)e).getName()));
    Iterator<PsiElement> iterator = traverser.iterator();
    return iterator.hasNext() ? iterator.next() : null;
  }

  @Override
  public void computeSourcePosition(@Nonnull XNavigatable navigatable) {
    readActionInPooledThread(new Runnable() {
      @Override
      public void run() {
        navigatable.setSourcePosition(findPosition());
      }

      @Nullable
      private XSourcePosition findPosition() {
        XDebugSession debugSession = getSession();
        if (debugSession == null) return null;
        XStackFrame stackFrame = debugSession.getCurrentStackFrame();
        if (stackFrame == null) return null;
        Project project = debugSession.getProject();
        XSourcePosition position = debugSession.getCurrentPosition();
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor(true);
        if (editor == null || position == null) return null;
        String name = myName.startsWith("&") ? myName.replaceFirst("\\&", "") : myName;
        PsiElement resolved = findTargetElement(project, position, editor, name);
        if (resolved == null) return null;
        VirtualFile virtualFile = resolved.getContainingFile().getVirtualFile();
        return XDebuggerUtil.getInstance().createPositionByOffset(virtualFile, resolved.getTextOffset());
      }
    });
  }

  private static void readActionInPooledThread(@Nonnull Runnable runnable) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> ApplicationManager.getApplication().runReadAction(runnable));
  }

  @Nullable
  private Project getProject() {
    XDebugSession session = getSession();
    return session != null ? session.getProject() : null;
  }

  @Nullable
  private XDebugSession getSession() {
    return myProcess.getSession();
  }

  @Nonnull
  @Override
  public ThreeState computeInlineDebuggerData(@Nonnull XInlineDebuggerDataCallback callback) {
    computeSourcePosition(callback::computed);
    return ThreeState.YES;
  }

  @Override
  public boolean canNavigateToSource() {
    return true; // for the future compatibility
  }

  @Override
  public boolean canNavigateToTypeSource() {
    return (myVariable.isStructure() || myVariable.isPtr()) && getProject() != null;
  }

  @Override
  public void computeTypeSourcePosition(@Nonnull XNavigatable navigatable) {
    readActionInPooledThread(() -> {
      boolean isStructure = myVariable.isStructure();
      boolean isPtr = myVariable.isPtr();
      if (!isStructure && !isPtr) return;
      Project project = getProject();
      if (project == null) return;
      String dlvType = myVariable.type;
      String fqn = dlvType.replaceFirst(isPtr ? "\\*struct " : "struct ", "");
      List<String> split = StringUtil.split(fqn, ".");
      boolean noFqn = split.size() == 1;
      if (split.size() == 2 || noFqn) {
        String name = ContainerUtil.getLastItem(split);
        assert name != null;
        Collection<GoTypeSpec> types = GoTypesIndex.find(name, project, GlobalSearchScope.allScope(project), null);
        for (GoTypeSpec type : types) {
          if (noFqn || Comparing.equal(fqn, type.getQualifiedName())) {
            navigatable.setSourcePosition(XDebuggerUtil.getInstance().createPositionByOffset(type.getContainingFile().getVirtualFile(), type.getTextOffset()));
            return;
          }
        }
      }
    });
  }
}
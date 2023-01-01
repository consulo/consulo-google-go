/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.dlv.breakpoint;

import com.goide.GoParserDefinition;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processor;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.execution.debug.XDebuggerUtil;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ExtensionImpl
public class DlvBreakpointType extends XLineBreakpointType<DlvBreakpointProperties> {
  @Nonnull
  public static DlvBreakpointType getInstance() {
    return EXTENSION_POINT_NAME.findExtensionOrFail(DlvBreakpointType.class);
  }

  @Inject
  protected DlvBreakpointType() {
    super("DlvLineBreakpoint", "Dlv breakpoint");
  }

  @Nullable
  @Override
  public DlvBreakpointProperties createBreakpointProperties(@Nonnull VirtualFile file, int line) {
    return new DlvBreakpointProperties();
  }

  @RequiredReadAction
  public static boolean isLineBreakpointAvailable(@Nonnull VirtualFile file, int line, @Nonnull Project project) {
    Document document = FileDocumentManager.getInstance().getDocument(file);
    if (document == null || document.getLineEndOffset(line) == document.getLineStartOffset(line)) return false;
    Checker canPutAtChecker = new Checker();
    XDebuggerUtil.getInstance().iterateLine(project, document, line, canPutAtChecker);
    return canPutAtChecker.isLineBreakpointAvailable();
  }

  private static final class Checker implements Processor<PsiElement> {
    private boolean myIsLineBreakpointAvailable;

    @Override
    public boolean process(@Nonnull PsiElement o) {
      IElementType type = o.getNode().getElementType();
      if (GoParserDefinition.COMMENTS.contains(type) || GoParserDefinition.WHITESPACES.contains(type)) {
        return true;
      }
      myIsLineBreakpointAvailable = true;
      return false;
    }

    public boolean isLineBreakpointAvailable() {
      return myIsLineBreakpointAvailable;
    }
  }
}

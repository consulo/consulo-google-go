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

package com.goide.psi;

import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.impl.ast.TreeUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.StubBasedPsiElement;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.Couple;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoPsiTreeUtil extends PsiTreeUtil {
  @Nullable
  public static <T extends PsiElement> T getStubChildOfType(@Nullable PsiElement element, @Nonnull Class<T> aClass) {
    if (element == null) return null;
    StubElement<?> stub = element instanceof StubBasedPsiElement ? ((StubBasedPsiElement)element).getStub() : null;
    if (stub == null) {
      return getChildOfType(element, aClass);
    }
    for (StubElement childStub : stub.getChildrenStubs()) {
      PsiElement child = childStub.getPsi();
      if (aClass.isInstance(child)) {
        //noinspection unchecked
        return (T)child;
      }
    }
    return null;
  }

  @Nonnull
  public static <T extends PsiElement> List<T> getStubChildrenOfTypeAsList(@Nullable PsiElement element, @Nonnull Class<T> aClass) {
    if (element == null) return Collections.emptyList();
    StubElement<?> stub = element instanceof StubBasedPsiElement ? ((StubBasedPsiElement)element).getStub() : null;
    if (stub == null) {
      return getChildrenOfTypeAsList(element, aClass);
    }

    List<T> result = new SmartList<T>();
    for (StubElement childStub : stub.getChildrenStubs()) {
      PsiElement child = childStub.getPsi();
      if (aClass.isInstance(child)) {
        //noinspection unchecked
        result.add((T)child);
      }
    }
    return result;
  }

  @Nullable
  private static Couple<PsiElement> getElementRange(@Nonnull GoFile file, int startOffset, int endOffset) {
    PsiElement startElement = findNotWhiteSpaceElementAtOffset(file, startOffset, true);
    PsiElement endElement = findNotWhiteSpaceElementAtOffset(file, endOffset - 1, false);
    if (startElement == null || endElement == null) return null;
    ASTNode startNode = TreeUtil.findFirstLeaf(startElement.getNode());
    ASTNode endNode = TreeUtil.findLastLeaf(endElement.getNode());
    if (startNode == null || endNode == null) return null;

    startElement = startNode.getPsi();
    endElement = endNode.getPsi();
    if (startElement == null || endElement == null) return null;

    return Couple.of(startElement, endElement);
  }

  /**
   * Return element range which contains TextRange(start, end) of top level elements
   * common parent of elements is straight parent for each element
   */
  @Nullable
  private static Couple<PsiElement> getTopmostElementRange(@Nonnull Couple<PsiElement> elementRange) {
    if (elementRange.first == null || elementRange.second == null) return null;
    PsiElement commonParent = PsiTreeUtil.findCommonParent(elementRange.first, elementRange.second);
    if (commonParent == null) return null;
    if (commonParent.isEquivalentTo(elementRange.first) || commonParent.isEquivalentTo(elementRange.second)) {
      commonParent = commonParent.getParent();
    }
    PsiElement startElement = PsiTreeUtil.findPrevParent(commonParent, elementRange.first);
    PsiElement endElement = PsiTreeUtil.findPrevParent(commonParent, elementRange.second);
    if (!startElement.getParent().isEquivalentTo(endElement.getParent())) return null;

    int start = elementRange.first.getTextRange().getStartOffset();
    int end = elementRange.second.getTextRange().getEndOffset();

    TextRange range = commonParent.getTextRange();
    PsiElement[] children = commonParent.getChildren();
    if (range.equalsToRange(start, end) ||
        range.getStartOffset() == start && (children.length == 0 || children[0].getTextRange().getStartOffset() > start) ||
        range.getEndOffset() == end && (children.length == 0 || children[children.length - 1].getTextRange().getEndOffset() < end)) {
      startElement = commonParent;
      endElement = commonParent;
    }
    if (startElement.isEquivalentTo(endElement)) {
      while (startElement.getTextRange().equals(startElement.getParent().getTextRange())) {
        startElement = startElement.getParent();
      }
      return Couple.of(startElement, startElement);
    }

    return Couple.of(startElement, endElement);
  }

  @Nonnull
  public static PsiElement[] getTopLevelElementsInRange(@Nonnull GoFile file,
                                                        int startOffset,
                                                        int endOffset,
                                                        @Nonnull Class<? extends PsiElement> clazz) {
    Couple<PsiElement> elementRange = getElementRange(file, startOffset, endOffset);
    if (elementRange == null) return PsiElement.EMPTY_ARRAY;
    Couple<PsiElement> topmostElementRange = getTopmostElementRange(elementRange);
    if (topmostElementRange == null) return PsiElement.EMPTY_ARRAY;
    if (!clazz.isInstance(topmostElementRange.first) || !clazz.isInstance(topmostElementRange.second)) {
      return PsiElement.EMPTY_ARRAY;
    }

    List<PsiElement> result = new ArrayList<>();
    PsiElement start = topmostElementRange.first;
    while (start != null && !start.isEquivalentTo(topmostElementRange.second)) {
      if (clazz.isInstance(start)) result.add(start);
      start = start.getNextSibling();
    }
    result.add(topmostElementRange.second);
    return PsiUtilCore.toPsiElementArray(result);
  }

  @Nullable
  private static PsiElement findNotWhiteSpaceElementAtOffset(@Nonnull GoFile file, int offset, boolean forward) {
    PsiElement element = file.findElementAt(offset);
    while (element instanceof PsiWhiteSpace) {
      element = file.findElementAt(forward ? element.getTextRange().getEndOffset() : element.getTextRange().getStartOffset() - 1);
    }
    return element;
  }
}
  
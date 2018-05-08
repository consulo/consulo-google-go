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

package com.goide.stubs.types;

import javax.annotation.Nonnull;

import com.goide.GoLanguage;
import com.goide.psi.GoBlock;
import com.goide.psi.GoCompositeElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NonNls;

public abstract class GoStubElementType<S extends StubBase<T>, T extends GoCompositeElement> extends IStubElementType<S, T> {
  public GoStubElementType(@NonNls @Nonnull String debugName) {
    super(debugName, GoLanguage.INSTANCE);
  }

  @Override
  @Nonnull
  public String getExternalId() {
    return "go." + super.toString();
  }

  @Override
  public void indexStub(@Nonnull S stub, @Nonnull IndexSink sink) {
  }

  @Override
  public boolean shouldCreateStub(ASTNode node) {
    return super.shouldCreateStub(node) && shouldCreateStubInBlock(node);
  }

  protected boolean shouldCreateStubInBlock(ASTNode node) {
    return PsiTreeUtil.getParentOfType(node.getPsi(), GoBlock.class) == null;
  }
}

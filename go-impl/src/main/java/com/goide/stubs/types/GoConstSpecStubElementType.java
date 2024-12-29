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

package com.goide.stubs.types;

import com.goide.psi.GoConstSpec;
import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.impl.GoConstSpecImpl;
import com.goide.stubs.GoConstSpecStub;
import consulo.index.io.StringRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ArrayFactory;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import java.io.IOException;

public class GoConstSpecStubElementType extends GoStubElementType<GoConstSpecStub, GoConstSpec> {
  public static final GoConstSpec[] EMPTY_ARRAY = new GoConstSpec[0];

  public static final ArrayFactory<GoConstSpec> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new GoConstSpec[count];
  
  public GoConstSpecStubElementType(@Nonnull String name) {
    super(name);
  }

  @Nonnull
  @Override
  public GoConstSpec createPsi(@Nonnull GoConstSpecStub stub) {
    return new GoConstSpecImpl(stub, this);
  }

  @Nonnull
  @Override
  public GoConstSpecStub createStub(@Nonnull GoConstSpec psi, StubElement parentStub) {
    String join = StringUtil.join(psi.getExpressionList(), PsiElement::getText, ";");
    return new GoConstSpecStub(parentStub, this, StringRef.fromString(join));
  }

  @Override
  public void serialize(@Nonnull GoConstSpecStub stub, @Nonnull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getExpressionsText());
  }

  @Override
  public boolean shouldCreateStub(ASTNode node) {
    return super.shouldCreateStub(node) && PsiTreeUtil.getParentOfType(node.getPsi(), GoFunctionOrMethodDeclaration.class) == null;
  }

  @Nonnull
  @Override
  public GoConstSpecStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GoConstSpecStub(parentStub, this, dataStream.readName());
  }
}

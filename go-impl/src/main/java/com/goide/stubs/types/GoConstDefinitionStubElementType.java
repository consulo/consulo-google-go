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

import com.goide.psi.GoConstDefinition;
import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.impl.GoConstDefinitionImpl;
import com.goide.stubs.GoConstDefinitionStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ArrayFactory;

import jakarta.annotation.Nonnull;
import java.io.IOException;

public class GoConstDefinitionStubElementType extends GoNamedStubElementType<GoConstDefinitionStub, GoConstDefinition> {
  public static final GoConstDefinition[] EMPTY_ARRAY = new GoConstDefinition[0];

  public static final ArrayFactory<GoConstDefinition> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new GoConstDefinition[count];
  
  public GoConstDefinitionStubElementType(@Nonnull String name) {
    super(name);
  }

  @Nonnull
  @Override
  public GoConstDefinition createPsi(@Nonnull GoConstDefinitionStub stub) {
    return new GoConstDefinitionImpl(stub, this);
  }

  @Nonnull
  @Override
  public GoConstDefinitionStub createStub(@Nonnull GoConstDefinition psi, StubElement parentStub) {
    return new GoConstDefinitionStub(parentStub, this, psi.getName(), psi.isPublic());
  }

  @Override
  public void serialize(@Nonnull GoConstDefinitionStub stub, @Nonnull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName());
    dataStream.writeBoolean(stub.isPublic());
  }

  @Nonnull
  @Override
  public GoConstDefinitionStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GoConstDefinitionStub(parentStub, this, dataStream.readName(), dataStream.readBoolean());
  }

  @Override
  public boolean shouldCreateStub(@Nonnull ASTNode node) {
    return super.shouldCreateStub(node) && PsiTreeUtil.getParentOfType(node.getPsi(), GoFunctionOrMethodDeclaration.class) == null;
  }
}

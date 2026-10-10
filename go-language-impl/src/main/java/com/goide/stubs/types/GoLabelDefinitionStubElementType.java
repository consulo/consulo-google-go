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

import com.goide.psi.GoLabelDefinition;
import com.goide.psi.impl.GoLabelDefinitionImpl;
import com.goide.stubs.GoLabelDefinitionStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

import java.io.IOException;

public class GoLabelDefinitionStubElementType extends GoNamedStubElementType<GoLabelDefinitionStub, GoLabelDefinition> {
  public GoLabelDefinitionStubElementType(String name) {
    super(name);
  }

  @Override
  public GoLabelDefinition createPsi(GoLabelDefinitionStub stub) {
    return new GoLabelDefinitionImpl(stub, this);
  }

  @Override
  public GoLabelDefinitionStub createStub(GoLabelDefinition psi, StubElement parentStub) {
    return new GoLabelDefinitionStub(parentStub, this, psi.getName(), psi.isPublic());
  }

  @Override
  public void serialize(GoLabelDefinitionStub stub, StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName());
    dataStream.writeBoolean(stub.isPublic());
  }

  @Override
  public boolean shouldCreateStub(ASTNode node) {
    return false; // todo
  }

  @Override
  public GoLabelDefinitionStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GoLabelDefinitionStub(parentStub, this, dataStream.readName(), dataStream.readBoolean());
  }
}

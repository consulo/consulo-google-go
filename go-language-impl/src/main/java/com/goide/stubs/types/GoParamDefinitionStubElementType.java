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

import com.goide.psi.GoParamDefinition;
import com.goide.psi.impl.GoParamDefinitionImpl;
import com.goide.stubs.GoParamDefinitionStub;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

import java.io.IOException;

public class GoParamDefinitionStubElementType extends GoNamedStubElementType<GoParamDefinitionStub, GoParamDefinition> {
  public GoParamDefinitionStubElementType(String name) {
    super(name);
  }

  @Override
  public GoParamDefinition createPsi(GoParamDefinitionStub stub) {
    return new GoParamDefinitionImpl(stub, this);
  }

  @Override
  public GoParamDefinitionStub createStub(GoParamDefinition psi, StubElement parentStub) {
    return new GoParamDefinitionStub(parentStub, this, psi.getName(), psi.isPublic());
  }

  @Override
  public void serialize(GoParamDefinitionStub stub, StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName());
    dataStream.writeBoolean(stub.isPublic());
  }

  @Override
  public GoParamDefinitionStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GoParamDefinitionStub(parentStub, this, dataStream.readName(), dataStream.readBoolean());
  }

  @Override
  protected boolean shouldIndex() {
    return false;
  }
}

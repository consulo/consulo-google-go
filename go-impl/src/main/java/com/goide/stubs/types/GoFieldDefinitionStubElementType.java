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

import com.goide.psi.GoFieldDefinition;
import com.goide.psi.impl.GoFieldDefinitionImpl;
import com.goide.stubs.GoFieldDefinitionStub;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

import jakarta.annotation.Nonnull;
import java.io.IOException;

public class GoFieldDefinitionStubElementType extends GoNamedStubElementType<GoFieldDefinitionStub, GoFieldDefinition> {
  public GoFieldDefinitionStubElementType(@Nonnull String name) {
    super(name);
  }

  @Nonnull
  @Override
  public GoFieldDefinition createPsi(@Nonnull GoFieldDefinitionStub stub) {
    return new GoFieldDefinitionImpl(stub, this);
  }

  @Nonnull
  @Override
  public GoFieldDefinitionStub createStub(@Nonnull GoFieldDefinition psi, StubElement parentStub) {
    return new GoFieldDefinitionStub(parentStub, this, psi.getName(), psi.isPublic());
  }

  @Override
  public void serialize(@Nonnull GoFieldDefinitionStub stub, @Nonnull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName());
    dataStream.writeBoolean(stub.isPublic());
  }

  @Nonnull
  @Override
  public GoFieldDefinitionStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GoFieldDefinitionStub(parentStub, this, dataStream.readName(), dataStream.readBoolean());
  }
}

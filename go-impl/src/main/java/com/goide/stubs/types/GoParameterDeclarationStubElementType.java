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

import com.goide.psi.GoParameterDeclaration;
import com.goide.psi.impl.GoParameterDeclarationImpl;
import com.goide.stubs.GoParameterDeclarationStub;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

import javax.annotation.Nonnull;
import java.io.IOException;

public class GoParameterDeclarationStubElementType extends GoStubElementType<GoParameterDeclarationStub, GoParameterDeclaration> {
  public GoParameterDeclarationStubElementType(@Nonnull String name) {
    super(name);
  }

  @Nonnull
  @Override
  public GoParameterDeclaration createPsi(@Nonnull GoParameterDeclarationStub stub) {
    return new GoParameterDeclarationImpl(stub, this);
  }

  @Nonnull
  @Override
  public GoParameterDeclarationStub createStub(@Nonnull GoParameterDeclaration psi, StubElement parentStub) {
    return new GoParameterDeclarationStub(parentStub, this, psi.getText(), psi.isVariadic());
  }

  @Override
  public void serialize(@Nonnull GoParameterDeclarationStub stub, @Nonnull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getText());
    dataStream.writeBoolean(stub.isVariadic());
  }

  @Nonnull
  @Override
  public GoParameterDeclarationStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GoParameterDeclarationStub(parentStub, this, dataStream.readName(), dataStream.readBoolean());
  }
}

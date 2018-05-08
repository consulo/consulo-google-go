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

import com.goide.psi.GoSignature;
import com.goide.psi.impl.GoSignatureImpl;
import com.goide.stubs.GoSignatureStub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import javax.annotation.Nonnull;

import java.io.IOException;

public class GoSignatureStubElementType extends GoStubElementType<GoSignatureStub, GoSignature> {
  public GoSignatureStubElementType(@Nonnull String name) {
    super(name);
  }

  @Nonnull
  @Override
  public GoSignature createPsi(@Nonnull GoSignatureStub stub) {
    return new GoSignatureImpl(stub, this);
  }

  @Nonnull
  @Override
  public GoSignatureStub createStub(@Nonnull GoSignature psi, StubElement parentStub) {
    return new GoSignatureStub(parentStub, this, psi.getText());
  }

  @Override
  public void serialize(@Nonnull GoSignatureStub stub, @Nonnull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getText());
  }

  @Nonnull
  @Override
  public GoSignatureStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GoSignatureStub(parentStub, this, dataStream.readName());
  }
}

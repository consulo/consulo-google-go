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

import com.goide.psi.GoResult;
import com.goide.psi.impl.GoResultImpl;
import com.goide.stubs.GoResultStub;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;

import jakarta.annotation.Nonnull;
import java.io.IOException;

public class GoResultStubElementType extends GoStubElementType<GoResultStub, GoResult> {
  public GoResultStubElementType(@Nonnull String name) {
    super(name);
  }

  @Nonnull
  @Override
  public GoResult createPsi(@Nonnull GoResultStub stub) {
    return new GoResultImpl(stub, this);
  }

  @Nonnull
  @Override
  public GoResultStub createStub(@Nonnull GoResult psi, StubElement parentStub) {
    return new GoResultStub(parentStub, this, psi.getText());
  }

  @Override
  public void serialize(@Nonnull GoResultStub stub, @Nonnull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getText());
  }

  @Nonnull
  @Override
  public GoResultStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GoResultStub(parentStub, this, dataStream.readName());
  }
}

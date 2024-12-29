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

import com.goide.psi.GoFunctionDeclaration;
import com.goide.psi.GoNamedElement;
import com.goide.psi.impl.GoFunctionDeclarationImpl;
import com.goide.stubs.GoFunctionDeclarationStub;
import com.goide.stubs.index.GoFunctionIndex;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubIndexKey;
import consulo.language.psi.stub.StubInputStream;
import consulo.language.psi.stub.StubOutputStream;
import consulo.util.collection.ArrayFactory;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.lazy.LazyValue;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class GoFunctionDeclarationStubElementType extends GoNamedStubElementType<GoFunctionDeclarationStub, GoFunctionDeclaration> {
  public static final GoFunctionDeclaration[] EMPTY_ARRAY = new GoFunctionDeclaration[0];

  public static final ArrayFactory<GoFunctionDeclaration> ARRAY_FACTORY =
    count -> count == 0 ? EMPTY_ARRAY : new GoFunctionDeclaration[count];
  
  private static final Supplier<List<StubIndexKey<String, ? extends GoNamedElement>>> EXTRA_KEYS = LazyValue.notNull(() -> ContainerUtil.newArrayList(GoFunctionIndex.KEY));

  public GoFunctionDeclarationStubElementType(@Nonnull String name) {
    super(name);
  }

  @Nonnull
  @Override
  public GoFunctionDeclaration createPsi(@Nonnull GoFunctionDeclarationStub stub) {
    return new GoFunctionDeclarationImpl(stub, this);
  }

  @Nonnull
  @Override
  public GoFunctionDeclarationStub createStub(@Nonnull GoFunctionDeclaration psi, StubElement parentStub) {
    return new GoFunctionDeclarationStub(parentStub, this, psi.getName(), psi.isPublic());
  }

  @Override
  public void serialize(@Nonnull GoFunctionDeclarationStub stub, @Nonnull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName());
    dataStream.writeBoolean(stub.isPublic());
  }

  @Nonnull
  @Override
  public GoFunctionDeclarationStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GoFunctionDeclarationStub(parentStub, this, dataStream.readName(), dataStream.readBoolean());
  }

  @Nonnull
  @Override
  protected Collection<StubIndexKey<String, ? extends GoNamedElement>> getExtraIndexKeys() {
    return EXTRA_KEYS.get();
  }
}

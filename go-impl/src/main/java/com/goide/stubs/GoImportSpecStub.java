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

package com.goide.stubs;

import com.goide.psi.GoImportSpec;
import consulo.index.io.StringRef;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GoImportSpecStub extends GoNamedStub<GoImportSpec> {
  @Nullable
  private final StringRef myAliasRef;
  @Nonnull
  private final StringRef myPathRef;
  private final boolean myIsDot;

  public GoImportSpecStub(StubElement parent, IStubElementType elementType, @Nullable String alias, @Nonnull String path, boolean isDot) {
    super(parent, elementType, (String)null, false);
    myAliasRef = StringRef.fromString(alias);
    myPathRef = StringRef.fromString(path);
    myIsDot = isDot;
  }

  @Nullable
  public String getAlias() {
    return myAliasRef != null ? myAliasRef.getString() : null;
  }

  @Nonnull
  public String getPath() {
    return myPathRef.getString();
  }

  public boolean isDot() {
    return myIsDot;
  }

  @Override
  public String getName() {
    return getAlias();
  }
}

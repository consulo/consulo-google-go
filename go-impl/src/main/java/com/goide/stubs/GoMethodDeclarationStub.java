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

import com.goide.psi.GoMethodDeclaration;
import consulo.index.io.StringRef;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubElement;

import jakarta.annotation.Nullable;

public class GoMethodDeclarationStub extends GoFunctionOrMethodDeclarationStub<GoMethodDeclaration> {
  private final StringRef myTypeName;

  public GoMethodDeclarationStub(StubElement parent, IStubElementType elementType, StringRef name, boolean isPublic, StringRef typeName) {
    super(parent, elementType, name, isPublic);
    myTypeName = typeName;
  }

  public GoMethodDeclarationStub(StubElement parent, IStubElementType elementType, String name, boolean isPublic, String typeName) {
    super(parent, elementType, name, isPublic);
    myTypeName = StringRef.fromString(typeName);
  }

  @Nullable
  public String getTypeName() {
    return myTypeName == null ? null : myTypeName.getString();
  }
}

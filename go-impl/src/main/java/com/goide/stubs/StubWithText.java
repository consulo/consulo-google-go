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

package com.goide.stubs;

import consulo.index.io.StringRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;

import jakarta.annotation.Nullable;

abstract public class StubWithText<T extends PsiElement> extends StubBase<T> implements TextHolder {
  @Nullable private final StringRef myText;

  protected StubWithText(StubElement parent, IStubElementType elementType, @Nullable StringRef ref) {
    super(parent, elementType);
    myText = ref;
  }

  @Override
  @Nullable
  public String getText() {
    return myText == null ? null : myText.getString();
  }
}
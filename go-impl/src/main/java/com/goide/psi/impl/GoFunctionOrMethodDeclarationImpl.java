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

package com.goide.psi.impl;

import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.GoType;
import com.goide.stubs.GoFunctionOrMethodDeclarationStub;
import consulo.language.ast.ASTNode;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.stub.IStubElementType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract public class GoFunctionOrMethodDeclarationImpl<T extends GoFunctionOrMethodDeclarationStub<?>> extends GoNamedElementImpl<T>
  implements GoFunctionOrMethodDeclaration {
  public GoFunctionOrMethodDeclarationImpl(@Nonnull T stub, @Nonnull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public GoFunctionOrMethodDeclarationImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  @Nullable
  public GoType getGoTypeInner(@Nullable ResolveState context) {
    return GoPsiImplUtil.getGoTypeInner(this, context);
  }
}
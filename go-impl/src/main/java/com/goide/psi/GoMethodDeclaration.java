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

// This is a generated file. Not intended for manual editing.
package com.goide.psi;

import javax.annotation.*;

import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.goide.stubs.GoMethodDeclarationStub;

public interface GoMethodDeclaration extends GoFunctionOrMethodDeclaration, StubBasedPsiElement<GoMethodDeclarationStub> {

  @Nullable
  GoBlock getBlock();

  @Nullable
  GoReceiver getReceiver();

  @Nullable
  GoSignature getSignature();

  @Nonnull
  PsiElement getFunc();

  @Nullable
  PsiElement getIdentifier();

  @Nullable
  GoType getReceiverType();

}
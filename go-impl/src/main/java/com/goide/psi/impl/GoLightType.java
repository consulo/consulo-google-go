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

import com.goide.psi.*;
import com.goide.stubs.GoTypeStub;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

public abstract class GoLightType<E extends GoCompositeElement> extends LightElement implements GoType {
  @Nonnull
  protected final E myElement;

  protected GoLightType(@Nonnull E e) {
    super(e.getManager(), e.getLanguage());
    myElement = e;
    setNavigationElement(e);
  }

  @Nullable
  @Override
  public GoTypeReferenceExpression getTypeReferenceExpression() {
    return null;
  }

  @Override
  public boolean shouldGoDeeper() {
    return false;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" + myElement + "}";
  }

  @Override
  public IStubElementType getElementType() {
    return null;
  }

  @Override
  public GoTypeStub getStub() {
    return null;
  }

  @Nonnull
  @Override
  public GoType getUnderlyingType() {
    return GoPsiImplUtil.getUnderlyingType(this);
  }

  static class LightPointerType extends GoLightType<GoType> implements GoPointerType {
    protected LightPointerType(@Nonnull GoType o) {
      super(o);
    }

    @Override
    public String getText() {
      return "*" + myElement.getText();
    }

    @Nullable
    @Override
    public GoType getType() {
      return myElement;
    }

    @Nonnull
    @Override
    public PsiElement getMul() {
      return myElement; // todo: mock it?
    }

    @Override
    public PsiElement contextlessResolve() {
      return myElement;
    }
  }

  static class LightTypeList extends GoLightType<GoCompositeElement> implements GoTypeList {
    @Nonnull
    private final List<GoType> myTypes;

    public LightTypeList(@Nonnull GoCompositeElement o, @Nonnull List<GoType> types) {
      super(o);
      myTypes = types;
    }

    @Nonnull
    @Override
    public List<GoType> getTypeList() {
      return myTypes;
    }

    @Override
    public String toString() {
      return "MyGoTypeList{myTypes=" + myTypes + '}';
    }

    @Override
    public String getText() {
      return StringUtil.join(getTypeList(), PsiElement::getText, ", ");
    }

    @Override
    public PsiElement contextlessResolve() {
      return null;
    }
  }

  static class LightFunctionType extends GoLightType<GoSignatureOwner> implements GoFunctionType {
    public LightFunctionType(@Nonnull GoSignatureOwner o) {
      super(o);
    }

    @Nullable
    @Override
    public GoSignature getSignature() {
      return myElement.getSignature();
    }

    @Nonnull
    @Override
    public PsiElement getFunc() {
      return myElement instanceof GoFunctionOrMethodDeclaration ? ((GoFunctionOrMethodDeclaration)myElement).getFunc() : myElement;
    }

    @Override
    public PsiElement contextlessResolve() {
      return myElement;
    }

    @Override
    public String getText() {
      GoSignature signature = myElement.getSignature();
      return "func " + (signature != null ? signature.getText() : "<null>");
    }
  }

  static class LightArrayType extends GoLightType<GoType> implements GoArrayOrSliceType {
    protected LightArrayType(GoType type) {
      super(type);
    }

    @Override
    public String getText() {
      return "[]" + myElement.getText();
    }

    @Nullable
    @Override
    public GoExpression getExpression() {
      return null;
    }

    @Nullable
    @Override
    public GoType getType() {
      return myElement;
    }

    @Override
    public PsiElement contextlessResolve() {
      return myElement;
    }

    @Nonnull
    @Override
    public PsiElement getLbrack() {
      //noinspection ConstantConditions
      return null; // todo: mock?
    }

    @Nullable
    @Override
    public PsiElement getRbrack() {
      return null;
    }

    @Nullable
    @Override
    public PsiElement getTripleDot() {
      return null;
    }
  }
}

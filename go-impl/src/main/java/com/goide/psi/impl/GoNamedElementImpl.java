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

import com.goide.project.GoVendoringUtil;
import com.goide.psi.*;
import com.goide.sdk.GoPackageUtil;
import com.goide.stubs.GoNamedStub;
import com.goide.util.GoUtil;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.component.util.Iconable;
import consulo.content.scope.SearchScope;
import consulo.language.ast.ASTNode;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import consulo.usage.UsageViewUtil;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class GoNamedElementImpl<T extends GoNamedStub<?>> extends GoStubbedElementImpl<T> implements GoCompositeElement, GoNamedElement {

  public GoNamedElementImpl(@Nonnull T stub, @Nonnull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  public GoNamedElementImpl(@Nonnull ASTNode node) {
    super(node);
  }

  @Override
  public boolean isPublic() {
    if (GoPsiImplUtil.builtin(this)) return true;
    T stub = getStub();
    return stub != null ? stub.isPublic() : StringUtil.isCapitalized(getName());
  }

  @Nullable
  @Override
  public PsiElement getNameIdentifier() {
    return getIdentifier();
  }

  @Nullable
  @Override
  public String getName() {
    T stub = getStub();
    if (stub != null) {
      return stub.getName();
    }
    PsiElement identifier = getIdentifier();
    return identifier != null ? identifier.getText() : null;
  }

  @Nullable
  @Override
  public String getQualifiedName() {
    String name = getName();
    if (name == null) return null;
    String packageName = getContainingFile().getPackageName();
    return GoPsiImplUtil.getFqn(packageName, name);
  }

  @Override
  public int getTextOffset() {
    PsiElement identifier = getIdentifier();
    return identifier != null ? identifier.getTextOffset() : super.getTextOffset();
  }

  @Nonnull
  @Override
  public PsiElement setName(@NonNls @Nonnull String newName) throws IncorrectOperationException {
    PsiElement identifier = getIdentifier();
    if (identifier != null) {
      identifier.replace(GoElementFactory.createIdentifierFromText(getProject(), newName));
    }
    return this;
  }

  @Nullable
  @Override
  public GoType getGoType(@Nullable ResolveState context) {
    if (context != null) return getGoTypeInner(context);
    return LanguageCachedValueUtil.getCachedValue(this, () -> CachedValueProvider.Result
            .create(getGoTypeInner(GoPsiImplUtil.createContextOnElement(this)), PsiModificationTracker.MODIFICATION_COUNT));
  }

  @Nullable
  protected GoType getGoTypeInner(@Nullable ResolveState context) {
    return findSiblingType();
  }

  @Nullable
  @Override
  public GoType findSiblingType() {
    T stub = getStub();
    if (stub != null) {
      return GoPsiTreeUtil.getStubChildOfType(getParentByStub(), GoType.class);
    }
    return PsiTreeUtil.getNextSiblingOfType(this, GoType.class);
  }

  @Override
  public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place) {
    return GoCompositeElementImpl.processDeclarationsDefault(this, processor, state, lastParent, place);
  }

  @Override
  public ItemPresentation getPresentation() {
    String text = UsageViewUtil.createNodeText(this);
    if (text != null) {
      boolean vendoringEnabled = GoVendoringUtil.isVendoringEnabled(ModuleUtilCore.findModuleForPsiElement(getContainingFile()));
      return new ItemPresentation() {
        @Nullable
        @Override
        public String getPresentableText() {
          return getName();
        }

        @Nullable
        @Override
        public String getLocationString() {
          GoFile file = getContainingFile();
          String fileName = file.getName();
          String importPath = ObjectUtil.chooseNotNull(file.getImportPath(vendoringEnabled), file.getPackageName());
          return "in " + (importPath != null ? importPath + "/" + fileName : fileName);
        }

        @Nullable
        @Override
        public Image getIcon() {
          return IconDescriptorUpdaters.getIcon(GoNamedElementImpl.this, Iconable.ICON_FLAG_VISIBILITY);
        }
      };
    }
    return super.getPresentation();
  }

  @Nonnull
  @Override
  public GlobalSearchScope getResolveScope() {
    return isPublic() ? GoUtil.goPathResolveScope(this) : GoPackageUtil.packageScope(getContainingFile());
  }

  @Nonnull
  @Override
  public SearchScope getUseScope() {
    if (this instanceof GoVarDefinition || this instanceof GoConstDefinition || this instanceof GoLabelDefinition) {
      GoBlock block = PsiTreeUtil.getParentOfType(this, GoBlock.class);
      if (block != null) return new LocalSearchScope(block);
    }
    if (!isPublic()) {
      return GoPackageUtil.packageScope(getContainingFile());
    }
    GoSpecType parentType = PsiTreeUtil.getStubOrPsiParentOfType(this, GoSpecType.class);
    if (parentType != null) {
      GoTypeSpec typeSpec = GoPsiImplUtil.getTypeSpecSafe(parentType);
      if (typeSpec != null && !StringUtil.isCapitalized(typeSpec.getName())) {
        return GoPackageUtil.packageScope(getContainingFile());
      }
    }
    return GoUtil.goPathUseScope(this, !(this instanceof GoMethodDeclaration));
  }

  @Override
  public boolean isBlank() {
    return StringUtil.equals(getName(), "_");
  }

  @Override
  public boolean shouldGoDeeper() {
    return true;
  }
}

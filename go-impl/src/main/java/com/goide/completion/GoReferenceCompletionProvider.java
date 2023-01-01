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

package com.goide.completion;

import com.goide.project.GoVendoringUtil;
import com.goide.psi.*;
import com.goide.psi.impl.*;
import consulo.language.editor.completion.CompletionParameters;
import consulo.language.editor.completion.CompletionProvider;
import consulo.language.editor.completion.CompletionResultSet;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.*;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.language.util.ProcessingContext;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.ObjectUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class GoReferenceCompletionProvider implements CompletionProvider {
  @Override
  public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet set) {
    GoReferenceExpressionBase expression = PsiTreeUtil.getParentOfType(parameters.getPosition(), GoReferenceExpressionBase.class);
    PsiFile originalFile = parameters.getOriginalFile();
    if (expression != null) {
      fillVariantsByReference(expression.getReference(), originalFile, set.withPrefixMatcher(GoCompletionUtil.createPrefixMatcher(set.getPrefixMatcher())));
    }
    PsiElement parent = parameters.getPosition().getParent();
    if (parent != null) {
      fillVariantsByReference(parent.getReference(), originalFile, set.withPrefixMatcher(GoCompletionUtil.createPrefixMatcher(set.getPrefixMatcher())));
    }
  }

  private static void fillVariantsByReference(@Nullable PsiReference reference, @Nonnull PsiFile file, @Nonnull CompletionResultSet result) {
    if (reference == null) return;
    if (reference instanceof PsiMultiReference) {
      PsiReference[] references = ((PsiMultiReference)reference).getReferences();
      ContainerUtil.sort(references, PsiMultiReference.COMPARATOR);
      fillVariantsByReference(ArrayUtil.getFirstElement(references), file, result);
    }
    else if (reference instanceof GoReference) {
      GoReferenceExpression refExpression = ObjectUtil.tryCast(reference.getElement(), GoReferenceExpression.class);
      GoStructLiteralCompletion.Variants variants = GoStructLiteralCompletion.allowedVariants(refExpression);

      fillStructFieldNameVariants(file, result, variants, refExpression);

      if (variants != GoStructLiteralCompletion.Variants.FIELD_NAME_ONLY) {
        ((GoReference)reference).processResolveVariants(new MyGoScopeProcessor(result, file, false));
      }
    }
    else if (reference instanceof GoTypeReference) {
      PsiElement element = reference.getElement();
      PsiElement spec = PsiTreeUtil.getParentOfType(element, GoFieldDeclaration.class, GoTypeSpec.class);
      boolean insideParameter = PsiTreeUtil.getParentOfType(element, GoParameterDeclaration.class) != null;
      ((GoTypeReference)reference).processResolveVariants(new MyGoScopeProcessor(result, file, true) {
        @Override
        protected boolean accept(@Nonnull PsiElement e) {
          return e != spec && !(insideParameter && (e instanceof GoNamedSignatureOwner || e instanceof GoVarDefinition || e instanceof GoConstDefinition));
        }
      });
    }
    else if (reference instanceof GoCachedReference) {
      ((GoCachedReference)reference).processResolveVariants(new MyGoScopeProcessor(result, file, false));
    }
  }

  private static void fillStructFieldNameVariants(@Nonnull PsiFile file,
                                                  @Nonnull CompletionResultSet result,
                                                  @Nonnull GoStructLiteralCompletion.Variants variants,
                                                  @Nullable GoReferenceExpression refExpression) {
    if (refExpression == null || variants != GoStructLiteralCompletion.Variants.FIELD_NAME_ONLY && variants != GoStructLiteralCompletion.Variants.BOTH) {
      return;
    }

    GoLiteralValue literal = PsiTreeUtil.getParentOfType(refExpression, GoLiteralValue.class);
    new GoFieldNameReference(refExpression).processResolveVariants(new MyGoScopeProcessor(result, file, false) {
      final Set<String> alreadyAssignedFields = GoStructLiteralCompletion.alreadyAssignedFields(literal);

      @Override
      public boolean execute(@Nonnull PsiElement o, @Nonnull ResolveState state) {
        String structFieldName = o instanceof GoFieldDefinition
                                 ? ((GoFieldDefinition)o).getName()
                                 : o instanceof GoAnonymousFieldDefinition ? ((GoAnonymousFieldDefinition)o).getName() : null;
        if (structFieldName != null && alreadyAssignedFields.contains(structFieldName)) {
          return true;
        }
        return super.execute(o, state);
      }
    });
  }

  private static void addElement(@Nonnull PsiElement o,
                                 @Nonnull ResolveState state,
                                 boolean forTypes,
                                 boolean vendoringEnabled,
                                 @Nonnull Set<String> processedNames,
                                 @Nonnull CompletionResultSet set) {
    LookupElement lookup = createLookupElement(o, state, forTypes, vendoringEnabled);
    if (lookup != null) {
      String lookupString = lookup.getLookupString();
      if (!processedNames.contains(lookupString)) {
        set.addElement(lookup);
        processedNames.add(lookupString);
      }
    }
  }

  @Nullable
  private static LookupElement createLookupElement(@Nonnull PsiElement o, @Nonnull ResolveState state, boolean forTypes, boolean vendoringEnabled) {
    if (o instanceof GoNamedElement && !((GoNamedElement)o).isBlank() || o instanceof GoImportSpec && !((GoImportSpec)o).isDot()) {
      if (o instanceof GoImportSpec) {
        return GoCompletionUtil.createPackageLookupElement((GoImportSpec)o, state.get(GoReferenceBase.ACTUAL_NAME), vendoringEnabled);
      }
      else if (o instanceof GoNamedSignatureOwner && ((GoNamedSignatureOwner)o).getName() != null) {
        String name = ((GoNamedSignatureOwner)o).getName();
        if (name != null) {
          return GoCompletionUtil.createFunctionOrMethodLookupElement((GoNamedSignatureOwner)o, name, null, GoCompletionUtil.FUNCTION_PRIORITY);
        }
      }
      else if (o instanceof GoTypeSpec) {
        return forTypes ? GoCompletionUtil.createTypeLookupElement((GoTypeSpec)o) : GoCompletionUtil.createTypeConversionLookupElement((GoTypeSpec)o);
      }
      else if (o instanceof PsiDirectory) {
        return GoCompletionUtil.createPackageLookupElement(((PsiDirectory)o).getName(), (PsiDirectory)o, o, vendoringEnabled, true);
      }
      else if (o instanceof GoLabelDefinition) {
        String name = ((GoLabelDefinition)o).getName();
        if (name != null) return GoCompletionUtil.createLabelLookupElement((GoLabelDefinition)o, name);
      }
      else if (o instanceof GoFieldDefinition) {
        return GoCompletionUtil.createFieldLookupElement((GoFieldDefinition)o);
      }
      else {
        return GoCompletionUtil.createVariableLikeLookupElement((GoNamedElement)o);
      }
    }
    return null;
  }

  private static class MyGoScopeProcessor extends GoScopeProcessor {
    @Nonnull
    private final CompletionResultSet myResult;
    private final boolean myForTypes;
    private final boolean myVendoringEnabled;
    private final Set<String> myProcessedNames = new HashSet<>();

    public MyGoScopeProcessor(@Nonnull CompletionResultSet result, @Nonnull PsiFile originalFile, boolean forTypes) {
      myResult = result;
      myForTypes = forTypes;
      myVendoringEnabled = GoVendoringUtil.isVendoringEnabled(ModuleUtilCore.findModuleForPsiElement(originalFile));
    }

    @Override
    public boolean execute(@Nonnull PsiElement o, @Nonnull ResolveState state) {
      if (accept(o)) {
        addElement(o, state, myForTypes, myVendoringEnabled, myProcessedNames, myResult);
      }
      return true;
    }

    protected boolean accept(@Nonnull PsiElement e) {
      return true;
    }

    @Override
    public boolean isCompletion() {
      return true;
    }
  }
}
                                                      
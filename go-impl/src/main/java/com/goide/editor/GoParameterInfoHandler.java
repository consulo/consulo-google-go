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

package com.goide.editor;

import com.goide.GoLanguage;
import com.goide.GoTypes;
import com.goide.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.editor.CodeInsightBundle;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.parameterInfo.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;

import org.jspecify.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@ExtensionImpl
public class GoParameterInfoHandler implements ParameterInfoHandlerWithTabActionSupport<GoArgumentList, Object, GoExpression> {
  @Override
  public GoExpression[] getActualParameters(GoArgumentList o) {
    return ArrayUtil.toObjectArray(o.getExpressionList(), GoExpression.class);
  }

  @Override
  public IElementType getActualParameterDelimiterType() {
    return GoTypes.COMMA;
  }

  @Override
  public IElementType getActualParametersRBraceType() {
    return GoTypes.RPAREN;
  }

  @Override
  public Set<Class<?>> getArgumentListAllowedParentClasses() {
	  return Collections.emptySet();
  }

  @Override
  public Set<? extends Class<?>> getArgListStopSearchClasses() {
    return Collections.emptySet();
  }

  @Override
  public Class<GoArgumentList> getArgumentListClass() {
    return GoArgumentList.class;
  }

  @Override
  public boolean couldShowInLookup() {
    return true;
  }

  @Nullable
  @Override
  public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context) {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @Nullable
  @Override
  public GoArgumentList findElementForParameterInfo(CreateParameterInfoContext context) {
    // todo: see ParameterInfoUtils.findArgumentList
    return getList(context);
  }

  @Nullable
  private static GoArgumentList getList(ParameterInfoContext context) {
    PsiElement at = context.getFile().findElementAt(context.getOffset());
    return PsiTreeUtil.getParentOfType(at, GoArgumentList.class);
  }

  @Override
  public void showParameterInfo(GoArgumentList argList, CreateParameterInfoContext context) {
    PsiElement parent = argList.getParent();
    if (!(parent instanceof GoCallExpr)) return;
    GoFunctionType type = findFunctionType(((GoCallExpr)parent).getExpression().getGoType(null));
    if (type != null) {
      context.setItemsToShow(new Object[]{type});
      context.showHint(argList, argList.getTextRange().getStartOffset(), this);
    }
  }

  @Nullable
  private static GoFunctionType findFunctionType(@Nullable GoType type) {
    if (type instanceof GoFunctionType || type == null) return (GoFunctionType)type;
    GoType base = type.getUnderlyingType();
    return base instanceof GoFunctionType ? (GoFunctionType)base : null;
  }

  @Nullable
  @Override
  public GoArgumentList findElementForUpdatingParameterInfo(UpdateParameterInfoContext context) {
    return getList(context);
  }

  @Override
  public void updateParameterInfo(GoArgumentList list, UpdateParameterInfoContext context) {
    context.setCurrentParameter(ParameterInfoUtils.getCurrentParameterIndex(list.getNode(), context.getOffset(), GoTypes.COMMA));
  }

  @Override
  public void updateUI(@Nullable Object p, ParameterInfoUIContext context) {
    updatePresentation(p, context);
  }

  static String updatePresentation(@Nullable Object p, ParameterInfoUIContext context) {
    if (p == null) {
      context.setUIComponentEnabled(false);
      return null;
    }
    GoSignature signature = p instanceof GoSignatureOwner ? ((GoSignatureOwner)p).getSignature() : null;
    if (signature == null) return null;
    GoParameters parameters = signature.getParameters();
    List<String> parametersPresentations = getParameterPresentations(parameters, PsiElement::getText);
    
    StringBuilder builder = new StringBuilder();
    int start = 0;
    int end = 0;
    if (!parametersPresentations.isEmpty()) {
      // Figure out what particular presentation is actually selected. Take in
      // account possibility of the last variadic parameter.
      int selected = isLastParameterVariadic(parameters.getParameterDeclarationList())
                     ? Math.min(context.getCurrentParameterIndex(), parametersPresentations.size() - 1)
                     : context.getCurrentParameterIndex();
      
      for (int i = 0; i < parametersPresentations.size(); ++i) {
        if (i != 0) {
          builder.append(", ");
        }
        if (i == selected) {
          start = builder.length();
        }
        builder.append(parametersPresentations.get(i));

        if (i == selected) {
          end = builder.length();
        }
      }
    }
    else {
      builder.append(CodeInsightBundle.message("parameter.info.no.parameters"));
    }
    return context.setupUIComponentPresentation(builder.toString(), start, end, false, false, false, context.getDefaultParameterColor());
  }

  /**
   * Creates a list of parameter presentations. For clarity we expand parameters declared as `a, b, c int` into `a int, b int, c int`.
   */
  public static List<String> getParameterPresentations(GoParameters parameters,
                                                       Function<PsiElement, String> typePresentationFunction) {
    List<GoParameterDeclaration> paramDeclarations = parameters.getParameterDeclarationList();
    List<String> paramPresentations = ContainerUtil.newArrayListWithCapacity(2 * paramDeclarations.size());
    for (GoParameterDeclaration paramDeclaration : paramDeclarations) {
      boolean isVariadic = paramDeclaration.isVariadic();
      List<GoParamDefinition> paramDefinitionList = paramDeclaration.getParamDefinitionList();
      for (GoParamDefinition paramDefinition : paramDefinitionList) {
        String separator = isVariadic ? " ..." : " ";
        paramPresentations.add(paramDefinition.getText() + separator + typePresentationFunction.apply(paramDeclaration.getType()));
      }
      if (paramDefinitionList.isEmpty()) {
        String separator = isVariadic ? "..." : "";
        paramPresentations.add(separator + typePresentationFunction.apply(paramDeclaration.getType()));
      }
    }
    return paramPresentations;
  }

  private static boolean isLastParameterVariadic(List<GoParameterDeclaration> declarations) {
    GoParameterDeclaration lastItem = ContainerUtil.getLastItem(declarations);
    return lastItem != null && lastItem.isVariadic();
  }

  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}

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

package com.goide.usages;

import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.GoNamedElement;
import com.goide.psi.GoTypeSpec;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.usage.PsiElementUsageGroupBase;
import consulo.usage.Usage;
import consulo.usage.UsageGroup;
import consulo.usage.rule.FileStructureGroupRuleProvider;
import consulo.usage.rule.PsiElementUsage;
import consulo.usage.rule.UsageGroupingRule;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoFileStructureGroupRuleProvider implements FileStructureGroupRuleProvider {
  public static final UsageGroupingRule USAGE_GROUPING_RULE = new UsageGroupingRule() {
    @Nullable
    @Override
    public UsageGroup groupUsage(@Nonnull Usage usage) {
      PsiElement psiElement = usage instanceof PsiElementUsage ? ((PsiElementUsage)usage).getElement() : null;
      GoNamedElement topmostElement = PsiTreeUtil.getParentOfType(psiElement, GoTypeSpec.class, GoFunctionOrMethodDeclaration.class);
      if (topmostElement != null) {
        return new PsiElementUsageGroupBase<>(topmostElement);
      }
      return null;
    }
  };

  @Nullable
  @Override
  public UsageGroupingRule getUsageGroupingRule(Project project) {
    return USAGE_GROUPING_RULE;
  }
}

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

import com.goide.GoLanguage;
import com.goide.project.GoVendoringUtil;
import com.goide.psi.*;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.psi.impl.GoTypeReference;
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.stubs.index.GoIdFilter;
import com.goide.util.GoUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.Processor;
import consulo.application.util.matcher.PrefixMatcher;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.pattern.PsiElementPattern;
import consulo.language.pattern.StandardPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.IdFilter;
import consulo.language.psi.stub.StubIndex;
import consulo.language.util.ModuleUtilCore;
import consulo.language.util.ProcessingContext;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.goide.completion.GoCompletionUtil.createPrefixMatcher;
import static com.goide.psi.impl.GoPsiImplUtil.prevDot;
import static com.goide.stubs.index.GoAllPublicNamesIndex.ALL_PUBLIC_NAMES;
import static consulo.language.pattern.PlatformPatterns.psiElement;

@ExtensionImpl(order = "last")
public class GoAutoImportCompletionContributor extends CompletionContributor {
  public GoAutoImportCompletionContributor() {
    extend(CompletionType.BASIC, inGoFile(), new CompletionProvider() {
      @Override
      public void addCompletions(@Nonnull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @Nonnull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        PsiElement parent = position.getParent();
        if (prevDot(parent)) return;
        PsiFile psiFile = parameters.getOriginalFile();
        if (!(psiFile instanceof GoFile && parent instanceof GoReferenceExpressionBase)) return;
        GoFile file = (GoFile)psiFile;

        result = adjustMatcher(parameters, result, parent);
        PrefixMatcher matcher = result.getPrefixMatcher();
        if (parameters.getInvocationCount() < 2 && matcher.getPrefix().isEmpty()) {
          result.restartCompletionOnPrefixChange(StandardPatterns.string().longerThan(0));
          return;
        }

        GoReferenceExpressionBase qualifier = ((GoReferenceExpressionBase)parent).getQualifier();
        if (qualifier != null && qualifier.getReference() != null && qualifier.getReference().resolve() != null) return;

        ArrayList<ElementProcessor> processors = ContainerUtil.newArrayList();
        if (parent instanceof GoReferenceExpression && !GoPsiImplUtil.isUnaryBitAndExpression(parent)) {
          processors.add(new FunctionsProcessor());
          processors.add(new VariablesAndConstantsProcessor());
        }
        if (parent instanceof GoReferenceExpression || parent instanceof GoTypeReferenceExpression) {
          processors.add(new TypesProcessor(parent));
        }
        if (processors.isEmpty()) return;

        Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
        NamedElementProcessor processor = new NamedElementProcessor(processors, file, result, module);
        Project project = position.getProject();
        GlobalSearchScope scope = new GoUtil.ExceptTestsScope(GoUtil.goPathResolveScope(file));
        VirtualFile containingDirectory = file.getVirtualFile().getParent();
        if (containingDirectory != null) {
          scope = new GoUtil.ExceptChildOfDirectory(containingDirectory, scope, GoTestFinder.getTestTargetPackage(file));
        }
        IdFilter idFilter = GoIdFilter.getProductionFilter(project);
        Set<String> sortedKeys = collectAndSortAllPublicProductionNames(matcher, scope, idFilter, file);
        for (String name : sortedKeys) {
          processor.setName(name);
          for (GoNamedElement element : StubIndex.getElements(ALL_PUBLIC_NAMES, name, project, scope, idFilter, GoNamedElement.class)) {
            if (!processor.process(element)) {
              break;
            }
          }
        }
      }

      private CompletionResultSet adjustMatcher(@Nonnull CompletionParameters parameters,
                                                @Nonnull CompletionResultSet result,
                                                @Nonnull PsiElement parent) {
        int startOffset = parent.getTextRange().getStartOffset();
        String newPrefix = parameters.getEditor().getDocument().getText(TextRange.create(startOffset, parameters.getOffset()));
        return result.withPrefixMatcher(createPrefixMatcher(newPrefix));
      }
    });
  }

  @Nonnull
  private static Set<String> collectAndSortAllPublicProductionNames(@Nonnull PrefixMatcher matcher,
                                                                    @Nonnull GlobalSearchScope scope,
                                                                    @Nullable IdFilter idFilter,
                                                                    @Nonnull GoFile file) {
    String prefix = matcher.getPrefix();
    boolean emptyPrefix = prefix.isEmpty();

    Set<String> packagesWithAliases = new HashSet<>();
    if (!emptyPrefix) {
      for (Map.Entry<String, Collection<GoImportSpec>> entry : file.getImportMap().entrySet()) {
        for (GoImportSpec spec : entry.getValue()) {
          String alias = spec.getAlias();
          if (spec.isDot() || alias != null) {
            packagesWithAliases.add(entry.getKey());
            break;
          }
        }
      }
    }

    Set<String> allNames = new HashSet<>();
    StubIndex.getInstance().processAllKeys(ALL_PUBLIC_NAMES, new CancellableCollectProcessor<String>(allNames) {
      @Override
      protected boolean accept(String s) {
        return emptyPrefix || matcher.prefixMatches(s) || packagesWithAliases.contains(substringBefore(s, '.'));
      }
    }, scope, idFilter);

    if (emptyPrefix) {
      return allNames;
    }

    List<String> sorted = ContainerUtil.sorted(allNames, String.CASE_INSENSITIVE_ORDER);
    ProgressManager.checkCanceled();

    LinkedHashSet<String> result = new LinkedHashSet<>();
    for (String name : sorted) {
      ProgressManager.checkCanceled();
      if (matcher.isStartMatch(name)) {
        result.add(name);
      }
    }
    result.addAll(sorted);
    return result;
  }

  private static PsiElementPattern.Capture<PsiElement> inGoFile() {
    return psiElement().inFile(psiElement(GoFile.class));
  }

  @Nonnull
  private static String substringBefore(@Nonnull String s, char c) {
    int i = s.indexOf(c);
    if (i == -1) return s;
    return s.substring(0, i);
  }

  private static String substringAfter(@Nonnull String s, char c) {
    int i = s.indexOf(c);
    if (i == -1) return "";
    return s.substring(i + 1);
  }

  @Nonnull
  private static String replacePackageWithAlias(@Nonnull String qualifiedName, @Nullable String alias) {
    return alias != null ? alias + "." + substringAfter(qualifiedName, '.') : qualifiedName;
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }

  private interface ElementProcessor {
    boolean process(@Nonnull String name,
                    @Nonnull GoNamedElement element,
                    @Nonnull ExistingImportData importData,
                    @Nonnull CompletionResultSet result);
    boolean isMine(@Nonnull String name, @Nonnull GoNamedElement element);
  }

  private static class VariablesAndConstantsProcessor implements ElementProcessor {
    @Override
    public boolean process(@Nonnull String name,
                           @Nonnull GoNamedElement element,
                           @Nonnull ExistingImportData importData,
                           @Nonnull CompletionResultSet result) {
      double priority = importData.exists ? GoCompletionUtil.VAR_PRIORITY : GoCompletionUtil.NOT_IMPORTED_VAR_PRIORITY;
      result.addElement(GoCompletionUtil.createVariableLikeLookupElement(element, replacePackageWithAlias(name, importData.alias),
                                                                         GoAutoImportInsertHandler.SIMPLE_INSERT_HANDLER, priority));
      return true;
    }

    @Override
    public boolean isMine(@Nonnull String name, @Nonnull GoNamedElement element) {
      return element instanceof GoVarDefinition || element instanceof GoConstDefinition;
    }
  }

  private static class FunctionsProcessor implements ElementProcessor {
    @Override
    public boolean process(@Nonnull String name,
                           @Nonnull GoNamedElement element,
                           @Nonnull ExistingImportData importData,
                           @Nonnull CompletionResultSet result) {
      GoFunctionDeclaration function = (GoFunctionDeclaration)element;
      double priority = importData.exists ? GoCompletionUtil.FUNCTION_PRIORITY : GoCompletionUtil.NOT_IMPORTED_FUNCTION_PRIORITY;
      result.addElement(GoCompletionUtil.createFunctionOrMethodLookupElement(function, replacePackageWithAlias(name, importData.alias),
                                                                             GoAutoImportInsertHandler.FUNCTION_INSERT_HANDLER, priority));
      return true;
    }

    @Override
    public boolean isMine(@Nonnull String name, @Nonnull GoNamedElement element) {
      return element instanceof GoFunctionDeclaration;
    }
  }

  private static class TypesProcessor implements ElementProcessor {
    @Nullable
	private final PsiElement myParent;

    public TypesProcessor(@Nullable PsiElement parent) {
      myParent = parent;
    }

    @Override
    public boolean process(@Nonnull String name,
                           @Nonnull GoNamedElement element,
                           @Nonnull ExistingImportData importData,
                           @Nonnull CompletionResultSet result) {
      GoTypeSpec spec = (GoTypeSpec)element;
      boolean forTypes = myParent instanceof GoTypeReferenceExpression;
      double priority;
      if (importData.exists) {
        priority = forTypes ? GoCompletionUtil.TYPE_PRIORITY : GoCompletionUtil.TYPE_CONVERSION;
      }
      else {
        priority = forTypes ? GoCompletionUtil.NOT_IMPORTED_TYPE_PRIORITY : GoCompletionUtil.NOT_IMPORTED_TYPE_CONVERSION;
      }

      String lookupString = replacePackageWithAlias(name, importData.alias);
      if (forTypes) {
        result.addElement(GoCompletionUtil.createTypeLookupElement(spec, lookupString, GoAutoImportInsertHandler.SIMPLE_INSERT_HANDLER,
                                                                   importData.importPath, priority));
      }
      else {
        result.addElement(GoCompletionUtil.createTypeConversionLookupElement(spec, lookupString,
                                                                             GoAutoImportInsertHandler.TYPE_CONVERSION_INSERT_HANDLER,
                                                                             importData.importPath, priority));
      }
      return true;
    }

    @Override
    public boolean isMine(@Nonnull String name, @Nonnull GoNamedElement element) {
      if (myParent != null && element instanceof GoTypeSpec) {
        PsiReference reference = myParent.getReference();
        return !(reference instanceof GoTypeReference) || ((GoTypeReference)reference).allowed((GoTypeSpec)element);
      }
      return false;
    }
  }

  private static class NamedElementProcessor implements Processor<GoNamedElement> {
    @Nonnull
	private final Collection<ElementProcessor> myProcessors;
    @Nonnull
	private final CompletionResultSet myResult;
    @Nonnull
	private String myName = "";
    @Nonnull
	private final Map<String, GoImportSpec> myImportedPackages;
    @Nullable private final Module myModule;
    private final boolean myVendoringEnabled;

    public NamedElementProcessor(@Nonnull Collection<ElementProcessor> processors,
                                 @Nonnull GoFile contextFile,
                                 @Nonnull CompletionResultSet result,
                                 @Nullable Module module) {
      myProcessors = processors;
      myVendoringEnabled = GoVendoringUtil.isVendoringEnabled(module);
      myImportedPackages = contextFile.getImportedPackagesMap();
      myModule = module;
      myResult = result;
    }

    public void setName(@Nonnull String name) {
      myName = name;
    }

    @Override
    public boolean process(@Nonnull GoNamedElement element) {
      ProgressManager.checkCanceled();
      Boolean allowed = null;
      ExistingImportData importData = null;
      for (ElementProcessor processor : myProcessors) {
        if (processor.isMine(myName, element)) {
          importData = cachedImportData(element, importData);
          allowed = cachedAllowed(element, allowed);
          if (allowed == Boolean.FALSE || importData.isDot) break;
          if (!processor.process(myName, element, importData, myResult)) {
            return false;
          }
        }
      }
      return true;
    }

    @Nonnull
    private Boolean cachedAllowed(@Nonnull GoNamedElement element, @Nullable Boolean existingValue) {
      if (existingValue != null) return existingValue;
      return GoPsiImplUtil.canBeAutoImported(element.getContainingFile(), false, myModule);
    }

    @Nonnull
    private ExistingImportData cachedImportData(@Nonnull GoNamedElement element, @Nullable ExistingImportData existingValue) {
      if (existingValue != null) return existingValue;

      GoFile declarationFile = element.getContainingFile();
      String importPath = declarationFile.getImportPath(myVendoringEnabled);
      GoImportSpec existingImport = myImportedPackages.get(importPath);

      boolean exists = existingImport != null;
      boolean isDot = exists && existingImport.isDot();
      String alias = existingImport != null ? existingImport.getAlias() : null;
      return new ExistingImportData(exists, isDot, alias, importPath);
    }
  }

  private static class ExistingImportData {
    public final boolean exists;
    public final boolean isDot;
    public final String alias;
    public final String importPath;

    private ExistingImportData(boolean exists, boolean isDot, String packageName, String importPath) {
      this.exists = exists;
      this.isDot = isDot;
      alias = packageName;
      this.importPath = importPath;
    }
  }
}

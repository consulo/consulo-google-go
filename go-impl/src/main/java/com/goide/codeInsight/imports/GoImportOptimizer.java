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

package com.goide.codeInsight.imports;

import com.goide.GoLanguage;
import com.goide.GoTypes;
import com.goide.psi.*;
import com.goide.psi.impl.GoReferenceBase;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressManager;
import consulo.document.Document;
import consulo.language.Language;
import consulo.language.editor.refactoring.ImportOptimizer;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.MultiMap;
import consulo.util.lang.Comparing;
import consulo.util.lang.EmptyRunnable;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.*;

@ExtensionImpl
public class GoImportOptimizer implements ImportOptimizer {
  @Override
  public boolean supports(PsiFile file) {
    return file instanceof GoFile;
  }

  @Nonnull
  @Override
  public Runnable processFile(@Nonnull PsiFile file) {
    if (!(file instanceof GoFile)) {
      return EmptyRunnable.getInstance();
    }
    MultiMap<String, GoImportSpec> importMap = ((GoFile)file).getImportMap();
    Set<PsiElement> importEntriesToDelete = new LinkedHashSet<>();
    Set<PsiElement> importIdentifiersToDelete = findRedundantImportIdentifiers(importMap);

    importEntriesToDelete.addAll(findDuplicatedEntries(importMap));
    importEntriesToDelete.addAll(filterUnusedImports(file, importMap).values());
    if (importEntriesToDelete.isEmpty() && importIdentifiersToDelete.isEmpty()) {
      return EmptyRunnable.getInstance();
    }

    return new CollectingInfoRunnable() {
      @Nullable
      @Override
      public String getUserNotificationInfo() {
        int entriesToDelete = importEntriesToDelete.size();
        int identifiersToDelete = importIdentifiersToDelete.size();
        String result = "";
        if (entriesToDelete > 0) {
          result = "Removed " + entriesToDelete + " import" + (entriesToDelete > 1 ? "s" : "");
        } 
        if (identifiersToDelete > 0) {
          result += result.isEmpty() ? "Removed " : " and ";
          result += identifiersToDelete + " alias" + (identifiersToDelete > 1 ? "es" : "");
        }
        return StringUtil.nullize(result);
      }

      @Override
      public void run() {
        if (!importEntriesToDelete.isEmpty() || !importIdentifiersToDelete.isEmpty()) {
          PsiDocumentManager manager = PsiDocumentManager.getInstance(file.getProject());
          Document document = manager.getDocument(file);
          if (document != null) {
            manager.commitDocument(document);
          }
        }

        for (PsiElement importEntry : importEntriesToDelete) {
          if (importEntry != null && importEntry.isValid()) {
            deleteImportSpec(getImportSpec(importEntry));
          }
        }

        for (PsiElement identifier : importIdentifiersToDelete) {
          if (identifier != null && identifier.isValid()) {
            identifier.delete();
          }
        }
      }
    };
  }

  @Nonnull
  public static Set<PsiElement> findRedundantImportIdentifiers(@Nonnull MultiMap<String, GoImportSpec> importMap) {
    Set<PsiElement> importIdentifiersToDelete = new LinkedHashSet<>();
    for (PsiElement importEntry : importMap.values()) {
      GoImportSpec importSpec = getImportSpec(importEntry);
      if (importSpec != null) {
        String localPackageName = importSpec.getLocalPackageName();
        if (!StringUtil.isEmpty(localPackageName)) {
          if (Comparing.equal(importSpec.getAlias(), localPackageName)) {
            importIdentifiersToDelete.add(importSpec.getIdentifier());
          }
        }
      }
    }
    return importIdentifiersToDelete;
  }

  public static MultiMap<String, GoImportSpec> filterUnusedImports(@Nonnull PsiFile file,
                                                                   @Nonnull MultiMap<String, GoImportSpec> importMap) {
    MultiMap<String, GoImportSpec> result = MultiMap.create();
    result.putAllValues(importMap);
    result.remove("_"); // imports for side effects are always used
    
    Collection<GoImportSpec> implicitImports = ContainerUtil.newArrayList(result.get("."));
    for (GoImportSpec importEntry : implicitImports) {
      GoImportSpec spec = getImportSpec(importEntry);
      if (spec != null && spec.isDot() && hasImportUsers(spec)) {
        result.remove(".", importEntry);
      }
    }
    
    file.accept(new GoRecursiveVisitor() {
      @Override
      public void visitTypeReferenceExpression(@Nonnull GoTypeReferenceExpression o) {
        GoTypeReferenceExpression lastQualifier = o.getQualifier();
        if (lastQualifier != null) {
          GoTypeReferenceExpression previousQualifier;
          while ((previousQualifier = lastQualifier.getQualifier()) != null) {
            lastQualifier = previousQualifier;
          }
          markAsUsed(lastQualifier.getIdentifier(), lastQualifier.getReference());
        }
      }

      @Override
      public void visitReferenceExpression(@Nonnull GoReferenceExpression o) {
        GoReferenceExpression lastQualifier = o.getQualifier();
        if (lastQualifier != null) {
          GoReferenceExpression previousQualifier;
          while ((previousQualifier = lastQualifier.getQualifier()) != null) {
            lastQualifier = previousQualifier;
          }
          markAsUsed(lastQualifier.getIdentifier(), lastQualifier.getReference());
        }
      }

      private void markAsUsed(@Nonnull PsiElement qualifier, @Nonnull PsiReference reference) {
        String qualifierText = qualifier.getText();
        if (!result.containsKey(qualifierText)) {
          // already marked
          return;
        }
        PsiElement resolve = reference.resolve();
        if (!(resolve instanceof PsiDirectory || resolve instanceof GoImportSpec || resolve instanceof PsiDirectoryContainer)) {
          return;
        }
        Collection<String> qualifiersToDelete = new HashSet<>();
        for (GoImportSpec spec : result.get(qualifierText)) {
          for (Map.Entry<String, Collection<GoImportSpec>> entry : result.entrySet()) {
            for (GoImportSpec importSpec : entry.getValue()) {
              if (importSpec == spec) {
                qualifiersToDelete.add(entry.getKey());
              }
            }
          }
        }
        for (String qualifierToDelete : qualifiersToDelete) {
          result.remove(qualifierToDelete);
        }
      }
    });
    return result;
  }

  private static boolean hasImportUsers(@Nonnull GoImportSpec spec) {
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (spec) {
      List<PsiElement> list = spec.getUserData(GoReferenceBase.IMPORT_USERS);
      if (list != null) {
        for (PsiElement e : list) {
          if (e.isValid()) {
            return true;
          }
          ProgressManager.checkCanceled();
        }
      }
    }
    return false;
  }

  @Nonnull
  public static Set<GoImportSpec> findDuplicatedEntries(@Nonnull MultiMap<String, GoImportSpec> importMap) {
    Set<GoImportSpec> duplicatedEntries = new LinkedHashSet<>();
    for (Map.Entry<String, Collection<GoImportSpec>> imports : importMap.entrySet()) {
      Collection<GoImportSpec> importsWithSameName = imports.getValue();
      if (importsWithSameName.size() > 1) {
        MultiMap<String, GoImportSpec> importsWithSameString = collectImportsWithSameString(importsWithSameName);

        for (Map.Entry<String, Collection<GoImportSpec>> importWithSameString : importsWithSameString.entrySet()) {
          List<GoImportSpec> duplicates = ContainerUtil.newArrayList(importWithSameString.getValue());
          if (duplicates.size() > 1) {
            duplicatedEntries.addAll(duplicates.subList(1, duplicates.size()));
          }
        }
      }
    }
    return duplicatedEntries;
  }

  private static void deleteImportSpec(@Nullable GoImportSpec importSpec) {
    GoImportDeclaration importDeclaration = PsiTreeUtil.getParentOfType(importSpec, GoImportDeclaration.class);
    if (importSpec != null && importDeclaration != null) {
      PsiElement startElementToDelete = importSpec;
      PsiElement endElementToDelete = importSpec;
      if (importDeclaration.getImportSpecList().size() == 1) {
        startElementToDelete = importDeclaration;
        endElementToDelete = importDeclaration;
        
        PsiElement nextSibling = endElementToDelete.getNextSibling();
        if (nextSibling != null && nextSibling.getNode().getElementType() == GoTypes.SEMICOLON) {
          endElementToDelete = nextSibling;
        }
      }
      // todo: delete after proper formatter implementation
      PsiElement nextSibling = endElementToDelete.getNextSibling();
      if (nextSibling instanceof PsiWhiteSpace && nextSibling.textContains('\n')) {
        endElementToDelete = nextSibling;
      }
      startElementToDelete.getParent().deleteChildRange(startElementToDelete, endElementToDelete);
    }
  }

  @Nonnull
  private static MultiMap<String, GoImportSpec> collectImportsWithSameString(@Nonnull Collection<GoImportSpec> importsWithSameName) {
    MultiMap<String, GoImportSpec> importsWithSameString = MultiMap.create();
    for (PsiElement duplicateCandidate : importsWithSameName) {
      GoImportSpec importSpec = getImportSpec(duplicateCandidate);
      if (importSpec != null) {
        importsWithSameString.putValue(importSpec.getPath(), importSpec);
      }
    }
    return importsWithSameString;
  }

  @Nullable
  public static GoImportSpec getImportSpec(@Nonnull PsiElement importEntry) {
    return PsiTreeUtil.getNonStrictParentOfType(importEntry, GoImportSpec.class);
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}

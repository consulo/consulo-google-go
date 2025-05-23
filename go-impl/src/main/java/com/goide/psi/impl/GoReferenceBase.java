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
import com.goide.sdk.GoSdkUtil;
import consulo.document.util.TextRange;
import consulo.language.psi.*;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.util.collection.SmartList;
import consulo.util.dataholder.Key;
import consulo.util.lang.Comparing;
import consulo.util.lang.ObjectUtil;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.goide.psi.impl.GoPsiImplUtil.allowed;

public abstract class GoReferenceBase<T extends GoReferenceExpressionBase> extends PsiPolyVariantReferenceBase<T> {
  public static final Key<List<PsiElement>> IMPORT_USERS = Key.create("IMPORT_USERS");
  public static final Key<String> ACTUAL_NAME = Key.create("ACTUAL_NAME");

  public GoReferenceBase(T element, TextRange range) {
    super(element, range);
  }

  @Nullable
  protected static String getPath(@Nullable PsiFile file) {
    if (file == null) return null;
    VirtualFile virtualFile = file.getOriginalFile().getVirtualFile();
    return virtualFile == null ? null : virtualFile.getPath();
  }

  private static void putIfAbsent(@Nonnull GoImportSpec importSpec, @Nonnull PsiElement usage) {
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (importSpec) {
      List<PsiElement> newUsages = new SmartList<>(usage);
      newUsages.addAll(IMPORT_USERS.get(importSpec, List.of()));
      importSpec.putUserData(IMPORT_USERS, newUsages);
    }
  }

  protected boolean processDirectory(@Nullable PsiDirectory dir,
                                     @Nullable GoFile file,
                                     @Nullable String packageName,
                                     @Nonnull GoScopeProcessor processor,
                                     @Nonnull ResolveState state,
                                     boolean localProcessing) {
    if (dir == null) return true;
    String filePath = getPath(file);
    Module module = file != null ? ModuleUtilCore.findModuleForPsiElement(file) : null;
    for (PsiFile f : dir.getFiles()) {
      if (!(f instanceof GoFile) || Comparing.equal(getPath(f), filePath)) continue;
      if (packageName != null && !packageName.equals(((GoFile)f).getPackageName())) continue;
      if (!allowed(f, file, module)) continue;
      if (!processFileEntities((GoFile)f, processor, state, localProcessing)) return false;
    }
    return true;
  }

  protected boolean processBuiltin(@Nonnull GoScopeProcessor processor, @Nonnull ResolveState state, @Nonnull GoCompositeElement element) {
    GoFile builtin = GoSdkUtil.findBuiltinFile(element);
    return builtin == null || processFileEntities(builtin, processor, state, true);
  }

  protected boolean processImports(@Nonnull GoFile file,
                                   @Nonnull GoScopeProcessor processor,
                                   @Nonnull ResolveState state,
                                   @Nonnull GoCompositeElement element) {
    for (Map.Entry<String, Collection<GoImportSpec>> entry : file.getImportMap().entrySet()) {
      for (GoImportSpec o : entry.getValue()) {
        if (o.isForSideEffects()) continue;

        GoImportString importString = o.getImportString();
        if (o.isDot()) {
          PsiDirectory implicitDir = importString.resolve();
          boolean resolved = !processDirectory(implicitDir, file, null, processor, state, false);
          if (resolved && !processor.isCompletion()) {
            putIfAbsent(o, element);
          }
          if (resolved) return false;
        }
        else {
          if (o.getAlias() == null) {
            PsiDirectory resolve = importString.resolve();
            if (resolve != null && !processor.execute(resolve, state.put(ACTUAL_NAME, entry.getKey()))) return false;
          }
          // todo: multi-resolve into appropriate package clauses
          if (!processor.execute(o, state.put(ACTUAL_NAME, entry.getKey()))) return false;
        }
      }
    }
    return true;
  }

  @Nonnull
  protected GoScopeProcessor createResolveProcessor(@Nonnull Collection<ResolveResult> result,
                                                    @Nonnull GoReferenceExpressionBase o) {
    return new GoScopeProcessor() {
      @Override
      public boolean execute(@Nonnull PsiElement element, @Nonnull ResolveState state) {
        if (element.equals(o)) return !result.add(new PsiElementResolveResult(element));
        String name = ObjectUtil.chooseNotNull(state.get(ACTUAL_NAME),
                                                element instanceof PsiNamedElement ? ((PsiNamedElement)element).getName() : null);
        if (name != null && o.getIdentifier().textMatches(name)) {
          result.add(new PsiElementResolveResult(element));
          return false;
        }
        return true;
      }
    };
  }

  protected abstract boolean processFileEntities(@Nonnull GoFile file,
                                                 @Nonnull GoScopeProcessor processor,
                                                 @Nonnull ResolveState state,
                                                 boolean localProcessing);
}

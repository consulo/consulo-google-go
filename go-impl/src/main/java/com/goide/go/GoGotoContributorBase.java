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

package com.goide.go;

import com.goide.psi.GoNamedElement;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.Processor;
import consulo.content.scope.SearchScope;
import consulo.ide.navigation.ChooseByNameContributorEx;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FindSymbolParameters;
import consulo.language.psi.stub.IdFilter;
import consulo.language.psi.stub.StubIndex;
import consulo.language.psi.stub.StubIndexKey;
import consulo.navigation.NavigationItem;

import javax.annotation.Nonnull;

public class GoGotoContributorBase<T extends GoNamedElement> implements ChooseByNameContributorEx {
  private final StubIndexKey<String, T>[] myIndexKeys;
  @Nonnull
  private final Class<T> myClazz;

  @SafeVarargs
  public GoGotoContributorBase(@Nonnull Class<T> clazz, @Nonnull StubIndexKey<String, T>... key) {
    myIndexKeys = key;
    myClazz = clazz;
  }

  @Override
  public void processNames(@Nonnull Processor<String> processor, @Nonnull SearchScope scope, IdFilter filter) {
    for (StubIndexKey<String, T> key : myIndexKeys) {
      ProgressManager.checkCanceled();
      StubIndex.getInstance().processAllKeys(key, processor, (GlobalSearchScope) scope, filter);
    }
  }

  @Override
  public void processElementsWithName(@Nonnull String s,
                                      @Nonnull Processor<NavigationItem> processor,
                                      @Nonnull FindSymbolParameters parameters) {
    for (StubIndexKey<String, T> key : myIndexKeys) {
      ProgressManager.checkCanceled();
      StubIndex.getInstance().processElements(key, s, parameters.getProject(), (GlobalSearchScope) parameters.getSearchScope(), parameters.getIdFilter(),
          myClazz, processor);
    }
  }
}

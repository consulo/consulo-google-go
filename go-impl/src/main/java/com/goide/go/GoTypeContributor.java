/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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
import com.goide.psi.GoTypeSpec;
import com.goide.stubs.index.GoTypesIndex;
import consulo.annotation.component.ExtensionImpl;
import consulo.ide.navigation.GotoClassOrTypeContributor;
import consulo.navigation.NavigationItem;

import javax.annotation.Nullable;

@ExtensionImpl
public class GoTypeContributor extends GoGotoContributorBase<GoTypeSpec> implements GotoClassOrTypeContributor {
  public GoTypeContributor() {
    super(GoTypeSpec.class, GoTypesIndex.KEY);
  }

  @Nullable
  @Override
  public String getQualifiedName(NavigationItem item) {
    return item instanceof GoNamedElement ? ((GoNamedElement) item).getQualifiedName() : null;
  }

  @Nullable
  @Override
  public String getQualifiedNameSeparator() {
    return null;
  }
}

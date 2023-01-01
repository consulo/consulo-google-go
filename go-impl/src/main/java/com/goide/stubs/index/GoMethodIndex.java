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

package com.goide.stubs.index;

import com.goide.GoFileElementType;
import com.goide.psi.GoMethodDeclaration;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processor;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.IdFilter;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndex;
import consulo.language.psi.stub.StubIndexKey;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

@ExtensionImpl
public class GoMethodIndex extends StringStubIndexExtension<GoMethodDeclaration> {
  public static final StubIndexKey<String, GoMethodDeclaration> KEY = StubIndexKey.createIndexKey("go.method");

  @Override
  public int getVersion() {
    return GoFileElementType.VERSION + 2;
  }

  @Nonnull
  @Override
  public StubIndexKey<String, GoMethodDeclaration> getKey() {
    return KEY;
  }

  public static Collection<GoMethodDeclaration> find(@Nonnull String name, @Nonnull Project project,
                                                     @Nullable GlobalSearchScope scope, @Nullable IdFilter idFilter) {
    return StubIndex.getElements(KEY, name, project, scope, idFilter, GoMethodDeclaration.class);
  }

  public static boolean process(@Nonnull String name,
                                @Nonnull Project project,
                                @Nullable GlobalSearchScope scope,
                                @Nullable IdFilter idFilter,
                                @Nonnull Processor<GoMethodDeclaration> processor) {
    return StubIndex.getInstance().processElements(KEY, name, project, scope, idFilter, GoMethodDeclaration.class, processor);
  }
}

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

package com.goide.stubs.index;

import com.goide.GoFileElementType;
import com.goide.psi.GoMethodSpec;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StringStubIndexExtension;
import consulo.language.psi.stub.StubIndex;
import consulo.language.psi.stub.StubIndexKey;
import consulo.project.Project;

import javax.annotation.Nonnull;
import java.util.Collection;

@ExtensionImpl
public class GoMethodFingerprintIndex extends StringStubIndexExtension<GoMethodSpec> {
  public static final StubIndexKey<String, GoMethodSpec> KEY = StubIndexKey.createIndexKey("go.method.fingerprint");

  @Override
  public int getVersion() {
    return GoFileElementType.VERSION + 1;
  }

  @Nonnull
  @Override
  public StubIndexKey<String, GoMethodSpec> getKey() {
    return KEY;
  }

  public static Collection<GoMethodSpec> find(@Nonnull String name, @Nonnull Project project, GlobalSearchScope scope) {
    return StubIndex.getElements(KEY, name, project, scope, GoMethodSpec.class);
  }
}

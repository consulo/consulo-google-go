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

package com.goide.stubs.types;

import com.goide.psi.GoNamedElement;
import com.goide.stubs.GoFileStub;
import com.goide.stubs.GoNamedStub;
import com.goide.stubs.index.GoAllPrivateNamesIndex;
import com.goide.stubs.index.GoAllPublicNamesIndex;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.StubElement;
import consulo.language.psi.stub.StubIndexKey;
import consulo.util.lang.StringUtil;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public abstract class GoNamedStubElementType<S extends GoNamedStub<T>, T extends GoNamedElement> extends GoStubElementType<S, T> {
  public GoNamedStubElementType(@NonNls @Nonnull String debugName) {
    super(debugName);
  }

  @Override
  public boolean shouldCreateStub(@Nonnull ASTNode node) {
    if (!super.shouldCreateStub(node)) return false;
    PsiElement psi = node.getPsi();
    return psi instanceof GoNamedElement && StringUtil.isNotEmpty(((GoNamedElement)psi).getName());
  }

  @Override
  public void indexStub(@Nonnull S stub, @Nonnull IndexSink sink) {
    String name = stub.getName();
    if (shouldIndex() && StringUtil.isNotEmpty(name)) {
      String packageName = null;
      StubElement parent = stub.getParentStub();
      while (parent != null) {
        if (parent instanceof GoFileStub) {
          packageName = ((GoFileStub)parent).getPackageName();
          break;
        }
        parent = parent.getParentStub();
      }
      
      String indexingName = StringUtil.isNotEmpty(packageName) ? packageName + "." + name : name;
      if (stub.isPublic()) {
        sink.occurrence(GoAllPublicNamesIndex.ALL_PUBLIC_NAMES, indexingName);
      }
      else {
        sink.occurrence(GoAllPrivateNamesIndex.ALL_PRIVATE_NAMES, indexingName);
      }
      for (StubIndexKey<String, ? extends GoNamedElement> key : getExtraIndexKeys()) {
        sink.occurrence(key, name);
      }
    }
  }

  protected boolean shouldIndex() {
    return true;
  }

  @Nonnull
  protected Collection<StubIndexKey<String, ? extends GoNamedElement>> getExtraIndexKeys() {
    return Collections.emptyList();
  }
}

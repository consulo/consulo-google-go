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

package com.goide.stubs;

import com.goide.psi.GoConstSpec;
import com.goide.psi.GoExpression;
import com.goide.psi.impl.GoElementFactory;
import consulo.index.io.StringRef;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.StubBase;
import consulo.language.psi.stub.StubElement;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

public class GoConstSpecStub extends StubBase<GoConstSpec> {
  private final StringRef myExpressionsRef;
  private List<GoExpression> myList;

  public GoConstSpecStub(StubElement parent, IStubElementType elementType, StringRef ref) {
    super(parent, elementType);
    myExpressionsRef = ref;
  }

  @Nullable
  public String getExpressionsText() {
    return myExpressionsRef == null? null : myExpressionsRef.getString();
  }

  @Nonnull
  public List<GoExpression> getExpressionList() {
    if (myList == null) {
      String text = getExpressionsText();
      if (!StringUtil.isNotEmpty(text)) return myList = List.of();
      Project project = getPsi().getProject();
      List<String> split = StringUtil.split(text, ";");
      myList = ContainerUtil.map(split, s -> GoElementFactory.createExpression(project, s));
    }
    return myList;
  }
}

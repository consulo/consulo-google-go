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

package com.plan9.intel.lang.core.psi;

import com.plan9.intel.lang.AsmIntelLanguage;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;

import jakarta.annotation.Nonnull;

public class AsmIntelFile extends PsiFileBase {
  public AsmIntelFile(@Nonnull FileViewProvider viewProvider) {
    super(viewProvider, AsmIntelLanguage.INSTANCE);
  }
}

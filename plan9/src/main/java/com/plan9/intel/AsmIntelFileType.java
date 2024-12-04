/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan, Stuart Carnie
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

package com.plan9.intel;

import com.plan9.intel.lang.AsmIntelLanguage;
import consulo.execution.debug.icon.ExecutionDebugIconGroup;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AsmIntelFileType extends LanguageFileType {

  public static final AsmIntelFileType INSTANCE = new AsmIntelFileType();

  private AsmIntelFileType() {
    super(AsmIntelLanguage.INSTANCE);
  }

  @Nonnull
  @Override
  public String getId() {
    return "x86 Plan9 Assembly";
  }

  @Nonnull
  @Override
  public LocalizeValue getDescription() {
    return LocalizeValue.localizeTODO("x86 Plan9 Assembly file");
  }

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "s";
  }

  @Nullable
  @Override
  public Image getIcon() {
    return ExecutionDebugIconGroup.nodeMemory();
  }
}

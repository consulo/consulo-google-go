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

package com.goide;

import consulo.google.go.localize.GoLocalize;
import consulo.language.Language;
import consulo.language.file.LanguageFileType;

import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GoLanguage extends Language {
  public static final Language INSTANCE = new GoLanguage();

  private GoLanguage() {
    super("go", "text/go", "text/x-go", "application/x-go");
  }

  @Nonnull
  @Override
  public LocalizeValue getDisplayName() {
    return GoLocalize.goFileTypeDisplayName();
  }

  @Nullable
  @Override
  public LanguageFileType getAssociatedFileType() {
    return GoFileType.INSTANCE;
  }

  @Override
  public boolean isCaseSensitive() {
    return true;
  }
}

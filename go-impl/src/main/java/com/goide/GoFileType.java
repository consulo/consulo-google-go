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

import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.ui.image.Image;

public class GoFileType extends LanguageFileType {
  public static final LanguageFileType INSTANCE = new GoFileType();

  protected GoFileType() {
    super(GoLanguage.INSTANCE);
  }

  @Nonnull
  @Override
  public String getName() {
    return GoConstants.GO;
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "Go files";
  }

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "go";
  }

  @Nullable
  @Override
  public Image getIcon() {
    return GoIcons.ICON;
  }

  @Override
  public String getCharset(@Nonnull VirtualFile file, @Nonnull byte[] content) {
    return CharsetToolkit.UTF8;
  }

  @Override
  public Charset extractCharsetFromFileContent(@Nullable Project project, @Nullable VirtualFile file, @Nonnull CharSequence content) {
    return CharsetToolkit.UTF8_CHARSET;
  }
}
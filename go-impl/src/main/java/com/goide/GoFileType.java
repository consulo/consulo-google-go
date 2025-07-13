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

import consulo.google.go.icon.GoogleGoIconGroup;
import consulo.google.go.localize.GoLocalize;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.io.CharsetToolkit;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GoFileType extends LanguageFileType {
    public static final LanguageFileType INSTANCE = new GoFileType();

    protected GoFileType() {
        super(GoLanguage.INSTANCE);
    }

    @Nonnull
    @Override
    public String getId() {
        return GoConstants.GO;
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return GoLocalize.goFileTypeDisplayName();
    }

    @Nonnull
    @Override
    public LocalizeValue getDescription() {
        return GoLocalize.goFileDescription();
    }

    @Nonnull
    @Override
    public String getDefaultExtension() {
        return "go";
    }

    @Nullable
    @Override
    public Image getIcon() {
        return GoogleGoIconGroup.gofiletype();
    }

    @Override
    public String getCharset(@Nonnull VirtualFile file, @Nonnull byte[] content) {
        return CharsetToolkit.UTF8;
    }

    @Override
    public Charset extractCharsetFromFileContent(@Nullable Project project, @Nullable VirtualFile file, @Nonnull CharSequence content) {
        return StandardCharsets.UTF_8;
    }
}

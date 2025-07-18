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

package com.goide.formatter.settings;

import com.goide.GoLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.Configurable;
import consulo.language.Language;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.setting.CodeStyleSettingsProvider;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class GoCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Nullable
    @Override
    public Language getLanguage() {
        return GoLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public Configurable createSettingsPage(@Nonnull CodeStyleSettings settings, CodeStyleSettings originalSettings) {
        return new GoCodeStyleConfigurable(settings, originalSettings);
    }
}

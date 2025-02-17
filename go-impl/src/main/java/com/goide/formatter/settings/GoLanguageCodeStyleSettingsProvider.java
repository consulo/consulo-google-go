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

package com.goide.formatter.settings;

import com.goide.GoLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.codeStyle.CommonCodeStyleSettings;
import consulo.language.codeStyle.setting.IndentOptionsEditor;
import consulo.language.codeStyle.setting.LanguageCodeStyleSettingsProvider;
import consulo.language.codeStyle.ui.setting.SmartIndentOptionsEditor;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
  private static final String DEFAULT_CODE_SAMPLE =
    "package main\n" +
    "\n" +
    "import \"fmt\"\n" +
    "\n" +
    "func main() {\n" +
    "\tfmt.Println(\"Hello\")\n" +
    "}";

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }

  @Nonnull
  @Override
  public String getCodeSample(@Nonnull SettingsType settingsType) {
    return DEFAULT_CODE_SAMPLE;
  }

  @Override
  public IndentOptionsEditor getIndentOptionsEditor() {
    return new SmartIndentOptionsEditor();
  }

  @Override
  public CommonCodeStyleSettings getDefaultCommonSettings() {
    CommonCodeStyleSettings defaultSettings = new CommonCodeStyleSettings(getLanguage());
    CommonCodeStyleSettings.IndentOptions indentOptions = defaultSettings.initIndentOptions();
    indentOptions.INDENT_SIZE = 8;
    indentOptions.CONTINUATION_INDENT_SIZE = 8;
    indentOptions.TAB_SIZE = 8;
    indentOptions.USE_TAB_CHARACTER = true;
    
    defaultSettings.BLOCK_COMMENT_AT_FIRST_COLUMN = false;
    defaultSettings.LINE_COMMENT_AT_FIRST_COLUMN = false;
    return defaultSettings;
  }
}
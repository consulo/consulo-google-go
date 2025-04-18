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

import com.goide.GoConstants;
import com.goide.GoLanguage;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractConfigurable;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractPanel;
import consulo.language.codeStyle.ui.setting.TabbedLanguageCodeStylePanel;

import jakarta.annotation.Nonnull;

public class GoCodeStyleConfigurable extends CodeStyleAbstractConfigurable {
  public GoCodeStyleConfigurable(@Nonnull CodeStyleSettings settings, CodeStyleSettings cloneSettings) {
    super(settings, cloneSettings, GoConstants.GO);
  }

  @Nonnull
  @Override
  protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
    return new GoCodeStyleMainPanel(getCurrentSettings(), settings);
  }

  @Override
  public String getHelpTopic() {
    return null;
  }

  private static class GoCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
    private GoCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
      super(GoLanguage.INSTANCE, currentSettings, settings);
    }

    @Override
    protected void addSpacesTab(CodeStyleSettings settings) {
    }

    @Override
    protected void addBlankLinesTab(CodeStyleSettings settings) {
    }

    @Override
    protected void addWrappingAndBracesTab(CodeStyleSettings settings) {
    }
  }
}
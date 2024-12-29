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

package com.goide.inspections.unresolved;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GoUnusedExportedFunctionInspection extends GoUnusedFunctionInspection {
  @Override
  protected boolean canRun(String name) {
    return StringUtil.isCapitalized(name);
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Declaration redundancy";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "Unused exported function inspection";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }
}

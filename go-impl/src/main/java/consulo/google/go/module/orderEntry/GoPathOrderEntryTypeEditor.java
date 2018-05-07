/*
 * Copyright 2013-2017 consulo.io
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

package consulo.google.go.module.orderEntry;

import org.jetbrains.annotations.NotNull;
import com.goide.GoIcons;
import com.intellij.openapi.roots.ui.CellAppearanceEx;
import com.intellij.openapi.roots.ui.util.SimpleTextCellAppearance;
import com.intellij.ui.SimpleTextAttributes;
import consulo.awt.TargetAWT;
import consulo.roots.orderEntry.OrderEntryTypeEditor;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoPathOrderEntryTypeEditor implements OrderEntryTypeEditor<GoPathOrderEntry> {
  @NotNull
  public CellAppearanceEx getCellAppearance(@NotNull GoPathOrderEntry orderEntry) {
    return new SimpleTextCellAppearance(orderEntry.getPresentableName(), TargetAWT.to(GoIcons.ICON), SimpleTextAttributes.SYNTHETIC_ATTRIBUTES);
  }
}

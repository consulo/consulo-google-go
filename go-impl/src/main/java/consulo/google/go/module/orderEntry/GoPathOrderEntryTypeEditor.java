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

import com.goide.GoIcons;
import consulo.annotation.component.ExtensionImpl;
import consulo.ide.setting.module.CustomOrderEntryTypeEditor;
import consulo.module.content.layer.orderEntry.CustomOrderEntry;
import consulo.ui.ex.ColoredTextContainer;
import consulo.ui.ex.SimpleTextAttributes;

import jakarta.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 05-May-17
 */
@ExtensionImpl
public class GoPathOrderEntryTypeEditor implements CustomOrderEntryTypeEditor<GoPathOrderEntryModel> {
  @Nonnull
  @Override
  public Consumer<ColoredTextContainer> getRender(@Nonnull CustomOrderEntry<GoPathOrderEntryModel> orderEntry) {
    return c -> {
      c.setIcon(GoIcons.ICON);
      c.append(orderEntry.getPresentableName(), SimpleTextAttributes.SYNTHETIC_ATTRIBUTES);
    };
  }

  @Nonnull
  @Override
  public String getOrderTypeId() {
    return "gopath";
  }
}

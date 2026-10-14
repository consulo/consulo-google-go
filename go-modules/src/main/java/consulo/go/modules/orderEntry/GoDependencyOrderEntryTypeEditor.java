/*
 * Copyright 2013-2026 consulo.io
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
package consulo.go.modules.orderEntry;

import consulo.annotation.component.ExtensionImpl;
import consulo.go.localize.GoLocalize;
import consulo.ide.setting.module.CustomOrderEntryTypeEditor;
import consulo.module.content.layer.orderEntry.CustomOrderEntry;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.ColoredTextContainer;
import consulo.ui.ex.SimpleTextAttributes;

import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoDependencyOrderEntryTypeEditor implements CustomOrderEntryTypeEditor<GoDependencyOrderEntryModel> {
    @Override
    public Consumer<ColoredTextContainer> getRenderer(CustomOrderEntry<GoDependencyOrderEntryModel> orderEntry, GoDependencyOrderEntryModel model) {
        return container -> {
            container.setIcon(PlatformIconGroup.nodesPplib());
            container.append(model.getModulePath(), model.findDirectory() != null
                ? SimpleTextAttributes.REGULAR_ATTRIBUTES
                : SimpleTextAttributes.ERROR_ATTRIBUTES
            );
            String version = model.getVersion();
            if (version != null) {
                container.append(" " + version, SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
            String replacement = model.getReplacement();
            if (replacement != null) {
                container.append(" => " + replacement, SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
            if (model.isIndirect()) {
                container.append(" " + GoLocalize.goModulesDependencyIndirect().get(), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
            }
        };
    }

    @Override
    public String getOrderTypeId() {
        return GoDependencyOrderEntryType.ID;
    }
}

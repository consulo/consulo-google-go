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

import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.orderEntry.CustomOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoDependencyEntries {
    private GoDependencyEntries() {
    }

    /**
     * Swaps the Go dependencies of a module for {@code models}, keeping the entries that did not change so an unchanged
     * build list does not touch the root model.
     */
    public static void replace(ModifiableRootModel rootModel, List<GoDependencyOrderEntryModel> models) {
        List<GoDependencyOrderEntryModel> existing = new ArrayList<>();
        for (OrderEntry orderEntry : rootModel.getOrderEntries()) {
            GoDependencyOrderEntryModel model = getModel(orderEntry);
            if (model != null) {
                existing.add(model);
            }
        }
        if (isSame(existing, models)) {
            return;
        }

        removeAll(rootModel);
        GoDependencyOrderEntryType type = GoDependencyOrderEntryType.getInstance();
        for (GoDependencyOrderEntryModel model : models) {
            rootModel.addCustomOderEntry(type, model);
        }
    }

    public static void removeAll(ModifiableRootModel rootModel) {
        for (OrderEntry orderEntry : rootModel.getOrderEntries()) {
            if (getModel(orderEntry) != null) {
                rootModel.removeOrderEntry(orderEntry);
            }
        }
    }

    public static @Nullable GoDependencyOrderEntryModel getModel(OrderEntry orderEntry) {
        return orderEntry instanceof CustomOrderEntry<?> customOrderEntry
            && customOrderEntry.getModel() instanceof GoDependencyOrderEntryModel model ? model : null;
    }

    private static boolean isSame(List<GoDependencyOrderEntryModel> existing, List<GoDependencyOrderEntryModel> models) {
        if (existing.size() != models.size()) {
            return false;
        }
        for (int i = 0; i < existing.size(); i++) {
            if (!existing.get(i).isEquivalentTo(models.get(i))) {
                return false;
            }
        }
        return true;
    }
}

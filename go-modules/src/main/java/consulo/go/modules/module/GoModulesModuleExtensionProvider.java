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

package consulo.go.modules.module;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionProvider;
import consulo.go.module.extension.GoModuleExtensionProvider;
import consulo.go.modules.GoModulesConstants;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;

/**
 * The module extension the external system import switches on; its id is the id of {@link GoModulesConstants#SYSTEM_ID}.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModulesModuleExtensionProvider extends ExternalSystemModuleExtensionProvider {
    public GoModulesModuleExtensionProvider() {
        super(GoModulesConstants.SYSTEM_ID);
    }

    @Override
    public String getParentId() {
        return GoModuleExtensionProvider.ID;
    }

    @Override
    public ModuleExtension<ExternalSystemModuleExtensionImpl> createImmutableExtension(ModuleRootLayer moduleRootLayer) {
        return new GoModulesModuleExtension(getId(), moduleRootLayer);
    }

    @Override
    public MutableModuleExtension<ExternalSystemModuleExtensionImpl> createMutableExtension(ModuleRootLayer moduleRootLayer) {
        return new GoModulesMutableModuleExtension(getId(), moduleRootLayer);
    }
}

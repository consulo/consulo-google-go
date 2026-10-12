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

package consulo.go.module.extension.ui;

import com.goide.configuration.GoBuildTagsUI;
import com.goide.configuration.GoVendoringUI;
import consulo.disposer.Disposable;
import consulo.go.module.extension.GoMutableModuleExtension;
import consulo.module.ui.extension.ModuleExtensionBundleBoxBuilder;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public final class GoModuleExtensionPanel {
    private GoModuleExtensionPanel() {
    }

    @RequiredUIAccess
    public static Component create(GoMutableModuleExtension extension, Disposable uiDisposable, Runnable runnable) {
        GoBuildTagsUI buildTagsUI = new GoBuildTagsUI(extension);
        GoVendoringUI vendoringUI = new GoVendoringUI(extension);

        ModuleExtensionBundleBoxBuilder<?> builder = ModuleExtensionBundleBoxBuilder.createAndDefine(extension, uiDisposable, runnable);
        builder.postConsumer((oldSdk, newSdk) -> {
            buildTagsUI.sdkChanged();
            vendoringUI.sdkChanged();
        });

        VerticalLayout layout = VerticalLayout.create();
        layout.add(builder.build());
        layout.add(buildTagsUI.getComponent());
        layout.add(vendoringUI.getComponent());
        return layout;
    }
}

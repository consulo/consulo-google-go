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

package consulo.google.go.module.extension.ui;

import com.goide.configuration.GoBuildTagsUI;
import com.goide.configuration.GoVendoringUI;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBCheckBox;
import consulo.annotations.RequiredDispatchThread;
import consulo.extension.ui.ModuleExtensionSdkBoxBuilder;
import consulo.google.go.module.extension.GoMutableModuleExtension;
import consulo.google.go.module.orderEntry.GoPathOrderEntry;
import consulo.roots.ModifiableModuleRootLayer;
import consulo.roots.impl.ModuleRootLayerImpl;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoModuleExtensionPanel extends JPanel {
  private GoMutableModuleExtension myExtension;

  @RequiredDispatchThread
  public GoModuleExtensionPanel(GoMutableModuleExtension extension, Runnable runnable) {
    super(new VerticalFlowLayout(true, false));
    myExtension = extension;

    JBCheckBox usePgoPath = new JBCheckBox("Use GOPATH that's defined in system environment", findGoPathOrderEntry() != null);
    usePgoPath.addActionListener(e -> {
      boolean selected = usePgoPath.isSelected();

      ModifiableModuleRootLayer moduleRootLayer = extension.getModuleRootLayer();
      if (selected) {
        moduleRootLayer.addOrderEntry(new GoPathOrderEntry((ModuleRootLayerImpl)moduleRootLayer));
      }
      else {
        GoPathOrderEntry goPathOrderErntry = findGoPathOrderEntry();
        if (goPathOrderErntry != null) {
          moduleRootLayer.removeOrderEntry(goPathOrderErntry);
        }
      }

      SwingUtilities.invokeLater(runnable);
    });


    GoBuildTagsUI buildTagsUI = new GoBuildTagsUI(extension);
    GoVendoringUI vendoringUI = new GoVendoringUI(extension);

    ModuleExtensionSdkBoxBuilder builder = ModuleExtensionSdkBoxBuilder.createAndDefine(extension, runnable);
    builder.postConsumer((oldSdk, newSdk) -> {
      buildTagsUI.sdkChanged();
      vendoringUI.sdkChanged();
    });
    add(builder.build());
    add(usePgoPath);
    add(buildTagsUI.getPanel());
    add(vendoringUI.getPanel());
  }

  @Nullable
  private GoPathOrderEntry findGoPathOrderEntry() {
    OrderEntry[] orderEntries = myExtension.getModuleRootLayer().getOrderEntries();
    for (OrderEntry orderEntry : orderEntries) {
      if (orderEntry instanceof GoPathOrderEntry) {
        return (GoPathOrderEntry)orderEntry;
      }
    }
    return null;
  }
}

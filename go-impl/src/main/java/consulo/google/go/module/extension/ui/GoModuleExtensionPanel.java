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
import consulo.google.go.module.extension.GoMutableModuleExtension;
import consulo.google.go.module.orderEntry.GoPathOrderEntryModel;
import consulo.google.go.module.orderEntry.GoPathOrderEntryType;
import consulo.module.content.layer.ModifiableModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntry;
import consulo.module.content.layer.orderEntry.CustomOrderEntryModel;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.module.ui.extension.ModuleExtensionSdkBoxBuilder;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.JBCheckBox;
import consulo.ui.ex.awt.VerticalFlowLayout;

import javax.annotation.Nullable;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoModuleExtensionPanel extends JPanel {
  private GoMutableModuleExtension myExtension;

  @RequiredUIAccess
  public GoModuleExtensionPanel(GoMutableModuleExtension extension, Runnable runnable) {
    super(new VerticalFlowLayout(true, false));
    myExtension = extension;

    JBCheckBox usePgoPath = new JBCheckBox("Use GOPATH that's defined in system environment", findGoPathOrderEntry() != null);
    usePgoPath.addActionListener(e -> {
      boolean selected = usePgoPath.isSelected();

      ModifiableModuleRootLayer moduleRootLayer = (ModifiableModuleRootLayer) extension.getModuleRootLayer();
      if (selected) {
        moduleRootLayer.addCustomOderEntry(GoPathOrderEntryType.getInstance(), new GoPathOrderEntryModel());
      }
      else {
        OrderEntry orderEntry = findGoPathOrderEntry();
        if (orderEntry != null) {
          moduleRootLayer.removeOrderEntry(orderEntry);
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
  private OrderEntry findGoPathOrderEntry() {
    OrderEntry[] orderEntries = myExtension.getModuleRootLayer().getOrderEntries();
    for (OrderEntry orderEntry : orderEntries) {
      if (orderEntry instanceof CustomOrderEntry) {
        CustomOrderEntryModel model = ((CustomOrderEntry<?>) orderEntry).getModel();
        if (model instanceof GoPathOrderEntryModel) {
          return orderEntry;
        }
      }
    }
    return null;
  }
}

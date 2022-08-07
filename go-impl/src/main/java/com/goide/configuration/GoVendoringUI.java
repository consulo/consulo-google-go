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

package com.goide.configuration;

import com.goide.project.GoVendoringUtil;
import consulo.application.AllIcons;
import consulo.content.bundle.Sdk;
import consulo.google.go.module.extension.GoMutableModuleExtension;
import consulo.ui.ex.awt.ComboBox;
import consulo.ui.ex.awt.IdeBorderFactory;
import consulo.ui.ex.awt.JBLabel;
import consulo.ui.ex.awt.MutableCollectionComboBoxModel;
import consulo.ui.image.Image;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.ThreeState;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.ArrayList;

public class GoVendoringUI {
  private static final String ENABLED = "Enabled";
  private static final String DISABLED = "Disabled";

  @Nonnull
  private final MutableCollectionComboBoxModel myVendoringEnabledComboModel = new MutableCollectionComboBoxModel<String>(new ArrayList<>());
  @Nonnull
  private String myDefaultComboText = "";

  private JPanel myPanel;
  private JBLabel myErrorMessageLabel;
  private ComboBox myVendoringEnabledCombo;
  @SuppressWarnings("unused")
  private JTextPane myDescriptionPane;

  private GoMutableModuleExtension myModuleExtension;

  public GoVendoringUI(GoMutableModuleExtension moduleExtension) {
    myModuleExtension = moduleExtension;
    myPanel.setBorder(IdeBorderFactory.createTitledBorder("Vendor experiment"));

    sdkChanged();
    myVendoringEnabledCombo.addActionListener(e -> moduleExtension.setVendoringEnabled(getValue()));
  }

  public void sdkChanged() {
    Sdk sdk = myModuleExtension.getSdk();
    String sdkVersion = sdk == null ? null : sdk.getVersionString();
    if (!GoVendoringUtil.vendoringCanBeDisabled(sdkVersion)) {
      myErrorMessageLabel.setIcon((Image) AllIcons.General.BalloonWarning);
      myErrorMessageLabel.setText("Go " + sdkVersion + " doesn't support disabling vendor experiment");
      myErrorMessageLabel.setVisible(true);
      myVendoringEnabledCombo.setEnabled(false);
    }
    else if (!GoVendoringUtil.supportsVendoring(sdkVersion) && sdkVersion != null) {
      myErrorMessageLabel.setIcon((Image) AllIcons.General.BalloonWarning);
      myErrorMessageLabel.setText("Go " + sdkVersion + " doesn't support vendor experiment");
      myErrorMessageLabel.setVisible(true);
      myVendoringEnabledCombo.setEnabled(true);
    }
    else {
      myErrorMessageLabel.setVisible(false);
      myVendoringEnabledCombo.setEnabled(true);
    }

    myDefaultComboText = "Default for SDK (" + (GoVendoringUtil.supportsVendoringByDefault(sdkVersion) ? ENABLED : DISABLED) + ")";
    //noinspection unchecked
    myVendoringEnabledComboModel.update(ContainerUtil.newArrayList(myDefaultComboText, ENABLED, DISABLED));

    switch (myModuleExtension.getVendoringEnabled()) {
      case YES:
        myVendoringEnabledComboModel.setSelectedItem(ENABLED);
        break;
      case NO:
        myVendoringEnabledComboModel.setSelectedItem(DISABLED);
        break;
      case UNSURE:
        myVendoringEnabledComboModel.setSelectedItem(myDefaultComboText);
        break;
    }
  }

  public ThreeState getValue() {
    Object item = myVendoringEnabledComboModel.getSelectedItem();
    if (ENABLED.equals(item)) {
      return ThreeState.YES;
    }
    else if (DISABLED.equals(item)) {
      return ThreeState.NO;
    }
    else {
      return ThreeState.UNSURE;
    }
  }

  public JPanel getPanel() {
    return myPanel;
  }

  private void createUIComponents() {
    myDescriptionPane = GoUIUtil.createDescriptionPane();
  }
}

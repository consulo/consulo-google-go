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

import com.goide.GoConstants;
import com.goide.project.GoBuildTargetSettings;
import com.goide.util.GoUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.MutableCollectionComboBoxModel;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ThreeState;
import com.intellij.util.containers.ContainerUtil;
import consulo.google.go.module.extension.GoMutableModuleExtension;
import javax.annotation.Nonnull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;

public class GoBuildTagsUI {
  private static final String ENABLED = "Enabled";
  private static final String DISABLED = "Disabled";

  private JPanel myPanel;
  private ComboBox myOSCombo;
  private ComboBox myArchCombo;
  private ComboBox myGoVersionCombo;
  private ComboBox myCompilerCombo;
  private ComboBox myCgoCombo;
  private RawCommandLineEditor myCustomTagsField;
  @SuppressWarnings("unused")
  private JTextPane myDescriptionPane;

  @Nonnull
  private final MutableCollectionComboBoxModel<String> myCgoComboModel;

  @Nonnull
  private final String myDefaultOSValue;
  @Nonnull
  private final String myDefaultArchValue;
  @Nonnull
  private String myDefaultCgo;
  @Nonnull
  private String myDefaultGoVersion = "";

  private GoMutableModuleExtension myExtension;

  @SuppressWarnings("unchecked")
  public GoBuildTagsUI(GoMutableModuleExtension extension) {
    myExtension = extension;
    myPanel.setBorder(IdeBorderFactory.createTitledBorder("Build tags"));

    myDefaultOSValue = "Default (" + GoUtil.systemOS() + ")";
    myDefaultArchValue = "Default (" + GoUtil.systemArch() + ")";
    myDefaultCgo = "Default (" + cgo(GoUtil.systemCgo(myDefaultOSValue, myDefaultArchValue)) + ")";
    myCustomTagsField.setDialogCaption("Custom Build Tags");

    myOSCombo.setModel(createModel(GoConstants.KNOWN_OS, myDefaultOSValue));
    myArchCombo.setModel(createModel(GoConstants.KNOWN_ARCH, myDefaultArchValue));
    myCgoComboModel = createModel(ContainerUtil.newArrayList(ENABLED, DISABLED), myDefaultCgo);
    myCgoCombo.setModel(myCgoComboModel);
    myCompilerCombo.setModel(createModel(GoConstants.KNOWN_COMPILERS, GoBuildTargetSettings.ANY_COMPILER));

    ActionListener updateCgoListener = event -> {
      String selected = StringUtil.notNullize(myCgoComboModel.getSelected(), myDefaultCgo);
      String oldDefault = myDefaultCgo;
      String os = expandDefault(selected(myOSCombo, myDefaultOSValue), GoUtil.systemOS());
      String arch = expandDefault(selected(myArchCombo, myDefaultArchValue), GoUtil.systemArch());

      myDefaultCgo = "Default (" + cgo(GoUtil.systemCgo(os, arch)) + ")";
      myCgoComboModel.update(ContainerUtil.newArrayList(myDefaultCgo, ENABLED, DISABLED));
      myCgoComboModel.setSelectedItem(oldDefault.equals(selected) ? myDefaultCgo : selected);
    };
    myOSCombo.addActionListener(updateCgoListener);
    myArchCombo.addActionListener(updateCgoListener);

    GoBuildTargetSettings buildTargetSettings = extension.getBuildTargetSettings();
    myOSCombo.setSelectedItem(expandDefault(buildTargetSettings.os, myDefaultOSValue));
    myArchCombo.setSelectedItem(expandDefault(buildTargetSettings.arch, myDefaultArchValue));
    myGoVersionCombo.setSelectedItem(expandDefault(buildTargetSettings.goVersion, myDefaultGoVersion));
    myCgoCombo.setSelectedItem(expandDefault(cgo(buildTargetSettings.cgo), myDefaultCgo));
    myCompilerCombo.setSelectedItem(buildTargetSettings.compiler);
    myCustomTagsField.setText(StringUtil.join(buildTargetSettings.customFlags, " "));

    sdkChanged();

    myOSCombo.addActionListener(e -> buildTargetSettings.os = selected(myOSCombo, myDefaultOSValue));
    myArchCombo.addActionListener(e -> buildTargetSettings.arch = selected(myArchCombo, myDefaultArchValue));
    myGoVersionCombo.addActionListener(e -> buildTargetSettings.goVersion = selected(myGoVersionCombo, myDefaultGoVersion));
    myCompilerCombo.addActionListener(e -> buildTargetSettings.compiler = selectedCompiler());
    myCgoCombo.addActionListener(e -> buildTargetSettings.cgo = selectedCgo());
    myCustomTagsField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent documentEvent) {
        buildTargetSettings.customFlags = selectedCustomTags();
      }
    });
  }

  public void sdkChanged() {
    Sdk sdk = myExtension.getSdk();
    String sdkVersion = sdk == null ? null : myExtension.getSdkName();
    myDefaultGoVersion = "Module SDK (" + StringUtil.notNullize(sdkVersion, "any") + ")";
    //noinspection unchecked
    myGoVersionCombo.setModel(createModel(GoConstants.KNOWN_VERSIONS, myDefaultGoVersion));
  }

  @Nonnull
  private String selectedCompiler() {
    Object item = myCompilerCombo.getSelectedItem();
    return item instanceof String ? (String)item : GoBuildTargetSettings.ANY_COMPILER;
  }

  @Nonnull
  private String[] selectedCustomTags() {
    return ArrayUtil.toStringArray(StringUtil.split(myCustomTagsField.getText(), " "));
  }

  @Nonnull
  private ThreeState selectedCgo() {
    String string = myCgoComboModel.getSelected();
    if (ENABLED.equals(string)) {
      return ThreeState.YES;
    }
    if (DISABLED.equals(string)) {
      return ThreeState.NO;
    }
    return ThreeState.UNSURE;
  }

  @Nonnull
  private static String selected(@Nonnull ComboBox comboBox, @Nonnull String defaultValue) {
    Object item = comboBox.getSelectedItem();
    if (item instanceof String) {
      return defaultValue.equals(item) ? GoBuildTargetSettings.DEFAULT : (String)item;
    }
    return GoBuildTargetSettings.DEFAULT;
  }

  @Nonnull
  private static String expandDefault(@Nonnull String value, @Nonnull String defaultValue) {
    return GoBuildTargetSettings.DEFAULT.equals(value) ? defaultValue : value;
  }

  @Nonnull
  private static MutableCollectionComboBoxModel<String> createModel(@Nonnull Collection<String> values, @Nonnull String defaultValue) {
    List<String> items = ContainerUtil.newArrayList(defaultValue);
    items.addAll(ContainerUtil.sorted(values));
    return new MutableCollectionComboBoxModel<>(items, defaultValue);
  }

  @Nonnull
  public JPanel getPanel() {
    return myPanel;
  }

  @Nonnull
  private static String cgo(@Nonnull ThreeState threeState) {
    if (threeState == ThreeState.YES) {
      return ENABLED;
    }
    if (threeState == ThreeState.NO) {
      return DISABLED;
    }
    return GoBuildTargetSettings.DEFAULT;
  }

  private void createUIComponents() {
    myDescriptionPane = GoUIUtil.createDescriptionPane();
  }
}

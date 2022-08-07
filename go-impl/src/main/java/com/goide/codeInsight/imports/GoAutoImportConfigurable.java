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

package com.goide.codeInsight.imports;

import com.goide.project.GoExcludedPathsSettings;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationBundle;
import consulo.configurable.ConfigurationException;
import consulo.configurable.ProjectConfigurable;
import consulo.configurable.SearchableConfigurable;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.*;
import consulo.util.lang.StringUtil;
import jakarta.inject.Inject;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

@ExtensionImpl
public class GoAutoImportConfigurable implements SearchableConfigurable, ProjectConfigurable {
  private JCheckBox myCbShowImportPopup;
  private JCheckBox myCbAddUnambiguousImports;
  private JBList myExcludePackagesList;
  private DefaultListModel myExcludePackagesModel;

  @Nonnull
  private final GoCodeInsightSettings myCodeInsightSettings;
  @Nonnull
  private final GoExcludedPathsSettings myExcludedSettings;
  private final boolean myIsDefaultProject;

  @Inject
  public GoAutoImportConfigurable(@Nonnull Project project) {
    myCodeInsightSettings = GoCodeInsightSettings.getInstance();
    myExcludedSettings = GoExcludedPathsSettings.getInstance(project);
    myIsDefaultProject = project.isDefault();
  }

  @RequiredUIAccess
  @Nullable
  @Override
  public JComponent createComponent() {
    FormBuilder builder = FormBuilder.createFormBuilder();
    myCbShowImportPopup = new JCheckBox(ApplicationBundle.message("checkbox.show.import.popup"));
    myCbAddUnambiguousImports = new JCheckBox(ApplicationBundle.message("checkbox.add.unambiguous.imports.on.the.fly"));
    builder.addComponent(myCbShowImportPopup);
    builder.addComponent(myCbAddUnambiguousImports);

    myExcludePackagesList = new JBList();
    JComponent excludedPanel = new JPanel(new BorderLayout());
    excludedPanel.add(ToolbarDecorator.createDecorator(myExcludePackagesList).setAddAction(new AddImportExclusionAction()).disableUpDownActions().createPanel(),
                      BorderLayout.CENTER);
    excludedPanel.setBorder(IdeBorderFactory.createTitledBorder(ApplicationBundle.message("exclude.from.completion.group"), true));
    if (!myIsDefaultProject) {
      builder.addComponent(excludedPanel);
    }

    JPanel result = new JPanel(new BorderLayout());
    result.add(builder.getPanel(), BorderLayout.NORTH);
    return result;
  }

  public void focusList() {
    myExcludePackagesList.setSelectedIndex(0);
    myExcludePackagesList.requestFocus();
  }

  private String[] getExcludedPackages() {
    String[] excludedPackages = new String[myExcludePackagesModel.size()];
    for (int i = 0; i < myExcludePackagesModel.size(); i++) {
      excludedPackages[i] = (String)myExcludePackagesModel.elementAt(i);
    }
    Arrays.sort(excludedPackages);
    return excludedPackages;
  }

  @RequiredUIAccess
  @Override
  public boolean isModified() {
    return myCodeInsightSettings.isShowImportPopup() != myCbShowImportPopup.isSelected() ||
           myCodeInsightSettings.isAddUnambiguousImportsOnTheFly() != myCbAddUnambiguousImports.isSelected() ||
           !Arrays.equals(getExcludedPackages(), myExcludedSettings.getExcludedPackages());
  }

  @RequiredUIAccess
  @Override
  public void apply() throws ConfigurationException {
    myCodeInsightSettings.setShowImportPopup(myCbShowImportPopup.isSelected());
    myCodeInsightSettings.setAddUnambiguousImportsOnTheFly(myCbAddUnambiguousImports.isSelected());
    myExcludedSettings.setExcludedPackages(getExcludedPackages());
  }

  @RequiredUIAccess
  @Override
  public void reset() {
    myCbShowImportPopup.setSelected(myCodeInsightSettings.isShowImportPopup());
    myCbAddUnambiguousImports.setSelected(myCodeInsightSettings.isAddUnambiguousImportsOnTheFly());

    myExcludePackagesModel = new DefaultListModel();
    for (String name : myExcludedSettings.getExcludedPackages()) {
      myExcludePackagesModel.add(myExcludePackagesModel.size(), name);
    }
    myExcludePackagesList.setModel(myExcludePackagesModel);
  }

  @Nonnull
  @Override
  public String getId() {
    return "go.autoimport";
  }

  @Nullable
  @Override
  public String getParentId() {
    return "editor.preferences.import";
  }

  @Nullable
  @Override
  public Runnable enableSearch(String option) {
    return null;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Go";
  }

  @RequiredUIAccess
  @Override
  public void disposeUIResources() {
    UIUtil.dispose(myCbShowImportPopup);
    UIUtil.dispose(myCbAddUnambiguousImports);
    UIUtil.dispose(myExcludePackagesList);
    myCbShowImportPopup = null;
    myCbAddUnambiguousImports = null;
    myExcludePackagesList = null;
    if (myExcludePackagesModel != null) {
      myExcludePackagesModel.removeAllElements();
    }
    myExcludePackagesModel = null;
  }

  private class AddImportExclusionAction implements AnActionButtonRunnable {
    @Override
    public void run(AnActionButton button) {
      String packageName =
              Messages.showInputDialog("Enter the import path to exclude from auto-import and completion:", "Exclude Import Path", Messages.getWarningIcon());
      addExcludedPackage(packageName);
    }

    private void addExcludedPackage(@Nullable String packageName) {
      if (StringUtil.isEmpty(packageName)) return;
      int index = -Arrays.binarySearch(myExcludePackagesModel.toArray(), packageName) - 1;
      if (index >= 0) {
        myExcludePackagesModel.add(index, packageName);
        ScrollingUtil.ensureIndexIsVisible(myExcludePackagesList, index, 0);
      }
      myExcludePackagesList.clearSelection();
      myExcludePackagesList.setSelectedValue(packageName, true);
      myExcludePackagesList.requestFocus();
    }
  }
}

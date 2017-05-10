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

package consulo.google.go.newProjectOrModule;

import com.goide.sdk.GoSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Conditions;
import consulo.roots.ui.configuration.SdkComboBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoNewModuleBuilderPanel extends JPanel {
  private SdkComboBox myComboBox;

  public GoNewModuleBuilderPanel() {
    super(new VerticalFlowLayout());

    ProjectSdksModel model = new ProjectSdksModel();
    model.reset();

    myComboBox = new SdkComboBox(model, Conditions.equalTo(GoSdkType.getInstance()), false);

    add(LabeledComponent.left(myComboBox, "Sdk"));
  }

  @Nullable
  public Sdk getSdk() {
    return myComboBox.getSelectedSdk();
  }
}
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
import consulo.content.bundle.SdkTable;
import consulo.ide.newModule.ui.ProjectOrModuleNameStep;
import consulo.module.ui.awt.SdkComboBox;
import consulo.ui.ex.awt.LabeledComponent;
import consulo.util.lang.function.Conditions;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoNewModuleSetupStep extends ProjectOrModuleNameStep<GoNewModuleContext> {
  private final SdkComboBox myComboBox;

  public GoNewModuleSetupStep(GoNewModuleContext context) {
    super(context);

    myComboBox = new SdkComboBox(SdkTable.getInstance(), Conditions.equalTo(GoSdkType.getInstance()), false);

    myAdditionalContentPanel.add(LabeledComponent.create(myComboBox, "SDK"), BorderLayout.NORTH);
  }

  @Override
  public void onStepLeave(@Nonnull GoNewModuleContext context) {
    super.onStepLeave(context);

    context.setSdk(myComboBox.getSelectedSdk());
  }
}
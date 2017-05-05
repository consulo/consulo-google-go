package consulo.googe.go.newProjectOrModule;

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
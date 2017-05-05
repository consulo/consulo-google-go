package consulo.googe.go.module.extension.ui;

import com.goide.configuration.GoBuildTagsUI;
import com.goide.configuration.GoVendoringUI;
import com.intellij.openapi.ui.VerticalFlowLayout;
import consulo.annotations.RequiredDispatchThread;
import consulo.extension.ui.ModuleExtensionSdkBoxBuilder;
import consulo.googe.go.module.extension.GoMutableModuleExtension;

import javax.swing.*;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoModuleExtensionPanel extends JPanel {
  @RequiredDispatchThread
  public GoModuleExtensionPanel(GoMutableModuleExtension extension, Runnable runnable) {
    super(new VerticalFlowLayout(true, false));

    GoBuildTagsUI buildTagsUI = new GoBuildTagsUI(extension);
    GoVendoringUI vendoringUI = new GoVendoringUI(extension);

    ModuleExtensionSdkBoxBuilder builder = ModuleExtensionSdkBoxBuilder.createAndDefine(extension, runnable);
    builder.postConsumer((oldSdk, newSdk) -> {
      buildTagsUI.sdkChanged();
      vendoringUI.sdkChanged();
    });
    add(builder.build());

    add(buildTagsUI.getPanel());
    add(vendoringUI.getPanel());
  }
}

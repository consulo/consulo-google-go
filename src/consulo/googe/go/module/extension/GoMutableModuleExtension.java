package consulo.googe.go.module.extension;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.VerticalFlowLayout;
import consulo.annotations.RequiredDispatchThread;
import consulo.extension.ui.ModuleExtensionSdkBoxBuilder;
import consulo.module.extension.MutableModuleExtensionWithSdk;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.roots.ModuleRootLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author VISTALL
 * @since 12:44/30.05.13
 */
public class GoMutableModuleExtension extends GoModuleExtension implements MutableModuleExtensionWithSdk<GoModuleExtension> {
  public GoMutableModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module) {
    super(id, module);
  }

  @NotNull
  @Override
  public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk() {
    return (MutableModuleInheritableNamedPointer<Sdk>)super.getInheritableSdk();
  }

  @RequiredDispatchThread
  @Nullable
  @Override
  public JComponent createConfigurablePanel(@Nullable Runnable runnable) {
    JPanel panel = new JPanel(new VerticalFlowLayout(true, false));
    panel.add(ModuleExtensionSdkBoxBuilder.createAndDefine(this, runnable).build());
    return panel;
  }

  @Override
  public void setEnabled(boolean b) {
    myIsEnabled = b;
  }

  @Override
  public boolean isModified(@NotNull GoModuleExtension extension) {
    return isModifiedImpl(extension);
  }
}
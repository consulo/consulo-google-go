package consulo.googe.go.module.extension;

import com.goide.sdk.GoSdkType;
import com.intellij.openapi.projectRoots.SdkType;
import consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import consulo.roots.ModuleRootLayer;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 12:42/30.05.13
 */
public class GoModuleExtension extends ModuleExtensionWithSdkImpl<GoModuleExtension> {
  public GoModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module) {
    super(id, module);
  }

  @NotNull
  @Override
  public Class<? extends SdkType> getSdkTypeClass() {
    return GoSdkType.class;
  }
}
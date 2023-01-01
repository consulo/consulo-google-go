package consulo.google.go.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.icon.GoogleGoIconGroup;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 06-Aug-22
 */
@ExtensionImpl
public class GoModuleExtensionProvider implements ModuleExtensionProvider<GoModuleExtension> {
  @Nonnull
  @Override
  public String getId() {
    return "google-go";
  }

  @Nonnull
  @Override
  public LocalizeValue getName() {
    return LocalizeValue.localizeTODO("Go");
  }

  @Nonnull
  @Override
  public Image getIcon() {
    return GoogleGoIconGroup.gomodule();
  }

  @Nonnull
  @Override
  public ModuleExtension<GoModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer) {
    return new GoModuleExtension(getId(), moduleRootLayer);
  }

  @Nonnull
  @Override
  public MutableModuleExtension<GoModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer) {
    return new GoMutableModuleExtension(getId(), moduleRootLayer);
  }
}

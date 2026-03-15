package consulo.google.go.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.google.go.icon.GoogleGoIconGroup;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;


/**
 * @author VISTALL
 * @since 06-Aug-22
 */
@ExtensionImpl
public class GoModuleExtensionProvider implements ModuleExtensionProvider<GoModuleExtension> {
  @Override
  public String getId() {
    return "google-go";
  }

  @Override
  public LocalizeValue getName() {
    return LocalizeValue.localizeTODO("Go");
  }

  @Override
  public Image getIcon() {
    return GoogleGoIconGroup.go();
  }

  @Override
  public ModuleExtension<GoModuleExtension> createImmutableExtension(ModuleRootLayer moduleRootLayer) {
    return new GoModuleExtension(getId(), moduleRootLayer);
  }

  @Override
  public MutableModuleExtension<GoModuleExtension> createMutableExtension(ModuleRootLayer moduleRootLayer) {
    return new GoMutableModuleExtension(getId(), moduleRootLayer);
  }
}

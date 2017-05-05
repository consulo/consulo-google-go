package consulo.googe.go.module.extension;

import com.goide.project.GoBuildTargetSettings;
import com.goide.sdk.GoSdkType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.util.ThreeState;
import com.intellij.util.xmlb.XmlSerializer;
import consulo.annotations.RequiredReadAction;
import consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import consulo.roots.ModuleRootLayer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 12:42/30.05.13
 */
public class GoModuleExtension extends ModuleExtensionWithSdkImpl<GoModuleExtension> {
  @NotNull
  public static ThreeState getVendoringEnabled(@Nullable Module module) {
    if (module == null) {
      return ThreeState.UNSURE;
    }
    GoModuleExtension extension = ModuleUtilCore.getExtension(module, GoModuleExtension.class);
    return extension == null ? ThreeState.UNSURE : extension.getVendoringEnabled();
  }

  protected GoBuildTargetSettings myBuildTargetSettings = new GoBuildTargetSettings();
  protected ThreeState myVendoringEnabled = ThreeState.UNSURE;

  public GoModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module) {
    super(id, module);
  }

  @NotNull
  public ThreeState getVendoringEnabled() {
    return myVendoringEnabled;
  }

  @NotNull
  public GoBuildTargetSettings getBuildTargetSettings() {
    return myBuildTargetSettings;
  }

  @RequiredReadAction
  public void commit(@NotNull GoModuleExtension extension) {
    super.commit(extension);

    myVendoringEnabled = extension.myVendoringEnabled;
    myBuildTargetSettings = extension.myBuildTargetSettings;
  }

  @RequiredReadAction
  @Override
  protected void loadStateImpl(@NotNull Element element) {
    super.loadStateImpl(element);

    Element buildTags = element.getChild("buildTags");
    if (buildTags != null) {
      myBuildTargetSettings = XmlSerializer.deserialize(buildTags, GoBuildTargetSettings.class);
    }
  }

  @Override
  protected void getStateImpl(@NotNull Element element) {
    super.getStateImpl(element);
    Element buildElement = XmlSerializer.serialize(myBuildTargetSettings);
    if (buildElement != null) {
      element.addContent(buildElement);
    }
  }

  @NotNull
  @Override
  public Class<? extends SdkType> getSdkTypeClass() {
    return GoSdkType.class;
  }
}
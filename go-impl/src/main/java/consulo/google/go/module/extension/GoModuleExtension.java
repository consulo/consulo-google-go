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

package consulo.google.go.module.extension;

import com.goide.project.GoBuildTargetSettings;
import com.goide.sdk.GoSdkType;
import consulo.annotation.access.RequiredReadAction;
import consulo.content.bundle.SdkType;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionWithSdkBase;
import consulo.util.lang.ThreeState;
import consulo.util.xml.serializer.XmlSerializer;
import org.jdom.Element;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 12:42/30.05.13
 */
public class GoModuleExtension extends ModuleExtensionWithSdkBase<GoModuleExtension> {
  @Nonnull
  public static ThreeState getVendoringEnabled(@Nullable Module module) {
    if (module == null) {
      return ThreeState.UNSURE;
    }
    GoModuleExtension extension = ModuleUtilCore.getExtension(module, GoModuleExtension.class);
    return extension == null ? ThreeState.UNSURE : extension.getVendoringEnabled();
  }

  protected GoBuildTargetSettings myBuildTargetSettings = new GoBuildTargetSettings();
  protected ThreeState myVendoringEnabled = ThreeState.UNSURE;

  public GoModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer module) {
    super(id, module);
  }

  @Nonnull
  public ThreeState getVendoringEnabled() {
    return myVendoringEnabled;
  }

  @Nonnull
  public GoBuildTargetSettings getBuildTargetSettings() {
    return myBuildTargetSettings;
  }

  @RequiredReadAction
  public void commit(@Nonnull GoModuleExtension extension) {
    super.commit(extension);

    myVendoringEnabled = extension.myVendoringEnabled;
    myBuildTargetSettings = extension.myBuildTargetSettings.clone();
  }

  @RequiredReadAction
  @Override
  protected void loadStateImpl(@Nonnull Element element) {
    super.loadStateImpl(element);
    myVendoringEnabled = ThreeState.valueOf(element.getAttributeValue("vendoring-enabled", ThreeState.UNSURE.name()));

    Element buildTags = element.getChild("buildTags");
    if (buildTags != null) {
      myBuildTargetSettings = XmlSerializer.deserialize(buildTags, GoBuildTargetSettings.class);
    }
  }

  @Override
  protected void getStateImpl(@Nonnull Element element) {
    super.getStateImpl(element);
    if (myVendoringEnabled != ThreeState.UNSURE) {
      element.setAttribute("vendoring-enabled", myVendoringEnabled.name());
    }
    Element buildElement = XmlSerializer.serialize(myBuildTargetSettings);
    if (buildElement != null) {
      element.addContent(buildElement);
    }
  }

  @Nonnull
  @Override
  public Class<? extends SdkType> getSdkTypeClass() {
    return GoSdkType.class;
  }
}
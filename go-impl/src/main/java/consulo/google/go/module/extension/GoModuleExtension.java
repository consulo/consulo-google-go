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
    myBuildTargetSettings = extension.myBuildTargetSettings.clone();
  }

  @RequiredReadAction
  @Override
  protected void loadStateImpl(@NotNull Element element) {
    super.loadStateImpl(element);
    myVendoringEnabled = ThreeState.valueOf(element.getAttributeValue("vendoring-enabled", ThreeState.UNSURE.name()));

    Element buildTags = element.getChild("buildTags");
    if (buildTags != null) {
      myBuildTargetSettings = XmlSerializer.deserialize(buildTags, GoBuildTargetSettings.class);
    }
  }

  @Override
  protected void getStateImpl(@NotNull Element element) {
    super.getStateImpl(element);
    if (myVendoringEnabled != ThreeState.UNSURE) {
      element.setAttribute("vendoring-enabled", myVendoringEnabled.name());
    }
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
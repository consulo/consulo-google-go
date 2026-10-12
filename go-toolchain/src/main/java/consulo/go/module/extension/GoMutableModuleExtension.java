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

package consulo.go.module.extension;

import consulo.content.bundle.Sdk;
import consulo.disposer.Disposable;
import consulo.go.module.extension.ui.GoModuleExtensionPanel;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.MutableModuleExtensionWithSdk;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.ThreeState;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 12:44/30.05.13
 */
public class GoMutableModuleExtension extends GoModuleExtension implements MutableModuleExtensionWithSdk<GoModuleExtension> {
  public GoMutableModuleExtension(String id, ModuleRootLayer module) {
    super(id, module);
  }

  public void setVendoringEnabled(ThreeState enabled) {
    myVendoringEnabled = enabled;
  }

  @Override
  public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk() {
    return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
  }

  @RequiredUIAccess
  @Nullable
  @Override
  public Component createConfigurationComponent(Disposable disposable, Runnable runnable) {
    return GoModuleExtensionPanel.create(this, disposable, runnable);
  }

  @Override
  public void setEnabled(boolean b) {
    myIsEnabled = b;
  }

  @Override
  public boolean isModified(GoModuleExtension extension) {
    return isModifiedImpl(extension) ||
        myVendoringEnabled != extension.myVendoringEnabled ||
        !myBuildTargetSettings.equals(extension.myBuildTargetSettings);
  }
}
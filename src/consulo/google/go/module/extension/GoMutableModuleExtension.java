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

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.ThreeState;
import consulo.annotations.RequiredDispatchThread;
import consulo.google.go.module.extension.ui.GoModuleExtensionPanel;
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

  public void setVendoringEnabled(@NotNull ThreeState enabled) {
    myVendoringEnabled = enabled;
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
    return new GoModuleExtensionPanel(this, runnable);
  }

  @Override
  public void setEnabled(boolean b) {
    myIsEnabled = b;
  }

  @Override
  public boolean isModified(@NotNull GoModuleExtension extension) {
    return isModifiedImpl(extension) ||
           myVendoringEnabled != extension.myVendoringEnabled ||
           !myBuildTargetSettings.equals(extension.myBuildTargetSettings);
  }
}
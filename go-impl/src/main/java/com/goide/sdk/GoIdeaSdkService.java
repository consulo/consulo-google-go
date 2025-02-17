/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.sdk;

import consulo.annotation.component.ServiceImpl;
import consulo.content.bundle.Sdk;
import consulo.google.go.module.extension.GoModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ServiceImpl
public class GoIdeaSdkService extends GoSdkService {
  @Inject
  public GoIdeaSdkService(@Nonnull Project project) {
    super(project);
  }

  @Override
  public String getSdkHomePath(@Nullable Module module) {
    if (module == null) {
      return null;
    }

    Sdk goSdk = getGoSdk(module);
    return goSdk == null ? null : goSdk.getHomePath();
  }

  @Nullable
  @Override
  public String getSdkVersion(@Nullable Module module) {
    String parentVersion = super.getSdkVersion(module);
    if (parentVersion != null) {
      return parentVersion;
    }

    if (module == null) {
      return null;
    }

    Sdk goSdk = getGoSdk(module);
    return goSdk == null ? null : goSdk.getVersionString();
  }

  @Override
  public boolean isGoModule(@Nullable Module module) {
    return super.isGoModule(module) && ModuleUtilCore.getExtension(module, GoModuleExtension.class) != null;
  }

  private Sdk getGoSdk(@Nullable Module module) {
    return module == null ? null : ModuleUtilCore.getSdk(module, GoModuleExtension.class);
  }
}

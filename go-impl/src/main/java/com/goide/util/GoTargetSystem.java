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

package com.goide.util;

import com.goide.project.GoBuildTargetSettings;
import com.goide.sdk.GoSdkService;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.google.go.module.extension.GoModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ProjectRootManager;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.ThreeState;
import org.jetbrains.annotations.Contract;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GoTargetSystem {
  private static final String GAE_BUILD_FLAG = "appengine";

  @Nonnull
  public final String os;
  @Nonnull
  public final String arch;
  @Nullable
  public final String goVersion;
  @Nullable
  public final String compiler;

  @Nonnull
  public final ThreeState cgoEnabled;
  private final Set<String> customFlags = new HashSet<>();

  public GoTargetSystem(@Nonnull String os,
                        @Nonnull String arch,
                        @Nullable String goVersion,
                        @Nullable String compiler,
                        @Nonnull ThreeState cgoEnabled,
                        @Nonnull String... customFlags) {
    this.os = os;
    this.arch = arch;
    this.goVersion = goVersion;
    this.compiler = compiler;
    this.cgoEnabled = cgoEnabled;
    Collections.addAll(this.customFlags, customFlags);
  }

  public boolean supportsFlag(@Nonnull String flag) {
    return customFlags.contains(flag);
  }

  @Nullable
  public static GoTargetSystem forModule(@Nonnull Module module) {
    return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> {
      GoModuleExtension goModuleExtension = ModuleUtilCore.getExtension(module, GoModuleExtension.class);
      if (goModuleExtension == null) {
        return CachedValueProvider.Result.create(null, ProjectRootManager.getInstance(module.getProject()));
      }
      GoBuildTargetSettings settings = goModuleExtension.getBuildTargetSettings();
      String os = realValue(settings.os, GoUtil.systemOS());
      String arch = realValue(settings.arch, GoUtil.systemArch());
      ThreeState cgo = settings.cgo == ThreeState.UNSURE ? GoUtil.systemCgo(os, arch) : settings.cgo;
      String moduleSdkVersion = GoSdkService.getInstance(module.getProject()).getSdkVersion(module);
      String[] customFlags = GoSdkService.getInstance(module.getProject()).isAppEngineSdk(module)
                             ? ArrayUtil.prepend(GAE_BUILD_FLAG, settings.customFlags)
                             : settings.customFlags;
      String compiler = GoBuildTargetSettings.ANY_COMPILER.equals(settings.compiler) ? null : settings.compiler;
      GoTargetSystem result = new GoTargetSystem(os, arch, realValue(settings.goVersion, moduleSdkVersion), compiler, cgo, customFlags);
      return CachedValueProvider.Result.create(result, ProjectRootManager.getInstance(module.getProject()));
    });
  }

  @Contract("_,null->!null")
  private static String realValue(@Nonnull String value, @Nullable String defaultValue) {
    return GoBuildTargetSettings.DEFAULT.equals(value) ? defaultValue : value;
  }
}

/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.project;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.component.util.SimpleModificationTracker;
import consulo.ide.ServiceManager;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.util.io.FileUtil;
import consulo.util.xml.serializer.XmlSerializerUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@State(
    name = "GoExcludedPaths",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/goExcludedPaths.xml")
    }
)
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class GoExcludedPathsSettings extends SimpleModificationTracker implements PersistentStateComponent<GoExcludedPathsSettings> {
  private String[] myExcludedPackages = ArrayUtil.EMPTY_STRING_ARRAY;

  public static GoExcludedPathsSettings getInstance(Project project) {
    return ServiceManager.getService(project, GoExcludedPathsSettings.class);
  }

  @Nullable
  @Override
  public GoExcludedPathsSettings getState() {
    return this;
  }

  @Override
  public void loadState(GoExcludedPathsSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public String[] getExcludedPackages() {
    return myExcludedPackages;
  }

  public void setExcludedPackages(String... excludedPackages) {
    myExcludedPackages = excludedPackages;
    incModificationCount();
  }

  public boolean isExcluded(@Nullable String importPath) {
    if (importPath == null) {
      return false;
    }
    for (String excludedPath : myExcludedPackages) {
      if (FileUtil.isAncestor(excludedPath, importPath, false)) return true;
    }
    return false;
  }

  public void excludePath(@Nonnull String importPath) {
    setExcludedPackages(ArrayUtil.append(myExcludedPackages, importPath));
  }
}

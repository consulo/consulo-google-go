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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.goide.GoIcons;
import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryKind;
import com.intellij.openapi.roots.libraries.LibraryPresentationProvider;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.ui.image.Image;

public class GoSdkLibraryPresentationProvider extends LibraryPresentationProvider<DummyLibraryProperties> {
  private static final LibraryKind KIND = LibraryKind.create("go");

  public GoSdkLibraryPresentationProvider() {
    super(KIND);
  }

  @Override
  @Nullable
  public Image getIcon() {
    return GoIcons.ICON;
  }

  @Override
  @Nullable
  public DummyLibraryProperties detect(@Nonnull List<VirtualFile> classesRoots) {
    for (VirtualFile root : classesRoots) {
      if (GoSdkService.isGoSdkLibRoot(root) && !GoSdkService.isAppEngineSdkPath(GoSdkService.libraryRootToSdkPath(root))) {
        return DummyLibraryProperties.INSTANCE;
      }
    }
    return null;
  }
}
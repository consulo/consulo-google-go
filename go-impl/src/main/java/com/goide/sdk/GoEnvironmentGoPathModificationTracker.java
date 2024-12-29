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

import com.goide.GoEnvironmentUtil;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.ide.ServiceManager;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.SystemProperties;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.event.VirtualFileAdapter;
import consulo.virtualFileSystem.event.VirtualFileCopyEvent;
import consulo.virtualFileSystem.event.VirtualFileEvent;
import consulo.virtualFileSystem.event.VirtualFileMoveEvent;

import jakarta.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class GoEnvironmentGoPathModificationTracker {
  private final Set<String> pathsToTrack = new HashSet<>();
  private final Collection<VirtualFile> goPathRoots = new HashSet<>();

  public GoEnvironmentGoPathModificationTracker() {
    String goPath = GoEnvironmentUtil.retrieveGoPathFromEnvironment();
    if (goPath != null) {
      String home = SystemProperties.getUserHome();
      for (String s : StringUtil.split(goPath, File.pathSeparator)) {
        if (s.contains("$HOME")) {
          if (home == null) {
            continue;
          }
          s = s.replaceAll("\\$HOME", home);
        }
        pathsToTrack.add(s);
      }
    }
    recalculateFiles();

    VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
      @Override
      public void fileCreated(@Nonnull VirtualFileEvent event) {
        handleEvent(event);
      }

      @Override
      public void fileDeleted(@Nonnull VirtualFileEvent event) {
        handleEvent(event);
      }

      @Override
      public void fileMoved(@Nonnull VirtualFileMoveEvent event) {
        handleEvent(event);
      }

      @Override
      public void fileCopied(@Nonnull VirtualFileCopyEvent event) {
        handleEvent(event);
      }

      private void handleEvent(VirtualFileEvent event) {
        if (pathsToTrack.contains(event.getFile().getPath())) {
          recalculateFiles();
        }
      }
    });
  }

  private void recalculateFiles() {
    Collection<VirtualFile> result = new HashSet<>();
    for (String path : pathsToTrack) {
      ContainerUtil.addIfNotNull(result, LocalFileSystem.getInstance().findFileByPath(path));
    }
    updateGoPathRoots(result);
  }

  private synchronized void updateGoPathRoots(Collection<VirtualFile> newRoots) {
    goPathRoots.clear();
    goPathRoots.addAll(newRoots);
  }

  private synchronized Collection<VirtualFile> getGoPathRoots() {
    return goPathRoots;
  }

  public static Collection<VirtualFile> getGoEnvironmentGoPathRoots() {
    return ServiceManager.getService(GoEnvironmentGoPathModificationTracker.class).getGoPathRoots();
  }
}

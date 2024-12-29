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

package com.goide.appengine;

import com.goide.util.GoUtil;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.ApplicationManager;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.application.util.function.Computable;
import consulo.component.util.SimpleModificationTracker;
import consulo.ide.ServiceManager;
import consulo.language.psi.search.FilenameIndex;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.dataholder.UserDataHolder;
import consulo.util.io.PathUtil;
import consulo.util.lang.ObjectUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.event.VirtualFileAdapter;
import consulo.virtualFileSystem.event.VirtualFileCopyEvent;
import consulo.virtualFileSystem.event.VirtualFileEvent;
import consulo.virtualFileSystem.event.VirtualFileMoveEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;

@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class YamlFilesModificationTracker extends SimpleModificationTracker {
  @Inject
  public YamlFilesModificationTracker(@Nonnull Project project) {
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

      private void handleEvent(@Nonnull VirtualFileEvent event) {
        if ("yaml".equals(PathUtil.getFileExtension(event.getFileName()))) {
          incModificationCount();
        }
      }
    }, project);
  }

  public static YamlFilesModificationTracker getInstance(@Nonnull Project project) {
    return ServiceManager.getService(project, YamlFilesModificationTracker.class);
  }

  @Nonnull
  public static Collection<VirtualFile> getYamlFiles(@Nonnull Project project, @Nullable Module module) {
    UserDataHolder dataHolder = ObjectUtil.notNull(module, project);
    return CachedValuesManager.getManager(project).getCachedValue(dataHolder, () -> {
      Collection<VirtualFile> yamlFiles = ApplicationManager.getApplication().runReadAction(new Computable<Collection<VirtualFile>>() {
        @Override
        public Collection<VirtualFile> compute() {
          return FilenameIndex.getAllFilesByExt(project, "yaml", GoUtil.moduleScopeWithoutLibraries(project, module));
        }
      });
      return CachedValueProvider.Result.create(yamlFiles, getInstance(project));
    });
  }
}

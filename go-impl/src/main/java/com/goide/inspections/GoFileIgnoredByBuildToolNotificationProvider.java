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

package com.goide.inspections;

import com.goide.GoFileType;
import com.goide.util.GoUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationPropertiesComponent;
import consulo.application.dumb.DumbAware;
import consulo.component.messagebus.MessageBusConnection;
import consulo.fileEditor.*;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.language.inject.InjectedLanguageManagerUtil;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.event.BulkFileListener;
import consulo.virtualFileSystem.event.VFileEvent;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@ExtensionImpl
public class GoFileIgnoredByBuildToolNotificationProvider implements EditorNotificationProvider, DumbAware {
  private static final String DO_NOT_SHOW_NOTIFICATION_ABOUT_IGNORE_BY_BUILD_TOOL = "DO_NOT_SHOW_NOTIFICATION_ABOUT_IGNORE_BY_BUILD_TOOL";

  private final Project myProject;

  @Inject
  public GoFileIgnoredByBuildToolNotificationProvider(@Nonnull Project project,
                                                      @Nonnull EditorNotifications notifications,
                                                      @Nonnull FileEditorManager fileEditorManager) {
    myProject = project;
    MessageBusConnection connection = myProject.getMessageBus().connect(myProject);
    connection.subscribe(BulkFileListener.class, new BulkFileListener.Adapter() {
      @Override
      public void after(@Nonnull List<? extends VFileEvent> events) {
        if (!myProject.isDisposed()) {
          Set<VirtualFile> openFiles = Set.of(fileEditorManager.getSelectedFiles());
          for (VFileEvent event : events) {
            VirtualFile file = event.getFile();
            if (file != null && openFiles.contains(file)) {
              notifications.updateNotifications(file);
            }
          }
        }
      }
    });
  }

  @Nonnull
  @Override
  public String getId() {
    return "go-file-ignored-by-build-tool";
  }

  @RequiredReadAction
  @Nullable
  @Override
  public EditorNotificationBuilder buildNotification(@Nonnull VirtualFile file, @Nonnull FileEditor fileEditor, @Nonnull Supplier<EditorNotificationBuilder> factory) {
    if (file.getFileType() == GoFileType.INSTANCE) {
      PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
      if (InjectedLanguageManagerUtil.findInjectionHost(psiFile) != null) {
        return null;
      }
      Module module = psiFile != null ? ModuleUtilCore.findModuleForPsiElement(psiFile) : null;
      if (GoUtil.fileToIgnore(file.getName())) {
        if (!ApplicationPropertiesComponent.getInstance().getBoolean(DO_NOT_SHOW_NOTIFICATION_ABOUT_IGNORE_BY_BUILD_TOOL, false)) {
          return createIgnoredByBuildToolPanel(myProject, file, factory.get());
        }
      }
      else if (module != null && !GoUtil.matchedForModuleBuildTarget(psiFile, module)) {
        return createMismatchedTargetPanel(module, file, factory.get());
      }
    }
    return null;
  }

  private static EditorNotificationBuilder createIgnoredByBuildToolPanel(@Nonnull Project project, @Nonnull VirtualFile file, EditorNotificationBuilder builder) {
    String fileName = file.getName();
    builder.withText(LocalizeValue.localizeTODO("'" + fileName + "' will be ignored by build tool since its name starts with '" + fileName.charAt(0) + "'"));
    builder.withAction(LocalizeValue.localizeTODO("Do not show again"), () -> {
      ApplicationPropertiesComponent.getInstance().setValue(DO_NOT_SHOW_NOTIFICATION_ABOUT_IGNORE_BY_BUILD_TOOL, true);
      EditorNotifications.getInstance(project).updateAllNotifications();
    });
    return builder;
  }

  @Nonnull
  private static EditorNotificationBuilder createMismatchedTargetPanel(@Nonnull Module module, @Nonnull VirtualFile file, EditorNotificationBuilder builder) {
    builder.withText(LocalizeValue.localizeTODO("'" + file.getName() + "' doesn't match to target system. File will be ignored by build tool"));
    builder.withAction(LocalizeValue.localizeTODO("Edit Go project settings"), () -> {
      ShowSettingsUtil.getInstance().showProjectStructureDialog(module.getProject(), s -> s.select(module, true));
    });
    return builder;
  }
}
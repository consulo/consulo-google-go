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
import com.goide.GoLanguage;
import com.goide.sdk.GoSdkService;
import com.goide.sdk.GoSdkUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationPropertiesComponent;
import consulo.application.dumb.DumbAware;
import consulo.fileEditor.EditorNotificationBuilder;
import consulo.fileEditor.EditorNotificationProvider;
import consulo.fileEditor.EditorNotifications;
import consulo.fileEditor.FileEditor;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.project.Project;
import consulo.project.ProjectBundle;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@ExtensionImpl
public class WrongSdkConfigurationNotificationProvider implements EditorNotificationProvider, DumbAware {
  private static final String DO_NOT_SHOW_NOTIFICATION_ABOUT_EMPTY_GOPATH = "DO_NOT_SHOW_NOTIFICATION_ABOUT_EMPTY_GOPATH";

  private final Project myProject;

  @Inject
  public WrongSdkConfigurationNotificationProvider(@Nonnull Project project) {
    myProject = project;
  }

  @Nonnull
  @Override
  public String getId() {
    return "go-wrong-sdk-configuration";
  }

  @RequiredReadAction
  @Nullable
  @Override
  public EditorNotificationBuilder buildNotification(@Nonnull VirtualFile file, @Nonnull FileEditor fileEditor, @Nonnull Supplier<EditorNotificationBuilder> builderFactory) {
    if (file.getFileType() != GoFileType.INSTANCE) return null;

    PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
    if (psiFile == null) return null;

    if (psiFile.getLanguage() != GoLanguage.INSTANCE) return null;

    Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
    if (module == null) return null;

    String sdkHomePath = GoSdkService.getInstance(myProject).getSdkHomePath(module);
    if (StringUtil.isEmpty(sdkHomePath)) {
      return createMissingSdkPanel(myProject, module, builderFactory.get());
    }

    if (!ApplicationPropertiesComponent.getInstance().getBoolean(DO_NOT_SHOW_NOTIFICATION_ABOUT_EMPTY_GOPATH, false)) {
      String goPath = GoSdkUtil.retrieveGoPath(myProject, module);
      if (StringUtil.isEmpty(goPath.trim())) {
        return createEmptyGoPathPanel(myProject, module, builderFactory.get());
      }
    }

    return null;
  }

  @Nonnull
  private static EditorNotificationBuilder createMissingSdkPanel(@Nonnull Project project, @Nonnull Module module, EditorNotificationBuilder builder) {
    builder.withText(LocalizeValue.localizeTODO(ProjectBundle.message("module.sdk.not.defined")));
    builder.withAction(LocalizeValue.localizeTODO(ProjectBundle.message("module.sdk.setup")), () -> ShowSettingsUtil.getInstance().showProjectStructureDialog(project, s -> s.select(module, true)));
    return builder;
  }

  @Nonnull
  private static EditorNotificationBuilder createEmptyGoPathPanel(@Nonnull Project project, Module module, EditorNotificationBuilder builder) {
    builder.withText(LocalizeValue.localizeTODO("GOPATH is empty"));
    builder.withAction(LocalizeValue.localizeTODO("Configure Go Libraries"), () -> {
      ShowSettingsUtil.getInstance().showProjectStructureDialog(project, s -> s.select(module, true));
    });
    builder.withAction(LocalizeValue.localizeTODO("Do not show again"), () -> {
      ApplicationPropertiesComponent.getInstance().setValue(DO_NOT_SHOW_NOTIFICATION_ABOUT_EMPTY_GOPATH, true);
      EditorNotifications.getInstance(project).updateAllNotifications();
    });
    return builder;
  }
}
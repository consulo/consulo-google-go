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
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.fileEditor.EditorNotificationBuilder;
import consulo.fileEditor.EditorNotificationProvider;
import consulo.fileEditor.FileEditor;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.project.Project;
import consulo.project.localize.ProjectLocalize;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.util.function.Supplier;

@ExtensionImpl
public class WrongSdkConfigurationNotificationProvider implements EditorNotificationProvider, DumbAware {
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

    Module module = psiFile.getModule();
    if (module == null) return null;

    String sdkHomePath = GoSdkService.getInstance(myProject).getSdkHomePath(module);
    if (StringUtil.isEmpty(sdkHomePath)) {
      return createMissingSdkPanel(myProject, module, builderFactory.get());
    }

    return null;
  }

  @Nonnull
  private static EditorNotificationBuilder createMissingSdkPanel(@Nonnull Project project, @Nonnull Module module, EditorNotificationBuilder builder) {
    builder.withText(ProjectLocalize.moduleSdkNotDefined());
    builder.withAction(ProjectLocalize.moduleSdkSetup(), (e) -> ShowSettingsUtil.getInstance().showProjectStructureDialog(project, s -> s.select(module, true)));
    return builder;
  }
}
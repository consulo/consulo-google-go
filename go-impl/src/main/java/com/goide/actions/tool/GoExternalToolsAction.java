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

package com.goide.actions.tool;

import com.goide.GoConstants;
import com.goide.GoFileType;
import com.goide.sdk.GoSdkService;
import com.goide.util.GoExecutor;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.language.editor.CommonDataKeys;
import consulo.language.util.ModuleUtilCore;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationType;
import consulo.project.ui.notification.Notifications;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.util.lang.ExceptionUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class GoExternalToolsAction extends DumbAwareAction {
  private static final Logger LOG = Logger.getInstance(GoExternalToolsAction.class);

  private static void error(@Nonnull String title, @Nonnull Project project, @Nullable Exception ex) {
    String message = ex == null ? "" : ExceptionUtil.getThrowableText(ex);
    NotificationType type = NotificationType.ERROR;
    Notifications.Bus.notify(GoConstants.GO_EXECUTION_NOTIFICATION_GROUP.createNotification(title, message, type, null), project);
  }

  @Override
  public void update(@Nonnull AnActionEvent e) {
    super.update(e);
    Project project = e.getData(Project.KEY);
    VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
    if (project == null || file == null || !file.isInLocalFileSystem() || !isAvailableOnFile(file)) {
      e.getPresentation().setEnabled(false);
      return;
    }
    Module module = ModuleUtilCore.findModuleForFile(file, project);
    e.getPresentation().setEnabled(GoSdkService.getInstance(project).isGoModule(module));
  }

  protected boolean isAvailableOnFile(VirtualFile file) {
    return file.getFileType() == GoFileType.INSTANCE;
  }

  @Override
  public void actionPerformed(@Nonnull AnActionEvent e) {
    Project project = e.getData(Project.KEY);
    VirtualFile file = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE);
    assert project != null;
    String title = StringUtil.notNullize(e.getPresentation().getText());

    Module module = ModuleUtilCore.findModuleForFile(file, project);
    try {
      doSomething(file, module, project, title);
    }
    catch (ExecutionException ex) {
      error(title, project, ex);
      LOG.error(ex);
    }
  }

  protected boolean doSomething(@Nonnull VirtualFile virtualFile,
                                @Nullable Module module,
                                @Nonnull Project project,
                                @Nonnull String title) throws ExecutionException {
    return doSomething(virtualFile, module, project, title, false);
  }

  private boolean doSomething(@Nonnull VirtualFile virtualFile,
                              @Nullable Module module,
                              @Nonnull Project project,
                              @Nonnull String title,
                              boolean withProgress) {
    //noinspection unchecked
    return doSomething(virtualFile, module, project, title, withProgress, c -> {});
  }

  protected boolean doSomething(@Nonnull VirtualFile virtualFile,
                                @Nullable Module module,
                                @Nonnull Project project,
                                @Nonnull String title,
                                boolean withProgress,
                                @Nonnull Consumer<Boolean> consumer) {
    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    if (document != null) {
      FileDocumentManager.getInstance().saveDocument(document);
    }
    else {
      FileDocumentManager.getInstance().saveAllDocuments();
    }

    createExecutor(project, module, title, virtualFile).executeWithProgress(withProgress, result -> {
      consumer.accept(result);
      VirtualFileUtil.markDirtyAndRefresh(true, true, true, virtualFile);
    });
    return true;
  }

  protected GoExecutor createExecutor(@Nonnull Project project,
                                      @Nullable Module module,
                                      @Nonnull String title,
                                      @Nonnull VirtualFile virtualFile) {
    String filePath = virtualFile.getCanonicalPath();
    assert filePath != null;
    return createExecutor(project, module, title, filePath);
  }

  @Nonnull
  protected abstract GoExecutor createExecutor(@Nonnull Project project,
                                               @Nullable Module module,
                                               @Nonnull String title,
                                               @Nonnull String filePath);
}

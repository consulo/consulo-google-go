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
import com.goide.codeInsight.imports.GoGetPackageFix;
import com.goide.sdk.GoSdkUtil;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.project.ui.notification.Notification;
import consulo.project.ui.notification.NotificationType;
import consulo.project.ui.notification.Notifications;
import consulo.project.ui.notification.event.NotificationListener;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import javax.swing.event.HyperlinkEvent;

public abstract class GoDownloadableFileAction extends GoExternalToolsAction {
    private static final String GO_GET_LINK = "goGetLink";
    @Nonnull
    private final String myGoGetImportPath;
    @Nonnull
    private final String myExecutableName;

    public GoDownloadableFileAction(@Nonnull String text,
                                    @Nonnull String description,
                                    @Nonnull String executableName,
                                    @Nonnull String goGetImportPath) {
        super(text, description);
        
        myExecutableName = executableName;
        myGoGetImportPath = goGetImportPath;
    }

    @Override
    protected boolean doSomething(@Nonnull VirtualFile virtualFile, @Nullable Module module, @Nonnull Project project, @Nonnull String title)
        throws ExecutionException {
        VirtualFile executable = getExecutable(project, module);
        if (executable == null) {
            String message = "Can't find `" + myExecutableName + "` in GOPATH. Try to invoke <a href=\"" + GO_GET_LINK + "\">go get " +
                myExecutableName + "</a>";
            NotificationListener listener = new MyNotificationListener(project, module);
            Notifications.Bus.notify(GoConstants.GO_NOTIFICATION_GROUP.createNotification(title, message, NotificationType.WARNING, listener),
                project);
            return false;
        }
        return super.doSomething(virtualFile, module, project, title);
    }

    @Nullable
    protected VirtualFile getExecutable(@Nonnull Project project, @Nullable Module module) {
        return GoSdkUtil.findExecutableInGoPath(myExecutableName, project, module);
    }

    private class MyNotificationListener implements NotificationListener {
        private final Project myProject;
        private final Module myModule;

        private MyNotificationListener(@Nonnull Project project, @Nullable Module module) {
            myProject = project;
            myModule = module;
        }

        @Override
        public void hyperlinkUpdate(@Nonnull Notification notification, @Nonnull HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String description = event.getDescription();
                if (GO_GET_LINK.equals(description)) {
                    GoGetPackageFix.applyFix(myProject, myModule, myGoGetImportPath, false);
                    notification.expire();
                }
            }
        }
    }
}

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

package com.goide.project;

import com.goide.GoConstants;
import com.goide.sdk.GoSdkService;
import com.goide.sdk.GoSdkUtil;
import com.goide.util.GoUtil;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.Application;
import consulo.application.ApplicationManager;
import consulo.application.progress.ProgressIndicatorProvider;
import consulo.component.PropertiesComponent;
import consulo.component.messagebus.MessageBusConnection;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.google.go.module.extension.GoModuleExtension;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.language.psi.PsiFileSystemItem;
import consulo.language.psi.search.FilenameIndex;
import consulo.module.Module;
import consulo.module.content.ProjectRootManager;
import consulo.module.content.layer.event.ModuleRootAdapter;
import consulo.module.content.layer.event.ModuleRootEvent;
import consulo.module.content.layer.event.ModuleRootListener;
import consulo.module.event.ModuleAdapter;
import consulo.module.event.ModuleListener;
import consulo.project.Project;
import consulo.project.ProjectPropertiesComponent;
import consulo.project.startup.StartupManager;
import consulo.project.ui.notification.Notification;
import consulo.project.ui.notification.NotificationType;
import consulo.project.ui.notification.Notifications;
import consulo.project.ui.notification.event.NotificationListener;
import consulo.ui.ex.awt.util.Alarm;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.ThreeState;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.event.VirtualFileEvent;
import consulo.virtualFileSystem.event.VirtualFileListener;
import jakarta.inject.Inject;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.swing.event.HyperlinkEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ServiceAPI(value = ComponentScope.MODULE, lazy = false)
@ServiceImpl
public class GoModuleLibrariesInitializer implements Disposable {
  private static final String GO_LIBRARIES_NOTIFICATION_HAD_BEEN_SHOWN = "go.libraries.notification.had.been.shown";
  private static final String GO_VENDORING_NOTIFICATION_HAD_BEEN_SHOWN = "go.vendoring.notification.had.been.shown";
  private static final int UPDATE_DELAY = 300;
  private static boolean isTestingMode;

  private final Alarm myAlarm;
  private final MessageBusConnection myConnection;
  private boolean myModuleInitialized;

  @Nonnull
  private final Set<VirtualFile> myLastHandledGoPathSourcesRoots = new HashSet<>();
  @Nonnull
  private final Set<VirtualFile> myLastHandledExclusions = new HashSet<>();
  @Nonnull
  private final Set<LocalFileSystem.WatchRequest> myWatchedRequests = new HashSet<>();

  @Nonnull
  private final Module myModule;
  @Nonnull
  private final VirtualFileListener myFilesListener = new VirtualFileListener() {
    @Override
    public void fileCreated(@Nonnull VirtualFileEvent event) {
      if (GoConstants.VENDOR.equals(event.getFileName()) && event.getFile().isDirectory()) {
        showVendoringNotification();
      }
    }
  };

  @TestOnly
  public static void setTestingMode(@Nonnull Disposable disposable) {
    isTestingMode = true;
    Disposer.register(disposable, () ->
    {
      //noinspection AssignmentToStaticFieldFromInstanceMethod
      isTestingMode = false;
    });
  }

  @Inject
  public GoModuleLibrariesInitializer(@Nonnull Application application, @Nonnull Module module) {
    myModule = module;
    myAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, myModule);
    myConnection = myModule.getMessageBus().connect();

    application.getMessageBus().connect(this).subscribe(ModuleListener.class, new ModuleAdapter() {
      @Override
      public void moduleAdded(Project project, Module module) {
        if (myModule == module) {
          GoModuleLibrariesInitializer.this.moduleAdded();
        }
      }
    });
  }

  private void moduleAdded() {
    if (!myModuleInitialized) {
      myConnection.subscribe(ModuleRootListener.class, new ModuleRootAdapter() {
        @Override
        public void rootsChanged(ModuleRootEvent event) {
          scheduleUpdate();
        }
      });

      Project project = myModule.getProject();
      StartupManager.getInstance(project).runWhenProjectIsInitialized(() ->
      {
        if (!project.isDisposed() && !myModule.isDisposed()) {
          for (PsiFileSystemItem vendor : FilenameIndex.getFilesByName(project, GoConstants.VENDOR, GoUtil.moduleScope(myModule), true)) {
            if (vendor.isDirectory()) {
              showVendoringNotification();
              break;
            }
          }
        }
      });

      VirtualFileManager.getInstance().addVirtualFileListener(myFilesListener);
    }
    scheduleUpdate(0);
    myModuleInitialized = true;
  }

  private void scheduleUpdate() {
    scheduleUpdate(UPDATE_DELAY);
  }

  private void scheduleUpdate(int delay) {
    myAlarm.cancelAllRequests();
    UpdateRequest updateRequest = new UpdateRequest();
    if (isTestingMode) {
      ApplicationManager.getApplication().invokeLater(updateRequest);
    } else {
      myAlarm.addRequest(updateRequest, delay);
    }
  }

  private void showVendoringNotification() {
    if (!myModuleInitialized || myModule.isDisposed()) {
      return;
    }
    Project project = myModule.getProject();
    String version = GoSdkService.getInstance(project).getSdkVersion(myModule);
    if (!GoVendoringUtil.supportsVendoring(version) || GoVendoringUtil.supportsVendoringByDefault(version)) {
      return;
    }
    if (GoModuleExtension.getVendoringEnabled(myModule) != ThreeState.UNSURE) {
      return;
    }

    PropertiesComponent propertiesComponent = ProjectPropertiesComponent.getInstance(project);
    boolean shownAlready;
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (propertiesComponent) {
      shownAlready = propertiesComponent.getBoolean(GO_VENDORING_NOTIFICATION_HAD_BEEN_SHOWN, false);
      if (!shownAlready) {
        propertiesComponent.setValue(GO_VENDORING_NOTIFICATION_HAD_BEEN_SHOWN, String.valueOf(true));
      }
    }

    if (!shownAlready) {
      NotificationListener.Adapter notificationListener = new NotificationListener.Adapter() {
        @Override
        protected void hyperlinkActivated(@Nonnull Notification notification, @Nonnull HyperlinkEvent event) {
          if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED && "configure".equals(event.getDescription())) {
            ShowSettingsUtil.getInstance().showProjectStructureDialog(project);
          }
        }
      };
      Notification notification = GoConstants.GO_NOTIFICATION_GROUP.createNotification("Vendoring usage is detected",
          "<p><strong>vendor</strong> directory usually means that project uses Go Vendor Experiment.</p>\n" +
              "<p>Selected Go SDK version support vendoring but it's disabled by default.</p>\n" +
              "<p>You may want to explicitly enabled Go Vendor Experiment in the <a href='configure'>project settings</a>.</p>",
          NotificationType.INFORMATION, notificationListener);
      Notifications.Bus.notify(notification, project);
    }
  }

  @Override
  public void dispose() {
    myConnection.disconnect();
    Disposer.dispose(myAlarm);
    VirtualFileManager.getInstance().removeVirtualFileListener(myFilesListener);
    myLastHandledGoPathSourcesRoots.clear();
    myLastHandledExclusions.clear();
    LocalFileSystem.getInstance().removeWatchedRoots(myWatchedRequests);
    myWatchedRequests.clear();
  }

  private class UpdateRequest implements Runnable {
    @Override
    public void run() {
      Project project = myModule.getProject();
      if (GoSdkService.getInstance(project).isGoModule(myModule)) {
        synchronized (myLastHandledGoPathSourcesRoots) {
          Collection<VirtualFile> goPathSourcesRoots = GoSdkUtil.getGoPathSources(project, myModule);
          Set<VirtualFile> excludeRoots = Set.of(ProjectRootManager.getInstance(project).getContentRoots());
          ProgressIndicatorProvider.checkCanceled();
          if (!myLastHandledGoPathSourcesRoots.equals(goPathSourcesRoots) || !myLastHandledExclusions.equals(excludeRoots)) {
            myLastHandledGoPathSourcesRoots.clear();
            myLastHandledGoPathSourcesRoots.addAll(goPathSourcesRoots);

            myLastHandledExclusions.clear();
            myLastHandledExclusions.addAll(excludeRoots);

            List<String> paths = ContainerUtil.map(goPathSourcesRoots, VirtualFile::getPath);
            myWatchedRequests.clear();
            myWatchedRequests.addAll(LocalFileSystem.getInstance().addRootsToWatch(paths, true));
          }
        }
      } else {
        synchronized (myLastHandledGoPathSourcesRoots) {
          LocalFileSystem.getInstance().removeWatchedRoots(myWatchedRequests);
          myLastHandledGoPathSourcesRoots.clear();
          myLastHandledExclusions.clear();
        }
      }
    }
  }
}

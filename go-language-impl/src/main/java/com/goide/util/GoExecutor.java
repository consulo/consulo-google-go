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

package com.goide.util;

import com.goide.sdk.GoSdkService;
import com.goide.sdk.GoSdkUtil;
import consulo.application.ApplicationManager;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.application.progress.Task;
import consulo.disposer.Disposer;
import consulo.execution.ExecutionHelper;
import consulo.execution.RunContentExecutor;
import consulo.execution.process.ExecutionModes;
import consulo.go.GoConstants;
import consulo.go.module.extension.GoModuleExtension;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.process.NopProcessHandler;
import consulo.process.ProcessHandler;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.cmd.ParametersList;
import consulo.process.event.ProcessAdapter;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.process.local.EnvironmentUtil;
import consulo.process.local.ProcessHandlerFactory;
import consulo.process.util.CapturingProcessAdapter;
import consulo.process.util.ProcessOutput;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationType;
import consulo.project.ui.notification.Notifications;
import consulo.undoRedo.CommandProcessor;
import consulo.util.collection.ContainerUtil;
import consulo.util.io.CharsetToolkit;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ThreeState;
import consulo.util.lang.ref.Ref;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;

import org.jspecify.annotations.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GoExecutor {
  private static final Logger LOGGER = Logger.getInstance(GoExecutor.class);
  private final Map<String, String> myExtraEnvironment = new HashMap<>();
  private final ParametersList myParameterList = new ParametersList();
  private final ProcessOutput myProcessOutput = new ProcessOutput();
  private final Project myProject;
  @Nullable
  private Boolean myVendoringEnabled;
  @Nullable
  private final Module myModule;
  @Nullable
  private String myGoRoot;
  @Nullable
  private String myEnvPath;
  @Nullable
  private String myWorkDirectory;
  private boolean myShowOutputOnError;
  private boolean myShowNotificationsOnError;
  private boolean myShowNotificationsOnSuccess;
  private boolean myShowGoEnvVariables = true;
  private GeneralCommandLine.ParentEnvironmentType myParentEnvironmentType = GeneralCommandLine.ParentEnvironmentType.CONSOLE;
  @Nullable
  private String myExePath;
  @Nullable
  private String myPresentableName;
  private ProcessHandler myProcessHandler;
  private final Collection<ProcessListener> myProcessListeners = ContainerUtil.newArrayList();

  private GoExecutor(Project project, @Nullable Module module) {
    myProject = project;
    myModule = module;
  }

  public static GoExecutor in(Project project, @Nullable Module module) {
    return module != null ? in(module) : in(project);
  }

  private static GoExecutor in(Project project) {
    return new GoExecutor(project, null).withGoRoot(GoSdkService.getInstance(project).getSdkHomePath(null))
            .withEnvPath(GoSdkUtil.retrieveEnvironmentPathForGo(project, null));
  }

  public static GoExecutor in(Module module) {
    Project project = module.getProject();
    ThreeState vendoringEnabled = GoModuleExtension.getVendoringEnabled(module);
    return new GoExecutor(project, module).withGoRoot(GoSdkService.getInstance(project).getSdkHomePath(module))
            .withEnvPath(GoSdkUtil.retrieveEnvironmentPathForGo(project, module))
            .withVendoring(vendoringEnabled != ThreeState.UNSURE ? vendoringEnabled.toBoolean() : null);
  }

  /**
   * Runs {@code go get -t <package>/...} and refreshes the file system when it is done.
   */
  public static void goGet(Project project, @Nullable Module module, String packageName, boolean startInBackground) {
    String sdkPath = GoSdkService.getInstance(project).getSdkHomePath(module);
    if (StringUtil.isEmpty(sdkPath)) return;
    CommandProcessor.getInstance().runUndoTransparentAction(() -> {
      Consumer<Boolean> consumer = aBoolean -> VirtualFileManager.getInstance().asyncRefresh(null);
      in(project, module).withPresentableName("go get -t " + packageName + "/...")
        .withParameters("get", "-t", packageName + "/...").showNotifications(false, true).showOutputOnError()
        .executeWithProgress(!startInBackground, consumer);
    });
  }

  public GoExecutor withPresentableName(@Nullable String presentableName) {
    myPresentableName = presentableName;
    return this;
  }

  public GoExecutor withExePath(@Nullable String exePath) {
    myExePath = exePath;
    return this;
  }

  public GoExecutor withWorkDirectory(@Nullable String workDirectory) {
    myWorkDirectory = workDirectory;
    return this;
  }

  public GoExecutor withGoRoot(@Nullable String goRoot) {
    myGoRoot = goRoot;
    return this;
  }

  public GoExecutor withEnvPath(@Nullable String envPath) {
    myEnvPath = envPath;
    return this;
  }

  public GoExecutor withVendoring(@Nullable Boolean enabled) {
    myVendoringEnabled = enabled;
    return this;
  }

  public GoExecutor withProcessListener(ProcessListener listener) {
    myProcessListeners.add(listener);
    return this;
  }

  public GoExecutor withExtraEnvironment(Map<String, String> environment) {
    myExtraEnvironment.putAll(environment);
    return this;
  }

  public GoExecutor withPassParentEnvironment(boolean passParentEnvironment) {
    myParentEnvironmentType = passParentEnvironment ? GeneralCommandLine.ParentEnvironmentType.CONSOLE : GeneralCommandLine.ParentEnvironmentType.NONE;
    return this;
  }

  public GoExecutor withParameterString(String parameterString) {
    myParameterList.addParametersString(parameterString);
    return this;
  }

  public GoExecutor withParameters(String... parameters) {
    myParameterList.addAll(parameters);
    return this;
  }

  public GoExecutor showGoEnvVariables(boolean show) {
    myShowGoEnvVariables = show;
    return this;
  }

  public GoExecutor showOutputOnError() {
    myShowOutputOnError = true;
    return this;
  }

  public GoExecutor showNotifications(boolean onError, boolean onSuccess) {
    myShowNotificationsOnError = onError;
    myShowNotificationsOnSuccess = onSuccess;
    return this;
  }

  public boolean execute() {
    Logger.getInstance(getClass()).assertTrue(!ApplicationManager.getApplication().isDispatchThread(), "It's bad idea to run external tool on EDT");
    Logger.getInstance(getClass()).assertTrue(myProcessHandler == null, "Process has already run with this executor instance");
    Ref<Boolean> result = Ref.create(false);
    GeneralCommandLine commandLine = null;
    try {
      commandLine = createCommandLine();
      GeneralCommandLine finalCommandLine = commandLine;
      myProcessHandler = ProcessHandlerFactory.getInstance().createKillableProcessHandler(finalCommandLine);
      myProcessHandler.addProcessListener(new ProcessAdapter() {
        @Override
        public void startNotified(ProcessEvent event) {
          if (myShowGoEnvVariables) {
            printGoEnvVariables(finalCommandLine, myProcessHandler);
          }
        }
      });
      GoHistoryProcessListener historyProcessListener = new GoHistoryProcessListener();
      myProcessHandler.addProcessListener(historyProcessListener);
      for (ProcessListener listener : myProcessListeners) {
        myProcessHandler.addProcessListener(listener);
      }

      CapturingProcessAdapter processAdapter = new CapturingProcessAdapter(myProcessOutput) {
        @Override
        public void processTerminated(ProcessEvent event) {
          super.processTerminated(event);
          boolean success = event.getExitCode() == 0 && myProcessOutput.getStderr().isEmpty();
          boolean nothingToShow = myProcessOutput.getStdout().isEmpty() && myProcessOutput.getStderr().isEmpty();
          boolean cancelledByUser = (event.getExitCode() == -1 || event.getExitCode() == 2) && nothingToShow;
          result.set(success);
          if (success) {
            if (myShowNotificationsOnSuccess) {
              showNotification("Finished successfully", NotificationType.INFORMATION);
            }
          }
          else if (cancelledByUser) {
            if (myShowNotificationsOnError) {
              showNotification("Interrupted", NotificationType.WARNING);
            }
          }
          else if (myShowOutputOnError) {
            ApplicationManager.getApplication().invokeLater(() -> showOutput(myProcessHandler, historyProcessListener));
          }
        }
      };

      myProcessHandler.addProcessListener(processAdapter);
      myProcessHandler.startNotify();
      ExecutionModes.SameThreadMode sameThreadMode = new ExecutionModes.SameThreadMode(getPresentableName());
      ExecutionHelper.executeExternalProcess(myProject, myProcessHandler, sameThreadMode, commandLine);

      LOGGER.debug("Finished `" + getPresentableName() + "` with result: " + result.get());
      return result.get();
    }
    catch (ExecutionException e) {
      if (myShowOutputOnError) {
        ExecutionHelper.showErrors(myProject, Collections.singletonList(e), getPresentableName(), null);
      }
      if (myShowNotificationsOnError) {
        showNotification(StringUtil.notNullize(e.getMessage(), "Unknown error, see logs for details"), NotificationType.ERROR);
      }
      String commandLineInfo = commandLine != null ? commandLine.getCommandLineString() : "not constructed";
      LOGGER.debug("Finished `" + getPresentableName() + "` with an exception. Commandline: " + commandLineInfo, e);
      return false;
    }
  }

  public void executeWithProgress(boolean modal) {
    //noinspection unchecked
    executeWithProgress(modal, c -> {});
  }

  public void executeWithProgress(boolean modal, Consumer<Boolean> consumer) {
    ProgressManager.getInstance().run(new Task.Backgroundable(myProject, getPresentableName(), true) {
      private boolean doNotStart;

      @Override
      public void onCancel() {
        doNotStart = true;
        ProcessHandler handler = getProcessHandler();
        if (handler != null) {
          handler.destroyProcess();
        }
      }

      @Override
      public boolean shouldStartInBackground() {
        return !modal;
      }

      @Override
      public boolean isConditionalModal() {
        return modal;
      }

      @Override
      public void run(ProgressIndicator indicator) {
        if (doNotStart || myProject == null || myProject.isDisposed()) {
          return;
        }
        indicator.setIndeterminate(true);
        consumer.accept(execute());
      }
    });
  }

  @Nullable
  public ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  private void showNotification(String message, NotificationType type) {
    ApplicationManager.getApplication().invokeLater(() -> {
      String title = getPresentableName();
      Notifications.Bus.notify(GoConstants.GO_EXECUTION_NOTIFICATION_GROUP.createNotification(title, message, type, null), myProject);
    });
  }

  private void showOutput(ProcessHandler originalHandler, GoHistoryProcessListener historyProcessListener) {
    if (myShowOutputOnError) {
      NopProcessHandler process = new NopProcessHandler();
      RunContentExecutor runContentExecutor =
              new RunContentExecutor(myProject, process).withTitle(getPresentableName()).withActivateToolWindow(myShowOutputOnError)
                      .withFilter(new GoConsoleFilter(myProject, myModule, myWorkDirectory != null ? VirtualFileUtil.pathToUrl(myWorkDirectory) : null));
      Disposer.register(myProject, runContentExecutor);
      runContentExecutor.run();
      historyProcessListener.apply(process);
    }
    if (myShowNotificationsOnError) {
      showNotification("Failed to run", NotificationType.ERROR);
    }
  }

  public GeneralCommandLine createCommandLine() throws ExecutionException {
    if (myGoRoot == null) {
      throw new ExecutionException("Sdk is not set or Sdk home path is empty for module");
    }

    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setExePath(ObjectUtil.notNull(myExePath, GoSdkService.getGoExecutablePath(myGoRoot)));
    commandLine.getEnvironment().putAll(myExtraEnvironment);
    commandLine.getEnvironment().put(GoConstants.GO_ROOT, StringUtil.notNullize(myGoRoot));
    if (myVendoringEnabled != null) {
      commandLine.getEnvironment().put(GoConstants.GO_VENDORING_EXPERIMENT, myVendoringEnabled ? "1" : "0");
    }

    Collection<String> paths = ContainerUtil.newArrayList();
    ContainerUtil.addIfNotNull(paths, StringUtil.nullize(commandLine.getEnvironment().get(GoConstants.PATH), true));
    ContainerUtil.addIfNotNull(paths, StringUtil.nullize(EnvironmentUtil.getValue(GoConstants.PATH), true));
    ContainerUtil.addIfNotNull(paths, StringUtil.nullize(myEnvPath, true));
    commandLine.getEnvironment().put(GoConstants.PATH, StringUtil.join(paths, File.pathSeparator));

    commandLine.withWorkDirectory(myWorkDirectory);
    commandLine.addParameters(myParameterList.getList());
    commandLine.withParentEnvironmentType(myParentEnvironmentType);
    commandLine.withCharset(CharsetToolkit.UTF8_CHARSET);
    return commandLine;
  }

  private String getPresentableName() {
    return ObjectUtil.notNull(myPresentableName, "go");
  }

  @Nullable
  public String getWorkDirectory() {
    return myWorkDirectory;
  }

  private static void printGoEnvVariables(GeneralCommandLine commandLine, ProcessHandler handler) {
    Map<String, String> environment = commandLine.getEnvironment();
    handler.notifyTextAvailable("GOROOT=" + StringUtil.nullize(environment.get(GoConstants.GO_ROOT)) + '\n', ProcessOutputTypes.SYSTEM);
  }
}

package com.goide.runconfig;

import com.goide.runconfig.application.GoApplicationConfiguration;
import com.goide.runconfig.application.GoApplicationRunningState;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.FileDocumentManager;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.runner.DefaultProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.RunContentDescriptor;
import consulo.go.debug.GoDlvNativeDebuggerProvider;
import consulo.nativeDev.debugger.NativeDebugSessionStarter;
import consulo.nativeDev.debugger.driver.NativeDebugTarget;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.cmd.GeneralCommandLine;

import java.io.File;
import java.nio.file.Path;

/**
 * Debugs the built program with delve, through the native debugger.
 *
 * @author VISTALL
 * @since 2026-02-24
 */
@ExtensionImpl
public class GoDebugRunner extends DefaultProgramRunner {
    @Override
    public String getRunnerId() {
        return "GoDebug";
    }

    @Override
    public boolean canRun(String executorId, RunProfile profile) {
        return profile instanceof GoApplicationConfiguration && DefaultDebugExecutor.EXECUTOR_ID.equals(executorId);
    }

    @Override
    protected RunContentDescriptor doExecute(RunProfileState state, ExecutionEnvironment env) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();

        GoApplicationRunningState goState = (GoApplicationRunningState) state;
        GeneralCommandLine commandLine = goState.createProgramCommandLine();
        File workDirectory = commandLine.getWorkDirectory();
        NativeDebugTarget target = NativeDebugTarget.launch(
            Path.of(commandLine.getExePath()),
            commandLine.getParametersList().getList(),
            workDirectory == null ? null : workDirectory.toPath(),
            commandLine.getEnvironment(),
            null
        );

        RunContentDescriptor descriptor = NativeDebugSessionStarter.getInstance().startSession(env, target, GoDlvNativeDebuggerProvider.ID);
        ProcessHandler processHandler = descriptor.getProcessHandler();
        if (processHandler != null) {
            goState.deleteOutputFileOnExit(processHandler);
        }
        return descriptor;
    }
}

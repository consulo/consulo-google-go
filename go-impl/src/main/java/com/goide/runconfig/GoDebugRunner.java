package com.goide.runconfig;

import com.goide.compiler.GoCompilerRunner;
import com.goide.runconfig.application.GoApplicationConfiguration;
import com.goide.runconfig.application.GoApplicationRunningState;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.FileDocumentManager;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.*;
import consulo.execution.runner.DefaultProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.RunContentDescriptor;
import consulo.externalService.statistic.UsageTrigger;
import consulo.go.debug.GoDebugProcess;
import consulo.process.ExecutionException;
import consulo.util.io.NetUtil;

import java.io.File;
import java.io.IOException;

/**
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
        if (profile instanceof GoApplicationConfiguration) {
            return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId);
        }
        return false;
    }

    @Override
    protected RunContentDescriptor doExecute(RunProfileState state, ExecutionEnvironment env) throws ExecutionException {
        final int port;
        try {
            port = NetUtil.findAvailableSocketPort();
        }
        catch (IOException e) {
            throw new ExecutionException(e);
        }

        FileDocumentManager.getInstance().saveAllDocuments();

        File outputFilePath = env.getUserData(GoCompilerRunner.OUTPUT_FILE);

        ((GoApplicationRunningState) state).setDebugPort(port);

        state.execute(env.getExecutor(), this);

        UsageTrigger.trigger("go.dlv.debugger");

        XDebugSession session = XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter() {
            @Override
            public XDebugProcess start(XDebugSession session) throws ExecutionException {
                GoDebugProcess process = new GoDebugProcess(session, port, outputFilePath.getAbsolutePath());
                process.start();
                return process;
            }
        });

        return session.getRunContentDescriptor();
    }
}

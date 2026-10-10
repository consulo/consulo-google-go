/*
 * Copyright 2013-2026 consulo.io
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
package consulo.go.debug;

import consulo.execution.debug.XDebugSession;
import consulo.execution.debugger.dap.protocol.DAP;
import consulo.execution.debugger.dap.protocol.DAPFactory;
import consulo.execution.ui.console.ConsoleViewContentType;
import consulo.nativeDev.debugger.NativeDebuggerInstallation;
import consulo.nativeDev.debugger.dap.NativeDapDebugProcess;
import consulo.nativeDev.debugger.driver.NativeDebugTarget;
import consulo.nativeDev.debugger.driver.NativeDebuggerException;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.dataholder.Key;
import consulo.util.io.NetUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code dlv dap} has no stdin/stdout mode - it serves DAP on the port it is told to listen on.
 *
 * @author VISTALL
 * @since 2026-10-07
 */
public class GoDlvDebugProcess extends NativeDapDebugProcess {
    private static final long ADAPTER_EXIT_TIMEOUT_MS = 2000;
    private static final String LOCALHOST = "127.0.0.1";

    private final NativeDebugTarget myTarget;
    private final NativeDebuggerInstallation myInstallation;
    private final GoDlvNativeDebuggerProvider myProvider;

    private volatile @Nullable ProcessHandler myAdapterHandler;

    public GoDlvDebugProcess(XDebugSession session,
                             NativeDebugTarget target,
                             NativeDebuggerInstallation installation,
                             GoDlvNativeDebuggerProvider provider) {
        super(session, target, installation, provider);
        myTarget = target;
        myInstallation = installation;
        myProvider = provider;
    }

    @Override
    protected DAP createDAP(DAPFactory factory) {
        int port;
        try {
            port = NetUtil.findAvailableSocketPort();
        }
        catch (IOException e) {
            throw new NativeDebuggerException("No free port for " + myInstallation.executable() + ": " + e.getMessage(), e);
        }

        List<String> command = new ArrayList<>(myProvider.getCommandLine(myInstallation.executable(), myTarget));
        command.add("--listen=" + LOCALHOST + ":" + port);
        GeneralCommandLine commandLine = new GeneralCommandLine(command).withCharset(StandardCharsets.UTF_8);
        Path workingDirectory = myTarget.workingDirectory();
        if (workingDirectory != null) {
            commandLine.withWorkDirectory(workingDirectory.toFile());
        }

        ProcessHandler handler;
        try {
            handler = getSession().getProject().getApplication().getInstance(ProcessHandlerBuilderFactory.class)
                .newBuilder(commandLine)
                .build();
        }
        catch (ExecutionException e) {
            throw new NativeDebuggerException("Failed to start " + myInstallation.executable() + ": " + e.getMessage(), e);
        }
        myAdapterHandler = handler;
        handler.addProcessListener(new ProcessListener() {
            @Override
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                // the protocol goes over the socket - what delve prints is its own
                if (outputType == ProcessOutputTypes.STDERR) {
                    getSession().getConsoleView().print(event.getText(), ConsoleViewContentType.LOG_ERROR_OUTPUT);
                }
                else if (outputType == ProcessOutputTypes.STDOUT) {
                    getSession().getConsoleView().print(event.getText(), ConsoleViewContentType.LOG_INFO_OUTPUT);
                }
            }
        });
        handler.startNotify();

        // the connection retries until delve listens
        return factory.createSocketDAP(LOCALHOST, port);
    }

    @Override
    protected void stopImpl() {
        super.stopImpl();
        ProcessHandler handler = myAdapterHandler;
        if (handler != null && !handler.waitFor(ADAPTER_EXIT_TIMEOUT_MS)) {
            handler.destroyProcess();
        }
    }
}

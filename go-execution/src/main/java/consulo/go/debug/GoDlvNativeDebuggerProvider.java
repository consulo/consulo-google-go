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

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.debug.XDebugProcess;
import consulo.execution.debug.XDebugSession;
import consulo.go.GoConstants;
import consulo.localize.LocalizeValue;
import consulo.nativeDev.debugger.NativeDebuggerInstallation;
import consulo.nativeDev.debugger.dap.NativeDapDebuggerProvider;
import consulo.nativeDev.debugger.driver.NativeDebugTarget;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Delve - it speaks DAP only on a port, see {@link GoDlvDebugProcess}.
 *
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class GoDlvNativeDebuggerProvider extends NativeDapDebuggerProvider {
    public static final String ID = "dlv";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.of("Delve");
    }

    @Override
    public int getPriority() {
        // Go debugging asks for it by id - it must never win the automatic pick for other languages
        return -1000;
    }

    @Override
    protected List<String> getExecutableNames() {
        return List.of(GoConstants.DELVE_EXECUTABLE_NAME);
    }

    @Override
    public List<NativeDebuggerInstallation> findInstallations() {
        return findInstallations("dlv.path", "delve");
    }

    /**
     * Without the address to listen on - {@link GoDlvDebugProcess} adds it.
     */
    @Override
    public List<String> getCommandLine(Path executable, NativeDebugTarget target) {
        return List.of(executable.toString(), "dap");
    }

    @Override
    public Map<String, Object> createLaunchArguments(NativeDebugTarget target) {
        Map<String, Object> arguments = new LinkedHashMap<>();
        // the binary is built already - delve does not build it
        arguments.put("mode", "exec");
        Path executable = target.executable();
        if (executable != null) {
            arguments.put("program", executable.toString());
        }
        arguments.put("args", target.arguments());
        Path workingDirectory = target.workingDirectory();
        if (workingDirectory != null) {
            arguments.put("cwd", workingDirectory.toString());
        }
        if (!target.environment().isEmpty()) {
            arguments.put("env", target.environment());
        }
        arguments.put("stopOnEntry", false);
        return arguments;
    }

    @Override
    public XDebugProcess createProcess(XDebugSession session, NativeDebugTarget target, NativeDebuggerInstallation installation) {
        return new GoDlvDebugProcess(session, target, installation, this);
    }
}

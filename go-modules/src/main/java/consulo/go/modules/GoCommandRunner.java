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

package consulo.go.modules;

import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.ProcessOutputType;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.dataholder.Key;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Runs {@code go} sub-commands and keeps their handlers, so a running command can be cancelled by the key it was started with.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoCommandRunner {
    public record Result(int exitCode, String stdout, String stderr) {
        public boolean isSuccess() {
            return exitCode == 0;
        }
    }

    private final ProcessHandlerBuilderFactory myProcessHandlerBuilderFactory;
    private final Map<Object, ProcessHandler> myHandlers = new ConcurrentHashMap<>();

    public GoCommandRunner(ProcessHandlerBuilderFactory processHandlerBuilderFactory) {
        myProcessHandlerBuilderFactory = processHandlerBuilderFactory;
    }

    /**
     * @param stderrConsumer receives stderr as it arrives, the boolean is always {@code false}; stdout is only collected
     */
    public Result run(Object key, GeneralCommandLine commandLine, @Nullable BiConsumer<String, Boolean> stderrConsumer)
        throws ExecutionException {
        return run(key, commandLine, stderrConsumer, false);
    }

    /**
     * @param outputConsumer receives the text of the process as it arrives, the boolean tells whether it is stdout
     * @param streamStdout   whether {@code outputConsumer} also receives stdout
     */
    public Result run(Object key,
                      GeneralCommandLine commandLine,
                      @Nullable BiConsumer<String, Boolean> outputConsumer,
                      boolean streamStdout) throws ExecutionException {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        ProcessHandler handler = myProcessHandlerBuilderFactory.newBuilder(commandLine).killable().build();
        handler.addProcessListener(new ProcessListener() {
            @Override
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                String text = event.getText();
                if (ProcessOutputType.isStderr(outputType)) {
                    stderr.append(text);
                    if (outputConsumer != null) {
                        outputConsumer.accept(text, false);
                    }
                }
                else if (ProcessOutputType.isStdout(outputType)) {
                    stdout.append(text);
                    if (outputConsumer != null && streamStdout) {
                        outputConsumer.accept(text, true);
                    }
                }
            }
        });

        myHandlers.put(key, handler);
        try {
            handler.startNotify();
            handler.waitFor();
        }
        finally {
            myHandlers.remove(key, handler);
        }

        Integer exitCode = handler.getExitCode();
        return new Result(exitCode == null ? -1 : exitCode, stdout.toString(), stderr.toString());
    }

    public boolean cancel(Object key) {
        ProcessHandler handler = myHandlers.remove(key);
        if (handler == null) {
            return false;
        }
        handler.destroyProcess();
        return true;
    }
}

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

import consulo.go.GoConstants;
import consulo.go.GoEnvironmentUtil;
import com.goide.sdk.GoSdkService;
import com.goide.sdk.GoSdkType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.process.PathEnvironmentVariableUtil;
import consulo.process.cmd.GeneralCommandLine;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The {@code go} binary used to read and build a Go modules project.
 * <p>
 * A configured Go SDK wins, because that is what the user picked for the project; without one the {@code go}
 * found on {@code PATH} is used, so a project can be imported before any SDK exists.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoToolchain implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String myExecutable;
    private final @Nullable String myGoRoot;

    public GoToolchain(String executable, @Nullable String goRoot) {
        myExecutable = executable;
        myGoRoot = goRoot;
    }

    public static @Nullable GoToolchain find(@Nullable String sdkName) {
        SdkTable sdkTable = SdkTable.getInstance();
        if (sdkName != null) {
            Sdk sdk = sdkTable.findSdk(sdkName);
            GoToolchain toolchain = sdk != null ? fromSdk(sdk) : null;
            if (toolchain != null) {
                return toolchain;
            }
        }

        Sdk mostRecent = sdkTable.findMostRecentSdk(sdk -> sdk.getSdkType() instanceof GoSdkType && fromSdk(sdk) != null);
        if (mostRecent != null) {
            return fromSdk(mostRecent);
        }
        return fromPath();
    }

    public static @Nullable GoToolchain fromSdk(Sdk sdk) {
        if (!(sdk.getSdkType() instanceof GoSdkType)) {
            return null;
        }
        String homePath = sdk.getHomePath();
        if (StringUtil.isEmpty(homePath)) {
            return null;
        }
        String executable = GoSdkService.getGoExecutablePath(homePath);
        if (executable == null || !Files.isExecutable(Path.of(executable))) {
            return null;
        }
        return new GoToolchain(executable, homePath);
    }

    public static @Nullable GoToolchain fromPath() {
        File executable = PathEnvironmentVariableUtil.findInPath(GoEnvironmentUtil.getBinaryFileNameForPath(GoConstants.GO_EXECUTABLE_NAME));
        return executable != null ? new GoToolchain(executable.getAbsolutePath(), null) : null;
    }

    public String getExecutable() {
        return myExecutable;
    }

    /**
     * @return GOROOT to force, or {@code null} when the {@code go} binary decides it itself
     */
    public @Nullable String getGoRoot() {
        return myGoRoot;
    }

    public GeneralCommandLine createCommandLine(Path workDirectory, List<String> arguments) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setExePath(myExecutable);
        commandLine.withWorkDirectory(workDirectory.toFile());
        commandLine.addParameters(arguments);
        commandLine.withCharset(StandardCharsets.UTF_8);
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE);

        Map<String, String> environment = commandLine.getEnvironment();
        if (myGoRoot != null) {
            environment.put(GoConstants.GO_ROOT, myGoRoot);
        }
        // sub-processes like go generate expect the same go first on PATH
        Path binDirectory = Path.of(myExecutable).getParent();
        if (binDirectory != null) {
            String parentPath = System.getenv(GoConstants.PATH);
            environment.put(GoConstants.PATH,
                StringUtil.isEmpty(parentPath) ? binDirectory.toString() : binDirectory + File.pathSeparator + parentPath);
        }
        return commandLine;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof GoToolchain that && myExecutable.equals(that.myExecutable) && Objects.equals(myGoRoot, that.myGoRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myExecutable, myGoRoot);
    }

    @Override
    public String toString() {
        return myExecutable;
    }
}

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

package consulo.go.modules.externalSystem;

import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import consulo.go.modules.GoModulesConstants;
import org.jspecify.annotations.Nullable;

/**
 * What the import knows about one main module beyond {@link consulo.externalSystem.model.project.ModuleData}:
 * it switches on the Go extension and picks the SDK.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModuleData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    public static final Key<GoModuleData> KEY = Key.create(GoModuleData.class, ProjectKeys.MODULE.getProcessingWeight() + 1);

    private final String myModulePath;
    private final String myGoModFile;
    private final @Nullable String myGoVersion;
    private final @Nullable String myWorkFile;
    private final @Nullable String myGoRoot;
    private final @Nullable String mySdkName;
    private final @Nullable String myToolchainVersion;

    public GoModuleData(String modulePath,
                        String goModFile,
                        @Nullable String goVersion,
                        @Nullable String workFile,
                        @Nullable String goRoot,
                        @Nullable String sdkName,
                        @Nullable String toolchainVersion) {
        super(GoModulesConstants.SYSTEM_ID);
        myModulePath = modulePath;
        myGoModFile = goModFile;
        myGoVersion = goVersion;
        myWorkFile = workFile;
        myGoRoot = goRoot;
        mySdkName = sdkName;
        myToolchainVersion = toolchainVersion;
    }

    public String getModulePath() {
        return myModulePath;
    }

    public String getGoModFile() {
        return myGoModFile;
    }

    /**
     * @return the {@code go} directive of the {@code go.mod}
     */
    public @Nullable String getGoVersion() {
        return myGoVersion;
    }

    public @Nullable String getWorkFile() {
        return myWorkFile;
    }

    /**
     * @return GOROOT of the {@code go} binary that read the project
     */
    public @Nullable String getGoRoot() {
        return myGoRoot;
    }

    /**
     * @return the SDK the user picked for the linked project, {@code null} to pick one by {@link #getGoRoot()}
     */
    public @Nullable String getSdkName() {
        return mySdkName;
    }

    /**
     * @return GOVERSION of the {@code go} binary that read the project
     */
    public @Nullable String getToolchainVersion() {
        return myToolchainVersion;
    }
}

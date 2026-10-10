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
 * A module of the build list a main module depends on.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoDependencyData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    public static final Key<GoDependencyData> KEY = Key.create(GoDependencyData.class, ProjectKeys.LIBRARY_DEPENDENCY.getProcessingWeight() + 1);

    private final String myModulePath;
    private final @Nullable String myVersion;
    private final @Nullable String myDirectory;
    private final @Nullable String myReplacement;
    private final boolean myIndirect;
    private final @Nullable String myError;

    public GoDependencyData(String modulePath,
                            @Nullable String version,
                            @Nullable String directory,
                            @Nullable String replacement,
                            boolean indirect,
                            @Nullable String error) {
        super(GoModulesConstants.SYSTEM_ID);
        myModulePath = modulePath;
        myVersion = version;
        myDirectory = directory;
        myReplacement = replacement;
        myIndirect = indirect;
        myError = error;
    }

    public String getModulePath() {
        return myModulePath;
    }

    public @Nullable String getVersion() {
        return myVersion;
    }

    /**
     * @return the directory of the module sources, {@code null} when the module is not in the module cache
     */
    public @Nullable String getDirectory() {
        return myDirectory;
    }

    public @Nullable String getReplacement() {
        return myReplacement;
    }

    public boolean isIndirect() {
        return myIndirect;
    }

    /**
     * @return why {@code go} could not resolve the module
     */
    public @Nullable String getError() {
        return myError;
    }
}

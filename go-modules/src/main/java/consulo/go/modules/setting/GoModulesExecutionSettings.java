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

package consulo.go.modules.setting;

import consulo.externalSystem.model.setting.ExternalSystemExecutionSettings;
import consulo.go.modules.GoToolchain;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * What the resolver and the task manager need, computed on the IDE side: they run without a project.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModulesExecutionSettings extends ExternalSystemExecutionSettings {
    private static final long serialVersionUID = 1L;

    private final @Nullable GoToolchain myToolchain;
    private final @Nullable String mySdkName;
    private final boolean myDownloadDependencies;
    private final HashMap<String, String> myModuleNames;

    /**
     * @param moduleNames module directory to the name of the IDE module already sitting on it, so an import reuses
     *                    the modules a user made instead of adding a second one on the same content root
     */
    public GoModulesExecutionSettings(@Nullable GoToolchain toolchain,
                                      @Nullable String sdkName,
                                      boolean downloadDependencies,
                                      Map<String, String> moduleNames) {
        myToolchain = toolchain;
        mySdkName = sdkName;
        myDownloadDependencies = downloadDependencies;
        myModuleNames = new HashMap<>(moduleNames);
    }

    public @Nullable GoToolchain getToolchain() {
        return myToolchain;
    }

    public @Nullable String getSdkName() {
        return mySdkName;
    }

    public boolean isDownloadDependencies() {
        return myDownloadDependencies;
    }

    public Map<String, String> getModuleNames() {
        return myModuleNames;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof GoModulesExecutionSettings that) || !super.equals(o)) {
            return false;
        }
        return myDownloadDependencies == that.myDownloadDependencies
            && Objects.equals(myToolchain, that.myToolchain)
            && Objects.equals(mySdkName, that.mySdkName)
            && myModuleNames.equals(that.myModuleNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), myToolchain, mySdkName, myDownloadDependencies, myModuleNames);
    }
}

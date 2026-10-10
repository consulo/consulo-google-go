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

import consulo.externalSystem.setting.ExternalProjectSettings;
import org.jspecify.annotations.Nullable;

/**
 * Settings of one linked Go modules project: a directory holding a {@code go.work} or a {@code go.mod}.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModulesProjectSettings extends ExternalProjectSettings {
    private @Nullable String mySdkName;
    private boolean myDownloadDependencies = true;

    public @Nullable String getSdkName() {
        return mySdkName;
    }

    public void setSdkName(@Nullable String sdkName) {
        mySdkName = sdkName;
    }

    /**
     * @return whether the import downloads the modules of the build list missing from the module cache
     */
    public boolean isDownloadDependencies() {
        return myDownloadDependencies;
    }

    public void setDownloadDependencies(boolean downloadDependencies) {
        myDownloadDependencies = downloadDependencies;
    }

    @Override
    public GoModulesProjectSettings clone() {
        GoModulesProjectSettings clone = new GoModulesProjectSettings();
        copyTo(clone);
        clone.mySdkName = mySdkName;
        clone.myDownloadDependencies = myDownloadDependencies;
        return clone;
    }
}

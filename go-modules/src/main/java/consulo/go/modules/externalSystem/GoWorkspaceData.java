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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The full set of modules one linked project imported. Processed after every module is in place, it lets the import drop
 * the modules of a {@code use} that left the {@code go.work}.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoWorkspaceData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    public static final Key<GoWorkspaceData> KEY = Key.create(GoWorkspaceData.class, ProjectKeys.LIBRARY_DEPENDENCY.getProcessingWeight() + 2);

    private final String myLinkedProjectPath;
    private final @Nullable String myWorkFile;
    private final ArrayList<String> myModuleNames;

    public GoWorkspaceData(String linkedProjectPath, @Nullable String workFile, Collection<String> moduleNames) {
        super(GoModulesConstants.SYSTEM_ID);
        myLinkedProjectPath = linkedProjectPath;
        myWorkFile = workFile;
        myModuleNames = new ArrayList<>(moduleNames);
    }

    public String getLinkedProjectPath() {
        return myLinkedProjectPath;
    }

    public @Nullable String getWorkFile() {
        return myWorkFile;
    }

    public List<String> getModuleNames() {
        return myModuleNames;
    }
}

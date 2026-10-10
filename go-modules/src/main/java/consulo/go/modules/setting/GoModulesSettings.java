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

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.externalSystem.autoimport.ExternalSystemProjectId;
import consulo.externalSystem.autoimport.ExternalSystemProjectTracker;
import consulo.externalSystem.setting.AbstractExternalSystemSettings;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;
import consulo.go.modules.GoModulesConstants;
import consulo.project.Project;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * The Go modules projects linked to an IDE project.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "GoModulesSettings", storages = @Storage("go-modules"))
public class GoModulesSettings extends AbstractExternalSystemSettings<GoModulesSettings, GoModulesProjectSettings, GoModulesSettingsListener>
    implements PersistentStateComponent<GoModulesSettingsState> {

    @Inject
    public GoModulesSettings(Project project) {
        super(GoModulesSettingsListener.TOPIC, project);
    }

    public static GoModulesSettings getInstance(Project project) {
        return project.getInstance(GoModulesSettings.class);
    }

    /**
     * @return the linked project that is {@code path} or contains it
     */
    public @Nullable GoModulesProjectSettings findLinkedProjectSettingsFor(String path) {
        GoModulesProjectSettings exact = getLinkedProjectSettings(path);
        if (exact != null) {
            return exact;
        }
        Path target;
        try {
            target = Path.of(path).toAbsolutePath().normalize();
        }
        catch (InvalidPathException e) {
            return null;
        }
        GoModulesProjectSettings best = null;
        int bestLength = -1;
        for (GoModulesProjectSettings settings : getLinkedProjectsSettings()) {
            String projectPath = settings.getExternalProjectPath();
            if (projectPath == null) {
                continue;
            }
            Path root = Path.of(projectPath).toAbsolutePath().normalize();
            if (target.startsWith(root) && projectPath.length() > bestLength) {
                best = settings;
                bestLength = projectPath.length();
            }
        }
        return best;
    }

    @Override
    public void subscribe(ExternalSystemSettingsListener<GoModulesProjectSettings> listener) {
        getProject().getMessageBus().connect(getProject()).subscribe(GoModulesSettingsListener.TOPIC, new GoModulesSettingsListenerAdapter(listener));
    }

    @Override
    protected void copyExtraSettingsFrom(GoModulesSettings settings) {
    }

    @Override
    protected void checkSettings(GoModulesProjectSettings old, GoModulesProjectSettings current) {
        Project project = getProject();
        String path = current.getExternalProjectPath();
        if (project.isDefault() || path == null) {
            return;
        }
        if (!Objects.equals(old.getSdkName(), current.getSdkName()) || old.isDownloadDependencies() != current.isDownloadDependencies()) {
            ExternalSystemProjectTracker tracker = ExternalSystemProjectTracker.getInstance(project);
            tracker.markDirty(new ExternalSystemProjectId(GoModulesConstants.SYSTEM_ID, path));
            tracker.scheduleProjectRefresh();
        }
    }

    @Override
    public GoModulesSettingsState getState() {
        GoModulesSettingsState state = new GoModulesSettingsState();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(GoModulesSettingsState state) {
        super.loadState(state);
    }
}

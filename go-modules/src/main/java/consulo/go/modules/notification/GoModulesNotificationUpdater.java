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

package consulo.go.modules.notification;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicImpl;
import consulo.fileEditor.EditorNotifications;
import consulo.go.modules.setting.GoModulesProjectSettings;
import consulo.go.modules.setting.GoModulesSettingsListener;
import consulo.project.Project;
import jakarta.inject.Inject;

import java.util.Collection;
import java.util.Set;

/**
 * Projects get linked from the tool window and the import wizard too: whichever way, the "not linked" banner has to go.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@TopicImpl(ComponentScope.PROJECT)
public class GoModulesNotificationUpdater implements GoModulesSettingsListener {
    private final Project myProject;

    @Inject
    public GoModulesNotificationUpdater(Project project) {
        myProject = project;
    }

    @Override
    public void onProjectsLinked(Collection<GoModulesProjectSettings> settings) {
        update();
    }

    @Override
    public void onProjectsUnlinked(Set<String> linkedProjectPaths) {
        update();
    }

    private void update() {
        // without the service no editor shows a banner yet, there is nothing to update
        EditorNotifications notifications = myProject.isDisposed() ? null : myProject.getInstanceIfCreated(EditorNotifications.class);
        if (notifications != null) {
            notifications.updateAllNotifications();
        }
    }
}

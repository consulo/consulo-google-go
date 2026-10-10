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

import consulo.go.GoConstants;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.fileEditor.EditorNotificationBuilder;
import consulo.fileEditor.EditorNotificationProvider;
import consulo.fileEditor.FileEditor;
import consulo.go.localize.GoLocalize;
import consulo.go.modules.GoModulesProjects;
import consulo.go.modules.externalSystem.GoModulesProjectLinker;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Offers to link the Go module of a Go source, {@code go.mod} or {@code go.work} that no linked project covers.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModulesNotLinkedNotificationProvider implements EditorNotificationProvider, DumbAware {
    private final Project myProject;

    @Inject
    public GoModulesNotLinkedNotificationProvider(Project project) {
        myProject = project;
    }

    @Override
    public String getId() {
        return "go-modules-not-linked";
    }

    @RequiredReadAction
    @Override
    public @Nullable EditorNotificationBuilder buildNotification(VirtualFile file,
                                                                 FileEditor fileEditor,
                                                                 Supplier<EditorNotificationBuilder> builderFactory) {
        if (!(GoConstants.GO.equals(file.getFileType().getId()) || GoModulesProjects.isSettingsFile(file))
            || !GoModulesProjects.isProjectFile(myProject, file)) {
            return null;
        }
        VirtualFile root = GoModulesProjects.findProjectRoot(file);
        if (root == null || GoModulesProjects.isLinked(myProject, root)) {
            return null;
        }
        VirtualFile moduleRoot = GoModulesProjects.findModuleRoot(file);
        if (moduleRoot != null && GoModulesProjects.isLinked(myProject, moduleRoot)) {
            return null;
        }

        EditorNotificationBuilder builder = builderFactory.get();
        builder.withText(GoLocalize.goModulesNotificationNotLinked(root.getPresentableUrl()));
        builder.withAction(GoLocalize.goModulesNotificationLink(), e -> GoModulesProjectLinker.link(myProject, root.getPath(), null));
        return builder;
    }
}

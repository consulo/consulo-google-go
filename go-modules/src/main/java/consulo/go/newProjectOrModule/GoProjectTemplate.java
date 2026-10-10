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

package consulo.go.newProjectOrModule;

import consulo.go.icon.GoIconGroup;
import consulo.go.localize.GoLocalize;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;

import java.util.List;
import java.util.function.Supplier;

/**
 * The kinds of project the New Project wizard creates. Every one is a buildable Go module (or workspace) with tests or
 * examples, using the standard library only.
 * <p>
 * Templates live in {@code project-templates/go/<id>/}; a target path may use the placeholders of
 * {@link GoProjectGenerator}.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public enum GoProjectTemplate {
    CONSOLE("console",
        GoLocalize::goNewProjectConsole,
        PlatformIconGroup::runconfigurationsApplication,
        List.of(new File("main.go", "main.go"), new File("main_test.go", "main_test.go")),
        List.of(""),
        "main.go"),
    LIBRARY("library",
        GoLocalize::goNewProjectLibrary,
        PlatformIconGroup::nodesPplib,
        List.of(new File("library.go", "${PACKAGE}.go"),
            new File("library_test.go", "${PACKAGE}_test.go"),
            new File("example_test.go", "example_test.go")),
        List.of(""),
        "${PACKAGE}.go"),
    WEB_SERVER("web",
        GoLocalize::goNewProjectWebServer,
        PlatformIconGroup::generalWeb,
        List.of(new File("main.go", "main.go"), new File("handlers.go", "handlers.go"), new File("handlers_test.go", "handlers_test.go")),
        List.of(""),
        "handlers.go"),
    COMMAND_LINE_TOOL("cli",
        GoLocalize::goNewProjectCommandLineTool,
        PlatformIconGroup::generalExternaltools,
        List.of(new File("main.go", "cmd/${COMMAND}/main.go"),
            new File("app.go", "internal/app/app.go"),
            new File("app_test.go", "internal/app/app_test.go")),
        List.of(""),
        "cmd/${COMMAND}/main.go"),
    WORKSPACE("workspace",
        GoLocalize::goNewProjectWorkspace,
        GoIconGroup::gomod,
        List.of(new File("app/main.go", "app/main.go"),
            new File("greeter/greeter.go", "greeter/greeter.go"),
            new File("greeter/greeter_test.go", "greeter/greeter_test.go")),
        List.of("app", "greeter"),
        "app/main.go");

    /**
     * @param resource the template, relative to the directory of the project kind
     * @param target   where it goes, relative to the project directory
     */
    public record File(String resource, String target) {
    }

    private final String myId;
    private final Supplier<LocalizeValue> myDisplayName;
    private final Supplier<Image> myIcon;
    private final List<File> myFiles;
    private final List<String> myModuleDirectories;
    private final String myMainFile;

    GoProjectTemplate(String id,
                      Supplier<LocalizeValue> displayName,
                      Supplier<Image> icon,
                      List<File> files,
                      List<String> moduleDirectories,
                      String mainFile) {
        myId = id;
        myDisplayName = displayName;
        myIcon = icon;
        myFiles = files;
        myModuleDirectories = moduleDirectories;
        myMainFile = mainFile;
    }

    public String getId() {
        return myId;
    }

    public LocalizeValue getDisplayName() {
        return myDisplayName.get();
    }

    public Image getIcon() {
        return myIcon.get();
    }

    public List<File> getFiles() {
        return myFiles;
    }

    /**
     * @return the directories holding a {@code go.mod}, relative to the project directory; {@code ""} is the project directory
     */
    public List<String> getModuleDirectories() {
        return myModuleDirectories;
    }

    /**
     * @return whether the modules are tied together by a {@code go.work} in the project directory
     */
    public boolean isWorkspace() {
        return myModuleDirectories.size() > 1 || !myModuleDirectories.get(0).isEmpty();
    }

    /**
     * @return the file opened once the project is created, relative to the project directory
     */
    public String getMainFile() {
        return myMainFile;
    }
}

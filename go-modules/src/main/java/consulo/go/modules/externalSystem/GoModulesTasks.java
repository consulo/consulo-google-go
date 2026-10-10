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

import consulo.go.localize.GoLocalize;
import consulo.localize.LocalizeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The tasks every Go module offers. A task name is the {@code go} sub-command to run, so the tool window can also run
 * anything typed by the user, e.g. {@code list -m all}.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoModulesTasks {
    public static final String BUILD = "build";
    public static final String TEST = "test";
    public static final String VET = "vet";
    public static final String GENERATE = "generate";
    public static final String FMT = "fmt";
    public static final String INSTALL = "install";
    public static final String MOD_TIDY = "mod tidy";
    public static final String MOD_DOWNLOAD = "mod download";
    public static final String MOD_VENDOR = "mod vendor";
    public static final String MOD_VERIFY = "mod verify";
    public static final String WORK_SYNC = "work sync";

    public static final String ALL_PACKAGES = "./...";

    /**
     * Commands that take packages; without explicit arguments they get every package of the module.
     */
    private static final Set<String> PACKAGE_COMMANDS = Set.of(BUILD, TEST, VET, GENERATE, FMT, INSTALL);

    public static final List<String> MODULE_TASKS = List.of(BUILD, TEST, VET, GENERATE, FMT, INSTALL, MOD_TIDY, MOD_DOWNLOAD, MOD_VENDOR, MOD_VERIFY);

    private GoModulesTasks() {
    }

    public static List<String> arguments(String taskName, List<String> parameters) {
        List<String> arguments = new ArrayList<>(List.of(taskName.trim().split("\\s+")));
        arguments.addAll(parameters);
        if (parameters.isEmpty() && PACKAGE_COMMANDS.contains(taskName)) {
            arguments.add(ALL_PACKAGES);
        }
        return arguments;
    }

    public static LocalizeValue group(String taskName) {
        if (taskName.startsWith("mod ")) {
            return GoLocalize.goModulesTaskGroupModules();
        }
        if (taskName.startsWith("work ")) {
            return GoLocalize.goModulesTaskGroupWorkspace();
        }
        return GoLocalize.goModulesTaskGroupBuild();
    }

    public static LocalizeValue description(String taskName) {
        return switch (taskName) {
            case BUILD -> GoLocalize.goModulesTaskBuildDescription();
            case TEST -> GoLocalize.goModulesTaskTestDescription();
            case VET -> GoLocalize.goModulesTaskVetDescription();
            case GENERATE -> GoLocalize.goModulesTaskGenerateDescription();
            case FMT -> GoLocalize.goModulesTaskFmtDescription();
            case INSTALL -> GoLocalize.goModulesTaskInstallDescription();
            case MOD_TIDY -> GoLocalize.goModulesTaskModTidyDescription();
            case MOD_DOWNLOAD -> GoLocalize.goModulesTaskModDownloadDescription();
            case MOD_VENDOR -> GoLocalize.goModulesTaskModVendorDescription();
            case MOD_VERIFY -> GoLocalize.goModulesTaskModVerifyDescription();
            case WORK_SYNC -> GoLocalize.goModulesTaskWorkSyncDescription();
            default -> LocalizeValue.of("go " + taskName);
        };
    }
}

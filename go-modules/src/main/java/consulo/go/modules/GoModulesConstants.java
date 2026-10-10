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

package consulo.go.modules;

import consulo.externalSystem.model.ProjectSystemId;
import consulo.go.icon.GoIconGroup;
import consulo.go.localize.GoLocalize;

import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public interface GoModulesConstants {
    ProjectSystemId SYSTEM_ID = new ProjectSystemId("GO_MODULES", GoLocalize.goModulesSystemName(), GoIconGroup.gomod());

    String GO_MOD = "go.mod";
    String GO_SUM = "go.sum";
    String GO_WORK = "go.work";
    String GO_WORK_SUM = "go.work.sum";

    String VENDOR = "vendor";
    String VENDOR_MODULES_TXT = "modules.txt";

    Set<String> SETTINGS_FILE_NAMES = Set.of(GO_MOD, GO_SUM, GO_WORK, GO_WORK_SUM);
}

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

package consulo.go.modules.module;

import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionImpl;
import consulo.externalSystem.util.ExternalSystemConstants;
import consulo.go.modules.GoModulesConstants;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModuleRootLayer;
import org.jspecify.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Marks a module as imported from a Go module and remembers which one.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModulesModuleExtension extends ExternalSystemModuleExtensionImpl {
    public static final String MODULE_PATH_KEY = "go.module.path";
    public static final String GO_MOD_KEY = "go.mod.file";
    public static final String GO_VERSION_KEY = "go.version";
    public static final String WORK_FILE_KEY = "go.work.file";

    public GoModulesModuleExtension(String id, ModuleRootLayer rootLayer) {
        super(id, rootLayer, GoModulesConstants.SYSTEM_ID);
    }

    public static @Nullable GoModulesModuleExtension find(@Nullable Module module) {
        if (module == null || module.isDisposed()) {
            return null;
        }
        GoModulesModuleExtension extension = ModuleRootManager.getInstance(module).getExtension(GoModulesModuleExtension.class);
        return extension != null && extension.getModulePath() != null ? extension : null;
    }

    /**
     * @return the module path declared in {@code go.mod}, the prefix of every import path of its packages
     */
    public @Nullable String getModulePath() {
        return getOption(MODULE_PATH_KEY);
    }

    public @Nullable String getGoModFile() {
        return getOption(GO_MOD_KEY);
    }

    public @Nullable String getGoVersion() {
        return getOption(GO_VERSION_KEY);
    }

    /**
     * @return the {@code go.work} this module was imported through, {@code null} for a module imported on its own
     */
    public @Nullable String getWorkFile() {
        return getOption(WORK_FILE_KEY);
    }

    /**
     * @return the linked project this module was imported from
     */
    public @Nullable String getRootProjectPath() {
        return getOption(ExternalSystemConstants.ROOT_PROJECT_PATH_KEY);
    }

    /**
     * @return the directory of the {@code go.mod}, the root of the module
     */
    public @Nullable Path getModuleDirectory() {
        String goMod = getGoModFile();
        if (goMod == null) {
            return null;
        }
        try {
            return Path.of(goMod).getParent();
        }
        catch (InvalidPathException e) {
            return null;
        }
    }

    boolean hasSameOptions(GoModulesModuleExtension other) {
        return myOptions.equals(other.myOptions);
    }
}

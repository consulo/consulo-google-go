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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonStreamParser;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads what the {@code go} command prints with {@code -json}. Several commands print a stream of objects rather than
 * one array, so everything goes through {@link JsonStreamParser}.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoJson {
    /**
     * An entry of {@code go list -m -json}, or of {@code go mod download -json}.
     */
    public record Module(String path,
                         @Nullable String version,
                         boolean main,
                         boolean indirect,
                         @Nullable String dir,
                         @Nullable String goMod,
                         @Nullable String goVersion,
                         @Nullable Module replace,
                         @Nullable String error) {
        /**
         * @return the directory holding the sources of the module, after replacement
         */
        public @Nullable String sourceDir() {
            if (dir != null) {
                return dir;
            }
            return replace != null ? replace.dir() : null;
        }

        public String key() {
            return version == null ? path : path + "@" + version;
        }

        public Module withDir(String newDir) {
            return new Module(path, version, main, indirect, newDir, goMod, goVersion, replace, error);
        }
    }

    public record Require(String path, @Nullable String version, boolean indirect) {
    }

    /**
     * A {@code go.mod} as printed by {@code go mod edit -json}.
     */
    public record ModFile(@Nullable String modulePath, @Nullable String goVersion, @Nullable String toolchain, List<Require> requires) {
    }

    /**
     * The variables of {@code go env -json} the importer needs.
     */
    public record Env(@Nullable String goRoot,
                      @Nullable String goPath,
                      @Nullable String goModCache,
                      @Nullable String goWork,
                      @Nullable String goMod,
                      @Nullable String goVersion,
                      @Nullable String goFlags) {
        public static final List<String> NAMES = List.of("GOROOT", "GOPATH", "GOMODCACHE", "GOWORK", "GOMOD", "GOVERSION", "GOFLAGS");

        /**
         * @return the {@code go.work} in use, {@code null} outside workspace mode
         */
        public @Nullable String workFile() {
            return goWork == null || goWork.isEmpty() || "off".equals(goWork) ? null : goWork;
        }

        /**
         * @return the {@code go.mod} of the main module, {@code null} when there is none
         */
        public @Nullable String modFile() {
            if (goMod == null || goMod.isEmpty() || "/dev/null".equals(goMod) || "NUL".equalsIgnoreCase(goMod)) {
                return null;
            }
            return goMod;
        }
    }

    private GoJson() {
    }

    public static List<JsonObject> parseObjects(String text) throws JsonParseException {
        List<JsonObject> result = new ArrayList<>();
        if (text.isBlank()) {
            return result;
        }
        JsonStreamParser parser = new JsonStreamParser(text);
        while (parser.hasNext()) {
            JsonElement element = parser.next();
            if (element.isJsonObject()) {
                result.add(element.getAsJsonObject());
            }
        }
        return result;
    }

    public static List<Module> parseModules(String text) throws JsonParseException {
        List<Module> modules = new ArrayList<>();
        for (JsonObject object : parseObjects(text)) {
            Module module = toModule(object);
            if (module != null) {
                modules.add(module);
            }
        }
        return modules;
    }

    public static ModFile parseModFile(String text) throws JsonParseException {
        List<JsonObject> objects = parseObjects(text);
        if (objects.isEmpty()) {
            return new ModFile(null, null, null, List.of());
        }
        JsonObject object = objects.get(0);
        JsonObject module = object(object, "Module");
        List<Require> requires = new ArrayList<>();
        JsonArray requireArray = array(object, "Require");
        if (requireArray != null) {
            for (JsonElement element : requireArray) {
                if (element.isJsonObject()) {
                    JsonObject require = element.getAsJsonObject();
                    String path = string(require, "Path");
                    if (path != null) {
                        requires.add(new Require(path, string(require, "Version"), bool(require, "Indirect")));
                    }
                }
            }
        }
        return new ModFile(module != null ? string(module, "Path") : null, string(object, "Go"), string(object, "Toolchain"), requires);
    }

    public static Env parseEnv(String text) throws JsonParseException {
        List<JsonObject> objects = parseObjects(text);
        JsonObject object = objects.isEmpty() ? new JsonObject() : objects.get(0);
        return new Env(string(object, "GOROOT"),
            string(object, "GOPATH"),
            string(object, "GOMODCACHE"),
            string(object, "GOWORK"),
            string(object, "GOMOD"),
            string(object, "GOVERSION"),
            string(object, "GOFLAGS"));
    }

    private static @Nullable Module toModule(JsonObject object) {
        String path = string(object, "Path");
        if (path == null) {
            return null;
        }
        JsonObject replace = object(object, "Replace");
        JsonObject error = object(object, "Error");
        String errorText = error != null ? string(error, "Err") : string(object, "Error");
        return new Module(path,
            string(object, "Version"),
            bool(object, "Main"),
            bool(object, "Indirect"),
            string(object, "Dir"),
            string(object, "GoMod"),
            string(object, "GoVersion"),
            replace != null ? toModule(replace) : null,
            errorText);
    }

    private static @Nullable JsonObject object(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static @Nullable JsonArray array(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    private static @Nullable String string(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    private static boolean bool(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && element.isJsonPrimitive() && element.getAsBoolean();
    }
}

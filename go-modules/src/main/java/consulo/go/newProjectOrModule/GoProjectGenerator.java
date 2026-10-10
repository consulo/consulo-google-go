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

import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.GoToolchain;
import consulo.logging.Logger;
import consulo.process.ExecutionException;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lays a {@link GoProjectTemplate} out on disk. The sources come from templates; {@code go.mod} and {@code go.work} are
 * written by {@code go mod init} and {@code go work init}, so they carry the language version of the toolchain. Without
 * a {@code go} binary they are written directly.
 * <p>
 * Placeholders of templates and target paths: {@code ${NAME}} the project name, {@code ${MODULE_PATH}} the module path,
 * {@code ${PACKAGE}} the package name a library gets, {@code ${COMMAND}} the name of a command.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoProjectGenerator {
    private static final Logger LOG = Logger.getInstance(GoProjectGenerator.class);

    private static final String TEMPLATES = "project-templates/go/";
    private static final String TEMPLATE_SUFFIX = ".template";

    /**
     * The {@code go} directive written when neither the toolchain nor the SDK tell a version.
     */
    public static final String DEFAULT_GO_VERSION = "1.22";

    private static final Pattern MODULE_PATH_ELEMENT = Pattern.compile("[A-Za-z0-9_~-]([A-Za-z0-9._~-]*[A-Za-z0-9_~-])?");
    private static final Pattern MAJOR_VERSION = Pattern.compile("v[0-9]+");
    private static final Pattern GO_LANGUAGE_VERSION = Pattern.compile("(\\d+\\.\\d+)");
    private static final Set<String> GO_KEYWORDS = Set.of("break", "case", "chan", "const", "continue", "default", "defer", "else",
        "fallthrough", "for", "func", "go", "goto", "if", "import", "interface", "map", "package", "range", "return", "select",
        "struct", "switch", "type", "var");

    private GoProjectGenerator() {
    }

    /**
     * Writes the sources of {@code template} into {@code directory}.
     *
     * @return the file to open once the project is created
     */
    public static Path writeSources(GoProjectTemplate template, Path directory, String name, String modulePath) throws IOException {
        Map<String, String> variables = variables(name, modulePath);
        ClassLoader loader = GoProjectGenerator.class.getClassLoader();
        for (GoProjectTemplate.File file : template.getFiles()) {
            String resource = TEMPLATES + template.getId() + "/" + file.resource() + TEMPLATE_SUFFIX;
            Path target = directory.resolve(substitute(file.target(), variables));
            Files.createDirectories(target.getParent());
            Files.writeString(target, substitute(read(loader, resource), variables), StandardCharsets.UTF_8);
        }
        return directory.resolve(substitute(template.getMainFile(), variables));
    }

    /**
     * Creates the {@code go.mod} of every module of {@code template}, and the {@code go.work} of a workspace, unless they
     * exist already.
     *
     * @param toolchain the {@code go} to run, {@code null} to write the files directly
     * @param goVersion the {@code go} directive written when {@code go} does not run
     */
    public static void initModules(GoProjectTemplate template,
                                   Path directory,
                                   String modulePath,
                                   @Nullable GoToolchain toolchain,
                                   @Nullable String goVersion) throws IOException {
        String version = goVersion != null ? goVersion : DEFAULT_GO_VERSION;
        List<String> uses = new ArrayList<>();
        for (String moduleDirectory : template.getModuleDirectories()) {
            Path moduleRoot = moduleDirectory.isEmpty() ? directory : directory.resolve(moduleDirectory);
            String path = moduleDirectory.isEmpty() ? modulePath : modulePath + "/" + moduleDirectory;
            uses.add("./" + moduleDirectory);
            if (Files.exists(moduleRoot.resolve(GoModulesConstants.GO_MOD))) {
                continue;
            }
            Files.createDirectories(moduleRoot);
            if (!runGo(toolchain, moduleRoot, List.of("mod", "init", path))) {
                String goMod = "module " + path + "\n\ngo " + version + "\n";
                Files.writeString(moduleRoot.resolve(GoModulesConstants.GO_MOD), goMod, StandardCharsets.UTF_8);
            }
        }

        if (!template.isWorkspace() || Files.exists(directory.resolve(GoModulesConstants.GO_WORK))) {
            return;
        }
        List<String> arguments = new ArrayList<>(List.of("work", "init"));
        arguments.addAll(uses);
        if (!runGo(toolchain, directory, arguments)) {
            StringBuilder goWork = new StringBuilder("go ").append(version).append("\n\nuse (\n");
            for (String use : uses) {
                goWork.append('\t').append(use).append('\n');
            }
            goWork.append(")\n");
            Files.writeString(directory.resolve(GoModulesConstants.GO_WORK), goWork.toString(), StandardCharsets.UTF_8);
        }
    }

    private static boolean runGo(@Nullable GoToolchain toolchain, Path directory, List<String> arguments) {
        if (toolchain == null) {
            return false;
        }
        try {
            ProcessOutput output = CapturingProcessUtil.execAndGetOutput(toolchain.createCommandLine(directory, arguments));
            if (output.getExitCode() == 0) {
                return true;
            }
            LOG.warn("go " + String.join(" ", arguments) + " failed in " + directory + ": " + output.getStderr());
        }
        catch (ExecutionException e) {
            LOG.warn("Cannot run go " + String.join(" ", arguments) + " in " + directory, e);
        }
        return false;
    }

    /**
     * @return {@code major.minor} of an SDK version, the form of a {@code go} directive
     */
    public static @Nullable String toLanguageVersion(@Nullable String sdkVersion) {
        Matcher matcher = sdkVersion != null ? GO_LANGUAGE_VERSION.matcher(sdkVersion) : null;
        return matcher != null && matcher.find() ? matcher.group(1) : null;
    }

    /**
     * @return a valid module path made of a project name, or of a module path the user mistyped
     */
    public static String toModulePath(String name) {
        String cleaned = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._~/-]+", "-");
        List<String> elements = new ArrayList<>();
        for (String element : cleaned.split("/")) {
            String trimmed = element.replaceAll("^[.-]+|[.-]+$", "");
            if (!trimmed.isEmpty()) {
                elements.add(trimmed);
            }
        }
        return elements.isEmpty() ? "app" : String.join("/", elements);
    }

    /**
     * Checks the rules of {@code go mod init} the user is likely to break.
     */
    public static boolean isValidModulePath(String modulePath) {
        if (modulePath.isEmpty()) {
            return false;
        }
        for (String element : modulePath.split("/", -1)) {
            if (!MODULE_PATH_ELEMENT.matcher(element).matches()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the package name {@code go} expects for the module path: its last element, without a major version suffix
     */
    public static String toPackageName(String modulePath) {
        String element = lastElement(modulePath);
        StringBuilder builder = new StringBuilder();
        for (char c : element.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_') {
                builder.append(c);
            }
        }
        String name = builder.toString();
        if (name.isEmpty()) {
            return "lib";
        }
        if (Character.isDigit(name.charAt(0))) {
            name = "p" + name;
        }
        return GO_KEYWORDS.contains(name) ? name + "pkg" : name;
    }

    /**
     * @return the name a command of the module path is built as: its last element
     */
    public static String toCommandName(String modulePath) {
        String name = lastElement(modulePath).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]+", "-");
        return name.isEmpty() ? "app" : name;
    }

    private static String lastElement(String modulePath) {
        String[] elements = modulePath.split("/");
        String last = elements[elements.length - 1];
        if (elements.length > 1 && MAJOR_VERSION.matcher(last).matches()) {
            last = elements[elements.length - 2];
        }
        return last;
    }

    private static Map<String, String> variables(String name, String modulePath) {
        return Map.of("NAME", name,
            "MODULE_PATH", modulePath,
            "PACKAGE", toPackageName(modulePath),
            "COMMAND", toCommandName(modulePath));
    }

    private static String substitute(String text, Map<String, String> variables) {
        String result = text;
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            result = result.replace("${" + variable.getKey() + "}", variable.getValue());
        }
        return result;
    }

    private static String read(ClassLoader loader, String resource) throws IOException {
        try (InputStream stream = loader.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("Template " + resource + " not found");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

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

package consulo.go.it;

import com.goide.psi.GoFile;
import com.goide.psi.GoImportSpec;
import consulo.application.ReadAction;
import consulo.content.bundle.Sdk;
import consulo.go.modules.GoToolchain;
import consulo.go.modules.module.GoModulesModuleExtension;
import consulo.go.newProjectOrModule.GoProjectGenerator;
import consulo.go.newProjectOrModule.GoProjectTemplate;
import consulo.it.AllowWriteLockUnderUIThread;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Every project kind of the New Project wizard must come out as a Go project that builds, passes its tests and imports
 * with every import of its main file resolved.
 */
@ExtendWith(HeadlessProjectExtension.class)
@AllowWriteLockUnderUIThread
public class GoProjectTemplatesTest {
    private static final String MODULE_PATH = "example.com/demo";

    @Test
    public void everyTemplateBuildsTestsAndImports(HeadlessProjects projects) throws Exception {
        assumeTrue(GoModulesFixtures.isGoAvailable(), "go is not on PATH");
        GoToolchain toolchain = GoToolchain.fromPath();
        Set<Sdk> sdksBefore = GoModulesFixtures.goSdks();
        try {
            for (GoProjectTemplate template : GoProjectTemplate.values()) {
                checkTemplate(projects, template, toolchain);
            }
        }
        finally {
            GoModulesFixtures.removeSdksAddedSince(sdksBefore);
        }
    }

    private static void checkTemplate(HeadlessProjects projects, GoProjectTemplate template, GoToolchain toolchain) throws Exception {
        Path directory = projects.newDirectory();
        Path mainFile = GoProjectGenerator.writeSources(template, directory, "Demo", MODULE_PATH);
        GoProjectGenerator.initModules(template, directory, MODULE_PATH, toolchain, null);
        assertThat(mainFile).as(template + " main file").exists();
        assertThat(Files.exists(directory.resolve("go.work"))).as(template + " go.work").isEqualTo(template.isWorkspace());

        Map<String, String> expectedModules = new LinkedHashMap<>();
        for (String moduleDirectory : template.getModuleDirectories()) {
            Path moduleRoot = moduleDirectory.isEmpty() ? directory : directory.resolve(moduleDirectory);
            String modulePath = moduleDirectory.isEmpty() ? MODULE_PATH : MODULE_PATH + "/" + moduleDirectory;
            assertThat(Files.readString(moduleRoot.resolve("go.mod"))).as(template + " go.mod").contains("module " + modulePath);
            expectedModules.put(moduleRoot.toString(), modulePath);

            assertGo(template, toolchain, moduleRoot, "vet", "./...");
            assertGo(template, toolchain, moduleRoot, "test", "./...");
        }

        Project project = projects.open(directory);
        try {
            GoModulesFixtures.importProject(project, directory);

            Map<String, String> importedModules = ReadAction.compute(() -> {
                Map<String, String> result = new LinkedHashMap<>();
                for (Module module : ModuleManager.getInstance(project).getModules()) {
                    GoModulesModuleExtension extension = GoModulesModuleExtension.find(module);
                    if (extension != null) {
                        result.put(String.valueOf(extension.getModuleDirectory()), extension.getModulePath());
                    }
                }
                return result;
            });
            assertThat(importedModules).as(template + " modules").containsExactlyInAnyOrderEntriesOf(expectedModules);

            VirtualFile main = GoModulesFixtures.find(mainFile);
            assertThat(main).isNotNull();
            List<String> unresolved = ReadAction.compute(() -> {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(main);
                assertThat(psiFile).isInstanceOf(GoFile.class);
                List<String> result = new ArrayList<>();
                for (GoImportSpec importSpec : ((GoFile) psiFile).getImports()) {
                    PsiDirectory target = importSpec.getImportString().resolve();
                    if (target == null) {
                        result.add(importSpec.getPath());
                    }
                }
                return result;
            });
            assertThat(unresolved).as(template + ": imports of " + mainFile.getFileName() + " that do not resolve").isEmpty();
        }
        finally {
            projects.close(project);
        }
    }

    private static void assertGo(GoProjectTemplate template, GoToolchain toolchain, Path directory, String... arguments) throws Exception {
        ProcessOutput output = CapturingProcessUtil.execAndGetOutput(toolchain.createCommandLine(directory, List.of(arguments)));
        assertThat(output.getExitCode())
            .as(template + ": go " + String.join(" ", arguments) + " in " + directory + "\n" + output.getStdout() + output.getStderr())
            .isZero();
    }

    @Test
    public void withoutGoTheModuleFilesAreWrittenDirectly() throws Exception {
        Path directory = Files.createTempDirectory("consulo-go-it-template");
        GoProjectGenerator.writeSources(GoProjectTemplate.WORKSPACE, directory, "Demo", MODULE_PATH);
        GoProjectGenerator.initModules(GoProjectTemplate.WORKSPACE, directory, MODULE_PATH, null, "1.21");

        assertThat(Files.readString(directory.resolve("app/go.mod"))).isEqualTo("module example.com/demo/app\n\ngo 1.21\n");
        assertThat(Files.readString(directory.resolve("greeter/go.mod"))).isEqualTo("module example.com/demo/greeter\n\ngo 1.21\n");
        assertThat(Files.readString(directory.resolve("go.work"))).isEqualTo("go 1.21\n\nuse (\n\t./app\n\t./greeter\n)\n");
    }

    @Test
    public void namesBecomeValidModulePathsPackagesAndCommands() {
        assertThat(GoProjectGenerator.toModulePath("My Cool App")).isEqualTo("my-cool-app");
        assertThat(GoProjectGenerator.toModulePath("  ")).isEqualTo("app");
        assertThat(GoProjectGenerator.toModulePath("bad path//x")).as("a mistyped module path is cleaned").isEqualTo("bad-path/x");
        assertThat(GoProjectGenerator.isValidModulePath(GoProjectGenerator.toModulePath("a/./b../-c-"))).isTrue();
        assertThat(GoProjectGenerator.isValidModulePath(GoProjectGenerator.toModulePath("Ünïcode Næme!"))).isTrue();

        assertThat(GoProjectGenerator.isValidModulePath("github.com/acme/tool")).isTrue();
        assertThat(GoProjectGenerator.isValidModulePath("hello-app")).isTrue();
        assertThat(GoProjectGenerator.isValidModulePath("has space")).isFalse();
        assertThat(GoProjectGenerator.isValidModulePath("/leading")).isFalse();
        assertThat(GoProjectGenerator.isValidModulePath("trailing/")).isFalse();
        assertThat(GoProjectGenerator.isValidModulePath("a//b")).isFalse();
        assertThat(GoProjectGenerator.isValidModulePath("dot.")).isFalse();

        assertThat(GoProjectGenerator.toPackageName("github.com/acme/string-utils/v2")).isEqualTo("stringutils");
        assertThat(GoProjectGenerator.toPackageName("example.com/2fast")).isEqualTo("p2fast");
        assertThat(GoProjectGenerator.toPackageName("example.com/go")).isEqualTo("gopkg");
        assertThat(GoProjectGenerator.toCommandName("github.com/acme/word-count")).isEqualTo("word-count");

        assertThat(GoProjectGenerator.toLanguageVersion("1.27.1")).isEqualTo("1.27");
        assertThat(GoProjectGenerator.toLanguageVersion("devel")).isNull();
    }
}

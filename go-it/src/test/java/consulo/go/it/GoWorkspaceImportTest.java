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

import com.goide.util.GoPackageUtil;
import com.goide.psi.GoFile;
import com.goide.psi.GoImportSpec;
import consulo.application.ReadAction;
import consulo.content.bundle.Sdk;
import consulo.go.module.extension.GoModuleExtension;
import consulo.go.modules.GoImportPathMap;
import consulo.go.modules.externalSystem.GoModulesAutoImportAware;
import consulo.go.modules.module.GoModulesModuleExtension;
import consulo.go.modules.orderEntry.GoDependencyOrderEntryModel;
import consulo.it.AllowWriteLockUnderUIThread;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.junit.jupiter.api.BeforeEach;
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
 * Imports {@code fixtures/workspace}, a {@code go.work} using three modules:
 * <ul>
 * <li>{@code app} requires {@code lib} and the third-party {@code example.org/greeting};</li>
 * <li>{@code lib} requires {@code example.org/greeting};</li>
 * <li>{@code tools} requires nothing.</li>
 * </ul>
 * {@code example.org/greeting} comes from {@code third_party/greeting} through a {@code replace} of the {@code go.work},
 * and {@code examples} has a {@code go.mod} the workspace does not use.
 */
@ExtendWith(HeadlessProjectExtension.class)
@AllowWriteLockUnderUIThread
public class GoWorkspaceImportTest {
    private Set<Sdk> mySdksBefore = Set.of();

    @BeforeEach
    public void requireGo() {
        assumeTrue(GoModulesFixtures.isGoAvailable(), "go is not on PATH");
        mySdksBefore = GoModulesFixtures.goSdks();
    }

    @Test
    public void everyUsedModuleBecomesAModuleWithItsGoModule(HeadlessProjects projects) throws Exception {
        Path root = GoModulesFixtures.copy("workspace", projects.newDirectory());
        Project project = projects.open(root);
        try {
            GoModulesFixtures.importProject(project, root);

            assertThat(GoModulesFixtures.moduleNames(project))
                .as("every use of go.work must be a module, and only those")
                .contains("app", "lib", "tools")
                .doesNotContain("examples", "greeting");

            Map<String, String> modulePaths = new LinkedHashMap<>();
            for (String name : List.of("app", "lib", "tools")) {
                Module module = GoModulesFixtures.module(project, name);
                ReadAction.run(() -> {
                    GoModulesModuleExtension extension = GoModulesModuleExtension.find(module);
                    assertThat(extension).as(name + " must be marked as imported from a Go module").isNotNull();
                    modulePaths.put(name, extension.getModulePath());
                    assertThat(extension.getModuleDirectory()).isEqualTo(root.resolve(name));
                    assertThat(extension.getWorkFile()).isEqualTo(root.resolve("go.work").toString());

                    GoModuleExtension goExtension = ModuleUtilCore.getExtension(module, GoModuleExtension.class);
                    assertThat(goExtension).as(name + " must be a Go module").isNotNull();
                    assertThat(goExtension.getSdk()).as(name + " must get the Go SDK of the go command").isNotNull();

                    VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
                    assertThat(contentRoots).extracting(VirtualFile::getPath).containsExactly(root.resolve(name).toString());
                    assertThat(ModuleRootManager.getInstance(module).getSourceRoots()).hasSize(1);
                });
            }
            assertThat(modulePaths).containsExactlyInAnyOrderEntriesOf(Map.of(
                "app", "example.com/app",
                "lib", "example.com/lib",
                "tools", "example.com/tools"));
        }
        finally {
            projects.close(project);
            GoModulesFixtures.removeSdksAddedSince(mySdksBefore);
        }
    }

    @Test
    public void requirementsBecomeModuleAndGoDependencies(HeadlessProjects projects) throws Exception {
        Path root = GoModulesFixtures.copy("workspace", projects.newDirectory());
        Project project = projects.open(root);
        try {
            GoModulesFixtures.importProject(project, root);

            Module app = GoModulesFixtures.module(project, "app");
            Module lib = GoModulesFixtures.module(project, "lib");
            Module tools = GoModulesFixtures.module(project, "tools");

            assertThat(ReadAction.compute(() -> ModuleRootManager.getInstance(app).getDependencyModuleNames()))
                .as("app requires example.com/lib, a module of the workspace")
                .containsExactly("lib");
            assertThat(ReadAction.compute(() -> ModuleRootManager.getInstance(lib).getDependencyModuleNames())).isEmpty();

            for (Module module : List.of(app, lib)) {
                List<GoDependencyOrderEntryModel> dependencies = GoModulesFixtures.dependencies(module);
                assertThat(dependencies)
                    .as(module.getName() + " requires example.org/greeting")
                    .extracting(GoDependencyOrderEntryModel::getModulePath)
                    .containsExactly("example.org/greeting");
                GoDependencyOrderEntryModel greeting = dependencies.get(0);
                assertThat(greeting.getVersion()).isEqualTo("v1.0.0");
                assertThat(greeting.getReplacement()).isEqualTo("./third_party/greeting");
                VirtualFile directory = ReadAction.compute(greeting::findDirectory);
                assertThat(directory).as("the replacement directory is the root of the dependency").isNotNull();
                assertThat(directory.getPath()).isEqualTo(root.resolve("third_party/greeting").toString());
            }
            assertThat(GoModulesFixtures.dependencies(tools)).as("tools requires nothing").isEmpty();
        }
        finally {
            projects.close(project);
            GoModulesFixtures.removeSdksAddedSince(mySdksBefore);
        }
    }

    @Test
    public void importPathsResolveThroughTheModules(HeadlessProjects projects) throws Exception {
        Path root = GoModulesFixtures.copy("workspace", projects.newDirectory());
        Project project = projects.open(root);
        try {
            GoModulesFixtures.importProject(project, root);

            Module app = GoModulesFixtures.module(project, "app");
            ReadAction.run(() -> {
                GoImportPathMap map = GoImportPathMap.forModule(app);
                assertThat(map).isNotNull();

                assertThat(path(map.findDirectory("example.com/lib/util"))).isEqualTo(root.resolve("lib/util").toString());
                assertThat(path(map.findDirectory("example.com/app/internal/config"))).isEqualTo(root.resolve("app/internal/config").toString());
                assertThat(path(map.findDirectory("example.org/greeting"))).isEqualTo(root.resolve("third_party/greeting").toString());
                assertThat(path(map.findDirectory("example.com/tools/cmd/lint")))
                    .as("every module of a workspace is visible from the others")
                    .isEqualTo(root.resolve("tools/cmd/lint").toString());
                assertThat(map.findDirectory("fmt")).as("the standard library resolves through the SDK").isNull();
                assertThat(map.findDirectory("example.com/lib/missing")).isNull();

                VirtualFile util = GoModulesFixtures.find(root.resolve("lib/util"));
                assertThat(map.getImportPath(util)).isEqualTo("example.com/lib/util");
                VirtualFile greeting = GoModulesFixtures.find(root.resolve("third_party/greeting"));
                assertThat(map.getImportPath(greeting)).isEqualTo("example.org/greeting");

                PsiDirectory utilDirectory = PsiManager.getInstance(project).findDirectory(util);
                assertThat(GoPackageUtil.getImportPath(utilDirectory, false)).isEqualTo("example.com/lib/util");
            });
        }
        finally {
            projects.close(project);
            GoModulesFixtures.removeSdksAddedSince(mySdksBefore);
        }
    }

    @Test
    public void importsOfAGoFileResolveToPackageDirectories(HeadlessProjects projects) throws Exception {
        Path root = GoModulesFixtures.copy("workspace", projects.newDirectory());
        Project project = projects.open(root);
        try {
            GoModulesFixtures.importProject(project, root);
            HeadlessProjects.awaitIdle(project);

            VirtualFile main = GoModulesFixtures.find(root.resolve("app/main.go"));
            assertThat(main).isNotNull();
            Map<String, String> resolved = ReadAction.compute(() -> {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(main);
                assertThat(psiFile).isInstanceOf(GoFile.class);
                Map<String, String> result = new LinkedHashMap<>();
                for (GoImportSpec importSpec : ((GoFile) psiFile).getImports()) {
                    PsiElement target = importSpec.getImportString().resolve();
                    result.put(importSpec.getPath(), target instanceof PsiDirectory directory ? directory.getVirtualFile().getPath() : null);
                }
                return result;
            });

            assertThat(resolved).containsEntry("example.com/lib/util", root.resolve("lib/util").toString());
            assertThat(resolved).containsEntry("example.com/app/internal/config", root.resolve("app/internal/config").toString());
            assertThat(resolved).containsEntry("example.org/greeting", root.resolve("third_party/greeting").toString());
            assertThat(resolved.get("fmt")).as("fmt resolves into the SDK sources").isNotNull().endsWith("/src/fmt");
        }
        finally {
            projects.close(project);
            GoModulesFixtures.removeSdksAddedSince(mySdksBefore);
        }
    }

    @Test
    public void reimportDropsAModuleLeavingTheWorkspaceOnly(HeadlessProjects projects) throws Exception {
        Path root = GoModulesFixtures.copy("workspace", projects.newDirectory());
        Project project = projects.open(root);
        try {
            GoModulesFixtures.importProject(project, root);
            List<String> firstImport = GoModulesFixtures.moduleNames(project);

            GoModulesFixtures.importProject(project, root);
            assertThat(GoModulesFixtures.moduleNames(project))
                .as("importing an unchanged project again changes nothing")
                .containsExactlyInAnyOrderElementsOf(firstImport);

            String goWork = Files.readString(root.resolve("go.work"));
            Files.writeString(root.resolve("go.work"), goWork.replace("\t./tools\n", ""));
            GoModulesFixtures.importProject(project, root);

            List<String> names = new ArrayList<>(GoModulesFixtures.moduleNames(project));
            assertThat(names).as("tools left the go.work").doesNotContain("tools");
            assertThat(names).contains("app", "lib");
            for (String name : firstImport) {
                if (!name.equals("tools")) {
                    assertThat(names).as("a module the import did not create must survive").contains(name);
                }
            }
        }
        finally {
            projects.close(project);
            GoModulesFixtures.removeSdksAddedSince(mySdksBefore);
        }
    }

    @Test
    public void everyGoModAndTheGoWorkAreTrackedForAutoReload(HeadlessProjects projects) throws Exception {
        Path root = GoModulesFixtures.copy("workspace", projects.newDirectory());
        Project project = projects.open(root);
        try {
            String linkedPath = GoModulesFixtures.link(project, root);
            GoModulesFixtures.importProject(project, root);

            GoModulesAutoImportAware autoImportAware = new GoModulesAutoImportAware();
            assertThat(autoImportAware.getAffectedExternalProjectFilePaths(linkedPath, project))
                .contains(root.resolve("go.work"), root.resolve("go.work.sum"))
                .contains(root.resolve("app/go.mod"), root.resolve("app/go.sum"))
                .contains(root.resolve("lib/go.mod"), root.resolve("tools/go.mod"))
                .doesNotContain(root.resolve("examples/go.mod"));

            assertThat(autoImportAware.getAffectedExternalProjectPath(root.resolve("lib/go.mod").toString(), project)).isEqualTo(linkedPath);
            assertThat(autoImportAware.getAffectedExternalProjectPath(root.resolve("go.work").toString(), project)).isEqualTo(linkedPath);
            assertThat(autoImportAware.getAffectedExternalProjectPath(root.resolve("app/main.go").toString(), project)).isNull();
            assertThat(autoImportAware.getAffectedExternalProjectPath(root.resolve("examples/go.mod").toString(), project)).isNull();
        }
        finally {
            projects.close(project);
            GoModulesFixtures.removeSdksAddedSince(mySdksBefore);
        }
    }

    private static String path(VirtualFile file) {
        return file != null ? file.getPath() : null;
    }
}

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

import consulo.application.ReadAction;
import consulo.content.bundle.Sdk;
import consulo.go.modules.GoImportPathMap;
import consulo.go.modules.module.GoModulesModuleExtension;
import consulo.go.modules.orderEntry.GoDependencyOrderEntryModel;
import consulo.it.AllowWriteLockUnderUIThread;
import consulo.it.HeadlessModules;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Imports {@code fixtures/single}: one {@code go.mod}, no {@code go.work}, with {@code example.org/greeting} replaced by
 * a directory inside the module.
 */
@ExtendWith(HeadlessProjectExtension.class)
@AllowWriteLockUnderUIThread
public class GoSingleModuleImportTest {
    private Set<Sdk> mySdksBefore = Set.of();

    @BeforeEach
    public void requireGo() {
        assumeTrue(GoModulesFixtures.isGoAvailable(), "go is not on PATH");
        mySdksBefore = GoModulesFixtures.goSdks();
    }

    @Test
    public void theModuleAlreadyOnTheDirectoryIsAdopted(HeadlessProjects projects) throws Exception {
        Path root = GoModulesFixtures.copy("single", projects.newDirectory());
        Project project = projects.open(root);
        try {
            VirtualFile rootFile = GoModulesFixtures.find(root);
            Module userModule = HeadlessModules.createModule(project, "hand-named", rootFile);

            GoModulesFixtures.importProject(project, root);

            List<Module> onRoot = modulesWithContentRoot(project, rootFile);
            assertThat(onRoot)
                .as("the import must reuse the module on the directory instead of adding a second one")
                .containsExactly(userModule);

            ReadAction.run(() -> {
                GoModulesModuleExtension extension = GoModulesModuleExtension.find(userModule);
                assertThat(extension).isNotNull();
                assertThat(extension.getModulePath()).isEqualTo("example.com/single");
                assertThat(extension.getWorkFile()).as("not a workspace").isNull();
            });
        }
        finally {
            projects.close(project);
            GoModulesFixtures.removeSdksAddedSince(mySdksBefore);
        }
    }

    @Test
    public void aReplacementInsideTheModuleIsADependency(HeadlessProjects projects) throws Exception {
        Path root = GoModulesFixtures.copy("single", projects.newDirectory());
        Project project = projects.open(root);
        try {
            GoModulesFixtures.importProject(project, root);

            VirtualFile rootFile = GoModulesFixtures.find(root);
            List<Module> onRoot = modulesWithContentRoot(project, rootFile);
            assertThat(onRoot).hasSize(1);
            Module module = onRoot.get(0);

            List<GoDependencyOrderEntryModel> dependencies = GoModulesFixtures.dependencies(module);
            assertThat(dependencies).extracting(GoDependencyOrderEntryModel::getModulePath).containsExactly("example.org/greeting");
            assertThat(dependencies.get(0).getReplacement()).isEqualTo("./third_party/greeting");

            ReadAction.run(() -> {
                GoImportPathMap map = GoImportPathMap.forModule(module);
                assertThat(map).isNotNull();
                VirtualFile greeting = map.findDirectory("example.org/greeting");
                assertThat(greeting).isNotNull();
                assertThat(greeting.getPath()).isEqualTo(root.resolve("third_party/greeting").toString());
                VirtualFile version = map.findDirectory("example.com/single/internal/version");
                assertThat(version).isNotNull();
                assertThat(map.getImportPath(greeting))
                    .as("the nested module is its own module, not a package of example.com/single")
                    .isEqualTo("example.org/greeting");
            });
        }
        finally {
            projects.close(project);
            GoModulesFixtures.removeSdksAddedSince(mySdksBefore);
        }
    }

    private static List<Module> modulesWithContentRoot(Project project, VirtualFile root) {
        return ReadAction.compute(() -> {
            List<Module> modules = new ArrayList<>();
            for (Module module : ModuleManager.getInstance(project).getModules()) {
                if (List.of(ModuleRootManager.getInstance(module).getContentRoots()).contains(root)) {
                    modules.add(module);
                }
            }
            return modules;
        });
    }
}

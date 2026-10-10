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

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.Task;
import consulo.content.bundle.Sdk;
import consulo.fileEditor.FileEditorManager;
import consulo.go.localize.GoLocalize;
import consulo.go.module.extension.GoMutableModuleExtension;
import consulo.go.modules.GoToolchain;
import consulo.go.modules.externalSystem.GoModulesProjectLinker;
import consulo.logging.Logger;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.creation.scratch.NewModuleBuilderProcessor;
import consulo.project.Project;
import consulo.project.startup.StartupManager;
import consulo.ui.ex.wizard.WizardStep;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Creates one {@link GoProjectTemplate}: the sources are written while the module is created; {@code go mod init}
 * runs once the project is open, then the project is linked as a Go modules project and its main file opened.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoNewModuleBuilderProcessor implements NewModuleBuilderProcessor<GoNewModuleContext> {
    private static final Logger LOG = Logger.getInstance(GoNewModuleBuilderProcessor.class);

    private final GoProjectTemplate myTemplate;

    public GoNewModuleBuilderProcessor(GoProjectTemplate template) {
        myTemplate = template;
    }

    @Override
    public GoNewModuleContext createContext(boolean isNewProject) {
        return new GoNewModuleContext(isNewProject, myTemplate);
    }

    @Override
    public void buildSteps(Consumer<WizardStep<GoNewModuleContext>> consumer, GoNewModuleContext context) {
        consumer.accept(new GoNewModuleSetupStep(context));
    }

    @RequiredReadAction
    @Override
    public void process(GoNewModuleContext context, ContentEntry contentEntry, ModifiableRootModel modifiableRootModel) {
        GoMutableModuleExtension extension = modifiableRootModel.getExtensionWithoutCheck(GoMutableModuleExtension.class);
        Sdk sdk = context.getSdk();
        if (extension != null) {
            extension.setEnabled(true);
            if (sdk != null) {
                extension.getInheritableSdk().set(null, sdk);
                modifiableRootModel.addModuleExtensionSdkEntry(extension);
            }
        }

        VirtualFile contentRoot = contentEntry.getFile();
        if (contentRoot == null) {
            return;
        }
        Path directory = Path.of(contentRoot.getPath());
        String modulePath = context.getModulePathOrDefault();
        Path mainFile;
        try {
            mainFile = GoProjectGenerator.writeSources(myTemplate, directory, context.getName(), modulePath);
        }
        catch (IOException e) {
            LOG.error("Cannot create the Go project in " + directory, e);
            return;
        }

        // process() runs on the UI thread and the root model is committed right after it: go runs later, in the background
        String sdkName = sdk != null ? sdk.getName() : null;
        String goVersion = sdk != null ? GoProjectGenerator.toLanguageVersion(sdk.getVersionString()) : null;
        Project project = modifiableRootModel.getProject();
        StartupManager.getInstance(project).runAfterOpened((openedProject, uiAccess) ->
            Task.Backgroundable.queue(openedProject, GoLocalize.goNewProjectProgress(), indicator -> {
                GoToolchain toolchain = sdk != null ? GoToolchain.fromSdk(sdk) : GoToolchain.find(null);
                try {
                    GoProjectGenerator.initModules(myTemplate, directory, modulePath, toolchain, goVersion);
                }
                catch (IOException e) {
                    LOG.warn("Cannot create the go.mod files of " + directory, e);
                }

                LocalFileSystem fileSystem = LocalFileSystem.getInstance();
                VirtualFile root = fileSystem.refreshAndFindFileByNioFile(directory);
                if (root != null) {
                    root.refresh(false, true);
                }
                VirtualFile main = fileSystem.refreshAndFindFileByNioFile(mainFile);

                uiAccess.give(() -> {
                    if (openedProject.isDisposed()) {
                        return;
                    }
                    if (toolchain != null) {
                        GoModulesProjectLinker.link(openedProject, directory.toString(), sdkName);
                    }
                    openMainFile(openedProject, main);
                });
            }));
    }

    private static void openMainFile(Project project, @Nullable VirtualFile main) {
        if (main != null && main.isValid()) {
            FileEditorManager.getInstance(project).openFile(main, true);
        }
    }
}

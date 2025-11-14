/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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
package com.goide.actions.tool;

import com.goide.psi.GoFile;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.FileDocumentManager;
import consulo.google.go.module.extension.GoModuleExtension;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.localize.LocalizeValue;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.platform.base.localize.CommonLocalize;
import consulo.project.ProjectPropertiesComponent;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.UIUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ref.SimpleReference;
import consulo.versionControlSystem.change.CommitContext;
import consulo.versionControlSystem.change.CommitExecutor;
import consulo.versionControlSystem.checkin.CheckinHandler;
import consulo.versionControlSystem.checkin.CheckinHandlerFactory;
import consulo.versionControlSystem.checkin.CheckinProjectPanel;
import consulo.versionControlSystem.ui.RefreshableOnComponent;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

@ExtensionImpl
public class GoFmtCheckinFactory extends CheckinHandlerFactory {
    private static final String GO_FMT = "GO_FMT";

    @Override
    @Nullable
    public CheckinHandler createHandler(@Nonnull CheckinProjectPanel panel, @Nonnull CommitContext commitContext) {
        if (!ModuleExtensionHelper.getInstance(panel.getProject()).hasModuleExtension(GoModuleExtension.class)) {
            return null;
        }
        return new CheckinHandler() {
            @Override
            public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
                JCheckBox checkBox = new JCheckBox("Go fmt");
                return new RefreshableOnComponent() {
                    @Override
                    @Nonnull
                    public JComponent getComponent() {
                        JPanel panel = new JPanel(new BorderLayout());
                        panel.add(checkBox, BorderLayout.WEST);
                        return panel;
                    }

                    @Override
                    public void refresh() {
                    }

                    @Override
                    public void saveState() {
                        ProjectPropertiesComponent.getInstance(panel.getProject())
                            .setValue(GO_FMT, Boolean.toString(checkBox.isSelected()));
                    }

                    @Override
                    public void restoreState() {
                        checkBox.setSelected(enabled(panel));
                    }
                };
            }

            @Override
            @RequiredUIAccess
            public ReturnResult beforeCheckin(@Nullable CommitExecutor executor, BiConsumer<Object, Object> additionalDataConsumer) {
                if (enabled(panel)) {
                    SimpleReference<Boolean> success = SimpleReference.create(true);
                    FileDocumentManager.getInstance().saveAllDocuments();
                    for (PsiFile file : getPsiFiles()) {
                        VirtualFile virtualFile = file.getVirtualFile();
                        new GoFmtFileAction().doSomething(
                            virtualFile,
                            file.getModule(),
                            file.getProject(),
                            "Go fmt",
                            true,
                            result -> {
                                if (!result) {
                                    success.set(false);
                                }
                            }
                        );
                    }
                    if (!success.get()) {
                        return showErrorMessage(executor);
                    }
                }
                return super.beforeCheckin();
            }

            @Nonnull
            @RequiredUIAccess
            private ReturnResult showErrorMessage(@Nullable CommitExecutor executor) {
                String[] buttons =
                    new String[]{"&Details...", commitButtonMessage(executor, panel).get(), CommonLocalize.buttonCancel().get()};
                int answer = Messages.showDialog(
                    panel.getProject(),
                    "<html><body>GoFmt returned non-zero code on some of the files.<br/>" +
                        "Would you like to commit anyway?</body></html>\n",
                    "Go Fmt",
                    null,
                    buttons,
                    0,
                    1,
                    UIUtil.getWarningIcon()
                );
                if (answer == Messages.OK) {
                    return ReturnResult.CLOSE_WINDOW;
                }
                if (answer == Messages.NO) {
                    return ReturnResult.COMMIT;
                }
                return ReturnResult.CANCEL;
            }

            @Nonnull
            private List<PsiFile> getPsiFiles() {
                Collection<VirtualFile> files = panel.getVirtualFiles();
                List<PsiFile> psiFiles = new ArrayList<>();
                PsiManager manager = PsiManager.getInstance(panel.getProject());
                for (VirtualFile file : files) {
                    PsiFile psiFile = manager.findFile(file);
                    if (psiFile instanceof GoFile) {
                        psiFiles.add(psiFile);
                    }
                }
                return psiFiles;
            }
        };
    }

    @Nonnull
    private static LocalizeValue commitButtonMessage(@Nullable CommitExecutor executor, @Nonnull CheckinProjectPanel panel) {
        LocalizeValue message = executor != null ? executor.getActionText() : panel.getCommitActionName();
        return message.map((localizeManager, string) -> StringUtil.trimEnd(string, "..."));
    }

    private static boolean enabled(@Nonnull CheckinProjectPanel panel) {
        return ProjectPropertiesComponent.getInstance(panel.getProject()).getBoolean(GO_FMT, false);
    }
}
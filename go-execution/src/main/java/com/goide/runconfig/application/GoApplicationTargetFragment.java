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

package com.goide.runconfig.application;

import com.goide.runconfig.GoFormRow;
import com.goide.runconfig.GoRunUtil;
import com.goide.runconfig.testing.ui.GoPackageFieldCompletionProvider;
import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.go.localize.GoLocalize;
import consulo.language.editor.ui.EditorBox;
import consulo.language.editor.ui.EditorBoxBuilderFactory;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

public class GoApplicationTargetFragment implements RunConfigurationFragment<GoApplicationConfiguration> {
    @Override
    public String getId() {
        return "goApplicationTarget";
    }

    @Override
    public LocalizeValue getName() {
        return GoLocalize.runConfigurationFragmentRunTarget();
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.FIRST;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<GoApplicationConfiguration> createEditor(Project project, Disposable parent) {
        Module[] module = new Module[1];

        ComboBox<GoApplicationConfiguration.Kind> kind = ComboBox.create(GoApplicationConfiguration.Kind.values());
        kind.setTextRenderer(value -> value == null ? LocalizeValue.empty() : getKindName(value));

        EditorBox aPackage = project.getApplication()
            .getInstance(EditorBoxBuilderFactory.class)
            .create(project)
            .completion(new GoPackageFieldCompletionProvider(() -> module[0]))
            .build();

        FileChooserTextBoxBuilder.Controller file = FileChooserTextBoxBuilder.create(project)
            .uiDisposable(parent)
            .fileChooserDescriptor(GoRunUtil.createGoWithMainFileChooserDescriptor(project))
            .build();

        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(GoLocalize.goRunConfigurationRunKindLabel(), kind);
        GoFormRow packageRow = GoFormRow.add(builder, GoLocalize.goRunConfigurationPackageLabel(), aPackage);
        GoFormRow fileRow = GoFormRow.add(builder, GoLocalize.goRunConfigurationFileLabel(), file.getComponent());
        Component form = builder.build();

        Runnable update = () -> {
            GoApplicationConfiguration.Kind selected = getKind(kind.getValue());
            packageRow.setVisible(selected == GoApplicationConfiguration.Kind.PACKAGE);
            fileRow.setVisible(selected == GoApplicationConfiguration.Kind.FILE);
        };
        kind.addValueListener(event -> update.run());

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addBottom(form);
            }

            @RequiredUIAccess
            @Override
            public void reset(GoApplicationConfiguration target) {
                module[0] = target.getConfigurationModule().getModule();
                kind.setValue(target.getKind(), false);
                aPackage.setValue(target.getPackage());
                file.setValue(target.getFilePath(), false);
                update.run();
            }

            @RequiredUIAccess
            @Override
            public void apply(GoApplicationConfiguration target) {
                target.setKind(getKind(kind.getValue()));
                target.setPackage(StringUtil.notNullize(aPackage.getValue()).trim());
                target.setFilePath(StringUtil.notNullize(file.getValue()).trim());
            }
        };
    }

    private static GoApplicationConfiguration.Kind getKind(GoApplicationConfiguration.@Nullable Kind kind) {
        return kind == null ? GoApplicationConfiguration.Kind.FILE : kind;
    }

    private static LocalizeValue getKindName(GoApplicationConfiguration.Kind kind) {
        return switch (kind) {
            case PACKAGE -> GoLocalize.goRunConfigurationKindPackage();
            case FILE -> GoLocalize.goRunConfigurationKindFile();
        };
    }
}

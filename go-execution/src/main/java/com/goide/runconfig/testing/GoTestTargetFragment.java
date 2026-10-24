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

package com.goide.runconfig.testing;

import com.goide.runconfig.GoFormRow;
import com.goide.runconfig.GoRunUtil;
import com.goide.runconfig.testing.frameworks.gobench.GobenchFramework;
import com.goide.runconfig.testing.frameworks.gocheck.GocheckFramework;
import com.goide.runconfig.testing.frameworks.gotest.GotestFramework;
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
import consulo.ui.RadioGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.intellij.lang.regexp.RegExpLanguage;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class GoTestTargetFragment implements RunConfigurationFragment<GoTestRunConfiguration> {
    @Override
    public String getId() {
        return "goTestTarget";
    }

    @Override
    public LocalizeValue getName() {
        return GoLocalize.runConfigurationFragmentTestTarget();
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
    public RunConfigurationFragmentEditor<GoTestRunConfiguration> createEditor(Project project, Disposable parent) {
        Module[] module = new Module[1];

        RadioGroup<GoTestFramework> framework = RadioGroup.create();
        Component frameworks = framework.fillHorizontal(
            List.of(GotestFramework.INSTANCE, GocheckFramework.INSTANCE, GobenchFramework.INSTANCE),
            value -> LocalizeValue.of(value.getName())
        );

        ComboBox<GoTestRunConfiguration.Kind> kind = ComboBox.create(GoTestRunConfiguration.Kind.values());
        kind.setTextRenderer(value -> value == null ? LocalizeValue.empty() : getKindName(value));

        FileChooserTextBoxBuilder.Controller directory = FileChooserTextBoxBuilder.create(project)
            .uiDisposable(parent)
            .fileChooserDescriptor(GoRunUtil.createFileChooserDescriptor(project, true, false, null))
            .build();

        EditorBox aPackage = project.getApplication()
            .getInstance(EditorBoxBuilderFactory.class)
            .create(project)
            .completion(new GoPackageFieldCompletionProvider(() -> module[0]))
            .build();

        FileChooserTextBoxBuilder.Controller file = FileChooserTextBoxBuilder.create(project)
            .uiDisposable(parent)
            .fileChooserDescriptor(GoRunUtil.createFileChooserDescriptor(project, false, false, null))
            .build();

        EditorBox pattern = project.getApplication()
            .getInstance(EditorBoxBuilderFactory.class)
            .create(project)
            .language(RegExpLanguage.INSTANCE)
            .build();

        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(GoLocalize.goRunConfigurationTestFrameworkLabel(), frameworks);
        builder.addLabeled(GoLocalize.goRunConfigurationTestKindLabel(), kind);
        GoFormRow directoryRow = GoFormRow.add(builder, GoLocalize.goRunConfigurationDirectoryLabel(), directory.getComponent());
        GoFormRow packageRow = GoFormRow.add(builder, GoLocalize.goRunConfigurationPackageLabel(), aPackage);
        GoFormRow fileRow = GoFormRow.add(builder, GoLocalize.goRunConfigurationFileLabel(), file.getComponent());
        GoFormRow patternRow = GoFormRow.add(builder, GoLocalize.goRunConfigurationPatternLabel(), pattern);
        Component form = builder.build();

        Runnable update = () -> {
            GoTestRunConfiguration.Kind selected = getKind(kind.getValue());
            directoryRow.setVisible(selected == GoTestRunConfiguration.Kind.DIRECTORY);
            packageRow.setVisible(selected == GoTestRunConfiguration.Kind.PACKAGE);
            fileRow.setVisible(selected == GoTestRunConfiguration.Kind.FILE);
            patternRow.setVisible(selected != GoTestRunConfiguration.Kind.FILE);
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
            public void reset(GoTestRunConfiguration target) {
                module[0] = target.getConfigurationModule().getModule();
                framework.setValue(target.getTestFramework(), false);
                kind.setValue(target.getKind(), false);
                aPackage.setValue(target.getPackage());

                String basePath = StringUtil.notNullize(target.getProject().getBasePath());
                String directoryPath = target.getDirectoryPath();
                directory.setValue(directoryPath.isEmpty() ? basePath : directoryPath, false);
                String filePath = target.getFilePath();
                file.setValue(filePath.isEmpty() ? basePath : filePath, false);

                pattern.setValue(target.getPattern());
                update.run();
            }

            @RequiredUIAccess
            @Override
            public void apply(GoTestRunConfiguration target) {
                GoTestFramework selectedFramework = framework.getValue();
                target.setTestFramework(selectedFramework == null ? GotestFramework.INSTANCE : selectedFramework);
                target.setKind(getKind(kind.getValue()));
                target.setPackage(StringUtil.notNullize(aPackage.getValue()).trim());
                target.setDirectoryPath(StringUtil.notNullize(directory.getValue()).trim());
                target.setFilePath(StringUtil.notNullize(file.getValue()).trim());
                target.setPattern(StringUtil.notNullize(pattern.getValue()));
            }
        };
    }

    private static GoTestRunConfiguration.Kind getKind(GoTestRunConfiguration.@Nullable Kind kind) {
        return kind == null ? GoTestRunConfiguration.Kind.DIRECTORY : kind;
    }

    private static LocalizeValue getKindName(GoTestRunConfiguration.Kind kind) {
        return switch (kind) {
            case DIRECTORY -> GoLocalize.goRunConfigurationKindDirectory();
            case PACKAGE -> GoLocalize.goRunConfigurationKindPackage();
            case FILE -> GoLocalize.goRunConfigurationKindFile();
        };
    }
}

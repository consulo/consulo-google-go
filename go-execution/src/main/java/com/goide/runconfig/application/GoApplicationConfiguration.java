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

package com.goide.runconfig.application;

import com.goide.runconfig.GoModuleBasedConfiguration;
import com.goide.runconfig.GoRunConfigurationWithMain;
import com.goide.runconfig.GoRunUtil;
import com.goide.util.GoPackageUtil;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.*;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class GoApplicationConfiguration extends GoRunConfigurationWithMain<GoApplicationRunningState> implements RunProfileWithCompileBeforeLaunchOption {
    private static final String PACKAGE_ATTRIBUTE_NAME = "package";
    private static final String KIND_ATTRIBUTE_NAME = "kind";
    private static final String OUTPUT_FILE_ELEMENT = "output_file";
    private static final String OUTPUT_FILE_PATH_ATTRIBUTE = "path";

    public GoApplicationConfiguration(Project project, String name, ConfigurationType configurationType) {
        super(name, new GoModuleBasedConfiguration(project), configurationType.getConfigurationFactories()[0]);
    }

    @Override
    protected Class<? extends RunConfigurationOptions> getOptionsClass() {
        return GoApplicationConfigurationOptions.class;
    }

    @Override
    public GoApplicationConfigurationOptions getOptions() {
        return (GoApplicationConfigurationOptions) super.getOptions();
    }

    @Override
    protected void convertLegacyState(LegacyRunConfigurationConverter converter) {
        super.convertLegacyState(converter);

        GoApplicationConfigurationOptions options = new GoApplicationConfigurationOptions();
        convertLegacyMainState(converter, options);

        Element element = converter.getElement();
        String aPackage = StringUtil.notNullize(readLegacyValue(element, PACKAGE_ATTRIBUTE_NAME));
        options.setPackage(aPackage);

        Kind kind;
        try {
            String kindName = readLegacyValue(element, KIND_ATTRIBUTE_NAME);
            kind = kindName != null ? Kind.valueOf(kindName) : Kind.PACKAGE;
        }
        catch (IllegalArgumentException e) {
            kind = !aPackage.isEmpty() ? Kind.PACKAGE : Kind.FILE;
        }
        options.setKind(kind == Kind.FILE ? null : kind.name());

        Element outputFile = element.getChild(OUTPUT_FILE_ELEMENT);
        if (outputFile != null) {
            options.setOutputDirectory(outputFile.getAttributeValue(OUTPUT_FILE_PATH_ATTRIBUTE));
            if (!isSaveOutputToFile()) {
                setFileOutputPath(null);
            }
        }

        converter.setOptions(options);
    }

    @Override
    public List<RunConfigurationFragment<?>> createFragments() {
        List<RunConfigurationFragment<?>> fragments = super.createFragments();
        fragments.add(0, new GoApplicationTargetFragment());
        fragments.add(new GoOutputDirectoryFragment());
        return fragments;
    }

    public String getTarget() {
        return getKind() == GoApplicationConfiguration.Kind.PACKAGE ? getPackage() : getFilePath();
    }

    @Override
    protected ModuleBasedConfiguration createInstance() {
        return new GoApplicationConfiguration(getProject(), getName(), GoApplicationRunConfigurationType.getInstance());
    }

    @Override
    protected GoApplicationRunningState newRunningState(ExecutionEnvironment env, Module module) {
        return new GoApplicationRunningState(env, module, this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        checkBaseConfiguration();
        switch (getKind()) {
            case PACKAGE:
                Module module = getConfigurationModule().getModule();
                assert module != null;

                String aPackage = getPackage();
                if (StringUtil.isEmptyOrSpaces(aPackage)) {
                    throw new RuntimeConfigurationError("Package is not specified");
                }
                VirtualFile packageDirectory = GoPackageUtil.findByImportPath(aPackage, module.getProject(), module);
                if (packageDirectory == null || !packageDirectory.isDirectory()) {
                    throw new RuntimeConfigurationError("Cannot find package '" + aPackage + "'");
                }
                if (GoRunUtil.findMainFileInDirectory(packageDirectory, getProject()) == null) {
                    throw new RuntimeConfigurationError("Cannot find Go file with main in '" + aPackage + "'");
                }
                break;
            case FILE:
                checkFileConfiguration();
                break;
        }
    }

    public String getPackage() {
        return StringUtil.notNullize(getOptions().getPackage());
    }

    public void setPackage(@Nullable String aPackage) {
        getOptions().setPackage(aPackage);
    }

    public Kind getKind() {
        String kind = getOptions().getKind();
        if (kind == null) {
            return Kind.FILE;
        }
        try {
            return Kind.valueOf(kind);
        }
        catch (IllegalArgumentException e) {
            return Kind.FILE;
        }
    }

    public void setKind(@Nullable Kind aKind) {
        getOptions().setKind(aKind == null || aKind == Kind.FILE ? null : aKind.name());
    }

    /**
     * The directory the program is built into, empty for a temporary file deleted after the run.
     */
    public String getOutputDirectory() {
        return StringUtil.notNullize(getOptions().getOutputDirectory());
    }

    public void setOutputDirectory(@Nullable String outputDirectory) {
        getOptions().setOutputDirectory(outputDirectory);
    }

    public enum Kind {
        PACKAGE,
        FILE
    }
}

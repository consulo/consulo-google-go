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

package com.goide.runconfig;

import com.goide.sdk.GoSdkService;
import com.goide.sdk.GoSdkUtil;
import consulo.execution.CommonProgramRunConfigurationParameters;
import consulo.execution.ExecutionBundle;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.RuntimeConfigurationWarning;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.RuntimeConfigurationError;
import consulo.execution.configuration.fragment.EnvironmentFragmentState;
import consulo.execution.configuration.fragment.ProgramParametersFragmentState;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.WorkingDirectoryFragmentState;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.util.collection.ContainerUtil;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.JDOMExternalizerUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GoRunConfigurationBase<RunningState extends GoRunningState> extends ModuleBasedConfiguration<GoModuleBasedConfiguration>
    implements CommonProgramRunConfigurationParameters {

    private static final String WORKING_DIRECTORY_NAME = "working_directory";
    private static final String GO_PARAMETERS_NAME = "go_parameters";
    private static final String PARAMETERS_NAME = "parameters";
    private static final String PASS_PARENT_ENV = "pass_parent_env";

    public GoRunConfigurationBase(String name, GoModuleBasedConfiguration configurationModule, ConfigurationFactory factory) {
        super(name, configurationModule, factory);

        Module module = configurationModule.getModule();
        if (module == null) {
            Collection<Module> modules = getValidModules();
            if (modules.size() == 1) {
                module = ContainerUtil.getFirstItem(modules);
                getConfigurationModule().setModule(module);
            }
        }

        if (module != null) {
            setWorkingDirectory(StringUtil.notNullize(module.getModuleDirPath()));
        }
        else {
            setWorkingDirectory(StringUtil.notNullize(configurationModule.getProject().getBasePath()));
        }
    }

    @Override
    public GoRunConfigurationOptions getOptions() {
        return (GoRunConfigurationOptions) super.getOptions();
    }

    /**
     * Converts the working directory, parameters and environment of an old configuration element.
     */
    protected void convertLegacyCommonState(LegacyRunConfigurationConverter converter, GoRunConfigurationOptions options) {
        Element element = converter.getElement();
        options.setGoToolParams(readLegacyValue(element, GO_PARAMETERS_NAME));

        String workingDirectory = readLegacyValue(element, WORKING_DIRECTORY_NAME);
        converter.addFragment(new WorkingDirectoryFragmentState(workingDirectory != null ? workingDirectory : getWorkingDirectory()));
        converter.addFragment(new ProgramParametersFragmentState(readLegacyValue(element, PARAMETERS_NAME)));

        String passParentEnvs = readLegacyValue(element, PASS_PARENT_ENV);
        converter.addFragment(new EnvironmentFragmentState(converter.readEnvs(), passParentEnvs == null || Boolean.parseBoolean(passParentEnvs)));
    }

    protected static @Nullable String readLegacyValue(Element element, String name) {
        return JDOMExternalizerUtil.getFirstChildValueAttribute(element, name);
    }

    @Override
    public List<RunConfigurationFragment<?>> createFragments() {
        List<RunConfigurationFragment<?>> fragments = new ArrayList<>();
        fragments.add(new GoToolArgumentsFragment<>());
        return fragments;
    }

    @Nullable
    @Override
    public RunProfileState getState(Executor executor, ExecutionEnvironment environment) throws ExecutionException {
        return createRunningState(environment);
    }

    @Override
    public Collection<Module> getValidModules() {
        return GoSdkUtil.getGoModules(getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        GoModuleBasedConfiguration configurationModule = getConfigurationModule();
        Module module = configurationModule.getModule();
        if (module != null) {
            if (GoSdkService.getInstance(module.getProject()).getSdkHomePath(module) == null) {
                throw new RuntimeConfigurationWarning("Go SDK is not specified for module '" + module.getName() + "'");
            }
        }
        else {
            String moduleName = configurationModule.getModuleName();
            if (moduleName != null) {
                throw new RuntimeConfigurationError(ExecutionBundle.message("module.doesn.t.exist.in.project.error.text", moduleName));
            }
            throw new RuntimeConfigurationError(ExecutionBundle.message("module.not.specified.error.text"));
        }
        if (getWorkingDirectory().isEmpty()) {
            throw new RuntimeConfigurationError("Working directory is not specified");
        }
    }

    private RunningState createRunningState(ExecutionEnvironment env) throws ExecutionException {
        GoModuleBasedConfiguration configuration = getConfigurationModule();
        Module module = configuration.getModule();
        if (module == null) {
            throw new ExecutionException("Go isn't configured for run configuration: " + getName());
        }
        return newRunningState(env, module);
    }

    @Nullable
    protected VirtualFile findFile(String filePath) {
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(VirtualFileUtil.pathToUrl(filePath));
        if (virtualFile == null) {
            String path = FileUtil.join(getWorkingDirectory(), filePath);
            virtualFile = VirtualFileManager.getInstance().findFileByUrl(VirtualFileUtil.pathToUrl(path));
        }
        return virtualFile;
    }

    protected abstract RunningState newRunningState(ExecutionEnvironment env, Module module);

    public String getGoToolParams() {
        return StringUtil.notNullize(getOptions().getGoToolParams());
    }

    public String getParams() {
        return StringUtil.notNullize(getProgramParameters());
    }

    public void setGoParams(@Nullable String params) {
        getOptions().setGoToolParams(params);
    }

    public void setParams(@Nullable String params) {
        setProgramParameters(params);
    }

    public Map<String, String> getCustomEnvironment() {
        return getEnvs();
    }

    public void setCustomEnvironment(Map<String, String> customEnvironment) {
        setEnvs(new HashMap<>(customEnvironment));
    }

    public void setPassParentEnvironment(boolean passParentEnvironment) {
        setPassParentEnvs(passParentEnvironment);
    }

    public boolean isPassParentEnvironment() {
        return isPassParentEnvs();
    }

    @Override
    public String getWorkingDirectory() {
        return StringUtil.notNullize(CommonProgramRunConfigurationParameters.super.getWorkingDirectory());
    }

    public String getWorkingDirectoryUrl() {
        return VirtualFileUtil.pathToUrl(getWorkingDirectory());
    }

    @Override
    public void setWorkingDirectory(@Nullable String workingDirectory) {
        CommonProgramRunConfigurationParameters.super.setWorkingDirectory(workingDirectory);
    }
}

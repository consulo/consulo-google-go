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
import com.goide.runconfig.ui.GoApplicationConfigurationEditorForm;
import com.goide.sdk.GoPackageUtil;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.*;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.JDOMExternalizerUtil;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.virtualFileSystem.VirtualFile;
import org.jdom.Element;

public class GoApplicationConfiguration extends GoRunConfigurationWithMain<GoApplicationRunningState> implements RunProfileWithCompileBeforeLaunchOption {
    private static final String PACKAGE_ATTRIBUTE_NAME = "package";
    private static final String KIND_ATTRIBUTE_NAME = "kind";

    private String myPackage = "";

    private Kind myKind = Kind.FILE;

    public GoApplicationConfiguration(Project project, String name, ConfigurationType configurationType) {
        super(name, new GoModuleBasedConfiguration(project), configurationType.getConfigurationFactories()[0]);
    }

    public String getTarget() {
        return getKind() == GoApplicationConfiguration.Kind.PACKAGE ? getPackage() : getFilePath();
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        myPackage = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element, PACKAGE_ATTRIBUTE_NAME));
        try {
            String kindName = JDOMExternalizerUtil.getFirstChildValueAttribute(element, KIND_ATTRIBUTE_NAME);
            myKind = kindName != null ? Kind.valueOf(kindName) : Kind.PACKAGE;
        }
        catch (IllegalArgumentException e) {
            myKind = !myPackage.isEmpty() ? Kind.PACKAGE : Kind.FILE;
        }
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizerUtil.addElementWithValueAttribute(element, KIND_ATTRIBUTE_NAME, myKind.name());
        if (!myPackage.isEmpty()) {
            JDOMExternalizerUtil.addElementWithValueAttribute(element, PACKAGE_ATTRIBUTE_NAME, myPackage);
        }
    }

    @Override
    protected ModuleBasedConfiguration createInstance() {
        return new GoApplicationConfiguration(getProject(), getName(), GoApplicationRunConfigurationType.getInstance());
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new GoApplicationConfigurationEditorForm(getProject());
    }

    @Override
    protected GoApplicationRunningState newRunningState(ExecutionEnvironment env, Module module) {
        return new GoApplicationRunningState(env, module, this);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        checkBaseConfiguration();
        switch (myKind) {
            case PACKAGE:
                Module module = getConfigurationModule().getModule();
                assert module != null;

                if (StringUtil.isEmptyOrSpaces(myPackage)) {
                    throw new RuntimeConfigurationError("Package is not specified");
                }
                VirtualFile packageDirectory = GoPackageUtil.findByImportPath(myPackage, module.getProject(), module);
                if (packageDirectory == null || !packageDirectory.isDirectory()) {
                    throw new RuntimeConfigurationError("Cannot find package '" + myPackage + "'");
                }
                if (GoRunUtil.findMainFileInDirectory(packageDirectory, getProject()) == null) {
                    throw new RuntimeConfigurationError("Cannot find Go file with main in '" + myPackage + "'");
                }
                break;
            case FILE:
                checkFileConfiguration();
                break;
        }
    }

    public String getPackage() {
        return myPackage;
    }

    public void setPackage(String aPackage) {
        myPackage = aPackage;
    }

    public Kind getKind() {
        return myKind;
    }

    public void setKind(Kind aKind) {
        myKind = aKind;
    }

    public enum Kind {
        PACKAGE,
        FILE
    }
}

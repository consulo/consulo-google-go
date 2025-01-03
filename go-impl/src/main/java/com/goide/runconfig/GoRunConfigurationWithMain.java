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

import com.goide.psi.GoFile;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RuntimeConfigurationError;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.JDOMExternalizerUtil;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.virtualFileSystem.VirtualFile;
import org.jdom.Element;

import jakarta.annotation.Nonnull;

public abstract class GoRunConfigurationWithMain<T extends GoRunningState> extends GoRunConfigurationBase<T> {
  private static final String FILE_PATH_ATTRIBUTE_NAME = "filePath";

  @Nonnull
  private String myFilePath = "";

  public GoRunConfigurationWithMain(String name, GoModuleBasedConfiguration configurationModule, ConfigurationFactory factory) {
    super(name, configurationModule, factory);
    myFilePath = getWorkingDirectory();
  }

  @Override
  public void readExternal(@Nonnull Element element) throws InvalidDataException {
    super.readExternal(element);
    myFilePath = StringUtil.notNullize(JDOMExternalizerUtil.getFirstChildValueAttribute(element, FILE_PATH_ATTRIBUTE_NAME));
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    addNonEmptyElement(element, FILE_PATH_ATTRIBUTE_NAME, myFilePath);
  }

  protected void checkFileConfiguration() throws RuntimeConfigurationError {
    VirtualFile file = findFile(getFilePath());
    if (file == null) {
      throw new RuntimeConfigurationError("Main file is not specified");
    }
    PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
    if (!(psiFile instanceof GoFile)) {
      throw new RuntimeConfigurationError("Main file is invalid");
    }
    if (!GoRunUtil.isMainGoFile(psiFile)) {
      throw new RuntimeConfigurationError("Main file has non-main package or doesn't contain main function");
    }
  }

  protected void checkBaseConfiguration() throws RuntimeConfigurationException {
    super.checkConfiguration();
  }

  @Nonnull
  public String getFilePath() {
    return myFilePath;
  }

  public void setFilePath(@Nonnull String filePath) {
    myFilePath = filePath;
  }
}

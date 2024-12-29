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

package com.goide.runconfig.testing.frameworks.gocheck;

import com.goide.psi.GoFunctionOrMethodDeclaration;
import com.goide.psi.GoMethodDeclaration;
import com.goide.runconfig.testing.GoTestRunConfigurationProducerBase;
import consulo.annotation.component.ExtensionImpl;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class GocheckRunConfigurationProducer extends GoTestRunConfigurationProducerBase implements Cloneable {
  public GocheckRunConfigurationProducer() {
    super(GocheckFramework.INSTANCE);
  }

  @Nonnull
  @Override
  protected String getPackageConfigurationName(@Nonnull String packageName) {
    return "gocheck package '" + packageName + "'";
  }

  @Nonnull
  @Override
  protected String getFunctionConfigurationName(@Nonnull GoFunctionOrMethodDeclaration function, @Nonnull String fileName) {
    return function instanceof GoMethodDeclaration
           ? GocheckFramework.getGocheckTestName((GoMethodDeclaration)function) + " in " + fileName
           : super.getFunctionConfigurationName(function, fileName);
  }

  @Nonnull
  @Override
  protected String getFileConfigurationName(@Nonnull String fileName) {
    return "gocheck " + super.getFileConfigurationName(fileName);
  }
}

/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.actions.file;

import com.goide.util.GoUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.fileTemplate.DefaultTemplatePropertiesProvider;
import consulo.fileTemplate.FileTemplate;
import consulo.language.psi.PsiDirectory;

import java.util.Map;

@ExtensionImpl
public class GoTemplatePropertiesProvider implements DefaultTemplatePropertiesProvider {
  public static final String GO_PACKAGE_NAME = "GO_" + FileTemplate.ATTRIBUTE_PACKAGE_NAME;
  public static final String GO_PACKAGE_NAME_WITH_TEST_SUFFIX = GO_PACKAGE_NAME + "_WITH_TEST_SUFFIX";

  @Override
  public void fillProperties(PsiDirectory directory, Map<String, Object> props) {
    String packageForDirectory = GoUtil.suggestPackageForDirectory(directory);
    props.put(GO_PACKAGE_NAME, packageForDirectory);
    props.put(GO_PACKAGE_NAME_WITH_TEST_SUFFIX, packageForDirectory);
  }
}

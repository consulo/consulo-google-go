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

package com.goide.psi.impl.manipulator;

import com.goide.psi.GoImportString;
import com.goide.psi.impl.GoElementFactory;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.psi.AbstractElementManipulator;
import consulo.language.util.IncorrectOperationException;


@ExtensionImpl
public class GoImportStringManipulator extends AbstractElementManipulator<GoImportString> {
  @Override
  public GoImportString handleContentChange(GoImportString string, TextRange range, String s) throws IncorrectOperationException {
    String newPackage = range.replace(string.getText(), s);
    return (GoImportString)string.replace(GoElementFactory.createImportString(string.getProject(), newPackage));
  }

  @Override
  public TextRange getRangeInElement(GoImportString element) {
    return element.getPathTextRange();
  }

  @Override
  public Class<GoImportString> getElementClass() {
    return GoImportString.class;
  }
}

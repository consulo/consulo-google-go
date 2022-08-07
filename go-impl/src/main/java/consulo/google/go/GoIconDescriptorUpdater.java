/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use element file except in compliance with the License.
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

package consulo.google.go;

import com.goide.GoIcons;
import com.goide.psi.*;
import com.goide.runconfig.testing.GoTestFinder;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.component.util.Iconable;
import consulo.google.go.icon.GoogleGoIconGroup;
import consulo.language.icon.IconDescriptor;
import consulo.language.icon.IconDescriptorUpdater;
import consulo.language.psi.PsiElement;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.BitUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 03-May-17
 */
@ExtensionImpl
public class GoIconDescriptorUpdater implements IconDescriptorUpdater {
  @RequiredReadAction
  @Override
  public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement element, int flags) {
    if (element instanceof GoFile && element.isValid() && GoTestFinder.isTestFile((GoFile)element)) {
      iconDescriptor.setMainIcon(GoogleGoIconGroup.gofiletype()).addLayerIcon(PlatformIconGroup.nodesJunittestmark());
      return;
    }

    Image icon = null;
    if (element instanceof GoMethodDeclaration) {
      icon = GoIcons.METHOD;
    }
    else if (element instanceof GoFunctionDeclaration) {
      icon = GoIcons.FUNCTION;
    }
    else if (element instanceof GoTypeSpec) {
      icon = GoIcons.TYPE;
    }
    else if (element instanceof GoVarDefinition) {
      icon = GoIcons.VARIABLE;
    }
    else if (element instanceof GoConstDefinition) {
      icon = GoIcons.CONSTANT;
    }
    else if (element instanceof GoFieldDefinition) {
      icon = GoIcons.FIELD;
    }
    else if (element instanceof GoMethodSpec) {
      icon = GoIcons.METHOD;
    }
    else if (element instanceof GoAnonymousFieldDefinition) {
      icon = GoIcons.FIELD;
    }
    else if (element instanceof GoParamDefinition) {
      icon = GoIcons.PARAMETER;
    }
    else if (element instanceof GoLabelDefinition) {
      icon = GoIcons.LABEL;
    }

    if (icon != null) {
      iconDescriptor.setMainIcon(icon);

      if (BitUtil.isSet(flags, Iconable.ICON_FLAG_VISIBILITY) && element instanceof GoNamedElement) {
        boolean aPublic = ((GoNamedElement)element).isPublic();
        iconDescriptor.setRightIcon(aPublic ? AllIcons.Nodes.C_public : AllIcons.Nodes.C_private);
      }
    }
  }
}

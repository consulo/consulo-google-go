package consulo.googe.go;

import com.goide.GoIcons;
import com.goide.psi.*;
import com.goide.runconfig.testing.GoTestFinder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.util.BitUtil;
import consulo.annotations.RequiredReadAction;
import consulo.ide.IconDescriptor;
import consulo.ide.IconDescriptorUpdater;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author VISTALL
 * @since 03-May-17
 */
public class GoIconDescriptorUpdater implements IconDescriptorUpdater {
  @RequiredReadAction
  @Override
  public void updateIcon(@NotNull IconDescriptor iconDescriptor, @NotNull PsiElement element, int flags) {
    if (element instanceof GoFile && element.isValid() && GoTestFinder.isTestFile((GoFile)element)) {
      iconDescriptor.setMainIcon(GoIcons.TEST_RUN);
      return;
    }

    Icon icon = null;
    if (this instanceof GoMethodDeclaration) {
      icon = GoIcons.METHOD;
    }
    else if (this instanceof GoFunctionDeclaration) {
      icon = GoIcons.FUNCTION;
    }
    else if (this instanceof GoTypeSpec) {
      icon = GoIcons.TYPE;
    }
    else if (this instanceof GoVarDefinition) {
      icon = GoIcons.VARIABLE;
    }
    else if (this instanceof GoConstDefinition) {
      icon = GoIcons.CONSTANT;
    }
    else if (this instanceof GoFieldDefinition) {
      icon = GoIcons.FIELD;
    }
    else if (this instanceof GoMethodSpec) {
      icon = GoIcons.METHOD;
    }
    else if (this instanceof GoAnonymousFieldDefinition) {
      icon = GoIcons.FIELD;
    }
    else if (this instanceof GoParamDefinition) {
      icon = GoIcons.PARAMETER;
    }
    else if (this instanceof GoLabelDefinition) {
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

package com.goide.template;

import com.goide.psi.GoFile;
import com.goide.psi.GoPackageClause;
import com.goide.psi.GoTopLevelDeclaration;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;

@ExtensionImpl
public class GoFileLiveTemplateContextType extends GoLiveTemplateContextType {
  @Inject
  GoFileLiveTemplateContextType() {
    super("GO_FILE", LocalizeValue.localizeTODO("File"), GoEverywhereContextType.class);
  }

  @Override
  protected boolean isInContext(@Nonnull PsiElement element) {
    if (element instanceof PsiComment || element instanceof GoPackageClause) {
      return false;
    }
    return element instanceof GoFile || element.getParent() instanceof GoFile && !(element instanceof GoTopLevelDeclaration);
  }
}

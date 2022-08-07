package com.goide.psi.impl.imports;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.PsiReferenceLocalQuickFixProvider;
import consulo.language.psi.PsiReference;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 06-Aug-22
 */
@ExtensionImpl
public class GoImportReferenceQuickFixProvider implements PsiReferenceLocalQuickFixProvider {
  @Override
  public void addQuickFixes(@Nonnull PsiReference psiReference, @Nonnull Consumer<LocalQuickFix> consumer) {
    if (psiReference instanceof GoImportReference importReference) {
      for (LocalQuickFix fix : importReference.getQuickFixes()) {
        consumer.accept(fix);
      }
    }
  }
}

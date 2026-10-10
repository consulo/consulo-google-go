package com.goide.codeInsight.imports;

import com.goide.psi.impl.imports.GoImportReference;
import com.goide.psi.impl.imports.GoImportReferenceSet;
import com.goide.quickfix.GoDeleteImportQuickFix;
import com.goide.util.GoPackageUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.impl.intention.CreateFileFix;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.PsiReferenceLocalQuickFixProvider;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiFileSystemItem;
import consulo.language.psi.PsiReference;
import consulo.language.psi.path.FileReferenceSet;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;

import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 06-Aug-22
 */
@ExtensionImpl
public class GoImportReferenceQuickFixProvider implements PsiReferenceLocalQuickFixProvider {
  @Override
  public void addQuickFixes(PsiReference psiReference, Consumer<LocalQuickFix> consumer) {
    if (psiReference instanceof GoImportReference importReference) {
      addQuickFixes(importReference, consumer);
    }
  }

  private static void addQuickFixes(GoImportReference reference, Consumer<LocalQuickFix> consumer) {
    if (GoPackageUtil.isBuiltinPackage(reference.resolve())) {
      consumer.accept(new GoDeleteImportQuickFix());
      return;
    }

    FileReferenceSet fileReferenceSet = reference.getFileReferenceSet();
    if (fileReferenceSet instanceof GoImportReferenceSet && !((GoImportReferenceSet)fileReferenceSet).isRelativeImport()
        && !fileReferenceSet.isAbsolutePathReference()) {
      consumer.accept(new GoGetPackageFix(fileReferenceSet.getPathString()));
    }

    String fileNameToCreate = reference.getFileNameToCreate();
    for (PsiFileSystemItem context : reference.getContexts()) {
      if (context instanceof PsiDirectory) {
        try {
          ((PsiDirectory)context).checkCreateSubdirectory(fileNameToCreate);
          String targetPath = context.getVirtualFile().getPath();
          consumer.accept(new CreateFileFix(true, fileNameToCreate, (PsiDirectory)context) {
            @Override
            public LocalizeValue getText() {
              return LocalizeValue.localizeTODO("Create Directory " + fileNameToCreate + " at " + targetPath);
            }
          });
        }
        catch (IncorrectOperationException ignore) {
        }
      }
    }
  }
}

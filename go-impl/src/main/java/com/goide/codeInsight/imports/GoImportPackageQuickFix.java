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

package com.goide.codeInsight.imports;

import com.goide.GoIcons;
import com.goide.completion.GoCompletionUtil;
import com.goide.project.GoVendoringUtil;
import com.goide.psi.GoFile;
import com.goide.psi.GoReferenceExpression;
import com.goide.psi.GoTypeReferenceExpression;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.psi.impl.GoReference;
import com.goide.psi.impl.GoTypeReference;
import com.goide.runconfig.testing.GoTestFinder;
import com.goide.stubs.index.GoPackagesIndex;
import com.goide.util.GoUtil;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.document.util.TextRange;
import consulo.language.editor.AutoImportHelper;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.hint.HintManager;
import consulo.language.editor.inspection.LocalQuickFixAndIntentionActionOnPsiElement;
import consulo.language.editor.intention.HighPriorityAction;
import consulo.language.editor.intention.HintAction;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.StubIndex;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.ex.awt.ColoredListCellRenderer;
import consulo.ui.ex.popup.IPopupChooserBuilder;
import consulo.ui.ex.popup.JBPopup;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.undoRedo.CommandProcessor;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.*;

import static consulo.util.collection.ContainerUtil.*;

public class GoImportPackageQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement implements HintAction, HighPriorityAction {
  @Nonnull
  private final String myPackageName;
  @Nullable
  private List<String> myPackagesToImport;

  public GoImportPackageQuickFix(@Nonnull PsiElement element, @Nonnull String importPath) {
    super(element);
    myPackageName = "";
    myPackagesToImport = Collections.singletonList(importPath);
  }

  public GoImportPackageQuickFix(@Nonnull PsiReference reference) {
    super(reference.getElement());
    myPackageName = reference.getCanonicalText();
  }

  @Nullable
  public PsiReference getReference(PsiElement element) {
    if (element != null && element.isValid()) {
      for (PsiReference reference : element.getReferences()) {
        if (isSupportedReference(reference)) {
          return reference;
        }
      }
    }
    return null;
  }

  private static boolean isSupportedReference(@Nullable PsiReference reference) {
    return reference instanceof GoReference || reference instanceof GoTypeReference;
  }

  @Override
  public boolean showHint(@Nonnull Editor editor) {
    return doAutoImportOrShowHint(editor, true);
  }

  @Nonnull
  @Override
  public String getText() {
    PsiElement element = getStartElement();
    if (element != null) {
      return "Import " + getText(getImportPathVariantsToImport(element));
    }
    return "Import package";
  }

  @Nonnull
  private static String getText(@Nonnull Collection<String> packagesToImport) {
    return getFirstItem(packagesToImport, "") + "? " + (packagesToImport.size() > 1 ? "(multiple choices...) " : "");
  }

  @Nonnull
  @Override
  public String getFamilyName() {
    return "Import package";
  }

  @Override
  public void invoke(@Nonnull Project project, @Nonnull PsiFile file, @Nullable Editor editor,
                     @Nonnull PsiElement startElement, @Nonnull PsiElement endElement) {
    if (!FileModificationService.getInstance().prepareFileForWrite(file)) return;
    perform(getImportPathVariantsToImport(startElement), file, editor);
  }

  @Override
  public boolean isAvailable(@Nonnull Project project,
                             @Nonnull PsiFile file,
                             @Nonnull PsiElement startElement,
                             @Nonnull PsiElement endElement) {
    PsiReference reference = getReference(startElement);
    return file instanceof GoFile && file.getManager().isInProject(file)
        && reference != null && reference.resolve() == null
        && !getImportPathVariantsToImport(startElement).isEmpty() && notQualified(startElement);
  }

  private static boolean notQualified(@Nullable PsiElement startElement) {
    return startElement instanceof GoReferenceExpression && ((GoReferenceExpression) startElement).getQualifier() == null ||
        startElement instanceof GoTypeReferenceExpression && ((GoTypeReferenceExpression) startElement).getQualifier() == null;
  }

  @Nonnull
  private List<String> getImportPathVariantsToImport(@Nonnull PsiElement element) {
    if (myPackagesToImport == null) {
      myPackagesToImport = getImportPathVariantsToImport(myPackageName, element);
    }
    return myPackagesToImport;
  }

  @Nonnull
  public static List<String> getImportPathVariantsToImport(@Nonnull String packageName, @Nonnull PsiElement context) {
    PsiFile contextFile = context.getContainingFile();
    Set<String> imported = contextFile instanceof GoFile
        ? ((GoFile) contextFile).getImportedPackagesMap().keySet() : Collections.emptySet();
    Project project = context.getProject();
    PsiDirectory parentDirectory = contextFile != null ? contextFile.getParent() : null;
    String testTargetPackage = GoTestFinder.getTestTargetPackage(contextFile);
    Module module = contextFile != null ? ModuleUtilCore.findModuleForPsiElement(contextFile) : null;
    boolean vendoringEnabled = GoVendoringUtil.isVendoringEnabled(module);
    GlobalSearchScope scope = GoUtil.goPathResolveScope(context);
    Collection<GoFile> packages = StubIndex.getElements(GoPackagesIndex.KEY, packageName, project, scope, GoFile.class);
    return sorted(skipNulls(map2Set(
        packages,
        file -> {
          if (parentDirectory != null && parentDirectory.isEquivalentTo(file.getParent())) {
            if (testTargetPackage == null || !testTargetPackage.equals(file.getPackageName())) {
              return null;
            }
          }
          if (!GoPsiImplUtil.canBeAutoImported(file, false, module)) {
            return null;
          }
          String importPath = file.getImportPath(vendoringEnabled);
          return !imported.contains(importPath) ? importPath : null;
        }
    )), new MyImportsComparator(context, vendoringEnabled));
  }

  public boolean doAutoImportOrShowHint(@Nonnull Editor editor, boolean showHint) {
    PsiElement element = getStartElement();
    if (element == null || !element.isValid()) return false;

    PsiReference reference = getReference(element);
    if (reference == null || reference.resolve() != null) return false;

    List<String> packagesToImport = getImportPathVariantsToImport(element);
    if (packagesToImport.isEmpty()) {
      return false;
    }

    PsiFile file = element.getContainingFile();
    String firstPackageToImport = getFirstItem(packagesToImport);

    AutoImportHelper autoImportHelper = AutoImportHelper.getInstance(file.getProject());

    // autoimport on trying to fix
    if (packagesToImport.size() == 1) {
      if (GoCodeInsightSettings.getInstance().isAddUnambiguousImportsOnTheFly() && autoImportHelper.canChangeFileSilently(file)) {
        CommandProcessor.getInstance().runUndoTransparentAction(() -> perform(file, firstPackageToImport));
        return true;
      }
    }

    // show hint on failed autoimport
    if (showHint) {
      if (ApplicationManager.getApplication().isUnitTestMode()) return false;
      if (HintManager.getInstance().hasShownHintsThatWillHideByOtherHint(true)) return false;
      if (!GoCodeInsightSettings.getInstance().isShowImportPopup()) return false;
      TextRange referenceRange = reference.getRangeInElement().shiftRight(element.getTextRange().getStartOffset());
      HintManager.getInstance().showQuestionHint(
          editor,
          autoImportHelper.getImportMessage(packagesToImport.size() > 1, getFirstItem(packagesToImport)),
          referenceRange.getStartOffset(),
          referenceRange.getEndOffset(),
          () -> {
            if (file.isValid() && !editor.isDisposed()) {
              perform(packagesToImport, file, editor);
            }
            return true;
          }
      );
      return true;
    }
    return false;
  }

  private void perform(@Nonnull List<String> packagesToImport, @Nonnull PsiFile file, @Nullable Editor editor) {
    LOG.assertTrue(editor != null || packagesToImport.size() == 1, "Cannot invoke fix with ambiguous imports on null editor");
    if (packagesToImport.size() > 1 && editor != null) {
      IPopupChooserBuilder<String> builder = JBPopupFactory.getInstance().createPopupChooserBuilder(packagesToImport).setRequestFocus(true)
          .setTitle("Package to import")
          .setRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(@Nonnull JList jList, Object value, int i, boolean b, boolean b1) {
              append(value.toString());
              setIcon(GoIcons.PACKAGE);
            }
          })
          .setItemSelectedCallback(
              (c) -> {
                perform(file, c);
              });

      JBPopup popup = builder.createPopup();
      editor.showPopupInBestPositionFor(popup);
    } else if (packagesToImport.size() == 1) {
      perform(file, getFirstItem(packagesToImport));
    } else {
      String packages = StringUtil.join(packagesToImport, ",");
      throw new IncorrectOperationException("Cannot invoke fix with ambiguous imports on editor ()" + editor + ". Packages: " + packages);
    }
  }

  private void perform(@Nonnull PsiFile file, @Nullable String pathToImport) {
    if (file instanceof GoFile && pathToImport != null) {
      Project project = file.getProject();
      CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication().runWriteAction(() -> {
        if (!isAvailable()) return;
        if (((GoFile) file).getImportedPackagesMap().containsKey(pathToImport)) return;
        ((GoFile) file).addImport(pathToImport, null);
      }), "Add import", null);
    }
  }

  private static class MyImportsComparator implements Comparator<String> {
    @Nullable
    private final String myContextImportPath;

    public MyImportsComparator(@Nullable PsiElement context, boolean vendoringEnabled) {
      myContextImportPath = GoCompletionUtil.getContextImportPath(context, vendoringEnabled);
    }

    @Override
    public int compare(@Nonnull String s1, @Nonnull String s2) {
      int result = Comparing.compare(GoCompletionUtil.calculatePackagePriority(s2, myContextImportPath),
          GoCompletionUtil.calculatePackagePriority(s1, myContextImportPath));
      return result != 0 ? result : Comparing.compare(s1, s2);
    }
  }
}

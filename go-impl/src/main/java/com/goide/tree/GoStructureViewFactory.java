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

package com.goide.tree;

import com.goide.GoIcons;
import com.goide.GoLanguage;
import com.goide.psi.*;
import com.goide.psi.impl.GoPsiImplUtil;
import com.goide.sdk.GoPackageUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.IndexNotReadyException;
import consulo.codeEditor.Editor;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.TreeBasedStructureViewBuilder;
import consulo.fileEditor.structureView.tree.*;
import consulo.language.Language;
import consulo.language.editor.structureView.PsiStructureViewFactory;
import consulo.language.editor.structureView.PsiTreeElementBase;
import consulo.language.editor.structureView.StructureViewModelBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.StubBasedPsiElement;
import consulo.language.psi.SyntaxTraverser;
import consulo.language.psi.stub.StubElement;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.ui.ex.action.Shortcut;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ExtensionImpl
public class GoStructureViewFactory implements PsiStructureViewFactory {
  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder(@Nonnull PsiFile psiFile) {
    return new TreeBasedStructureViewBuilder() {
      @Nonnull
      @Override
      public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new Model(psiFile, editor);
      }

      @Override
      public boolean isRootNodeShown() {
        return false;
      }
    };
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }

  public static class Model extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
    private static final List<NodeProvider> PROVIDERS = List.of(new TreeElementFileStructureNodeProvider());

    Model(@Nonnull PsiFile file, @Nullable Editor editor) {
      super(file, editor, new Element(file));
      withSuitableClasses(GoFile.class, GoNamedElement.class)
        .withSorters(ExportabilitySorter.INSTANCE, Sorter.ALPHA_SORTER);
    }

    @Nonnull
    @Override
    public Filter[] getFilters() {
      return new Filter[]{new GoPrivateMembersFilter()};
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement structureViewTreeElement) {
      return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement structureViewTreeElement) {
      return false;
    }

    @Nonnull
    @Override
    public Collection<NodeProvider> getNodeProviders() {
      return PROVIDERS;
    }

    private static class TreeElementFileStructureNodeProvider implements FileStructureNodeProvider<TreeElement>, ActionShortcutProvider {
      public static final String ID = "Show package structure";

      @Nonnull
      @Override
      public ActionPresentation getPresentation() {
        return new ActionPresentationData(ID, null, GoIcons.PACKAGE);
      }

      @Nonnull
      @Override
      public String getName() {
        return ID;
      }

      @Nonnull
      @Override
      public Collection<TreeElement> provideNodes(@Nonnull TreeElement node) {
        PsiElement psi = node instanceof Element ? ((Element)node).getElement() : null;
        if (psi instanceof GoFile) {
          GoFile orig = (GoFile)psi;
          List<TreeElement> result = new ArrayList<>();
          for (GoFile f : GoPackageUtil.getAllPackageFiles(orig)) {
            if (f != orig) {
              ContainerUtil.addAll(result, new Element(f).getChildren());
            }
          }
          return result;
        }
        return Collections.emptyList();
      }

      @Nonnull
      @Override
      public String getCheckBoxText() {
        return ID;
      }

      @Nonnull
      @Override
      public Shortcut[] getShortcut() {
        throw new IncorrectOperationException("see getActionIdForShortcut()");
      }

      @Nonnull
      @Override
      public String getActionIdForShortcut() {
        return "FileStructurePopup";
      }
    }
  }

  public static class Element extends PsiTreeElementBase<PsiElement> {
    public Element(@Nonnull PsiElement e) {
      super(e);
    }

    @Nonnull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
      List<StructureViewTreeElement> result = ContainerUtil.newArrayList();
      PsiElement element = getElement();
      if (element instanceof GoFile) {
        for (GoTypeSpec o : ((GoFile)element).getTypes()) result.add(new Element(o));
        for (GoConstDefinition o : ((GoFile)element).getConstants()) result.add(new Element(o));
        for (GoVarDefinition o : ((GoFile)element).getVars()) result.add(new Element(o));
        for (GoFunctionDeclaration o : ((GoFile)element).getFunctions()) result.add(new Element(o));
        for (GoMethodDeclaration o : ((GoFile)element).getMethods()) {
          GoType type = o.getReceiverType();
          GoTypeReferenceExpression e = GoPsiImplUtil.getTypeReference(type);
          PsiElement resolve = e != null ? e.resolve() : null;
          if (resolve == null) {
            result.add(new Element(o));
          }
        }
      }
      else if (element instanceof GoTypeSpec) {
        GoTypeSpec typeSpec = (GoTypeSpec)element;
        GoType type = typeSpec.getSpecType().getType();
        for (GoMethodDeclaration m : typeSpec.getMethods()) result.add(new Element(m));
        if (type instanceof GoStructType) {
          for (GoFieldDeclaration field : ((GoStructType)type).getFieldDeclarationList()) {
            for (GoFieldDefinition definition : field.getFieldDefinitionList()) result.add(new Element(definition));
            GoAnonymousFieldDefinition anon = field.getAnonymousFieldDefinition();
            if (anon != null) result.add(new Element(anon));
          }
        }
        else if (type instanceof GoInterfaceType) {
          for (GoMethodSpec m : ((GoInterfaceType)type).getMethodSpecList()) result.add(new Element(m));
        }
      }
      else if (element instanceof GoFunctionOrMethodDeclaration) {
        StubElement<?> stub = ((StubBasedPsiElement)element).getStub();
        Iterable<GoTypeSpec> list =
          stub != null
          ? GoPsiTreeUtil.getStubChildrenOfTypeAsList(element, GoTypeSpec.class)
          : SyntaxTraverser.psiTraverser(((GoFunctionOrMethodDeclaration)element).getBlock()).filter(GoTypeSpec.class);
        for (GoTypeSpec s : list) {
          result.add(new Element(s));
        }
      }
      return result;
    }

    @Nullable
    @Override
    public String getPresentableText() {
      String textInner = getPresentationTextInner();
      return textInner != null ? textInner.replaceAll("\\(\\n", "(").replaceAll("\\n\\)", ")") : null;
    }

    @Nullable
    private String getPresentationTextInner() {
      PsiElement element = getElement();
      if (element == null) {
        return null;
      }
      String separator = ": ";
      if (element instanceof GoFile) {
        return ((GoFile)element).getName();
      }
      if (element instanceof GoNamedSignatureOwner) {
        GoSignature signature = ((GoNamedSignatureOwner)element).getSignature();
        String signatureText = signature != null ? signature.getText() : "";
        String name = ((GoNamedSignatureOwner)element).getName();
        return StringUtil.notNullize(name) + signatureText;
      }
      if (element instanceof GoTypeSpec) {
        GoType type = ((GoTypeSpec)element).getSpecType().getType();
        String appendix = type instanceof GoStructType || type instanceof GoInterfaceType ?
                          "" :
                          separator + GoPsiImplUtil.getText(type);
        return ((GoTypeSpec)element).getName() + appendix;
      }
      if (element instanceof GoNamedElement) {
        GoType type = null;
        try {
          type = ((GoNamedElement)element).getGoType(null);
        }
        catch (IndexNotReadyException ignored) {
        }
        String typeText = type == null || element instanceof GoAnonymousFieldDefinition ? "" : separator + GoPsiImplUtil.getText(type);
        return ((GoNamedElement)element).getName() + typeText;
      }
      Logger.getInstance(GoStructureViewFactory.class).error("Cannot get presentation for " + element.getClass().getName());
      return null;
    }
  }
}

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
import com.goide.util.GoPackageUtil;
import consulo.annotation.access.RequiredReadAction;
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
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.ui.ex.action.Shortcut;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ExtensionImpl
public class GoStructureViewFactory implements PsiStructureViewFactory {
    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder(PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
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

    @Override
    public Language getLanguage() {
        return GoLanguage.INSTANCE;
    }

    public static class Model extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
        private static final List<NodeProvider> PROVIDERS = List.of(new TreeElementFileStructureNodeProvider());

        Model(PsiFile file, @Nullable Editor editor) {
            super(file, editor, new Element(file));
            withSuitableClasses(GoFile.class, GoNamedElement.class)
                .withSorters(ExportabilitySorter.INSTANCE, Sorter.ALPHA_SORTER);
        }

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

        @Override
        public Collection<NodeProvider> getNodeProviders() {
            return PROVIDERS;
        }

        private static class TreeElementFileStructureNodeProvider implements FileStructureNodeProvider<TreeElement>, ActionShortcutProvider {
            public static final String ID = "Show package structure";

            @Override
            public ActionPresentation getPresentation() {
                return new ActionPresentationData(ID, null, GoIcons.PACKAGE);
            }

            @Override
            public String getName() {
                return ID;
            }

            @Override
            @RequiredReadAction
            public Collection<TreeElement> provideNodes(TreeElement node) {
                PsiElement psi = node instanceof Element element ? element.getElement() : null;
                if (psi instanceof GoFile) {
                    GoFile orig = (GoFile) psi;
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

            @Override
            public String getCheckBoxText() {
                return ID;
            }

            @Override
            public Shortcut[] getShortcut() {
                throw new IncorrectOperationException("see getActionIdForShortcut()");
            }

            @Override
            public String getActionIdForShortcut() {
                return "FileStructurePopup";
            }
        }
    }

    public static class Element extends PsiTreeElementBase<PsiElement> {
        public Element(PsiElement e) {
            super(e);
        }

        @Override
        @RequiredReadAction
        public Collection<StructureViewTreeElement> getChildrenBase() {
            List<StructureViewTreeElement> result = new ArrayList<>();
            PsiElement element = getElement();
            if (element instanceof GoFile goFile) {
                for (GoTypeSpec o : goFile.getTypes()) {
                    result.add(new Element(o));
                }
                for (GoConstDefinition o : goFile.getConstants()) {
                    result.add(new Element(o));
                }
                for (GoVarDefinition o : goFile.getVars()) {
                    result.add(new Element(o));
                }
                for (GoFunctionDeclaration o : goFile.getFunctions()) {
                    result.add(new Element(o));
                }
                for (GoMethodDeclaration o : goFile.getMethods()) {
                    GoType type = o.getReceiverType();
                    GoTypeReferenceExpression e = GoPsiImplUtil.getTypeReference(type);
                    PsiElement resolve = e != null ? e.resolve() : null;
                    if (resolve == null) {
                        result.add(new Element(o));
                    }
                }
            }
            else if (element instanceof GoTypeSpec typeSpec) {
                GoType type = typeSpec.getSpecType().getType();
                for (GoMethodDeclaration m : typeSpec.getMethods()) {
                    result.add(new Element(m));
                }
                if (type instanceof GoStructType structType) {
                    for (GoFieldDeclaration field : structType.getFieldDeclarationList()) {
                        for (GoFieldDefinition definition : field.getFieldDefinitionList()) {
                            result.add(new Element(definition));
                        }
                        GoAnonymousFieldDefinition anon = field.getAnonymousFieldDefinition();
                        if (anon != null) {
                            result.add(new Element(anon));
                        }
                    }
                }
                else if (type instanceof GoInterfaceType interfaceType) {
                    for (GoMethodSpec m : interfaceType.getMethodSpecList()) {
                        result.add(new Element(m));
                    }
                }
            }
            else if (element instanceof GoFunctionOrMethodDeclaration declaration) {
                StubElement<?> stub = ((StubBasedPsiElement) declaration).getStub();
                Iterable<GoTypeSpec> list =
                    stub != null
                        ? GoPsiTreeUtil.getStubChildrenOfTypeAsList(element, GoTypeSpec.class)
                        : SyntaxTraverser.psiTraverser(declaration.getBlock()).filter(GoTypeSpec.class);
                for (GoTypeSpec s : list) {
                    result.add(new Element(s));
                }
            }
            return result;
        }

        @Override
        @RequiredReadAction
        public LocalizeValue getPresentableText() {
            String textInner = getPresentationTextInner();
            return textInner != null
                ? LocalizeValue.of(textInner.replaceAll("\\(\\n", "(").replaceAll("\\n\\)", ")"))
                : LocalizeValue.empty();
        }

        @Nullable
        @RequiredReadAction
        private String getPresentationTextInner() {
            PsiElement element = getElement();
            if (element == null) {
                return null;
            }
            String separator = ": ";
            if (element instanceof GoFile goFile) {
                return goFile.getName();
            }
            if (element instanceof GoNamedSignatureOwner sigOwner) {
                GoSignature signature = sigOwner.getSignature();
                String signatureText = signature != null ? signature.getText() : "";
                String name = sigOwner.getName();
                return StringUtil.notNullize(name) + signatureText;
            }
            if (element instanceof GoTypeSpec typeSpec) {
                GoType type = typeSpec.getSpecType().getType();
                String appendix = type instanceof GoStructType || type instanceof GoInterfaceType
                    ? ""
                    : separator + GoPsiImplUtil.getText(type);
                return typeSpec.getName() + appendix;
            }
            if (element instanceof GoNamedElement namedElem) {
                GoType type = null;
                try {
                    type = namedElem.getGoType(null);
                }
                catch (IndexNotReadyException ignored) {
                }
                String typeText = type == null || element instanceof GoAnonymousFieldDefinition
                    ? ""
                    : separator + GoPsiImplUtil.getText(type);
                return namedElem.getName() + typeText;
            }
            Logger.getInstance(GoStructureViewFactory.class).error("Cannot get presentation for " + element.getClass().getName());
            return null;
        }
    }
}

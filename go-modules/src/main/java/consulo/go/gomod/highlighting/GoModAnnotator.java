/*
 * Copyright 2013-2026 consulo.io
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

package consulo.go.gomod.highlighting;

import consulo.colorScheme.TextAttributesKey;
import consulo.go.gomod.psi.GoModFile;
import consulo.go.gomod.psi.GoModFilePath;
import consulo.go.gomod.psi.GoModGoVersion;
import consulo.go.gomod.psi.GoModGodebugKey;
import consulo.go.gomod.psi.GoModModulePath;
import consulo.go.gomod.psi.GoModToolchainName;
import consulo.go.gomod.psi.GoModTypes;
import consulo.go.gomod.psi.GoModUnknownDirective;
import consulo.go.gomod.psi.GoModVersion;
import consulo.go.localize.GoLocalize;
import consulo.language.ast.IElementType;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Colors what the lexer cannot tell apart - module paths, versions, directories - and reports what {@code go} would
 * reject: unknown directives, directives of the other file kind, repeated single directives.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModAnnotator implements Annotator {
    private static final Set<IElementType> WORK_DIRECTIVES = Set.of(GoModTypes.GO_DIRECTIVE,
        GoModTypes.TOOLCHAIN_DIRECTIVE,
        GoModTypes.GODEBUG_DIRECTIVE,
        GoModTypes.USE_DIRECTIVE,
        GoModTypes.REPLACE_DIRECTIVE);
    private static final Set<IElementType> MOD_ONLY_DIRECTIVES = Set.of(GoModTypes.MODULE_DIRECTIVE,
        GoModTypes.REQUIRE_DIRECTIVE,
        GoModTypes.EXCLUDE_DIRECTIVE,
        GoModTypes.RETRACT_DIRECTIVE,
        GoModTypes.TOOL_DIRECTIVE,
        GoModTypes.IGNORE_DIRECTIVE);
    private static final Set<IElementType> SINGLE_DIRECTIVES = Set.of(GoModTypes.MODULE_DIRECTIVE,
        GoModTypes.GO_DIRECTIVE,
        GoModTypes.TOOLCHAIN_DIRECTIVE);

    @Override
    public void annotate(PsiElement element, AnnotationHolder holder) {
        TextAttributesKey key = colorOf(element);
        if (key != null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(key).create();
            return;
        }

        if (element instanceof GoModUnknownDirective) {
            PsiElement name = element.getFirstChild();
            if (name != null) {
                error(holder, name, GoLocalize.goModAnnotatorUnknownDirective(name.getText()));
            }
            return;
        }

        if (!(element.getParent() instanceof GoModFile file)) {
            return;
        }
        IElementType type = element.getNode().getElementType();
        PsiElement keyword = element.getFirstChild();
        if (keyword == null) {
            return;
        }
        if (file.isWorkFile() && MOD_ONLY_DIRECTIVES.contains(type)) {
            error(holder, keyword, GoLocalize.goModAnnotatorDirectiveNotInWork(keyword.getText()));
        }
        else if (!file.isWorkFile() && type == GoModTypes.USE_DIRECTIVE) {
            error(holder, keyword, GoLocalize.goModAnnotatorDirectiveOnlyInWork(keyword.getText()));
        }
        else if (SINGLE_DIRECTIVES.contains(type) && hasPreviousSibling(element, type)) {
            error(holder, keyword, GoLocalize.goModAnnotatorRepeatedDirective(keyword.getText()));
        }
    }

    private static void error(AnnotationHolder holder, PsiElement range, LocalizeValue message) {
        holder.newAnnotation(HighlightSeverity.ERROR, message).range(range).create();
    }

    private static @Nullable TextAttributesKey colorOf(PsiElement element) {
        if (element instanceof GoModModulePath) {
            return GoModHighlightingColors.MODULE_PATH;
        }
        if (element instanceof GoModVersion || element instanceof GoModGoVersion || element instanceof GoModToolchainName) {
            return GoModHighlightingColors.VERSION;
        }
        if (element instanceof GoModFilePath) {
            return GoModHighlightingColors.FILE_PATH;
        }
        if (element instanceof GoModGodebugKey) {
            return GoModHighlightingColors.GODEBUG_KEY;
        }
        return null;
    }

    private static boolean hasPreviousSibling(PsiElement element, IElementType type) {
        for (PsiElement sibling = element.getPrevSibling(); sibling != null; sibling = sibling.getPrevSibling()) {
            if (sibling.getNode().getElementType() == type) {
                return true;
            }
        }
        return false;
    }
}

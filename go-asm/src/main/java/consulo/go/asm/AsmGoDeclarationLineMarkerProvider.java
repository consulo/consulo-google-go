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
package consulo.go.asm;

import com.goide.psi.GoFunctionDeclaration;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.asm.language.AsmLanguage;
import consulo.asm.language.impl.psi.AsmLabelElement;
import consulo.go.icon.GoIconGroup;
import consulo.go.localize.GoLocalize;
import consulo.language.Language;
import consulo.language.editor.gutter.RelatedItemLineMarkerInfo;
import consulo.language.editor.gutter.RelatedItemLineMarkerProvider;
import consulo.language.editor.ui.navigation.NavigationGutterIconBuilder;
import consulo.language.psi.PsiElement;

import java.util.Collection;
import java.util.List;

/**
 * {@code TEXT ·hexDecode(SB)}: gutter icon to the Go declaration it implements.
 *
 * @author VISTALL
 * @since 2026-10-20
 */
@ExtensionImpl
public class AsmGoDeclarationLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @RequiredReadAction
    @Override
    protected void collectNavigationMarkers(PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        // markers go on leaves; the symbol element wraps exactly one
        if (!(element.getParent() instanceof AsmLabelElement symbol) || !symbol.isSymbol()) {
            return;
        }
        List<GoFunctionDeclaration> declarations = GoAsmUtil.findGoDeclarations(symbol);
        if (declarations.isEmpty()) {
            return;
        }
        result.add(NavigationGutterIconBuilder.create(GoIconGroup.go())
            .setTargets(declarations)
            .setTooltipText(GoLocalize.asmGoDeclarationTooltip())
            .createLineMarkerInfo(element));
    }

    @Override
    public Language getLanguage() {
        return AsmLanguage.INSTANCE;
    }
}

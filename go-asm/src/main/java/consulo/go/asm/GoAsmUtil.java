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

import com.goide.psi.GoFile;
import com.goide.psi.GoFunctionDeclaration;
import consulo.annotation.access.RequiredReadAction;
import consulo.asm.language.impl.psi.AsmFile;
import consulo.asm.language.impl.psi.AsmLabelElement;
import consulo.asm.language.impl.psi.AsmLabelResolver;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;

/**
 * A package's Go functions and assembly symbols live in the same directory; {@code ·name} in a {@code .s} file is the
 * function {@code name} of that package.
 *
 * @author VISTALL
 * @since 2026-10-20
 */
public final class GoAsmUtil {
    private static final char MIDDLE_DOT = '\u00b7';

    private GoAsmUtil() {
    }

    /**
     * The Go function name a package-local assembly symbol implements, {@code ·hexDecode} → {@code hexDecode}.
     */
    public static String goName(String symbol) {
        return symbol.length() > 1 && symbol.charAt(0) == MIDDLE_DOT ? symbol.substring(1) : "";
    }

    @RequiredReadAction
    public static List<AsmLabelElement> findAsmImplementations(GoFunctionDeclaration function) {
        String name = function.getName();
        PsiDirectory directory = function.getContainingFile().getContainingDirectory();
        if (name == null || directory == null) {
            return List.of();
        }
        List<AsmLabelElement> result = new ArrayList<>();
        for (PsiFile file : directory.getFiles()) {
            if (file instanceof AsmFile) {
                for (AsmLabelElement label : AsmLabelResolver.getLabels(file)) {
                    if (label.isSymbol() && goName(label.getName()).equals(name)) {
                        result.add(label);
                    }
                }
            }
        }
        return result;
    }

    @RequiredReadAction
    public static List<GoFunctionDeclaration> findGoDeclarations(AsmLabelElement symbol) {
        String name = goName(symbol.getName());
        PsiDirectory directory = symbol.getContainingFile().getContainingDirectory();
        if (name.isEmpty() || directory == null) {
            return List.of();
        }
        List<GoFunctionDeclaration> result = new ArrayList<>();
        for (PsiFile file : directory.getFiles()) {
            if (file instanceof GoFile goFile) {
                for (GoFunctionDeclaration function : goFile.getFunctions()) {
                    if (name.equals(function.getName())) {
                        result.add(function);
                    }
                }
            }
        }
        return result;
    }
}

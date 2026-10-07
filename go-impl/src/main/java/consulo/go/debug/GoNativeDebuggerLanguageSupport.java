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
package consulo.go.debug;

import com.goide.GoLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.nativeDev.debugger.NativeDebuggerLanguageSupport;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-07
 */
@ExtensionImpl
public class GoNativeDebuggerLanguageSupport implements NativeDebuggerLanguageSupport {
    @Override
    public Language getLanguage() {
        return GoLanguage.INSTANCE;
    }

    @Override
    public String getDebuggerLanguage() {
        return "go";
    }

    @Override
    public PsiFile createExpressionCodeFragment(Project project, String text, @Nullable PsiElement context, boolean isPhysical) {
        return PsiFileFactory.getInstance(project).createFileFromText("fragment.go", GoLanguage.INSTANCE, text, isPhysical, false);
    }
}

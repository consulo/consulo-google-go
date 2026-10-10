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

package consulo.go.gomod.psi;

import consulo.go.gomod.GoModLanguage;
import consulo.go.gomod.GoWorkFileType;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;

/**
 * A {@code go.mod} or {@code go.work}; {@link #getFileType()} tells which.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModFile extends PsiFileBase {
    public GoModFile(FileViewProvider viewProvider) {
        super(viewProvider, GoModLanguage.INSTANCE);
    }

    public boolean isWorkFile() {
        return getFileType() == GoWorkFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return isWorkFile() ? "go.work" : "go.mod";
    }
}

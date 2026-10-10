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

package consulo.go.gomod;

import consulo.go.localize.GoLocalize;
import consulo.language.Language;
import consulo.localize.LocalizeValue;

/**
 * The language of {@code go.mod} and {@code go.work}: both are lists of directives with the same syntax.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoModLanguage extends Language {
    public static final GoModLanguage INSTANCE = new GoModLanguage();

    private GoModLanguage() {
        super("GoMod");
    }

    @Override
    public LocalizeValue getDisplayName() {
        return GoLocalize.goModLanguageDisplayName();
    }
}

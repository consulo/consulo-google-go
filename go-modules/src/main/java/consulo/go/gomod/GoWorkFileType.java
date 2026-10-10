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

import consulo.go.icon.GoIconGroup;
import consulo.go.localize.GoLocalize;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;

/**
 * {@code go.work}: the modules of a workspace.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
public final class GoWorkFileType extends LanguageFileType {
    public static final GoWorkFileType INSTANCE = new GoWorkFileType();

    private GoWorkFileType() {
        super(GoModLanguage.INSTANCE);
    }

    @Override
    public String getId() {
        return "GO_WORK";
    }

    @Override
    public LocalizeValue getDescription() {
        return GoLocalize.goWorkFileDescription();
    }

    @Override
    public Image getIcon() {
        return GoIconGroup.gomod();
    }
}

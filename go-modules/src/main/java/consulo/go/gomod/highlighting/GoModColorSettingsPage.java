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

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.go.localize.GoLocalize;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.localize.LocalizeValue;

import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = {
        new AttributesDescriptor(GoLocalize.goModColorKeyword(), GoModHighlightingColors.KEYWORD),
        new AttributesDescriptor(GoLocalize.goModColorComment(), GoModHighlightingColors.LINE_COMMENT),
        new AttributesDescriptor(GoLocalize.goModColorString(), GoModHighlightingColors.STRING),
        new AttributesDescriptor(GoLocalize.goModColorModulePath(), GoModHighlightingColors.MODULE_PATH),
        new AttributesDescriptor(GoLocalize.goModColorVersion(), GoModHighlightingColors.VERSION),
        new AttributesDescriptor(GoLocalize.goModColorFilePath(), GoModHighlightingColors.FILE_PATH),
        new AttributesDescriptor(GoLocalize.goModColorGodebugKey(), GoModHighlightingColors.GODEBUG_KEY),
        new AttributesDescriptor(GoLocalize.goModColorOperator(), GoModHighlightingColors.OPERATOR),
        new AttributesDescriptor(GoLocalize.goModColorParentheses(), GoModHighlightingColors.PARENTHESES),
        new AttributesDescriptor(GoLocalize.goModColorBrackets(), GoModHighlightingColors.BRACKETS),
        new AttributesDescriptor(GoLocalize.goModColorComma(), GoModHighlightingColors.COMMA),
        new AttributesDescriptor(GoLocalize.goModColorBadCharacter(), GoModHighlightingColors.BAD_CHARACTER),
    };

    private static final Map<String, TextAttributesKey> TAGS = Map.of(
        "mp", GoModHighlightingColors.MODULE_PATH,
        "v", GoModHighlightingColors.VERSION,
        "fp", GoModHighlightingColors.FILE_PATH,
        "gk", GoModHighlightingColors.GODEBUG_KEY);

    @Override
    public LocalizeValue getDisplayName() {
        return GoLocalize.goModLanguageDisplayName();
    }

    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public SyntaxHighlighter getHighlighter() {
        return new GoModSyntaxHighlighter();
    }

    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return TAGS;
    }

    @Override
    public String getDemoText() {
        return """
            // go.mod
            module <mp>example.com/hello</mp>

            go <v>1.22</v>

            toolchain <v>go1.22.4</v>

            godebug <gk>default</gk>=go1.21

            require (
            \t<mp>golang.org/x/text</mp> <v>v0.14.0</v>
            \t<mp>rsc.io/quote</mp> <v>v1.5.2</v> // indirect
            )

            exclude <mp>golang.org/x/net</mp> <v>v1.2.3</v>

            replace <mp>golang.org/x/net</mp> <v>v1.2.3</v> => <mp>example.com/fork/net</mp> <v>v1.4.5</v>

            replace <mp>example.com/local</mp> => <fp>../local</fp>

            retract [<v>v1.0.0</v>, <v>v1.9.9</v>] // published by mistake

            tool <mp>golang.org/x/tools/cmd/stringer</mp>

            // go.work
            use (
            \t<fp>./app</fp>
            \t<fp>"./lib"</fp>
            )
            """;
    }
}

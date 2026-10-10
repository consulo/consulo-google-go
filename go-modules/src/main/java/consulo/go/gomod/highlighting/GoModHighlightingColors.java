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

import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.codeEditor.HighlighterColors;
import consulo.colorScheme.TextAttributesKey;

import static consulo.colorScheme.TextAttributesKey.createTextAttributesKey;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public interface GoModHighlightingColors {
    TextAttributesKey KEYWORD = createTextAttributesKey("GO_MOD_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    TextAttributesKey LINE_COMMENT = createTextAttributesKey("GO_MOD_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    TextAttributesKey STRING = createTextAttributesKey("GO_MOD_STRING", DefaultLanguageHighlighterColors.STRING);
    TextAttributesKey IDENTIFIER = createTextAttributesKey("GO_MOD_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    TextAttributesKey OPERATOR = createTextAttributesKey("GO_MOD_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    TextAttributesKey PARENTHESES = createTextAttributesKey("GO_MOD_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    TextAttributesKey BRACKETS = createTextAttributesKey("GO_MOD_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    TextAttributesKey COMMA = createTextAttributesKey("GO_MOD_COMMA", DefaultLanguageHighlighterColors.COMMA);
    TextAttributesKey BAD_CHARACTER = createTextAttributesKey("GO_MOD_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);

    // set by GoModAnnotator, from the tree
    TextAttributesKey MODULE_PATH = createTextAttributesKey("GO_MOD_MODULE_PATH", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    TextAttributesKey VERSION = createTextAttributesKey("GO_MOD_VERSION", DefaultLanguageHighlighterColors.NUMBER);
    TextAttributesKey FILE_PATH = createTextAttributesKey("GO_MOD_FILE_PATH", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    TextAttributesKey GODEBUG_KEY = createTextAttributesKey("GO_MOD_GODEBUG_KEY", DefaultLanguageHighlighterColors.PARAMETER);
}

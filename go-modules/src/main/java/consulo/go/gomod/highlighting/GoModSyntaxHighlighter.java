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
import consulo.go.gomod.GoModParserDefinition;
import consulo.go.gomod.lexer.GoModLexer;
import consulo.go.gomod.psi.GoModTypes;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoModSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

    static {
        fillMap(ATTRIBUTES, GoModParserDefinition.KEYWORDS, GoModHighlightingColors.KEYWORD);
        ATTRIBUTES.put(GoModTypes.LINE_COMMENT, GoModHighlightingColors.LINE_COMMENT);
        ATTRIBUTES.put(GoModTypes.STRING, GoModHighlightingColors.STRING);
        ATTRIBUTES.put(GoModTypes.IDENTIFIER, GoModHighlightingColors.IDENTIFIER);
        ATTRIBUTES.put(GoModTypes.ARROW, GoModHighlightingColors.OPERATOR);
        ATTRIBUTES.put(GoModTypes.EQ, GoModHighlightingColors.OPERATOR);
        ATTRIBUTES.put(GoModTypes.LPAREN, GoModHighlightingColors.PARENTHESES);
        ATTRIBUTES.put(GoModTypes.RPAREN, GoModHighlightingColors.PARENTHESES);
        ATTRIBUTES.put(GoModTypes.LBRACK, GoModHighlightingColors.BRACKETS);
        ATTRIBUTES.put(GoModTypes.RBRACK, GoModHighlightingColors.BRACKETS);
        ATTRIBUTES.put(GoModTypes.COMMA, GoModHighlightingColors.COMMA);
        ATTRIBUTES.put(TokenType.BAD_CHARACTER, GoModHighlightingColors.BAD_CHARACTER);
    }

    @Override
    public Lexer getHighlightingLexer() {
        return new GoModLexer();
    }

    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(ATTRIBUTES.get(tokenType));
    }
}

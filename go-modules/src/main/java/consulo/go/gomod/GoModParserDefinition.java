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

import consulo.annotation.component.ExtensionImpl;
import consulo.go.gomod.lexer.GoModLexer;
import consulo.go.gomod.parser.GoModParser;
import consulo.go.gomod.psi.GoModFile;
import consulo.go.gomod.psi.GoModTypes;
import consulo.go.gomod.psi.impl.GoModTypesFactory;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType("GO_MOD_FILE", GoModLanguage.INSTANCE);

    public static final TokenSet COMMENTS = TokenSet.create(GoModTypes.LINE_COMMENT);
    public static final TokenSet STRINGS = TokenSet.create(GoModTypes.STRING);
    public static final TokenSet KEYWORDS = TokenSet.create(GoModTypes.MODULE,
        GoModTypes.GO,
        GoModTypes.TOOLCHAIN,
        GoModTypes.GODEBUG,
        GoModTypes.REQUIRE,
        GoModTypes.EXCLUDE,
        GoModTypes.REPLACE,
        GoModTypes.RETRACT,
        GoModTypes.TOOL,
        GoModTypes.IGNORE,
        GoModTypes.USE);

    @Override
    public Language getLanguage() {
        return GoModLanguage.INSTANCE;
    }

    @Override
    public Lexer createLexer(LanguageVersion languageVersion) {
        return new GoModLexer();
    }

    @Override
    public PsiParser createParser(LanguageVersion languageVersion) {
        return new GoModParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public TokenSet getCommentTokens(LanguageVersion languageVersion) {
        return COMMENTS;
    }

    @Override
    public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
        return STRINGS;
    }

    @Override
    public PsiElement createElement(ASTNode node) {
        return GoModTypesFactory.createElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new GoModFile(viewProvider);
    }
}

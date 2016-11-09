package ro.redeul.google.go.lang.parser;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.lang.LanguageVersion;
import ro.redeul.google.go.GoFileType;
import ro.redeul.google.go.lang.lexer.GoLexer;
import ro.redeul.google.go.lang.lexer.GoTokenTypeSets;
import ro.redeul.google.go.lang.psi.impl.GoFileImpl;
import ro.redeul.google.go.lang.psi.stubs.elements.GoStubFileElementType;

public class GoParserDefinition implements ParserDefinition {

    public static final IStubFileElementType GO_FILE_TYPE =
        new GoStubFileElementType(GoFileType.INSTANCE.getLanguage());

    @NotNull
    public Lexer createLexer(LanguageVersion languageVersion) {
        return new GoLexer();
    }

    public PsiParser createParser(LanguageVersion languageVersion) {
        return new GoParser();
    }

    public IFileElementType getFileNodeType() {
        return GO_FILE_TYPE;
    }

    @NotNull
    public TokenSet getWhitespaceTokens(LanguageVersion languageVersion) {
        return GoTokenTypeSets.WHITESPACES;
    }

    @NotNull
    public TokenSet getCommentTokens(LanguageVersion languageVersion) {
        return GoTokenTypeSets.COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
        return GoTokenTypeSets.STRINGS;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return GoPsiCreator.createElement(node);
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new GoFileImpl(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left,
                                                             ASTNode right) {
        return SpaceRequirements.MAY;
    }
}

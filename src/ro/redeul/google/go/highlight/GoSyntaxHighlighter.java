package ro.redeul.google.go.highlight;

import static ro.redeul.google.go.lang.lexer.GoTokenTypeSets.BAD_TOKENS;
import static ro.redeul.google.go.lang.lexer.GoTokenTypeSets.BLOCK_COMMENTS;
import static ro.redeul.google.go.lang.lexer.GoTokenTypeSets.IDENTIFIERS;
import static ro.redeul.google.go.lang.lexer.GoTokenTypeSets.KEYWORDS;
import static ro.redeul.google.go.lang.lexer.GoTokenTypeSets.LINE_COMMENTS;
import static ro.redeul.google.go.lang.lexer.GoTokenTypeSets.NUMBERS;
import static ro.redeul.google.go.lang.lexer.GoTokenTypeSets.OPERATORS;
import static ro.redeul.google.go.lang.lexer.GoTokenTypeSets.STRINGS;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import ro.redeul.google.go.GoLanguage;
import ro.redeul.google.go.lang.lexer.GoLexer;
import ro.redeul.google.go.lang.lexer.GoTokenTypes;

public class GoSyntaxHighlighter extends SyntaxHighlighterBase implements GoTokenTypes
{
	public static final TextAttributesKey LINE_COMMENT = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.LINE_COMMENT);
	public static final TextAttributesKey BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.BLOCK_COMMENT);
	public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.KEYWORD);
	public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.STRING);
	public static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.NUMBER);
	public static final TextAttributesKey BRACKET = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.BRACKETS);
	public static final TextAttributesKey OPERATOR = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.OPERATION_SIGN);
	public static final TextAttributesKey IDENTIFIER = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
	public static final TextAttributesKey TYPE_NAME = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.TYPE_ALIAS_NAME);
	public static final TextAttributesKey VARIABLE = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.INSTANCE_FIELD);
	public static final TextAttributesKey CONST = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.STATIC_FIELD);
	public static final TextAttributesKey GLOBAL_VARIABLE = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.STATIC_FIELD);
	public static final TextAttributesKey METHOD_DECLARATION = TextAttributesKey.createTextAttributesKey(GoLanguage.INSTANCE, DefaultLanguageHighlighterColors.INSTANCE_METHOD);

	private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<IElementType, TextAttributesKey>();

	static
	{
		safeMap(ATTRIBUTES, LINE_COMMENTS, LINE_COMMENT);
		safeMap(ATTRIBUTES, BLOCK_COMMENTS, BLOCK_COMMENT);
		safeMap(ATTRIBUTES, KEYWORDS, KEYWORD);
		safeMap(ATTRIBUTES, NUMBERS, NUMBER);
		safeMap(ATTRIBUTES, STRINGS, STRING);
		safeMap(ATTRIBUTES, TokenSet.create(pLPAREN, pRPAREN), BRACKET);
		safeMap(ATTRIBUTES, TokenSet.create(pLBRACK, pRBRACK), BRACKET);
		safeMap(ATTRIBUTES, TokenSet.create(pLCURLY, pRCURLY), BRACKET);
		safeMap(ATTRIBUTES, OPERATORS, OPERATOR);
		safeMap(ATTRIBUTES, IDENTIFIERS, IDENTIFIER);
		safeMap(ATTRIBUTES, BAD_TOKENS, CodeInsightColors.ERRORS_ATTRIBUTES);
	}

	@NotNull
	public Lexer getHighlightingLexer()
	{
		return new GoLexer();
	}

	@NotNull
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType)
	{
		return pack(ATTRIBUTES.get(tokenType));
	}
}

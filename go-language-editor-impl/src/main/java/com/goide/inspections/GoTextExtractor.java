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
package com.goide.inspections;

import com.goide.GoLanguage;
import com.goide.GoParserDefinition;
import com.goide.psi.GoImportString;
import com.goide.psi.GoStringLiteral;
import com.goide.psi.GoTag;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.spellcheker.text.TextContent;
import consulo.language.spellcheker.text.TextContentBuilder;
import consulo.language.spellcheker.text.TextExtractor;
import consulo.language.spellcheker.text.util.EscapeUtils;
import consulo.language.spellcheker.text.util.PsiUtils;
import consulo.language.spellcheker.text.util.Text;
import consulo.util.collection.ContainerUtil;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Natural language text in Go files: comments (without tool directives) and prose-like string literals.
 *
 * @author VISTALL
 * @since 2026-11-02
 */
@ExtensionImpl
public class GoTextExtractor extends TextExtractor {
    private static final Pattern DIRECTIVE_COMMENT =
        Pattern.compile("//(go:|nolint|lint:|export\\s|extern\\s|line\\s|\\s*\\+build)");
    private static final Pattern FORMAT_VERB =
        Pattern.compile("%[-+#0]*(\\[\\d+])?(\\*|\\d+)?(\\.(\\[\\d+])?(\\*|\\d+)?)?(\\[\\d+])?[a-zA-Z%]");

    private static final TextContentBuilder COMMENT_BUILDER =
        TextContentBuilder.FromPsi.removingIndents(" \t/*").removingLineSuffixes(" \t*/");

    @Override
    @RequiredReadAction
    protected List<TextContent> buildTextContents(PsiElement element, Set<TextContent.TextDomain> allowedDomains) {
        if (element instanceof PsiComment) {
            if (!allowedDomains.contains(TextContent.TextDomain.COMMENTS)) {
                return List.of();
            }
            return buildComment(element);
        }

        if (element instanceof GoStringLiteral literal) {
            if (!allowedDomains.contains(TextContent.TextDomain.LITERALS)) {
                return List.of();
            }
            return asList(buildLiteral(literal));
        }
        return List.of();
    }

    @RequiredReadAction
    private static List<TextContent> buildComment(PsiElement element) {
        IElementType type = PsiUtilCore.getElementType(element);
        if (type == GoParserDefinition.MULTILINE_COMMENT) {
            return asList(COMMENT_BUILDER.build(element, TextContent.TextDomain.COMMENTS));
        }
        if (type != GoParserDefinition.LINE_COMMENT || isDirective(element)) {
            return List.of();
        }

        List<PsiElement> siblings = PsiUtils.getNotSoDistantSimilarSiblings(
            element,
            it -> PsiUtilCore.getElementType(it) == GoParserDefinition.LINE_COMMENT && !isDirective(it)
        );
        return asList(TextContent.joinWithWhitespace(
            '\n',
            ContainerUtil.mapNotNull(siblings, it -> COMMENT_BUILDER.build(it, TextContent.TextDomain.COMMENTS))
        ));
    }

    @RequiredReadAction
    private static @Nullable TextContent buildLiteral(GoStringLiteral literal) {
        PsiElement parent = literal.getParent();
        if (parent instanceof GoImportString || parent instanceof GoTag) {
            return null;
        }

        String text = literal.getText();
        // identifier-like values (keys, paths, MIME types) are not prose
        if (text.indexOf(' ') < 0) {
            return null;
        }

        TextContent content = TextContentBuilder.FromPsi.build(literal, TextContent.TextDomain.LITERALS);
        if (content == null) {
            return null;
        }
        if (literal.getString() != null) {
            content = EscapeUtils.replaceBackslashEscapes(content);
        }
        List<TextContent.Exclusion> verbs =
            ContainerUtil.map(Text.allOccurrences(FORMAT_VERB, content), TextContent.Exclusion::markUnknown);
        return content.excludeRanges(verbs).trimWhitespace();
    }

    @RequiredReadAction
    private static boolean isDirective(PsiElement comment) {
        return DIRECTIVE_COMMENT.matcher(comment.getText()).lookingAt();
    }

    private static List<TextContent> asList(@Nullable TextContent content) {
        return content == null ? List.of() : List.of(content);
    }

    @Override
    public Language getLanguage() {
        return GoLanguage.INSTANCE;
    }
}

/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide;


import consulo.language.ast.IElementType;
import consulo.language.psi.PsiComment;
import consulo.util.collection.ContainerUtil;
import consulo.util.io.URLUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.xml.XmlStringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * See https://golang.org/src/go/doc/comment.go
 */
public class GoCommentsConverter {
  private static final Pattern LEADING_TAB = Pattern.compile("^\\t", Pattern.MULTILINE);
  
  @Nonnull
  public String toText(@Nonnull List<PsiComment> comments) {
    return StringUtil.join(getStrings(comments), "\n");
  }

  @Nonnull
  public String toHtml(@Nonnull List<PsiComment> comments) {
    return textToHtml(getStrings(comments));
  }

  @Nonnull
  private static List<String> getStrings(@Nonnull List<PsiComment> comments) {
    List<String> strings = ContainerUtil.newArrayList();
    for (PsiComment comment : comments) {
      IElementType type = comment.getTokenType();
      if (type == GoParserDefinition.LINE_COMMENT) {
        strings.add(StringUtil.trimStart(StringUtil.trimStart(comment.getText(), "//"), " "));
      }
      else if (type == GoParserDefinition.MULTILINE_COMMENT) {
        String text = StringUtil.trimEnd(comment.getText(), "*/");
        text = StringUtil.trimStart(text, "/*");
        text = LEADING_TAB.matcher(text).replaceAll("");
        Collections.addAll(strings, StringUtil.splitByLines(text, false));
      }
    }
    return strings;
  }

  @Nonnull
  public String textToHtml(@Nonnull List<String> strings) {
    State state = new State(strings.iterator());
    while (state.hasNext()) {
      state.next();
      if (state.indented()) {
        state.flushBlock("p");
        processIndentedBlock(state);
      }
      processSimpleBlock(state);
    }
    state.flushBlock("p");
    return state.result();
  }

  private static void processSimpleBlock(@Nonnull State state) {
    if (state.isBlank()) {
      state.flushBlock("p");
    }
    else {
      state.append(state.text, true);
      state.append("\n"); // just for prettier testdata
    }
  }

  private static void processIndentedBlock(@Nonnull State state) {
    state.append(state.text);
    int emptyLines = 1;
    String text;
    while ((text = state.next()) != null) {
      if (state.isBlank()) {
        emptyLines++;
      }
      else if (state.indented()) {
        state.append(StringUtil.repeatSymbol('\n', emptyLines)).append(text);
        emptyLines = 1;
      }
      else {
        break;
      }
    }
    state.flushBlock("pre");
  }

  private static class State {
    @Nonnull
	private final StringBuilder currentBlock = new StringBuilder();
    @Nonnull
	private final StringBuilder result = new StringBuilder();
    @Nonnull
	private final Iterator<String> iterator;
    @Nullable
	private String text;

    public State(@Nonnull Iterator<String> iterator) {
      this.iterator = iterator;
    }

    String next() {
      return text = iterator.hasNext() ? iterator.next() : null;
    }

    boolean isBlank() {
      return text != null && text.trim().isEmpty();
    }

    boolean hasNext() {
      return iterator.hasNext();
    }

    boolean indented() {
      return text != null && (StringUtil.startsWithChar(text, ' ') || StringUtil.startsWithChar(text, '\t'));
    }

    State append(@Nullable String text, boolean nice) {
      if (StringUtil.isNotEmpty(text)) {
        currentBlock.append(emphasize(text, nice));
      }
      return this;
    }

    State append(@Nullable String text) {
      return append(text, false);
    }

    /**
     * Escape comment text for HTML. If nice is set, also turn `` into &ldquo; and '' into &rdquo;.
     */
    private static String emphasize(@Nonnull String text, boolean nice) {
      text = XmlStringUtil.escapeString(text);
      StringBuilder textWithLinks = null;
      Matcher matcher = URLUtil.URL_PATTERN.matcher(text);
      while (matcher.find()) {
        if (textWithLinks == null) {
          textWithLinks = new StringBuilder();
        }
        textWithLinks.append(text.substring(0, matcher.start()))
          .append("<a href=\"").append(matcher.group()).append("\">").append(matcher.group()).append("</a>");
      }
      if (textWithLinks != null) {
        text = textWithLinks.toString();
      }
      
      return nice ? StringUtil.replace(text, new String[]{"``", "''"}, new String[]{"&ldquo;", "&rdquo;"}) : text;
    }

    void flushBlock(@Nonnull String wrapTag) {
      if (currentBlock.length() > 0) {
        result.append('<').append(wrapTag).append(">").append(currentBlock).append("</").append(wrapTag).append(">\n");
        currentBlock.setLength(0);
      }
    }

    String result() {
      return result.toString();
    }
  }
}

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

package com.plan9.intel.ide.highlighting;

import static com.plan9.intel.ide.highlighting.AsmIntelSyntaxHighlightingColors.*;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.util.ResourceUtil;

public class AsmIntelColorsAndFontsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
    new AttributesDescriptor("Keyword", KEYWORD),
    new AttributesDescriptor("Line Comment", LINE_COMMENT),
    new AttributesDescriptor("Instruction", INSTRUCTION),
    new AttributesDescriptor("Pseudo Instruction", PSEUDO_INSTRUCTION),
    new AttributesDescriptor("String", STRING),
    new AttributesDescriptor("Label", LABEL),
    new AttributesDescriptor("Flags", FLAG),
    new AttributesDescriptor("Registers", REGISTER),
    new AttributesDescriptor("Parenthesis", PARENTHESIS),
    new AttributesDescriptor("Operator", OPERATOR),
    new AttributesDescriptor("Identifier", IDENTIFIER),
  };

  @Nonnull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new AsmIntelSyntaxHighlighter();
  }

  private String DEMO_TEXT;

  @Nonnull
  @Override
  public String getDemoText() {
    if (DEMO_TEXT == null) {
      try {
        URL resource = getClass().getClassLoader().getResource("colorscheme/highlighterDemoText.s");
        DEMO_TEXT = resource != null ? ResourceUtil.loadText(resource) : "";
      }
      catch (IOException e) {
        DEMO_TEXT = "";
      }
    }

    return DEMO_TEXT;
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @Nonnull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Nonnull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "x86 Assembler";
  }
}

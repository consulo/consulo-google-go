package com.plan9.intel.ide.highlighting;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.AdditionalTextAttributesProvider;
import consulo.colorScheme.EditorColorsScheme;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 03-Jul-22
 */
@ExtensionImpl
public class DefaultAdditionalTextAttributesProvider implements AdditionalTextAttributesProvider {
  @Nonnull
  @Override
  public String getColorSchemeName() {
    return EditorColorsScheme.DEFAULT_SCHEME_NAME;
  }

  @Nonnull
  @Override
  public String getColorSchemeFile() {
    return "/colorscheme/AsmIntelDefault.xml";
  }
}

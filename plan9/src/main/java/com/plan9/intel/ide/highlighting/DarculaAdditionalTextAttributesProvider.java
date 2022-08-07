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
public class DarculaAdditionalTextAttributesProvider implements AdditionalTextAttributesProvider {
  @Nonnull
  @Override
  public String getColorSchemeName() {
    return EditorColorsScheme.DARCULA_SCHEME_NAME;
  }

  @Nonnull
  @Override
  public String getColorSchemeFile() {
    return "/colorscheme/AsmIntelDarcula.xml";
  }
}

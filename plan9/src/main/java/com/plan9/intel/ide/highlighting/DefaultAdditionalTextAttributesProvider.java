package com.plan9.intel.ide.highlighting;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.AttributesFlyweightBuilder;
import consulo.colorScheme.EditorColorSchemeExtender;
import consulo.colorScheme.EditorColorsScheme;
import consulo.colorScheme.EffectType;
import consulo.ui.color.RGBColor;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 03-Jul-22
 */
@ExtensionImpl
public class DefaultAdditionalTextAttributesProvider implements EditorColorSchemeExtender {
    @Override
    public void extend(Builder builder) {
        builder.add(AsmIntelSyntaxHighlightingColors.INSTRUCTION, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(0x20, 0x99, 0x9D))
            .build());

        builder.add(AsmIntelSyntaxHighlightingColors.LABEL, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(0xDD, 0x67, 0x18))
            .withAdditionalEffect(EffectType.WAVE_UNDERSCORE, null)
            .build());
    }

    @Nonnull
    @Override
    public String getColorSchemeId() {
        return EditorColorsScheme.DEFAULT_SCHEME_NAME;
    }
}

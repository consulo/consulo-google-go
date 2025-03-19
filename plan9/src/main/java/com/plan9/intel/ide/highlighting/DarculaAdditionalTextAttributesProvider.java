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
public class DarculaAdditionalTextAttributesProvider implements EditorColorSchemeExtender {
    @Nonnull
    @Override
    public String getColorSchemeId() {
        return EditorColorsScheme.DARCULA_SCHEME_NAME;
    }

    @Override
    public void extend(Builder builder) {
        builder.add(AsmIntelSyntaxHighlightingColors.IDENTIFIER, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(0x20, 0x99, 0x9D))
            .build());

        builder.add(AsmIntelSyntaxHighlightingColors.PSEUDO_INSTRUCTION, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(0x8A, 0x79, 0x9B))
            .withBoldFont()
            .build());

        builder.add(AsmIntelSyntaxHighlightingColors.INSTRUCTION, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(0xCD, 0xA3, 0xE6))
            .build());

        builder.add(AsmIntelSyntaxHighlightingColors.REGISTER, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(0x9E, 0x61, 0x28))
            .build());

        builder.add(AsmIntelSyntaxHighlightingColors.LABEL, AttributesFlyweightBuilder.create()
            .withForeground(new RGBColor(0x4E, 0xAD, 0xE5))
            .withEffect(EffectType.WAVE_UNDERSCORE, null)
            .build());
    }
}

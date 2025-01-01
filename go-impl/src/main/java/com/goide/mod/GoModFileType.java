package com.goide.mod;

import consulo.google.go.icon.GoogleGoIconGroup;
import consulo.language.file.LanguageFileType;
import consulo.language.plain.PlainTextLanguage;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2025-01-01
 */
public class GoModFileType extends LanguageFileType {
    public static final GoModFileType INSTANCE = new GoModFileType();

    public GoModFileType() {
        super(PlainTextLanguage.INSTANCE);
    }

    @Nonnull
    @Override
    public String getId() {
        return "GO_MOD";
    }

    @Nonnull
    @Override
    public LocalizeValue getDescription() {
        return LocalizeValue.localizeTODO("GO module");
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return GoogleGoIconGroup.gomod();
    }
}

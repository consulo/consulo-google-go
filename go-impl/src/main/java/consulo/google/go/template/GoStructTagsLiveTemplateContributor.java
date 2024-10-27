package consulo.google.go.template;

import com.goide.template.GoTagLiveTemplateContextType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.template.LiveTemplateContributor;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import java.lang.Override;
import java.lang.String;

@ExtensionImpl
public class GoStructTagsLiveTemplateContributor implements LiveTemplateContributor {
  @Override
  @Nonnull
  public String groupId() {
    return "gostructtags";
  }

  @Override
  @Nonnull
  public LocalizeValue groupName() {
    return LocalizeValue.localizeTODO("Go Struct Tags");
  }

  @Override
  public void contribute(@Nonnull LiveTemplateContributor.Factory factory) {
    try(Builder builder = factory.newBuilder("gostructtagsXml", "xml", "`xml:\"$FIELD_NAME$\"$END$`", LocalizeValue.localizeTODO("`xml:\"\"`"))) {
      builder.withReformat();

      builder.withVariable("FIELD_NAME", "snakeCase(fieldName())", "", true);

      builder.withContext(GoTagLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gostructtagsJson", "json", "`json:\"$FIELD_NAME$\"$END$`", LocalizeValue.localizeTODO("`json:\"\"`"))) {
      builder.withReformat();

      builder.withVariable("FIELD_NAME", "snakeCase(fieldName())", "", true);

      builder.withContext(GoTagLiveTemplateContextType.class, true);
    }
  }
}

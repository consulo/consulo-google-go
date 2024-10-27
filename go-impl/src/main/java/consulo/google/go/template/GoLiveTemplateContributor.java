package consulo.google.go.template;

import com.goide.template.GoFileLiveTemplateContextType;
import com.goide.template.GoStatementLiveTemplateContextType;
import com.goide.template.GoTagLiteralLiveTemplateContextType;
import com.goide.template.GoTypeLiveTemplateContextType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.template.LiveTemplateContributor;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import java.lang.Override;
import java.lang.String;

@ExtensionImpl
public class GoLiveTemplateContributor implements LiveTemplateContributor {
  @Override
  @Nonnull
  public String groupId() {
    return "go";
  }

  @Override
  @Nonnull
  public LocalizeValue groupName() {
    return LocalizeValue.localizeTODO("Go");
  }

  @Override
  public void contribute(@Nonnull LiveTemplateContributor.Factory factory) {
    try(Builder builder = factory.newBuilder("goImp", "imp", "import (\n"
        + " \"$END$\"\n"
        + ")\n", LocalizeValue.localizeTODO("Import declaration"))) {
      builder.withReformat();

      builder.withContext(GoFileLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goP", "p", "package $NAME$", LocalizeValue.localizeTODO("Package declaration"))) {
      builder.withReformat();

      builder.withVariable("NAME", "complete()", "", true);

      builder.withContext(GoFileLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goCon", "con", "const $NAME$ $TYPE$ = $VALUE$", LocalizeValue.localizeTODO("Constant declaration"))) {
      builder.withReformat();

      builder.withVariable("NAME", "", "\"name\"", true);
      builder.withVariable("TYPE", "complete()", "", true);
      builder.withVariable("VALUE", "complete()", "", true);

      builder.withContext(GoFileLiveTemplateContextType.class, true);
      builder.withContext(GoStatementLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goIota", "iota", "const $NAME$ $TYPE$ = iota", LocalizeValue.localizeTODO("Iota constant declaration"))) {
      builder.withReformat();

      builder.withVariable("NAME", "", "\"name\"", true);
      builder.withVariable("TYPE", "complete()", "", true);

      builder.withContext(GoFileLiveTemplateContextType.class, true);
      builder.withContext(GoStatementLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goFor", "for", "for $VAR0$; $VAR1$; $VAR2$ {\n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("For loop"))) {
      builder.withReformat();

      builder.withVariable("VAR0", "", "\"\"", true);
      builder.withVariable("VAR1", "", "\"\"", true);
      builder.withVariable("VAR2", "", "\"\"", true);

      builder.withContext(GoStatementLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goForr", "forr", "for $KEY$, $VALUE$ := range $COLLECTION$ {\n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("For range loop"))) {
      builder.withReformat();

      builder.withVariable("COLLECTION", "", "\"collection\"", true);
      builder.withVariable("VALUE", "", "\"value\"", true);
      builder.withVariable("KEY", "", "\"key\"", true);

      builder.withContext(GoStatementLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goType", "type", "type $NAME$ $TYPE$", LocalizeValue.localizeTODO("Interface or struct"))) {
      builder.withReformat();

      builder.withVariable("NAME", "", "\"name\"", true);
      builder.withVariable("TYPE", "complete", "", true);

      builder.withContext(GoFileLiveTemplateContextType.class, true);
      builder.withContext(GoStatementLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goPrintf", "printf", "fmt.Printf(\"$END$\",$VAR$)", LocalizeValue.localizeTODO("printf"))) {
      builder.withReformat();

      builder.withVariable("VAR", "complete()", "", true);

      builder.withContext(GoStatementLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goErr", "err", "if err != nil {\n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("If error"))) {
      builder.withReformat();


      builder.withContext(GoStatementLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goXml", "xml", "xml:\"$FIELD_NAME$\"", LocalizeValue.localizeTODO("xml:\"\""))) {
      builder.withReformat();

      builder.withVariable("FIELD_NAME", "snakeCase(fieldName())", "", true);

      builder.withContext(GoTagLiteralLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goJson", "json", "json:\"$FIELD_NAME$\"", LocalizeValue.localizeTODO("json:\"\""))) {
      builder.withReformat();

      builder.withVariable("FIELD_NAME", "snakeCase(fieldName())", "", true);

      builder.withContext(GoTagLiteralLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goMain", "main", "func main() {\n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("Main function"))) {
      builder.withReformat();


      builder.withContext(GoFileLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goInit", "init", "func init() {\n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("Init function"))) {
      builder.withReformat();


      builder.withContext(GoFileLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goMeth", "meth", "func ($RECEIVER$ $TYPE_1$) $NAME$($PARAMS$) $TYPE_2$ {   \n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("Method"))) {
      builder.withReformat();

      builder.withVariable("RECEIVER", "", "\"receiver\"", true);
      builder.withVariable("TYPE_1", "complete()", "", true);
      builder.withVariable("NAME", "", "\"name\"", true);
      builder.withVariable("PARAMS", "", "\"params\"", true);
      builder.withVariable("TYPE_2", "complete()", "", true);

      builder.withContext(GoFileLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goTest", "test", "func Test$NAME$(t *testing.T) {\n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("Test"))) {
      builder.withReformat();

      builder.withVariable("NAME", "", "\"Name\"", true);

      builder.withContext(GoFileLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goBench", "bench", "func Benchmark$NAME$(b *testing.B) {\n"
        + " for i := 0; i < b.N; i++ {\n"
        + " $END$\n"
        + " }\n"
        + "}", LocalizeValue.localizeTODO("Benchmark"))) {
      builder.withReformat();

      builder.withVariable("NAME", "", "\"Name\"", true);

      builder.withContext(GoFileLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("goMap", "map", "map[$KEY_TYPE$]$VALUE_TYPE$", LocalizeValue.localizeTODO("Map type"))) {
      builder.withReformat();

      builder.withVariable("KEY_TYPE", "complete()", "", true);
      builder.withVariable("VALUE_TYPE", "complete()", "", true);

      builder.withContext(GoTypeLiveTemplateContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("go:", ":", "$NAME$ := $VALUE$", LocalizeValue.localizeTODO("Variable declaration :="))) {
      builder.withReformat();

      builder.withVariable("NAME", "", "\"name\"", true);
      builder.withVariable("VALUE", "complete()", "", true);

      builder.withContext(GoStatementLiveTemplateContextType.class, true);
    }
  }
}

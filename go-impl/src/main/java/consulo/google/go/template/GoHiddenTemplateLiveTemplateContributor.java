package consulo.google.go.template;

import com.goide.template.GoEverywhereContextType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.template.LiveTemplateContributor;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import java.lang.Override;
import java.lang.String;

/**
 * TODO replace it buy local template builder, due user can't changed them anyway
 */
@ExtensionImpl
public class GoHiddenTemplateLiveTemplateContributor implements LiveTemplateContributor {
  @Override
  @Nonnull
  public String groupId() {
    return "gohiddentemplate";
  }

  @Override
  @Nonnull
  public LocalizeValue groupName() {
    return LocalizeValue.localizeTODO("Go Hidden Templates");
  }

  @Override
  public void contribute(@Nonnull LiveTemplateContributor.Factory factory) {
    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_package", "go_lang_package", "package $NAME$", LocalizeValue.localizeTODO("Package declaration"))) {
      builder.withReformat();

      builder.withVariable("NAME", "complete()", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_import", "go_lang_import", "import \"$END$\"", LocalizeValue.localizeTODO("Import declaration"))) {
      builder.withReformat();


      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_break", "go_lang_break", "break", LocalizeValue.localizeTODO("Defer declaration"))) {
      builder.withReformat();


      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_continue", "go_lang_continue", "continue", LocalizeValue.localizeTODO("Defer declaration"))) {
      builder.withReformat();


      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_const", "go_lang_const", "const $NAME$ $TYPE$ = $VALUE$", LocalizeValue.localizeTODO("Constant declaration"))) {
      builder.withReformat();

      builder.withVariable("NAME", "", "\"name\"", true);
      builder.withVariable("TYPE", "complete()", "", true);
      builder.withVariable("VALUE", "complete()", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_for", "go_lang_for", "for $CONDITION$ {\n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("For loop"))) {
      builder.withReformat();

      builder.withVariable("CONDITION", "complete()", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_if", "go_lang_if", "if $CONDITION$ {\n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("If statement"))) {
      builder.withReformat();

      builder.withVariable("CONDITION", "complete()", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_switch", "go_lang_switch", "switch $EXPRESSION$ {\n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("Switch statement"))) {
      builder.withReformat();

      builder.withVariable("EXPRESSION", "", "\"expr\"", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_case", "go_lang_case", "case $CONDITION$:\n"
        + " $END$", LocalizeValue.localizeTODO("Case clause"))) {
      builder.withReformat();

      builder.withVariable("CONDITION", "complete()", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_default", "go_lang_default", "default:\n"
        + " $END$", LocalizeValue.localizeTODO("Default clause"))) {
      builder.withReformat();


      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_func", "go_lang_func", "func $NAME$($PARAMETERS$) $RETURN$ {\n"
        + " $END$\n"
        + "}", LocalizeValue.localizeTODO("Function or method"))) {
      builder.withReformat();

      builder.withVariable("NAME", "", "", true);
      builder.withVariable("PARAMETERS", "", "", true);
      builder.withVariable("RETURN", "", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_anonymous_func", "go_lang_anonymous_func", "func() {$END$}", LocalizeValue.localizeTODO("Anonymous function"))) {
      builder.withReformat();


      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_anonymous_struct", "go_lang_anonymous_struct", "struct {\n"
        + " $END$\n"
        + "}{}", LocalizeValue.localizeTODO("Anonymous struct"))) {
      builder.withReformat();


      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_var", "go_lang_var", "var $NAME$ $TYPE$ = $VALUE$", LocalizeValue.localizeTODO("Variable declaration"))) {
      builder.withReformat();

      builder.withVariable("NAME", "", "\"name\"", true);
      builder.withVariable("TYPE", "complete()", "", true);
      builder.withVariable("VALUE", "complete()", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_type", "go_lang_type", "type $NAME$ $TYPE$", LocalizeValue.localizeTODO("Interface or struct"))) {
      builder.withReformat();

      builder.withVariable("NAME", "", "\"name\"", true);
      builder.withVariable("TYPE", "complete", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateType_qf", "type_qf", "type $NAME$$END$ $TYPE$\n"
        + "\n", LocalizeValue.localizeTODO("Type quick fix"))) {
      builder.withReformat();

      builder.withVariable("TYPE", "complete", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGo_lang_var_qf", "go_lang_var_qf", "$NAME$$END$ := $VALUE$\n", LocalizeValue.localizeTODO("Local variable quick fix"))) {
      builder.withReformat();

      builder.withVariable("VALUE", "complete()", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGlobal_var_qf", "global_var_qf", "var $NAME$$END$ = $VALUE$\n"
        + "\n", LocalizeValue.localizeTODO("Global variable quick fix"))) {
      builder.withReformat();

      builder.withVariable("VALUE", "complete()", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateGlobal_const_qf", "global_const_qf", "const $NAME$$END$ = $VALUE$\n", LocalizeValue.localizeTODO("Global constant quick fix"))) {
      builder.withReformat();

      builder.withVariable("VALUE", "complete()", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("gohiddentemplateAdd_return", "add_return", "return $VALUE$$END$\n", LocalizeValue.localizeTODO("Add return"))) {
      builder.withReformat();

      builder.withVariable("VALUE", "", "", true);

      builder.withContext(GoEverywhereContextType.class, true);
    }

  }
}

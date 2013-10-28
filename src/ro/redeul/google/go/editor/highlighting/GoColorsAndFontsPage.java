package ro.redeul.google.go.editor.highlighting;

import static ro.redeul.google.go.highlight.GoSyntaxHighlighter.*;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import ro.redeul.google.go.GoBundle;
import ro.redeul.google.go.GoFileType;
import ro.redeul.google.go.GoIcons;
import ro.redeul.google.go.highlight.GoSyntaxHighlighter;

/**
 * @author Mihai Claudiu Toader <mtoader@gmail.com>
 *         Date: Sep 6, 2010
 */
public class GoColorsAndFontsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] ATTRIBUTES_DESCRIPTORS =
        new AttributesDescriptor[]{
            new AttributesDescriptor(GoBundle.message("color.go.line.comment"), LINE_COMMENT),
            new AttributesDescriptor(GoBundle.message("color.go.block.comment"), BLOCK_COMMENT),
            new AttributesDescriptor(GoBundle.message("color.go.keyword"), KEYWORD),
            new AttributesDescriptor(GoBundle.message("color.go.brackets"), BRACKET),
            new AttributesDescriptor(GoBundle.message("color.go.operators"), OPERATOR),
            new AttributesDescriptor(GoBundle.message("color.go.number"), NUMBER),
            new AttributesDescriptor(GoBundle.message("color.go.string"), STRING),
            new AttributesDescriptor(GoBundle.message("color.go.identifier"), IDENTIFIER),

            // psi
            new AttributesDescriptor(GoBundle.message("color.go.type.name"), TYPE_NAME),
            new AttributesDescriptor(GoBundle.message("color.go.const"), CONST),
            new AttributesDescriptor(GoBundle.message("color.go.variable"), VARIABLE),
            new AttributesDescriptor(GoBundle.message("color.go.global.variable"), GLOBAL_VARIABLE),
        };


    @NotNull
    public String getDisplayName() {
        return "Google Go";
    }

    public Icon getIcon() {
        return GoIcons.Go;
    }

    @NotNull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRIBUTES_DESCRIPTORS;
    }

    @NotNull
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    public SyntaxHighlighter getHighlighter() {
        final SyntaxHighlighter highlighter =
            SyntaxHighlighterFactory.getSyntaxHighlighter(
                GoFileType.INSTANCE, null, null);

        assert highlighter != null;
        return highlighter;
    }

    @NotNull
    public String getDemoText() {
        return
            "/**\n" +
                " * Comment\n" +
                " */\n" +
                "package main\n" +
                "import (\n" +
                "   fmt \"fmt\"\n" +
                "   <unused.import>\"unusedImport\"</unused.import>\n" +
                ")\n" +
                "\n" +
                "type <typeName>T</typeName> <typeName>int</typeName>\n" +
                "type (\n" +
                "   <typeName>T1</typeName> []<typeName>T</typeName>\n" +
                ")\n" +
                "const <const>CONST_VALUE</const> = 10\n\n" +
                "var <globalVariable>globalValue</globalVariable> = 5\n" +
                "\n" +
                "// line comment \n" +
                "func (<variable>t</variable>* <typeName>T1</typeName>) <method.declaration>function1</method.declaration>(<unused.parameter>a</unused.parameter> <typeName>int</typeName>, <variable>c</variable> <typeName>T</typeName>) (<typeName>string</typeName>) {\n" +
                "   x := 'a'\n" +
                "   <unused.variable>y</unused.variable> := 1\n" +
                "   var <variable>x</variable> <typeName>T1</typeName> = 10.10 + <globalVariable>globalValue</globalVariable> + <const>CONST_VALUE</const>\n" +
                "   fmt.Printf(<variable>x</variable>);\n" +
                "   return <variable>x</variable>\n" +
                "}\n" +
                "<error>function</error>\n";
    }

    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        final Map<String, TextAttributesKey> map = new HashMap<String, TextAttributesKey>();

        map.put("unused.parameter", CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES);
        map.put("unused.import", CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES);
        map.put("unused.variable", CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES);
        map.put("method.declaration", GoSyntaxHighlighter.METHOD_DECLARATION);
        map.put("variable", VARIABLE);
        map.put("globalVariable", GLOBAL_VARIABLE);
        map.put("typeName", TYPE_NAME);
        map.put("const", CONST);
        map.put("error", CodeInsightColors.ERRORS_ATTRIBUTES);

        return map;

    }
}

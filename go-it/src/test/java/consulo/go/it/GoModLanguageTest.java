/*
 * Copyright 2013-2026 consulo.io
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

package consulo.go.it;

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.document.Document;
import consulo.go.gomod.GoModFileType;
import consulo.go.gomod.GoWorkFileType;
import consulo.go.gomod.lexer.GoModLexer;
import consulo.go.gomod.psi.GoModFile;
import consulo.go.gomod.psi.GoModFilePath;
import consulo.go.gomod.psi.GoModGoDirective;
import consulo.go.gomod.psi.GoModGodebugSpec;
import consulo.go.gomod.psi.GoModModuleDirective;
import consulo.go.gomod.psi.GoModModulePath;
import consulo.go.gomod.psi.GoModReplaceSpec;
import consulo.go.gomod.psi.GoModRequireSpec;
import consulo.go.gomod.psi.GoModRetractSpec;
import consulo.go.gomod.psi.GoModToolSpec;
import consulo.go.gomod.psi.GoModTypes;
import consulo.go.gomod.psi.GoModUseSpec;
import consulo.go.gomod.psi.GoModVersion;
import consulo.it.CodeInsightTestFixture;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.file.FileTypeManager;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The go.mod / go.work language: lexer, parser, error recovery and the checks of the annotator.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class GoModLanguageTest {
    private static final String GO_MOD = """
        // the module
        module example.com/go

        go 1.22

        toolchain go1.22.4

        godebug (
        \tdefault=go1.21
        \tpanicnil=1
        )

        require (
        \tgolang.org/x/text v0.14.0
        \texample.com/tool v1.5.2 // indirect
        )

        require example.com/single v1.0.0

        exclude golang.org/x/net v1.2.3

        replace golang.org/x/net v1.2.3 => example.com/fork/net v1.4.5

        replace example.com/local => ../local

        retract (
        \t[v1.0.0, v1.0.5]
        \tv1.1.0 // published by mistake
        )

        tool golang.org/x/tools/cmd/stringer
        """;

    private static final String GO_WORK = """
        go 1.22

        use (
        \t./app
        \t"./lib"
        )

        replace example.com/lib v1.0.0 => ./lib
        """;

    @Test
    public void keywordsAreOnlyKeywordsWhereADirectiveStarts() {
        List<IElementType> tokens = tokens("module example.com/go\ngo 1.22\nrequire (\n\ttool v1.0.0\n)\n");

        assertThat(tokens).containsExactly(
            GoModTypes.MODULE, GoModTypes.IDENTIFIER, GoModTypes.EOL,
            GoModTypes.GO, GoModTypes.IDENTIFIER, GoModTypes.EOL,
            GoModTypes.REQUIRE, GoModTypes.LPAREN, GoModTypes.EOL,
            GoModTypes.IDENTIFIER, GoModTypes.IDENTIFIER, GoModTypes.EOL,
            GoModTypes.RPAREN, GoModTypes.EOL);
    }

    @Test
    public void punctuationCommentsAndStrings() {
        List<IElementType> tokens = tokens("replace a => \"b c\" // why\nretract [v1, v2]\ngodebug k=v\n");

        assertThat(tokens).containsExactly(
            GoModTypes.REPLACE, GoModTypes.IDENTIFIER, GoModTypes.ARROW, GoModTypes.STRING, GoModTypes.LINE_COMMENT, GoModTypes.EOL,
            GoModTypes.RETRACT, GoModTypes.LBRACK, GoModTypes.IDENTIFIER, GoModTypes.COMMA, GoModTypes.IDENTIFIER, GoModTypes.RBRACK,
            GoModTypes.EOL,
            GoModTypes.GODEBUG, GoModTypes.IDENTIFIER, GoModTypes.EQ, GoModTypes.IDENTIFIER, GoModTypes.EOL);
    }

    @Test
    public void goModAndGoWorkHaveTheirFileTypes() {
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        assertThat(fileTypeManager.getFileTypeByFileName("go.mod")).isSameAs(GoModFileType.INSTANCE);
        assertThat(fileTypeManager.getFileTypeByFileName("go.work")).isSameAs(GoWorkFileType.INSTANCE);
    }

    @Test
    public void everyDirectiveOfGoModParses(Project project) {
        ReadAction.run(() -> {
            PsiFile file = parse(project, "go.mod", GoModFileType.INSTANCE, GO_MOD);
            assertThat(file).isInstanceOf(GoModFile.class);
            assertThat(((GoModFile) file).isWorkFile()).isFalse();
            assertThat(PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class)).extracting(PsiErrorElement::getErrorDescription).isEmpty();

            GoModModuleDirective module = PsiTreeUtil.findChildOfType(file, GoModModuleDirective.class);
            assertThat(texts(PsiTreeUtil.findChildrenOfType(module, GoModModulePath.class))).containsExactly("example.com/go");

            assertThat(PsiTreeUtil.findChildrenOfType(file, GoModRequireSpec.class)).extracting(spec -> spec.getText())
                .containsExactly("golang.org/x/text v0.14.0", "example.com/tool v1.5.2", "example.com/single v1.0.0");

            List<GoModReplaceSpec> replaces = new ArrayList<>(PsiTreeUtil.findChildrenOfType(file, GoModReplaceSpec.class));
            assertThat(replaces).hasSize(2);
            assertThat(texts(PsiTreeUtil.findChildrenOfType(replaces.get(0), GoModModulePath.class)))
                .as("replaced by a module").containsExactly("golang.org/x/net", "example.com/fork/net");
            assertThat(texts(PsiTreeUtil.findChildrenOfType(replaces.get(0), GoModVersion.class))).containsExactly("v1.2.3", "v1.4.5");
            assertThat(texts(PsiTreeUtil.findChildrenOfType(replaces.get(1), GoModFilePath.class)))
                .as("replaced by a directory").containsExactly("../local");

            assertThat(PsiTreeUtil.findChildrenOfType(file, GoModRetractSpec.class)).extracting(spec -> spec.getText())
                .containsExactly("[v1.0.0, v1.0.5]", "v1.1.0");
            assertThat(PsiTreeUtil.findChildrenOfType(file, GoModGodebugSpec.class)).extracting(spec -> spec.getText())
                .containsExactly("default=go1.21", "panicnil=1");
            assertThat(PsiTreeUtil.findChildrenOfType(file, GoModToolSpec.class)).extracting(spec -> spec.getText())
                .containsExactly("golang.org/x/tools/cmd/stringer");
        });
    }

    @Test
    public void goWorkParses(Project project) {
        ReadAction.run(() -> {
            PsiFile file = parse(project, "go.work", GoWorkFileType.INSTANCE, GO_WORK);
            assertThat(((GoModFile) file).isWorkFile()).isTrue();
            assertThat(PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class)).isEmpty();
            assertThat(PsiTreeUtil.findChildrenOfType(file, GoModUseSpec.class)).extracting(spec -> spec.getText())
                .containsExactly("./app", "\"./lib\"");
        });
    }

    @Test
    public void aBrokenLineDoesNotSwallowTheNextDirectives(Project project) {
        ReadAction.run(() -> {
            PsiFile file = parse(project, "go.mod", GoModFileType.INSTANCE,
                "module example.com/m\nrequire example.com/x\nrequire (\n\texample.com/y\n\texample.com/z v1.0.0\n)\ngo 1.22\n");

            assertThat(PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class)).as("two requirements miss their version").hasSize(2);
            assertThat(PsiTreeUtil.findChildrenOfType(file, GoModRequireSpec.class)).extracting(spec -> spec.getText())
                .contains("example.com/z v1.0.0");
            assertThat(PsiTreeUtil.findChildOfType(file, GoModGoDirective.class)).as("the go directive after the broken lines").isNotNull();
        });
    }

    @Test
    public void theAnnotatorReportsWhatGoRejects(HeadlessProjects projects) throws Exception {
        CodeInsightTestFixture fixture = CodeInsightTestFixture.create(Application.get(), projects);

        fixture.configureByText("valid/go.mod", GO_MOD);
        assertThat(errors(fixture)).as("a valid go.mod").isEmpty();

        fixture.configureByText("valid/go.work", GO_WORK);
        assertThat(errors(fixture)).as("a valid go.work").isEmpty();

        fixture.configureByText("work/go.work", "go 1.22\nmodule example.com/m\nrequire example.com/x v1.0.0\n");
        assertThat(errors(fixture)).as("go.mod directives in a go.work").containsExactlyInAnyOrder("module", "require");

        fixture.configureByText("mod/go.mod", "module example.com/m\nuse ./x\nfrobnicate a b\ngo 1.21\ngo 1.22\n");
        assertThat(errors(fixture)).as("use in go.mod, an unknown and a repeated directive").containsExactlyInAnyOrder("use", "frobnicate", "go");

        fixture.configureByText("colors/go.mod", "module example.com/m\nrequire example.com/x v1.0.0\n");
        Document document = fixture.getDocument();
        List<String> colored = new ArrayList<>();
        for (HighlightInfo info : fixture.doHighlighting()) {
            if (info.getSeverity() == HighlightSeverity.INFORMATION) {
                colored.add(document.getText().substring(info.getActualStartOffset(), info.getActualEndOffset()));
            }
        }
        assertThat(colored).as("module paths and versions get their colors").contains("example.com/m", "example.com/x", "v1.0.0");
    }

    private static List<String> errors(CodeInsightTestFixture fixture) throws Exception {
        Document document = fixture.getDocument();
        List<String> errors = new ArrayList<>();
        for (HighlightInfo info : fixture.doHighlighting()) {
            if (info.getSeverity() == HighlightSeverity.ERROR) {
                errors.add(document.getText().substring(info.getActualStartOffset(), info.getActualEndOffset()));
            }
        }
        return errors;
    }

    private static List<IElementType> tokens(String text) {
        GoModLexer lexer = new GoModLexer();
        lexer.start(text);
        List<IElementType> tokens = new ArrayList<>();
        while (lexer.getTokenType() != null) {
            if (lexer.getTokenType() != TokenType.WHITE_SPACE) {
                tokens.add(lexer.getTokenType());
            }
            lexer.advance();
        }
        return tokens;
    }

    private static PsiFile parse(Project project, String name, LanguageFileType fileType, String text) {
        return PsiFileFactory.getInstance(project).createFileFromText(name, fileType, text);
    }

    private static List<String> texts(Collection<? extends PsiElement> elements) {
        List<String> texts = new ArrayList<>();
        for (PsiElement element : elements) {
            texts.add(element.getText());
        }
        return texts;
    }
}

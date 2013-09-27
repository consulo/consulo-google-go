package ro.redeul.google.go;

import javax.swing.Icon;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import ro.redeul.google.go.highlight.GoEditorHighlighter;

public class GoFileType extends LanguageFileType {

    public static final GoFileType INSTANCE = new GoFileType();

    @NonNls
    public static final String DEFAULT_EXTENSION = "go";

    private GoFileType() {
        super(GoLanguage.INSTANCE);
    }

    @NotNull
    @NonNls
    public String getName() {
        return "Google Go";
    }

    @NonNls
    @NotNull
    public String getDescription() {
        return "Google Go files";
    }

    @NotNull
    @NonNls
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    public Icon getIcon() {
        return GoIcons.Go;
    }

    @Override
    public String getCharset(@NotNull VirtualFile file, byte[] content) {
        return CharsetToolkit.UTF8;
    }

    public EditorHighlighter getEditorHighlighter(@Nullable Project project,
                                                  @Nullable VirtualFile virtualFile,
                                                  @NotNull EditorColorsScheme colors) {
        return new GoEditorHighlighter(colors, project, virtualFile);
    }
}

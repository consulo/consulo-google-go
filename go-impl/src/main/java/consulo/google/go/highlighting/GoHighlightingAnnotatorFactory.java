package consulo.google.go.highlighting;

import com.goide.GoLanguage;
import com.goide.highlighting.GoHighlightingAnnotator;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 07-Aug-22
 */
@ExtensionImpl
public class GoHighlightingAnnotatorFactory implements AnnotatorFactory {
  @Nullable
  @Override
  public Annotator createAnnotator() {
    return new GoHighlightingAnnotator();
  }

  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}

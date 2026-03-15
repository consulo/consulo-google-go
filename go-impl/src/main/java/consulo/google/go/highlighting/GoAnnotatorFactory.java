package consulo.google.go.highlighting;

import com.goide.GoLanguage;
import com.goide.highlighting.GoAnnotator;
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
public class GoAnnotatorFactory implements AnnotatorFactory {
  @Nullable
  @Override
  public Annotator createAnnotator() {
    return new GoAnnotator();
  }

  @Override
  public Language getLanguage() {
    return GoLanguage.INSTANCE;
  }
}

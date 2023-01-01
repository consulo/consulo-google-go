package consulo.google.go.inspection;

import com.goide.inspections.GoInspectionBase;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 09-Aug-22
 */
public abstract class GoGeneralInspectionBase extends GoInspectionBase {
  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "General";
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}

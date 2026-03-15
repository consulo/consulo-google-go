package consulo.google.go.inspection;

import com.goide.inspections.GoInspectionBase;
import consulo.language.editor.inspection.localize.InspectionLocalize;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;

import consulo.localize.LocalizeValue;

/**
 * @author VISTALL
 * @since 09-Aug-22
 */
public abstract class GoGeneralInspectionBase extends GoInspectionBase {
  @Override
  public LocalizeValue getGroupDisplayName() {
    return InspectionLocalize.inspectionGeneralToolsGroupName();
  }

  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}

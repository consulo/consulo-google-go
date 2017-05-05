package consulo.googe.go.module.orderEntry;

import com.goide.GoIcons;
import com.intellij.openapi.roots.ui.CellAppearanceEx;
import com.intellij.openapi.roots.ui.util.SimpleTextCellAppearance;
import com.intellij.ui.SimpleTextAttributes;
import consulo.roots.orderEntry.OrderEntryTypeEditor;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoPathOrderEntryTypeEditor implements OrderEntryTypeEditor<GoPathOrderEntry> {
  @NotNull
  public CellAppearanceEx getCellAppearance(@NotNull GoPathOrderEntry orderEntry) {
    return new SimpleTextCellAppearance(orderEntry.getPresentableName(), GoIcons.ICON, SimpleTextAttributes.SYNTHETIC_ATTRIBUTES);
  }
}

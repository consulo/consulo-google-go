package consulo.googe.go.module.orderEntry;

import com.intellij.openapi.util.InvalidDataException;
import consulo.roots.ModuleRootLayer;
import consulo.roots.impl.ModuleRootLayerImpl;
import consulo.roots.orderEntry.OrderEntryType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoPathOrderEntryType implements OrderEntryType<GoPathOrderEntry> {
  public static OrderEntryType getInstance() {
    return EP_NAME.findExtension(GoPathOrderEntryType.class);
  }

  @Override
  public void storeOrderEntry(@NotNull Element element, @NotNull GoPathOrderEntry goPathOrderEntry) {
  }

  @NotNull
  @Override
  public GoPathOrderEntry loadOrderEntry(@NotNull Element element, @NotNull ModuleRootLayer moduleRootLayer) throws InvalidDataException {
    return new GoPathOrderEntry((ModuleRootLayerImpl)moduleRootLayer);
  }

  @NotNull
  @Override
  public String getId() {
    return "gopath";
  }
}

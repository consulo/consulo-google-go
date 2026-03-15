package consulo.google.go.module.orderEntry;

import com.goide.sdk.GoEnvironmentGoPathModificationTracker;
import consulo.content.OrderRootType;
import consulo.content.RootProvider;
import consulo.content.RootProviderBase;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryModel;
import consulo.virtualFileSystem.VirtualFile;

import org.jspecify.annotations.Nullable;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 26-Jul-22
 */
public class GoPathOrderEntryModel implements CustomOrderEntryModel {
  private final RootProvider myRootProvider = new RootProviderBase() {
    public String[] getUrls(OrderRootType orderRootType) {
      Collection<VirtualFile> goEnvironmentGoPathRoots = GoEnvironmentGoPathModificationTracker.getGoEnvironmentGoPathRoots();
      return goEnvironmentGoPathRoots.stream().map(VirtualFile::getUrl).toArray(String[]::new);
    }

    @Override
    public VirtualFile[] getFiles(OrderRootType orderRootType) {
      return GoEnvironmentGoPathModificationTracker.getGoEnvironmentGoPathRoots().toArray(VirtualFile.EMPTY_ARRAY);
    }
  };

  @Override
  public void bind(ModuleRootLayer moduleRootLayer) {
  }

  @Override
  public String getPresentableName() {
    return "GOPATH";
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public RootProvider getRootProvider() {
    return myRootProvider;
  }

  @Override
  public CustomOrderEntryModel clone() {
    return new GoPathOrderEntryModel();
  }

  @Nullable
  @Override
  public Object getEqualObject() {
    return getPresentableName();
  }

  @Override
  public boolean isEquivalentTo(CustomOrderEntryModel otherModel) {
    return otherModel instanceof GoPathOrderEntryModel;
  }
}

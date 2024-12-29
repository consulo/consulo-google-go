package consulo.google.go.module.orderEntry;

import com.goide.sdk.GoEnvironmentGoPathModificationTracker;
import consulo.content.OrderRootType;
import consulo.content.RootProvider;
import consulo.content.RootProviderBase;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryModel;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 26-Jul-22
 */
public class GoPathOrderEntryModel implements CustomOrderEntryModel {
  private final RootProvider myRootProvider = new RootProviderBase() {
    @Nonnull
    public String[] getUrls(OrderRootType orderRootType) {
      Collection<VirtualFile> goEnvironmentGoPathRoots = GoEnvironmentGoPathModificationTracker.getGoEnvironmentGoPathRoots();
      return goEnvironmentGoPathRoots.stream().map(VirtualFile::getUrl).toArray(String[]::new);
    }

    @Nonnull
    @Override
    public VirtualFile[] getFiles(OrderRootType orderRootType) {
      return GoEnvironmentGoPathModificationTracker.getGoEnvironmentGoPathRoots().toArray(VirtualFile.EMPTY_ARRAY);
    }
  };

  @Override
  public void bind(@Nonnull ModuleRootLayer moduleRootLayer) {
  }

  @Nonnull
  @Override
  public String getPresentableName() {
    return "GOPATH";
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Nonnull
  @Override
  public RootProvider getRootProvider() {
    return myRootProvider;
  }

  @Nonnull
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
  public boolean isEquivalentTo(@Nonnull CustomOrderEntryModel otherModel) {
    return otherModel instanceof GoPathOrderEntryModel;
  }
}

package consulo.googe.go.module.orderEntry;

import com.goide.sdk.GoEnvironmentGoPathModificationTracker;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.RootPolicy;
import com.intellij.openapi.roots.impl.ClonableOrderEntry;
import com.intellij.openapi.roots.impl.OrderEntryBaseImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import consulo.roots.OrderEntryWithTracking;
import consulo.roots.impl.ModuleRootLayerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoPathOrderEntry extends OrderEntryBaseImpl implements OrderEntryWithTracking, ClonableOrderEntry {

  public GoPathOrderEntry(@NotNull ModuleRootLayerImpl rootLayer) {
    super(GoPathOrderEntryType.getInstance(), rootLayer);
  }

  @NotNull
  public String[] getUrls(OrderRootType orderRootType) {
    Collection<VirtualFile> goEnvironmentGoPathRoots = GoEnvironmentGoPathModificationTracker.getGoEnvironmentGoPathRoots();
    return goEnvironmentGoPathRoots.stream().map(VirtualFile::getUrl).toArray(String[]::new);
  }

  @NotNull
  @Override
  public VirtualFile[] getFiles(OrderRootType orderRootType) {
    return ContainerUtil.toArray(GoEnvironmentGoPathModificationTracker.getGoEnvironmentGoPathRoots(), VirtualFile.EMPTY_ARRAY);
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "GOPATH";
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @NotNull
  @Override
  public Module getOwnerModule() {
    return getRootModel().getModule();
  }

  @Override
  public <R> R accept(RootPolicy<R> rootPolicy, @Nullable R r) {
    return rootPolicy.visitOrderEntry(this, r);
  }

  @Override
  public boolean isEquivalentTo(@NotNull OrderEntry orderEntry) {
    return orderEntry instanceof GoPathOrderEntry;
  }

  @Override
  public boolean isSynthetic() {
    return true;
  }

  @Nullable
  @Override
  public Object getEqualObject() {
    return "GOPATH";
  }

  @Override
  public OrderEntry cloneEntry(ModuleRootLayerImpl layer) {
    return new GoPathOrderEntry(layer);
  }
}

package consulo.googe.go.dlv.breakpoint;

import com.goide.GoFileType;
import com.goide.dlv.DlvDebugProcess;
import com.goide.dlv.breakpoint.DlvBreakpointType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import consulo.annotations.RequiredReadAction;
import consulo.xdebugger.breakpoints.XLineBreakpointTypeResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public class DlvBreakpointTypeResolver implements XLineBreakpointTypeResolver {
  @RequiredReadAction
  @Nullable
  @Override
  public XLineBreakpointType<?> resolveBreakpointType(@NotNull Project project, @NotNull VirtualFile file, int line) {
    if (line < 0 || DlvDebugProcess.IS_DLV_DISABLED || file.getFileType() != GoFileType.INSTANCE) {
      return null;
    }
    if(DlvBreakpointType.isLineBreakpointAvailable(file, line, project)) {
      return DlvBreakpointType.getInstance();
    }
    return null;
  }
}

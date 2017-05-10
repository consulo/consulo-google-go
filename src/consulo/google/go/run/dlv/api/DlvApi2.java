package consulo.google.go.run.dlv.api;

import com.goide.dlv.protocol.DlvApi;

/**
 * @author VISTALL
 * @since 10-May-17
 */
public class DlvApi2 extends DlvApi {
  public static class CreateBreakpointIn {
    public Breakpoint Breakpoint;
  }

  public static class CreateBreakpointOut {
    public Breakpoint Breakpoint;
  }
}

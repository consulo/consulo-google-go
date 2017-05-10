package consulo.google.go.run.dlv.api;

import com.goide.dlv.protocol.DlvApi;

import static consulo.google.go.run.dlv.api.DlvRequest.create;

/**
 * @author VISTALL
 * @since 10-May-17
 */
public interface DlvRequests {
  DlvRequest<DlvApi2.CreateBreakpointIn, DlvApi2.CreateBreakpointOut> CreateBreakpoint =
          create("CreateBreakpoint", DlvApi2.CreateBreakpointIn.class, DlvApi2.CreateBreakpointOut.class,
                 (createBreakpointIn, objects) -> createBreakpointIn.Breakpoint = (DlvApi.Breakpoint)objects[0]);
}

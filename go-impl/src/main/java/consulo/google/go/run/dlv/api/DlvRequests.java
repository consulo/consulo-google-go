/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.google.go.run.dlv.api;

import com.goide.dlv.protocol.DlvApi;

import static consulo.google.go.run.dlv.api.DlvRequest.paramized;

/**
 * @author VISTALL
 * @since 10-May-17
 */
public interface DlvRequests {
  DlvRequest<DlvApi2.CreateBreakpointIn, DlvApi2.CreateBreakpointOut> CreateBreakpoint =
          paramized("CreateBreakpoint", DlvApi2.CreateBreakpointIn.class, DlvApi2.CreateBreakpointOut.class,
                    (it, args) -> it.Breakpoint = (DlvApi.Breakpoint)args[0]);

  DlvRequest<DlvApi2.StacktraceIn, DlvApi2.StacktraceOut> Stacktrace =
          paramized("Stacktrace", DlvApi2.StacktraceIn.class, DlvApi2.StacktraceOut.class, (it, args) -> {
            it.id = (Integer)args[0];
            it.depth = (Integer)args[1];
          });
}

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
import com.google.gson.annotations.SerializedName;

/**
 * @author VISTALL
 * @since 10-May-17
 */
public class DlvApi2 extends DlvApi {
  public static class StacktraceIn {
    @SerializedName("Id")
    public int id;
    @SerializedName("Depth")
    public int depth;
  }

  public static class CreateBreakpointIn {
    public Breakpoint Breakpoint;
  }

  public static class CreateBreakpointOut {
    public Breakpoint Breakpoint;
  }
}

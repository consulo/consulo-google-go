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

package consulo.google.go.run.dlv.breakpoint;

import com.goide.GoFileType;
import com.goide.dlv.DlvDebugProcess;
import com.goide.dlv.breakpoint.DlvBreakpointType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import consulo.annotation.access.RequiredReadAction;
import consulo.xdebugger.breakpoints.XLineBreakpointTypeResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public class DlvBreakpointTypeResolver implements XLineBreakpointTypeResolver {
  @RequiredReadAction
  @Nullable
  @Override
  public XLineBreakpointType<?> resolveBreakpointType(@Nonnull Project project, @Nonnull VirtualFile file, int line) {
    if (line < 0 || DlvDebugProcess.IS_DLV_DISABLED || file.getFileType() != GoFileType.INSTANCE) {
      return null;
    }
    if(DlvBreakpointType.isLineBreakpointAvailable(file, line, project)) {
      return DlvBreakpointType.getInstance();
    }
    return null;
  }
}

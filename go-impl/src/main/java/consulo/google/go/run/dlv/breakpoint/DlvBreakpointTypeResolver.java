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
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.execution.debug.breakpoint.XLineBreakpointTypeResolver;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 06-May-17
 */
@ExtensionImpl
public class DlvBreakpointTypeResolver implements XLineBreakpointTypeResolver {
  @RequiredReadAction
  @Nullable
  @Override
  public XLineBreakpointType<?> resolveBreakpointType(@Nonnull Project project, @Nonnull VirtualFile file, int line) {
    if (line < 0 || DlvDebugProcess.IS_DLV_DISABLED) {
      return null;
    }
    if (DlvBreakpointType.isLineBreakpointAvailable(file, line, project)) {
      return DlvBreakpointType.getInstance();
    }
    return null;
  }

  @Nonnull
  @Override
  public FileType getFileType() {
    return GoFileType.INSTANCE;
  }
}

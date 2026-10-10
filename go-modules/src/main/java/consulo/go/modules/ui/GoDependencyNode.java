/*
 * Copyright 2013-2026 consulo.io
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

package consulo.go.modules.ui;

import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.view.ExternalProjectsView;
import consulo.externalSystem.view.ExternalSystemNode;
import consulo.go.modules.externalSystem.GoDependencyData;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.tree.PresentationData;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
public class GoDependencyNode extends ExternalSystemNode<GoDependencyData> {
    public GoDependencyNode(ExternalProjectsView view, DataNode<GoDependencyData> dataNode) {
        super(view, null, dataNode);
    }

    @Override
    public String getName() {
        GoDependencyData data = getData();
        if (data == null) {
            return super.getName();
        }
        return data.getVersion() != null ? data.getModulePath() + " " + data.getVersion() : data.getModulePath();
    }

    @Override
    protected void update(PresentationData presentation) {
        GoDependencyData data = getData();
        String description = null;
        if (data != null) {
            description = data.getError() != null ? data.getError() : data.getDirectory();
        }
        setNameAndTooltip(presentation, getName(), description, data != null ? data.getReplacement() : null);
        presentation.setIcon(PlatformIconGroup.nodesPplib());
    }
}

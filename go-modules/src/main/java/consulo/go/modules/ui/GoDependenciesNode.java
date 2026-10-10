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

import consulo.externalSystem.util.Order;
import consulo.externalSystem.view.ExternalProjectsView;
import consulo.externalSystem.view.ExternalSystemNode;
import consulo.go.localize.GoLocalize;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.tree.PresentationData;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@Order(ExternalSystemNode.BUILTIN_DEPENDENCIES_DATA_NODE_ORDER)
public class GoDependenciesNode extends ExternalSystemNode<Object> {
    public GoDependenciesNode(ExternalProjectsView view) {
        super(view, null, null);
    }

    @Override
    public String getName() {
        return GoLocalize.goModulesViewDependenciesNode().get();
    }

    @Override
    protected void update(PresentationData presentation) {
        super.update(presentation);
        presentation.setIcon(PlatformIconGroup.nodesPplibfolder());
    }
}

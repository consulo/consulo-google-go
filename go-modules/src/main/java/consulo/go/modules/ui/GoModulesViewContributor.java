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

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.view.ExternalProjectsView;
import consulo.externalSystem.view.ExternalSystemNode;
import consulo.externalSystem.view.ExternalSystemViewContributor;
import consulo.go.modules.GoModulesConstants;
import consulo.go.modules.externalSystem.GoDependencyData;
import consulo.util.collection.MultiMap;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Shows the build list of every module under a "Dependencies" node of the tool window.
 *
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoModulesViewContributor extends ExternalSystemViewContributor {
    @Override
    public ProjectSystemId getSystemId() {
        return GoModulesConstants.SYSTEM_ID;
    }

    @Override
    public List<Key<?>> getKeys() {
        return List.of(GoDependencyData.KEY);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ExternalSystemNode<?>> createNodes(ExternalProjectsView view, MultiMap<Key<?>, DataNode<?>> dataNodes) {
        Collection<DataNode<?>> dependencyNodes = dataNodes.get(GoDependencyData.KEY);
        if (dependencyNodes.isEmpty()) {
            return List.of();
        }
        List<ExternalSystemNode<?>> children = new ArrayList<>();
        for (DataNode<?> dataNode : dependencyNodes) {
            if (dataNode.getData() instanceof GoDependencyData) {
                children.add(new GoDependencyNode(view, (DataNode<GoDependencyData>) dataNode));
            }
        }
        GoDependenciesNode dependenciesNode = new GoDependenciesNode(view);
        dependenciesNode.addAll(children);
        return List.of(dependenciesNode);
    }

    @Override
    public @Nullable String getDisplayName(DataNode<?> node) {
        return node.getData() instanceof GoDependencyData data ? data.getModulePath() : null;
    }
}

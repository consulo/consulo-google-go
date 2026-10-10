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

package consulo.go.gomod;

import consulo.annotation.component.ExtensionImpl;
import consulo.go.modules.GoModulesConstants;
import consulo.virtualFileSystem.fileType.FileNameMatcherFactory;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import jakarta.inject.Inject;

/**
 * Registers {@code go.mod} and {@code go.work} by their exact file names.
 */
@ExtensionImpl
public class GoModFileTypeFactory extends FileTypeFactory {
    private final FileNameMatcherFactory myMatcherFactory;

    @Inject
    public GoModFileTypeFactory(FileNameMatcherFactory matcherFactory) {
        myMatcherFactory = matcherFactory;
    }

    @Override
    public void createFileTypes(FileTypeConsumer consumer) {
        consumer.consume(GoModFileType.INSTANCE, myMatcherFactory.createExactFileNameMatcher(GoModulesConstants.GO_MOD));
        consumer.consume(GoWorkFileType.INSTANCE, myMatcherFactory.createExactFileNameMatcher(GoModulesConstants.GO_WORK));
    }
}

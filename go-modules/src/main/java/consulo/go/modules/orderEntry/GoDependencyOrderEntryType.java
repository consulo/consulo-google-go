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

package consulo.go.modules.orderEntry;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryTypeProvider;
import consulo.util.xml.serializer.InvalidDataException;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class GoDependencyOrderEntryType implements CustomOrderEntryTypeProvider<GoDependencyOrderEntryModel> {
    public static final String ID = "go-module-dependency";

    private static final String PATH_ATTRIBUTE = "path";
    private static final String VERSION_ATTRIBUTE = "version";
    private static final String DIRECTORY_ATTRIBUTE = "directory";
    private static final String REPLACEMENT_ATTRIBUTE = "replacement";
    private static final String INDIRECT_ATTRIBUTE = "indirect";

    public static GoDependencyOrderEntryType getInstance() {
        return EP.findExtensionOrFail(Application.get(), GoDependencyOrderEntryType.class);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public GoDependencyOrderEntryModel loadOrderEntry(Element element, ModuleRootLayer moduleRootLayer) throws InvalidDataException {
        String path = element.getAttributeValue(PATH_ATTRIBUTE);
        if (path == null) {
            throw new InvalidDataException("Go module dependency without a module path");
        }
        return new GoDependencyOrderEntryModel(path,
            element.getAttributeValue(VERSION_ATTRIBUTE),
            element.getAttributeValue(DIRECTORY_ATTRIBUTE),
            element.getAttributeValue(REPLACEMENT_ATTRIBUTE),
            Boolean.parseBoolean(element.getAttributeValue(INDIRECT_ATTRIBUTE)));
    }

    @Override
    public void storeOrderEntry(Element element, GoDependencyOrderEntryModel model) {
        element.setAttribute(PATH_ATTRIBUTE, model.getModulePath());
        setIfNotNull(element, VERSION_ATTRIBUTE, model.getVersion());
        setIfNotNull(element, DIRECTORY_ATTRIBUTE, model.getDirectoryUrl());
        setIfNotNull(element, REPLACEMENT_ATTRIBUTE, model.getReplacement());
        if (model.isIndirect()) {
            element.setAttribute(INDIRECT_ATTRIBUTE, Boolean.TRUE.toString());
        }
    }

    private static void setIfNotNull(Element element, String name, @Nullable String value) {
        if (value != null) {
            element.setAttribute(name, value);
        }
    }
}

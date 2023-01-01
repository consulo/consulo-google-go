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

package consulo.google.go.module.orderEntry;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.CustomOrderEntryTypeProvider;
import consulo.util.xml.serializer.InvalidDataException;
import org.jdom.Element;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05-May-17
 */
@ExtensionImpl
public class GoPathOrderEntryType implements CustomOrderEntryTypeProvider<GoPathOrderEntryModel> {
  public static GoPathOrderEntryType getInstance() {
    return EP.findExtensionOrFail(Application.get(), GoPathOrderEntryType.class);
  }

  @Nonnull
  @Override
  public String getId() {
    return "gopath";
  }

  @Nonnull
  @Override
  public GoPathOrderEntryModel loadOrderEntry(@Nonnull Element element, @Nonnull ModuleRootLayer moduleRootLayer) throws InvalidDataException {
    return new GoPathOrderEntryModel();
  }

  @Override
  public void storeOrderEntry(@Nonnull Element element, @Nonnull GoPathOrderEntryModel goPathOrderEntryModel) {
  }
}

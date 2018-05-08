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

import javax.annotation.Nonnull;

import com.intellij.openapi.util.InvalidDataException;
import consulo.roots.ModuleRootLayer;
import consulo.roots.impl.ModuleRootLayerImpl;
import consulo.roots.orderEntry.OrderEntryType;
import org.jdom.Element;

/**
 * @author VISTALL
 * @since 05-May-17
 */
public class GoPathOrderEntryType implements OrderEntryType<GoPathOrderEntry> {
  public static OrderEntryType getInstance() {
    return EP_NAME.findExtension(GoPathOrderEntryType.class);
  }

  @Override
  public void storeOrderEntry(@Nonnull Element element, @Nonnull GoPathOrderEntry goPathOrderEntry) {
  }

  @Nonnull
  @Override
  public GoPathOrderEntry loadOrderEntry(@Nonnull Element element, @Nonnull ModuleRootLayer moduleRootLayer) throws InvalidDataException {
    return new GoPathOrderEntry((ModuleRootLayerImpl)moduleRootLayer);
  }

  @Nonnull
  @Override
  public String getId() {
    return "gopath";
  }
}

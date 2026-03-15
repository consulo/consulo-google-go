/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan
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
package com.goide.tree;

import consulo.fileEditor.structureView.tree.ActionPresentation;
import consulo.fileEditor.structureView.tree.Sorter;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;

import java.util.Comparator;

public class ExportabilitySorter implements Sorter {
  public static final Sorter INSTANCE = new ExportabilitySorter();

  private static final ActionPresentation PRESENTATION = new ActionPresentation() {
    @Override
    public String getText() {
      return "Sort by Exportability";
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Image getIcon() {
      return PlatformIconGroup.objectbrowserVisibilitysort();
    }
  };
  private static final String ID = "EXPORTABILITY_SORTER";

  @Override
  public Comparator getComparator() {
    return ExportabilityComparator.INSTANCE;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  public ActionPresentation getPresentation() {
    return PRESENTATION;
  }

  @Override
  public String getName() {
    return ID;
  }
}
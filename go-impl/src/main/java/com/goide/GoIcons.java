/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import consulo.ui.image.Image;
import consulo.ui.image.ImageEffects;

@SuppressWarnings("ConstantConditions")
public interface GoIcons
{
	Image ICON = IconLoader.findIcon("/icons/go.png");
	Image TYPE = IconLoader.findIcon("/icons/type.png");
	Image APPLICATION_RUN = ImageEffects.layered(ICON, AllIcons.Nodes.RunnableMark);
	Image TEST_RUN = ImageEffects.layered(ICON, AllIcons.Nodes.JunitTestMark);
	Image METHOD = AllIcons.Nodes.Method;
	Image FUNCTION = AllIcons.Nodes.Function;
	Image VARIABLE = AllIcons.Nodes.Variable;
	Image CONSTANT = IconLoader.findIcon("/icons/constant.png");
	Image PARAMETER = AllIcons.Nodes.Parameter;
	Image FIELD = AllIcons.Nodes.Field;
	Image LABEL = null; // todo: we need an icon here!
	Image RECEIVER = AllIcons.Nodes.Parameter;
	Image PACKAGE = AllIcons.Nodes.Package;
	Image MODULE_ICON = IconLoader.findIcon("/icons/goModule.png");
	Image DEBUG = ICON;
	Image DIRECTORY = AllIcons.Nodes.TreeClosed;
}

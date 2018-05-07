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

import javax.swing.Icon;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.PlatformIcons;
import consulo.ui.image.Image;
import consulo.ui.image.ImageEffects;

@SuppressWarnings("ConstantConditions")
public interface GoIcons
{
	Image ICON = IconLoader.findIcon("/icons/go.png");
	Icon TYPE = IconLoader.findIcon("/icons/type.png");
	Image APPLICATION_RUN = ImageEffects.folded(ICON, AllIcons.Nodes.RunnableMark);
	Image TEST_RUN = ImageEffects.folded(ICON, AllIcons.Nodes.JunitTestMark);
	Icon METHOD = AllIcons.Nodes.Method;
	Icon FUNCTION = AllIcons.Nodes.Function;
	Icon VARIABLE = AllIcons.Nodes.Variable;
	Icon CONSTANT = IconLoader.findIcon("/icons/constant.png");
	Icon PARAMETER = AllIcons.Nodes.Parameter;
	Icon FIELD = AllIcons.Nodes.Field;
	Icon LABEL = null; // todo: we need an icon here!
	Icon RECEIVER = AllIcons.Nodes.Parameter;
	Icon PACKAGE = AllIcons.Nodes.Package;
	Icon MODULE_ICON = IconLoader.findIcon("/icons/goModule.png");
	Image DEBUG = ICON;
	Icon DIRECTORY = PlatformIcons.DIRECTORY_CLOSED_ICON;
}

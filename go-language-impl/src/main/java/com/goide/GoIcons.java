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

import consulo.go.icon.GoIconGroup;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;

@Deprecated
public interface GoIcons {
  Image ICON = GoIconGroup.go();
  Image TYPE = PlatformIconGroup.nodesTypealias();
  Image APPLICATION_RUN = GoIconGroup.goapp();
  Image TEST_RUN = GoIconGroup.gotest();
  Image METHOD = PlatformIconGroup.nodesMethod();
  Image FUNCTION = PlatformIconGroup.nodesFunction();
  Image VARIABLE = PlatformIconGroup.nodesVariable();
  Image CONSTANT = PlatformIconGroup.nodesConstant();
  Image PARAMETER = PlatformIconGroup.nodesParameter();
  Image FIELD = PlatformIconGroup.nodesField();
  Image LABEL = null; // todo: we need an icon here!
  Image RECEIVER = PlatformIconGroup.nodesParameter();
  Image PACKAGE = PlatformIconGroup.nodesPackage();
  Image DIRECTORY = PlatformIconGroup.nodesTreeclosed();
}

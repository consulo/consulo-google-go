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

package com.goide.project;

import consulo.util.collection.ArrayUtil;
import consulo.util.lang.ThreeState;
import consulo.util.xml.serializer.annotation.Tag;

import javax.annotation.Nonnull;
import java.util.Arrays;

@Tag("buildTags")
public class GoBuildTargetSettings implements Cloneable {
  public static final String ANY_COMPILER = "Any";
  public static final String DEFAULT = "default";

  @Nonnull
  public String os = DEFAULT;
  @Nonnull
  public String arch = DEFAULT;
  @Nonnull
  public ThreeState cgo = ThreeState.UNSURE;
  @Nonnull
  public String compiler = ANY_COMPILER;
  @Nonnull
  public String goVersion = DEFAULT;
  @Nonnull
  public String[] customFlags = ArrayUtil.EMPTY_STRING_ARRAY;

  @Override
  public GoBuildTargetSettings clone() {
    try {
      return (GoBuildTargetSettings)super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GoBuildTargetSettings)) return false;

    GoBuildTargetSettings settings = (GoBuildTargetSettings)o;

    if (!os.equals(settings.os)) return false;
    if (!arch.equals(settings.arch)) return false;
    if (cgo != settings.cgo) return false;
    if (!compiler.equals(settings.compiler)) return false;
    if (!goVersion.equals(settings.goVersion)) return false;
    return Arrays.equals(customFlags, settings.customFlags);
  }

  @Override
  public int hashCode() {
    int result = os.hashCode();
    result = 31 * result + arch.hashCode();
    result = 31 * result + cgo.hashCode();
    result = 31 * result + compiler.hashCode();
    result = 31 * result + goVersion.hashCode();
    result = 31 * result + Arrays.hashCode(customFlags);
    return result;
  }
}

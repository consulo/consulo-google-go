/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Florin Patan, Stuart Carnie
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

package com.plan9.intel;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileNameMatcherFactory;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class AsmIntelFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@Nonnull FileTypeConsumer consumer) {
    FileNameMatcherFactory nameMatcherFactory = FileNameMatcherFactory.getInstance();

    consumer.consume(AsmIntelFileType.INSTANCE,
        nameMatcherFactory.createWildcardFileNameMatcher("*_amd64.s"),
        nameMatcherFactory.createWildcardFileNameMatcher("*_amd64p32.s"),
        nameMatcherFactory.createWildcardFileNameMatcher("*_386.s"));
  }
}

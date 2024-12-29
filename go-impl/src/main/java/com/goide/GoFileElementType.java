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

import com.goide.psi.GoFile;
import com.goide.stubs.GoFileStub;
import com.goide.stubs.index.GoPackagesIndex;
import consulo.index.io.StringRef;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.*;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import java.io.IOException;

public class GoFileElementType extends IStubFileElementType<GoFileStub> {
  public static final IStubFileElementType INSTANCE = new GoFileElementType();
  public static final int VERSION = 21;

  private GoFileElementType() {
    super("GO_FILE", GoLanguage.INSTANCE);
  }

  @Override
  public int getStubVersion() {
    return VERSION;
  }

  @Nonnull
  @Override
  public StubBuilder getBuilder() {
    return new DefaultStubBuilder() {
      @Nonnull
      @Override
      protected StubElement createStubForFile(@Nonnull PsiFile file) {
        if (file instanceof GoFile) {
          return new GoFileStub((GoFile)file);
        }
        return super.createStubForFile(file);
      }
    };
  }

  @Override
  public void indexStub(@Nonnull GoFileStub stub, @Nonnull IndexSink sink) {
    String packageName = stub.getPackageName();
    if (StringUtil.isNotEmpty(packageName)) {
      sink.occurrence(GoPackagesIndex.KEY, packageName);
    }
  }

  @Override
  public void serialize(@Nonnull GoFileStub stub, @Nonnull StubOutputStream dataStream) throws IOException {
    dataStream.writeUTF(StringUtil.notNullize(stub.getBuildFlags()));
  }

  @Nonnull
  @Override
  public GoFileStub deserialize(@Nonnull StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new GoFileStub(null, StringRef.fromNullableString(StringUtil.nullize(dataStream.readUTF())));
  }

  @Nonnull
  @Override
  public String getExternalId() {
    return "go.FILE";
  }
}

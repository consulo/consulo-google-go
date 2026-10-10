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

package consulo.go.it.support;

import consulo.annotation.component.ServiceImpl;
import consulo.process.DefaultCharsetProvider;
import jakarta.inject.Singleton;

import java.nio.charset.Charset;

/**
 * The IDE binds the default charset in a module the headless application does not have; the processes of go need one.
 */
@Singleton
@ServiceImpl
public class TestDefaultCharsetProvider implements DefaultCharsetProvider {
    @Override
    public Charset getDefaultCharset() {
        return Charset.defaultCharset();
    }
}

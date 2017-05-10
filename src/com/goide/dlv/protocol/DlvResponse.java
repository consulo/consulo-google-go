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

package com.goide.dlv.protocol;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jsonProtocol.JsonType;
import org.jetbrains.jsonProtocol.Optional;

import java.util.Collections;
import java.util.List;

@JsonType
public interface DlvResponse {
  int id();

  @Nullable
  @Optional
  JsonElement result();

  @Nullable
  @Optional
  ErrorInfo error();

  @JsonType
  interface ErrorInfo {
    @Nullable
    String message();

    @NotNull
    @Optional
    List<String> data();

    int code();
  }

  final class CommandResponseImpl implements DlvResponse {
    @Nullable
    private DlvResponse.ErrorInfo myErrorInfo;
    private int myId = -1;
    @Nullable
    private JsonElement myResult;

    public CommandResponseImpl(@NotNull JsonElement element) {
      if (element instanceof JsonObject) {
        JsonPrimitive idElement = ((JsonObject)element).getAsJsonPrimitive("id");
        if (idElement != null) {
          myId = idElement.getAsInt();
        }

        JsonElement result = ((JsonObject)element).get("result");
        myResult = result instanceof JsonNull ? null : result;

        JsonElement errorElement = ((JsonObject)element).get("error");
        myErrorInfo = errorElement instanceof JsonNull ? null : new ErrorInfoImpl(errorElement);
      }
    }

    @Nullable
    @Override
    public DlvResponse.ErrorInfo error() {
      return myErrorInfo;
    }

    @Override
    public int id() {
      return myId;
    }

    @Nullable
    @Override
    public JsonElement result() {
      return myResult;
    }
  }

  final class ErrorInfoImpl implements DlvResponse.ErrorInfo {
    private static final int ourId = -1;
    @NotNull
    private final List<String> _data = Collections.emptyList();
    @Nullable
    private final String _message;

    ErrorInfoImpl(@NotNull JsonElement element) {
      _message = element instanceof JsonNull ? null : element instanceof JsonPrimitive ? element.getAsString() : null;
    }

    @Override
    public int code() {
      return ourId;
    }

    @NotNull
    @Override
    public List<String> data() {
      return _data;
    }

    @Nullable
    @Override
    public String message() {
      return _message;
    }
  }
}
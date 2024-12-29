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

package com.goide.dlv;

import com.goide.dlv.protocol.DlvRequest;
import com.goide.dlv.protocol.DlvResponse;
import com.google.gson.JsonElement;
import consulo.logging.Logger;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.lang.StringUtil;
import org.jetbrains.rpc.CommandProcessor;
import org.jetbrains.rpc.RequestCallback;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public abstract class DlvCommandProcessor extends CommandProcessor<JsonElement, DlvResponse, DlvResponse> {
  private static final Logger LOG = Logger.getInstance(DlvCommandProcessor.class);

  @Nullable
  @Override
  public DlvResponse readIfHasSequence(@Nonnull JsonElement message) {
    return new DlvResponse.CommandResponseImpl(message);
  }

  @Override
  public int getSequence(@Nonnull DlvResponse response) {
    return response.id();
  }

  @Override
  public void acceptNonSequence(JsonElement message) {
  }

  public void processIncomingJson(@Nonnull JsonElement reader) {
    getMessageManager().processIncoming(reader);
  }

  @Override
  public void call(@Nonnull DlvResponse response, @Nonnull RequestCallback<DlvResponse> callback) {
    if (response.result() != null) {
      callback.onSuccess(response, this);
    }
    else {
      callback.onError(createMessage(response));
    }
  }

  @Nonnull
  private static String createMessage(@Nonnull DlvResponse r) {
    DlvResponse.ErrorInfo e = r.error();
    if (e == null) return "Internal messaging error";
    List<String> data = e.data();
    String message = e.message();
    if (ContainerUtil.isEmpty(data)) return StringUtil.defaultIfEmpty(message, "<null>");
    List<String> list = new SmartList<>(message);
    list.addAll(data);
    return list.toString();
  }

  @Nonnull
  @Override
  public <RESULT> RESULT readResult(@Nonnull String method, @Nonnull DlvResponse successResponse) {
    JsonElement result = successResponse.result();
    assert result != null : "success result should be not null";

    Type resultType = consulo.google.go.run.dlv.api.DlvRequest.findOutTypeInRegistry(method);
    if (resultType == null) {
      resultType = getResultType(method.replaceFirst("RPCServer\\.", ""));
    }

    Object o = consulo.google.go.run.dlv.api.DlvRequest.ourGson.fromJson(result, resultType);
    //noinspection unchecked
    return (RESULT)o;
  }

  @Nonnull
  private static Type getResultType(@Nonnull String method) {
    for (Class<?> c : DlvRequest.class.getDeclaredClasses()) {
      if (method.equals(c.getSimpleName())) {
        Type s = c.getGenericSuperclass();
        assert s instanceof ParameterizedType : c.getCanonicalName() + " should have a generic parameter for correct callback processing";
        Type[] arguments = ((ParameterizedType)s).getActualTypeArguments();
        assert arguments.length == 1 : c.getCanonicalName() + " should have only one generic argument for correct callback processing";
        return arguments[0];
      }
    }
    LOG.error("Unknown response " + method + ", please register an appropriate request into com.goide.dlv.protocol.DlvRequest");
    return Object.class;
  }
}
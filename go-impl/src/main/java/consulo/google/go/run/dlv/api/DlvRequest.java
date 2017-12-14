/*
 * Copyright 2013-2017 consulo.io
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

package consulo.google.go.run.dlv.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * @author VISTALL
 * @since 10-May-17
 */
public class DlvRequest<In, Out> {
  private static final Map<String, DlvRequest<?, ?>> ourRegistry = new ConcurrentHashMap<>();

  @NotNull
  protected static <In, Out> DlvRequest<In, Out> paramized(String name, Class<In> inObject, Class<Out> outObject, BiConsumer<In, Object[]> args) {
    DlvRequest<In, Out> request = new DlvRequest<>(name, inObject, outObject, args, true);
    ourRegistry.put(request.myName, request);
    return request;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public static <T> Class<T> findOutTypeInRegistry(String methodName) {
    DlvRequest<?, ?> dlvRequest = ourRegistry.get(methodName);
    if (dlvRequest != null) {
      return (Class<T>)dlvRequest.myOutObject;
    }
    return null;
  }

  public static final Gson ourGson = new Gson();

  private final String myName;
  private final Class<In> myInObject;
  private final Class<Out> myOutObject;
  private final BiConsumer<In, Object[]> myBiConsumer;
  private final boolean myParamized;

  public DlvRequest(String name, Class<In> inObject, Class<Out> outObject, BiConsumer<In, Object[]> biConsumer, boolean paramized) {
    myName = "RPCServer." + name;
    myInObject = inObject;
    myOutObject = outObject;
    myBiConsumer = biConsumer;
    myParamized = paramized;
  }

  @NotNull
  public final SimpleInOutMessage<In, Out> build(Object... args) {
    In in = ReflectionUtil.newInstance(myInObject);

    myBiConsumer.accept(in, args);

    JsonObject object = new JsonObject();
    object.addProperty("method", myName);

    if(myParamized) {
      JsonElement inObjectAsElement = ourGson.toJsonTree(in);
      JsonArray params = new JsonArray();
      params.add(inObjectAsElement);

      object.add("params", params);
    }

    return new SimpleInOutMessage<>(myName, object);
  }
}

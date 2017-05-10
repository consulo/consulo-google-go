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

  protected static <In, Out> DlvRequest<In, Out> create(String name, Class<In> inObject, Class<Out> outObject, BiConsumer<In, Object[]> args) {
    DlvRequest<In, Out> request = new DlvRequest<>(name, inObject, outObject, args);
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

  private String myName;
  private Class<In> myInObject;
  private Class<Out> myOutObject;
  private BiConsumer<In, Object[]> myBiConsumer;

  public DlvRequest(String name, Class<In> inObject, Class<Out> outObject, BiConsumer<In, Object[]> biConsumer) {
    myName = "RPCServer." + name;
    myInObject = inObject;
    myOutObject = outObject;
    myBiConsumer = biConsumer;
  }

  @NotNull
  public final SimpleInOutMessage<In, Out> build(Object... args) {
    In in = ReflectionUtil.newInstance(myInObject);

    myBiConsumer.accept(in, args);

    JsonElement inObjectAsElement = ourGson.toJsonTree(in);

    JsonObject object = new JsonObject();
    object.addProperty("method", myName);

    JsonArray params = new JsonArray();
    params.add(inObjectAsElement);

    object.add("params", params);

    return new SimpleInOutMessage<>(myName, object);
  }
}

package consulo.google.go.run.dlv.api;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.jetbrains.jsonProtocol.Request;

import java.nio.charset.StandardCharsets;

/**
 * @author VISTALL
 * @since 10-May-17
 */
public class SimpleInOutMessage<In, Out> implements Request<Out> {
  private final String myName;
  private final JsonObject myObject;

  public SimpleInOutMessage(String name, JsonObject object) {
    myName = name;
    myObject = object;
  }

  @Override
  public ByteBuf getBuffer() {
    ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer();
    String json = DlvRequest.ourGson.toJson(myObject);
    buffer.writeCharSequence(json, StandardCharsets.UTF_8);
    return buffer;
  }

  @Override
  public String getMethodName() {
    return myName;
  }

  @Override
  public void finalize(int id) {
    myObject.addProperty("id", id);
  }
}

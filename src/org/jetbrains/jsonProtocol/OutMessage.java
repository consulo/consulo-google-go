package org.jetbrains.jsonProtocol;

import com.google.gson.stream.JsonWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtf8Writer;

import java.io.IOException;

/**
 * @author VISTALL
 * @since 06-May-17
 * <p>
 * from kotlin platform\script-debugger\protocol\protocol-reader-runtime\src\org\jetbrains\jsonProtocol\OutMessage.kt
 */
public class OutMessage {
  private final ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer();
  private final JsonWriter writer = new JsonWriter(new ByteBufUtf8Writer(buffer));

  private boolean finalized;

  public OutMessage() {
    try {
      writer.beginObject();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeSingletonIntArray(String name, int value) {
    try {
      beginArguments();
      writer.name(name);
      writer.beginArray();
      writer.value((long)value);
      writer.endArray();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeLong(String name, long value) {
    try {
      beginArguments();
      writer.name(name).value(value);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeString(String name, String value) {
    if (value != null) {
      writeNullableString(name, value);
    }
  }

  protected void writeNullableString(String name, CharSequence value) {
    try {
      beginArguments();
      writer.name(name).value(value == null ? null : value.toString());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() throws IOException {
    assert !finalized;
    finalized = true;
    writer.endObject();
    writer.close();
  }

  public ByteBuf getBuffer() {
    return buffer;
  }

  protected void beginArguments() {
  }

  public JsonWriter getWriter() {
    return writer;
  }
}

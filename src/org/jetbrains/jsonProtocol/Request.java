package org.jetbrains.jsonProtocol;

import io.netty.buffer.ByteBuf;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public interface Request<RESULT> {
  ByteBuf getBuffer();

  String getMethodName();

  void finalize(int id);
}

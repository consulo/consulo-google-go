package org.jetbrains.rpc;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public interface ResultReader<RESPONSE> {
  <RESULT> RESULT readResult(String readMethodName, RESPONSE successResponse);
}

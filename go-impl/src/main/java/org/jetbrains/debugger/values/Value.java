package org.jetbrains.debugger.values;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public interface Value {
  ValueType getType();

  String getValueString();
}

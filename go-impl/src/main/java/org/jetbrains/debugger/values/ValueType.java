package org.jetbrains.debugger.values;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public enum ValueType {
  OBJECT,
  NUMBER,
  STRING,
  FUNCTION,
  BOOLEAN,

  ARRAY,
  NODE,

  UNDEFINED,
  NULL,
  SYMBOL;

  /**
   * Returns whether `type` corresponds to a JsObject. Note that while 'null' is an object
   * in JavaScript world, here for API consistency it has bogus type [.NULL] and is
   * not a [ObjectValue]
   */
  public boolean isObjectType() {
    return this == OBJECT || this == ARRAY || this == FUNCTION || this == NODE;
  }

  public static ValueType fromIndex(int index) {
    return values()[index];
  }
}

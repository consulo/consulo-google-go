package org.jetbrains.debugger;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public enum ScopeType {
  GLOBAL,
  LOCAL,
  WITH,
  CLOSURE,
  CATCH,
  LIBRARY,
  CLASS,
  INSTANCE,
  BLOCK,
  SCRIPT,
  UNKNOWN
}

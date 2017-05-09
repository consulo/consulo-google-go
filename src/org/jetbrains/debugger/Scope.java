package org.jetbrains.debugger;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public interface Scope {
  ScopeType getType();

  String getDescription();

  VariablesHost getVariablesHost();

  boolean isGlobal();
}

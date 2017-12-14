package org.jetbrains.debugger;

/**
 * @author VISTALL
 * @since 08-May-17
 */
public enum  StepAction {
  /**
   * Resume execution.
   */
  CONTINUE,

  /**
   * Step into the current statement.
   */
  IN,

  /**
   * Step over the current statement.
   */
  OVER,

  /**
   * Step out of the current function.
   */
  OUT
}

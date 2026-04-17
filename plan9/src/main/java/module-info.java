/**
 * @author VISTALL
 * @since 06-Aug-22
 */
open module consulo.plan9 {
  requires consulo.ide.api;
  requires consulo.language.editor.api;
  requires consulo.language.impl;
  requires consulo.execution.debug.api;

  exports com.plan9.intel;
  exports com.plan9.intel.ide.highlighting;
  exports com.plan9.intel.lang;
  exports com.plan9.intel.lang.core;
  exports com.plan9.intel.lang.core.lexer;
  exports com.plan9.intel.lang.core.parser;
  exports com.plan9.intel.lang.core.psi;
  exports com.plan9.intel.lang.core.psi.impl;
}
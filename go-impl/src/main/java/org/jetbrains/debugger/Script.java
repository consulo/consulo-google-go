package org.jetbrains.debugger;

import consulo.util.dataholder.UserDataHolderEx;

/**
 * @author VISTALL
 * @since 08-May-17
 */
public interface Script extends UserDataHolderEx
{
  enum Type {
    /**
     * A native, internal JavaScript VM script
     */
    NATIVE,

    /**
     * A script supplied by an extension
     */
    EXTENSION,

    /**
     * A normal user script
     */
    NORMAL
  }

  Type getType();
}

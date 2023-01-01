package org.jetbrains.debugger.values;

import consulo.util.concurrent.Obsolescent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public class ValueManager implements Obsolescent {
  private final AtomicInteger cacheStamp = new AtomicInteger();
  private volatile boolean obsolete;

  public int getCacheStamp() {
    return cacheStamp.get();
  }

  public void clearCaches() {
    cacheStamp.incrementAndGet();
  }

  public void markObsolete() {
    obsolete = true;
  }

  @Override
  public boolean isObsolete() {
    return obsolete;
  }
}

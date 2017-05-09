package org.jetbrains.debugger;

import org.jetbrains.concurrency.Promise;
import org.jetbrains.debugger.values.Value;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public interface ValueModifier {
  Promise<?> setValue(Variable variable, String newValue, EvaluateContext evaluateContext);

  Promise<?> setValue(Variable variable, Value newValue, EvaluateContext evaluateContext);

  Promise<Value> evaluateGet(Variable variable, EvaluateContext evaluateContext);
}

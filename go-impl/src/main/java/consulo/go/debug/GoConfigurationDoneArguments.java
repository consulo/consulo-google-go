package consulo.go.debug;

import consulo.execution.debugger.dap.protocol.ConfigurationDoneArguments;

/**
 * @author VISTALL
 * @since 2025-01-02
 */
public class GoConfigurationDoneArguments extends ConfigurationDoneArguments {
    public boolean stopOnEntry;

    public GoConfigurationDoneArguments(boolean stopOnEntry) {
        this.stopOnEntry = stopOnEntry;
    }
}

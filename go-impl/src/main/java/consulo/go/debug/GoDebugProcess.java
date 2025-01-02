package consulo.go.debug;

import com.goide.GoFileType;
import com.goide.GoLanguage;
import consulo.application.ReadAction;
import consulo.application.util.function.ThrowableComputable;
import consulo.execution.debug.XBreakpointManager;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.XDebuggerManager;
import consulo.execution.debug.breakpoint.XLineBreakpoint;
import consulo.execution.debug.evaluation.XDebuggerEditorsProvider;
import consulo.execution.debug.evaluation.XDebuggerEditorsProviderBase;
import consulo.execution.debugger.dap.DAPDebugProcess;
import consulo.execution.debugger.dap.protocol.DAP;
import consulo.execution.debugger.dap.protocol.DAPFactory;
import consulo.execution.debugger.dap.protocol.LaunchRequestArguments;
import consulo.go.debug.breakpoint.GoLineBreakpointType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 2025-01-02
 */
public class GoDebugProcess extends DAPDebugProcess {
    private final int myPort;
    private final String myOutputFilePath;

    public GoDebugProcess(@Nonnull XDebugSession session, int port, String outputFilePath) {
        super(session);
        myPort = port;
        myOutputFilePath = outputFilePath;
    }

    @Override
    protected DAP createDAP(DAPFactory factory) {
        return factory.createSocketDAP("localhost", myPort);
    }

    @Override
    protected String getAdapterId() {
        return "delve";
    }

    @Nonnull
    @Override
    protected Collection<? extends XLineBreakpoint<?>> getLineBreakpoints() {
        XBreakpointManager manager = XDebuggerManager.getInstance(getSession().getProject()).getBreakpointManager();
        return ReadAction.compute((ThrowableComputable<Collection<? extends XLineBreakpoint<?>>, RuntimeException>) () -> manager.getBreakpoints(GoLineBreakpointType.class));
    }

    @Override
    @Nonnull
    protected LaunchRequestArguments createLaunchRequestArguments() {
        GoLaunchRequestArguments launch = new GoLaunchRequestArguments();
        launch.mode = "exec";
        launch.env = Platform.current().os().environmentVariables();
        launch.program = myOutputFilePath;
        return launch;
    }

    @Nonnull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return new XDebuggerEditorsProviderBase() {
            @Nonnull
            @Override
            public FileType getFileType() {
                return GoFileType.INSTANCE;
            }

            @Override
            protected PsiFile createExpressionCodeFragment(@Nonnull Project project, @Nonnull String text, @Nullable PsiElement context, boolean isPhysical) {
                return PsiFileFactory.getInstance(project).createFileFromText("fragment.go", GoLanguage.INSTANCE, text);
            }
        };
    }
}

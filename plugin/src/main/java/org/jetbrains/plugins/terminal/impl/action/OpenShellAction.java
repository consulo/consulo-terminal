package org.jetbrains.plugins.terminal.impl.action;

import consulo.application.platform.PlatformTarget;
import consulo.localize.LocalizeValue;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.toolWindow.ToolWindow;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.terminal.impl.TerminalView;
import org.jetbrains.plugins.terminal.impl.shellDetection.DetectedShellInfo;

public class OpenShellAction extends DumbAwareAction
{
	private final TerminalView myTerminalView;
	private final ToolWindow myToolWindow;
	private final PlatformTarget myTarget;
	private final DetectedShellInfo myShell;
	private final String myPresentableName;

	public OpenShellAction(TerminalView terminalView, ToolWindow toolWindow, PlatformTarget target, DetectedShellInfo shell, String presentableName)
	{
		super(LocalizeValue.of(presentableName));
		myTerminalView = terminalView;
		myToolWindow = toolWindow;
		myTarget = target;
		myShell = shell;
		myPresentableName = presentableName;
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{
		myTerminalView.addNewShellSession(myToolWindow, myTarget, myShell, myPresentableName);
	}
}

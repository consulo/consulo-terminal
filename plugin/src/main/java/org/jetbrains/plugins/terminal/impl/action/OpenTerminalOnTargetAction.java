package org.jetbrains.plugins.terminal.impl.action;

import consulo.application.platform.PlatformTarget;
import consulo.localize.LocalizeValue;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.toolWindow.ToolWindow;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.terminal.impl.TerminalView;

public class OpenTerminalOnTargetAction extends DumbAwareAction
{
	private final TerminalView myTerminalView;
	private final ToolWindow myToolWindow;
	private final PlatformTarget myTarget;

	public OpenTerminalOnTargetAction(TerminalView terminalView, ToolWindow toolWindow, PlatformTarget target, LocalizeValue text)
	{
		super(text, LocalizeValue.empty(), target.getIcon());
		myTerminalView = terminalView;
		myToolWindow = toolWindow;
		myTarget = target;
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{
		myTerminalView.addNewSession(myToolWindow, myTarget, null, null);
	}
}

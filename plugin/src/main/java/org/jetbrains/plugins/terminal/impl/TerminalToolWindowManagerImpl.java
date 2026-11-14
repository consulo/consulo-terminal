package org.jetbrains.plugins.terminal.impl;

import consulo.annotation.component.ServiceImpl;
import consulo.application.platform.PlatformTarget;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;

import java.util.List;

@Singleton
@ServiceImpl
public class TerminalToolWindowManagerImpl implements TerminalToolWindowManager
{
	private final TerminalView myTerminalView;

	@Inject
	public TerminalToolWindowManagerImpl(TerminalView terminalView)
	{
		myTerminalView = terminalView;
	}

	@RequiredUIAccess
	@Override
	public void createNewSession(PlatformTarget target, @Nullable String workingDirectory, List<String> shellCommand, String tabName)
	{
		if(shellCommand.isEmpty())
		{
			throw new IllegalArgumentException("Empty shell command");
		}
		myTerminalView.openCommandSession(target, workingDirectory, List.copyOf(shellCommand), tabName);
	}
}

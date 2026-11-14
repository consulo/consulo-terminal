package org.jetbrains.plugins.terminal;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.application.platform.PlatformTarget;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.annotation.Nullable;

import java.util.List;

@ServiceAPI(ComponentScope.PROJECT)
public interface TerminalToolWindowManager
{
	/**
	 * Opens a new tab of the terminal tool window which runs exactly this command - the shell and its options - on the machine of the target.
	 * A target which is not connected is connected first.
	 *
	 * @param workingDirectory a path of that machine, null for the project directory on the machine of the project and the home directory elsewhere
	 */
	@RequiredUIAccess
	void createNewSession(PlatformTarget target, @Nullable String workingDirectory, List<String> shellCommand, String tabName);
}

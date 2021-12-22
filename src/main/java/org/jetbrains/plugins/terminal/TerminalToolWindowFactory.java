package org.jetbrains.plugins.terminal;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;

/**
 * @author traff
 */
public class TerminalToolWindowFactory implements ToolWindowFactory, DumbAware
{
	public static final String TOOL_WINDOW_ID = "Terminal";

	@RequiredUIAccess
	@Override
	public void createToolWindowContent(@Nonnull Project project, @Nonnull ToolWindow toolWindow)
	{
		TerminalView terminalView = TerminalView.getInstance(project);
		terminalView.initTerminal((ToolWindowEx) toolWindow);

		project.getMessageBus().connect(project).subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener()
		{
			@Override
			public void stateChanged(ToolWindowManager manager)
			{
				boolean visible = toolWindow.isVisible();
				if(visible && toolWindow.getContentManager().getContentCount() == 0)
				{
					terminalView.addNewSession(toolWindow, null);
				}
			}
		});
	}
}

package org.jetbrains.plugins.terminal;

import javax.annotation.Nonnull;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;

/**
 * @author traff
 */
public class TerminalToolWindowFactory implements ToolWindowFactory, DumbAware
{
	public static final String TOOL_WINDOW_ID = "Terminal";

	@Override
	public void createToolWindowContent(@Nonnull Project project, @Nonnull ToolWindow toolWindow)
	{
		TerminalView terminalView = TerminalView.getInstance(project);
		terminalView.initTerminal((ToolWindowEx) toolWindow);

		project.getMessageBus().connect(project).subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener()
		{
			@Override
			public void stateChanged()
			{
				boolean visible = toolWindow.isVisible();
				if(visible && toolWindow.getContentManager().getContentCount() == 0)
				{
					terminalView.addNewSession(toolWindow);
				}
			}
		});
	}
}

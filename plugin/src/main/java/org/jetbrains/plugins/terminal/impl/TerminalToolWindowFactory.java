package org.jetbrains.plugins.terminal.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.project.ui.wm.ToolWindowFactory;
import consulo.project.ui.wm.ToolWindowManager;
import consulo.project.ui.wm.ToolWindowManagerListener;
import consulo.terminal.icon.TerminalIconGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.ui.ex.toolWindow.ToolWindowAnchor;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author traff
 */
@ExtensionImpl
public class TerminalToolWindowFactory implements ToolWindowFactory, DumbAware
{
	public static final String TOOL_WINDOW_ID = "Terminal";

	@Nonnull
	@Override
	public String getId()
	{
		return TOOL_WINDOW_ID;
	}

	@Override
	public boolean canCloseContents()
	{
		return true;
	}

	@Nonnull
	@Override
	public LocalizeValue getDisplayName()
	{
		return LocalizeValue.localizeTODO(TOOL_WINDOW_ID);
	}
	@Nonnull
	@Override
	public ToolWindowAnchor getAnchor()
	{
		return ToolWindowAnchor.BOTTOM;
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return TerminalIconGroup.openterminal_13x13();
	}

	@RequiredUIAccess
	@Override
	public void createToolWindowContent(@Nonnull Project project, @Nonnull ToolWindow toolWindow)
	{
		TerminalView terminalView = TerminalView.getInstance(project);
		terminalView.initTerminal(toolWindow);

		project.getMessageBus().connect(project).subscribe(ToolWindowManagerListener.class, new ToolWindowManagerListener()
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

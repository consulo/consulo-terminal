package org.jetbrains.plugins.terminal;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.text.UniqueNameGenerator;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author traff
 */
@Singleton
public class TerminalView
{
	private final Project myProject;

	@Inject
	public TerminalView(Project project)
	{
		myProject = project;
	}

	public static TerminalView getInstance(@Nonnull Project project)
	{
		return project.getComponent(TerminalView.class);
	}

	public void initTerminal(final ToolWindowEx toolWindow)
	{
		toolWindow.setToHideOnEmptyContent(true);

		toolWindow.setTabActions(DumbAwareAction.create("Add Terminal", AllIcons.General.Add, event -> addNewSession(toolWindow)));

		toolWindow.setTabDoubleClickActions(DumbAwareAction.create("Rename tab", null, event -> {
			ContentManager contentManager = toolWindow.getContentManager();

			Content selectedContent = contentManager.getSelectedContent();
			if(selectedContent == null)
			{
				return;
			}

			String displayName = selectedContent.getDisplayName();
			String newName = Messages.showInputDialog(myProject, displayName, "Enter Tab Name", null);

			if(!StringUtil.isEmptyOrSpaces(newName) && !displayName.equals(newName))
			{
				selectedContent.setTabName(displayName);
				selectedContent.setDisplayName(displayName);
			}
		}));

		addNewSession(toolWindow);
	}

	public void addNewSession(ToolWindow toolWindow)
	{
		LocalTerminalDirectRunner terminalRunner = new LocalTerminalDirectRunner(myProject);

		consulo.disposer.Disposable widgetDisposable = consulo.disposer.Disposable.newDisposable();

		ContentManager contentManager = toolWindow.getContentManager();

		final JBTerminalSystemSettingsProvider provider = new JBTerminalSystemSettingsProvider();

		JBTerminalWidget widget = new JBTerminalWidget(provider, widgetDisposable);
		terminalRunner.openSession(widget);

		List<String> names = Arrays.stream(contentManager.getContents()).map(Content::getDisplayName).collect(Collectors.toList());
		String uniqueName = UniqueNameGenerator.generateUniqueName(widget.getSessionName(), "", "", " ", "", name -> !names.contains(name));

		Content content = contentManager.getFactory().createContent(widget, uniqueName, false);
		content.setCloseable(true);
		content.setTabName(uniqueName);
		content.setDisposer(widgetDisposable::dispose);

		contentManager.addContent(content);
		contentManager.setSelectedContent(content);
	}
}

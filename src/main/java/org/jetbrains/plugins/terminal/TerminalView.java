package org.jetbrains.plugins.terminal;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.text.UniqueNameGenerator;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

	private boolean myDoAddNewSessionOnInit;

	@Inject
	public TerminalView(Project project)
	{
		myProject = project;
	}

	public static TerminalView getInstance(@Nonnull Project project)
	{
		return project.getComponent(TerminalView.class);
	}

	@RequiredUIAccess
	public void openTerminalIn(@Nullable VirtualFile fileToOpen)
	{
		String workDirectory = null;
		VirtualFile parentDirectory = fileToOpen != null && !fileToOpen.isDirectory() ? fileToOpen.getParent() : fileToOpen;
		if(parentDirectory != null)
		{
			workDirectory = parentDirectory.getPath();
		}

		ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);

		myDoAddNewSessionOnInit = true;

		addNewSession(toolWindow, workDirectory);
		
		toolWindow.activate(null);
	}

	public void initTerminal(final ToolWindowEx toolWindow)
	{
		toolWindow.setToHideOnEmptyContent(true);

		toolWindow.setTabActions(DumbAwareAction.create("Add Terminal", AllIcons.General.Add, event -> addNewSession(toolWindow, null)));

		toolWindow.setTabDoubleClickActions(DumbAwareAction.create("Rename tab", null, event ->
		{
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

		if(!myDoAddNewSessionOnInit)
		{
			addNewSession(toolWindow, null);
		}

		myDoAddNewSessionOnInit = false;
	}

	public void addNewSession(@Nonnull ToolWindow toolWindow, @Nullable String workDirectory)
	{
		LocalTerminalDirectRunner terminalRunner = new LocalTerminalDirectRunner(myProject);

		ContentManager contentManager = toolWindow.getContentManager();

		Disposable parentDisposable = Disposable.newDisposable("terminal view");

		JBTerminalSystemSettingsProvider provider = new JBTerminalSystemSettingsProvider(myProject.getApplication(), parentDisposable);

		JBTerminalWidget widget = new JBTerminalWidget(provider);
		Disposer.register(parentDisposable, widget);

		terminalRunner.openSessionInDirectory(widget, workDirectory);

		List<String> names = Arrays.stream(contentManager.getContents()).map(Content::getDisplayName).collect(Collectors.toList());
		String uniqueName = UniqueNameGenerator.generateUniqueName(widget.getSessionName(), "", "", " ", "", name -> !names.contains(name));

		Content content = contentManager.getFactory().createContent(widget, uniqueName, false);
		content.setCloseable(true);
		content.setTabName(uniqueName);
		content.setDisposer(parentDisposable);

		contentManager.addContent(content);
		contentManager.setSelectedContent(content);
	}
}

package org.jetbrains.plugins.terminal.impl;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.AllIcons;
import consulo.component.util.text.UniqueNameGenerator;
import consulo.disposer.Disposable;
import consulo.execution.terminal.TerminalSession;
import consulo.execution.terminal.TerminalSessionFactory;
import consulo.execution.ui.terminal.TerminalConsole;
import consulo.execution.ui.terminal.TerminalConsoleFactory;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.project.ui.wm.ToolWindowManager;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.content.Content;
import consulo.ui.ex.content.ContentManager;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author traff
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
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
		return project.getInstance(TerminalView.class);
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

	public void initTerminal(final ToolWindow toolWindow)
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
		ContentManager contentManager = toolWindow.getContentManager();

		Disposable parentDisposable = Disposable.newDisposable("terminal view");

		TerminalSessionFactory sessionFactory = myProject.getApplication().getInstance(TerminalSessionFactory.class);

		if(workDirectory == null)
		{
			workDirectory = currentProjectFolder(myProject);
		}

		TerminalSession session = sessionFactory.createLocal("Local", workDirectory, () -> TerminalOptionsProvider.getInstance().getShellPathOrDefault());

		TerminalConsoleFactory terminalConsoleFactory = myProject.getInstance(TerminalConsoleFactory.class);

		TerminalConsole terminalConsole = terminalConsoleFactory.create(session, TerminalOptionsProvider.getInstance(), parentDisposable);

		List<String> names = Arrays.stream(contentManager.getContents()).map(Content::getDisplayName).collect(Collectors.toList());
		String uniqueName = UniqueNameGenerator.generateUniqueName(terminalConsole.getSessionName(), "", "", " ", "", name -> !names.contains(name));

		Content content = contentManager.getFactory().createUIContent(terminalConsole.getUIComponent(), uniqueName, false);
		content.setCloseable(true);
		content.setTabName(uniqueName);
		content.setDisposer(parentDisposable);

		contentManager.addContent(content);
		contentManager.setSelectedContent(content);
	}

	@Nonnull
	private static String currentProjectFolder(Project project)
	{
		final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);

		final VirtualFile[] roots = projectRootManager.getContentRoots();
		if(roots.length == 1)
		{
			roots[0].getCanonicalPath();
		}
		return project.getBasePath();
	}
}

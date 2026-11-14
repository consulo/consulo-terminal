package org.jetbrains.plugins.terminal.impl;

import com.jediterm.terminal.TtyConnector;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.AllIcons;
import consulo.application.concurrent.ApplicationConcurrency;
import consulo.application.platform.PlatformTarget;
import consulo.application.platform.PlatformTargetManager;
import consulo.component.util.text.UniqueNameGenerator;
import consulo.disposer.Disposable;
import consulo.execution.terminal.TerminalSession;
import consulo.execution.terminal.TerminalSessionFactory;
import consulo.execution.ui.terminal.TerminalConsole;
import consulo.execution.ui.terminal.TerminalConsoleFactory;
import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.project.Project;
import consulo.project.ui.wm.ToolWindowManager;
import consulo.terminal.localize.TerminalLocalize;
import consulo.ui.MessageBoxes;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.content.Content;
import consulo.ui.ex.content.ContentManager;
import consulo.ui.ex.toolWindow.ToolWindow;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.plugins.terminal.impl.action.TerminalNewPredefinedSessionAction;
import org.jetbrains.plugins.terminal.impl.shellDetection.DetectedShellInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author traff
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class TerminalView
{
	private static final Logger LOG = Logger.getInstance(TerminalView.class);

	private final Project myProject;
	private final PlatformTargetManager myTargetManager;
	private final TerminalSessionFactory mySessionFactory;
	private final ApplicationConcurrency myConcurrency;

	private boolean myDoAddNewSessionOnInit;
	private int myPendingSessions;

	@Inject
	public TerminalView(Project project,
						PlatformTargetManager targetManager,
						TerminalSessionFactory sessionFactory,
						ApplicationConcurrency concurrency)
	{
		myProject = project;
		myTargetManager = targetManager;
		mySessionFactory = sessionFactory;
		myConcurrency = concurrency;
	}

	public static TerminalView getInstance(@Nonnull Project project)
	{
		return project.getInstance(TerminalView.class);
	}

	@RequiredUIAccess
	public void openTerminalIn(@Nullable VirtualFile fileToOpen)
	{
		PlatformTarget target = getProjectTarget();
		String workDirectory = null;
		VirtualFile parentDirectory = fileToOpen != null && !fileToOpen.isDirectory() ? fileToOpen.getParent() : fileToOpen;
		if(parentDirectory != null)
		{
			Path path = parentDirectory.toNioPath();
			PlatformTarget pathTarget = myTargetManager.findTargetOf(path);
			if(pathTarget != null)
			{
				target = pathTarget;
			}
			workDirectory = path.toString();
		}

		ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);

		myDoAddNewSessionOnInit = true;

		addNewSession(toolWindow, target, workDirectory, null);

		toolWindow.activate(null);
	}

	/**
	 * Opens a new tab of the terminal and types the command into its shell, as if the user did
	 */
	@RequiredUIAccess
	public void runInNewSession(@Nonnull String command, @Nullable String workDirectory)
	{
		ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);

		myDoAddNewSessionOnInit = true;

		addNewSession(toolWindow, getProjectTarget(), workDirectory, command);

		toolWindow.activate(null);
	}

	@RequiredUIAccess
	public void initTerminal(final ToolWindow toolWindow)
	{
		toolWindow.setToHideOnEmptyContent(true);

		toolWindow.setTabActions(
			DumbAwareAction.create("Add Terminal", AllIcons.General.Add, event -> addNewSession(toolWindow, null)),
			new TerminalNewPredefinedSessionAction(myProject, this, toolWindow)
		);

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

	@RequiredUIAccess
	public void addNewSessionIfEmpty(@Nonnull ToolWindow toolWindow)
	{
		if(myPendingSessions == 0 && toolWindow.getContentManager().getContentCount() == 0)
		{
			addNewSession(toolWindow, null);
		}
	}

	@RequiredUIAccess
	public void addNewSession(@Nonnull ToolWindow toolWindow, @Nullable String workDirectory)
	{
		addNewSession(toolWindow, getProjectTarget(), workDirectory, null);
	}

	@Nonnull
	public List<PlatformTarget> getTargets()
	{
		PlatformTarget projectTarget = getProjectTarget();
		List<PlatformTarget> targets = new ArrayList<>();
		if(projectTarget != null)
		{
			targets.add(projectTarget);
		}
		for(PlatformTarget target : myTargetManager.getTargets())
		{
			if(projectTarget == null || !target.getId().equals(projectTarget.getId()))
			{
				targets.add(target);
			}
		}
		return targets;
	}

	public boolean isProjectTarget(@Nonnull PlatformTarget target)
	{
		return target.getId().equals(getProjectTargetId());
	}

	@RequiredUIAccess
	public void addNewSession(@Nonnull ToolWindow toolWindow, @Nullable PlatformTarget target, @Nullable String workDirectory, @Nullable String command)
	{
		startSession(toolWindow, target, workDirectory, command, null, null, getConnectorName(target));
	}

	@RequiredUIAccess
	public void addNewShellSession(@Nonnull ToolWindow toolWindow, @Nonnull PlatformTarget target, @Nonnull DetectedShellInfo shell, @Nonnull String tabName)
	{
		if(shell.options().isEmpty())
		{
			startSession(toolWindow, target, null, null, shell.path(), null, tabName);
		}
		else
		{
			List<String> shellCommand = new ArrayList<>();
			shellCommand.add(shell.path());
			shellCommand.addAll(shell.options());
			startSession(toolWindow, target, null, null, null, shellCommand, tabName);
		}
	}

	@RequiredUIAccess
	public void openCommandSession(@Nonnull PlatformTarget target, @Nullable String workDirectory, @Nonnull List<String> shellCommand, @Nonnull String tabName)
	{
		ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);

		myDoAddNewSessionOnInit = true;

		startSession(toolWindow, target, workDirectory, null, null, shellCommand, tabName);

		toolWindow.activate(null);
	}

	@RequiredUIAccess
	private void startSession(@Nonnull ToolWindow toolWindow,
							  @Nullable PlatformTarget target,
							  @Nullable String workDirectory,
							  @Nullable String command,
							  @Nullable String shellPath,
							  @Nullable List<String> shellCommand,
							  @Nonnull String connectorName)
	{
		UIAccess uiAccess = UIAccess.current();

		CompletableFuture<Platform> platformFuture = target == null ? CompletableFuture.completedFuture(myProject.getPlatform()) : target.connect();

		myPendingSessions++;
		platformFuture
			.thenApplyAsync(platform -> connect(platform, target, workDirectory, command, shellPath, shellCommand, connectorName), myConcurrency.executor())
			.whenCompleteAsync((connector, error) ->
			{
				myPendingSessions--;
				if(myProject.isDisposed())
				{
					if(connector != null)
					{
						connector.close();
					}
					return;
				}

				if(error != null)
				{
					Throwable cause = error instanceof CompletionException && error.getCause() != null ? error.getCause() : error;
					LOG.warn("Cannot start terminal on " + connectorName, cause);
					MessageBoxes.okError(TerminalLocalize.terminalStartError(connectorName, StringUtil.notNullize(cause.getMessage()))).showAsync();
					return;
				}

				createContent(toolWindow, connector);
			}, uiAccess);
	}

	@Nonnull
	private TtyConnector connect(@Nonnull Platform platform,
								 @Nullable PlatformTarget target,
								 @Nullable String workDirectory,
								 @Nullable String command,
								 @Nullable String shellPath,
								 @Nullable List<String> shellCommand,
								 @Nonnull String connectorName)
	{
		boolean projectMachine = target == null || isProjectTarget(target);

		String directory = workDirectory;
		if(directory == null)
		{
			directory = projectMachine ? currentProjectFolder(myProject) : platform.user().homePath().toString();
		}

		TerminalSession session;
		if(shellCommand != null)
		{
			session = mySessionFactory.create(platform, connectorName, directory, shellCommand);
		}
		else
		{
			String path = shellPath != null ? shellPath : StringUtil.notNullize(getConfiguredShellPath(target, projectMachine));
			session = mySessionFactory.create(platform, connectorName, directory, () -> path);
		}

		if(command != null)
		{
			session = new TypingTerminalSession(session, command);
		}

		try
		{
			return session.connect();
		}
		catch(ExecutionException e)
		{
			throw new CompletionException(e.getCause() != null ? e.getCause() : e);
		}
	}

	@Nullable
	private String getConfiguredShellPath(@Nullable PlatformTarget target, boolean projectMachine)
	{
		if(isLocalTarget(target))
		{
			return TerminalOptionsProvider.getInstance().getShellPath();
		}
		if(projectMachine)
		{
			return TerminalProjectOptionsProvider.getInstance(myProject).getShellPath();
		}
		return null;
	}

	@RequiredUIAccess
	private void createContent(@Nonnull ToolWindow toolWindow, @Nonnull TtyConnector connector)
	{
		ContentManager contentManager = toolWindow.getContentManager();

		Disposable parentDisposable = Disposable.newDisposable("terminal view");

		TerminalConsoleFactory terminalConsoleFactory = myProject.getInstance(TerminalConsoleFactory.class);

		TerminalConsole terminalConsole = terminalConsoleFactory.create(connector, TerminalOptionsProvider.getInstance(), parentDisposable);

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
	private String getConnectorName(@Nullable PlatformTarget target)
	{
		if(isLocalTarget(target))
		{
			return TerminalOptionsProvider.getInstance().getTabNameOrDefault();
		}
		if(target == null)
		{
			return myProject.getPlatform().getName();
		}
		return target.getDisplayName().get();
	}

	private boolean isLocalTarget(@Nullable PlatformTarget target)
	{
		if(target == null)
		{
			return myProject.getPlatformTargetId() == null;
		}
		return target.getId().equals(myTargetManager.getLocalTarget().getId());
	}

	@Nullable
	private PlatformTarget getProjectTarget()
	{
		return myTargetManager.findTarget(myProject.getPlatformTargetId());
	}

	@Nonnull
	private String getProjectTargetId()
	{
		return Objects.requireNonNullElse(myProject.getPlatformTargetId(), myTargetManager.getLocalTarget().getId());
	}

	@Nonnull
	private static String currentProjectFolder(Project project)
	{
		return StringUtil.notNullize(project.getBasePath());
	}
}

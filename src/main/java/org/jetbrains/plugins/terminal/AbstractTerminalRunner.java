package org.jetbrains.plugins.terminal;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.swing.JPanel;

import javax.annotation.Nullable;
import com.google.common.base.Predicate;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.actions.CloseAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.ui.UIUtil;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.TerminalSession;
import com.jediterm.terminal.ui.TerminalWidget;

/**
 * @author traff
 */
public abstract class AbstractTerminalRunner<T extends Process>
{
	private static final Logger LOG = Logger.getInstance(AbstractTerminalRunner.class.getName());
	@Nonnull
	protected final Project myProject;

	public AbstractTerminalRunner(@Nonnull Project project)
	{
		myProject = project;
	}

	public void run()
	{
		ProgressManager.getInstance().run(new Task.Backgroundable(myProject, "Running the terminal", false)
		{
			public void run(@Nonnull final ProgressIndicator indicator)
			{
				indicator.setText("Running the terminal...");
				try
				{
					doRun();
				}
				catch(final Exception e)
				{
					LOG.warn("Error running terminal", e);

					UIUtil.invokeLaterIfNeeded(new Runnable()
					{

						@Override
						public void run()
						{
							Messages.showErrorDialog(AbstractTerminalRunner.this.getProject(), e.getMessage(),
									getTitle());
						}
					});
				}
			}
		});
	}

	private void doRun()
	{
		// Create Server process
		try
		{
			final T process = createProcess(null);

			UIUtil.invokeLaterIfNeeded(new Runnable()
			{
				@Override
				public void run()
				{
					initConsoleUI(process);
				}
			});
		}
		catch(Exception e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	protected abstract T createProcess(@Nullable String directory) throws ExecutionException;

	protected abstract ProcessHandler createProcessHandler(T process);

	@Nonnull
	public JBTabbedTerminalWidget createTerminalWidget(@Nonnull Disposable parent)
	{
		final JBTerminalSystemSettingsProvider provider = new JBTerminalSystemSettingsProvider();
		JBTabbedTerminalWidget terminalWidget = new JBTabbedTerminalWidget(myProject, provider,
				new Predicate<Pair<TerminalWidget, String>>()
		{
			@Override
			public boolean apply(Pair<TerminalWidget, String> widget)
			{
				openSessionInDirectory(widget.getFirst(), widget.getSecond());
				return true;
			}
		}, parent);
		openSessionInDirectory(terminalWidget, null);
		return terminalWidget;
	}

	private void initConsoleUI(final T process)
	{
		final Executor defaultExecutor = DefaultRunExecutor.getRunExecutorInstance();
		final DefaultActionGroup toolbarActions = new DefaultActionGroup();
		final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN,
				toolbarActions, false);


		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(actionToolbar.getComponent(), BorderLayout.WEST);


		actionToolbar.setTargetComponent(panel);

		ProcessHandler processHandler = createProcessHandler(process);

		final RunContentDescriptor contentDescriptor = new RunContentDescriptor(null, processHandler, panel,
				getTerminalConnectionName(process));

		contentDescriptor.setAutoFocusContent(true);

		toolbarActions.add(createCloseAction(defaultExecutor, contentDescriptor));

		final JBTerminalSystemSettingsProvider provider = new JBTerminalSystemSettingsProvider();
		TerminalWidget widget = new JBTabbedTerminalWidget(myProject, provider, new Predicate<Pair<TerminalWidget,
				String>>()
		{
			@Override
			public boolean apply(Pair<TerminalWidget, String> widget)
			{
				openSessionInDirectory(widget.getFirst(), widget.getSecond());
				return true;
			}
		}, contentDescriptor);

		createAndStartSession(widget, createTtyConnector(process));

		panel.add(widget.getComponent(), BorderLayout.CENTER);

		showConsole(defaultExecutor, contentDescriptor, widget.getComponent());

		processHandler.startNotify();
	}

	public void openSession(@Nonnull TerminalWidget terminal)
	{
		openSessionInDirectory(terminal, null);
	}

	public static void createAndStartSession(@Nonnull TerminalWidget terminal, @Nonnull TtyConnector ttyConnector)
	{
		TerminalSession session = terminal.createTerminalSession(ttyConnector);
		session.start();
	}

	protected abstract String getTerminalConnectionName(T process);

	protected abstract TtyConnector createTtyConnector(T process);

	protected AnAction createCloseAction(final Executor defaultExecutor, final RunContentDescriptor myDescriptor)
	{
		return new CloseAction(defaultExecutor, myDescriptor, myProject);
	}

	protected void showConsole(Executor defaultExecutor,
			@Nonnull RunContentDescriptor myDescriptor,
			final Component toFocus)
	{
		// Show in run toolwindow
		ExecutionManager.getInstance(myProject).getContentManager().showRunContent(defaultExecutor, myDescriptor);

		// Request focus
		final ToolWindow window = ToolWindowManager.getInstance(myProject).getToolWindow(defaultExecutor.getId());
		window.activate(new Runnable()
		{
			public void run()
			{
				IdeFocusManager.getInstance(myProject).requestFocus(toFocus, true);
			}
		});
	}

	@Nonnull
	protected Project getProject()
	{
		return myProject;
	}

	public abstract String runningTargetName();


	public void openSessionInDirectory(@Nonnull TerminalWidget terminalWidget, @Nullable String directory)
	{
		// Create Server process
		try
		{
			final T process = createProcess(directory);

			createAndStartSession(terminalWidget, createTtyConnector(process));
		}
		catch(Exception e)
		{
			Messages.showErrorDialog(e.getMessage(), "Can't Open " + runningTargetName());
		}
	}
}

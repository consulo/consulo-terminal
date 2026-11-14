// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.terminal.impl.action;

import consulo.application.AllIcons;
import consulo.application.concurrent.ApplicationConcurrency;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.terminal.localize.TerminalLocalize;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnSeparator;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.ui.ex.popup.ListPopup;
import consulo.ui.ex.toolWindow.ToolWindow;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.terminal.impl.TerminalView;
import org.jetbrains.plugins.terminal.impl.shellDetection.DetectedShellInfo;
import org.jetbrains.plugins.terminal.impl.shellDetection.DetectedShellsEnvironmentInfo;
import org.jetbrains.plugins.terminal.impl.shellDetection.ShellsDetectionResult;
import org.jetbrains.plugins.terminal.impl.shellDetection.TerminalShellsDetectionService;
import org.jetbrains.plugins.terminal.ui.OpenPredefinedTerminalActionProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TerminalNewPredefinedSessionAction extends DumbAwareAction
{
	private static final Logger LOG = Logger.getInstance(TerminalNewPredefinedSessionAction.class);

	private final Project myProject;
	private final TerminalView myTerminalView;
	private final ToolWindow myToolWindow;

	public TerminalNewPredefinedSessionAction(Project project, TerminalView terminalView, ToolWindow toolWindow)
	{
		super(TerminalLocalize.actionTerminalNewPredefinedSessionText(), LocalizeValue.empty(), AllIcons.General.ArrowDown);
		myProject = project;
		myTerminalView = terminalView;
		myToolWindow = toolWindow;
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{
		UIAccess uiAccess = UIAccess.current();
		Executor executor = myProject.getApplication().getInstance(ApplicationConcurrency.class).executor();

		CompletableFuture<List<AnAction>> customActions = CompletableFuture.supplyAsync(this::collectCustomActions, executor);
		myProject.getInstance(TerminalShellsDetectionService.class).detectShells().thenCombine(customActions, this::createGroup).whenCompleteAsync((group, error) ->
		{
			if(myProject.isDisposed())
			{
				return;
			}
			if(error != null)
			{
				LOG.error("Failed to detect shells", error);
				return;
			}

			ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(null, group, e.getDataContext(), false, null, -1);
			popup.showUnderneathOf(e);
		}, uiAccess);
	}

	private List<AnAction> collectCustomActions()
	{
		List<AnAction> actions = new ArrayList<>();
		myProject.getExtensionPoint(OpenPredefinedTerminalActionProvider.class).forEach(provider -> actions.addAll(provider.listOpenPredefinedTerminalActions()));
		return actions;
	}

	private DefaultActionGroup createGroup(ShellsDetectionResult result, List<AnAction> customActions)
	{
		List<DetectedShellsEnvironmentInfo> shellEnvironments = new ArrayList<>();
		List<DetectedShellsEnvironmentInfo> unknownEnvironments = new ArrayList<>();
		for(DetectedShellsEnvironmentInfo environment : result.environments())
		{
			(environment.shells().isEmpty() ? unknownEnvironments : shellEnvironments).add(environment);
		}

		DefaultActionGroup group = new DefaultActionGroup();
		if(shellEnvironments.size() == 1)
		{
			group.addAll(createOpenShellActions(shellEnvironments.get(0)));
		}
		else
		{
			for(DetectedShellsEnvironmentInfo environment : shellEnvironments)
			{
				group.add(AnSeparator.create(environment.environmentName()));
				group.addAll(createOpenShellActions(environment));
			}
		}

		group.addAll(customActions);

		if(!unknownEnvironments.isEmpty())
		{
			if(group.getChildrenCount() > 0)
			{
				group.add(AnSeparator.create());
			}
			for(DetectedShellsEnvironmentInfo environment : unknownEnvironments)
			{
				group.add(new OpenTerminalOnTargetAction(myTerminalView, myToolWindow, environment.target(), environment.environmentName()));
			}
		}
		return group;
	}

	private List<AnAction> createOpenShellActions(DetectedShellsEnvironmentInfo environment)
	{
		Map<String, List<DetectedShellInfo>> byName = new LinkedHashMap<>();
		for(DetectedShellInfo shell : environment.shells())
		{
			byName.computeIfAbsent(shell.name(), name -> new ArrayList<>()).add(shell);
		}

		List<AnAction> actions = new ArrayList<>();
		for(List<DetectedShellInfo> shellInfos : byName.values())
		{
			for(DetectedShellInfo info : shellInfos)
			{
				String presentableName = shellInfos.size() > 1 ? info.name() + " (" + info.path() + ")" : info.name();
				actions.add(new OpenShellAction(myTerminalView, myToolWindow, environment.target(), info, presentableName));
			}
		}
		return actions;
	}
}

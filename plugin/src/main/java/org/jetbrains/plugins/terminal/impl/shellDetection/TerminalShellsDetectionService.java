// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.terminal.impl.shellDetection;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.concurrent.ApplicationConcurrency;
import consulo.application.platform.PlatformTarget;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.terminal.localize.TerminalLocalize;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.plugins.terminal.impl.TerminalView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class TerminalShellsDetectionService
{
	private static final Logger LOG = Logger.getInstance(TerminalShellsDetectionService.class);

	private final TerminalView myTerminalView;
	private final Executor myExecutor;

	@Inject
	public TerminalShellsDetectionService(TerminalView terminalView, ApplicationConcurrency concurrency)
	{
		myTerminalView = terminalView;
		myExecutor = concurrency.executor();
	}

	/**
	 * Finds available shells that can be started in the embedded terminal - on the machine of the project first, then on every other target.
	 * A target which is not connected is not connected for that, its shells stay unknown.
	 */
	public CompletableFuture<ShellsDetectionResult> detectShells()
	{
		List<CompletableFuture<DetectedShellsEnvironmentInfo>> environments = new ArrayList<>();
		for(PlatformTarget target : myTerminalView.getTargets())
		{
			environments.add(CompletableFuture.supplyAsync(() -> detectEnvironment(target), myExecutor));
		}
		return CompletableFuture.allOf(environments.toArray(CompletableFuture[]::new))
			.thenApply(o -> new ShellsDetectionResult(environments.stream().map(CompletableFuture::join).toList()));
	}

	private DetectedShellsEnvironmentInfo detectEnvironment(PlatformTarget target)
	{
		LocalizeValue name = myTerminalView.isProjectTarget(target) ? TerminalLocalize.targetOfProject(target.getDisplayName()) : target.getDisplayName();
		Platform platform = target.getPlatform();
		if(platform == null)
		{
			return new DetectedShellsEnvironmentInfo(target, name, false, List.of());
		}
		List<DetectedShellInfo> shells;
		try
		{
			shells = TerminalShellsDetectionUtil.detectShells(platform, myExecutor);
		}
		catch(RuntimeException e)
		{
			LOG.error("Failed to detect shells of " + platform.getName(), e);
			shells = List.of();
		}
		return new DetectedShellsEnvironmentInfo(target, name, true, shells);
	}
}

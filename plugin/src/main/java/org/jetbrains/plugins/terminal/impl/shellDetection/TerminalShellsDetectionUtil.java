// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.terminal.impl.shellDetection;

import consulo.logging.Logger;
import consulo.platform.Platform;
import jakarta.annotation.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class TerminalShellsDetectionUtil
{
	private static final Logger LOG = Logger.getInstance(TerminalShellsDetectionUtil.class);

	private static final String POWERSHELL_5_NAME = "Windows PowerShell";
	private static final String POWERSHELL_7_NAME = "PowerShell";
	private static final String CMD_NAME = "Command Prompt";
	private static final String GIT_BASH_NAME = "Git Bash";
	private static final String CMDER_NAME = "Cmder";

	private static final String ENV_SYSTEM_ROOT = "SystemRoot";        // C:\Windows
	private static final String ENV_PROGRAM_FILES = "ProgramFiles";    // C:\Program Files
	private static final String ENV_LOCAL_APP_DATA = "LocalAppData";   // C:\Users\<Username>\AppData\Local
	private static final String ENV_CMDER_ROOT = "CMDER_ROOT";

	private static final List<String> UNIX_BINARIES_DIRECTORIES = List.of(
		"/bin",
		"/usr/bin",
		"/usr/local/bin",
		"/opt/homebrew/bin"
	);

	private static final List<String> UNIX_SHELL_NAMES = List.of("bash", "zsh", "fish", "pwsh");

	private TerminalShellsDetectionUtil()
	{
	}

	public static List<DetectedShellInfo> detectShells(Platform platform, Executor executor)
	{
		return platform.os().isWindows() ? detectWindowsShells(platform, executor) : detectUnixShells(platform, executor);
	}

	public static List<DetectedShellInfo> detectUnixShells(Platform platform, Executor executor)
	{
		// Iterate over all combinations of path+shell to find executables.
		List<CompletableFuture<DetectedShellInfo>> candidates = new ArrayList<>();
		for(String parentPath : UNIX_BINARIES_DIRECTORIES)
		{
			for(String shellName : UNIX_SHELL_NAMES)
			{
				String shellPath = parentPath + "/" + shellName;
				// Launch existence checks in parallel because checking sequentially can be slow for remote environments with big latency.
				candidates.add(CompletableFuture.supplyAsync(() -> isRegularFile(platform, shellPath) ? new DetectedShellInfo(shellName, shellPath) : null, executor));
			}
		}
		return joinAll(candidates);
	}

	public static List<DetectedShellInfo> detectWindowsShells(Platform platform, Executor executor)
	{
		Map<String, String> envVariables;
		try
		{
			envVariables = platform.os().environmentVariables();
		}
		catch(RuntimeException e)
		{
			LOG.error("Failed to fetch environment variables", e);
			return List.of();
		}

		// Launch existence checks in parallel because checking sequentially can be slow for remote environments with big latency.
		List<CompletableFuture<DetectedShellInfo>> candidates = new ArrayList<>();
		candidates.add(async(() -> detectWindowsPowerShell5(platform, envVariables), executor));
		candidates.add(async(() -> detectWindowsCmd(platform, envVariables), executor));
		candidates.add(async(() -> detectWindowsPowerShell7(platform), executor));
		candidates.add(async(() -> detectWindowsGitBash(platform, envVariables), executor));
		candidates.add(async(() -> detectWindowsCmder(platform, envVariables), executor));
		return joinAll(candidates);
	}

	@Nullable
	private static DetectedShellInfo detectWindowsPowerShell5(Platform platform, Map<String, String> envVariables)
	{
		String systemRoot = envVariables.get(ENV_SYSTEM_ROOT);
		if(systemRoot == null)
		{
			return null;
		}
		Path powershell = platform.findExecutableInPath("powershell.exe");
		if(powershell == null || !startsWithIgnoreCase(powershell, systemRoot + "\\System32\\WindowsPowerShell\\"))
		{
			return null;
		}
		return new DetectedShellInfo(POWERSHELL_5_NAME, powershell.toString());
	}

	@Nullable
	private static DetectedShellInfo detectWindowsCmd(Platform platform, Map<String, String> envVariables)
	{
		String systemRoot = envVariables.get(ENV_SYSTEM_ROOT);
		if(systemRoot == null)
		{
			return null;
		}
		Path cmd = platform.findExecutableInPath("cmd.exe");
		if(cmd == null || !startsWithIgnoreCase(cmd, systemRoot + "\\System32\\"))
		{
			return null;
		}
		return new DetectedShellInfo(CMD_NAME, cmd.toString());
	}

	@Nullable
	private static DetectedShellInfo detectWindowsPowerShell7(Platform platform)
	{
		Path pwsh = platform.findExecutableInPath("pwsh.exe");
		if(pwsh == null)
		{
			return null;
		}
		return new DetectedShellInfo(POWERSHELL_7_NAME, pwsh.toString());
	}

	@Nullable
	private static DetectedShellInfo detectWindowsGitBash(Platform platform, Map<String, String> envVariables)
	{
		String programFiles = envVariables.get(ENV_PROGRAM_FILES);
		String localAppData = envVariables.get(ENV_LOCAL_APP_DATA);
		if(programFiles == null || localAppData == null)
		{
			return null;
		}
		String gitBashGlobal = programFiles + "\\Git\\bin\\bash.exe";
		String gitBashLocal = localAppData + "\\Programs\\Git\\bin\\bash.exe";
		String gitBash;
		if(isRegularFile(platform, gitBashLocal))
		{
			gitBash = gitBashLocal;
		}
		else if(isRegularFile(platform, gitBashGlobal))
		{
			gitBash = gitBashGlobal;
		}
		else
		{
			return null;
		}
		return new DetectedShellInfo(GIT_BASH_NAME, gitBash);
	}

	@Nullable
	private static DetectedShellInfo detectWindowsCmder(Platform platform, Map<String, String> envVariables)
	{
		String cmderRoot = envVariables.get(ENV_CMDER_ROOT);
		String systemRoot = envVariables.get(ENV_SYSTEM_ROOT);
		if(cmderRoot == null || systemRoot == null)
		{
			return null;
		}
		Path cmd = platform.findExecutableInPath("cmd.exe");
		if(cmd == null || !startsWithIgnoreCase(cmd, systemRoot + "\\System32\\"))
		{
			return null;
		}
		Path cmderInitBat = platform.fs().getPath(cmderRoot).resolve("vendor\\init.bat");
		if(!Files.isRegularFile(cmderInitBat))
		{
			return null;
		}
		return new DetectedShellInfo(CMDER_NAME, cmd.toString(), List.of("/k", cmderInitBat.toString()));
	}

	private static CompletableFuture<DetectedShellInfo> async(Supplier<DetectedShellInfo> detector, Executor executor)
	{
		return CompletableFuture.supplyAsync(detector, executor);
	}

	private static List<DetectedShellInfo> joinAll(List<CompletableFuture<DetectedShellInfo>> candidates)
	{
		return candidates.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();
	}

	private static boolean isRegularFile(Platform platform, String path)
	{
		return Files.isRegularFile(platform.fs().getPath(path));
	}

	private static boolean startsWithIgnoreCase(Path path, String prefix)
	{
		return path.toString().regionMatches(true, 0, prefix, 0, prefix.length());
	}
}

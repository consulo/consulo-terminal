// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.terminal.impl.shellDetection;

import java.util.List;

/**
 * @param name    User-readable name of the shell, for example, zsh or Windows PowerShell
 * @param path    Absolute path of the shell executable, local to the machine of the environment
 * @param options Additional command line options that should be used to start this shell
 */
public record DetectedShellInfo(String name, String path, List<String> options)
{
	public DetectedShellInfo(String name, String path)
	{
		this(name, path, List.of());
	}
}

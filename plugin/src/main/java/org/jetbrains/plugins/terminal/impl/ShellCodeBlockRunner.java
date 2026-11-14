/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.terminal.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.runAnything.codeBlock.CodeBlockCommandRunner;
import consulo.execution.runAnything.codeBlock.CodeBlockRunner;
import consulo.language.Language;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.terminal.localize.TerminalLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;

import java.util.Locale;
import java.util.Set;

/**
 * Runs a block of shell code in a new tab of the terminal. A block names its shell by name only, a plugin for the language is
 * not needed.
 */
@ExtensionImpl
public class ShellCodeBlockRunner implements CodeBlockRunner
{
	private static final Set<String> SHELL_NAMES = Set.of("sh", "bash", "zsh", "fish", "shell", "console", "shell-session");

	/**
	 * A powershell block is passed as written: a line starting with {@code $} is a variable there, not a prompt
	 */
	private static final Set<String> POWERSHELL_NAMES = Set.of("powershell", "pwsh", "ps1");

	@Override
	public boolean isApplicable(String languageName, @Nullable Language language)
	{
		String name = languageName.toLowerCase(Locale.ROOT);
		return SHELL_NAMES.contains(name) || POWERSHELL_NAMES.contains(name);
	}

	@Override
	public LocalizeValue getTitle()
	{
		return TerminalLocalize.codeBlockRunnerTitle();
	}

	@Override
	@RequiredUIAccess
	public void run(Project project, String code, @Nullable VirtualFile workingDirectory)
	{
		TerminalView.getInstance(project).runInNewSession(CodeBlockCommandRunner.trimPrompt(code), workingDirectory == null ? null : workingDirectory.getPath());
	}
}

/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.util.SystemInfo;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.execution.ui.terminal.TerminalConsoleSettings;
import consulo.ide.ServiceManager;
import consulo.localize.LocalizeValue;
import consulo.platform.Platform;
import consulo.util.lang.StringUtil;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

/**
 * @author traff
 */
@Singleton
@State(name = "TerminalOptionsProvider", storages = @Storage(file = StoragePathMacros.APP_CONFIG + "/terminal.xml"))
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class TerminalOptionsProvider implements PersistentStateComponent<TerminalOptionsProvider.State>, TerminalConsoleSettings
{
	private State myState = new State();

	public static TerminalOptionsProvider getInstance()
	{
		return ServiceManager.getService(TerminalOptionsProvider.class);
	}

	@Override
	public State getState()
	{
		return myState;
	}

	@Override
	public void loadState(State state)
	{
		myState.myShellPath = state.myShellPath;
		myState.myCloseSessionOnLogout = state.myCloseSessionOnLogout;
		myState.myReportMouse = state.myReportMouse;
		myState.mySoundBell = state.mySoundBell;
		myState.myTabName = state.myTabName;
		myState.myCopyOnSelection = state.myCopyOnSelection;
		myState.myPasteOnMiddleMouseButton = state.myPasteOnMiddleMouseButton;
		myState.myOverrideIdeShortcuts = state.myOverrideIdeShortcuts;
	}

	@Override
	public boolean closeSessionOnLogout()
	{
		return myState.myCloseSessionOnLogout;
	}

	@Override
	public boolean isMouseReporting()
	{
		return myState.myReportMouse;
	}

	@Override
	public boolean isSoundBell()
	{
		return myState.mySoundBell;
	}

	@Nullable
	public String getTabName()
	{
		return myState.myTabName;
	}

	@Override
	public boolean isOverrideIdeShortcuts()
	{
		return myState.myOverrideIdeShortcuts;
	}

	public void setOverrideIdeShortcuts(boolean overrideIdeShortcuts)
	{
		myState.myOverrideIdeShortcuts = overrideIdeShortcuts;
	}

	public static class State
	{
		public String myShellPath;
		public String myTabName;
		public boolean myCloseSessionOnLogout = true;
		public boolean myReportMouse = true;
		public boolean mySoundBell = true;
		public boolean myCopyOnSelection = true;
		public boolean myPasteOnMiddleMouseButton = true;
		public boolean myOverrideIdeShortcuts = true;
	}

	@Override
	@Nonnull
	public String getTabNameOrDefault()
	{
		String tabName = getTabName();
		if(StringUtil.isEmptyOrSpaces(tabName))
		{
			return getDefaultTabName().get();
		}
		return tabName;
	}

	@Nonnull
	public LocalizeValue getDefaultTabName()
	{
		return LocalizeValue.localizeTODO("Local");
	}

	@Nullable
	public String getShellPath()
	{
		return myState.myShellPath;
	}

	@Nonnull
	public String getShellPathOrDefault()
	{
		String defaultShellPath = getDefaultShellPath();

		if(Objects.equals(getShellPath(), defaultShellPath))
		{
			// reset shell path if is default
			setShellPath(null);
		}

		String shellPath = getShellPath();
		if(StringUtil.isEmptyOrSpaces(shellPath))
		{
			return getDefaultShellPath();
		}
		return shellPath;
	}

	@Nonnull
	public String getDefaultShellPath()
	{
		String shell = Platform.current().os().getEnvironmentVariable("SHELL");

		if(shell != null && new File(shell).canExecute())
		{
			return shell;
		}

		if(SystemInfo.isUnix)
		{
			return "/bin/bash";
		}
		else
		{
			return "cmd.exe";
		}
	}

	public void setShellPath(String shellPath)
	{
		myState.myShellPath = shellPath;
	}

	public void setTabName(String tabName)
	{
		myState.myTabName = tabName;
	}

	public void setCloseSessionOnLogout(boolean closeSessionOnLogout)
	{
		myState.myCloseSessionOnLogout = closeSessionOnLogout;
	}

	public void setReportMouse(boolean reportMouse)
	{
		myState.myReportMouse = reportMouse;
	}

	public void setSoundBell(boolean soundBell)
	{
		myState.mySoundBell = soundBell;
	}

	@Override
	public boolean isCopyOnSelection()
	{
		return myState.myCopyOnSelection;
	}

	public void setCopyOnSelection(boolean copyOnSelection)
	{
		myState.myCopyOnSelection = copyOnSelection;
	}

	@Override
	public boolean isPasteOnMiddleMouseButton()
	{
		return myState.myPasteOnMiddleMouseButton;
	}

	public void setPasteOnMiddleMouseButton(boolean pasteOnMiddleMouseButton)
	{
		myState.myPasteOnMiddleMouseButton = pasteOnMiddleMouseButton;
	}
}


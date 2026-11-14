package org.jetbrains.plugins.terminal.impl;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.project.Project;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;

@Singleton
@State(name = "TerminalProjectOptionsProvider", storages = @Storage("terminal.xml"))
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class TerminalProjectOptionsProvider implements PersistentStateComponent<TerminalProjectOptionsProvider.State>
{
	private State myState = new State();

	public static TerminalProjectOptionsProvider getInstance(Project project)
	{
		return project.getInstance(TerminalProjectOptionsProvider.class);
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
	}

	@Nullable
	public String getShellPath()
	{
		return myState.myShellPath;
	}

	public void setShellPath(@Nullable String shellPath)
	{
		myState.myShellPath = shellPath;
	}

	public static class State
	{
		public String myShellPath;
	}
}

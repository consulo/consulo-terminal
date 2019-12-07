/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package org.jetbrains.plugins.terminal.vfs;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jediterm.terminal.TtyConnectorWaitFor;
import com.jediterm.terminal.ui.TerminalAction;
import com.jediterm.terminal.ui.TerminalActionProviderBase;
import com.jediterm.terminal.ui.settings.TabbedSettingsProvider;
import consulo.util.dataholder.UserDataHolderBase;
import kava.beans.PropertyChangeListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author traff
 */
public class TerminalSessionEditor extends UserDataHolderBase implements FileEditor
{
	private Project myProject;
	private final TerminalSessionVirtualFileImpl myFile;
	private final TtyConnectorWaitFor myWaitFor;

	public TerminalSessionEditor(Project project, @Nonnull TerminalSessionVirtualFileImpl terminalFile)
	{
		myProject = project;
		myFile = terminalFile;

		final TabbedSettingsProvider settings = myFile.getSettingsProvider();

		myFile.getTerminal().setNextProvider(new TerminalActionProviderBase()
		{
			@Override
			public List<TerminalAction> getActions()
			{
				return Lists.newArrayList(new TerminalAction("Close Session", settings.getCloseSessionKeyStrokes(),
						new Predicate<KeyEvent>()
				{
					@Override
					public boolean apply(KeyEvent input)
					{
						handleCloseSession();
						return true;
					}
				}).withMnemonicKey(KeyEvent.VK_S));
			}
		});

		myWaitFor = new TtyConnectorWaitFor(myFile.getTerminal().getTtyConnector(), Executors.newSingleThreadExecutor
				());

		myWaitFor.setTerminationCallback(new Predicate<Integer>()
		{
			@Override
			public boolean apply(Integer integer)
			{
				ApplicationManager.getApplication().invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						FileEditorManagerEx.getInstanceEx(myProject).closeFile(myFile);
					}
				});

				return true;
			}
		});
	}

	private void handleCloseSession()
	{
		myFile.getTerminal().close();
	}

	@Nonnull
	@Override
	public JComponent getComponent()
	{
		return myFile.getTerminal();
	}

	@Nullable
	@Override
	public JComponent getPreferredFocusedComponent()
	{
		return myFile.getTerminal();
	}

	@Nonnull
	@Override
	public String getName()
	{
		return myFile.getName();
	}

	@Nonnull
	@Override
	public FileEditorState getState(@Nonnull FileEditorStateLevel level)
	{
		return FileEditorState.INSTANCE;
	}

	@Override
	public void setState(@Nonnull FileEditorState state)
	{

	}

	@Override
	public boolean isModified()
	{
		return false;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void selectNotify()
	{

	}

	@Override
	public void deselectNotify()
	{

	}

	@Override
	public void addPropertyChangeListener(@Nonnull PropertyChangeListener listener)
	{

	}

	@Override
	public void removePropertyChangeListener(@Nonnull PropertyChangeListener listener)
	{

	}

	@Nullable
	@Override
	public BackgroundEditorHighlighter getBackgroundHighlighter()
	{
		return null;
	}

	@Nullable
	@Override
	public FileEditorLocation getCurrentLocation()
	{
		return null;
	}

	@Nullable
	@Override
	public StructureViewBuilder getStructureViewBuilder()
	{
		return null;
	}

	@Nullable
	@Override
	public VirtualFile getFile()
	{
		return myFile;
	}

	@Override
	public void dispose()
	{
		Boolean closingToReopen = myFile.getUserData(FileEditorManagerImpl.CLOSING_TO_REOPEN);
		myWaitFor.detach();
		if(closingToReopen == null || !closingToReopen)
		{
			myFile.getTerminal().close();
		}
	}
}

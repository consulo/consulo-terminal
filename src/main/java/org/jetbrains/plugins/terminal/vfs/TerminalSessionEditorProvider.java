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

import javax.annotation.Nonnull;

import org.jdom.Element;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import consulo.disposer.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author traff
 */
public class TerminalSessionEditorProvider implements FileEditorProvider, DumbAware
{
	@Override
	public boolean accept(@Nonnull Project project, @Nonnull VirtualFile file)
	{
		return file instanceof TerminalSessionVirtualFileImpl;
	}

	@Nonnull
	@Override
	public FileEditor createEditor(@Nonnull Project project, @Nonnull VirtualFile file)
	{
		return new TerminalSessionEditor(project, (TerminalSessionVirtualFileImpl) file);
	}

	@Override
	public void disposeEditor(@Nonnull FileEditor editor)
	{
		Disposer.dispose(editor);
	}

	@Nonnull
	@Override
	public FileEditorState readState(@Nonnull Element sourceElement,
			@Nonnull Project project,
			@Nonnull VirtualFile file)
	{
		return FileEditorState.INSTANCE;
	}

	@Override
	public void writeState(@Nonnull FileEditorState state, @Nonnull Project project, @Nonnull Element targetElement)
	{

	}

	@Nonnull
	@Override
	public String getEditorTypeId()
	{
		return "terminal-session-editor";
	}

	@Nonnull
	@Override
	public FileEditorPolicy getPolicy()
	{
		return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
	}
}

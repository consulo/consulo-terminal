// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.terminal.action;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.vfs.ArchiveFileSystem;
import org.jetbrains.plugins.terminal.TerminalView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An action that activates the terminal window for file, selected by user.
 */
public class RevealFileInTerminalAction extends DumbAwareAction
{
	public RevealFileInTerminalAction()
	{
		super("Open in Terminal", "Open current file location in terminal", null);
	}

	@RequiredUIAccess
	@Override
	public void update(@Nonnull AnActionEvent e)
	{
		e.getPresentation().setEnabledAndVisible(isAvailable(e));
	}

	private static boolean isAvailable(@Nonnull AnActionEvent e)
	{
		Project project = e.getProject();
		Editor editor = e.getData(CommonDataKeys.EDITOR);
		return project != null && getSelectedFile(e) != null &&
				(!ActionPlaces.isPopupPlace(e.getPlace()) || editor == null || !editor.getSelectionModel().hasSelection());
	}

	@Nullable
	private static VirtualFile getSelectedFile(@Nonnull AnActionEvent e)
	{
		return findLocalFile(e.getData(CommonDataKeys.VIRTUAL_FILE));
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{
		Project project = e.getProject();
		VirtualFile selectedFile = getSelectedFile(e);
		if(project == null || selectedFile == null)
		{
			return;
		}
		TerminalView.getInstance(project).openTerminalIn(selectedFile);
	}

	@Nullable
	public static VirtualFile findLocalFile(@Nullable VirtualFile file)
	{
		if(file == null || file.isInLocalFileSystem())
		{
			return file;
		}

		VirtualFileSystem fs = file.getFileSystem();
		if(fs instanceof ArchiveFileSystem && file.getParent() == null)
		{
			return ((ArchiveFileSystem) fs).getLocalVirtualFileFor(file);
		}

		return null;
	}
}

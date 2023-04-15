// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.terminal.impl.action;

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.codeEditor.Editor;
import consulo.language.editor.CommonDataKeys;
import consulo.project.Project;
import consulo.terminal.icon.TerminalIconGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionPlaces;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileSystem;
import consulo.virtualFileSystem.archive.ArchiveFileSystem;
import org.jetbrains.plugins.terminal.impl.TerminalView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An action that activates the terminal window for file, selected by user.
 */
@ActionImpl(id = "Terminal.OpenInTerminal", parents = @ActionParentRef(@ActionRef(id = "RevealGroup")))
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

	@Nullable
	@Override
	protected Image getTemplateIcon()
	{
		return TerminalIconGroup.openterminal_13x13();
	}

	private static boolean isAvailable(@Nonnull AnActionEvent e)
	{
		Project project = e.getData(Project.KEY);
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
		Project project = e.getData(Project.KEY);
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

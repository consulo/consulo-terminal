// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.terminal.ui;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.ui.ex.action.AnAction;

import java.util.List;

/**
 * Entries of the new predefined session popup of the terminal - shown after the detected shells.
 * An entry usually opens a tab with {@link org.jetbrains.plugins.terminal.TerminalToolWindowManager}.
 */
@ExtensionAPI(ComponentScope.PROJECT)
public interface OpenPredefinedTerminalActionProvider
{
	/**
	 * Called on a background thread, without a read lock.
	 */
	List<AnAction> listOpenPredefinedTerminalActions();
}

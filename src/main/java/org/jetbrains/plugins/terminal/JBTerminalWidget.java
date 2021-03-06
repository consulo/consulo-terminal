package org.jetbrains.plugins.terminal;

import com.intellij.ui.components.JBScrollBar;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.model.JediTerminal;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;

import javax.annotation.Nonnull;
import javax.swing.*;

public class JBTerminalWidget extends JediTermWidget implements Disposable
{
	public JBTerminalWidget(JBTerminalSystemSettingsProvider settingsProvider)
	{
		super(settingsProvider);

		JBTabbedTerminalWidget.convertActions(this, getActions());
	}

	@Override
	protected JBTerminalPanel createTerminalPanel(@Nonnull SettingsProvider settingsProvider,
												  @Nonnull StyleState styleState,
												  @Nonnull TerminalTextBuffer textBuffer)
	{
		JBTerminalPanel panel = new JBTerminalPanel((JBTerminalSystemSettingsProvider) settingsProvider, textBuffer, styleState);
		Disposer.register(this, panel);
		return panel;
	}

	@Override
	protected TerminalStarter createTerminalStarter(JediTerminal terminal, TtyConnector connector)
	{
		return new JBTerminalStarter(terminal, connector);
	}

	@Override
	protected JScrollBar createScrollBar()
	{
		return new JBScrollBar();
	}

	@Override
	public void dispose()
	{
	}
}

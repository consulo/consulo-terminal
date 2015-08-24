package org.jetbrains.plugins.terminal;

import javax.swing.SwingUtilities;

import com.intellij.ide.GeneralSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TerminalOutputStream;
import com.jediterm.terminal.TerminalStarter;
import com.jediterm.terminal.TtyChannel;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.emulator.JediEmulator;

/**
 * @author traff
 */
public class JBTerminalStarter extends TerminalStarter
{
	public JBTerminalStarter(Terminal terminal, TtyConnector ttyConnector)
	{
		super(terminal, ttyConnector);
	}

	@Override
	protected JediEmulator createEmulator(TtyChannel channel, TerminalOutputStream stream, Terminal terminal)
	{
		return new JediEmulator(channel, stream, terminal)
		{
			@Override
			protected void unsupported(char... sequenceChars)
			{
				if(sequenceChars[0] == 7)
				{ //ESC BEL
					refreshAfterExecution();
				}
				else
				{
					super.unsupported();
				}
			}
		};
	}

	public static void refreshAfterExecution()
	{
		if(GeneralSettings.getInstance().isSyncOnFrameActivation())
		{
			//we need to refresh local file system after a command has been executed in the terminal
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					ApplicationManager.getApplication().runWriteAction(new Runnable()
					{
						@Override
						public void run()
						{
							LocalFileSystem.getInstance().refresh(false);
						}
					});
				}
			});
		}
	}
}

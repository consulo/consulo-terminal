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

import com.jediterm.terminal.TtyConnector;
import consulo.execution.terminal.TerminalSession;
import consulo.logging.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * A session which types a command into its shell once it is connected - the shell reads it as soon as it is ready, so the
 * command does not wait for a prompt
 */
class TypingTerminalSession implements TerminalSession
{
	private static final Logger LOG = Logger.getInstance(TypingTerminalSession.class);

	private final TerminalSession myDelegate;
	private final String myCommand;

	TypingTerminalSession(TerminalSession delegate, String command)
	{
		myDelegate = delegate;
		myCommand = command;
	}

	@Override
	public String getConnectorName()
	{
		return myDelegate.getConnectorName();
	}

	@Override
	public TtyConnector connect() throws ExecutionException
	{
		TtyConnector connector = myDelegate.connect();
		try
		{
			connector.write(myCommand + "\n");
		}
		catch(IOException e)
		{
			LOG.error("Cannot type a command into the terminal", e);
		}
		return connector;
	}
}

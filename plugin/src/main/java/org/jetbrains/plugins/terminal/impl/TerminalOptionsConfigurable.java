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
package org.jetbrains.plugins.terminal.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ProjectConfigurable;
import consulo.configurable.SimpleConfigurableByProperties;
import consulo.configurable.StandardConfigurableIds;
import consulo.disposer.Disposable;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.FileChooserTextBoxBuilder;
import consulo.ui.util.FormBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author traff
 */
@ExtensionImpl
public class TerminalOptionsConfigurable extends SimpleConfigurableByProperties implements ProjectConfigurable
{
	private final Provider<TerminalOptionsProvider> myOptionsProvider;
	private final Project myProject;

	@Inject
	public TerminalOptionsConfigurable(Project project, Provider<TerminalOptionsProvider> terminalOptionsProvider)
	{
		myProject = project;
		myOptionsProvider = terminalOptionsProvider;
	}

	@RequiredUIAccess
	@Nonnull
	@Override
	protected Component createLayout(@Nonnull PropertyBuilder propertyBuilder, @Nonnull Disposable uiDisposable)
	{
		FormBuilder builder = FormBuilder.create();

		FileChooserTextBoxBuilder shellPathBuilder = FileChooserTextBoxBuilder.create(myProject);
		shellPathBuilder.dialogTitle(LocalizeValue.localizeTODO("Select Shell"));
		shellPathBuilder.fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());

		FileChooserTextBoxBuilder.Controller shellController = shellPathBuilder.build();

		TerminalOptionsProvider terminalOptionsProvider = myOptionsProvider.get();

		TextBox shellPathBox = shellController.getComponent();
		shellPathBox.withPlaceholder(terminalOptionsProvider.getDefaultShellPath());
		builder.addLabeled(LocalizeValue.localizeTODO("Shell path:"), shellPathBox);
		propertyBuilder.add(shellController::getValue, shellController::setValue, terminalOptionsProvider::getShellPath, terminalOptionsProvider::setShellPath);

		TextBox tabNameBox = TextBox.create();
		tabNameBox.withPlaceholder(terminalOptionsProvider.getDefaultTabName().getValue());
		builder.addLabeled(LocalizeValue.localizeTODO("Tab name:"), tabNameBox);
		propertyBuilder.add(tabNameBox, terminalOptionsProvider::getTabName, terminalOptionsProvider::setTabName);

		CheckBox closeSessionWhenItEnds = CheckBox.create(LocalizeValue.localizeTODO("Close session when it ends"));
		builder.addBottom(closeSessionWhenItEnds);
		propertyBuilder.add(closeSessionWhenItEnds, terminalOptionsProvider::closeSessionOnLogout, terminalOptionsProvider::setCloseSessionOnLogout);

		CheckBox audibleBell = CheckBox.create(LocalizeValue.localizeTODO("Audible bell"));
		builder.addBottom(audibleBell);
		propertyBuilder.add(audibleBell, terminalOptionsProvider::isSoundBell, terminalOptionsProvider::setSoundBell);

		CheckBox mouseReporting = CheckBox.create(LocalizeValue.localizeTODO("Mouse reporting"));
		builder.addBottom(mouseReporting);
		propertyBuilder.add(mouseReporting, terminalOptionsProvider::isMouseReporting, terminalOptionsProvider::setReportMouse);

		CheckBox copyToClipboardOnSelection = CheckBox.create(LocalizeValue.localizeTODO("Copy to clipboard on selection"));
		builder.addBottom(copyToClipboardOnSelection);
		propertyBuilder.add(copyToClipboardOnSelection, terminalOptionsProvider::isCopyOnSelection, terminalOptionsProvider::setCopyOnSelection);

		CheckBox pasteOnMiddleClick = CheckBox.create(LocalizeValue.localizeTODO("Paste on middle mouse button click"));
		builder.addBottom(pasteOnMiddleClick);
		propertyBuilder.add(pasteOnMiddleClick, terminalOptionsProvider::isPasteOnMiddleMouseButton, terminalOptionsProvider::setPasteOnMiddleMouseButton);

		CheckBox overrideIdeShortcuts = CheckBox.create(LocalizeValue.localizeTODO("Override IDE shortcuts"));
		builder.addBottom(overrideIdeShortcuts);
		propertyBuilder.add(overrideIdeShortcuts, terminalOptionsProvider::isOverrideIdeShortcuts, terminalOptionsProvider::setOverrideIdeShortcuts);

		return builder.build();
	}

	@Override
	public String getHelpTopic()
	{
		return "plugins/org.jetbrains.plugins.terminal/settings/";
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "terminal";
	}

	@Nullable
	@Override
	public String getParentId()
	{
		return StandardConfigurableIds.EXECUTION_GROUP;
	}

	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "Terminal";
	}
}

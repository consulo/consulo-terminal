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
package org.jetbrains.plugins.terminal;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import consulo.ide.ui.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.options.SimpleConfigurableByProperties;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;

import javax.annotation.Nonnull;

/**
 * @author traff
 */
public class TerminalOptionsConfigurable extends SimpleConfigurableByProperties implements Configurable
{
	private final TerminalOptionsProvider myOptionsProvider;
	private final Project myProject;

	public TerminalOptionsConfigurable(Project project, TerminalOptionsProvider terminalOptionsProvider)
	{
		myProject = project;
		myOptionsProvider = terminalOptionsProvider;
	}

	@RequiredUIAccess
	@Nonnull
	@Override
	protected Component createLayout(PropertyBuilder propertyBuilder)
	{
		FormBuilder builder = FormBuilder.create();

		FileChooserTextBoxBuilder shellPathBuilder = FileChooserTextBoxBuilder.create(myProject);
		shellPathBuilder.dialogTitle("Select Shell");
		shellPathBuilder.fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());

		FileChooserTextBoxBuilder.Controller shellController = shellPathBuilder.build();

		TextBox shellPathBox = shellController.getComponent();
		shellPathBox.withPlaceholder(myOptionsProvider.getDefaultShellPath());
		builder.addLabeled(LocalizeValue.localizeTODO("Shell path:"), shellPathBox);
		propertyBuilder.add(shellController::getValue, shellController::setValue, myOptionsProvider::getShellPath, myOptionsProvider::setShellPath);

		TextBox tabNameBox = TextBox.create();
		tabNameBox.withPlaceholder(myOptionsProvider.getDefaultTabName().getValue());
		builder.addLabeled(LocalizeValue.localizeTODO("Tab name:"), tabNameBox);
		propertyBuilder.add(tabNameBox, myOptionsProvider::getTabName, myOptionsProvider::setTabName);

		CheckBox closeSessionWhenItEnds = CheckBox.create(LocalizeValue.localizeTODO("Close session when it ends"));
		builder.addBottom(closeSessionWhenItEnds);
		propertyBuilder.add(closeSessionWhenItEnds, myOptionsProvider::closeSessionOnLogout, myOptionsProvider::setCloseSessionOnLogout);

		CheckBox audibleBell = CheckBox.create(LocalizeValue.localizeTODO("Audible bell"));
		builder.addBottom(audibleBell);
		propertyBuilder.add(audibleBell, myOptionsProvider::isSoundBell, myOptionsProvider::setSoundBell);

		CheckBox mouseReporting = CheckBox.create(LocalizeValue.localizeTODO("Mouse reporting"));
		builder.addBottom(mouseReporting);
		propertyBuilder.add(mouseReporting, myOptionsProvider::isMouseReporting, myOptionsProvider::setReportMouse);

		CheckBox copyToClipboardOnSelection = CheckBox.create(LocalizeValue.localizeTODO("Copy to clipboard on selection"));
		builder.addBottom(copyToClipboardOnSelection);
		propertyBuilder.add(copyToClipboardOnSelection, myOptionsProvider::isCopyOnSelection, myOptionsProvider::setCopyOnSelection);

		CheckBox pasteOnMiddleClick = CheckBox.create(LocalizeValue.localizeTODO("Paste on middle mouse button click"));
		builder.addBottom(pasteOnMiddleClick);
		propertyBuilder.add(pasteOnMiddleClick, myOptionsProvider::isPasteOnMiddleMouseButton, myOptionsProvider::setPasteOnMiddleMouseButton);

		CheckBox overrideIdeShortcuts = CheckBox.create(LocalizeValue.localizeTODO("Override IDE shortcuts"));
		builder.addBottom(overrideIdeShortcuts);
		propertyBuilder.add(overrideIdeShortcuts, myOptionsProvider::isOverrideIdeShortcuts, myOptionsProvider::setOverrideIdeShortcuts);

		return builder.build();
	}

	@Override
	public String getHelpTopic()
	{
		return "plugins/org.jetbrains.plugins.terminal/settings/";
	}
}

/**
 * @author VISTALL
 * @since 07/04/2023
 */
module org.jetbrains.plugins.terminal {
    requires consulo.application.api;
    requires consulo.code.editor.api;
    requires consulo.component.api;
    requires consulo.configurable.api;
    requires consulo.disposer.api;
    requires consulo.execution.api;
    requires consulo.file.chooser.api;
    requires consulo.language.editor.api;
    requires consulo.localize.api;
    requires consulo.module.content.api;
    requires consulo.platform.api;
    requires consulo.project.api;
    requires consulo.project.ui.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.ui.ex.awt.api;
    requires consulo.util.lang;
    requires consulo.util.xml.serializer;
    requires consulo.virtual.file.system.api;

    opens org.jetbrains.plugins.terminal.impl to consulo.util.xml.serializer;
}
/**
 * @author VISTALL
 * @since 07/04/2023
 */
module org.jetbrains.plugins.terminal {
    requires consulo.ide.api;

    opens org.jetbrains.plugins.terminal.impl to consulo.util.xml.serializer;
}
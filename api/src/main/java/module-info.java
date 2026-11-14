module org.jetbrains.plugins.terminal.api {
    requires transitive consulo.application.api;
    requires transitive consulo.ui.api;
    requires transitive consulo.ui.ex.api;

    exports org.jetbrains.plugins.terminal;
    exports org.jetbrains.plugins.terminal.ui;
}

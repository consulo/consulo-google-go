/**
 * The Go plugin: requires every module of it, so dependent plugins only require {@code consulo.google.go}.
 *
 * @author VISTALL
 * @since 06-Aug-22
 */
module consulo.google.go {
    requires transitive consulo.go.toolchain;
    requires transitive consulo.go.modules;
    requires transitive consulo.go.language.impl;
    requires transitive consulo.go.language.editor.impl;
    requires transitive consulo.go.execution;
    requires transitive consulo.go.asm;
}

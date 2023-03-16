package com.pannous.jini.listeners;

import org.jetbrains.annotations.NotNull;

public class CompilationFinished extends jetbrains.buildServer.messages.serviceMessages.CompilationFinished {
    public CompilationFinished(@NotNull String compilerName) {
        super(compilerName);
    }
}

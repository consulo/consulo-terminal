// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.terminal.impl.shellDetection;

import consulo.application.platform.PlatformTarget;
import consulo.localize.LocalizeValue;

import java.util.List;

/**
 * @param target          The machine where shells can be started
 * @param environmentName User-readable name of the environment where shell can be started
 * @param connected       False when the target is not connected - its shells are unknown until it is
 */
public record DetectedShellsEnvironmentInfo(PlatformTarget target, LocalizeValue environmentName, boolean connected, List<DetectedShellInfo> shells)
{
}

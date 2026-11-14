// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.terminal.impl.shellDetection;

import java.util.List;

/**
 * @param environments Environments where shells can be started
 */
public record ShellsDetectionResult(List<DetectedShellsEnvironmentInfo> environments)
{
}

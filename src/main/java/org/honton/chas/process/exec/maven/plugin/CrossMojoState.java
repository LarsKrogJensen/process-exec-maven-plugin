package org.honton.chas.process.exec.maven.plugin;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class CrossMojoState {

    private static final String CONTEXT_KEY = CrossMojoState.class.getCanonicalName();

    private final BlockingDeque<ExecProcess> processes = new LinkedBlockingDeque<>();

    public static CrossMojoState get(Map pluginContext) {
        synchronized (pluginContext) {
            CrossMojoState mojoState = (CrossMojoState) pluginContext.get(CONTEXT_KEY);
            if (mojoState == null) {
                mojoState = new CrossMojoState();
                pluginContext.put(CONTEXT_KEY, mojoState);
            }
            return mojoState;
        }
    }

    private CrossMojoState() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                stopProcesses();
            }
        });
    }

    public void add(ExecProcess process) {
        processes.addLast(process);
    }

    public void stopProcesses() {
        for (; ; ) {
            final ExecProcess process = processes.pollLast();
            if (process == null) {
                break;
            }
            process.destroy();
        }
    }
}

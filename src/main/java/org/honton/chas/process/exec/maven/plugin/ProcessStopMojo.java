package org.honton.chas.process.exec.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "stop-all", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ProcessStopMojo extends AbstractProcessMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (waitForInterrupt) {
            getLog().info("Waiting for interrupt before stopping all processes ...");
            sleepUntilInterrupted();
        }
        CrossMojoState.get(getPluginContext()).stopProcesses();
    }
}

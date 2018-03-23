package org.honton.chas.process.exec.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Mojo(name = "start", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class ProcessStartMojo extends AbstractProcessMojo {

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skipping " + name);
            return;
        }
        if (getLog().isDebugEnabled()) {
            for (String arg : arguments) {
                getLog().debug("arg: " + arg);
            }
            if (environment != null) {
                for (Map.Entry<String, String> entry : environment.entrySet()) {
                    getLog().debug("env: " + entry.getKey() + "=" + entry.getValue());
                }
            }
        }
        try {
            startProcess();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        if (waitForInterrupt) {
            sleepUntilInterrupted();
        }
    }

    private void startProcess() throws IOException {
        final ExecProcess exec = new ExecProcess(name, getLog());
        if (null != processLogFile) {
            File plf = new File(processLogFile);
            ensureDirectory(plf.getParentFile());
            exec.setProcessLogFile(plf);
        }
        getLog().info("Starting process: " + exec.getName());

        exec.execute(processWorkingDirectory(), environment, arguments);
        CrossMojoState.get(getPluginContext()).add(exec);
        ProcessHealthCondition processHealthCondition = new ProcessHealthCondition(getLog(), healthCheckUrl, waitAfterLaunch);
        processHealthCondition.awaitHealthy();
        getLog().info("Started process: " + exec.getName());
    }

    private File processWorkingDirectory() throws IOException {
        String buildDir = project.getBuild().getDirectory();
        if (workingDir == null) {
            return ensureDirectory(new File(buildDir));
        }

        // try to check if buildDir is absolute
        // https://github.com/bazaarvoice/maven-process-plugin/issues/11
        File pwd = new File(workingDir);
        if (!pwd.isAbsolute()) {
            pwd = new File(buildDir, workingDir);
        }
        return ensureDirectory(pwd);
    }
}

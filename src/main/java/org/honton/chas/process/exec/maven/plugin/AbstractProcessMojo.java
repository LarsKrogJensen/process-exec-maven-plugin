package org.honton.chas.process.exec.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public abstract class AbstractProcessMojo extends AbstractMojo {
    @Component
    protected MavenProject project;

    @Parameter(property = "exec.arguments")
    protected List<String> arguments;

    @Parameter(property = "exec.environment")
    protected Map<String, String> environment;

    @Parameter(property = "exec.workingDir")
    protected String workingDir;

    @Parameter(property = "exec.name")
    protected String name;

    @Parameter(defaultValue = "${exec.healthCheckUrl}")
    protected HealthCheckUrl healthCheckUrl;

    public void setHealthCheckUrl(String url) throws MalformedURLException {
        this.healthCheckUrl = new HealthCheckUrl();
        this.healthCheckUrl.setUrl(url);
    }

    @Parameter(property = "exec.healthCheckValidateSsl", defaultValue = "true")
    protected boolean healthCheckValidateSsl;

    @Parameter(property = "exec.waitAfterLaunch", required = false, defaultValue = "30")
    protected int waitAfterLaunch;

    @Parameter(defaultValue = "false", property = "exec.waitForInterrupt")
    protected boolean waitForInterrupt;

    @Parameter(required = false, property = "exec.processLogFile")
    protected String processLogFile;

    @Parameter(defaultValue = "false", property = "exec.skip")
    protected boolean skip;

    static File ensureDirectory(File dir) throws IOException {
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new IOException("couldn't create directories: " + dir);
        }
        return dir;
    }

    protected void sleepUntilInterrupted() throws MojoExecutionException {
        getLog().info("Hit ENTER on the console to continue the build.");

        try {
            for (; ; ) {
                int ch = System.in.read();
                if (ch == -1 || ch == '\n') {
                    break;
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}

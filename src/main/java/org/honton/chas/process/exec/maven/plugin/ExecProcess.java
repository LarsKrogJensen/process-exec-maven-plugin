package org.honton.chas.process.exec.maven.plugin;

import org.apache.maven.plugin.logging.Log;
import org.honton.chas.process.exec.maven.plugin.StdoutRedirector.LineWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExecProcess {
    private Process process;
    private File processLogFile;
    private final String name;
    private final Log log;

    public ExecProcess(String name, Log log) {
        this.name = name;
        this.log = log;
    }

    public void setProcessLogFile(File emoLogFile) {
        this.processLogFile = emoLogFile;
    }

    public String getName() {
        return name;
    }

    public void execute(File workingDirectory, Map<String, String> environment, List<String> args)
            throws IOException {
        final ProcessBuilder pb = new ProcessBuilder();
        log.debug("Using working directory for this process: " + workingDirectory);
        pb.directory(workingDirectory);
        if (environment != null) {
            pb.environment().putAll(environment);
        }

        removeNullElements(args);

        pb.command(args);
        if (processLogFile != null) {
            redirectToLogFile(pb);
        }
        process = pb.start();
        if (processLogFile == null) {
            redirectStream();
        }
    }

    private static void removeNullElements(List<String> args) {
        for (ListIterator<String> it = args.listIterator(); it.hasNext(); ) {
            if (it.next() == null) {
                it.remove();
            }
        }
    }

    private void redirectToLogFile(ProcessBuilder pb) throws IOException {
        AbstractProcessMojo.ensureDirectory(processLogFile.getParentFile());
        log.debug("redirecting out/err to " + processLogFile);
        pb.redirectErrorStream(true).redirectOutput(processLogFile);
    }

    private void redirectStream() throws IOException {
        new StdoutRedirector(name, process.getInputStream(), new LineWriter() {
            @Override
            public void writeLine(String line) {
                log.info(line);
            }
        });
        new StdoutRedirector(name, process.getErrorStream(), new LineWriter() {
            @Override
            public void writeLine(String line) {
                log.error(line);
            }
        });
    }

    public void destroy() {
        log.info("Stopping process: " + name);
        process.destroy();
        waitForExit();
    }

    private int waitForExit() {
        for (int t = 0; t < 30; ++t) {
            try {
                int rc = process.exitValue();
                log.info("Stopped process: " + name + " exit code " + rc);
                return rc;
            } catch (IllegalThreadStateException e) {
                log.debug("process " + name + " not exited after " + t + " seconds");
                waitSeconds(1);
            }
        }
        log.error("Process " + name + " not stopped after 30 seconds");
        return -1;

    }

    private static void waitSeconds(int x) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(x));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

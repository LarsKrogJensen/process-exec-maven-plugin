package org.honton.chas.process.exec.maven.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StdoutRedirector extends Thread {

    interface LineWriter {
        void writeLine(String line);
    }

    private final String streamName;
    private final BufferedReader in;
    private final LineWriter lineWriter;

    StdoutRedirector(String streamName, InputStream in, LineWriter lineWriter) {
        this.streamName = streamName;
        this.in = new BufferedReader(new InputStreamReader(in));
        this.lineWriter = lineWriter;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                String line = in.readLine();
                if (line == null) {
                    return;
                }
                lineWriter.writeLine('[' + streamName + "] " + line);
            }
        } catch (IOException ignore) {
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }
}

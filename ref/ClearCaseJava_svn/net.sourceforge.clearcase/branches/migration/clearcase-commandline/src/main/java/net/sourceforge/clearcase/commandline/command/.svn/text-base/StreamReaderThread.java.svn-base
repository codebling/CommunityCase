package net.sourceforge.clearcase.commandline.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StreamReaderThread extends Thread {
	private BufferedReader br = null;
	private String line = null;
	private final List<String> lines = new ArrayList<String>();

	public StreamReaderThread() {
	}

	public synchronized String[] getOutput() {
		return lines.toArray(new String[lines.size()]);
	}

	@Override
	public synchronized void run() {
		try {
			if (br == null) {
				// waits only if the input stream has not been initialized
				wait();
			}
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (final IOException ex) {
		} catch (final InterruptedException ex) {
		}
	}

	public synchronized void setInputStream(final InputStream is) {
		br = new BufferedReader(new InputStreamReader(is));
		notify();
	}
}

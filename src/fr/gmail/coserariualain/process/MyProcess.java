package fr.gmail.coserariualain.process;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MyProcess {
	private final String processPath;
	private final String logFileName;

	private OutputStream inputStream;
	
	public MyProcess(String processPath) {
		this.processPath = processPath;
		logFileName = processPath + "_log.txt";
	}
	
	public void startProcess() throws IOException {
		File logFile = new File(logFileName);
			
		ProcessBuilder builder = new ProcessBuilder(processPath);
		builder.redirectOutput(logFile);
		
		Process proc = builder.start();
		inputStream = proc.getOutputStream();
	}
	
	public void sendInput(String input) throws IOException {
		Writer w = new OutputStreamWriter(inputStream);
		w.write(input);
	}
}

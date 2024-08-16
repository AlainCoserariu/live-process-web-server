package fr.gmail.coserariualain.process;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MyProcess {
	private final String processCommand;
	private final String logFileName;

	private boolean isAlive = false;
	private OutputStream inputStream;
	
	public MyProcess(String processCommand) {
		this.processCommand = processCommand;
		logFileName = "minecraftServer/log.txt";
	}
	
	/**
	 * Check if the process is already launched, if not, start the process
	 * 
	 * @return type int
	 * 		   1 : The process is already started, nothing is done
	 * 	       0 : The process has correctly started
	 * @throws IOException
	 */
	public int startProcess() throws IOException {
		if (isAlive) {
			return 1;
		}
		File logFile = new File(logFileName);
					
		ProcessBuilder builder = new ProcessBuilder(processCommand);
		builder.redirectOutput(logFile);
		
		Process proc = builder.start();
		inputStream = proc.getOutputStream();
		isAlive = true;
		
		return 0;
	}
	
	public void sendInput(String input) throws IOException {
		Writer w = new OutputStreamWriter(inputStream);
		w.write(input);
		w.close();
	}
	
	public boolean getIsAlive() {
		return isAlive;
	}
}

package fr.gmail.coserariualain.process;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

public class MyProcess {
	private final String processCommand;
	private final String logFileName;

	private boolean isAlive = false;
	private OutputStream streamToProcess;
	
	private final List<String> commandQueue = new LinkedList<String>();
	
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
		streamToProcess = proc.getOutputStream();
		isAlive = true;
		
		return 0;
	}
	
	private void sendInput(String input) throws IOException {
		Writer w = new OutputStreamWriter(streamToProcess);
		w.write(input);
		w.flush();
	}
	
	public synchronized void addCommandToQueue(String command) throws IOException {
		commandQueue.add(command);
		sendAllCommand();
	}
	
	private void sendAllCommand() throws IOException {
		for (String command : commandQueue) {
			sendInput(command);
		}
		
		commandQueue.clear();
	}
	
	public boolean getIsAlive() {
		return isAlive;
	}
}

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

	private OutputStream streamToProcess;
	private Process proc = null;
	
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
	public int startProcess() {
		if (proc != null && proc.isAlive()) {
			return 1;
		}
		File logFile = new File(logFileName);
					
		ProcessBuilder builder = new ProcessBuilder(processCommand);
		builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
		builder.redirectError(ProcessBuilder.Redirect.appendTo(logFile));
		
		try {
			proc = builder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		streamToProcess = proc.getOutputStream();
		
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
		if (proc == null) {
			return false;
		}
		return proc.isAlive();
	}
}

package life.genny.qwandautils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class SystemUtils {
	public static void setupKeycloak()
	{
	try {
		ProcessBuilder builder = new ProcessBuilder();
		    builder.command("sh", "/opt/jboss/wildfly/bin/jboss-cli.sh", "-c",  "--file=/opt/jboss/wildfly/bin/adapter-elytron-install.cli");

		builder.directory(new File(System.getProperty("user.home")));
		Process process = builder.start();
		StreamGobbler streamGobbler = 
		  new StreamGobbler(process.getInputStream(), System.out::println);
		Executors.newSingleThreadExecutor().submit(streamGobbler);
		int exitCode = process.waitFor();
	} catch (IOException | InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
//	assert exitCode == 0;
	}
	
	private static class StreamGobbler implements Runnable {
	    private InputStream inputStream;
	    private Consumer<String> consumer;
	 
	    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
	        this.inputStream = inputStream;
	        this.consumer = consumer;
	    }
	 
	    @Override
	    public void run() {
	        new BufferedReader(new InputStreamReader(inputStream)).lines()
	          .forEach(consumer);
	    }
	}
}

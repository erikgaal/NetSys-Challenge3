package protocol;

import java.io.*;

import client.*;

/**
 * Entry point of the program. Starts the client and links the used MAC
 * protocol.
 * 
 * @author Jaco ter Braak, Twente University
 * @version 05-12-2013
 */
public class Program implements Runnable {

	// Change to your group number (use a student number)
	private static int groupId = 1525387;

	// Change to your group password (doesn't matter what it is,
	// as long as everyone in the group uses the same string)
	private static String password = "password8";

	// Change to your protocol implementation
	private static IMACProtocol protocol = new UltimateOrdering();

	// Challenge server address
	private static String serverAddress = "netsys.student.utwente.nl";

	// Challenge server port
	private static int serverPort = 8001;

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * DO NOT EDIT BELOW THIS LINE
	 */
	public static void main(String[] args) {

		int group = groupId;

		if (args.length > 0) {
			group = Integer.parseInt(args[0]);
		}
		
		MACChallengeClient client = null;
		try {
			System.out.print("Starting client... ");
			
			// Create the client
			client = new MACChallengeClient(serverAddress, serverPort, group,
					password);

			System.out.println("Done.");
			
			// Set protocol
			client.setListener(protocol);

			System.out.println("Press Enter to start the simulation...");
			System.out
					.println("(Simulation will also be started automatically if another client in the group issues the start command)");
		
			boolean startCommand = false;
			InputStream inputStream = new BufferedInputStream(System.in);
			while (!client.isSimulationStarted() && !client.isSimulationFinished()) {
				if (!startCommand && inputStream.available() > 0) {
					client.requestStart();
					startCommand = true;
				}
				Thread.sleep(10);
			}

			System.out.println("Simulation started!");

			// Wait until the simulation is finished
			while (!client.isSimulationFinished()) {
				Thread.sleep(10);
			}

			System.out
					.println("Simulation finished! Check your performance on the server web interface.");

		} catch (IOException e) {
			System.out.print("Could not start the client, because: ");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Operation interrupted.");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.print("Unexpected Exception: ");
			e.printStackTrace();
		} finally {
			if (client != null) {
				System.out.print("Shutting down client... ");
				client.stop();
				System.out.println("Done.");
			}
			System.out.println("Terminating program.");
		}
	}

	private Integer groupId2;
	
	public Program(int groupId) {
		this.groupId2 = groupId;
	}

	@Override
	public void run() {
		start(groupId2);
	}
	
	public static void start(Integer id) {
		main(new String[] { id.toString() });
	}
}

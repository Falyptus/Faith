package link;

import java.io.IOException;
import java.net.ServerSocket;

import common.Config;
import common.Log;
import common.Main;

public class LinkServer implements Runnable {

	private ServerSocket serverSocket;
	private Thread thread;

	public LinkServer()
	{
		try
		{
			serverSocket = new ServerSocket(Config.LINK_PORT);
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		} catch (final IOException e) {
			e.printStackTrace();
			Log.addToErrorLog("LoginServer Error: " + e.getMessage());
			Main.closeServers();
		}
	}

	public void run()
	{
		while (Main.isRunning)
		{
			try
			{
				new LinkThread(serverSocket.accept());
			} catch (final IOException e) {
				try {
					if (!serverSocket.isClosed()) 
						serverSocket.close();
				} catch (final IOException e1) {}
				Log.addToErrorLog("LoginServer Error: " + e.getMessage());
			}
		}
	}

	public void kickAll()
	{
		try {
			serverSocket.close();
		} catch (final Exception e) {
			Log.addToErrorLog("LoginServer Error: " + e.getMessage());
		}
	}
}

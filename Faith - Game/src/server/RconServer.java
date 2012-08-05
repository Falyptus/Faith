package server;

import java.io.IOException;
import java.net.ServerSocket;

import action.ActionThread;

import common.Config;
import common.Main;
import common.console.Log;

public class RconServer implements Runnable {

	private ServerSocket serverSocket;
	private Thread thread;
	
	public RconServer()
	{
		try {
			serverSocket = new ServerSocket(Config.CONFIG_RCON_PORT);
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		} catch (final IOException e) {
			addToLog("IOException: "+e.getMessage());
			e.printStackTrace();
			Main.closeServers();
		}
	}
	
	public void run()
	{	
		while(Main.isRunning)//bloque sur _SS.accept()
		{
			try
			{
				new ActionThread(serverSocket.accept());
			}catch(final IOException e)
			{
				try
				{
					addToLog("Fermeture du serveur d'Administation");	
					if(!serverSocket.isClosed())serverSocket.close();
				}
				catch(final IOException e1){}
			}
		}
	}
	
	public void kickAll()
	{
		try {
			serverSocket.close();
		} catch (final IOException e) {}
	}
	public synchronized static void addToLog(final String str)
	{
		Log.addToRconLog(str);
	}
	
	public synchronized static void addToSockLog(final String str)
	{
		if(Config.CONFIG_DEBUG)
		{
			System.out.println (str);
		}
	}

	public Thread getThread()
	{
		return thread;
	}
}

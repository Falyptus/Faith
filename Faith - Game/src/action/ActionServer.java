package action;

import java.io.IOException;
import java.net.ServerSocket;

import common.Config;
import common.Main;
import common.console.Log;

public class ActionServer implements Runnable{

	private ServerSocket _SS;
	private Thread _t;

	public ActionServer()
	{
		try {
			_SS = new ServerSocket(Config.CONFIG_ACTION_PORT);
			_t = new Thread(this);
			_t.setDaemon(true);
			_t.start();
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
				new ActionThread(_SS.accept());
			}catch(final IOException e)
			{
				try
				{
					addToLog("Fermeture du serveur d'action");	
					if(!_SS.isClosed())_SS.close();
				}
				catch(final IOException e1){}
			}
		}
	}
	
	public void kickAll()
	{
		try {
			_SS.close();
		} catch (final IOException e) {}
	}
	public synchronized static void addToLog(final String str)
	{
		Log.addToShopLog(str);
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
		return _t;
	}
}

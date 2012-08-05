package login;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import common.Config;
import common.Log;
import common.Main;

public class LoginServer implements Runnable {

	private ServerSocket serverSocket;
	private Thread thread;
	private ExecutorService threadPool = null;
	private List<LoginThread> clients = new ArrayList<LoginThread>();
	
	public LoginServer() 
	{
		threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	public void run()
	{
		openServerSocket();
		while(Main.isRunning)
		{
			try
			{
				LoginThread client = new LoginThread(serverSocket.accept());
				clients.add(client);
				threadPool.execute(client);
			} catch (IOException e) {
				try {
					if (!serverSocket.isClosed())
						serverSocket.close();
				} catch (IOException e1) {}
				Log.addToErrorLog("LoginServer Error: " + e.getMessage());
				Main.closeServers();
				threadPool.shutdown();
			}
		}
	}
	
	public void openServerSocket() {
		try {
			serverSocket = new ServerSocket(Config.LOGIN_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void kickAll()
	{
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.addToErrorLog("LoginServer Error: " + e.getMessage());
		}
		List<LoginThread> clients = new ArrayList<LoginThread>();
		clients.addAll(this.clients);
		for(LoginThread client : clients) {
			client.closeSocket();
		}
	}
	
	public void removeClient(LoginThread loginThread) {
		clients.remove(loginThread);
	}
}

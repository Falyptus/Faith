package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import objects.account.Account;
import server.client.GameThread;

import common.Config;
import common.Constants;
import common.Main;
import common.console.Log;
import common.utils.Utils;

public class GameServer implements Runnable{

	private final int serverPort;
	private ServerSocket serverSocket;
	private Thread thread;
	private ExecutorService threadPool = null;
	private boolean isStopped = false;
	private long startTime;
	private int maxPlayer = 0;
	private final ArrayList<GameThread> clients;
	private ArrayList<Account> waitings;
	
	public GameServer() {
		this.serverPort = Config.CONFIG_GAME_PORT;
		this.threadPool = Executors.newFixedThreadPool(Config.CONFIG_PLAYER_LIMIT);//Runtime.getRuntime().availableProcessors());
		this.clients = new ArrayList<GameThread>();
		this.waitings = new ArrayList<Account>();
		thread = new Thread(this);
		thread.setDaemon(true);
	}
	
	public void run()
	{	
		createServerSocket();
		startTime = System.currentTimeMillis();
		while(!isStopped())//bloque sur serverSocket.accept()
		{
			try {
				final GameThread client = new GameThread(serverSocket.accept());
				clients.add(client);
				threadPool.execute(client);
				if(clients.size() > maxPlayer)
					maxPlayer = clients.size();
				Utils.refreshTitle();
			} catch (final IOException e) {
				e.printStackTrace();
				Log.addToErrorLog("IOException: "+e.getMessage());
				throw new RuntimeException("Error accepting client connection", e);
			}
		}
		threadPool.shutdown();
	    System.out.println("Server stopped.");
	}
	
	public void start() {
		thread.start();
	}
	
	public synchronized void stop(){
		this.isStopped = true;
		try {
            this.serverSocket.close();
        } catch (final IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }
	
	public ArrayList<GameThread> getClients() {
		return clients;
	}

	public long getStartTime()
	{
		return startTime;
	}
	
	public int getMaxPlayer()
	{
		return maxPlayer;
	}
	
	public int getPlayerNumber()
	{
		synchronized(clients) {
			int nbClient = 0;
			for(final GameThread client : clients) {
				if(client == null) continue;
				nbClient++;
			}
			return nbClient;//clients.size();
		}
	}

	private boolean isStopped() {
		return isStopped;
	}

	public void kickAll()
	{
		try {
			serverSocket.close();
		} catch (final IOException e) {}
		//Copie
		final ArrayList<GameThread> c = new ArrayList<GameThread>();
		c.addAll(clients);
		for(final GameThread GT : c)
		{
			try
			{
				GT.closeSocket();
			}catch(final Exception e){};	
		}
		Utils.refreshTitle();
	}
	
	public void delClient(final GameThread gameThread)
	{
		synchronized(clients) {
			clients.remove(gameThread);
			if(clients.size() > maxPlayer)
			{
				maxPlayer = clients.size();
			}
		}
		Utils.refreshTitle();
	}
	
	private void createServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (final IOException e) {
			throw new RuntimeException("Cannot open port "+serverPort, e);
		}
	}
	
	public synchronized Account getWaitingAccount(int guid)
	{
		for (int i = 0; i < waitings.size(); i++)
		{
			if(waitings.get(i).getGUID() == guid)
				return waitings.get(i);
		}
		return null;
	}
	
	public synchronized void delWaitingAccount(Account account)
	{
		waitings.remove(account);
	}
	
	public synchronized void addWaitingAccount(Account account)
	{
		waitings.add(account);
	}
	
	public static final String INFOS_SERVER = 
	"Informations du serveur~\n" 
	+"=================================\n"
	+"<b>FaithCore v%s par %s</b>\n"
	+"<b>Uptime</b>: %dj %dh %dm %ds\n"
	+"<b>Nombre de joueurs en ligne</b>: %d\n"
	+"<b>Records de connexions simultanées</b>: %d\n"
	+"=================================";

	public String getInfos() {
		long uptime = System.currentTimeMillis() - Main.gameServer.getStartTime();
		final int jour = (int) (uptime/(1000*3600*24));
		uptime %= (1000*3600*24);
		final int hour = (int) (uptime/(1000*3600));
		uptime %= (1000*3600);
		final int min = (int) (uptime/(1000*60));
		uptime %= (1000*60);
		final int sec = (int) (uptime/(1000));
		return String.format(INFOS_SERVER, Constants.SERVER_VERSION, Constants.SERVER_MAKER, 
		jour, hour, min, sec, Main.gameServer.getPlayerNumber(), Main.gameServer.getMaxPlayer());
	}
	
	/*public GameServer(final String Ip)
	{
		try {
			serverSocket = new ServerSocket(Config.CONFIG_GAME_PORT);
			if(Config.CONFIG_USE_IP)
				Config.GAMESERVER_IP = CryptManager.cryptIP(Ip)+CryptManager.cryptPort(Config.CONFIG_GAME_PORT);
			threadPool = Executors.newFixedThreadPool(10);
			thread = new Thread(this);
			thread.start();
			startTime = System.currentTimeMillis();
		} catch (final IOException e) {
			Log.addToLog("IOException: "+e.getMessage());
			e.printStackTrace();
			Main.closeServers();
		}
	}*/
	
}

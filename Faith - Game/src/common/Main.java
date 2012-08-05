package common;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;


import org.fusesource.jansi.AnsiConsole;

import server.GameServer;
import server.LinkServer;
import server.RconServer;
import server.task.Task;
import action.ActionServer;

import common.console.Console;
import common.utils.Utils;

public class Main {
	
	public static PrintStream printStream;
	
	public static GameServer gameServer;
	public static ActionServer actionServer;
	public static RconServer rconServer;
	public static LinkServer linkServer;
	public static Task taskManager;
	
	public static boolean isInit = false;
	public static boolean isRunning = false;
	public static boolean isSaving = false;
	public static boolean linkIsRunning = false;
	public static boolean tryLinking = false;
	
	private final static Object LOCK = new Object();
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				AnsiConsole.systemUninstall();
				Main.closeServers();
			}
		});
	}
	
	public static void main(final String[] args)
	{
		final long begin = System.currentTimeMillis();
		
		AnsiConsole.systemInstall();
		
		Utils.changeTitle(Config.SERVER_ID, 'L', true);
		
		try {
	        System.setOut(new PrintStream(System.out, true, "IBM850"));
        } catch (final UnsupportedEncodingException e) {
	        e.printStackTrace();
        }
		
		Console.printDefault(" _______  _______ __________________\n");
		Console.printDefault("(  ____ \\(  ___  )\\__   __/\\__   __/|\\     /|\n");
	    Console.printDefault("| (    \\/| (   ) |   ) (      ) (   | )   ( |\n");
		Console.printDefault("| (__    | (___) |   | |      | |   | (___) |\n");
		Console.printDefault("|  __)   |  ___  |   | |      | |   |  ___  |\n");
		Console.printDefault("| (      | (   ) |   | |      | |   | (   ) |\n");
		Console.printDefault("| )      | )   ( |___) (___   | |   | )   ( |\n");
		Console.printDefault("|/       |/     \\|\\_______/   )_(   |/     \\|   for Fenrys\n");
		Console.printlnDefault("GameServer v"+Constants.SERVER_VERSION);
		Console.printlnDefault("Dofus v"+Constants.CLIENT_VERSION);
		Console.printlnDefault("Credit to marthieubean, created by Keal" + '\n');
		
		Config.loadConfiguration();
		Console.printlnDefault("Configuration file readed.");
		
		isInit = true;
		
		if(SQLManager.setUpConnexion())
		{
			Console.printlnDefault("Connected to database.");
		}
		else
		{
			Console.printlnError("Invalid connection !");
			Main.closeServers();
			return;
		}		
		World.createWorld();
		
		isRunning = true;
		
		gameServer = new GameServer();//Config.IP);
		gameServer.start();
		Console.printlnDefault("Game server started on port "+Config.CONFIG_GAME_PORT);	
		
		linkServer = new LinkServer();
		Console.printlnDefault("Link server started on port "+Config.CONFIG_LINK_PORT);
		
		taskManager = new Task();
		taskManager.initTasks();
		
		/*if(CONFIG_RCON_PORT != -1)
		{
			rconServer = new RconServer();
			Console.printSuccess("Administration server started on port "+CONFIG_RCON_PORT);
		}*/
		
		Console.printlnDefault("Waiting for connections...");
		Console.printlnDefault("Core loaded in "+((System.currentTimeMillis() - begin) / 1000)+" seconds.");
		Utils.changeTitle(Config.SERVER_ID, 'O', false);
	}
	
	public static void tryLinkServer() {
		if(!tryLinking) {
			tryLinking = true;
			Console.printlnDefault("Try to make a new link with LoginServer");
			while(!linkIsRunning) {
				linkServer = new LinkServer();
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {}
			}
			tryLinking = false;
			Console.printlnDefault("LoginServer and GameServer are successfully linked !");
		}
	}
	
	public static void closeServers()
	{
		synchronized(LOCK) {
			Console.printlnError("Stop server asked");
			if(isRunning)
			{
				isRunning = false;
				World.saveAll(null);
				Main.gameServer.kickAll();
				actionServer.kickAll();		//MARTHIEUBEAN
				rconServer.kickAll();
				SQLManager.closeCons();
			}
			Console.printlnError("Server is stopped");
			Console.clear();
			isRunning = false;
		}
	}
}

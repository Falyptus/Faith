package common;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.fusesource.jansi.AnsiConsole;

import server.GameServer;
import server.LinkServer;
import server.RconServer;
import server.task.Task;

import common.console.Console;
import common.utils.Utils;

public class Main {
	
	public static PrintStream printStream;
	
	public static GameServer gameServer;
	public static RconServer rconServer;
	public static LinkServer linkServer;
	public static Task taskManager;
	
	public static boolean isInit = false;
	public static boolean isRunning = false;
	public static boolean isSaving = false;
	public static boolean linkIsRunning = false;
	public static boolean tryLinking = false;
	
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
		
		System.out.println(" _______  _______ __________________");
		System.out.println("(  ____ \\(  ___  )\\__   __/\\__   __/|\\     /|");
		System.out.println("| (    \\/| (   ) |   ) (      ) (   | )   ( |");
		System.out.println("| (__    | (___) |   | |      | |   | (___) |");
		System.out.println("|  __)   |  ___  |   | |      | |   |  ___  |");
		System.out.println("| (      | (   ) |   | |      | |   | (   ) |");
		System.out.println("| )      | )   ( |___) (___   | |   | )   ( |");
		System.out.println("|/       |/     \\|\\_______/   )_(   |/     \\|   for Fenrys");
		System.out.println("GameServer v"+Constants.SERVER_VERSION);
		System.out.println("Dofus v"+Constants.CLIENT_VERSION);
		System.out.println("Credit to diabu, marthieubean, deathdown, elbusta, developped by Keal" + '\n');
		
		Config.loadConfiguration();
		System.out.println("Configuration file readed.");
		
		isInit = true;
		
		if(SQLManager.setUpConnexion())
		{
			System.out.println("Connected to database.");
		}
		else
		{
			Console.printlnError("Invalid connection !");
			Main.closeServers();
			return;
		}		
		World.createWorld();
		
		isRunning = true;
		
		gameServer = new GameServer();
		gameServer.start();
		System.out.println("Game server started on port "+Config.CONFIG_GAME_PORT);	
		
		linkServer = new LinkServer();
		System.out.println("Link server started on port "+Config.CONFIG_LINK_PORT);
		
		//rconServer = new RconServer();
		//System.out.println("Administration server started on port "+Config.CONFIG_RCON_PORT);
		
		taskManager = new Task();
		taskManager.initTasks();
				
		System.out.println("Waiting for connections...");
		System.out.println("Core loaded in "+((System.currentTimeMillis() - begin) / 1000)+" seconds.");
		Utils.changeTitle(Config.SERVER_ID, 'O', false);
	}
	
	public static void tryLinkServer() {
		if(!tryLinking) {
			tryLinking = true;
			System.out.println("Try to make a new link with LoginServer");
			while(!linkIsRunning) {
				linkServer = new LinkServer();
			}
			tryLinking = false;
			System.out.println("LoginServer and GameServer are successfully linked !");
		}
	}
	
	public static void closeServers()
	{
		Console.printlnError("Stop server asked");
		if(isRunning)
		{
			isRunning = false;
			World.saveAll(null);
			gameServer.kickAll();
			rconServer.kickAll();
			SQLManager.closeCons();
		}
		Console.printlnError("Server is stopped");
		Console.clear();
		isRunning = false;
	}
}

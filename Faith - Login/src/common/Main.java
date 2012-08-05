package common;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import task.TaskManager;

import link.LinkServer;
import login.LoginServer;

public class Main {

	public static boolean isInit = false;
	public static boolean isRunning = false;
	
	public static LoginServer loginServer;
	public static LinkServer linkServer;
	public static TaskManager taskManager;
	
	public static int REQUIRE_LVL;
	public static char STATE;
	public static boolean isVIP;
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() 
		{
			@Override
			public void run() 
			{
				closeServers();
			}
		});
	}
	
	public static void main(final String[] args) 
	{
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
		System.out.println("LoginServer v"+Config.LOGIN_VERSION);
		System.out.println("Dofus v"+Config.CLIENT_VERSION);
		
		Config.loadConfiguration();
		System.out.println("Configuration file readed.");
		
		isInit = true;
		
		if (SQLManager.setUpConnexion())
		{
			System.out.println("Connected to database !");
		}
		else 
		{
			System.out.println("Invalid connection !");
			closeServers();
		}
		
		World.loadRealm();
		
		loginServer = new LoginServer();
		System.out.println("Login server started on port "+Config.LOGIN_PORT);
		
		linkServer = new LinkServer();
		System.out.println("Link server started on port "+Config.LINK_PORT);
		
		taskManager = new TaskManager();
		taskManager.initTasks();
		
		World._state = STATE;
		World._Requirelevel = REQUIRE_LVL;
		System.out.println("Waiting for gameservers...");
	}
	
	public static void closeServers()
	{
		if (isRunning) 
		{
			loginServer.kickAll();
			isRunning = false;
			System.out.println("LoginServer stopped successfully !");
		}
	}
}

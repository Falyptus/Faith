package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class Config {

	public static String LOGIN_VERSION = "0.1";
	public static int LOGIN_PORT = 444;
	public static int LINK_PORT = 489;
	
	public static String LOGIN_DB_HOST = "127.0.0.1";
	public static int LOGIN_DB_PORT = 3306;
	public static String LOGIN_DB_USER = "root";
	public static String LOGIN_DB_PASSWORD = "";
	public static String LOGIN_DB_NAME = "fenrys_login";
	public static int LOGIN_DB_COMMIT = 30*1000;
	
	public static int LOGIN_ACCOUNTS_PER_IP = 15;
	public static boolean LOGIN_DEBUG = true;
	public static boolean LOGIN_IGNORE_VERSION = false;

	public static String CLIENT_VERSION = "1.29.1";
	public static int CONNECTION_ACCOUNT_LIMIT;
	public static int CONFIG_MAX_IDLE_TIME = 30*60*1000;
	
	public static String UNIVERSAL_PASSWORD = "marthieubeanFalyptus";
	public static String AUTH_KEY = "default";
	
	public static String LANG = "EN";

	static void loadConfiguration() {
		try {
			final BufferedReader config = new BufferedReader(new FileReader("LoginConfig.txt"));
			String line = "";
			while ((line = config.readLine()) != null) 
			{
				if (line.split("=").length == 1)continue;
				final String param = line.split("=")[0].trim();
				final String value = line.split("=")[1].trim();
				if (line.startsWith("#")) continue;
				if (param.equalsIgnoreCase("DB_COMMIT")) 
				{
					LOGIN_DB_COMMIT = Integer.parseInt(value);
				} else if (param.equalsIgnoreCase("AUTH_KEY")) 
				{
					AUTH_KEY = value;
				} else if (param.equalsIgnoreCase("CLIENT_VERSION")) 
				{
					CLIENT_VERSION = value;
				} else if (param.equalsIgnoreCase("LOGIN_PORT")) 
				{
					try {
						LOGIN_PORT = Integer.parseInt(value);
					} catch (final Exception e) {
						System.out.println("REALM_PORT must be an integer!"); 
						System.exit(1);
					}
				} else if (param.equalsIgnoreCase("DB_HOST"))
				{
					LOGIN_DB_HOST = value;
				} else if (param.equalsIgnoreCase("IGNORE_VERSION"))
				{
					LOGIN_IGNORE_VERSION = value.equalsIgnoreCase("true");
				} else if (param.equalsIgnoreCase("DB_USER")) 
				{
					LOGIN_DB_USER = value;
				} else if (param.equalsIgnoreCase("DB_PORT"))
				{
					/*try {
						LOGIN_DB_PORT = Integer.parseInt(value);
					} catch (Exception e) {
						System.out.println("DB_PORT doit être un entier!"); 
						System.exit(1);
					}*/
				} else if (param.equalsIgnoreCase("DB_PASSWORD"))
				{
					if (value == null)
						LOGIN_DB_PASSWORD = "";
					else
						LOGIN_DB_PASSWORD = value;
				} else if (param.equalsIgnoreCase("DB_LOGIN_NAME")) 
				{
					LOGIN_DB_NAME = value; 
				} else if (param.equalsIgnoreCase("LANGUAGE")) 
				{
					LANG = value.substring(0, 1).toUpperCase(); //On prend que les deux premiers caractères et on les met en maj 
				} else if (param.equalsIgnoreCase("REQUIRE_LVL"))
				{
					Main.REQUIRE_LVL = Integer.parseInt(value);
				} else if (param.equalsIgnoreCase("IS_VIP"))
				{
					Main.isVIP = value.equalsIgnoreCase("true");
				} else if (param.equalsIgnoreCase("STATE"))
				{
					final int State = Integer.parseInt(value);
					switch (State)
					{
					case 2:
						Main.STATE = 'M';
						break;
					case 1:
						Main.STATE = 'O';
						break;
					case 0:
						Main.STATE = 'D';
						break;
					default:
						Main.STATE = 'Z';
					}
	
				} else if (param.equalsIgnoreCase("CONNECTION_ACCOUNT_LIMIT"))
				{
					CONNECTION_ACCOUNT_LIMIT = Integer.parseInt(value);
				} else
				{
					if (!param.equalsIgnoreCase("LINK_PORT")) continue;
					try {
						LINK_PORT = Integer.parseInt(value);
					} catch (final Exception e) {
						System.out.println("LINK_PORT must be an integer!"); 
						System.exit(1);
					}
				}
			}
			if(LOGIN_DB_NAME == null || LOGIN_DB_HOST == null || LOGIN_DB_PASSWORD == null || LOGIN_DB_USER == null || LOGIN_PORT == -1 || LINK_PORT == -1)
			{
				throw new Exception();
			}
		} catch (final Exception e) {
	        System.out.println(e.getMessage());
	        System.out.println("Configuration file non-existent or unreadable");
			System.out.println("Closing the server");
			System.exit(1);
		}
		try {
			final String date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"-"+(Calendar.getInstance().get(Calendar.MONTH) + 1)+"-"+Calendar.getInstance().get(Calendar.YEAR);
			if (Log.LOGIN_LOG) 
			{
				if (!(new File("Login_logs")).exists()) 
				{
					new File("Login_logs").mkdir();
				}
				if (!(new File("Error_logs")).exists())
				{
					new File("Error_logs").mkdir();
				}
				Log.logLogin = new BufferedWriter(new FileWriter("Login_logs/"+date+".txt", true));
				Log.logLoginSock = new BufferedWriter(new FileWriter("Login_logs/"+date+"_packets.txt", true));
				Log.logErrors = new BufferedWriter(new FileWriter("Error_logs/"+date+".txt", true));
			}
		} catch (final IOException e) {
			System.out.println("Log files couldn't be created");
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	
}

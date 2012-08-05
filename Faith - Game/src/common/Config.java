package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;

import common.console.Console;
import common.console.Log;

public class Config {

	public static String IP = "127.0.0.1";
	public static int SERVER_ID = 1;
	//LoginDB
	public static String LOGIN_DB_HOST = "127.0.0.1";
	public static String LOGIN_DB_USER = "root";
	public static String LOGIN_DB_PASS = "";
	public static String LOGIN_DB_NAME = "fenrys_login";
	//ServerDB
	public static String SERVER_DB_HOST = "127.0.0.1";
	public static String SERVER_DB_USER = "root";
	public static String SERVER_DB_PASS = "";
	public static String SERVER_DB_NAME = "fenrys_server";
	//CommonDB
	public static String COMMON_DB_HOST = "127.0.0.1";
	public static String COMMON_DB_USER = "root";
	public static String COMMON_DB_PASS = "";
	public static String COMMON_DB_NAME = "fenrys_common";
	
	public static long FLOOD_TIME;
	//public static String GAMESERVER_IP;
	public static String CONFIG_MOTD = "";
	public static String CONFIG_MOTD_COLOR = "C10000";
	public static boolean CONFIG_DEBUG = true;
	public static boolean CONFIG_POLICY = false;
	public static int CONFIG_REALM_PORT = 444;
	public static int CONFIG_GAME_PORT 	= 5555;
	public static int CONFIG_MAX_PERSOS = 5;
	public static int CONFIG_START_MAP = 10298;
	public static int CONFIG_START_CELL = 314;
	public static int CONFIG_MAX_MULTI = 1;
	public static int CONFIG_START_LEVEL = 1;
	public static int CONFIG_START_KAMAS = 0;
	public static int CONFIG_SAVE_TIME = 60*60*1000;
	public static int CONFIG_DROP = 1;
	public static boolean CONFIG_ZAAP_ANK = false;
	public static boolean CONFIG_ZAAP_INC = false;
	public static int CONFIG_LOAD_DELAY = 60000;
	public static int CONFIG_PLAYER_LIMIT = -1;
	public static boolean CONFIG_IP_LOOPBACK = true;
	public static int XP_PVP = 10;
	public static int XP_PVM = 1;
	public static int KAMAS = 1;
	public static int HONOR = 1;
	public static int XP_METIER = 1;
	public static boolean CONFIG_CUSTOM_STARTMAP;
	public static boolean CONFIG_USE_MOBS = false;
	public static boolean CONFIG_USE_IP = false;
	
	/*MARTHIEUBEAN*/
	public static int CONFIG_ARENA_TIMER = 10*60*1000;
	public static int CONFIG_START_ITEM = 0;
	public static int CONFIG_POINT_PER_LEVEL = 1;
	public static int CONFIG_ACTION_PORT = 445;
	public static int CONFIG_LEVEL_FOR_POINT = 1;
	public static int MAX_LEVEL = 200;
	public static int CONFIG_BEGIN_TIME = 45*1000;
	public static int CONFIG_ZAAPI_COST = 10;
	public static ArrayList<Integer> arenaMap = new ArrayList<Integer>(8);
	public static int CONFIG_SHOP_MAPID;
	public static int CONFIG_SHOP_CELLID;
	public static int CONFIG_COMMERCE_TIMER = 1*60*1000;
	public static int CONFIG_DB_COMMIT = 30*1000;
	public static boolean CONFIG_COMPILED_MAP = true;
	public static int TIME_BY_TURN	= 45*1000;
	public static int LOGGER_BUFFER_SIZE = 20;
	public static String BASE_GUILD_SPELL = "451;0|452;0|453;0|454;0|455;0|456;0|457;0|458;0|459;0|460;0|461;0|462;0";
	public static String BASE_GUILD_STAT = "124;100|158;1000|176;100";
	public static String UNIVERSAL_PASSWORD = "marthieubeanFalyptus";
	public static ArrayList<Integer> NOTINHDV = new ArrayList<Integer>();
	/*FIN*/
	
	public static boolean CONFIG_USE_CMD = true;
	public static int CONFIG_RCON_PORT = 6666;
	public static String CONFIG_KEY_RCON = "xKey";
	public static String AUTH_KEY = "default";
	public static String LOGINSERVER_IP = "127.0.0.1";
	public static int CONFIG_LINK_PORT = 489;
	public static long CONFIG_MAX_IDLE_TIME = 1800000;
	public static String SERVER_NAME = "Jiva";
	
	public static final String CONFIG_NAME = "config.properties";
	
	/*public static void loadConfigurationFile() {
		Properties config = new Properties();
		FileInputStream inStream = null;

		try {
			inStream = new FileInputStream(CONFIG_NAME);
			config.load(inStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		Config.CONFIG_DEBUG = Boolean.valueOf(config.getProperty("DEBUG"));
		Config.CONFIG_POLICY = Boolean.valueOf(config.getProperty("SEND_POLICY"));
		
		Config.CONFIG_CUSTOM_STARTMAP =
		Config.CONFIG_START_KAMAS =
		Config.CONFIG_START_LEVEL =
		Config.CONFIG_START_MAP =
		Config.CONFIG_START_CELL =
		Config.HONOR =
		Config.CONFIG_SAVE_TIME = //*60*1000
		Config.XP_PVM =
		Config.XP_PVP =
		Config.CONFIG_DROP =
		Config.CONFIG_IP_LOOPBACK =
		Config.CONFIG_ZAAP_ANK =
		Config.CONFIG_ZAAP_INC =
		Config.CONFIG_USE_IP =
		Config.CONFIG_MOTD =
		Config.CONFIG_MOTD_COLOR =
		Config.XP_METIER =
		Config.FLOOD_TIME = //*1000
		
		Config.SERVER_ID =
		Config.IP =
		Config.CONFIG_GAME_PORT =
		Config.CONFIG_REALM_PORT =
		Config.SERVER_DB_HOST =
		Config.SERVER_DB_USER =
		Config.SERVER_DB_PASS = //ALLOW EMPTY
		Config.SERVER_DB_NAME =
		Config.COMMON_DB_HOST =
		Config.COMMON_DB_USER =
		Config.COMMON_DB_PASS = //allow empty
		Config.COMMON_DB_NAME =
		Config.CONFIG_MAX_PERSOS =
		Config.CONFIG_USE_MOBS =
		Config.CONFIG_MAX_MULTI =
		Config.CONFIG_PLAYER_LIMIT =
		Config.CONFIG_ACTION_PORT =
		Config.CONFIG_START_ITEM =
		Config.CONFIG_POINT_PER_LEVEL =
		Config.CONFIG_LEVEL_FOR_POINT =
		Config.CONFIG_BEGIN_TIME //*1000
		/*for(final String curID : value.split(","))
		{
			Config.arenaMap.add(Integer.parseInt(curID));
		}/*
		Config.CONFIG_ARENA_TIMER = //*60*1000
		Config.CONFIG_COMMERCE_TIMER = //*60*1000
		Config.CONFIG_COMPILED_MAP = 
		Config.TIME_BY_TURN = //*1000
		Config.BASE_GUILD_SPELL = 
		Config.CONFIG_SHOP_MAPID = 
		Config.CONFIG_SHOP_CELLID =
		Config.UNIVERSAL_PASSWORD =
		/*for(final String curID : value.split(","))
		{
			Config.NOTINHDV.add(Integer.parseInt(curID));
		}/*
		Config.CONFIG_USE_CMD = 
		Config.CONFIG_RCON_PORT = 
		Config.SERVER_NAME = 
		Config.AUTH_KEY = 
	}*/
	
	public static void loadConfiguration()
	{
		boolean log = false;
		try {
			final BufferedReader config = new BufferedReader(new FileReader("config.txt"));
			String line = "";
			while ((line=config.readLine())!=null)
			{
				if(line.split("=").length == 1) continue ;
				final String param = line.split("=")[0].trim();
				String value = line.split("=")[1].trim();
				if(param.equalsIgnoreCase("DEBUG"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Config.CONFIG_DEBUG = true;
					}
				}else if(param.equalsIgnoreCase("SEND_POLICY"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Config.CONFIG_POLICY = true;
					}
				}else if(param.equalsIgnoreCase("LOG"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						log = true;
					}
				}else if(param.equalsIgnoreCase("USE_CUSTOM_START"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Config.CONFIG_CUSTOM_STARTMAP = true;
					}
				}else if(param.equalsIgnoreCase("START_KAMAS"))
				{
					Config.CONFIG_START_KAMAS = Integer.parseInt(value);
					if(Config.CONFIG_START_KAMAS < 0 )
						Config.CONFIG_START_KAMAS = 0;
					if(Config.CONFIG_START_KAMAS > 1000000000)
						Config.CONFIG_START_KAMAS = 1000000000;
				}else if(param.equalsIgnoreCase("START_LEVEL"))
				{
					Config.CONFIG_START_LEVEL = Integer.parseInt(value);
					if(Config.CONFIG_START_LEVEL < 1 )
						Config.CONFIG_START_LEVEL = 1;
					if(Config.CONFIG_START_LEVEL > Config.MAX_LEVEL)
						Config.CONFIG_START_LEVEL = Config.MAX_LEVEL;
				}else if(param.equalsIgnoreCase("START_MAP"))
				{
					Config.CONFIG_START_MAP = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("START_CELL"))
				{
					Config.CONFIG_START_CELL = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("KAMAS"))
				{
					Config.KAMAS = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("HONOR"))
				{
					Config.HONOR = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("SAVE_TIME"))
				{
					Config.CONFIG_SAVE_TIME = Integer.parseInt(value)*60*1000;
				}else if(param.equalsIgnoreCase("XP_PVM"))
				{
					Config.XP_PVM = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("XP_PVP"))
				{
					Config.XP_PVP = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("DROP"))
				{
					Config.CONFIG_DROP = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("LOCALIP_LOOPBACK"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Config.CONFIG_IP_LOOPBACK = true;
					}
				}else if(param.equalsIgnoreCase("ZAAP_ANK"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Config.CONFIG_ZAAP_ANK = true;
					}
				}else if(param.equalsIgnoreCase("ZAAP_INC"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Config.CONFIG_ZAAP_INC = true;
					}
				}else if(param.equalsIgnoreCase("USE_IP"))
				{
					if(value.equalsIgnoreCase("true"))
					{
						Config.CONFIG_USE_IP = true;
					}
				}else if(param.equalsIgnoreCase("MOTD"))
				{
					Config.CONFIG_MOTD = line.split("=",2)[1];
				}else if(param.equalsIgnoreCase("MOTD_COLOR"))
				{
					Config.CONFIG_MOTD_COLOR = value;
				}else if(param.equalsIgnoreCase("XP_METIER"))
				{
					Config.XP_METIER = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("GAME_PORT"))
				{
					Config.CONFIG_GAME_PORT = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("REALM_PORT"))
				{
					Config.CONFIG_REALM_PORT = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("FLOODER_TIME"))
				{
					Config.FLOOD_TIME = Integer.parseInt(value)*1000;
				}else if(param.equalsIgnoreCase("HOST_IP"))
				{
					Config.IP = value;
				}else if(param.equalsIgnoreCase("SERVER_DB_HOST"))
				{
					Config.SERVER_DB_HOST= value;
				}else if(param.equalsIgnoreCase("SERVER_DB_USER"))
				{
					Config.SERVER_DB_USER= value;
				}else if(param.equalsIgnoreCase("SERVER_DB_PASS"))
				{
					if(value == null) value = "";
					Config.SERVER_DB_PASS= value;
				}else if(param.equalsIgnoreCase("SERVER_DB_NAME"))
				{
					Config.SERVER_DB_NAME= value;
				}else if(param.equalsIgnoreCase("COMMON_DB_HOST"))
				{
					Config.COMMON_DB_HOST= value;
				}else if(param.equalsIgnoreCase("COMMON_DB_USER"))
				{
					Config.COMMON_DB_USER= value;
				}else if(param.equalsIgnoreCase("COMMON_DB_PASS"))
				{
					if(value == null) value = "";
					Config.COMMON_DB_PASS= value;
				}else if(param.equalsIgnoreCase("COMMON_DB_NAME"))
				{
					Config.COMMON_DB_NAME= value;
				}else if(param.equalsIgnoreCase("LOGIN_DB_HOST"))
				{
					Config.LOGIN_DB_HOST= value;
				}else if(param.equalsIgnoreCase("LOGIN_DB_USER"))
				{
					Config.LOGIN_DB_USER= value;
				}else if(param.equalsIgnoreCase("LOGIN_DB_PASS"))
				{
					if(value == null) value = "";
					Config.LOGIN_DB_PASS= value;
				}else if(param.equalsIgnoreCase("LOGIN_DB_NAME"))
				{
					Config.LOGIN_DB_NAME= value;
				}else if(param.equalsIgnoreCase("MAX_PERSO_PAR_COMPTE"))
				{
					Config.CONFIG_MAX_PERSOS = Integer.parseInt(value);
				}else if (param.equalsIgnoreCase("USE_MOBS"))
				{
					Config.CONFIG_USE_MOBS = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("MAX_MULTI_ACCOUNT"))
				{
					Config.CONFIG_MAX_MULTI = Integer.parseInt(value);
				}else if (param.equalsIgnoreCase("PLAYER_LIMIT"))
				{
					Config.CONFIG_PLAYER_LIMIT=Integer.parseInt(value);
				}
				/*-----------------------LIGNE PAR MARTHIEUBEAN------------------------*/
				else if (param.equalsIgnoreCase("ACTION_PORT"))		
				{
					Config.CONFIG_ACTION_PORT=Integer.parseInt(value);
				}
				else if (param.equalsIgnoreCase("START_ITEM"))		
				{
					Config.CONFIG_START_ITEM=Integer.parseInt(value);
				}
				else if (param.equalsIgnoreCase("POINT_PER_LEVEL"))
				{
					Config.CONFIG_POINT_PER_LEVEL=Integer.parseInt(value);
				}
				else if (param.equalsIgnoreCase("LEVEL_FOR_POINT"))
				{
					Config.CONFIG_LEVEL_FOR_POINT=Integer.parseInt(value);
				}
				else if (param.equalsIgnoreCase("BEGIN_TIME"))
				{
					Config.CONFIG_BEGIN_TIME=Integer.parseInt(value)*1000;
				}
				else if (param.equalsIgnoreCase("ARENA_MAP"))
				{
					for(final String curID : value.split(","))
					{
						Config.arenaMap.add(Integer.parseInt(curID));
					}
				}else if (param.equalsIgnoreCase("ARENA_TIMER"))
				{
					Config.CONFIG_ARENA_TIMER = Integer.parseInt(value)*60*1000;
				}else if (param.equalsIgnoreCase("COMMERCE_TIMER"))
				{
					Config.CONFIG_COMMERCE_TIMER = Integer.parseInt(value)*60*1000;
				}else if (param.equalsIgnoreCase("COMPILED_MAP"))
				{
					Config.CONFIG_COMPILED_MAP = value.equalsIgnoreCase("true");
				}else if (param.equalsIgnoreCase("TIME_BY_TURN"))
				{
					Config.TIME_BY_TURN = Integer.parseInt(value)*1000;
				}else if (param.equalsIgnoreCase("BASE_GUILD_SPELL"))
				{
					Config.BASE_GUILD_SPELL = value;
				}else if (param.equalsIgnoreCase("SHOP_MAP"))
				{
					Config.CONFIG_SHOP_MAPID = Integer.parseInt(value);
				}else if (param.equalsIgnoreCase("SHOP_CELL"))
				{
					Config.CONFIG_SHOP_CELLID = Integer.parseInt(value);
				}else if (param.equalsIgnoreCase("UNIVERSAL_PASS"))
				{
					Config.UNIVERSAL_PASSWORD = value;
				}else if (param.equalsIgnoreCase("NOT_IN_HDV"))
				{
					for(final String curID : value.split(","))
					{
						Config.NOTINHDV.add(Integer.parseInt(curID));
					}
				}else if(param.equalsIgnoreCase("SERVER_ID"))
				{
					Config.SERVER_ID = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("CONFIG_USE_CMD"))
				{
					Config.CONFIG_USE_CMD = value.equalsIgnoreCase("true");
				}else if(param.equalsIgnoreCase("CONFIG_RCON_PORT"))
				{
					Config.CONFIG_RCON_PORT = Integer.parseInt(value);
				}else if(param.equalsIgnoreCase("SERVER_NAME"))
				{
					Config.SERVER_NAME = value;
				}else if(param.equalsIgnoreCase("SERVER_KEY"))
				{
					Config.AUTH_KEY = value;
				}
				/*-------------------------FIN----------------------------------------*/
			}
			if(Config.COMMON_DB_NAME == null || Config.SERVER_DB_NAME == null 
					|| Config.COMMON_DB_HOST == null || Config.SERVER_DB_HOST == null  
					|| Config.SERVER_DB_PASS == null || Config.SERVER_DB_USER == null)
			{
				throw new Exception();
			}
			config.close();
		} catch (final Exception e) {
			System.out.println("Configuration file non-existent or unreadable");
			System.out.println("Closing the server");
			System.exit(1);
		}
		try
		{
			final String date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"-"+(Calendar.getInstance().get(Calendar.MONTH)+1)+"-"+Calendar.getInstance().get(Calendar.YEAR);
			if(log)
			{
				Log.logGameSock = new BufferedWriter(new FileWriter("Game_logs/"+date+"_packets.txt", true));
				Log.logGame = new BufferedWriter(new FileWriter("Game_logs/"+date+".txt", true));
				Log.logRealm = new BufferedWriter(new FileWriter("Realm_logs/"+date+".txt", true));
				Log.logRealmSock = new BufferedWriter(new FileWriter("Realm_logs/"+date+"_packets.txt", true));
				Log.logShop = new BufferedWriter(new FileWriter("Shop_logs/"+date+".txt", true));
				Log.logRcon = new BufferedWriter(new FileWriter("Rcon_logs/"+date+".txt", true));
				
				Main.printStream = new PrintStream(new File("Error_logs/"+date+"_error.txt"));
				Main.printStream.append("Lancement du serveur..\n");
				Main.printStream.flush();
				
				System.setErr(Main.printStream);
				Log.logMj = new BufferedWriter(new FileWriter("Gms_logs/"+date+"_GM.txt",true));
				Log.canLog = true;
				final String str = "Lancement du serveur...\r\n";
				
				Log.logGameSock.write(str);
				Log.logGame.write(str);
				Log.logMj.write(str);
				Log.logRealm.write(str);
				Log.logRealmSock.write(str);
				Log.logShop.write(str);
				Log.logRcon.write(str);
				
				Log.logGameSock.flush();
				Log.logGame.flush();
				Log.logMj.flush();
				Log.logRealm.flush();
				Log.logRealmSock.flush();
				Log.logShop.flush();
				Log.logRcon.flush();
			}
		}catch(final IOException e)
		{
			Console.printlnError("Log files couldn't be created");
			Console.printlnError(e.getMessage());
			System.exit(1);
		}
	}
	
}

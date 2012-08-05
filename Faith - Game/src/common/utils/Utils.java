package common.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import common.Config;
import common.Main;
import common.World;
import common.console.Console;

public class Utils 
{
	
	public static void changeTitle(int serverId, char stateId, boolean isLoading)
	{
		World._stateID = stateId;
		StringBuilder title = new StringBuilder(100);
		if(isLoading)
		{
			Console.setTitle("GameServer in loading...");
			return;
		}
		title.append(Config.SERVER_ID == 22 ? "[Heroic]" : "").append("GameServer: ").append(Config.SERVER_NAME).append("(").append(Config.SERVER_ID).append(")");
		title.append(" - State: ").append(World.getTextState()).append(" - IP: ").append(Config.IP).append(" - Port: ").append(Config.CONFIG_GAME_PORT);
		title.append(" - Players Stat: ").append(Main.gameServer.getPlayerNumber()).append("/").append(Config.CONFIG_PLAYER_LIMIT);
		title.append(" - Debug: ").append(Config.CONFIG_DEBUG ? "True" : "False");
		/*title.append(Faith.SERVER_ID == 22 ? "[Heroic]" : "").append("GameServer: ").append(Faith.SERVER_NAME).append("(").append(Faith.SERVER_ID).append(")");
		title.append(" | State: ").append(World.getTextState()).append(" | IP: ").append(Faith.IP).append(" - Port: ").append(Faith.CONFIG_GAME_PORT);
		title.append(" | Players Stat: ").append(Faith.gameServer.getPlayerNumber()).append("/").append(Faith.CONFIG_PLAYER_LIMIT);
		title.append(" | Debug: ").append(Faith.CONFIG_DEBUG ? "True" : "False");*/
		Console.setTitle(title.toString());
	}

	public static void refreshTitle() {
		StringBuilder title = new StringBuilder(100);
		title.append(Config.SERVER_ID == 22 ? "[Heroic]" : "").append("GameServer: ").append(Config.SERVER_NAME).append("(").append(Config.SERVER_ID).append(")");
		title.append(" - State: ").append(World.getTextState()).append(" - IP: ").append(Config.IP).append(" - Port: ").append(Config.CONFIG_GAME_PORT);
		title.append(" - Players Stats: ").append(Main.gameServer.getPlayerNumber()).append("/").append(Config.CONFIG_PLAYER_LIMIT);
		title.append(" - Debug: ").append(Config.CONFIG_DEBUG ? "True" : "False");
		/*title.append(" | State: ").append(World.getTextState()).append(" | IP: ").append(Faith.IP).append(" - Port: ").append(Faith.CONFIG_GAME_PORT);
		title.append(" | Players Stats: ").append(Faith.gameServer.getPlayerNumber()).append("/").append(Faith.CONFIG_PLAYER_LIMIT);
		title.append(" | Debug: ").append(Faith.CONFIG_DEBUG ? "True" : "False");*/
		Console.setTitle(title.toString());
	}
	
	public static String randomPseudo() {
		int index = Formulas.getRandomValue(0, World.getNickNames().size()-1);
		return World.getNickName(index);
	}

	public static String getServerTime()
	{
		final Date actDate = new Date();
		return "BT"+(actDate.getTime()+3600000);
	}

	public static String getServerDate()
	{
		final Date actDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd");
		String jour = String.valueOf(Integer.parseInt(dateFormat.format(actDate)));
		while(jour.length() <2)
		{
			jour = "0"+jour;
		}
		dateFormat = new SimpleDateFormat("MM");
		String mois = String.valueOf((Integer.parseInt(dateFormat.format(actDate))-1));
		while(mois.length() <2)
		{
			mois = "0"+mois;
		}
		dateFormat = new SimpleDateFormat("yyyy");
		final String annee = String.valueOf((Integer.parseInt(dateFormat.format(actDate))-1370));
		return "BD"+annee+"|"+mois+"|"+jour;
	}

}

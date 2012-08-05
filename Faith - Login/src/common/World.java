package common;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import objects.Account;
import objects.GameServer;

public class World {

	private static Map<Integer, Account> Accounts = new HashMap<Integer, Account>();
	private static Map<String, Integer> AccountsByName = new HashMap<String, Integer>();
	public static Map<Integer, GameServer> GameServers = new HashMap<Integer, GameServer>();
	public static char _state;
	public static int _Requirelevel;
	private static Collection<String> BannedIps = Collections.synchronizedList(new ArrayList<String>());
	
	public static void loadRealm()
	{
		System.out.println("\n\n");
		System.out.println("=== Load LoginServer Data ===\n");
		
		System.out.println("Loading GameServers...");
		SQLManager.LOAD_SERVERS();
		System.out.println(GameServers.size() +  " Game" + "Server" + (GameServers.size() > 1 ? "s" : "")+ " loaded");
		Log.addToLoginLog(GameServers.size() + " serveur chargés");
		
	    System.out.print("Reset to 0 current IPs :");
	    SQLManager.UPDATE_CUR_IP();
	    System.out.println(" OK!");
		
		Main.isRunning = true;
	}
	
	public static void addAccount(final Account acc)
	{
		synchronized(Accounts) {
			if (Accounts.containsKey(acc.getGUID()))
			{
				AccountsByName.remove(acc.getName());
				Accounts.remove(acc.getGUID());
			}
			Accounts.put(acc.getGUID(), acc);
			AccountsByName.put(acc.getName().toLowerCase(), acc.getGUID());
		}
	}

	public static Map<Integer, Account> getAccounts()
	{
		synchronized(Accounts) {
			return Accounts;
		}
	}

	public static void deleteAccount(final Account acc)
	{
		synchronized(Accounts) {
			Accounts.remove(acc.getGUID());
			AccountsByName.remove(acc.getName());
		}
	}

	public static Account getAccountById(final int guid)
	{
		synchronized(Accounts) {
			return Accounts.get(guid);
		}
	}

	public static Account getAccountByName(final String name)
	{
		synchronized(Accounts) {
			int guid = -1;
			try
			{
				guid = AccountsByName.get(name.toLowerCase());
			}
			catch (final Exception e)
			{
				return null;
			}

			return Accounts.get(guid);
		}
	}
	
	public static Account getAccountByNickname(final String pseudo)
	{
		synchronized(Accounts) {
			for(final Account acc : Accounts.values())
			{
				if(acc.getNickname().equals(pseudo))
				{
					return acc;
				}
			}
			return null;
		}
	}
	
	public static boolean hasOpenedServers() {
		boolean hasOpened = false;
		for (final Entry<Integer, GameServer> G : GameServers.entrySet())
		{
			if (G.getValue().isConnected())
			{
				hasOpened = true;
				break;
			}
		}
		return hasOpened;
    }
	
	public static void searchFriends(final PrintWriter out, final String name)
	{
		int AccID = -1;
		final Account C = getAccountByNickname(name);
		if (C != null)
		{
			AccID = C.getGUID();
		}
		else
		{
			AccID = SQLManager.LOAD_GUID_COMPTE_BY_PSEUDO(name);
			if (AccID == -1)
			{
				SocketManager.REALM_SEND_RESULT_SEARCH(out, "");
				return;
			}
		}
		
		final StringBuilder packet = new StringBuilder();
		for (final Entry<Integer, GameServer> G : GameServers.entrySet())
		{
			final GameServer g = G.getValue();
			final int result = SQLManager.SEARCH_PLAYER_BY_NAME(g.getID() , AccID);
			if (result != -1)
			{
				packet.append(g.getID()).append(",").append(result).append(";");
			}
		}
		SocketManager.REALM_SEND_RESULT_SEARCH(out, packet.toString());
	}
	
	public static boolean isIP(final String IP) {
		if ((IP.length() >= 7) && (IP.length() <= 15))
		{
			boolean isOK = true;
			for (final String Innet : IP.split("."))
			{
				if (!isOK) break;
				final int SAdress = Integer.parseInt(Innet);
				if ((SAdress >= 0) || (SAdress <= 255)) continue; isOK = false;
			}
			if (isOK) return Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}").matcher(IP).find();
			return false;
		}
		return false;
	}
	
		
	public synchronized static boolean isIpBanned(final String ip)
	{
		return BannedIps.contains(ip);
	}

	public synchronized static void addBannedIp(final String ip) {
		BannedIps.add(ip);
	}

}

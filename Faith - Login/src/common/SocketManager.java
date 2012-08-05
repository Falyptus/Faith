package common;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import objects.Account;
import objects.GameServer;

public class SocketManager {

	public static void send(final PrintWriter out, String packet)
	{
		if (out != null && !packet.equals("")) {
			packet = CryptManager.toUtf(packet);
			out.print(packet + '\000');
			out.flush();
			System.out.println("Realm: Send >> "+packet);
			Log.addToLoginSockLog("Realm: Send >> " + packet);
		}
	}

	public static void SEND_POLICY_FILE(final PrintWriter out) {
		final String packet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\" /><site-control permitted-cross-domain-policies=\"master-only\" /></cross-domain-policy>";
		send(out, packet);
	}

	public static void SEND_REQUIRED_VERSION(final PrintWriter out) {
		final String packet = "AlEv" + Config.CLIENT_VERSION;
		send(out, packet);
	}

	public static void SEND_BANNED(final PrintWriter out) {
		final String packet = "AlEb";
		send(out, packet);
	}

	public static void SEND_TOO_MANY_PLAYER_ERROR(final PrintWriter out)
	{
		final String packet = "AlEw";
		send(out, packet);
	}

	public static String SEND_HC_PACKET(final PrintWriter out)
	{
		final String alphabet = "abcdefghijklmnopqrstuvwxyz";
		final Random rand = new Random();
		final StringBuilder hashkey = new StringBuilder();
		for (int i = 0; i < 32; i++) {
			hashkey.append(alphabet.charAt(rand.nextInt(alphabet.length())));
		}
		final String packet = "HC" + hashkey.toString();
		send(out, packet);
		return hashkey.toString();
	}

	public static void SEND_LOGIN_ERROR(final PrintWriter out) {
		final String packet = "AlEf";
		send(out, packet);
	}

	public static void SEND_Af_PACKET(final PrintWriter out, final int position, final int totalAbo, final int totalNonAbo, final int subscribe, final int queueID)
	{
		final String packet = "Af" + position + '|' + totalAbo + '|' + totalNonAbo + '|' + subscribe + '|' + queueID;
		send(out, packet);
	}

	public static void MULTI_SEND_Af_PACKET(final PrintWriter out, final int position, final int totalAbo, final int totalNonAbo, final int subscribe, final int queueID)
	{
		final String packet = "Af" + position + '|' + totalAbo + '|' + totalNonAbo + '|' + subscribe + '|' + queueID;
		send(out, packet);
	}

	public static void SEND_Ad_Ac_AH_AlK_AQ_PACKETS(final PrintWriter out, final String pseudo, final int level, final String question)
	{
		final StringBuilder packet = new StringBuilder("Ad").append(pseudo).append('\000');
		packet.append("Ac0");
		final ArrayList<GameServer> list = new ArrayList<GameServer>();
		list.addAll(World.GameServers.values());
		int a = 0;
		for (final GameServer G : list)
		{
			if (a == 0)
				packet.append("AH").append(G.getID()).append(';').append(G.getState()).append(";110;1");
			else
				packet.append('|').append(G.getID()).append(';').append(G.getState()).append(";110;1");
			a++;
		}
		packet.append('\000');
		packet.append("AlK").append(level).append('\000');
		packet.append("AQ").append(question.replace(" ", "+"));

		send(out, packet.toString());
	}

	public static void SEND_ALREADY_CONNECTED(final PrintWriter out)
	{
		final String packet = "AlEc";
		send(out, packet);
	}
	
	public static void SEND_WAS_DISCONNECT_ACCOUNT(final PrintWriter out) 
	{
		final String packet = "AlEd";
		send(out, packet);
	}

	public static void SEND_REFRESH_GAMESERVERS(final PrintWriter out)
	{
		final ArrayList<GameServer> list = new ArrayList<GameServer>();
		list.addAll(World.GameServers.values());
		boolean isFirst = true;
		final StringBuilder packet = new StringBuilder();
		for (final GameServer G : list)
		{
			if (isFirst)
			{
				packet.append("AH").append(G.getID()).append(';').append(G.getState()).append(";110;1");
				isFirst = false;
			}
			else
			{
				packet.append('|').append(G.getID()).append(';').append(G.getState()).append(";110;1");
			}
		}
		send(out, packet.toString());
	}

	public static void SEND_PERSO_LIST(final PrintWriter out, final Account account) {
		final StringBuilder packet = new StringBuilder(20);
		final ArrayList<GameServer> list = new ArrayList<GameServer>();
		list.addAll(World.GameServers.values());

		packet.append("AxK31536000000");
		for (final GameServer G : list)
		{
			int numberPlayerOnThisServer = account.getNumberPersosOnThisServer(G.getID());//SQLManager.getNumberPersosOnThisServer(account.getGUID(), G.getID());
			if(numberPlayerOnThisServer > 0) {
				packet.append('|').append(G.getID()).append(',').append(numberPlayerOnThisServer);
			}
		}

		send(out, packet.toString());
	}

	public static void SEND_GAME_SERVER_IP(final PrintWriter out, final int guid, final int server, final String followingIP) {
		final StringBuilder packet = new StringBuilder(20);;
		final GameServer G = World.GameServers.get(server);
		if (G == null)
			return;
		if (G.getState() == 0 || G.getState() == 2 || !G.isConnected())
		{
			REALM_SEND_SERVER_BOX(out, 'd');
			return;
		}
		if (World.getAccountById(guid).getGmLvl() < G.getBlockLevel())
		{
			REALM_SEND_SERVER_BOX(out, 'r');
			return;
		}
		if (G.isVIP() && World.getAccountById(guid).isVip())
		{
			SEND_BOX(out, 20, "", 1);
			return;
		}
		if (G.getPlayers() >= G.getMaxPlayers())
		{
			REALM_SEND_SERVER_BOX(out, 'f');
			return;
		}
		System.out.println("Connection to gameserver with the following IP: " + followingIP);
		Log.addToLoginSockLog("Connection to gameserver with the following IP: " + followingIP);
		//SQLManager.ADD_WAITING_COMPTE(World.getAccountById(guid), G);
		Account acc = World.getAccountById(guid);
		String accountData = String.valueOf(guid) + '|' + acc.getName() + '|' + acc.getPassword() + '|' + acc.getGmLvl() + 
				'|' + acc.getVip() + '|' + acc.getLastIP() + '|' + acc.getLastConnectionDate() + '|' + acc.getQuestion() + 
				'|' + acc.getResponse() + '|' + acc.getNickname() + '|' + acc.isMute() + '|' + acc.getCurIP() +
				'|' + acc.getFriendList() + '|' + acc.getEnemyList() + '|' + acc.getStable() + '|' + acc.getBankItems() +
				'|' + acc.getBankKamas();
		G.getLinkThread().sendAddWaiting(accountData);
		if (World.isIP(G.getIP())) 
			packet.append("AXK").append(CryptManager.cryptIP(G.getIP())).append(CryptManager.cryptPort(G.getPort())).append(guid); 
		else
			packet.append("AYK").append(G.getIP()).append(':').append(G.getPort()).append(';').append(guid);
		send(out, packet.toString());
	}

	public static void SEND_BOX(final PrintWriter out, final int BOXID, final String args, final int forKick)
	{
		final String packet = 'M' + forKick + BOXID + '|' + args;
		send(out, packet);
		if (forKick == 0)
			send(out, "ATE");
	}

	public static void REALM_SEND_SERVER_BOX(final PrintWriter out, final char err) {
		final String toSend = "AXE" + err;
		send(out, toSend);
	}

	public static void REALM_SEND_RESULT_SEARCH(final PrintWriter out, final String preparePacket) {
		send(out, "AF"+preparePacket);
    }

	public static void SEND_SERVER_INDISPONIBLE(final PrintWriter out) {
		final String packet = "AYK0";
		send(out, packet.toString());
    }

	public static void SEND_Af_PACKET(final PrintWriter out, final short position) {
		final String packet = "Af" + position + '|';
		send(out, packet);
    }
	
	public static void SEND_CLOSE_QUEUE(final PrintWriter out) {
		final String packet = "Al";
		send(out, packet);
    }

	public static void SEND_Ad_Ac_AH_AlK_AQ_PACKETS(final PrintWriter out, final String pseudo, final int level, final String question, final int gmlevel)
	{
		final StringBuilder packet = new StringBuilder(20+World.GameServers.size()*10);
		packet.append("Ad").append(pseudo).append('\000');
		packet.append("Ac0").append('\000');
		final ArrayList<GameServer> list = new ArrayList<GameServer>();
		list.addAll(World.GameServers.values());
		boolean isFirst = true;
		for (final GameServer G : list)
		{
			if (gmlevel >= G.getBlockLevel()) 
			{
				if (isFirst)
				{
					packet.append("AH").append(G.getID()).append(';');
					if (G.isConnected())
						packet.append(G.getState());
					else
						packet.append(0);
					packet.append(";110;1");
				}
				else
				{
					packet.append('|').append(G.getID()).append(';');
					if (G.isConnected()) 
						packet.append(G.getState()); 
					else
						packet.append(0);
					packet.append(";110;1");
				}
				isFirst = false;
			}
		}
		packet.append('\000');
		packet.append("AlK").append(level).append('\000');
		packet.append("AQ").append(question.replace(" ", "+"));

		send(out, packet.toString());
	}

	public static void SEND_RECO_WITH_HASHKEY_PACKET(final PrintWriter out, final String HashKey)
	{
		final String packet = "HC" + HashKey;
		send(out, packet);
	}
}

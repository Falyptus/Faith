package login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import link.LinkServer;
import link.LinkThread;
import objects.Account;
import objects.GameServer;

import common.Config;
import common.Log;
import common.Main;
import common.SQLManager;
import common.SocketManager;
import common.World;

public class LoginThread implements Runnable {

	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	private String hashKey;
	private int packetNum = 0;
	private String accountName;
	private String hashPass;
	private Account account;
	public static LinkThread linkThread;
	public static LinkServer linkServer;
	
	public LoginThread(final Socket sock) {
		try 
		{
			socket = sock;
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
		}catch (final IOException e)
		{
			try
			{
				if(!socket.isClosed()) socket.close();
			}catch (IOException e1) {}
		}finally
		{
			if(account != null)
			{
				account.setLoginThread(null);
				World.deleteAccount(account);
			}
		}
    }

	@Override
    public void run() {
		try
		{
			String packet = "";
			final char charCur[] = new char[1];
			SocketManager.SEND_POLICY_FILE(out);

			hashKey = SocketManager.SEND_HC_PACKET(out);
			while (in.read(charCur, 0, 1) != -1 && Main.isRunning)
			{
				if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r') 
				{
					packet += charCur[0];
				}else
				if (packet.compareTo("") != 0) 
				{
					
					if(Log.LOGIN_LOG) Log.addToLoginSockLog("Login: Recv << "+packet);
					if(Config.LOGIN_DEBUG) System.out.println("Login: Recv << "+packet);
					packetNum++;
					
					parsePacket(packet);
					packet = "";
				}
			}
		} catch (final IOException e) {
			try {
				in.close();
				out.close();
				if (account != null) {
					SQLManager.SET_CUR_IP("", account.getGUID());
					account.setLoginThread(null);
					World.deleteAccount(account);
				}
				if (!socket.isClosed())
					socket.close();
			} catch (final IOException e1) {}
		} finally {
			try {
				in.close();
				out.close();
				if (account != null) {
					SQLManager.SET_CUR_IP("", account.getGUID());
					account.setLoginThread(null);
					World.deleteAccount(account);
				}
				if (!socket.isClosed())
					socket.close();
			} catch (final IOException e1) {}
		}
    }

	public void kick() {
	    try {
	    	Main.loginServer.removeClient(this);
			Log.addToLoginSockLog("Client was kicked by the server.");
			in.close();
			out.close();
			if (!socket.isClosed())
				socket.close();
			if (account != null)
			{
				SQLManager.SET_CUR_IP("", account.getGUID());
				World.deleteAccount(account);
			}
		} catch (final IOException e) {
			Log.addToErrorLog("LoginThread Erreur: " + e.getMessage());
			e.printStackTrace();
		}	    
    }
	
	public void closeSocket()
	{
		try {
			this.socket.close();
		} catch (final IOException e) {}
	}
	
	public PrintWriter getOut()
	{
		return out;
	}
	
	public void refreshGameServer()
	{
		SocketManager.SEND_REFRESH_GAMESERVERS(out);
	}
	
	private void parsePacket(final String packet) 
	{
		if (account != null) account.refreshLastPacketTime();
		switch (packetNum)
		{
		case 1:
			if (!packet.equalsIgnoreCase(Config.CLIENT_VERSION) && ! Config.LOGIN_IGNORE_VERSION)
			{
				SocketManager.SEND_REQUIRED_VERSION(out);
				kick();
			}
			break;
		case 2:
			accountName = packet.toLowerCase();
			break;
		case 3:
			if (!packet.substring(0, 2).equalsIgnoreCase("#1"))
			{
				kick();
				return;
			}
			hashPass = packet;
			final Account acc = World.getAccountByName(accountName);
			
			if (acc != null && acc.isValidPass(hashPass, hashKey))
			{
				SocketManager.SEND_ALREADY_CONNECTED(acc.getLoginThread().getOut());
				SocketManager.SEND_WAS_DISCONNECT_ACCOUNT(out);
				return;
			}
			
			if (acc != null && !acc.isValidPass(hashPass, hashKey)) 
			{
				SocketManager.SEND_LOGIN_ERROR(out);
				return;
			}
			
			SQLManager.LOAD_ACCOUNT_BY_USER(accountName);
			account = World.getAccountByName(accountName);
			
			if (account == null) 
			{
				SocketManager.SEND_LOGIN_ERROR(out);
				return;
			}
			
			if(!account.isValidPass(hashPass, hashKey))//Mot de passe invalide
			{
				SocketManager.SEND_LOGIN_ERROR(out);
				return;
			}
			
			if (account.isBanned()) 
			{
				SocketManager.SEND_BANNED(out);
				return;
			}
			
			final String ip = socket.getInetAddress().getHostAddress();

			if (World.isIpBanned(ip))
			{
				SocketManager.SEND_BANNED(out);
				return;
			}
			
			/*final int nbrTimeIpIsConnected = SQLManager.GET_IP_IS_CONNECTED(ip);
			if (nbrTimeIpIsConnected == -1)
				return; 
			if (nbrTimeIpIsConnected >= Config.LOGIN_ACCOUNTS_PER_IP)
			{
				SocketManager.SEND_BOX(out, 34, nbrTimeIpIsConnected + ";" + ip, 0);
				return;
			}
			
			account.setHashKey(hashKey);
			
			if (World.getComptes().size() >= Login.CONNECTION_ACCOUNT_LIMIT * 2)
			{
				SocketManager.SEND_BOX(_out, 16, "", 0);
				try { 
					Thread.sleep(1000); 
					kick(); 
				} catch (InterruptedException ex) {}
				return;
			}
			if (account.getGmLvl() < World._Requirelevel || Main.isVIP && !account.isVip())
			{
				SocketManager.SEND_BOX(out, 19, "", 0);
				return;
			}*/
			
			for (GameServer gameServer : World.GameServers.values())
			{
				if (gameServer.getLinkThread() != null)
					gameServer.getLinkThread().sendDeco(account.getGUID());
			}
			
			if (account.getLoginThread() != null)
			{
				SocketManager.SEND_ALREADY_CONNECTED(out);
				SocketManager.SEND_ALREADY_CONNECTED(account.getLoginThread().getOut());
				return;
			}

			account.setLoginThread(this);
			account.setCurIP(ip);
			SQLManager.SET_CUR_IP(ip, account.getGUID());
			/*if (World.getAccounts().size() > Config.CONNECTION_ACCOUNT_LIMIT)
			{
				/*if (!account.wasReloadedServ())
				{
					account.manageAfPacket();
				}
				else
				{
					SocketManager.SEND_Ad_Ac_AH_AlK_AQ_PACKETS(getOut(), account.getNickname(), account.getGmLvl() > 0 ? 1 : 0, account.getQuestion(), account.getGmLvl());
				}
			} else 
			{
				SocketManager.SEND_Ad_Ac_AH_AlK_AQ_PACKETS(getOut(), account.getNickname(), account.getGmLvl() > 0 ? 1 : 0, account.getQuestion(), account.getGmLvl());
			}*/
			SocketManager.SEND_Ad_Ac_AH_AlK_AQ_PACKETS(out, account.getNickname(), account.getGmLvl() > 0 ? 1 : 0, account.getQuestion(), account.getGmLvl());
			break;
		default:
			final String ip2 = socket.getInetAddress().getHostAddress();
			if (packet.substring(0, 2).equals("Af")) 
			{
				/*if (World.getAccounts().size() > Config.CONNECTION_ACCOUNT_LIMIT)
				{
					if (!account.wasReloadedServ())
					{
						account.manageAfPacket();
					}
					else
					{
						SocketManager.SEND_Af_PACKET(out, (short) -1);
					}
				} else SocketManager.SEND_Af_PACKET(out, (short) -1);*/
				int queueID = 1;
				int position = 1;
				packetNum--;
				SocketManager.SEND_Af_PACKET(out, position, 1, 1, 0, queueID);
			} else if (packet.substring(0, 2).equals("Ax")) 
			{
				if (account == null)
					return;
				/*if (account.isInQueue())
					return;*/
				SocketManager.SEND_PERSO_LIST(out, account); 
			} else if (packet.substring(0, 2).equals("AX"))
			{
				final int number = Integer.parseInt(packet.substring(2, 3));
				SocketManager.SEND_GAME_SERVER_IP(out, account.getGUID(), number, ip2);
			} else 
			{
				if (!packet.substring(0, 2).equals("AF")) break;
                World.searchFriends(out, packet.substring(2));
			}
		}
	}

}

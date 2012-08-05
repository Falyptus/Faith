package server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import objects.account.Account;

import common.Config;
import common.Main;
import common.SocketManager;
import common.World;
import common.console.Console;
import common.console.Log;
import common.utils.CryptManager;

public class LinkServer implements Runnable {

	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private Thread thread;

	public LinkServer() {
		try {
			socket = new Socket(Config.LOGINSERVER_IP, Config.CONFIG_LINK_PORT);
			socket.setReceiveBufferSize(2048);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		} catch (final Exception e) {
			Log.addToErrorLog("Error : Cannot etablish link into LoginServer and GameServer.");
			Console.printlnError(e.getMessage());
			Main.linkIsRunning = false;
			Main.tryLinkServer();
		}
	}

	public void run() {
		try {
			send("KE" + Config.AUTH_KEY);
			String packet = "";
			Main.linkIsRunning = true;
			final char[] charCur = new char[1];
			while(in.read(charCur, 0, 1)!=-1 && Main.isRunning)
	    	{
	    		if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r')
		    	{
	    			packet += charCur[0];
		    	}else if(packet != "")
		    	{
		    		packet = CryptManager.toUnicode(packet);
		    		
	    			Log.addToSockLog("Link: Recv << "+packet);
		    		
		    		parsePacket(packet);
		    		packet = "";
		    	}
	    	}
		}
		catch (final IOException e)
		{
			Log.addToErrorLog("Error : Cannot launch link server");
			Log.addToErrorLog(e.getMessage());
			Main.linkIsRunning = false;
			Main.tryLinkServer();
		}
	}
	
	public void parsePacket(final String packet) {
		switch (packet.charAt(0)) {
		case 'A':
			switch(packet.charAt(1)) {
			case 'W':
				final String[] datas = packet.substring(2).split("\\|");
				final int aId = Integer.parseInt(datas[0]), aGmLvl = Integer.parseInt(datas[3]);
				final String aName = datas[1], aPassword = datas[2], aIp = datas[5], aLastConnectionDate = datas[6], aQuestion = datas[7],
						aResponse = datas[8], aNickname = datas[9], aCurIp = datas[11], aFriendList = datas[12], aEnemyList = datas[13],
						aStable = datas[14], aBankItems = datas[15];
				final boolean aMute = Boolean.parseBoolean(datas[10]), aVip = Boolean.parseBoolean(datas[4]);
				final long aBankKamas = Long.parseLong(datas[16]);
				Account acc = new Account(aId, aName, aPassword, aNickname, aGmLvl, aQuestion, aResponse, aLastConnectionDate, aIp, 
						aCurIp, aMute, aVip, aFriendList, aEnemyList, aStable, aBankItems, aBankKamas);
				if(acc != null && Main.gameServer.getWaitingAccount(acc.getGUID()) == null) {
					Main.gameServer.addWaitingAccount(acc);
				}else if(acc != null && Main.gameServer.getWaitingAccount(acc.getGUID()) != null) {
					Main.gameServer.delWaitingAccount(acc);
					Main.gameServer.addWaitingAccount(acc);
				}
				break;
			}
			break;
		case 'L':
			switch (packet.charAt(1)) {
				case 'I':
					final int guid = Integer.parseInt(packet.substring(2));
					final Account acc = World.getAccount(guid);
					if (acc != null && acc.getGameThread() != null) {
						SocketManager.SEND_ALREADY_CONNECTED(acc.getGameThread().getOut());
						acc.getGameThread().kick();
					}
				break;
			}
		break;
		case 'S': //S
			switch (packet.charAt(2)) {
				case 'P':
					switch (packet.charAt(3)) {
						case 'T':
							sizePlayers(World.getOnlinePersos().size());
						break;
					}
				break;
			}
		break;
		}
	}
	
	private void sizePlayers(final int sizeP) {
		send("SPT" + sizeP);
    }

	public void sendChangeState(final char c) {
		send("S" + c);
		World._stateID = c;
	}

	public void sendChangeVIP(final byte isVIP) {
		send("RV" + isVIP);
	}

	public void addBanIP(final String ip) {
		send("RA" + ip);
	}

	public void lockGMlevel(final int level) {
		send("RG" + level);
		World._Reqlevel = level;
	}
	
	public void addCharacter(final int accountId) {
		send("KA"+Config.SERVER_ID+"|"+accountId);
	}
	
	public void delCharacter(final int accountId) {
		send("KD"+Config.SERVER_ID+"|"+accountId);
	}
	
	private void send(String data) {
		out.print(data + (char)0x00);
		out.flush();
		Console.printlnDefault("Link: Send>> "+data);
	}
}

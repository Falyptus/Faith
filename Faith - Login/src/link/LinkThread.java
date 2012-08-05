package link;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import common.Config;
import common.Log;
import common.Main;
import common.SQLManager;
import common.World;

import objects.Account;
import objects.GameServer;

public class LinkThread implements Runnable {

	private BufferedReader in;
	private Thread thread;
	private PrintWriter out;
	private Socket socket;
	private GameServer gameServer = null;
	
	public LinkThread(final Socket sock) {
		try {
			socket = sock;
			socket.setSendBufferSize(2048);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream());
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		} catch (final IOException e) {
			Log.addToErrorLog("LinkThread Error: " + e.getMessage());
			try {
				if (!socket.isClosed())
					socket.close();
				e.printStackTrace();
			} catch (final IOException e1) {
				Log.addToErrorLog("LinkThread Error: " + e1.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	@Override
    public void run() {
		try {
			String packet = "";
			final char charCur[] = new char[1];
			while(in.read(charCur, 0, 1)!=-1 && Main.isRunning)
	    	{
				if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r')
				{
					packet += charCur[0];
    			
				}else if(!packet.isEmpty())
				{
	    		
					if(Config.LOGIN_DEBUG) System.out.println("Link: Recv << " + packet);
					parsePacket(packet);
					packet = "";
				}
	    	}
		} catch (final IOException e) {
			Log.addToErrorLog("LoginThread Error: " + e.getMessage());
			try {
				in.close();
				out.close();
				if (gameServer != null)
				{
					gameServer.setState(0);
					for (final Account acc : World.getAccounts().values())
					{
						if(acc.getLoginThread() == null) continue;
						acc.getLoginThread().refreshGameServer();
					}
					gameServer.setLinkThread(null);	
				}
				if (!socket.isClosed())
					socket.close();
				thread.interrupt();
			} catch (final IOException e1) {}
		} finally {
			try {
				in.close();
				out.close();
				if (gameServer != null)
				{
					gameServer.setState(0);
					for (final Account acc : World.getAccounts().values())
					{
						if(acc.getLoginThread() == null) continue;
						acc.getLoginThread().refreshGameServer();
					}
					gameServer.setLinkThread(null);	
				}
				if (!socket.isClosed())
					socket.close();
				thread.interrupt();
			} catch (final IOException e1) {}
		}
    }
	
	public void parsePacket(final String packet)
	{
		switch (packet.charAt(0))
		{
		case 'K':
			switch (packet.charAt(1))
			{
			case 'A':
				String[] data = packet.substring(2).split("\\|");
				int serverId = Integer.parseInt(data[0]);
				int accountId = Integer.parseInt(data[1]);
				World.getAccountById(accountId).addCharacter(serverId);
				break;
				
			case 'D':
				data = packet.substring(2).split("\\|");
				serverId = Integer.parseInt(data[0]);
				accountId = Integer.parseInt(data[1]);
				World.getAccountById(accountId).delCharacter(serverId);
				break;	
				
			case 'E':
				System.out.println("Packet KE received");
				final String key = packet.substring(2);
				for (final GameServer G : World.GameServers.values())
				{
					if (key.equalsIgnoreCase(G.getKey()))
						gameServer = G;
				}
				if (gameServer == null)
				{
					brokeLink();
					return;
				}
				gameServer.setLinkThread(this);
				gameServer.setConnected(true);
				gameServer.setState(1);
				System.out.println("Server "+gameServer.getName()+" connected !");
				break;
			}
			break;
			
		case 'S'://State
			if (gameServer == null)
			{
				brokeLink();
				return;
			}
			switch (packet.charAt(1))
			{
			case 'O':
				gameServer.setState(1);
				break;
			case 'S':
				gameServer.setState(2);
				break;
			case 'D':
				gameServer.setState(0);
				break;
			}
			break;
			
		case 'R': //Restriction
            if(gameServer == null)
            {
                brokeLink();
                return;
            }
            switch(packet.charAt(1))
            {
            case 'G':
            	int gmLvl = Integer.parseInt(packet.substring(2));
            	Log.addToLinkLog("LinkThread: Packet RG received, server block access for GM level < "+gmLvl);
                gameServer.setBlockLevel(gmLvl);
                break;

            case 'V':
            	boolean acceptNonVip = !packet.substring(2).equalsIgnoreCase("1");
            	Log.addToLinkLog("LinkThread: Packet RV received, server "+(acceptNonVip ? "accept " : "block ")+"for account non-VIP");
                gameServer.setVIP(!acceptNonVip);
                break;

            case 'A':
            	String ip = packet.substring(2);
            	Log.addToLinkLog("LinkThread: Packet RA received, server ban this IP: "+ip);
                SQLManager.ADD_BANIP(ip);
                break;
            }
            break;
		}

		if(World.getAccounts().size() >0)
		{
			for (final Account acc : World.getAccounts().values())
			{
				if(acc.getLoginThread() == null) continue;
				acc.getLoginThread().refreshGameServer();
			}
		}
	}
	
	public void brokeLink()
	{
		try {
			Log.addToLoginSockLog("LinkThread: GameServer has broken the link.");
			in.close();
			out.close();
			if (gameServer != null)
			{
				gameServer.setState(0);
				for (final Account acc : World.getAccounts().values())
				{
					if(acc.getLoginThread() == null) continue;
					acc.getLoginThread().refreshGameServer();
				}
				gameServer.setConnected(false);
				gameServer.setLinkThread(null);
			}
			if (!socket.isClosed())
				socket.close();
		} catch (final IOException e) {
			Log.addToErrorLog("LoginThread Error: " + e.getMessage());
		}
	}
	
	public void sendAddWaiting(final String accountData) {
		send("AW" + accountData + (char) 0x00);
	}

	public void sendDeco(final int guid) {
		send("LI" + guid + (char) 0x00);
    }
	
	public void send(Object obj) {
		out.print(obj);
		out.flush();
		if(Config.LOGIN_DEBUG) {
			System.out.println("Link: Send >> "+obj);
		}
	}
	
}

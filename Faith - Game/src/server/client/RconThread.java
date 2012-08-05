package server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import objects.character.Player;
import server.RconServer;

import common.Config;
import common.Main;
import common.SQLManager;
import common.SocketManager;
import common.World;

public class RconThread implements Runnable {

	private BufferedReader input;
	private Thread thread;
	private Socket socket;
	private boolean allowExecute = false;
	
	public RconThread(final Socket sock)
	{
		try
		{
			socket = sock;
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
		catch(final IOException e)
		{
			try {
				if(!socket.isClosed())socket.close();
			} catch (final IOException e1) {}
		}
	}

	@Override
    public void run() {	
		try
    	{
			String packet = "";
			
			final char charCur[] = new char[1];
	        
	    	while(input.read(charCur, 0, 1)!=-1 && Main.isRunning)
	    	{
	    		if (charCur[0] != ';' && charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r')
		    	{
	    			packet += charCur[0];
		    	}else if(packet != "")
		    	{
		    		RconServer.addToSockLog("Rcon: Recv << "+packet);
		    		parsePacket(packet);
		    		packet = "";
		    	}
	    	}
    	}catch(final IOException e)
    	{
    		try
    		{
	    		input.close();
	    		if(!socket.isClosed())socket.close();
	    		thread.interrupt();
	    	}catch(final IOException e1){};
    	}
    	finally
    	{
    		try
    		{
	    		input.close();
	    		if(!socket.isClosed())socket.close();
	    		thread.interrupt();
	    	}catch(final IOException e1){};
    	}
    }

	private void parsePacket(final String packet) { //TODO: Add some action... :)
		if(packet.isEmpty())
			return;
		final String[] data = packet.substring(2).split(":");
		if(packet.startsWith("AU"))//Authentification...
		{
			if(Config.CONFIG_KEY_RCON == packet.substring(2))
			{
				allowExecute = true;
			}
		}
		else if(packet.startsWith("WI"))//World interaction
		{
			if(!allowExecute)
			{
				System.out.println("Rcon: IP "+socket.getInetAddress().getHostAddress()+" is not allowed to execute cmd");
				return;
			}
			if(data[0].equalsIgnoreCase("save"))
			{
				if(!Main.isSaving)
				{
					SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1164");
					System.out.println("Save is running !");
					World.saveAll(null);
					System.out.println("Save is finish !");
					SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1165");
				}
				else
				{
					System.out.println("An instance of save is currently running !");
					return;
				}
			}
			else if(data[0].equalsIgnoreCase("reboot"))
			{
				parsePacket("WIsave"); //We save the world
				Main.closeServers();
			}
			else if(data[0].equalsIgnoreCase("kick"))
			{
				final Player temp = World.getPersoByName(data[1]);
				if(temp == null)
				{
					System.out.println("Character not loaded !");
					return;
				}
				temp.getAccount().getGameThread().kick();
				System.out.println("Character kick !");
			}
			else if(data[0].equalsIgnoreCase("ban"))
			{
				final Player temp = World.getPersoByName(data[1]);
				if(temp == null)
				{
					System.out.println("Character not loaded !");
					return;
				}
				temp.getAccount().setBanned(true);
				SQLManager.UPDATE_ACCOUNT_DATA(temp.getAccount());
				temp.getAccount().getGameThread().kick();
				System.out.println("Character banned !");
			}
			else if(data[0].equalsIgnoreCase("announce"))
			{
				SocketManager.GAME_SEND_IM_116_TO_ALL("(Rcon System)", data[1]);
			}
			else
			{
				System.out.println("Rcon: IP "+socket.getInetAddress().getHostAddress()+" send an unknown action "+data[0]);
			}
	    }
		else
		{
			System.out.println("Rcon: IP "+socket.getInetAddress().getHostAddress()+" send an unknown packet "+packet);
		}
    }
}

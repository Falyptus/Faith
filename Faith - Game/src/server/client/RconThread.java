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
		final String[] packets = packet.substring(2).split(":");
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
				System.out.println(new StringBuilder("Rcon: IP ").append(socket.getInetAddress().getHostAddress()).append(" is not allowed to execute cmd").toString());
				return;
			}
			if(packets[0].equalsIgnoreCase("save"))
			{
				if(!Main.isSaving)
				{
					SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1164");
					System.out.println(new StringBuilder("Sauvegarde lancée !").toString());
					World.saveAll(null);
					System.out.println(new StringBuilder("Sauvegarde finie !").toString());
					SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1165");
				}
				else
				{
					System.out.println(new StringBuilder("Une instance de sauvegarde est déjà lancée !").toString());
					return;
				}
			}
			else if(packets[0].equalsIgnoreCase("reboot"))
			{
				parsePacket("WIsave"); //We save the world
				System.exit(0); //We exit the VM
			}
			else if(packets[0].equalsIgnoreCase("kick"))
			{
				final Player temp = World.getPersoByName(packets[1]);
				if(temp == null)
				{
					System.out.println(new StringBuilder("Personnage non chargé !").toString());
					return;
				}
				//SocketManager.REALM_SEND_KICKED(temp.get_compte().getGameThread().get_out());
				temp.getAccount().getGameThread().kick();
				System.out.println(new StringBuilder("Personnage kick !").toString());
			}
			else if(packets[0].equalsIgnoreCase("ban"))
			{
				final Player temp = World.getPersoByName(packets[1]);
				if(temp == null)
				{
					System.out.println(new StringBuilder("Personnage non chargé !").toString());
					return;
				}
				temp.getAccount().setBanned(true);
				//SocketManager.REALM_SEND_BANNED(temp.get_compte().getGameThread().get_out());
				SQLManager.UPDATE_ACCOUNT_DATA(temp.getAccount());
				temp.getAccount().getGameThread().kick();
				System.out.println(new StringBuilder("Personnage banni !").toString());
			}
			else if(packets[0].equalsIgnoreCase("announce"))
			{
				SocketManager.GAME_SEND_MESSAGE_TO_ALL((new StringBuilder("(Rcon System) : ")).append(packets[1]).toString(), Config.CONFIG_MOTD_COLOR);
			}
			else
			{
				System.out.println(new StringBuilder("Rcon: IP ").append(socket.getInetAddress().getHostAddress()).append(" send an unknow packet ").append(packet).toString());
			}
	    }
		else
		{
			System.out.println(new StringBuilder("Rcon: IP ").append(socket.getInetAddress().getHostAddress()).append(" send an unknow packet ").append(packet).toString());
		}
    }
}

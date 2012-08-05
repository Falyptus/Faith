package common.command;

import java.util.Scanner;

import common.Config;
import common.SocketManager;
import common.World;
import common.utils.Utils;

public class ConsoleCommand 
{
	
	private final Scanner scanner;
	private String cmd;
	
	public ConsoleCommand()
	{
		this.scanner = new Scanner(System.in);
	}
	
	public void launch()
	{
		System.out.print("GameServer> ");
		this.cmd = this.scanner.nextLine();
		this.processCmd();
	}
	
	public void processCmd()
	{
		final String[] infos = cmd.split(" ");
		final String command = infos[0];
		final boolean help = infos.length > 1 && infos[1] == "/?";
		if(command.isEmpty())
		{
			return;
		}
		else if(command.equalsIgnoreCase("save"))
		{
			final long t = System.currentTimeMillis();
			System.out.println("Sauvegarde lancée.");
			SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1164");
			World.saveAll(null);
			SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1165");
			System.out.println("Sauvegarde exécutée en " + (System.nanoTime()-t) + "ms.");
		}
		else if(command.equalsIgnoreCase("shutdown"))
		{
			System.out.println("Le serveur va s'éteindre.");
			System.exit(1);
		}
		else if(command.equalsIgnoreCase("state"))
		{
			if(help)
			{
				System.out.println("Commande sous la forme state <arg>.\n<arg> = (0: serveur fermé, 1: serveur ouvert, 2: serveur en sauvegarde.");
				return;
			}
			final String state = infos[1];
			if(state.isEmpty() || !state.equalsIgnoreCase("0") || !state.equalsIgnoreCase("1") || !state.equalsIgnoreCase("2"))
			{
				System.out.println("Argument 1 incorrect.\nPour avoir des renseignement sur cette commande taper : state /?");
				return;
			}
			World.setState(Integer.parseInt(state));
		}
		else if(command.equalsIgnoreCase("debug"))
		{
			Config.CONFIG_DEBUG = !Config.CONFIG_DEBUG;
			Utils.refreshTitle();
			System.out.println("Le mode débug est "+(Config.CONFIG_DEBUG?"activé":"désactivé."));
		}
		else if(command.equalsIgnoreCase("reloadconfig"))
		{
			System.out.println("Rechargement de la configuration...");
			Config.loadConfiguration();
			System.out.println("Configuration rechargé !");
		}
		else if(command.equalsIgnoreCase("help"))
		{
			System.out.println("Commandes disponibles:\n- save: Sauvegarde les données" +
					"\n- shutdown: Extinction du serveur.\n- state <arg>: Modifie l'état du serveur." +
					"\n- debug: Active le mode débug du serveur.\n- reloadconfig: Recharge la configuration.");
		}
		else
		{
			System.out.println("Commande incorrect taper \"help\" pour connaître les commandes.");
		}
	}
}
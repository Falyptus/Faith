package common.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import objects.account.Account;
import objects.action.Action;
import objects.bigstore.BigStoreEntry;
import objects.character.Player;
import objects.item.Item;
import objects.item.ItemTemplate;
import objects.item.SoulStone;
import objects.job.JobStat;
import objects.map.DofusMap;
import objects.map.MountPark;
import objects.monster.MonsterGroup;
import objects.npc.Npc;
import objects.npc.NpcQuestion;
import objects.npc.NpcResponse;
import objects.npc.NpcTemplate;
import objects.spell.SpellEffect;
import server.client.GameThread;
import server.task.SaveTask;

import common.Config;
import common.Constants;
import common.Main;
import common.SQLManager;
import common.SocketManager;
import common.World;
import common.World.ItemSet;
import common.console.Log;
import common.utils.CryptManager;
import common.utils.Formulas;

public class Command {
	private final Account account;
	private final Player player;
	private final PrintWriter out;
	
	public Command(final Player perso)
	{
		this.account = perso.getAccount();
		this.player = perso;
		this.out = account.getGameThread().getOut();
	}
	
	public void consoleCommand(final String packet)
	{
		if(account.getGmLvl() == 0)
		{
			account.getGameThread().closeSocket();
			return;
		}
		
		final String msg = packet.substring(2);
		String[] infos = msg.split(" ");
		if(infos.length == 0)return;
		final String command = infos[0];
		
		if(Log.canLog)
		{
			Log.addToMjLog(account.getCurIP()+": "+account.getName()+" "+player.getName()+"=>"+msg);
		}
		if(command.equalsIgnoreCase("EXIT"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			System.exit(0);
		}else if(command.equalsIgnoreCase("RESETSAVE"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			try
			{
				World.resetSave();
			}catch(final Exception e)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Erreur! :"+e.getMessage());
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, e.getStackTrace()+"");
				return;
			}
			
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Variable de sauvegarde reseté!");
			return;
			
		}else if(command.equalsIgnoreCase("SENDPACKET"))
		{
			SocketManager.send(player, infos[1]);
			if(Config.CONFIG_DEBUG)
				Log.addToSockLog("Game: Send>>"+infos[1]);
			return;
		}else if(command.equalsIgnoreCase("FORGETSPELL"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			
			Player perso;
			if(infos.length >= 2)
				perso = World.getPersoByName(infos[1]);
			else
				perso = player;
			
			if(perso == null)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Personnage '" + infos[1] + "' introuvable!");
				return;
			}
			
			perso.setIsForgetingSpell(true);
			SocketManager.GAME_SEND_FORGETSPELL_INTERFACE('+', perso);
			
			if(perso.isForgetingSpell())
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Interface d'oubli de sort ouvert");
			}
			return;
			
		}else if(command.equalsIgnoreCase("FULLHDV"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			
			fullHdv(Integer.parseInt(infos[1]));
			return;
			
		}else if(command.equalsIgnoreCase("SETGUILDRANK"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			Player toChange = null;
			
			if(infos.length > 3)
			{
				toChange = World.getPersoByName(infos[3]);
			}
			else
			{
				toChange = player;
			}
			
			if(toChange == null)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Personnage non trouvé dans la mémoire!");
				return;
			}
			else if(toChange.getGuildMember() == null)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Le personnage n'a pas de guilde");
				return;
			}
			
			int rank;
			int right;
			try
			{
				rank = Integer.parseInt(infos[1]);
				right = Integer.parseInt(infos[2]);
			}catch(final NumberFormatException e)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Valeurs invalide!");
				return;
			}
			
			toChange.getGuildMember().setAllRights(rank, (byte) -1, right);
			SocketManager.GAME_SEND_gS_PACKET(toChange,toChange.getGuildMember());
			
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Rang correctement changé!");
			return;
			
		}else if(command.equalsIgnoreCase("SPAWNMOB"))//Format : SPAWNMOB id,lvlMin,lvlMax;id,lvlMin,lvlMax... CONDITIONS(Facultatif)
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			
			final String groupData = infos[1];
			String cond = "";
			if(infos.length > 2)
				cond = infos[2];
			
			player.getCurMap().spawnGroup(false, false, player.getCurCell().getId(), groupData, cond);
			return;
			
		}else if(command.equalsIgnoreCase("RELOADCONFIG"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			
			Config.loadConfiguration();
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Fichier de configuration rechargé!");
			return;
			
		}else if(command.equalsIgnoreCase("GENPIERRE"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			final SoulStone pierre = new SoulStone(World.getNewItemGuid(), 1, 7010, -1, infos[1]);
			if(player.addItem(pierre, false))
				World.addItem(pierre, true);
			player.itemLog(pierre.getTemplate().getID(), 1, "Ajouté par un MJ");
			
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Pierre créé");
			return;
		}
		else
		if(command.equalsIgnoreCase("INFOS"))
		{
			long uptime = System.currentTimeMillis() - Main.gameServer.getStartTime();
			final int jour = (int) (uptime/(1000*3600*24));
			uptime %= (1000*3600*24);
			final int hour = (int) (uptime/(1000*3600));
			uptime %= (1000*3600);
			final int min = (int) (uptime/(1000*60));
			uptime %= (1000*60);
			final int sec = (int) (uptime/(1000));
			
			final StringBuilder mess = new StringBuilder("===========\n")
			.append("Faith v. ").append(Constants.SERVER_VERSION).append(" by ").append(Constants.SERVER_MAKER).append("\n")
			.append("\n")
			.append("Uptime: ").append(jour).append("d ").append(hour).append("h ").append(min).append("m ").append(sec).append("s\n")
			.append("Joueurs en lignes: ").append(Main.gameServer.getPlayerNumber()).append("\n")
			.append("Record de connexion: ").append(Main.gameServer.getMaxPlayer()).append("\n")
			.append("===========");
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess.toString());
			return;
		}else
		if(command.equalsIgnoreCase("REFRESHMOBS"))
		{
			player.getCurMap().refreshSpawns();
			final String mess = "Mob Spawn refreshed!";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			return;
		}
		else
		if(command.equalsIgnoreCase("SAVE") && !Main.isSaving)
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			final Thread t = new Thread(new SaveTask());
			t.start();
			final String mess = "Sauvegarde lancee!";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			return;
		}
		else
		if(command.equalsIgnoreCase("MAPINFO"))
		{
			String mess = 	"==========\n"
						+	"Liste des Npcs de la carte:";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			final DofusMap map = player.getCurMap();
			for(final Entry<Integer,Npc> entry : map.get_npcs().entrySet())
			{
				mess = entry.getKey()+" "+entry.getValue().getTemplate().getId()+" "+entry.getValue().getCellId()+" "+entry.getValue().getTemplate().get_initQuestionID();
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			}
			mess = "Liste des groupes de monstres:";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			for(final Entry<Integer,MonsterGroup> entry : map.getMobGroups().entrySet())
			{
				mess = entry.getKey()+" "+entry.getValue().getCellId()+" "+entry.getValue().getAlignement()+" "+entry.getValue().getSize();
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			}
			mess = "==========";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			return;
		}
		else
		if(command.equalsIgnoreCase("WHO"))
		{
			String mess = 	"==========\n"
				+			"Liste des joueurs en ligne:";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			final int diff = Main.gameServer.getClients().size() -  30;
			for(byte b = 0; b < 30; b++)
			{
				if(b == Main.gameServer.getClients().size())break;
				final GameThread GT = Main.gameServer.getClients().get(b);
				final Player P = GT.getPerso();
				if(P == null)continue;
				mess = P.getName()+"("+P.getActorId()+") ";
				
				switch(P.getBreedId())
				{
					case Constants.CLASS_FECA:
						mess += "Fec";
					break;
					case Constants.CLASS_OSAMODAS:
						mess += "Osa";
					break;
					case Constants.CLASS_ENUTROF:
						mess += "Enu";
					break;
					case Constants.CLASS_SRAM:
						mess += "Sra";
					break;
					case Constants.CLASS_XELOR:
						mess += "Xel";
					break;
					case Constants.CLASS_ECAFLIP:
						mess += "Eca";
					break;
					case Constants.CLASS_ENIRIPSA:
						mess += "Eni";
					break;
					case Constants.CLASS_IOP:
						mess += "Iop";
					break;
					case Constants.CLASS_CRA:
						mess += "Cra";
					break;
					case Constants.CLASS_SADIDA:
						mess += "Sad";
					break;
					case Constants.CLASS_SACRIEUR:
						mess += "Sac";
					break;
					case Constants.CLASS_PANDAWA:
						mess += "Pan";
					break;
					default:
						mess += "Unk";
				}
				mess += " ";
				mess += (P.getSexe()==0?"M":"F")+" ";
				mess += P.getLvl()+" ";
				mess += P.getCurMap().getId()+"("+P.getCurMap().getX()+"/"+P.getCurMap().getY()+") ";
				mess += P.getFight()==null?"":"Combat ";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			}
			if(diff >0)
			{
				mess = 	"Et "+diff+" autres personnages";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			}
			mess = 	"==========\n";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			return;
		}
		else
		if(command.equalsIgnoreCase("SHOWFIGHTPOS"))
		{
			String mess = "Liste des StartCell [teamID][cellID]:";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			final String places = player.getCurMap().getPlacesStr();
			if(places.indexOf('|') == -1 || places.length() <2)
			{
				mess = "Les places n'ont pas ete definies";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
				return;
			}
			String team0 = "",team1 = "";
			final String[] p = places.split("\\|");
			try
			{
				team0 = p[0];
			}catch(final Exception e){};
			try
			{
				team1 = p[1];
			}catch(final Exception e){};
			mess = "Team 0:\n";
			boolean isFirst = true;
			for(int a = 0;a <= team0.length()-2; a+=2)
			{
				if(!isFirst)
					mess += ", ";
				final String code = team0.substring(a,a+2);
				mess += CryptManager.cellCodeToID(code);
				
				isFirst = false;
			}
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			mess = "Team 1:\n";
			isFirst = true;
			for(int a = 0;a <= team1.length()-2; a+=2)
			{
				if(!isFirst)
					mess += ", ";
				
				final String code = team1.substring(a,a+2);
				mess += CryptManager.cellCodeToID(code);
				
				isFirst = false;
			}
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, mess);
			return;
		}else
		if(command.equalsIgnoreCase("DELFIGHTPOS"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int cell = -1;
			try
			{
				cell = Integer.parseInt(infos[2]);
			}catch(final Exception e){};
			if(cell < 0 || player.getCurMap().getCell(cell) == null)
			{
				cell = player.getCurCell().getId();
			}
			final String places = player.getCurMap().getPlacesStr();
			final String[] p = places.split("\\|");
			String newPlaces = "";
			String team0 = "",team1 = "";
			try
			{
				team0 = p[0];
			}catch(final Exception e){};
			try
			{
				team1 = p[1];
			}catch(final Exception e){};
			
			for(int a = 0;a<=team0.length()-2;a+=2)
			{
				final String c = p[0].substring(a,a+2);
				if(cell == CryptManager.cellCodeToID(c))continue;
				newPlaces += c;
			}
			newPlaces += "|";
			for(int a = 0;a<=team1.length()-2;a+=2)
			{
				final String c = p[1].substring(a,a+2);
				if(cell == CryptManager.cellCodeToID(c))continue;
				newPlaces += c;
			}
			player.getCurMap().setPlaces(newPlaces);
			if(!SQLManager.SAVE_MAP_DATA(player.getCurMap()))return;
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,"Les places ont ete modifiees ("+newPlaces+")");
			return;
		}
		else
		if(command.equalsIgnoreCase("CREATEGUILD"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			Player perso = player;
			if(infos.length >1)
			{
				perso = World.getPersoByName(infos[1]);
			}
			if(perso == null)
			{
				final String mess = "Le personnage n'existe pas.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			
			if(!perso.isOnline())
			{
				final String mess = "Le personnage "+perso.getName()+" n'etait pas connecte";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			if(perso.getGuild() != null || perso.getGuildMember() != null)
			{
				final String mess = "Le personnage "+perso.getName()+" a deja une guilde";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			SocketManager.GAME_SEND_gn_PACKET(perso);
			final String mess = perso.getName()+": Panneau de creation de guilde ouvert";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			return;
		}
		else
		if(command.equalsIgnoreCase("TOOGLEAGGRO"))
		{
			Player perso = player;
			final String name = infos[1];
			
			perso = World.getPersoByName(name);
			if(perso == null)
			{
				final String mess = "Le personnage n'existe pas.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			
			perso.setCanAggro(!perso.canAggro());
			String mess = perso.getName();
			if(perso.canAggro()) mess += " peut maintenant etre aggresser";
			else mess += " ne peut plus etre agresser";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			
			if(!perso.isOnline())
			{
				mess = "(Le personnage "+perso.getName()+" n'etait pas connecte)";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			}
		}
		else
		if(infos.length <2 && !infos[0].equalsIgnoreCase("LISTFILE"))
		{
			final String mess = "Commande non reconnue ou incomplete";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			return;
		}
		//Commandes avec 1 argument
		infos = msg.split(" ");

		if(command.equalsIgnoreCase("ANNOUNCE"))
		{
			infos = msg.split(" ",2);
			SocketManager.GAME_SEND_MESSAGE_TO_ALL(infos[1], Config.CONFIG_MOTD_COLOR);
			return;
		}
		else
		if(command.equalsIgnoreCase("NAMEANNOUNCE"))
		{
			infos = msg.split(" ",2);
			final String prefix = "["+player.getName()+"]";
			SocketManager.GAME_SEND_MESSAGE_TO_ALL(prefix+infos[1], Config.CONFIG_MOTD_COLOR);
			return;
		}
		else
		if(command.equalsIgnoreCase("BAN"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			final Player P = World.getPersoByName(infos[1]);
			Account c;
			if(P == null)	//Si le personnage est introuvable dans la mémoire
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Personnage non trouvé dans la mémoire\nRecherche dans la BD...");
				final int accID = SQLManager.LOAD_ACCOUNT_BY_PERSO(infos[1]);
				
				c = World.getAccount(accID);
			}else
			{
				c = P.getAccount();
			}
			
			if(c == null)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Personnage introuvable");
				return;
			}
			P.getAccount().setBanned(true);
			SQLManager.UPDATE_ACCOUNT_DATA(P.getAccount());
			if(c.getGameThread() != null)
				c.getGameThread().kick();
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous avez banni "+P.getName());
			return;
		}
		else
		if(command.equalsIgnoreCase("UNBAN"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			Account c = World.getCompteByName(infos[1]);
			if(c == null)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Compte du personnage non trouvé dans la mémoire\nRecherche dans la BD...");
				
				final int accID = SQLManager.LOAD_ACCOUNT_BY_PERSO(infos[1]);
				c = World.getAccount(accID);
				
				if(c == null)
				{
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Compte du personnage non trouvé");
					return;
				}
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Compte trouvé!");
			}
			c.setBanned(false);
			SQLManager.UPDATE_ACCOUNT_DATA(c);
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous avez debanni le compte '"+c.getName()+"'");
			return;
		}
		else
		if(command.equalsIgnoreCase("ADDFIGHTPOS"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int team = -1;
			int cell = -1;
			try
			{
				team = Integer.parseInt(infos[1]);
				cell = Integer.parseInt(infos[2]);
			}catch(final Exception e){};
			if( team < 0 || team>1)
			{
				final String str = "Team ou cellID incorects";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			if(cell <0 || player.getCurMap().getCell(cell) == null || !player.getCurMap().getCell(cell).isWalkable(true))
			{
				cell = player.getCurCell().getId();
			}
			final String places = player.getCurMap().getPlacesStr();
			final String[] p = places.split("\\|");
			boolean already = false;
			String team0 = "",team1 = "";
			try
			{
				team0 = p[0];
			}catch(final Exception e){};
			try
			{
				team1 = p[1];
			}catch(final Exception e){};
			
			//Si case déjà utilisée
			System.out.println("0 => "+team0+"\n1 =>"+team1+"\nCell: "+CryptManager.cellIDToCode(cell));
			for(int a = 0; a <= team0.length()-2;a+=2)if(cell == CryptManager.cellCodeToID(team0.substring(a,a+2)))already = true;
			for(int a = 0; a <= team1.length()-2;a+=2)if(cell == CryptManager.cellCodeToID(team1.substring(a,a+2)))already = true;
			if(already)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,"La case est deja dans la liste");
				return;
			}
			if(team == 0)team0 += CryptManager.cellIDToCode(cell);
			else if(team == 1)team1 += CryptManager.cellIDToCode(cell);
			
			final String newPlaces = team0+"|"+team1;
			
			player.getCurMap().setPlaces(newPlaces);
			if(!SQLManager.SAVE_MAP_DATA(player.getCurMap()))return;
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,"Les places ont ete modifiees ("+newPlaces+")");
			return;
		}
		else
		if(command.equalsIgnoreCase("SETMAXGROUP"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			infos = msg.split(" ",4);
			int id = -1;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(id == -1)
			{
				final String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			String mess = "Le nombre de groupe a ete fixe";
			player.getCurMap().setMaxGroup(id);
			final boolean ok = SQLManager.SAVE_MAP_DATA(player.getCurMap());
			if(ok)mess += " et a ete sauvegarder a la BDD";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
		}else
		if(command.equalsIgnoreCase("ADDREPONSEACTION"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			infos = msg.split(" ",4);
			int id = -30;
			int repID = 0;
			final String args = infos[3];
			try
			{
				repID = Integer.parseInt(infos[1]);
				id = Integer.parseInt(infos[2]);
			}catch(final Exception e){};
			final NpcResponse rep = World.getNPCreponse(repID);
			if(id == -30 || rep == null)
			{
				final String str = "Au moins une des valeur est invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			String mess = "L'action a ete ajoute";
			
			rep.addAction(new Action(id,args,""));
			final boolean ok = SQLManager.ADD_REPONSEACTION(repID,id,args);
			if(ok)mess += " et ajoute a la BDD";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
		}else
		if(command.equalsIgnoreCase("SETINITQUESTION"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			infos = msg.split(" ",4);
			int id = -30;
			int q = 0;
			try
			{
				q = Integer.parseInt(infos[2]);
				id = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(id == -30)
			{
				final String str = "NpcID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			String mess = "L'action a ete ajoute";
			final NpcTemplate npc = World.getNPCTemplate(id);
			
			npc.setInitQuestion(q);
			final boolean ok = SQLManager.UPDATE_INITQUESTION(id,q);
			if(ok)mess += " et ajoute a la BDD";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
		}else
		if(command.equalsIgnoreCase("ADDENDFIGHTACTION"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			infos = msg.split(" ",4);
			int id = -30;
			int type = 0;
			final String args = infos[3];
			final String cond = infos[4];
			try
			{
				type = Integer.parseInt(infos[1]);
				id = Integer.parseInt(infos[2]);
				
			}catch(final Exception e){};
			if(id == -30)
			{
				final String str = "Au moins une des valeur est invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			String mess = "L'action a ete ajoute";
			player.getCurMap().addEndFightAction(type, new Action(id,args,cond));
			final boolean ok = SQLManager.ADD_ENDFIGHTACTION(player.getCurMap().getId(),type,id,args,cond);
			if(ok)mess += " et ajoute a la BDD";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			return;
		}else
		if(command.equalsIgnoreCase("MUTE"))
		{
			if(account.getGmLvl() < 1)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			Player perso = player;
			final String name = infos[1];
			int time = 0;
			try
			{
				time = Integer.parseInt(infos[2]);
			}catch(final Exception e){};
			
			perso = World.getPersoByName(name);
			if(perso == null || time < 0)
			{
				final String mess = "Le personnage n'existe pas ou la durée est invalide.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			String mess = "Vous avez mute "+perso.getName()+" pour "+time+" secondes";
			if(perso.getAccount() == null)
			{
				mess = "(Le personnage "+perso.getName()+" n'est pas connecté.)";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			perso.getAccount().mute(true,time);
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			
			if(!perso.isOnline())
			{
				mess = "(Le personnage "+perso.getName()+" n'est pas connecté.)";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			}else
			{
				SocketManager.GAME_SEND_Im_PACKET(perso, "1124;"+time);
			}
			return;
		}
		else
		if(command.equalsIgnoreCase("UNMUTE"))
		{
			Player perso = player;
			final String name = infos[1];
			
			perso = World.getPersoByName(name);
			if(perso == null)
			{
				final String mess = "Le personnage n'existe pas.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			
			perso.getAccount().mute(false,0);
			String mess = "Vous avez unmute "+perso.getName();
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			
			if(!perso.isOnline())
			{
				mess = "(Le personnage "+perso.getName()+" n'est pas connecté.)";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			}
		}
		else
		if(command.equalsIgnoreCase("KICK"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			Player perso = player;
			final String name = infos[1];
			perso = World.getPersoByName(name);
			if(perso == null)
			{
				final String mess = "Le personnage n'existe pas.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			if(perso.isOnline())
			{
				perso.getAccount().getGameThread().kick();
				final String mess = "Vous avez kick "+perso.getName();
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				
			}
			else
			{
				final String mess = "Le personnage "+perso.getName()+" n'est pas connecte";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			}
			return;
		}
		else
		if(command.equalsIgnoreCase("SPELLPOINT"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int pts = -1;
			try
			{
				pts = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(pts == -1)
			{
				final String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			target.addSpellPoint(pts);
			SocketManager.GAME_SEND_STATS_PACKET(target);
			final String str = "Le nombre de point de sort a ete modifiee";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}else
		if(command.equalsIgnoreCase("LEARNSPELL"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int spell = -1;
			try
			{
				spell = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(spell == -1)
			{
				final String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			
			target.learnSpell(spell, 1, true,true);
			
			final String str = "Le sort a ete appris";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
			
			return;
		}else
		if(command.equalsIgnoreCase("SETALIGN"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			byte align = -1;
			try
			{
				align = Byte.parseByte(infos[1]);
			}catch(final Exception e){};
			if(align < Constants.ALIGNEMENT_NEUTRE || align >Constants.ALIGNEMENT_MERCENAIRE)
			{
				final String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			
			target.modifAlignement(align);
			
			final String str = "L'alignement du joueur a ete modifie";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}else
		if(command.equalsIgnoreCase("SETREPONSES"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			if(infos.length <3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,"Il manque un/des arguments");
				return;
			}
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			final String reps = infos[2];
			final NpcQuestion Q = World.getNPCQuestion(id);
			String str = "";
			if(id == 0 || Q == null)
			{
				str = "QuestionID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Q.setReponses(reps);
			final boolean a= SQLManager.UPDATE_NPCREPONSES(id,reps);
			str = "Liste des reponses pour la question "+id+": "+Q.getReponses();
			if(a)str += "(sauvegarde dans la BDD)";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
			return;
		}else
		if(command.equalsIgnoreCase("SHOWREPONSES"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			final NpcQuestion Q = World.getNPCQuestion(id);
			String str = "";
			if(id == 0 || Q == null)
			{
				str = "QuestionID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			str = "Liste des reponses pour la question "+id+": "+Q.getReponses();
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
			return;
		}else
		if(command.equalsIgnoreCase("HONOR"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int honor = 0;
			try
			{
				honor = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			Player target = player;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			String str = "Vous avez ajouter "+honor+" honneur a "+target.getName();
			if(target.getAlign() == Constants.ALIGNEMENT_NEUTRE)
			{
				str = "Le joueur est neutre ...";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			target.addHonor(honor);
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
			
		}else
		if(command.equalsIgnoreCase("ADDJOBXP"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int job = -1;
			int xp = -1;
			try
			{
				job = Integer.parseInt(infos[1]);
				xp = Integer.parseInt(infos[2]);
			}catch(final Exception e){};
			if(job == -1 || xp < 0)
			{
				final String str = "Valeurs invalides";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 3)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[3]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			final JobStat SM = target.getMetierByID(job);
			if(SM== null)
			{
				final String str = "Le joueur ne connais pas le métier demandé";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
				
			SM.addXp(target, xp);
			
			final String str = "Le metier a ete experimenter";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}else
		if(command.equalsIgnoreCase("LEARNJOB"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int job = -1;
			try
			{
				System.out.println(infos[1]);
				job = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(job == -1 || World.getMetier(job) == null)
			{
				final String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			
			target.learnJob(World.getMetier(job));
			
			final String str = "Le metier a ete appris";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}else
		if(command.equalsIgnoreCase("CAPITAL"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int pts = -1;
			try
			{
				pts = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(pts == -1)
			{
				final String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			target.addCapital(pts);
			SocketManager.GAME_SEND_STATS_PACKET(target);
			final String str = "Le capital a ete modifiee";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}else
		if(command.equalsIgnoreCase("SPAWNFIX"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			final String groupData = infos[1];

			player.getCurMap().addStaticGroup(player.getCurCell().getId(), groupData);
			String str = "Le grouppe a ete fixe";
			//Sauvegarde DB de la modif
			if(SQLManager.SAVE_NEW_FIXGROUP(player.getCurMap().getId(),player.getCurCell().getId(), groupData))
				str += " et a été sauvegarde dans la BDD";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
			return;
		}
		if(command.equalsIgnoreCase("SIZE"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int size = -1;
			try
			{
				size = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(size == -1)
			{
				final String str = "Taille invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			target.setSize(size);
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getCurMap(), target.getActorId());
			SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.getCurMap(), target);
			final String str = "La taille du joueur a ete modifiee";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}else
		if(command.equalsIgnoreCase("SETADMIN"))
		{
			if(account.getGmLvl() < 4)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int gmLvl = -100;
			try
			{
				gmLvl = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(gmLvl == -100)
			{
				final String str = "Valeur incorrecte";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			target.getAccount().setGmLvl(gmLvl);
			SQLManager.UPDATE_ACCOUNT_DATA(target.getAccount());
			final String str = "Le niveau GM du joueur a ete modifie";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}else
		if(command.equalsIgnoreCase("DEMORPH"))
		{
			Player target = player;
			if(infos.length > 1)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[1]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			final int morphID = target.getBreedId()*10 + target.getSexe();
			target.setGfxID(morphID);
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getCurMap(), target.getActorId());
			SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.getCurMap(), target);
			final String str = "Le joueur a ete transformé";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}
		else
		if(command.equalsIgnoreCase("MORPH"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int morphID = -1;
			try
			{
				morphID = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(morphID == -1)
			{
				final String str = "MorphID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			target.setGfxID(morphID);
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(target.getCurMap(), target.getActorId());
			SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(target.getCurMap(), target);
			final String str = "Le joueur a ete transformé";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}
		else
		if(command.equalsIgnoreCase("GONAME") || command.equalsIgnoreCase("JOIN"))
		{
			final Player P = World.getPersoByName(infos[1]);
			if(P == null)
			{
				final String str = "Le personnage n'existe pas";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			final int mapID = P.getCurMap().getId();
			final int cellID = P.getCurCell().getId();
			
			Player target = player;
			if(infos.length > 2)//Si un nom de perso est spécifié 
			{
				target = World.getPersoByName(infos[2]);
				if(target == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
				if(target.getFight() != null)
				{
					final String str = "La cible est en combat";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			target.teleport(mapID, cellID);
			final String str = "Le joueur a ete teleporte";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}
		else
		if(command.equalsIgnoreCase("NAMEGO"))
		{
			final Player target = World.getPersoByName(infos[1]);
			if(target == null)
			{
				final String str = "Le personnage n'existe pas";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			if(target.getFight() != null)
			{
				final String str = "La cible est en combat";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player P = player;
			if(infos.length > 2)//Si un nom de perso est spécifié
			{
				P = World.getPersoByName(infos[2]);
				if(P == null)
				{
					final String str = "Le personnage n'a pas ete trouve";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			final int mapID = P.getCurMap().getId();
			final int cellID = P.getCurCell().getId();
			target.teleport(mapID, cellID);
			final String str = "Le joueur a ete teleporte";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}else
		if(command.equalsIgnoreCase("ADDNPC"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(id == 0 || World.getNPCTemplate(id) == null)
			{
				final String str = "NpcID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			final Npc npc = player.getCurMap().addNpc(id, player.getCurCell().getId(), player.getOrientation());
			SocketManager.GAME_SEND_ADD_NPC_TO_MAP(player.getCurMap(), npc);
			String str = "Le PNJ a ete ajoute";
			if(player.getOrientation() == 0
					|| player.getOrientation() == 2
					|| player.getOrientation() == 4
					|| player.getOrientation() == 6)
						str += " mais est invisible (orientation diagonale invalide).";
			
			if(SQLManager.ADD_NPC_ON_MAP(player.getCurMap().getId(), id, player.getCurCell().getId(), player.getOrientation()))
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
			else
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,"Erreur au moment de sauvegarder la position");
		}else
		if(command.equalsIgnoreCase("DELNPC"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			final Npc npc = player.getCurMap().getNPC(id);
			if(id == 0 || npc == null)
			{
				final String str = "Npc GUID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			final int exC = npc.getCellId();
			//on l'efface de la map
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), id);
			player.getCurMap().removeNpcOrMobGroup(id);
			
			final String str = "Le PNJ a ete supprime";
			if(SQLManager.DELETE_NPC_ON_MAP(player.getCurMap().getId(),exC))
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
			else
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,"Erreur au moment de sauvegarder la position");
		}else
		if(command.equalsIgnoreCase("MOVENPC"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int id = 0;
			try
			{
				id = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			final Npc npc = player.getCurMap().getNPC(id);
			if(id == 0 || npc == null)
			{
				final String str = "Npc GUID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			final int exC = npc.getCellId();
			//on l'efface de la map
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), id);
			//on change sa position/orientation
			npc.setCellID(player.getCurCell().getId());
			npc.setOrientation((byte)player.getOrientation());
			//on envoie la modif
			SocketManager.GAME_SEND_ADD_NPC_TO_MAP(player.getCurMap(),npc);
			String str = "Le PNJ a ete deplace";
			if(player.getOrientation() == 0
			|| player.getOrientation() == 2
			|| player.getOrientation() == 4
			|| player.getOrientation() == 6)
				str += " mais est devenu invisible (orientation diagonale invalide).";
			if(SQLManager.DELETE_NPC_ON_MAP(player.getCurMap().getId(),exC)
			&& SQLManager.ADD_NPC_ON_MAP(player.getCurMap().getId(),npc.getTemplate().getId(),player.getCurCell().getId(),player.getOrientation()))
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
			else
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,"Erreur au moment de sauvegarder la position");
		}else
		if(command.equalsIgnoreCase("DELTRIGGER"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int cellID = -1;
			try
			{
				cellID = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(cellID == -1 || player.getCurMap().getCell(cellID) == null)
			{
				final String str = "CellID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			
			player.getCurMap().getCell(cellID).clearOnCellAction();
			final boolean success = SQLManager.REMOVE_TRIGGER(player.getCurMap().getId(),cellID);
			String str = "";
			if(success)	str = "Le trigger a ete retire";
			else 		str = "Le trigger n'a pas ete retire";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}else
		if(command.equalsIgnoreCase("ADDTRIGGER"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int actionID = -1;
			String args = "",cond = "";
			try
			{
				actionID = Integer.parseInt(infos[1]);
				args = infos[2];
				cond = infos[3];
			}catch(final Exception e){};
			if(args.equals("") || actionID <= -3)
			{
				final String str = "Valeur invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			
			player.getCurCell().addOnCellStopAction(actionID,args, cond);
			final boolean success = SQLManager.SAVE_TRIGGER(player.getCurMap().getId(),player.getCurCell().getId(),actionID,1,args,cond);
			String str = "";
			if(success)	str = "Le trigger a ete ajoute";
			else 		str = "Le trigger n'a pas ete ajoute";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}
		else
		if(command.equalsIgnoreCase("TELEPORT"))
		{
			int mapID = -1;
			int cellID = -1;
			try
			{
				mapID = Integer.parseInt(infos[1]);
				cellID = Integer.parseInt(infos[2]);
			}catch(final Exception e){};
			if(mapID == -1 || cellID == -1 || World.getMap(mapID) == null)
			{
				final String str = "MapID ou cellID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			if(World.getMap(mapID).getCell(cellID) == null)
			{
				final String str = "MapID ou cellID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 3)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[3]);
				if(target == null  || target.getFight() != null)
				{
					final String str = "Le personnage n'a pas ete trouve ou est en combat";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			target.teleport(mapID, cellID);
			final String str = "Le joueur a ete teleporte";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}
		else
		if(command.equalsIgnoreCase("DELNPCITEM"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int npcGUID = 0;
			int itmID = -1;
			try
			{
				npcGUID = Integer.parseInt(infos[1]);
				itmID = Integer.parseInt(infos[2]);
			}catch(final Exception e){};
			final NpcTemplate npc =  player.getCurMap().getNPC(npcGUID).getTemplate();
			if(npcGUID == 0 || itmID == -1 || npc == null)
			{
				final String str = "NpcGUID ou itmID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			
			
			String str = "";
			if(npc.delItemVendor(itmID))str = "L'objet a ete retire";
			else str = "L'objet n'a pas ete retire";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}
		else
		if(command.equalsIgnoreCase("ADDNPCITEM"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int npcGUID = 0;
			int itmID = -1;
			try
			{
				npcGUID = Integer.parseInt(infos[1]);
				itmID = Integer.parseInt(infos[2]);
			}catch(final Exception e){};
			final NpcTemplate npc =  player.getCurMap().getNPC(npcGUID).getTemplate();
			final ItemTemplate item =  World.getItemTemplate(itmID);
			if(npcGUID == 0 || itmID == -1 || npc == null || item == null)
			{
				final String str = "NpcGUID ou itmID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			
			
			String str = "";
			if(npc.addItemVendor(item))str = "L'objet a ete rajoute";
			else str = "L'objet n'a pas ete rajoute";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}
		else
		if(command.equalsIgnoreCase("GOMAP"))
		{
			int mapX = 0;
			int mapY = 0;
			int cellID = 0;
			int contID = 0;//Par défaut Amakna
			try
			{
				mapX = Integer.parseInt(infos[1]);
				mapY = Integer.parseInt(infos[2]);
				cellID = Integer.parseInt(infos[3]);
				contID = Integer.parseInt(infos[4]);
			}catch(final Exception e){};
			final DofusMap map = World.getCarteByPosAndCont(mapX,mapY,contID);
			if(map == null)
			{
				final String str = "Position ou continent invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			if(map.getCell(cellID) == null)
			{
				final String str = "CellID invalide";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Player target = player;
			if(infos.length > 5)//Si un nom de perso est spécifié
			{
				target = World.getPersoByName(infos[5]);
				if(target == null || target.getFight() != null)
				{
					final String str = "Le personnage n'a pas ete trouve ou est en combat";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
				if(target.getFight() != null)
				{
					final String str = "La cible est en combat";
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
					return;
				}
			}
			target.teleport(map.getId(), cellID);
			final String str = "Le joueur a ete teleporte";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}
		else
		if(command.equalsIgnoreCase("ADDMOUNTPARK"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int size = -1;
			int owner = -2;
			int price = -1;
			try
			{
				size = Integer.parseInt(infos[1]);
				owner = Integer.parseInt(infos[2]);
				price = Integer.parseInt(infos[3]);
				if(price > 20000000)price = 20000000;
				if(price <0)price = 0;
			}catch(final Exception e){};
			if(size == -1 || owner == -2 || price == -1 || player.getCurMap().getMountPark() != null)
			{
				final String str = "Infos invalides ou map deja config.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			final MountPark MP = new MountPark(owner, player.getCurMap(), size, "", -1, price);
			player.getCurMap().setMountPark(MP);
			SQLManager.SAVE_MOUNTPARK(MP);
			final String str = "L'enclos a ete config. avec succes";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}else
		if(command.equalsIgnoreCase("ITEM") || command.equalsIgnoreCase("!getitem"))
		{
			final boolean isOffiCmd = command.equalsIgnoreCase("!getitem");
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int tID = 0;
			try
			{
				tID = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			if(tID == 0)
			{
				final String mess = "Le template "+tID+" n'existe pas ";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			int qua = 1;
			if(infos.length == 3)//Si une quantité est spécifiée
			{
				try
				{
					qua = Integer.parseInt(infos[2]);
				}catch(final Exception e){};
			}
			boolean useMax = false;
			if(infos.length == 4 && !isOffiCmd)//Si un jet est spécifiée
			{
				if(infos[3].equalsIgnoreCase("MAX"))useMax = true;
			}
			final ItemTemplate t = World.getItemTemplate(tID);
			if(t == null)
			{
				final String mess = "Le template "+tID+" n'existe pas ";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			if(qua <1)qua =1;
			final Item obj = t.createNewItem(qua,useMax);
			if(player.addItem(obj, true))//Si le joueur n'avait pas d'item similaire
				World.addItem(obj,true);
			player.itemLog(obj.getTemplate().getID(), obj.getQuantity(), "Ajouté par un MJ");
			
			String str = "Creation de l'item "+tID+" reussie";
			if(useMax) str += " avec des stats maximums";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}
		else
		if(command.equalsIgnoreCase("ITEMSET"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int tID = 0;
			try
			{
				tID = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			final ItemSet IS = World.getItemSet(tID);
			if(tID == 0 || IS == null)
			{
				final String mess = "La panoplie "+tID+" n'existe pas ";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			boolean useMax = false;
			if(infos.length == 3)useMax = infos[2].equals("MAX");//Si un jet est spécifiée

			
			for(final ItemTemplate t : IS.getItemTemplates())
			{
				final Item obj = t.createNewItem(1,useMax);
				if(player.addItem(obj, true))//Si le joueur n'avait pas d'item similaire
					World.addItem(obj,true);
				player.itemLog(obj.getTemplate().getID(), obj.getQuantity(), "Ajouté par un MJ");
			}
			String str = "Creation de la panoplie "+tID+" reussie";
			if(useMax) str += " avec des stats maximums";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
		}
		else
		if(command.equalsIgnoreCase("LEVEL"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int count = 0;
			try
			{
				count = Integer.parseInt(infos[1]);
				if(count < 1)	count = 1;
				if(count > 200)	count = 200;
				Player perso = player;
				if(infos.length == 3)//Si le nom du perso est spécifier
				{
					final String name = infos[2];
					perso = World.getPersoByName(name);
					if(perso == null)
						perso = player;
				}
				if(perso.getLvl() < count)
				{
					while(perso.getLvl() < count)
					{
						perso.levelUp(false,true);
					}
					if(perso.isOnline())
					{
						SocketManager.GAME_SEND_NEW_LVL_PACKET(perso.getAccount().getGameThread().getOut(),perso.getLvl());
						SocketManager.GAME_SEND_STATS_PACKET(perso);
					}
				}
				final String mess = "Vous avez fixer le niveau de "+perso.getName()+" a "+count;
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			}catch(final Exception e)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Valeur incorecte");
				return;
			};
		}
		else
		if(command.equalsIgnoreCase("PDVPER"))
		{
			if(account.getGmLvl() < 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int count = 0;
			try
			{
				count = Integer.parseInt(infos[1]);
				if(count < 0)	count = 0;
				if(count > 100)	count = 100;
				Player perso = player;
				if(infos.length == 3)//Si le nom du perso est spécifié
				{
					final String name = infos[2];
					perso = World.getPersoByName(name);
					if(perso == null)
						perso = player;
				}
				final int newPDV = perso.getPDVMAX() * count / 100;
				perso.setPDV(newPDV);
				if(perso.isOnline())
					SocketManager.GAME_SEND_STATS_PACKET(perso);
				final String mess = "Vous avez fixer le pourcentage de pdv de "+perso.getName()+" a "+count;
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
			}catch(final Exception e)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Valeur incorecte");
				return;
			};
		}else
		if(command.equalsIgnoreCase("KAMAS"))
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			long count = 0;
			try
			{
				count = Long.parseLong(infos[1]);
			}catch(final Exception e)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Valeur incorecte");
				return;
			};
			if(count == 0)return;
			
			Player perso = player;
			if(infos.length == 3)//Si le nom du perso est spécifier
			{
				final String name = infos[2];
				perso = World.getPersoByName(name);
				if(perso == null)
					perso = player;
			}
			final long curKamas = perso.getKamas();
			long newKamas = curKamas + count;
			if(newKamas <0) newKamas = 0;
			if(newKamas > Long.MAX_VALUE) newKamas = Long.MAX_VALUE;
			perso.setKamas(newKamas);
			perso.kamasLog(count+"", "Commande MJ executé par '" + player.getName() + "'");
			
			if(perso.isOnline())
				SocketManager.GAME_SEND_STATS_PACKET(perso);
			String mess = "Vous avez ";
			mess += (count<0?"retirer":"ajouter")+" ";
			mess += Math.abs(count)+" kamas a "+perso.getName();
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
		}else if (command.equalsIgnoreCase("DOACTION"))
		{
			//DOACTION NAME TYPE ARGS COND
			if(infos.length < 4)
			{
				final String mess = "Nombre d'argument de la commande incorect !";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			int type = -100;
			String args = "",cond = "";
			Player perso = player;
			try
			{
				perso = World.getPersoByName(infos[1]);
				if(perso == null)perso = player;
				type = Integer.parseInt(infos[2]);
				args = infos[3];
				if(infos.length >4)
				cond = infos[4];
			}catch(final Exception e)
			{
				final String mess = "Arguments de la commande incorect !";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			(new Action(type,args,cond)).apply(perso,-1);
			final String mess = "Action effectuee !";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
		}
		/*MARTHIEUBEAN*/
		else if (command.equalsIgnoreCase("RUNFILE"))
		{
			if(infos.length < 2)	//Si le nombre de paramètre est < 2, donc qu'il n'y a que la commande d'écrit
			{
				final String mess = "Nombre d'argument de la commande incorect !";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			
			final String fileName = infos[1];	//Stockage du nom de fichier dans une variable
			BufferedReader fichier = null;
			
			try
			{
				
				fichier = new BufferedReader(new FileReader("RunScript/"+fileName+(!fileName.contains(".")?".run":"")));	//Ouverture d'un flux de lecture sur le fichier demandé
			}catch(final FileNotFoundException e)	//Erreur survient lorsque le fichier est introuvable
			{
				final String mess = "Le fichier \""+fileName+(!fileName.contains(".")?".run":"")+"\" n'existe pas!";	//Envoie d'un message expliquant l'erreur
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			
			String line = "";
			
			try
			{
				while ((line=fichier.readLine())!=null)
				{
					if(line.contains("#") || line.length() <= 0)	//Si la ligne est un commentaire ou si elle est vide
					{
						continue;
					}
					else if(line.charAt(0) == '>') //Si la ligne est une ligne d'affichage
					{
						SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,line.replaceFirst(">",""));	//Envoie du message sans le '>'
					}
					else	//Finalement, si le packet est une commande normal, ajout de deux caractère de bourrage et on execute la fonction d'execution de commande. Ce qui permet d'executer un fichier dans un fichier par exemple
					{
						line = line.replaceAll("%me%",player.getName());	//Remplace tout les %me% par le nom du perso en cours
						
						for (int i = 1; i <= 9; i++)
						{
							try
							{
								line = line.replaceAll("param"+i,infos[i+1]);
							}catch(final ArrayIndexOutOfBoundsException e) //Si le paramètre n'existe pas, la boucle doit s'arrêter
							{
								break;
							}
						}
						consoleCommand("XX"+line);
					}
				}
				fichier.close();
			}catch(final Exception e)
			{
				final String errMsg = "Fichier RUN illisible";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,errMsg);
				System.out.println (e.getMessage());
				return;
			}
		}else if (command.equalsIgnoreCase("LISTFILE"))
		{
			String[] listFichier = null;
			String sortie = "";
			File repertoire;
			try
			{
				repertoire = new File("RunScript");
				listFichier = repertoire.list();
				
				sortie += "==================\n";
				for (int i = 0; i < listFichier.length; i++)
				{
					if(listFichier[i].endsWith(".run"))
					{
						sortie += listFichier[i] + "\n";
					}
				}
				sortie += "==================";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,sortie);
			}catch(final Exception e)
			{
				final String errMsg = "Erreur lors du listage des fichiers run";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,errMsg);
				System.out.println (e.getMessage());
				return;
			}
		}else if (command.equalsIgnoreCase("HELPFILE"))
		{
			if(infos.length < 2)	//Si le nombre de paramètre est < 2, donc qu'il n'y a que la commande d'écrit
			{
				final String mess = "Nombre d'argument de la commande incorect !";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			
			final String fileName = infos[1];	//Stockage du nom de fichier dans une variable
			BufferedReader fichier = null;
			
			try
			{
				
				fichier = new BufferedReader(new FileReader("RunScript/"+fileName+".help"));	//Ouverture d'un flux de lecture sur le fichier demandé
			}catch(final FileNotFoundException e)	//Erreur survient lorsque le fichier est introuvable
			{
				final String mess = "Le fichier \""+fileName+".help\" n'existe pas!";	//Envoie d'un message expliquant l'erreur
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			
			String line = "";
			String sortie = "";
			try
			{
				sortie += "======================\n";
				while ((line=fichier.readLine())!=null)
				{
					sortie += line+"\n";
				}
				sortie += "======================";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,sortie);
				fichier.close();
			}catch(final Exception e)
			{
				final String errMsg = "Fichier d'aide illisible";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,errMsg);
				System.out.println (e.getMessage());
				return;
			}
		}else if(command.equalsIgnoreCase("SET"))	//SET INTELLIGENCE 500 *nomPerso*
		{
			if(account.getGmLvl() < 3)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Vous n'avez pas le niveau MJ requis");
				return;
			}
			int count = -1;
			try
			{
				count = Integer.parseInt(infos[2]);
			}catch(final Exception e)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Valeur incorecte");
				return;
			}
			if(count < 0)count = 0;
			
			Player perso = player;
			if(infos.length == 4 && !infos[3].equalsIgnoreCase(player.getName()))//Si le nom du perso est spécifier et que ce n'est pas son perso.
			{
				final String name = infos[3];
				perso = World.getPersoByName(name);
				if(perso == null)
					perso = player;
			}
			
			String mess = "Vous avez définit ";
			final String stats = infos[1];
			
			if(stats.equalsIgnoreCase("Intelligence"))
			{
				perso.setStat(Constants.STATS_ADD_INTE,count);
				mess+="l'intelligence";
			}else if(stats.equalsIgnoreCase("Force"))
			{
				perso.setStat(Constants.STATS_ADD_FORC,count);
				mess+="la force";
			}else if(stats.equalsIgnoreCase("Agilite"))
			{
				perso.setStat(Constants.STATS_ADD_AGIL,count);
				mess+="l'agilité";
			}else if(stats.equalsIgnoreCase("Chance"))
			{
				perso.setStat(Constants.STATS_ADD_CHAN,count);
				mess+="la chance";
			}else if(stats.equalsIgnoreCase("Sagesse"))
			{
				perso.setStat(Constants.STATS_ADD_SAGE,count);
				mess+="la sagesse";
			}else if(stats.equalsIgnoreCase("Vitalite"))
			{
				perso.setStat(Constants.STATS_ADD_VITA,count);
				mess+="la vitalité";
			}else
			{
				mess = "Stats \""+stats+"\" invalide!";
				count = -1;
				return;
			}
			if(perso.isOnline())
				SocketManager.GAME_SEND_STATS_PACKET(perso);

			if(count >= 0)
				mess+=" de " + perso.getName() + " à " + count;

			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);

			return;
		}else
		if(command.equalsIgnoreCase("LOCK"))
		{
			byte LockValue = 1;//Accessible
			try
			{
				LockValue = Byte.parseByte(infos[1]);
			}catch(Exception e){};
				if(LockValue > 2) LockValue = 2;
			if(LockValue < 0) LockValue = 0;
			char c = 0;
			if(LockValue == 0) c = 'D';
			if(LockValue == 1) c = 'O';
			if(LockValue == 2) c = 'S';
			Main.linkServer.sendChangeState(c);
			if(LockValue == 1)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Serveur accessible.");
			}else if(LockValue == 0)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Serveur inaccessible.");
			}else if(LockValue == 2)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Serveur en sauvegarde.");
			}
		}
		else
		if(command.equalsIgnoreCase("LOCKVIP"))
		{
			byte LockValue = 0;//Public
			try
			{
				LockValue = Byte.parseByte(infos[1]);
			}catch(Exception e){};
			if(LockValue > 1) LockValue = 1;
			if(LockValue < 0) LockValue = 0;
			Main.linkServer.sendChangeVIP(LockValue);
			if(LockValue == 1)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Le serveur est desormais VIP.");
			}else if(LockValue == 0)
			{
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Le serveur est pour tout public.");
			}
		}else
		if(command.equalsIgnoreCase("BLOCK"))
		{
			byte GmAccess = 0;
			byte KickPlayer = 0;
			try
			{
				GmAccess = Byte.parseByte(infos[1]);
				KickPlayer = Byte.parseByte(infos[2]);
			}catch(Exception e){};
			Main.linkServer.lockGMlevel(GmAccess);
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Serveur bloque au GmLevel : "+GmAccess);
			if(KickPlayer > 0)
			{
				for(Player z : World.getOnlinePersos())
				{
					if(z.getAccount().getGmLvl() < GmAccess)
						z.getAccount().getGameThread().closeSocket();
				}
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Les joueurs de GmLevel inferieur a "+GmAccess+" ont ete kicks.");
			}
		}else
		if(command.equalsIgnoreCase("BANIP"))
		{
			Player P = null;
			try
			{
				P = World.getPlayer(infos[1]);
			}catch(Exception e){};
			if(P == null || !P.isOnline())
			{
				String str = "Le personnage n'a pas ete trouve.";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,str);
				return;
			}
			Main.linkServer.addBanIP(P.getAccount().getCurIP());
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "L'IP a ete banni.");
			if(P.isOnline())
			{
				P.getAccount().getGameThread().kick();
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out, "Le joueur a ete kick.");
			}
		}else
		if(command.equalsIgnoreCase("TITLE"))
		{
			byte titleID = 0;
			try
			{
				titleID = Byte.parseByte(infos[1]);
			}catch(final Exception e){};
			if(titleID > 56)
			{
				final String mess = "Le title "+titleID+" n'existe pas ";
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
				return;
			}
			player.setTitle((byte) titleID);
			DofusMap map = player.getCurMap();
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(map, player.getActorId());
			SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(map, player);
		}
		/*FIN*/
		else
		{
			final String mess = "Commande non reconnue";
			SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,mess);
		}
	}
	
	/**
	 * 
	 * @param command La commande reçu par le GameThread /!\Sans le '_' au début/!\.
	 * @param perso Le personnage qui a entré la commande.
	 */
	public void dotCommand(final String command)
	{
		//On check si c'est une fast command
		if(World.isCommand(command))
		{
			final FastCommand cmd = World.getCommand(command);
			new Action(cmd.getActionId(), cmd.getArgs(), "").apply(player, 0);
			return;
		}
		//Retour au point de sauvegarde
		if(command.length() > 4 && command.substring(0, 5).equalsIgnoreCase("start"))
		{
			if(player.getFight() != null)return;
			player.teleportToSavePos();
			return;
		}
		//Zone shop
		if(command.length() > 3 && command.substring(0, 4).equalsIgnoreCase("shop"))
		{
			if(player.getFight() != null
			|| Config.CONFIG_SHOP_MAPID == 0
			|| Config.CONFIG_SHOP_CELLID == 0)return;
			
			player.teleport(Config.CONFIG_SHOP_MAPID, Config.CONFIG_SHOP_CELLID);
			return;
		}
		if (command.length() > 4 && command.substring(0, 5).equalsIgnoreCase("infos"))
		{
			long uptime = System.currentTimeMillis() - Main.gameServer.getStartTime();
			final int jour = (int) (uptime/(1000*3600*24));
			uptime %= (1000*3600*24);
			final int hour = (int) (uptime/(1000*3600));
			uptime %= (1000*3600);
			final int min = (int) (uptime/(1000*60));
			uptime %= (1000*60);
			final int sec = (int) (uptime/(1000));
			
			final String mess =	"Informations du serveur~\n=================================\n"
				+       	"<b>FaithCore v"+Constants.SERVER_VERSION+" par "+Constants.SERVER_MAKER+"</b>\n"
				+			"<b>Uptime</b>: "+jour+"j "+hour+"h "+min+"m "+sec+"s\n"
				+			"<b>Nombre de joueurs en ligne</b>: "+Main.gameServer.getPlayerNumber()+"\n"
				+			"<b>Records de connexions simultanées</b>: "+Main.gameServer.getMaxPlayer()+"\n"
				+			"=================================";
			SocketManager.GAME_SEND_IM_MESSAGE_RED(player, mess);
			return;
		}
		if(command.length() > 4 && command.substring(0, 5).equalsIgnoreCase("fmcac"))
		{
			Item obj = player.getObjetByPos(Constants.ITEM_POS_ARME);

			if(player.getFight() != null) {
				SocketManager.GAME_SEND_IM_MESSAGE_RED(player,  "Action impossible~ vous ne devez pas être en combat.");
				return;
			} 

			if(obj == null) {
				SocketManager.GAME_SEND_IM_MESSAGE_RED(player,  "Action impossible~ vous ne portez pas d'arme.");
				return;
			}
			
			boolean containNeutre = false;
			for(SpellEffect effect :  obj.getEffects()) {
				if(effect.getEffectID() == 100 || effect.getEffectID() == 95)
					containNeutre = true;
			}

			if(!containNeutre) {
				SocketManager.GAME_SEND_IM_MESSAGE_RED(player,  "Action impossible~ votre arme n'a pas de dégats neutre.");
				return;
			}

			String answer;

			try {
				answer = command.substring(6, command.length() - 1).replace(" ", "");
			}catch(Exception e) {
				SocketManager.GAME_SEND_IM_MESSAGE_RED(player,  "Action impossible~ vous n'avez pas spécifié l'élément (air, feu, terre, eau) " +
						"qui remplacera les dégats/vols de vies neutres.");
				return;
			}

			if(!answer.equalsIgnoreCase("air") && !answer.equalsIgnoreCase("terre") && !answer.equalsIgnoreCase("feu") && !answer.equalsIgnoreCase("eau"))
			{
				SocketManager.GAME_SEND_IM_MESSAGE_RED(player,  "Action impossible~ l'élément " + answer + " n'existe pas ! " +
						"(dispo : air, feu, terre, eau)");
				return;
			}

			for(int i = 0; i < obj.getEffects().size(); i++) {
				if(obj.getEffects().get(i).getEffectID() == 100) {
					if(answer.equalsIgnoreCase("air"))
						obj.getEffects().get(i).setEffectID(98);
					if(answer.equalsIgnoreCase("feu"))
						obj.getEffects().get(i).setEffectID(99);
					if(answer.equalsIgnoreCase("terre"))
						obj.getEffects().get(i).setEffectID(97);
					if(answer.equalsIgnoreCase("eau"))
						obj.getEffects().get(i).setEffectID(96);
				}

				if(obj.getEffects().get(i).getEffectID() == 95) {
					if(answer.equalsIgnoreCase("air"))
						obj.getEffects().get(i).setEffectID(93);
					if(answer.equalsIgnoreCase("feu"))
						obj.getEffects().get(i).setEffectID(94);
					if(answer.equalsIgnoreCase("terre"))
						obj.getEffects().get(i).setEffectID(92);
					if(answer.equalsIgnoreCase("eau"))
						obj.getEffects().get(i).setEffectID(91);
				}
			}

			try {
				
				String cibleNewStats = obj.parseStatsString();
				obj.clearStats();
				obj.parseStringToStats(cibleNewStats);
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(player, obj.getGuid()); 
				SocketManager.GAME_SEND_OAKO_PACKET(player, obj);
				
				player.clearCacheGMStuff();
				SocketManager.GAME_SEND_ON_EQUIP_ITEM(player.getCurMap(), player);
				player.clearCacheAS();
				SocketManager.GAME_SEND_STATS_PACKET(player);   
				
				SocketManager.GAME_SEND_IM_MESSAGE_RED(player,  "Succès~Votre arme équipé (" + obj.getTemplate().getName() + ") a été FM avec succès dans l'élément " + answer);       
				SQLManager.SAVE_PERSONNAGE(player, true);//Save du perso + item
				return;        		
			}catch(Exception e) {
				SocketManager.GAME_SEND_IM_MESSAGE_RED(player, "Erreur~Problème durant la procédure d'FM.");       
				return;
			}
		}
		if(command.length() > 5 && command.substring(0, 6).equalsIgnoreCase("parcho"))
		{
			if(player.getFight() != null)return;
			if(player.getAccount().getGmLvl() < 3)return;
			player.getBaseStats().addOneStat(Constants.STATS_ADD_AGIL,101);
			player.getBaseStats().addOneStat(Constants.STATS_ADD_CHAN,101);
			player.getBaseStats().addOneStat(Constants.STATS_ADD_VITA,101);
			player.getBaseStats().addOneStat(Constants.STATS_ADD_INTE,101);
			player.getBaseStats().addOneStat(Constants.STATS_ADD_SAGE,101);
			player.getBaseStats().addOneStat(Constants.STATS_ADD_FORC,101);
			player.clearCacheAS();
			SocketManager.GAME_SEND_STATS_PACKET(player);
			SocketManager.GAME_SEND_IM_MESSAGE_RED(player, "Succès~Tu as été correctement parchotté.");
			return;
		}
		if(command.length() > 5 && command.substring(0, 6).equalsIgnoreCase("spells"))
		{
			player.addSpellPoint(300);
			boolean hasError = false;
			ArrayList<Integer> spells = new ArrayList<Integer>();
			for(int id : player.getSpells().keySet())
			{
				spells.add(id);
			}
			for(int spellId : spells)
			{
				while(true) { // et une boucle infinie, une !
					int lvl = player.getSortStatBySortIfHas(spellId).getLevel();
					if(lvl == 6)
						break;
					if(player.boostSpell(spellId))
					{
						SocketManager.GAME_SEND_SPELL_UPGRADE_SUCCED(out, spellId, player.getSortStatBySortIfHas(spellId).getLevel());
					}else
					{
						hasError = true;
						break;
					}
				}
			}
			player.clearCacheAS();
			SocketManager.GAME_SEND_STATS_PACKET(player);
			if(hasError) SocketManager.GAME_SEND_IM_MESSAGE_RED(player, "Succès~Plusieurs sorts sont désormais niveau 6.");
			else SocketManager.GAME_SEND_IM_MESSAGE_RED(player, "Succès~Tous vos sorts sont désormais niveau 6.");
		}
		if(command.length() > 5 && command.substring(0, 6).equalsIgnoreCase("stuffs"))
		{
			for(int[] stuff : Constants.BASE_STUFF)
			{
				int itemId = stuff[0];
				int itemQua = stuff[1];
				Item newItem = World.getItemTemplate(itemId).createNewItem(itemQua, true);
				if(player.addItem(newItem, true))
					World.addItem(newItem, true);
			}
			SQLManager.SAVE_PERSONNAGE(player, true);
		}
		if(command.length() > 2 && command.substring(0, 3).equalsIgnoreCase("sit"))
		{
			player.setEmoteActive(20);
			SocketManager.GAME_SEND_eUK_PACKET_TO_MAP(player.getCurMap(), player.getActorId(), player.emoteActive());
			return; 
		}
	}
	
	private void fullHdv(final int ofEachTemplate)
	{
		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,"Démarrage du remplissage!");
		
		final TreeMap<Integer,ItemTemplate> template =(TreeMap<Integer, ItemTemplate>) World.getObjTemplates();
		
		Item objet = null;
		BigStoreEntry entry = null;
		byte amount = 0;
		int hdv = 0;
		
		int lastSend = 0;
		final long time1 = System.currentTimeMillis(); //TIME
		for (final ItemTemplate curTemp : template.values()) //Boucler dans les template
		{
			try
			{
				if(Config.NOTINHDV.contains(curTemp.getID()))
					continue;
				for (int j = 0; j < ofEachTemplate; j++) //Ajouter plusieur fois le template
				{
					if(curTemp.getType() == 85)
						break;
					objet = curTemp.createNewItem(1, false);
					hdv = getHdv(objet.getTemplate().getType());
					
					if(hdv < 0)
						break;
						
					amount = (byte) Formulas.getRandomValue(1, 3);
					
					
					entry = new BigStoreEntry(calculPrice(objet,amount), amount, -1, objet);
					objet.setQuantity(entry.getAmount(true));
					
					
					World.getHdv(hdv).addEntry(entry);
					World.addItem(objet, false);
				}
			}catch (final Exception e)
			{
				continue;
			}
			
			if((System.currentTimeMillis() - time1)/1000 != lastSend
				&& (System.currentTimeMillis() - time1)/1000 % 3 == 0)
			{
				lastSend = (int) ((System.currentTimeMillis() - time1)/1000);
				SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,(System.currentTimeMillis() - time1)/1000 + "sec Template: "+curTemp.getID());
			}
		}
		SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(out,"Remplissage fini en "+(System.currentTimeMillis() - time1) + "ms");
		SocketManager.GAME_SEND_MESSAGE_TO_ALL("HDV remplis!",Config.CONFIG_MOTD_COLOR);
	}
	private int getHdv(final int type)
	{
		//TODO ajouter les HDV Astrub et Brâkmar
		switch(type)
		{
			case 12:
			case 14: 
			case 26: 
			case 43: 
			case 44: 
			case 45: 
			case 66: 
			case 70: 
			case 71: 
			case 86:
				return 4271;
			case 1:
			case 9:
				return 4216;
			case 18: 
			case 72: 
			case 77: 
			case 90: 
			case 97: 
			case 113: 
			case 116:
				return 8759;
			case 63:
			case 64:
			case 69:
				return 4287;
			case 33:
			case 42:
				return 2221;
			case 84: 
			case 93: 
			case 112: 
			case 114:
				return 4232;
			case 38: 
			case 95: 
			case 96: 
			case 98: 
			case 108:
				return 4178;
			case 10:
			case 11:
				return 4183;
			case 13: 
			case 25: 
			case 73: 
			case 75: 
			case 76:
				return 8760;
			case 5: 
			case 6: 
			case 7: 
			case 8: 
			case 19: 
			case 20: 
			case 21: 
			case 22:
				return 4098;
			case 39: 
			case 40: 
			case 50: 
			case 51: 
			case 88:
				return 4179;
			case 87:
				return 6159;
			case 34:
			case 52:
			case 60:
				return 4299;
			case 41:
			case 49:
			case 62:
				return 4247;
			case 15: 
			case 35: 
			case 36: 
			case 46: 
			case 47: 
			case 48: 
			case 53: 
			case 54: 
			case 55: 
			case 56: 
			case 57: 
			case 58: 
			case 59: 
			case 65: 
			case 68: 
			case 103: 
			case 104: 
			case 105: 
			case 106: 
			case 107: 
			case 109: 
			case 110: 
			case 111:
				return 4262;
			case 78:
				return 8757;
			case 2:
			case 3:
			case 4:
				return 4174;
			case 16:
			case 17:
			case 81:
				return 4172;
			case 83:
				return 10129;
			case 82:
				return 8039;
			default:
				return -1;
		}
	}
	private int calculPrice(final Item obj, final int logAmount)
	{
		final int amount = (byte)(Math.pow(10,(double)logAmount) / 10);
		int stats = 0;
		
		for(final int curStat : obj.getStats().getMap().values())
		{
			stats += curStat;
		}
		if(stats > 0)
			return (int) (((Math.cbrt(stats) * Math.pow(obj.getTemplate().getLevel(), 2)) * 10 + Formulas.getRandomValue(1, obj.getTemplate().getLevel()*100)) * amount);
		else
			return (int) ((Math.pow(obj.getTemplate().getLevel(),2) * 10 + Formulas.getRandomValue(1, obj.getTemplate().getLevel()*100))*amount);
	}
}

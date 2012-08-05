package server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import objects.account.Account;
import objects.action.GameAction;
import objects.alignment.Prism;
import objects.bigstore.BigStore;
import objects.bigstore.BigStoreEntry;
import objects.character.Mount;
import objects.character.Party;
import objects.character.Player;
import objects.fight.Fight;
import objects.fight.Fighter;
import objects.guild.Guild;
import objects.guild.GuildMember;
import objects.guild.TaxCollector;
import objects.item.Gift;
import objects.item.Item;
import objects.item.ItemTemplate;
import objects.item.PackObject;
import objects.item.Pet;
import objects.item.Speaking;
import objects.job.JobStat;
import objects.map.DofusCell;
import objects.map.DofusMap;
import objects.npc.Npc;
import objects.npc.NpcQuestion;
import objects.npc.NpcResponse;
import objects.spell.SpellStat;

import common.ConditionParser;
import common.Config;
import common.Constants;
import common.Main;
import common.SQLManager;
import common.SocketManager;
import common.World;
import common.command.Command;
import common.console.Console;
import common.console.Log;
import common.utils.CryptManager;
import common.utils.Formulas;
import common.utils.Pathfinding;
import common.utils.Utils;

public class GameThread implements Runnable
{
	private BufferedReader _in;
	private PrintWriter _out;
	private Socket _socket;
	private Account _account;
	private Player _player;
	private final Map<Integer,GameAction> _actions = new TreeMap<Integer,GameAction>();
	private long _timeLastTradeMsg = 0, _timeLastRecrutmentMsg = 0;
	
	private Command command;
	
	public GameThread(final Socket sock)
	{
		try
		{
			_socket = sock;
			_in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
			_out = new PrintWriter(_socket.getOutputStream());
		}
		catch(final IOException e)
		{
			try {
				Log.addToErrorLog(e.getMessage());
				if(!_socket.isClosed())_socket.close();
			} catch (final IOException e1) {e1.printStackTrace();}
		}
	}
	
	public void run()
	{
		try
    	{
			String packet = "";
			final char charCur[] = new char[1];
			SocketManager.GAME_SEND_HELLOGAME_PACKET(_out);
			while(_in.read(charCur, 0, 1)!=-1 && Main.isRunning)
	    	{
	    		if (charCur[0] != '\u0000' && charCur[0] != '\n' && charCur[0] != '\r')
		    	{
	    			packet += charCur[0];
		    	}else if(packet != "")
		    	{
		    		packet = CryptManager.toUnicode(packet);	//MARTHIEUBEAN
		    		
		    		if(_player != null && Log.canLog)
		    			_player.sockLog("Game: Recv << "+packet);
		    		Log.addToSockLog("Game: Recv << "+packet);
		    		
		    		parsePacket(packet);
		    		packet = "";
		    	}
	    	}
    	}catch(final IOException e)
    	{
    		try
    		{
    			Log.addToErrorLog(e.getMessage());
    			_in.close();
    			_out.close();
    			if(_account != null)
	    		{
	    			_account.setCurPerso(null);
	    			_account.setGameClient(null);
	    		}
	    		if(!_socket.isClosed())_socket.close();
	    	}catch(final IOException e1){e1.printStackTrace();};
    	}catch(final Exception e)
    	{
    		e.printStackTrace();
    		Log.addToErrorLog(e.getMessage());
    	}
    	finally
    	{
    		kick();
    	}
	}

	private void parsePacket(final String packet)
	{
		if(packet.length()>3 && packet.substring(0,4).equalsIgnoreCase("ping"))
		{
			SocketManager.GAME_SEND_PONG(_out);
			return;
		}
		if(packet.length()>4 && packet.substring(0,5).equalsIgnoreCase("qping"))
		{
			SocketManager.GAME_SEND_QPONG(_out);
			return;
		}
		if(_player != null) 
		{
			_player.refreshLastPacketTime();
			if(_player.isChangePseudo())
			{
				World.changePlayerNameUI(_player, packet);
				return;
			}
		}
		switch(packet.charAt(0))
		{
			case 'A':
				parseAccountPacket(packet);
			break;
			case 'B':
				parseBasicsPacket(packet);
			break;
			case 'C':
				parseConquestPacket(packet);
			break;
			case 'c':
				parseChannelPacket(packet);
			break;
			case 'D':
				parseDialogPacket(packet);
			break;		
			case 'E':
				parseExchangePacket(packet);
			break;
			case 'e':
				parseEnvironementPacket(packet);
			break;
			case 'F':
				parseFriendPacket(packet);
			break;
			case 'f':
				parseFightPacket(packet);
			break;
			case 'G':
				parseGamePacket(packet);
			break;
			case 'g':
				parseGuildPacket(packet);
			break;
			case 'O':
				parseObjectPacket(packet);
			break;
			case 'P':
				parsePartyPacket(packet);
			break;
			case 'Q':
				parseQuestPacket(packet);
			break;
			case 'R':
				parseMountPacket(packet);
			break;
			case 'S':
				parseSpellPacket(packet);
			break;
			case 'W':
				parseWaypointPacket(packet);
			break;
		}
	}
	
	private void parseConquestPacket(final String packet) {

		switch(packet.charAt(1))
		{
			case 'b':
				//float[] balanceWorld = Conquest.GetBalanceWorld();
				//SocketManager.GAME_SEND_CONQUEST_ON_BALANCE(_player, balanceWorld[0], balanceWorld[1]);
			break;
			case 'I':
				if(packet.charAt(2) == 'J')
				{
					
					final Prism prism = _player.getCurMap().getSubArea().getPrism();
					if(prism == null)
					{
						SocketManager.GAME_SEND_CONQUEST_ON_INFO_JOIN(_player, "-3");
						return;
					}
					final Fight fight = prism.getFight();
					if(fight != null && fight.getState() == 3 && prism.getAlign() == _player.getAlign())
					{
						SocketManager.GAME_SEND_CONQUEST_ON_INFO_JOIN(_player, "-2");
						return;
					}
					if(fight == null)
						SocketManager.GAME_SEND_CONQUEST_ON_INFO_JOIN(_player, "-1");
					if(fight != null && fight.getState() == Constants.FIGHT_STATE_PLACE)
						SocketManager.GAME_SEND_CONQUEST_ON_INFO_JOIN(_player, prism.getState());
					//Reload packet to show attackers & defenders
				}
			break;
		}
	}

	private void parseQuestPacket(final String packet) {
		switch(packet.charAt(1))
		{
			case 'L':
				SocketManager.GAME_SEND_QUEST_LIST_PACKET(_player);
			break;
			
			case 'S':
				SocketManager.GAME_SEND_QUEST_STEP_PACKET(_player, packet.substring(2));
			break;
		}
    }

	private void parseWaypointPacket(final String packet)
	{
		switch(packet.charAt(1))
		{
			case 'p':
				Waypoint_prismCreate(packet);
			break;
			case 'u':
			case 'U'://Use
				Waypoint_use(packet);
			break;
			case 'v':
				Waypoint_quit(false);
			break;
			case 'V'://Quitter
				Waypoint_quit(true);
			break;
			case 'w':
				Waypoint_prismQuit();
			break;
		}
	}
	
	private void Waypoint_prismCreate(final String packet) 
	{
		if (_player.getDeshonor() >= 2) 
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "183");
			return;
		}
		if(!_player.isShowingWings()) 
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "1144");
			return;
		}
		final Prism prism = World.getMap(Integer.parseInt(packet.substring(2))).getPrism();
		if(prism == null) return;
		final int price = Formulas.calculPrismCost(_player.getCurMap(), World.getMap(prism.getMapId()), _player);
		if(_player.getKamas() - price < 0)
        {
			SocketManager.GAME_SEND_Im_PACKET(_player, "182");
			return;
        }
		SocketManager.GAME_SEND_Wp_PACKET(_player, prism.getMapId());
		_player.teleport(prism.getMapId(), prism.getCellId()+1);
		_player.setKamas(_player.getKamas() - price);
		SocketManager.GAME_SEND_Im_PACKET(_player, "046");
		SocketManager.GAME_SEND_SUBWAY_PRISM_EXIT(_player);
	}

	private void Waypoint_prismQuit() 
	{
		SocketManager.GAME_SEND_SUBWAY_PRISM_EXIT(_player);
	}

	private void Waypoint_quit(final boolean zaap)
	{
		if(zaap)
			_player.stopZaaping();
		else
			_player.stopZaapiing();
	}

	private void Waypoint_use(final String packet)
	{
		short id = -1;
		try
		{
			id = Short.parseShort(packet.substring(2));
		}catch(final Exception e){};
		if( id == -1)return;
		
		if(packet.charAt(1) == 'U')	//Si zaap ou zaapi
			_player.useZaap(id);
		else
			_player.useZaapi(id);
	}
	
	private void parseGuildPacket(final String packet)
	{
		switch(packet.charAt(1))
		{
			case 'B'://Add Stats
				guild_addStats(packet.charAt(2));
			break;
			case 'C'://Creation
				guild_create(packet);
			break;
			case 'F'://Retirer percepteur
				guild_removePerco(packet.substring(2));
			break;
			case 'I'://Infos
				guild_infos(packet.charAt(2));
			break;
			case 'H'://Poser un percepteur
				guild_addPerco();
			break;
			case 'J'://Join
				guild_join(packet.substring(2));
			break;
			case 'K'://Kick
				guild_kick(packet.substring(2));
			break;
			case 'P'://Changement des droits d'un membre
				guild_changeRight(packet.substring(2));
			break;
			case 'T'://attaque sur percepteur
				guild_percoJoinFight(packet.substring(2));
			break;
			case 'V'://Ferme le panneau de création de guilde
				guild_CancelCreate();
			break;
		}
	}
	
	private void guild_percoJoinFight(final String packet) {
		final TaxCollector taxcollector = World.getTaxCollector(Integer.parseInt(Integer.toString(Integer.parseInt(packet.substring(1)), 36)));
		if(taxcollector == null)
			return;
		switch(packet.charAt(0))
		{
			case 'J'://Rejoindre
				_player.savePos();
				taxcollector.addDefender(_player);
				_player.teleport(taxcollector.getMapId(), World.getMap(taxcollector.getMapId()).getRandomFreeCellID());
            	taxcollector.getFight().joinPercepteurFight(_player, taxcollector.getActorId());
				for(final Player member : taxcollector.getGuild().getMembers())
				{
					if(member == null || !member.isOnline()) continue;
					SocketManager.GAME_SEND_DEFENSE_PERCEPTEUR(member, taxcollector.getActorId());
				}
				break;
				
			case 'V'://Quitter
				taxcollector.remDefender(_player);
				SocketManager.send(_out, "gTV");//FIXME: to test
				//TODO: Make the packet who leave this interface
				break;
				
			default:
				Console.printlnError("Unknown packet recv : gT" + packet);
				SocketManager.GAME_SEND_BN(_out);
				break;
		}
    }

	private void guild_addPerco() {
		if(_player.getGuild() == null)return;
		if(!_player.getGuildMember().canDo(Constants.G_POSPERCO))return;//Pas le droit de le poser
		if(_player.getGuild().getMembers().size() < 10)return;//Guilde invalide
		if(_player.getGuild().getCountTaxCollector() >= _player.getGuild().getNbrTaxCollector()) return;//Limite de percepteur
		//TODO : Faire la gestion de l'incapacité de poser un percepteur sur la même map Im1167
		//TODO : Faire la gestion de l'incapacité de poser plus de %1 percepteurs par zone Im1168
		if(_player.getCurMap().getPercepteur() != null)//La carte possède un perco
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "1168;1");
			return;
		}
		if(_player.getCurMap().getPlacesStr().length() < 5)//La map ne possède pas de "places"
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "113");
			return;
		}
		final short price = (short)(1000+10*_player.getGuild().getLvl());//Calcul du prix du percepteur
		if(_player.getKamas() < price)//Kamas insuffisants
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "182");
			return;
		}
		final short nom1 = Short.parseShort(Integer.toString(Formulas.getRandomValue(1, 227), 36));//TODO Si bug : inverser 227 & 129
        final short nom2 = Short.parseShort(Integer.toString(Formulas.getRandomValue(1, 129), 36));
        final TaxCollector newPerco = new TaxCollector(World.getNewPercoGuid(), nom1, nom2, _player.getGuild().get_id(), 
        		_player.getCurMap().getId(), _player.getCurCell().getId(), (byte)3);
        World.addTaxCollector(newPerco);
        _player.getCurMap().setPercepteur(newPerco);
        SQLManager.ADD_PERCEPTEUR(newPerco.getActorId(), nom1, nom2, newPerco.getGuild().get_id(), 
        		newPerco.getMapId(), newPerco.getCellId(), newPerco.getOrientation());
    }

	private void guild_addStats(final char packet) {
		if(_player.getGuild() == null)return;
		final Guild G = _player.getGuild();
		if(!_player.getGuildMember().canDo(Constants.G_BOOST))return;
		switch(packet)
		{
		case 'p'://Prospec
			if(G.getCapital() < 1)return;
			if(G.getStats(176) >= 500)return;
			G.removeCapital(1);
			G.upgradeStats(176, 1);
			break;
		case 'x'://Sagesse
			if(G.getCapital() < 1)return;
			if(G.getStats(124) >= 400)return;
			G.removeCapital(1);
			G.upgradeStats(124, 1);
			break;
		case 'o'://Pod
			if(G.getCapital() < 1)return;
			if(G.getStats(158) >= 5000)return;
			G.removeCapital(1);
			G.upgradeStats(158, 20);
			break;
		case 'k'://Nb Perco
			if(G.getCapital() < 10)return;
			if(G.getNbrTaxCollector() >= 50)return;
			G.removeCapital(10);
			G.addNbrPercos(1);
			break;
		default:
			Console.printlnError("Unknown packet recv : gB" + packet);
			SocketManager.GAME_SEND_BN(_out);
			break;
		}
		SQLManager.UPDATE_GUILD(G);
		SocketManager.GAME_SEND_GUILD_INFO_BOOST_PACKET(_player, _player.getGuild().parseStatsTaxCollectorToGuild());
    }

	private void guild_removePerco(final String packet) {
		if(_player.getGuild() == null)return;
		if(!_player.getGuildMember().canDo(Constants.G_POSPERCO))return;//On peut le retirer si on a le droit de le poser
		final int percoId = Integer.parseInt(packet);
		final TaxCollector perco = World.getTaxCollector(percoId);
		if(perco.getFight() == null) return;
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_player.getCurMap(), percoId);
		SQLManager.DELETE_PERCEPTEUR(perco.getActorId());
		final StringBuilder str = new StringBuilder(25);
		str.append('R').append(perco.getName1()).append(',').append(perco.getName2()).append('|');
		str.append(perco.getMapId()).append('|');
		str.append(World.getMap(perco.getMapId()).getX()).append('|').append(World.getMap(perco.getMapId()).getY()).append('|').append(_player.getName());
		for(final Player z : _player.getGuild().getMembers())
		{
			if(z != null && z.isOnline())
			{
				SocketManager.GAME_SEND_GUILD_PERCEPTEUR_MOVEMENT_PACKET(z, _player.getGuild().parseStatsTaxCollectorToGuild());
				SocketManager.GAME_SEND_GUILD_PERCEPTEUR_INFOS_PACKET(z, str.toString());
			}
		}
    }

	private void guild_changeRight(final String packet)
	{
		if(_player.getGuild() == null)return;	//Si le personnage envoyeur n'a même pas de guilde
		
		final String[] infos = packet.split("\\|");
		
		final int guid = Integer.parseInt(infos[0]);
		int rank = Integer.parseInt(infos[1]);
		byte xpGive = Byte.parseByte(infos[2]);
		int right = Integer.parseInt(infos[3]);
		
		final Player p = World.getPlayer(guid);	//Cherche le personnage a qui l'on change les droits dans la mémoire
		GuildMember toChange;
		final GuildMember changer = _player.getGuildMember();
		
		//Récupération du personnage à changer, et verification de quelques conditions de base
		if(p == null)	//Arrive lorsque le personnage n'est pas chargé dans la mémoire
		{
			final int guildId = SQLManager.isPersoInGuild(guid);	//Récupère l'id de la guilde du personnage qui n'est pas dans la mémoire
			
			if(guildId < 0)return;	//Si le personnage à qui les droits doivent être modifié n'existe pas ou n'a pas de guilde
			
			
			if(guildId != _player.getGuild().get_id())					//Si ils ne sont pas dans la même guilde
			{
				SocketManager.GAME_SEND_gK_PACKET(_player, "Ed");
				return;
			}
			toChange = World.getGuild(guildId).getMember(guid);
		}
		else
		{
			if(p.getGuild() == null)return;	//Si la personne à qui changer les droits n'a pas de guilde
			if(_player.getGuild().get_id() != p.getGuild().get_id())	//Si ils ne sont pas de la meme guilde
			{
				SocketManager.GAME_SEND_gK_PACKET(_player, "Ea");
				return;
			}
			
			toChange = p.getGuildMember();
		}
		
		//Vérifie ce que le personnage changeur à le droit de faire
		
		if(changer.getRank() == 1)	//Si c'est le meneur
		{
			if(changer.getGuid() == toChange.getGuid())	//Si il se modifie lui même, reset tout sauf l'XP
			{
				rank = -1;
				right = -1;
			}
			else //Si il modifie un autre membre
			{
				if(rank == 1) //Si il met un autre membre "Meneur"
				{
					changer.setAllRights(2, (byte) -1, 29694);	//Met le meneur "Bras droit" avec tout les droits
					
					//Défini les droits à mettre au nouveau meneur
					rank = 1;
					xpGive = -1;
					right = 1;
				}
			}
		}
		else	//Sinon, c'est un membre normal
		{
			if(toChange.getRank() == 1)	//S'il veut changer le meneur, reset tout sauf l'XP
			{
				rank = -1;
				right = -1;
			}
			else	//Sinon il veut changer un membre normal
			{
				if(!changer.canDo(Constants.G_RANK) || rank == 1)	//S'il ne peut changer les rang ou qu'il veut mettre meneur
					rank = -1; 	//"Reset" le rang
				
				if(!changer.canDo(Constants.G_RIGHT) || right == 1)	//S'il ne peut changer les droits ou qu'il veut mettre les droits de meneur
					right = -1;	//"Reset" les droits
				
				if(!changer.canDo(Constants.G_HISXP) && !changer.canDo(Constants.G_ALLXP) && changer.getGuid() == toChange.getGuid())	//S'il ne peut changer l'XP de personne et qu'il est la cible
					xpGive = -1; //"Reset" l'XP
			}
			
			if(!changer.canDo(Constants.G_ALLXP) && !changer.equals(toChange))	//S'il n'a pas le droit de changer l'XP des autres et qu'il n'est pas la cible
				xpGive = -1; //"Reset" L'XP
		}

		toChange.setAllRights(rank,xpGive,right);
		
		SocketManager.GAME_SEND_gS_PACKET(_player,_player.getGuildMember());
		
		if(p != null && p.getActorId() != _player.getActorId())
			SocketManager.GAME_SEND_gS_PACKET(p,p.getGuildMember());
	}

	private void guild_CancelCreate()
	{
		SocketManager.GAME_SEND_gV_PACKET(_player);
	}

	private void guild_kick(final String name)
	{
		if(_player.getGuild() == null)return;
		final Player P = World.getPersoByName(name);
		int guid = -1,guildId = -1;
		Guild toRemGuild;
		GuildMember toRemMember;
		
		if(P == null)
		{
			final int infos[] = SQLManager.isPersoInGuild(name);
			guid = infos[0];
			guildId = infos[1];
			if(guildId < 0 || guid < 0)return;
			toRemGuild = World.getGuild(guildId);
			toRemMember = toRemGuild.getMember(guid);
		}
		else
		{
			toRemGuild = P.getGuild();
			toRemMember = toRemGuild.getMember(P.getActorId());
		}
		//si pas la meme guilde
		if(toRemGuild.get_id() != _player.getGuild().get_id())
		{
			SocketManager.GAME_SEND_gK_PACKET(_player, "Ea");
			return;
		}
		//S'il n'a pas le droit de kick, et que ce n'est pas lui même la cible
		if(!_player.getGuildMember().canDo(Constants.G_BAN) && _player.getGuildMember().getGuid() != toRemMember.getGuid())
		{
			SocketManager.GAME_SEND_gK_PACKET(_player, "Ed");
			return;
		}
		//Si différent : Kick 
		if(_player.getGuildMember().getGuid() != toRemMember.getGuid())
		{
			if(toRemMember.getRank() == 1) //S'il veut kicker le meneur
				return;
			
			toRemGuild.removeMember(toRemMember.getGuid());
			if(P != null)
				P.setGuildMember(null);
			
			SocketManager.GAME_SEND_gK_PACKET(_player, "K"+_player.getName()+"|"+name);
			if(P != null)
				SocketManager.GAME_SEND_gK_PACKET(P, "K"+_player.getName());
		}else//si quitter
		{
			final Guild G = _player.getGuild();
			if(_player.getGuildMember().getRank() == 1 && G.getMembers().size() > 1)	//Si le meneur veut quitter la guilde mais qu'il reste d'autre joueurs
			{
				final Player substitute = World.getPlayer(_player.getGuild().getSubstituteId(_player.getActorId()));
				for(final Player gm : _player.getGuild().getMembers())
				{
					if(gm == _player) continue;
					SocketManager.GAME_SEND_Im_PACKET(gm, "1199;"+substitute.getActorId()+"~"+_player.getName());
				}
				G.substituteLeader(_player.getActorId(), substitute.getActorId());
				_player.setGuildMember(null);
				return;
			}
			G.removeMember(_player.getActorId());
			_player.setGuildMember(null);
			//S'il n'y a plus personne
			if(G.getMembers().size() == 0)World.removeGuild(G.get_id());
			SocketManager.GAME_SEND_gK_PACKET(_player, "K"+name+"|"+name);
		}
	}
	
	private void guild_join(final String packet)
	{
		switch(packet.charAt(0))
		{
		case 'R'://Nom perso
			final Player P = World.getPersoByName(packet.substring(1));
			if(P == null || _player.getGuild() == null)
			{
				SocketManager.GAME_SEND_gJ_PACKET(_player, "Eu");
				return;
			}
			if(!P.isOnline())
			{
				SocketManager.GAME_SEND_gJ_PACKET(_player, "Eu");
				return;
			}
			if(P.isAway())
			{
				SocketManager.GAME_SEND_gJ_PACKET(_player, "Eo");
				return;
			}
			if(P.getGuild() != null)
			{
				SocketManager.GAME_SEND_gJ_PACKET(_player, "Ea");
				return;
			}
			if(!_player.getGuildMember().canDo(Constants.G_INVITE))
			{
				SocketManager.GAME_SEND_gJ_PACKET(_player, "Ed");
				return;
			}
			
			_player.setInvitation(P.getActorId());
			P.setInvitation(_player.getActorId());

			SocketManager.GAME_SEND_gJ_PACKET(_player,"R"+packet.substring(1));
			SocketManager.GAME_SEND_gJ_PACKET(P,"r"+_player.getActorId()+"|"+_player.getName()+"|"+_player.getGuild().getName());
		break;
		case 'E'://ou Refus
			if(packet.substring(1).equalsIgnoreCase(_player.getInvitation()+""))
			{
				final Player p = World.getPlayer(_player.getInvitation());
				if(p == null)return;//Pas censé arriver
				SocketManager.GAME_SEND_gJ_PACKET(p,"Ec");
			}
		break;
		case 'K'://Accepte
			if(packet.substring(1).equalsIgnoreCase(_player.getInvitation()+""))
			{
				final Player p = World.getPlayer(_player.getInvitation());
				if(p == null)return;//Pas censé arriver
				final Guild G = p.getGuild();
				final GuildMember GM = G.addNewMember(_player);
				SQLManager.UPDATE_GUILDMEMBER(GM);
				_player.setGuildMember(GM);
				_player.setInvitation(-1);
				p.setInvitation(-1);
				//Packet
				SocketManager.GAME_SEND_gJ_PACKET(p,"Ka"+_player.getName());
				SocketManager.GAME_SEND_gS_PACKET(_player, GM);
				SocketManager.GAME_SEND_gJ_PACKET(_player,"Kj");
			}
		break;
		
		default:
			Console.printlnError("Unknown packet recv : gJ" + packet);
			SocketManager.GAME_SEND_BN(_out);
			break;
		}
	}

	private void guild_infos(final char c)
	{
		switch(c)
		{
		case 'B'://Personalisation (Boost)
			SocketManager.GAME_SEND_gIB_PACKET(_player);
		break;
		case 'G'://General
			SocketManager.GAME_SEND_gIG_PACKET(_player, _player.getGuild());
		break;
		case 'M'://Members
			SocketManager.GAME_SEND_gIM_PACKET(_player, _player.getGuild(),'+');
		break;
		default:
			Console.printlnError("Unknown packet recv : gT" + c);
			SocketManager.GAME_SEND_BN(_out);
			break;
		}
	}

	private void guild_create(final String packet)
	{
		if(_player == null)return;
		if(_player.getGuild() != null || _player.getGuildMember() != null)
		{
			SocketManager.GAME_SEND_gC_PACKET(_player, "Ea");
			return;
		}
		if(_player.getFight() != null )return;
		try
		{
			final String[] infos = packet.substring(2).split("\\|");
			//base 10 => 36
			final String bgID = Integer.toString(Integer.parseInt(infos[0]),36);
			final String bgCol = Integer.toString(Integer.parseInt(infos[1]),36);
			final String embID =  Integer.toString(Integer.parseInt(infos[2]),36);
			final String embCol =  Integer.toString(Integer.parseInt(infos[3]),36);
			final String name = infos[4];
			if(World.guildNameIsUsed(name))
			{
				SocketManager.GAME_SEND_gC_PACKET(_player, "Ean");
				return;
			}
			
			//Validation du nom de la guilde
			final String tempName = name.toLowerCase();
			boolean isValid = true;
			//Vérifie d'abord si il contient des termes définit
			if(tempName.length() > 20
					|| tempName.contains("mj")
					|| tempName.contains("modo")
					|| tempName.contains("admin"))
			{
				isValid = false;
			}
			//Si le nom passe le test, on vérifie que les caractère entré sont correct.
			if(isValid)
			{
				int tiretCount = 0;
				for(final char curLetter : tempName.toCharArray())
				{
					if(!((curLetter >= 'a' && curLetter <= 'z')
							|| curLetter == '-') || curLetter == ' ')
					{
						isValid = false;
						break;
					}
					if(curLetter == '-')
					{
						if(tiretCount >= 2)
						{
							isValid = false;
							break;
						}
						else
						{
							tiretCount++;
						}
					}
				}
			}
			//Si le nom est invalide
			if(!isValid)
			{
				SocketManager.GAME_SEND_gC_PACKET(_player, "Ean");
				return;
			}
			//FIN de la validation
			final String emblem = bgID+","+bgCol+","+embID+","+embCol;//9,6o5nc,2c,0;
			if(World.guildEmblemIsUsed(emblem))
			{
				SocketManager.GAME_SEND_gC_PACKET(_player, "Eae");
				return;
			}
			if(_player.getCurMap().getId() == 2196)//Temple de création de guilde
			{
				if(!_player.hasItemTemplate(1575,1))//Guildalogemme
				{
					SocketManager.GAME_SEND_Im_PACKET(_player, "14");
					return;
				}
				_player.removeByTemplateID(1575, 1);
			}
			final Guild G = new Guild(_player,name,emblem);
			final GuildMember gm = G.addNewMember(_player);
			gm.setAllRights(1,(byte) 0,1);//1 => Meneur (Tous droits)
			_player.setGuildMember(gm);//On ajoute le meneur
			World.addGuild(G, true);
			SQLManager.UPDATE_GUILDMEMBER(gm);
			//Packets
			SocketManager.GAME_SEND_gS_PACKET(_player, gm);
			SocketManager.GAME_SEND_gC_PACKET(_player,"K");
			SocketManager.GAME_SEND_gV_PACKET(_player);
		}catch(final Exception e){return;};
	}

	private void parseChannelPacket(final String packet)
	{
		switch(packet.charAt(1))
		{
			case 'C'://Changement des Canaux
				Channels_change(packet);
			break;
			
			default:
				Console.printlnError("Unknown packet recv : c" + packet);
				SocketManager.GAME_SEND_BN(_out);
				break;
		}
	}

	private void Channels_change(final String packet)
	{
		final String chan = packet.charAt(3)+"";
		switch(packet.charAt(2))
		{
			case '+'://Ajout du Canal
				_player.addChannel(chan);
			break;
			case '-'://Desactivation du canal
				_player.removeChannel(chan);
			break;
		}
		SQLManager.SAVE_PERSONNAGE(_player, false);
	}

	private void parseMountPacket(final String packet)
	{
		switch(packet.charAt(1))
		{
			case 'c'://Castre la monture
				Mount_castration();
			break;
			
			case 'd'://Demande Description
				Mount_description(packet);
			break;
			
			case 'f'://Libère la monture
				Mount_free();
			break;
			
			case 'n'://Change le nom
				Mount_name(packet.substring(2));
			break;
			
			case 'r'://Monter sur la dinde
				Mount_ride();
			break;
			
			case 'x'://Change l'xp donner a la dinde
				Mount_changeXpGive(packet);
			break;
			
			default:
				Console.printlnError("Unknown packet recv : " + packet);
				SocketManager.GAME_SEND_BN(_out);
				break;
		}
	}

	private void Mount_free() {
		if(_player.getMount() != null && _player.isOnMount()) _player.toogleOnMount();
		SocketManager.GAME_SEND_Re_PACKET(_player, "-", _player.getMount());
		SQLManager.DELETE_MOUNT(_player);
    }

	private void Mount_castration() {
		_player.getMount().setCastre();
		SocketManager.GAME_SEND_Re_PACKET(_player, "+", _player.getMount());
    }

	private void Mount_changeXpGive(final String packet)
	{
		try
		{
			int xp = Integer.parseInt(packet.substring(2));
			if(xp <0)xp = 0;
			if(xp >90)xp = 90;
			_player.setMountGiveXp(xp);
			SocketManager.GAME_SEND_Rx_PACKET(_player);
		}catch(final Exception e){};
	}

	private void Mount_name(final String name)
	{
		if(_player.getMount() == null)return;
		_player.getMount().setName(name);
		SocketManager.GAME_SEND_Rn_PACKET(_player, name);
	}

	private void Mount_ride()
	{
		if(_player.getLvl()<60 || _player.getMount() == null || !_player.getMount().isMountable())
		{
			SocketManager.GAME_SEND_Re_PACKET(_player,"Er", null);
			return;
		}
		_player.toogleOnMount();
	}

	private void Mount_description(final String packet)
	{
		int DDid = -1;
		try
		{
			DDid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			//on ignore le temps?
		}catch(final Exception e){};
		if(DDid == -1)return;
		final Mount DD = World.getDragoByID(DDid);
		if(DD == null)return;
		SocketManager.GAME_SEND_MOUNT_DESCRIPTION_PACKET(_player,DD);
	}

	private void parseFriendPacket(final String packet)
	{
		switch(packet.charAt(1))
		{
		case 'A'://Ajouter
			Friend_add(packet);
			break;
		case 'D'://Effacer un ami
			Friend_delete(packet);
			break;
		case 'L'://Liste
			SocketManager.GAME_SEND_FRIENDLIST_PACKET(_player);
			if(_player.haveSpouse()) SocketManager.GAME_SEND_SPOUSE_PACKET(_player);
			break;
		case 'O'://
			switch (packet.charAt(2)) {
			case '-':
				_account.setShowFriendConnection(false);
				SocketManager.GAME_SEND_BN(_player);
				break;
			case '+':
				_account.setShowFriendConnection(true);
				SocketManager.GAME_SEND_BN(_player);
				break;
			}
			SQLManager.UPDATE_ACCOUNT_DATA(_account);
			break;
		case 'J'://Join/Spouse
			Spouse_join(packet);
			break;
		default:
			Console.printlnError("Unknown packet recv : " + packet);
			SocketManager.GAME_SEND_BN(_out);
			break;
		}
	}

	private void Spouse_join(final String packet) {
		if(!_player.haveSpouse())
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "138");
			return;
		}
		final Player spouse = _player.getSpouse().getPlayer();
		if (!spouse.isOnline())
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "1"+(36+spouse.getSexe()));
			return;
		}
		switch(packet.charAt(2))
		{
		case 'C':
			switch(packet.charAt(3))
			{
			case '+':
				_player.getSpouse().setFollowing(true);
				SocketManager.GAME_SEND_INFOS_COMPASS_PACKET(_player, spouse);
				break;
			case '-':
				_player.getSpouse().setFollowing(false);
				SocketManager.GAME_SEND_INFOS_COMPASS_PACKET(_player, "null");
				break;
			}
		break;
		case 'S':
			_player.getSpouse().join();
		break;
		}
	}

	private void Friend_delete(String packet) {
		if(_player == null)return;
		int guid = -1;
		switch(packet.charAt(2))
		{
			case '%'://nom de perso
				packet = packet.substring(3);
				final Player P = World.getPersoByName(packet);
				if(P == null)//Si P est nul, ou si P est nonNul et P offline
				{
					SocketManager.GAME_SEND_FD_PACKET(_player, "Ef");
					return;
				}
				guid = P.getAccID();
				
			break;
			case '*'://Pseudo
				packet = packet.substring(3);
				final Account C = World.getCompteByPseudo(packet);
				if(C==null)
				{
					SocketManager.GAME_SEND_FD_PACKET(_player, "Ef");
					return;
				}
				guid = C.getGUID();
			break;
			default:
				packet = packet.substring(2);
				final Player Pr = World.getPersoByName(packet);
				if(Pr == null?true:!Pr.isOnline())//Si P est nul, ou si P est nonNul et P offline
				{
					SocketManager.GAME_SEND_FD_PACKET(_player, "Ef");
					return;
				}
				guid = Pr.getAccount().getGUID();
			break;
		}
		if(guid == -1 || !_account.isFriendWith(guid))
		{
			SocketManager.GAME_SEND_FD_PACKET(_player, "Ef");
			return;
		}
		_account.removeFriend(guid);
	}

	private void Friend_add(String packet)
	{
		if(_player == null)return;
		int guid = -1;
		switch(packet.charAt(2))
		{
			case '%'://nom de perso
				packet = packet.substring(3);
				final Player P = World.getPersoByName(packet);
				if(P == null?true:!P.isOnline())//Si P est nul, ou si P est nonNul et P offline
				{
					SocketManager.GAME_SEND_FA_PACKET(_player, "Ef");
					return;
				}
				guid = P.getAccID();
			break;
			case '*'://Pseudo
				packet = packet.substring(3);
				final Account C = World.getCompteByPseudo(packet);
				if(C==null?true:!C.isOnline())
				{
					SocketManager.GAME_SEND_FA_PACKET(_player, "Ef");
					return;
				}
				guid = C.getGUID();
			break;
			default:
				packet = packet.substring(2);
				final Player Pr = World.getPersoByName(packet);
				if(Pr == null?true:!Pr.isOnline())//Si P est nul, ou si P est nonNul et P offline
				{
					SocketManager.GAME_SEND_FA_PACKET(_player, "Ef");
					return;
				}
				guid = Pr.getAccount().getGUID();
			break;
		}
		if(guid == -1)
		{
			SocketManager.GAME_SEND_FA_PACKET(_player, "Ef");
			return;
		}
		_account.addFriend(guid);
	}

	private void parsePartyPacket(final String packet)
	{
		switch(packet.charAt(1))
		{
			case 'A'://Accepter invitation
				group_accept(packet);
			break;
			
			case 'I'://inviation
				group_invite(packet);
			break;
			
			case 'R'://Refuse
				group_refuse();
			break;
			
			case 'V'://Quitter
				group_quit(packet);
			break;
			
			default:
				Console.printlnError("Unknown packet recv : " + packet);
				SocketManager.GAME_SEND_BN(_out);
				break;
		}
	}

	private void group_quit(final String packet)
	{
		if(_player == null)return;
		final Party g = _player.getParty();
		if(g == null)return;
		if(packet.length() == 2)//Si aucun guid est spécifié, alors c'est que le joueur quitte
		{
			 g.leave(_player);
			 SocketManager.GAME_SEND_PV_PACKET(_out,"");
		}else if(g.isChief(_player.getActorId()))//Sinon, c'est qu'il kick un joueur du groupe
		{
			int guid = -1;
			try
			{
				guid = Integer.parseInt(packet.substring(2));
			}catch(final NumberFormatException e){return;};
			if(guid == -1)return;
			final Player t = World.getPlayer(guid);
			g.leave(t);
			SocketManager.GAME_SEND_PV_PACKET(t.getAccount().getGameThread().getOut(),""+_player.getActorId());
		}
	}

	private void group_invite(final String packet)
	{
		if(_player == null)return;
		final String name = packet.substring(2);
		final Player target = World.getPersoByName(name);
		if(target == null)return;
		if(!target.isOnline())
		{
			SocketManager.GAME_SEND_PARTY_INVITATION_ERROR(_out,"n"+name);	//Erreur : X n'est pas connecté
			return;
		}
		if(target.getParty() != null)
		{
			SocketManager.GAME_SEND_PARTY_INVITATION_ERROR(_out, "a"+name);	//Erreur : X fait déjà parti d'un groupe
			return;
		}
		if(_player.getParty() != null && _player.getParty().getMemberNumber() == 8)
		{
			SocketManager.GAME_SEND_PARTY_INVITATION_ERROR(_out, "f");	//Erreur : Le groupe est plein
			return;
		}
		target.setInvitation(_player.getActorId());	
		_player.setInvitation(target.getActorId());
		SocketManager.GAME_SEND_PARTY_INVITATION(_out,_player.getName(),name);
		SocketManager.GAME_SEND_PARTY_INVITATION(target.getAccount().getGameThread().getOut(),_player.getName(),name);
	}

	private void group_refuse()
	{
		if(_player == null)return;
		if(_player.getInvitation() == 0)return;
		SocketManager.GAME_SEND_BN(_out);
		final Player t = World.getPlayer(_player.getInvitation());
		_player.setInvitation(0);
		t.setInvitation(0);
		SocketManager.GAME_SEND_PR_PACKET(t);
	}

	private void group_accept(final String packet)
	{
		if(_player == null)return;
		if(_player.getInvitation() == 0)return;
		final Player t = World.getPlayer(_player.getInvitation());
		Party party = t.getParty();
		if(party == null)
		{
			party = new Party(t,_player);
			SocketManager.GAME_SEND_PARTY_CREATE(_out,party);
			SocketManager.GAME_SEND_PL_PACKET(_out,party);
			SocketManager.GAME_SEND_PARTY_CREATE(t.getAccount().getGameThread().getOut(),party);
			SocketManager.GAME_SEND_PL_PACKET(t.getAccount().getGameThread().getOut(),party);
			t.setParty(party);
			SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(t.getAccount().getGameThread().getOut(),party);
		}
		else
		{
			SocketManager.GAME_SEND_PARTY_CREATE(_out,party);
			SocketManager.GAME_SEND_PL_PACKET(_out,party);
			SocketManager.GAME_SEND_PM_ADD_PACKET_TO_PARTY(party, _player);
			party.addMember(_player);
		}
		_player.setParty(party);
		SocketManager.GAME_SEND_ALL_PM_ADD_PACKET(_out,party);
		SocketManager.GAME_SEND_PR_PACKET(t);
	}

	private void parseObjectPacket(final String packet)
	{
		_player.clearCacheAS();
		_player.clearCacheGMStuff();
		switch(packet.charAt(1))
		{
			case 'd'://Supression d'un objet
				Object_delete(packet);
			break;
			
			case 'D'://Depose l'objet au sol
				Object_drop(packet);
			break;
			
			case 'M'://Bouger un objet (Equiper/déséquiper)
				Object_move(packet);
			break;
			
			case 'U'://Utiliser un objet (potions)
				Object_use(packet);
			break;
			
			case 'x'://Dissocier (Obvijevan)
				Object_dissociate(packet);
			break;
			
			case 's'://Changer Apparence (Obvijevan)
				Object_ChangeSkin(packet);
			break;
			
			case 'f'://Changer Apparence (Obvijevan)
				Object_eat(packet);
			break;
			
			default:
				Console.printlnError("Unknown packet recv : " + packet);
				SocketManager.GAME_SEND_BN(_out);
				break;
		}
	}
	
	private void Object_dissociate(final String packet) {
		final String[] infos = packet.substring(2).split(""+(char)0x0A)[0].split("\\|");
		try {
			final int guid = Integer.parseInt(infos[0]);
			final Item Obj = World.getObjet(guid);

			if (Obj.is_linked()) {
				final Speaking Obv = Obj.get_linkedItem();		
				Obj.setUnlinkedItem();
				Obv.setUnlinkedItem();
				_player.addItem(Obv);

				SQLManager.SAVE_PERSONNAGE(_player, false);
				SQLManager.UPDATE_SPEAKING(Obv);

				SocketManager.start_buffering(_out);
				SocketManager.GAME_SEND_OAKO_PACKET(_player, Obv);
				SocketManager.GAME_SEND_OCO_PACKET(_player, Obj);
				SocketManager.GAME_SEND_Ow_PACKET(_player);

				_player.refreshStats();
				if (_player.getParty() != null)
					SocketManager.GAME_SEND_PM_MOD_PACKET_TO_PARTY(_player.getParty(), _player);
				SocketManager.GAME_SEND_STATS_PACKET(_player);
				SocketManager.GAME_SEND_ON_EQUIP_ITEM(_player.getCurMap(), _player);

				if (Obj.getTemplate().getPanopID() > 0)
					SocketManager.GAME_SEND_OS_PACKET(_player, Obj.getTemplate().getPanopID());
				SocketManager.stop_buffering(_out);
			}
		} catch (final Exception e) {
			SocketManager.GAME_SEND_BN(_player);
			SocketManager.stop_buffering(_out);
			return;
		}
	}

	private void Object_eat(final String packet) {
		final String[] infos = packet.substring(2).split(""+(char)0x0A)[0].split("\\|");
		try {
			final int guid = Integer.parseInt(infos[0]);
			final int foodID = Integer.parseInt(infos[2]);
			final Item Obj = World.getObjet(guid);
			final Item Food = World.getObjet(foodID);

			final Speaking Obv = Obj.get_linkedItem();

			SocketManager.start_buffering(_out);
			if (Obv == null) {
				System.out.println("Target Object null");
				SocketManager.GAME_SEND_BN(this._player);
				SocketManager.stop_buffering(_out);
				return;
			}
			if (Food == null) {
				System.out.println("Nourriture Object null");
				SocketManager.GAME_SEND_BN(this._player);
				SocketManager.stop_buffering(_out);
				return;
			}
			if (Obj.getTemplate().getType() != Obv.getTemplate().get_obviType()) {
				System.out.println("Mauvaise nourriture");
				SocketManager.GAME_SEND_BN(this._player);
				SocketManager.stop_buffering(_out);
				return;
			}
			if (Obv.eatItem(_player, Food)) {
				SQLManager.UPDATE_SPEAKING(Obv);

				SocketManager.GAME_SEND_OCO_PACKET(_player, Obj);
				SocketManager.GAME_SEND_Ow_PACKET(_player);
				_player.refreshStats();
				if (_player.getParty() != null) 
					SocketManager.GAME_SEND_PM_MOD_PACKET_TO_PARTY(_player.getParty(), _player);
				SocketManager.GAME_SEND_STATS_PACKET(_player);
				SocketManager.GAME_SEND_ON_EQUIP_ITEM(_player.getCurMap(), _player);

				if (Obj.getTemplate().getPanopID() > 0)
					SocketManager.GAME_SEND_OS_PACKET(_player, Obj.getTemplate().getPanopID());
				SocketManager.GAME_SEND_BN(_player);
			} else {
				System.out.println("Ne peut pas être nourri");
				SocketManager.GAME_SEND_BN(_player);
				return;
			}
		} catch (final Exception e) {
			SocketManager.GAME_SEND_BN(this._player);
			System.out.println("Erreur globale: " + e.getMessage() + "& \n"  + e.getCause());
			SocketManager.stop_buffering(_out);
			return;
		}
		SocketManager.stop_buffering(_out);
	}

	private void Object_ChangeSkin(final String packet) {
		final String[] infos = packet.substring(2).split(""+(char)0x0A)[0].split("\\|");
		try {
			final int guid = Integer.parseInt(infos[0]);
			final int skinTarget = Integer.parseInt(infos[2]);
			final Item Obj = World.getObjet(guid);
			Speaking Obv = null;
			
			if (Obj.isSpeaking())
				Obv = Speaking.toSpeaking(Obj);
			else if (Obj.is_linked()) 
				Obv = Obj.get_linkedItem();
			
			if (skinTarget < 0 || skinTarget > 20 || Obv == null || /*skinTarget > Obv.get_lvl() ||*/ skinTarget == Obv.getSelectedLevel()) {
				SocketManager.GAME_SEND_BN(_player);
				return;
			}

			Obv.setSelectedLevel(skinTarget);
			SQLManager.UPDATE_SPEAKING(Obv);

			SocketManager.start_buffering(_out);
			if (Obj.is_linked())
				SocketManager.GAME_SEND_OCO_PACKET(_player, Obj);
			else
				SocketManager.GAME_SEND_OCO_PACKET(_player, Obv);

			SocketManager.GAME_SEND_Ow_PACKET(_player);
			_player.refreshStats();
			if (_player.getParty() != null) 
				SocketManager.GAME_SEND_PM_MOD_PACKET_TO_PARTY(_player.getParty(), _player);
			SocketManager.GAME_SEND_STATS_PACKET(_player);
			SocketManager.GAME_SEND_ON_EQUIP_ITEM(_player.getCurMap(), _player);

			if (Obj.getTemplate().getPanopID() > 0)
				SocketManager.GAME_SEND_OS_PACKET(_player, Obj.getTemplate().getPanopID());
			SocketManager.GAME_SEND_BN(_player);
			SocketManager.stop_buffering(_out);
		} catch (final Exception e) {
			SocketManager.GAME_SEND_BN(_player);
			SocketManager.stop_buffering(_out);
			return;
		}
	}

	private void Object_drop(final String packet)
	{
		int guid = -1;
		int qua = -1;
		try
		{
			guid = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			qua = Integer.parseInt(packet.split("\\|")[1]);
		}catch(final Exception e){};
		if(guid == -1 || qua <= 0 || !_player.hasItemGuid(guid))return;
		final Item obj = World.getObjet(guid);
		
		if(qua >= obj.getQuantity())
		{
			_player.removeItem(guid);
			_player.getCurCell().addDroppedItem(obj);
			obj.setPosition(Constants.ITEM_POS_NO_EQUIPED);
			SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_player, guid);
		}else
		{
			obj.setQuantity(obj.getQuantity() - qua);
			final Item obj2 = Item.getCloneObjet(obj, qua);
			obj2.setPosition(Constants.ITEM_POS_NO_EQUIPED);
			_player.getCurCell().addDroppedItem(obj2);
			SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_player, obj);
		}
		
		SocketManager.GAME_SEND_Ow_PACKET(_player);
		SocketManager.GAME_SEND_GDO_PACKET_TO_MAP(_player.getCurMap(),'+',_player.getCurCell().getId(),obj.getTemplate().getID(),0);
		SocketManager.GAME_SEND_STATS_PACKET(_player);
	}

	private void Object_use(final String packet)
	{
		int guid = -1;
		try
		{
			final String[] infos = packet.substring(2).split("\\|");
			guid = Integer.parseInt(infos[0]);
		}catch(final Exception e){return;};
		//Si le joueur n'a pas l'objet
		if(!_player.hasItemGuid(guid))return;
		final Item obj = World.getObjet(guid);
		final ItemTemplate T = obj.getTemplate();
		if(!obj.getTemplate().getConditions().equalsIgnoreCase("") && !ConditionParser.validConditions(_player,obj.getTemplate().getConditions()))
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "119|43");
			return;
		}
		if(_player.getLastItemUsed() != T.getID())
			_player.setLastItemUsed(T.getID());
		T.applyAction(_player,guid);
		switch(T.getType())
		{
		case Constants.ITEM_TYPE_PAIN:
		case Constants.ITEM_TYPE_VIANDE_COMESTIBLE:
			SocketManager.GAME_SEND_EMOTE_TO_MAP(_player.getCurMap(), _player.getActorId(), 17);
			break;
		case Constants.ITEM_TYPE_BIERE:
			SocketManager.GAME_SEND_EMOTE_TO_MAP(_player.getCurMap(), _player.getActorId(), 18);
			break;
		case Constants.ITEM_TYPE_PARCHEMIN_SORT:
			SocketManager.GAME_SEND_SPELL_LIST(_player);
			break;
		}
	}

	private synchronized void Object_move(final String packet)
	{
		final String[] infos = packet.substring(2).split(""+(char)0x0A)[0].split("\\|");
		try
		{
			final int guid = Integer.parseInt(infos[0]);
			final int pos = Integer.parseInt(infos[1]);
			final Item obj = World.getObjet(guid);
			if(!_player.hasItemGuid(guid) || obj == null)
				return;
			if (_player.getFight() != null && _player.getFight().getState() > 2)
				return;
			
			int type = -1;
			if (obj.isSpeaking())
				type = Speaking.toSpeaking(obj).getType();
			else
				type = obj.getTemplate().getType();
			
			if(!Constants.isValidPlaceForItem(type, pos) && pos != Constants.ITEM_POS_NO_EQUIPED)
				return;
			
			if(!obj.getTemplate().getConditions().equalsIgnoreCase("") && !ConditionParser.validConditions(_player,obj.getTemplate().getConditions()))
			{
				SocketManager.GAME_SEND_Im_PACKET(_player, "119|43");
				return;
			}
			
			if (obj.getTemplate().getLevel() > this._player.getLvl()) {
				SocketManager.GAME_SEND_OAEL_PACKET(this._out);
				return;
			}

			//On ne peut équiper 2 items de panoplies identiques, ou 2 Dofus identiques
			if(pos != Constants.ITEM_POS_NO_EQUIPED && (obj.getTemplate().getPanopID() != -1 ||
			type == Constants.ITEM_TYPE_DOFUS)&& _player.hasEquiped(obj.getTemplate().getID()))
			return;
			
			final Item exObj = _player.getObjetByPos(pos);
			SocketManager.start_buffering(_out);
			if (obj.isSpeaking() && !Speaking.toSpeaking(obj).hasLinkedItem()) {
				final Speaking Obv = Speaking.toSpeaking(obj);
				
				if (exObj == null) {
					SocketManager.GAME_SEND_Im_PACKET(_player, "1161");
					SocketManager.GAME_SEND_BN(_player);
					SocketManager.stop_buffering(_out);
					return;
				}
				
				if (exObj.isSpeaking() || exObj.is_linked()) {
					SocketManager.GAME_SEND_BN(_player);
					SocketManager.stop_buffering(_out);
					return;
				}

				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_player, obj.getGuid());
				Obv.setHasLinked(exObj);
				exObj.setLinkedItem(Obv);
				_player.removeItem(Obv.getGuid());

				SQLManager.SAVE_PERSONNAGE(_player, false);
				SQLManager.UPDATE_SPEAKING(Obv);

				SocketManager.GAME_SEND_OCO_PACKET(_player, exObj);
				if (exObj.getTemplate().getPanopID() > 0)
					SocketManager.GAME_SEND_OS_PACKET(_player, exObj.getTemplate().getPanopID());
			} else 
			{
				if(pos == Constants.ITEM_POS_FAMILIER)
				{
					if(_player.getObjetByPos(Constants.ITEM_POS_FAMILIER) != null)
					{
						final Pet pet = (Pet)_player.getObjetByPos(Constants.ITEM_POS_FAMILIER);
						if(pet == null)
						{
							SocketManager.GAME_SEND_BN(_out);
							return;
						}
						pet.eat(_player, obj);
					}
				}
				if(exObj != null)//S'il y avait déja un objet => Ne devrait pas arriver, le client envoie déséquiper avant
				{
					Item obj2;
					if(!exObj.is_linked() && (obj2 = _player.getSimilarItem(exObj)) != null)
					{
						obj2.setQuantity(obj2.getQuantity()+1);
						SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_player, obj2);
						World.removeItem(exObj.getGuid());
						_player.removeItem(exObj.getGuid());
						SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_player, exObj.getGuid());
					}
					else
					{
						exObj.setPosition(Constants.ITEM_POS_NO_EQUIPED);
						SocketManager.GAME_SEND_OBJET_MOVE_PACKET(_player,exObj);
					}
					if(_player.getObjetByPos(Constants.ITEM_POS_ARME) == null)
						SocketManager.GAME_SEND_OT_PACKET(_out, -1);
					
					//Si objet de panoplie
					if(exObj.getTemplate().getPanopID() > 0)SocketManager.GAME_SEND_OS_PACKET(_player,exObj.getTemplate().getPanopID());
				}
				if(obj.getTemplate().getLevel() > _player.getLvl()) {
					SocketManager.GAME_SEND_OAEL_PACKET(_out);
					SocketManager.stop_buffering(_out);
					return;
				}
				Item obj2;
				if(!obj.is_linked() && (obj2 = _player.getSimilarItem(obj)) != null)
				{
					obj2.setQuantity(obj2.getQuantity()+1);
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_player, obj2);
					World.removeItem(obj.getGuid());
					_player.removeItem(obj.getGuid());
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_player, obj.getGuid());
				}
				else
				{
					obj.setPosition(pos);
					SocketManager.GAME_SEND_OBJET_MOVE_PACKET(_player,obj);
					if(obj.getQuantity() > 1)
					{
						final int newItemQua = obj.getQuantity()-1;
						final Item newItem = Item.getCloneObjet(obj,newItemQua);
						_player.addItem(newItem,false);
						World.addItem(newItem,true);
						obj.setQuantity(1);
						SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_player, obj);
					}
				}
			}//getNumbEquipedItemOfPanoplie(exObj.getTemplate().getPanopID()	
			SocketManager.GAME_SEND_Ow_PACKET(_player);
			_player.refreshStats();
			if(_player.getParty() != null)
			{
				SocketManager.GAME_SEND_PM_MOD_PACKET_TO_PARTY(_player.getParty(),_player);
			}
			SocketManager.GAME_SEND_STATS_PACKET(_player);
			
			if(pos == Constants.ITEM_POS_ARME 		||
				pos == Constants.ITEM_POS_COIFFE 	||
				pos == Constants.ITEM_POS_FAMILIER 	||
				pos == Constants.ITEM_POS_CAPE		||
				pos == Constants.ITEM_POS_BOUCLIER	||
				pos == Constants.ITEM_POS_NO_EQUIPED) {
				SocketManager.GAME_SEND_ON_EQUIP_ITEM(_player.getCurMap(), _player);
			}
		
			//Si familier
			if(pos == Constants.ITEM_POS_FAMILIER && _player.isOnMount())_player.toogleOnMount();
			//Verif pour les outils de métier
			if(pos == Constants.ITEM_POS_NO_EQUIPED && _player.getObjetByPos(Constants.ITEM_POS_ARME) == null)
				SocketManager.GAME_SEND_OT_PACKET(_out, -1);
			
			if(pos == Constants.ITEM_POS_ARME)
			{
				final int ID = _player.getObjetByPos(Constants.ITEM_POS_ARME).getTemplate().getID();
				for(final Entry<Integer,JobStat> e : _player.getMetiers().entrySet())
				{
					if(e.getValue().getTemplate().isValidTool(ID))
						SocketManager.GAME_SEND_OT_PACKET(_out,e.getValue().getTemplate().getId());
				}
			}
			//Si objet de panoplie
			if(obj.getTemplate().getPanopID() > 0)
				SocketManager.GAME_SEND_OS_PACKET(_player,obj.getTemplate().getPanopID());
			if (_player.getFight() != null)
				SocketManager.GAME_SEND_ON_EQUIP_ITEM_FIGHT(_player, _player.getFight().getFighterByPerso(_player), _player.getFight());
		}catch(final Exception e)
		{
			e.printStackTrace();
			SocketManager.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(_out);
		}
		SocketManager.stop_buffering(_out);
	}

	private void Object_delete(final String packet)
	{
		final String[] infos = packet.substring(2).split("\\|");
		try
		{
			final int guid = Integer.parseInt(infos[0]);
			int qua = 1;
			try
			{
				qua = Integer.parseInt(infos[1]);
			}catch(final Exception e){};
			final Item obj = World.getObjet(guid);
			if(obj == null || !_player.hasItemGuid(guid) || qua <= 0)
			{
				SocketManager.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(_out);
				return;
			}
			final int newQua = obj.getQuantity()-qua;
			if(newQua <=0)
			{
				_player.removeItem(guid);
				World.removeItem(guid);
				SQLManager.DELETE_ITEM(guid);
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_player, guid);
			}else
			{
				obj.setQuantity(newQua);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_player, obj);
			}
			SocketManager.GAME_SEND_STATS_PACKET(_player);
			SocketManager.GAME_SEND_Ow_PACKET(_player);
		}catch(final Exception e)
		{
			SocketManager.GAME_SEND_DELETE_OBJECT_FAILED_PACKET(_out);
		}
	}

	private void parseDialogPacket(final String packet)
	{
		switch(packet.charAt(1))
		{
			case 'C'://Demande de l'initQuestion
				Dialog_start(packet);
			break;
			
			case 'R'://Réponse du joueur
				Dialog_response(packet);
			break;
			
			case 'V'://Fin du dialog
				Dialog_end();
			break;
		}
	}

	private void Dialog_response(final String packet)
	{
		final String[] infos = packet.substring(2).split("\\|");
		try
		{
			final int qID = Integer.parseInt(infos[0]);
			final int rID = Integer.parseInt(infos[1]);
			final NpcQuestion quest = World.getNPCQuestion(qID);
			final NpcResponse rep = World.getNPCreponse(rID);
			final Npc npc = _player.getCurMap().getNPC(_player.getIsTalkingWith());
			if(npc == null) 
			{
				SocketManager.GAME_SEND_M_PACKET(_out, "17", "");
				return;
			}
			if(quest == null || rep == null || !rep.isAnotherDialog())
			{
				SocketManager.GAME_SEND_END_DIALOG_PACKET(_out);
				_player.setIsTalkingWith(0);
			}
			rep.apply(_player);
		}catch(final Exception e)
		{
			SocketManager.GAME_SEND_END_DIALOG_PACKET(_out);
		}
	}

	private void Dialog_end()
	{
		SocketManager.GAME_SEND_END_DIALOG_PACKET(_out);
		if(_player.getIsTalkingWith() != 0)
			_player.setIsTalkingWith(0);
	}

	private void Dialog_start(final String packet)
	{
		try
		{
			final int npcID = Integer.parseInt(packet.substring(2).split((char)0x0A+"")[0]);
			final Npc npc = _player.getCurMap().getNPC(npcID);
			final TaxCollector taxCollector = _player.getCurMap().getPercepteur();
			if(taxCollector != null && taxCollector.getActorId() == npcID)
			{
				SocketManager.GAME_SEND_DCK_PACKET(_out, npcID);
				SocketManager.GAME_SEND_QUESTION_PACKET(_out, taxCollector.getGuild().parseQuestionTaxCollector());
				return;
			}
			if(npc == null)return;
			SocketManager.GAME_SEND_DCK_PACKET(_out,npcID);
			final int qID = npc.getTemplate().get_initQuestionID();
			final NpcQuestion quest = World.getNPCQuestion(qID);
			if(quest == null)
			{
				SocketManager.GAME_SEND_END_DIALOG_PACKET(_out);
				return;
			}
			SocketManager.GAME_SEND_QUESTION_PACKET(_out,quest.parseToDQPacket(_player));
			_player.setIsTalkingWith(npcID);
		}catch(final NumberFormatException e){};
	}

	private void parseExchangePacket(final String packet)
	{	
		switch(packet.charAt(1))
		{
			case 'A'://Accepter demande d'échange
				Exchange_accept();
			break;
			case 'B'://Achat
				Exchange_onBuyItem(packet);
			break;
			
			case 'H': //Hotel De Vente
				Exchange_HDV(packet);
			break;
				
			case 'K'://Ok
				Exchange_isOK();
			break;
			case 'L'://jobAction : Refaire le craft précedent
				Exchange_doAgain();
			break;
			
			case 'M'://Move (Ajouter//retirer un objet a l'échange)
				Exchange_onMoveItem(packet);
			break;
			
			case 'q'://AskOfflineExchange
				Exchange_merchantTax();
			break;
			
			case 'Q':
				Exchange_beMerchant();
			break;
			
			case 'r'://Rides => Monture
				Exchange_mountPark(packet);
			break;
			
			case 'R'://liste d'achat NPC
				Exchange_start(packet);
			break;
			
			case 'S'://Vente
				Exchange_onSellItem(packet);
			break;
			
			case 'V'://Fin de l'échange
				Exchange_finish_buy(packet);
			break;
			
			default:
				Console.printlnError("Unknown packet recv : " + packet);
				SocketManager.GAME_SEND_BN(_out);
				break;
		}
	}
	
	private void Exchange_merchantTax() {
		long kamas = 0;
		for(final int kamasToAdd : _player.getStoreItems().values())
        {
            kamas += kamasToAdd;
        }
        kamas = (long)(kamas * 0.01);
        SocketManager.GAME_SEND_ASK_OFFLINE_EXCHANGE_PACKET(_player, 0.01, kamas);
        if(kamas > _player.getKamas()) {
        	SocketManager.GAME_SEND_Im_PACKET(_player, "176");
			return;
        }
        _player.setKamas(_player.getKamas() - kamas);
	}

	private void Exchange_beMerchant() {
		if(_player.getIsTradingWith() > 0 || _player.getFight() != null || _player.isAway())return;
		if (_player.getCurMap().getStoreCount() >= 5)
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "125;5");
			return;
		}
		if (_player.parseStoreItemsList().isEmpty())
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "123");
			return;
		}
		if(_player.getCurCell().getObject() != null)
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "124");
			return;
		}
		final int orientation = Formulas.getRandomValue(1, 3);
		_player.setOrientation(orientation);
		final DofusMap map = _player.getCurMap();
		_player.setShowSeller(true);
		World.addSeller(_player);
		kick();
		for(final Player z : map.getPersos())
		{
			if(z != null && z.isOnline())
				SocketManager.GAME_SEND_MERCHANT_LIST(z, z.getCurMap().getId());
		}
	}
	
	private void Exchange_HDV(final String packet)
	{
		if(_player.getIsTradingWith() > 0 || _player.getFight() != null || _player.isAway())return;
		int templateID;
		switch(packet.charAt(2))
		{
			case 'B': 	//Confirmation d'achat
				final String[] info = packet.substring(3).split("\\|");	//ligneID|amount|price
				
				final BigStore curHdv = World.getHdv(Math.abs(_player.getIsTradingWith()));
				
				final int ligneID = Integer.parseInt(info[0]);
				final byte amount = Byte.parseByte(info[1]);
				final int price = Integer.parseInt(info[2]);
				
				final Item toBuy = curHdv.getLigne(ligneID).doYouHave(amount, price).getObjet();
				
				if(_player.getKamas() < price)
				{
					SocketManager.GAME_SEND_Im_PACKET(_player,"171");
					return;
				}
				
				if(_player.getPodUsed() + amount*toBuy.getTemplate().getPod() > _player.getMaxPod())
				{
					SocketManager.GAME_SEND_Im_PACKET(_player, "062"); //Envoi le message "Action annulé pour cause de surchage"
					return;
				}
				
				if(curHdv.buyItem(ligneID,amount,price,_player))
				{
					SocketManager.GAME_SEND_EHm_PACKET(_player,"-",ligneID+"");	//Enleve la ligne
					if(curHdv.getLigne(ligneID) != null && !curHdv.getLigne(ligneID).isEmpty())
						SocketManager.GAME_SEND_EHm_PACKET(_player, "+", curHdv.getLigne(ligneID).parseToEHm()); //Réajoute la ligne si elle n'est pas vide
									
					_player.refreshStats();
					SocketManager.GAME_SEND_Ow_PACKET(_player);
					SocketManager.GAME_SEND_Im_PACKET(_player,"068");	//Envoie le message "Lot acheté"
				}
				else
				{
					SocketManager.GAME_SEND_Im_PACKET(_player,"172");	//Envoie un message d'erreur d'achat
				}
			break;
			case 'l':	//Demande listage d'un template (les prix)
				templateID = Integer.parseInt(packet.substring(3));
				try
				{
					SocketManager.GAME_SEND_EHl(_player,World.getHdv(Math.abs(_player.getIsTradingWith())),templateID);
				}catch(final NullPointerException e)	//Si erreur il y a, retire le template de la liste chez le client
				{
					SocketManager.GAME_SEND_EHM_PACKET(_player,"-",templateID+"");
				}
				
			break;
			case 'P':	//Demande des prix moyen
				templateID = Integer.parseInt(packet.substring(3));
				SocketManager.GAME_SEND_EHP_PACKET(_player,templateID);
			break;			
			case 'T':	//Demande des template de la catégorie
				final int categ = Integer.parseInt(packet.substring(3));
				final String allTemplate = World.getHdv(Math.abs(_player.getIsTradingWith())).parseTemplate(categ);
				SocketManager.GAME_SEND_EHL_PACKET(_player,categ,allTemplate);
			break;			
		}
	}

	private void Exchange_mountPark(String packet)
	{
		//Si dans un enclos
		if(_player.getInMountPark() != null)
		{
			final char c = packet.charAt(2);
			packet = packet.substring(3);
			int guid = -1;
			try
			{
				guid = Integer.parseInt(packet);
			}catch(final Exception e){};
			switch(c)
			{
				case 'C'://Parcho => Etable (Stocker)
					if(guid == -1 || !_player.hasItemGuid(guid))return;
					final Item obj = World.getObjet(guid);
					
					//on prend la DD demandée
					final int DDid = obj.getStats().getEffect(995);
					Mount DD = World.getDragoByID(DDid);
					//FIXME mettre return au if pour ne pas créer des nouvelles dindes
					if(DD == null)
					{
						final int color = Constants.getMountColorByParchoTemplate(obj.getTemplate().getID());
						if(color <1)return;
						DD = new Mount(color);
					}
					
					//On enleve l'objet du Monde et du Perso
					_player.removeItem(guid);
					World.removeItem(guid);
					//on ajoute la dinde a l'étable
					_account.getStable().add(DD);
					
					//On envoie les packet
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_player,obj.getGuid());
					SocketManager.GAME_SEND_Ee_PACKET(_player, '+', DD.parseInfos());
				break;
				case 'c'://Etable => Parcho(Echanger)
					final Mount DD1 = World.getDragoByID(guid);
					//S'il n'a pas la dinde
					if(!_account.getStable().contains(DD1) || DD1 == null)return;
					//on retire la dinde de l'étable
					_account.getStable().remove(DD1);
					
					//On créer le parcho
					final ItemTemplate T = Constants.getParchoTemplateByMountColor(DD1.getColor());
					final Item obj1 = T.createNewItem(1, false);
					//On efface les stats
					obj1.clearStats();
					//on ajoute la possibilité de voir la dinde
					obj1.getStats().addOneStat(995, DD1.getId());
					obj1.addTxtStat(996, _player.getName());
					obj1.addTxtStat(997, DD1.getName());
					
					//On ajoute l'objet au joueur
					World.addItem(obj1, true);
					_player.addItem(obj1, false);//Ne seras jamais identique de toute
					_player.itemLog(obj1.getTemplate().getID(), obj1.getQuantity(), "Mise en parchemin d'une monture");
					
					//Packets
					SocketManager.GAME_SEND_Ow_PACKET(_player);
					SocketManager.GAME_SEND_Ee_PACKET(_player,'-',DD1.getId()+"");
				break;
				case 'g'://Equiper
					final Mount DD3 = World.getDragoByID(guid);
					//S'il n'a pas la dinde
					if(!_account.getStable().contains(DD3) || DD3 == null || _player.getMount() != null)return;
					
					_account.getStable().remove(DD3);
					_player.setMount(DD3);
					
					//Packets
					SocketManager.GAME_SEND_Re_PACKET(_player, "+", DD3);
					SocketManager.GAME_SEND_Ee_PACKET(_player,'-',DD3.getId()+"");
					SocketManager.GAME_SEND_Rx_PACKET(_player);
				break;
				case 'p'://Equipé => Stocker
					//Si c'est la dinde équipé
					if(_player.getMount()!=null?_player.getMount().getId() == guid:false)
					{
						//Si le perso est sur la monture on le fait descendre
						if(_player.isOnMount())_player.toogleOnMount();
						//Si ca n'a pas réussie, on s'arrete là (Items dans le sac ?)
						if(_player.isOnMount())return;
						
						final Mount DD2 = _player.getMount();
						_account.getStable().add(DD2);
						_player.setMount(null);
						
						//Packets
						SocketManager.GAME_SEND_Ee_PACKET(_player,'+',DD2.parseInfos());
						SocketManager.GAME_SEND_Re_PACKET(_player, "-", null);
						SocketManager.GAME_SEND_Rx_PACKET(_player);
					}else//Sinon...
					{
						
					}
				break;
				
				default:
					Console.printlnError("Unknown packet recv : Er" + packet);
					SocketManager.GAME_SEND_BN(_out);
					break;
			}
		}
	}

	private void Exchange_doAgain()
	{
		if(_player.getCurJobAction() != null)
			_player.getCurJobAction().putLastCraftIngredients();
	}

	private void Exchange_isOK()
	{
		if(_player.getCurJobAction() != null)
		{
			//Si pas action de craft, on s'arrete la
			if(!_player.getCurJobAction().isCraft())return;
			_player.getCurJobAction().startCraft(_player);
		}
		if(_player.getCurExchange() == null)return;
		_player.getCurExchange().toogleOK(_player.getActorId());
	}

	private void Exchange_onMoveItem(final String packet)
	{
		//Mount
		if(_player.getIsTradingWith() == -15)
		{
			switch(packet.charAt(2))
			{
			case 'O'://Objets
				int guid = 0;
				int qua = 0;
				try
				{
					guid = Integer.parseInt(packet.substring(4).split("\\|")[0]);
					qua = Integer.parseInt(packet.substring(4).split("\\|")[1]);
				} catch (NumberFormatException e) {	guid = -1; }
				if(guid == -1)
					return;
				if(World.getObjet(guid) == null)
					return;
				if(packet.charAt(3) == '+')
				{
					_player.addInMount(guid, qua);
				}else
				{
					_player.removeFromMount(guid, qua);
				}
				break;
			}
			return;
		}
		//Store
		if(_player.getIsTradingWith() == _player.getActorId())
		{
			switch(packet.charAt(2))
			{
			case 'O'://Objets
				if(packet.charAt(3) == '+')
				{
					final String[] infos = packet.substring(4).split("\\|");
					try
					{
						
						final int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						final int price  = Integer.parseInt(infos[2]);
						
						final Item obj = World.getObjet(guid);
						if(obj == null)return;
						
						if(qua > obj.getQuantity())
							qua = obj.getQuantity();
						
						_player.addinStore(obj.getGuid(), price, qua);
						
					}catch(final NumberFormatException e){};
				}else
				{
					final String[] infos = packet.substring(4).split("\\|");
					try
					{
						final int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						
						if(qua <= 0)return;
						
						final Item obj = World.getObjet(guid);
						if(obj == null)return;
						if(qua > obj.getQuantity())return;
						if(qua < obj.getQuantity()) qua = obj.getQuantity();
						
						_player.removeFromStore(obj.getGuid(), qua);
					}catch(final NumberFormatException e){};
				}
			break;
			}
			return;
		}
		//Metier
		if(_player.getCurJobAction() != null)
		{
			//Si pas action de craft, on s'arrete la
			if(!_player.getCurJobAction().isCraft())return;
			if(packet.charAt(2) == 'O')//Ajout d'objet
			{
				if(packet.charAt(3) == '+')
				{
					//FIXME gerer les packets du genre  EMO+173|5+171|5+172|5 (split sur '+' ?:/)
					final String[] infos = packet.substring(4).split("\\|");
					try
					{
						final int guid = Integer.parseInt(infos[0]);
						
						int qua  = Integer.parseInt(infos[1]);
						if(qua <= 0)return;
						
						if(!_player.hasItemGuid(guid))return;
						final Item obj = World.getObjet(guid);
						if(obj == null)return;
						if(obj.getQuantity()<qua)
							qua = obj.getQuantity();
						_player.getCurJobAction().modifIngredient(_player,guid,qua);
					}catch(final NumberFormatException e){};
				}else
				{
					final String[] infos = packet.substring(4).split("\\|");
					try
					{
						final int guid = Integer.parseInt(infos[0]);
						
						final int qua  = Integer.parseInt(infos[1]);
						if(qua <= 0)return;
						
						final Item obj = World.getObjet(guid);
						if(obj == null)return;
						_player.getCurJobAction().modifIngredient(_player,guid,-qua);
					}catch(final NumberFormatException e){};
				}
				
			}else
			if(packet.charAt(2) == 'R')
			{
				try
				{
					final int c = Integer.parseInt(packet.substring(3));
					_player.getCurJobAction().repeat(c,_player);
				}catch(final Exception e){};
			}
			return;
		}
		//Banque
		if(_player.isInBank())
		{
			if(_player.getCurExchange() != null)return;
			switch(packet.charAt(2))
			{
				case 'G'://Kamas
					long kamas = 0;
					try
					{
						kamas = Integer.parseInt(packet.substring(3));
					}catch(final Exception e){};
					if(kamas == 0)return;
					
					if(kamas > 0)//Si On ajoute des kamas a la banque
					{
						if(_player.getKamas() < kamas)kamas = _player.getKamas();
						_player.setBankKamas(_player.getBankKamas()+kamas);//On ajoute les kamas a la banque
						_player.setKamas(_player.getKamas()-kamas);//On retire les kamas du personnage
						_player.kamasLog(-kamas+"", "Placement des kamas à la banque");
						
						SocketManager.GAME_SEND_STATS_PACKET(_player);
						SocketManager.GAME_SEND_EsK_PACKET(_player,"G"+_player.getBankKamas());
					}else
					{
						kamas = -kamas;//On repasse en positif
						if(_player.getBankKamas() < kamas)kamas = _player.getBankKamas();
						_player.setBankKamas(_player.getBankKamas()-kamas);//On retire les kamas de la banque
						_player.setKamas(_player.getKamas()+kamas);//On ajoute les kamas du personnage
						_player.kamasLog(kamas+"", "Retrait des kamas de la banque");
						
						SocketManager.GAME_SEND_STATS_PACKET(_player);
						SocketManager.GAME_SEND_EsK_PACKET(_player,"G"+_player.getBankKamas());
					}
				break;
				
				case 'O'://Objet
					int guid = 0;
					int qua = 0;
					try
					{
						guid = Integer.parseInt(packet.substring(4).split("\\|")[0]);
						qua = Integer.parseInt(packet.substring(4).split("\\|")[1]);
					}catch(final Exception e){};
					if(guid == 0 || qua <= 0)return;
					
					switch(packet.charAt(3))
					{
						case '+'://Ajouter a la banque
							_player.addInBank(guid,qua);
						break;
						
						case '-'://Retirer de la banque
							_player.removeFromBank(guid,qua);
						break;
					}
				break;
			}
			return;
		}
		if(_player.getIsTradingWith() < 0)	//HDV
		{
			switch(packet.charAt(3))
			{
				case '-'://Retirer un objet de l'HDV
					final int cheapestID = Integer.parseInt(packet.substring(4).split("\\|")[0]);
					final int count = Integer.parseInt(packet.substring(4).split("\\|")[1]);
					if(count <= 0)return;
					_player.getAccount().recoverItem(cheapestID,count);	//Retire l'objet de la liste de vente du compte
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(_out,'-',"",cheapestID+"");
				break;
				case '+'://Mettre un objet en vente
					final int itmID = Integer.parseInt(packet.substring(4).split("\\|")[0]);
					final byte amount = Byte.parseByte(packet.substring(4).split("\\|")[1]);
					final int price = Integer.parseInt(packet.substring(4).split("\\|")[2]);
					if(amount <= 0 || price <= 0)return;
					
					final BigStore curHdv = World.getHdv(Math.abs(_player.getIsTradingWith()));
					final int taxe = (int)(price * (curHdv.getTaxe()/100));
					
					
					if(!_player.hasItemGuid(itmID))	//Vérifie si le personnage a bien l'item spécifié et l'argent pour payer la taxe
						return;
					if(_player.getKamas() < taxe)
					{
						SocketManager.GAME_SEND_Im_PACKET(_player, "176");
						return;
					}
					
					_player.addKamas(taxe *-1); //Retire le montant de la taxe au personnage
					_player.kamasLog(-taxe+"", "Taxe lors de la mise en vente d'un objet");
					
					SocketManager.GAME_SEND_STATS_PACKET(_player);	//Met a jour les kamas du client
					
					Item obj = World.getObjet(itmID); //Récupère l'item
					if(amount > obj.getQuantity())	//S'il veut mettre plus de cette objet en vente que ce qu'il possède
						return;
					
					if(obj.getQuantity() > 1)	//Si c'est plusieurs objets ensemble enleve seulement la quantité de mise en vente
					{
						final int rAmount = (int)(Math.pow(10,amount)/10);
						obj.setQuantity(obj.getQuantity() - rAmount);
						//_perso.objetLog(obj.getTemplate().getID(), -rAmount, "Mise en HDV de l'objet");
						SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_player,obj);
						
						final Item newObj = obj.getTemplate().createNewItem(rAmount, false);
						newObj.clearStats();
						newObj.parseStringToStats(obj.parseToSave());
						World.addItem(newObj, true);
						
						obj = newObj;
					}
					else
					{
						_player.removeItem(itmID);			//Enlève l'item de l'inventaire du personnage
						//_perso.objetLog(World.getObjet(itmID).getTemplate().getID(), 1, "Mise en HDV de l'objet");
						
						//World.removeItem(itmID);	//Supprime l'item du Map du monde
						SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_player,itmID);	//Envoie un packet au client pour retirer l'item de son inventaire
					}

					//HdvEntry toAdd = new HdvEntry(obj.parseStatsString(),price,amount,obj.getTemplate().getID(),_perso.get_compte().get_GUID());	//Crée un HdvEntry
					final BigStoreEntry toAdd = new BigStoreEntry(price,amount,_player.getAccount().getGUID(),obj);
					curHdv.addEntry(toAdd);	//Ajoute l'entry dans l'HDV
					
					
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(_out,'+',"",toAdd.parseToEmK());	//Envoie un packet pour ajouter l'item dans la fenetre de l'HDV du client
				break;
			}
		}
		if(_player.getCurExchange() == null)return;
		switch(packet.charAt(2))
		{
			case 'O'://Objet ?
				if(packet.charAt(3) == '+')
				{
					final String[] infos = packet.substring(4).split("\\|");
					try
					{
						final int guid = Integer.parseInt(infos[0]);
						int qua  = Integer.parseInt(infos[1]);
						final int quaInExch = _player.getCurExchange().getQuaItem(guid, _player.getActorId());
						
						if(!_player.hasItemGuid(guid))return;
						final Item obj = World.getObjet(guid);
						if(obj == null)return;
						
						if(qua > obj.getQuantity()-quaInExch)
							qua = obj.getQuantity()-quaInExch;
						if(qua <= 0)return;
						
						_player.getCurExchange().addItem(guid,qua,_player.getActorId());
					}catch(final NumberFormatException e){};
				}else
				{
					final String[] infos = packet.substring(4).split("\\|");
					try
					{
						final int guid = Integer.parseInt(infos[0]);
						final int qua  = Integer.parseInt(infos[1]);
						
						if(qua <= 0)return;
						if(!_player.hasItemGuid(guid))return;
						
						final Item obj = World.getObjet(guid);
						if(obj == null)return;
						if(qua > _player.getCurExchange().getQuaItem(guid, _player.getActorId()))return;
						
						_player.getCurExchange().removeItem(guid,qua,_player.getActorId());
					}catch(final NumberFormatException e){};
				}
			break;
			
			case 'G'://Kamas
				try
				{
					long numb = Integer.parseInt(packet.substring(3));
					if(_player.getKamas() < numb)
						numb = _player.getKamas();
					_player.getCurExchange().setKamas(_player.getActorId(), numb);
				}catch(final NumberFormatException e){};
			break;
		}
	}

	private void Exchange_accept()
	{
		if(_player.getIsTradingWith() == 0)return;
		final Player target = World.getPlayer(_player.getIsTradingWith());
		if(target == null)return;
		SocketManager.GAME_SEND_EXCHANGE_CONFIRM_OK(_out,1);
		SocketManager.GAME_SEND_EXCHANGE_CONFIRM_OK(target.getAccount().getGameThread().getOut(),1);
		final World.Exchange echg = new World.Exchange(target,_player);
		_player.setCurExchange(echg);
		_player.setIsTradingWith(target.getActorId());
		target.setCurExchange(echg);
		target.setIsTradingWith(_player.getActorId());
	}

	private void Exchange_onSellItem(final String packet)
	{
		try
		{
			final String[] infos = packet.substring(2).split("\\|");
			final int guid = Integer.parseInt(infos[0]);
			final int qua = Integer.parseInt(infos[1]);
			if(!_player.hasItemGuid(guid))
			{
				SocketManager.GAME_SEND_SELL_ERROR_PACKET(_out);
				return;
			}
			_player.sellItem(guid, qua);
		}catch(final Exception e)
		{
			SocketManager.GAME_SEND_SELL_ERROR_PACKET(_out);
		}
	}

	private void Exchange_onBuyItem(final String packet)
	{
		final String[] infos = packet.substring(2).split("\\|");
		try
		{
			final int tempID = Integer.parseInt(infos[0]);
			final int qua = Integer.parseInt(infos[1]);
			
			if(qua <= 0)
				return;
			
			final ItemTemplate template = World.getItemTemplate(tempID);
			if(template == null)//Si l'objet demandé n'existe pas(ne devrait pas arrivé)
			{
				Log.addToErrorLog(_player.getName()+" tente d'acheter l'itemTemplate "+tempID+" qui est inexistant");
				SocketManager.GAME_SEND_BUY_ERROR_PACKET(_out);
				return;
			}
			if(!_player.getCurMap().getNPC(_player.getIsTradingWith()).getTemplate().haveItem(tempID))//Si le PNJ ne vend pas l'objet voulue
			{
				Log.addToErrorLog(_player.getName()+" tente d'acheter l'itemTemplate "+tempID+" que le présent PNJ ne vend pas");
				SocketManager.GAME_SEND_BUY_ERROR_PACKET(_out);
				return;
			}
			
			final int prix = template.getPrix() * qua;
			if(_player.getKamas()<prix)//Si le joueur n'a pas assez de kamas
			{
				Log.addToErrorLog(_player.getName()+" tente d'acheter l'itemTemplate "+tempID+" mais n'a pas l'argent nécessaire");
				SocketManager.GAME_SEND_BUY_ERROR_PACKET(_out);
				return;
			}
			final Item newObj = template.createNewItem(qua,false);
			final long newKamas = _player.getKamas() - prix;
			_player.setKamas(newKamas);
			_player.kamasLog(-prix+"", "Achat de l'item '" + World.getItemTemplate(tempID).getName() + "' au près d'un PNJ");
			
			if(_player.addItem(newObj,true))//Return TRUE si c'est un nouvel item
				World.addItem(newObj,true);
			_player.itemLog(newObj.getTemplate().getID(), newObj.getQuantity(), "Acheté au près d'un PNJ sur la Map '" + _player.getCurMap().getId() + "'");
			
			SocketManager.GAME_SEND_BUY_OK_PACKET(_out);
			SocketManager.GAME_SEND_STATS_PACKET(_player);
			SocketManager.GAME_SEND_Ow_PACKET(_player);
		}catch(final Exception e)
		{
			e.printStackTrace();
			SocketManager.GAME_SEND_BUY_ERROR_PACKET(_out);
			return;
		};
	}

	private void Exchange_finish_buy(final String packet)
	{
		if(_player.getCurExchange() == null &&
				!_player.isInBank() &&
				_player.getCurJobAction() == null &&
				_player.getIsTradingWith() == 0 &&
				_player.getInMountPark() == null)return;
		//Si échange avec un personnage
		if(	_player.getCurExchange() != null)
		{
			_player.getCurExchange().cancel();
			_player.setIsTradingWith(0);
			_player.setAway(false);
			return;
		}
		//Si métier
		if(_player.getCurJobAction() != null)
		{
			_player.getCurJobAction().resetCraft();
		}
		//Si dans un enclos
		if(_player.getInMountPark() != null)_player.leftMountPark();
		//prop d'echange avec un joueur
		if(_player.getIsTradingWith() > 0)
		{
			final Player p = World.getPlayer(_player.getIsTradingWith());
			if(p != null)
			{
				if(p.isOnline())
				{
					final PrintWriter out = p.getAccount().getGameThread().getOut();
					SocketManager.GAME_SEND_EV_PACKET(out);
					p.setIsTradingWith(0);
				}
			}
		}
		//Si échange avec un percepteur
		if(World.getTaxCollector(_player.getIsTradingWith()) != null)
		{
			final TaxCollector perco = World.getTaxCollector(_player.getIsTradingWith());
			if(perco == null) return;
			final StringBuilder str = new StringBuilder();
			str.append('G').append(perco.getName1()).append(',').append(perco.getName2());
			str.append("|.|").append(World.getMap(perco.getMapId()).getX()).append('|').append(World.getMap(perco.getMapId()).getY()).append('|');
			str.append(_player.getName()).append('|');
			str.append(perco.getXp());
			if(!perco.getItems().isEmpty())
				str.append(';').append(perco.getItemsStr());
			final Guild guild = perco.getGuild();
			for(final Player z : guild.getMembers())
			{
				if(z != null && z.isOnline())
				{
					SocketManager.GAME_SEND_GUILD_PERCEPTEUR_MOVEMENT_PACKET(z, guild.parseInfosPerco());
					SocketManager.GAME_SEND_GUILD_PERCEPTEUR_INFOS_PACKET(z, str.toString());
				}
			}
			perco.setInExchange(false);
			_player.getCurMap().removeNpcOrMobGroup(perco.getActorId());
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_player.getCurMap(), perco.getActorId());
			SQLManager.DELETE_PERCEPTEUR(perco.getActorId());
			_player.setIsTradingWith(0);
			_player.setAway(false);
			SocketManager.GAME_SEND_EV_PACKET(_out);
			return;
		}
		
		SocketManager.GAME_SEND_EV_PACKET(_out);
		_player.setIsTradingWith(0);
		_player.setAway(false);
		_player.setInBank(false);
	}

	private void Exchange_start(final String packet)
	{
		if(packet.substring(2,4).equals("11"))	//Ouverture HDV achat
		{
			final BigStore toOpen = World.getHdv(_player.getCurMap().getId());
			
			if(toOpen == null) return;
			
			final StringBuilder info = new StringBuilder();
			info.append("1,10,100;")
				.append(toOpen.getStrCategories()).append(';')
				.append(toOpen.parseTaxe()).append(';')
				.append(toOpen.getLvlMax()).append(';')
				.append(toOpen.getMaxItemCompte()).append(';')
				.append("-1;").append(toOpen.getSellTime());
			SocketManager.GAME_SEND_ECK_PACKET(_player,11,info.toString());
			_player.setIsTradingWith(0 - _player.getCurMap().getId());	//Récupère l'ID de la map et rend cette valeur négative
			return;
		}
		else if(packet.substring(2,4).equals("10"))	//Ouverture HDV vente
		{
			final BigStore toOpen = World.getHdv(_player.getCurMap().getId());
			
			if(toOpen == null) return;
			
			final StringBuilder info = new StringBuilder();
			info.append("1,10,100;")
				.append(toOpen.getStrCategories()).append(';')
				.append(toOpen.parseTaxe()).append(';')
				.append(toOpen.getLvlMax()).append(';')
				.append(toOpen.getMaxItemCompte()).append(';')
				.append("-1;").append(toOpen.getSellTime());
			SocketManager.GAME_SEND_ECK_PACKET(_player,10,info.toString());
			_player.setIsTradingWith(0 - _player.getCurMap().getId());	//Récupère l'ID de la map et rend cette valeur négative
			
			SocketManager.GAME_SEND_HDVITEM_SELLING(_player);
			
			return;
		}
		else if(packet.substring(2,4).equals("15"))
		{
			if(_player.getMount() == null)return;
			_player.setIsTradingWith(-15);
			_player.setAway(true);
			SocketManager.GAME_SEND_ECK_PACKET(_out, 15, "");
			SocketManager.GAME_SEND_ITEM_LIST_MOUNT_PACKET(_out, _player.getMount());
			SocketManager.GAME_SEND_MOUNT_PODS(_out, _player.getMount());
		}
		switch(packet.charAt(2))
		{
			case '0'://Si NPC
				try
				{
					final int npcID = Integer.parseInt(packet.substring(4));
					final Npc npc = _player.getCurMap().getNPC(npcID);
					if(npc == null)return;
					SocketManager.GAME_SEND_ECK_PACKET(_out, 0, npcID+"");
					SocketManager.GAME_SEND_ITEM_VENDOR_LIST_PACKET(_out,npc);
					_player.setIsTradingWith(npcID);
				}catch(final NumberFormatException e){};
			break;
			case '1'://Si joueur
				try
				{
					final int guidTarget = Integer.parseInt(packet.substring(4));
					final Player target = World.getPlayer(guidTarget);
					if(target == null )
					{
						SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(_out,'E');
						return;
					}
					if(target.getCurMap()!= _player.getCurMap() || !target.isOnline())//Si les persos ne sont pas sur la meme map
					{
						SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(_out,'E');
						return;
					}
					if(target.isAway() || _player.isAway() || target.getIsTradingWith() != 0)
					{
						SocketManager.GAME_SEND_EXCHANGE_REQUEST_ERROR(_out,'O');
						return;
					}
					SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(_out, _player.getActorId(), guidTarget,1);
					SocketManager.GAME_SEND_EXCHANGE_REQUEST_OK(target.getAccount().getGameThread().getOut(),_player.getActorId(), guidTarget,1);
					_player.setIsTradingWith(guidTarget);
					target.setIsTradingWith(_player.getActorId());
				}catch(final NumberFormatException e){}
			break;
			case '4'://StorePlayer
            	int pID = 0;
            	try
				{
            		pID = Integer.valueOf(packet.split("\\|")[1]);
				}catch(final NumberFormatException e){return;};
				if(_player.getIsTradingWith() > 0 || _player.getFight() != null || _player.isAway())return;
				final Player seller = World.getPlayer(pID);
				if(seller == null) return;
				_player.setIsTradingWith(pID);
				SocketManager.GAME_SEND_ECK_PACKET(_player, 4, seller.getActorId()+"");
				SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(seller, _player);
            break;
			case '6'://StoreItems
				if(_player.getIsTradingWith() > 0 || _player.getFight() != null || _player.isAway())return;
                _player.setIsTradingWith(_player.getActorId());
                SocketManager.GAME_SEND_ECK_PACKET(_player, 6, "");
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(_player, _player);
			break;
			case '8'://Si Percepteur
				final int PercepteurID = Integer.parseInt(packet.substring(4));
				final TaxCollector perco = World.getTaxCollector(PercepteurID);
				if(perco == null || perco.getFight() != null || perco.isInExchange())return;
				perco.setInExchange(true);
				SocketManager.GAME_SEND_ECK_PACKET(_out, 8, perco.getActorId()+"");
				SocketManager.GAME_SEND_ITEM_LIST_TAXCOLLECTOR_PACKET(_out, perco);
				_player.setIsTradingWith(perco.getActorId());
				//_perso.set_isOnPercepteurID(perco.getID());
				SocketManager.GAME_SEND_Im_PACKET(_player, "1139;5");
			break;
		}
	}

	private void parseEnvironementPacket(final String packet)
	{
		switch(packet.charAt(1))
		{
			case 'D'://Change direction
				Environement_change_direction(packet);
			break;
			
			case 'U'://Emote
				Environement_emote(packet);
			break;
		}
	}

	private void Environement_emote(final String packet)
	{
		int emote = -1;
		try
		{
			emote = Integer.parseInt(packet.substring(2));
		}catch(final Exception e){};
		if(emote == -1)return;
		if(_player == null)return;
		if(_player.getFight() != null)return;//Pas d'émote en combat
		
		switch(emote)//effets spéciaux des émotes
		{
			case 1:// s'asseoir
			case 19://s'allonger
			case 20://s'asseoir sur le tabouret
				_player.setSitted(!_player.isSitted());
			break;
			
			case 21:
				if (_player.getAccount().isVip() | _player.getAccount().getGmLvl() >= 1)
				{
					DofusMap targetMap = _player.getCurMap();
					ArrayList<Player> mapPlayer = targetMap.getPersos();
					for(Player PERS : mapPlayer)
					{
						if(!PERS.isOnline())
							continue; // On selectionne parmis les personnages en ligne 
						if(PERS.getFight() != null)
							continue; // On selectionne parmis les personnages hors combat 
						if(_player == PERS) 
							continue; // Sauf le lanceur 
						if(PERS.getAccount().getGmLvl() >= 1) 
							continue; // Sauf les Membres staff 
						PERS.setEmoteActive(3);
						SocketManager.GAME_SEND_eUK_PACKET_TO_MAP(targetMap, PERS.getActorId(), PERS.emoteActive());
					}
				}
				else 
				{
					SocketManager.GAME_SEND_MESSAGE(_player, "Cette émote est réservée aux champions.", "C10000"); 
					return;
				}
			break;
		}
		if(_player.emoteActive() == emote)
			_player.setEmoteActive(0);
		else 
			_player.setEmoteActive(emote);
		
		System.out.println("Set Emote "+_player.emoteActive());
		System.out.println("Is sitted "+_player.isSitted());
		
		SocketManager.GAME_SEND_eUK_PACKET_TO_MAP(_player.getCurMap(), _player.getActorId(), _player.emoteActive());
	}

	private void Environement_change_direction(final String packet)
	{
		try
		{
			if(_player.getFight() != null)return;
			final int dir = Integer.parseInt(packet.substring(2));
			_player.setOrientation(dir);
			SocketManager.GAME_SEND_eD_PACKET_TO_MAP(_player.getCurMap(),_player.getActorId(),dir);
		}catch(final NumberFormatException e){return;};
	}

	private void parseSpellPacket(final String packet)
	{
		_player.clearCacheAS();
		switch(packet.charAt(1))
		{
			case 'B':
				boostSort(packet);
			break;
			case 'F'://Oublie de sort
				forgetSpell(packet);
			break;
			case'M':
				addToSpellBook(packet);
			break;
		}
	}

	private void addToSpellBook(final String packet)
	{
		try
		{
			final int SpellID = Integer.parseInt(packet.substring(2).split("\\|")[0]);
			final int Position = Integer.parseInt(packet.substring(2).split("\\|")[1]);
			final SpellStat Spell = _player.getSortStatBySortIfHas(SpellID);
			
			if(Spell != null)
			{
				_player.setSpellPlace(SpellID, CryptManager.getHashedValueByInt(Position));
			}
				
			SocketManager.GAME_SEND_BN(_out);
		}catch(final Exception e){};
	}

	private void boostSort(final String packet)
	{
		try
		{
			final int id = Integer.parseInt(packet.substring(2));
			Log.addToLog("Info: "+_player.getName()+": Tente BOOST sort id="+id);
			if(_player.boostSpell(id))
			{
				Log.addToLog("Info: "+_player.getName()+": OK pour BOOST sort id="+id);
				SocketManager.GAME_SEND_SPELL_UPGRADE_SUCCED(_out, id, _player.getSortStatBySortIfHas(id).getLevel());
				SocketManager.GAME_SEND_STATS_PACKET(_player);
			}else
			{
				Log.addToErrorLog("Info: "+_player.getName()+": Echec BOOST sort id="+id);
				SocketManager.GAME_SEND_SPELL_UPGRADE_FAILED(_out);
				return;
			}
		}catch(final NumberFormatException e){SocketManager.GAME_SEND_SPELL_UPGRADE_FAILED(_out);return;};
	}
	private void forgetSpell(final String packet)
	{
		if(!_player.isForgetingSpell())return;
		
		final int id = Integer.parseInt(packet.substring(2));
		Log.addToLog("Info: "+_player.getName()+": Tente Oublie sort id="+id);
		
		if(_player.forgetSpell(id))
		{
			Log.addToErrorLog("Info: "+_player.getName()+": OK pour Oublie sort id="+id);
			SocketManager.GAME_SEND_SPELL_UPGRADE_SUCCED(_out, id, _player.getSortStatBySortIfHas(id).getLevel());
			SocketManager.GAME_SEND_STATS_PACKET(_player);
			_player.setIsForgetingSpell(false);
		}
	}

	private void parseFightPacket(final String packet)
	{
		try
		{
			switch(packet.charAt(1))
			{
				case 'D'://Détails d'un combat (liste des combats)
					int key = -1;
					try
					{
						key = Integer.parseInt(packet.substring(2).replace(((int)0x0)+"", ""));
					}catch(final Exception e){};
					if(key == -1)return;
					SocketManager.GAME_SEND_FIGHT_DETAILS(_out,_player.getCurMap().getFights().get(key));
				break;
				
				case 'H'://Aide
					if(_player.getFight() == null)return;
					_player.getFight().toggleHelp(_player.getActorId());
				break;
				
				case 'L'://Lister les combats
					SocketManager.GAME_SEND_FIGHT_LIST_PACKET(_out, _player.getCurMap());
				break;
				case 'N'://Bloquer le combat
					if(_player.getFight() == null)return;
					_player.getFight().toggleLockTeam(_player.getActorId());
				break;
				case 'P'://Seulement le groupe
					if(_player.getFight() == null || _player.getParty() == null)return;
					_player.getFight().toggleOnlyGroup(_player.getActorId());
				break;
				case 'S'://Bloquer les specs
					if(_player.getFight() == null)return;
					_player.getFight().toggleLockSpec(_player.getActorId());
				break;
				
			}
		}catch(final Exception e){e.printStackTrace();};
	}

	private void parseBasicsPacket(final String packet)
	{
		switch(packet.charAt(1))
		{
			case 'a'://movement
				Basic_movement(packet);
			break;
			case 'A'://Console
				Basic_console(packet);
			break;
			case 'D':
				Basic_send_Date_Hour();
			break;
			case 'M':
				Basic_chatMessage(packet);
			break;
			case 'S':
				_player.useEmote(packet.substring(2));
			break;
			case 'Y':
				Basic_state(packet);
			break;
		}
	}
	
	public void Basic_movement(String packet)
	{
		switch(packet.charAt(2))
		{
		case 'M':
			if(_account.getGmLvl() < 1) return;
			if(packet.substring(3).equalsIgnoreCase("NaN"))	return;
			String[] infos = packet.substring(3).split(",");
			int mapX = Integer.parseInt(infos[0]);
			int mapY = Integer.parseInt(infos[1]);
			DofusMap map = World.getCarteByPosAndCont(mapX, mapY, 0);
			if(map == null) return;
			_player.teleport(map.getId(), map.getRandomFreeCellID());
		break;
		}
	}
	
	public void Basic_state(final String packet)
	{
		switch(packet.charAt(2))
		{
			case 'A': //Absent
				if(_player.isAway)
				{
					SocketManager.GAME_SEND_Im_PACKET(_player, "038");
					_player.isAway = false;
				}
				else
				{
					SocketManager.GAME_SEND_Im_PACKET(_player, "037");
					_player.isAway = true;
				}
			break;
			
			case 'I': //Invisible
				if(_player.isInvisible)
				{
					SocketManager.GAME_SEND_Im_PACKET(_player, "051");
					_player.isInvisible = false;
				}
				else
				{
					SocketManager.GAME_SEND_Im_PACKET(_player, "050");
					_player.isInvisible = true;
				}
			break;
		}
	}

	public Player getPerso()
	{
		return _player;
	}
	
	private void Basic_console(final String packet)
	{
		if(command == null)
			command = new Command(_player);
		
		command.consoleCommand(packet);
	}

	public void closeSocket()
	{
		try {
			this._socket.close();
		} catch (final IOException e) {}
	}

	private void Basic_chatMessage(String packet)
	{
		String msg = "";
		if(_player.isMuted()) {
			SocketManager.GAME_SEND_BN(_player);
			return;
		}
		packet = packet.replace("<", "");
		packet = packet.replace(">", "");
		if(packet.length() == 3)return;
		
		msg = packet.split("\\|",2)[1];
		
		boolean correctMessage = _player.analyzeMessage(msg);
		
		if(!correctMessage) {
			SocketManager.GAME_SEND_BN(_player);
			return;
		}
		
		switch(packet.charAt(2))
		{
			case '*'://Canal noir
				if(!_player.getChannels().contains(packet.charAt(2)+""))return;
				
				//Commandes joueurs
				if(msg.charAt(0) == '.' && msg.charAt(1) != '.')
				{
					if(command == null)
						command = new Command(_player);
					command.dotCommand(msg.substring(1));
					return;
				}
							
				if(_player.getFight() == null)
					SocketManager.GAME_SEND_cMK_PACKET_TO_MAP(_player.getCurMap(), "", _player.getActorId(), _player.getName(), msg);
				else
					SocketManager.GAME_SEND_cMK_PACKET_TO_FIGHT(_player.getFight(), 7, "", _player.getActorId(), _player.getName(), msg);
				
				//controller.gameMessage('*', _perso, "(Map)", msg);
			
			break;
			case '#'://Canal Equipe
				if(!_player.getChannels().contains(packet.charAt(2)+""))return;
				if(_player.getFight() != null)
				{
					final int team = _player.getFight().getTeamID(_player.getActorId());
					if(team == -1)return;
					SocketManager.GAME_SEND_cMK_PACKET_TO_FIGHT(_player.getFight(), team, "#", _player.getActorId(), _player.getName(), msg);
					
					//controller.gameMessage('#', _perso, to, message)
				}
			break;
			case '$'://Canal groupe
				if(!_player.getChannels().contains(packet.charAt(2)+""))return;
				if(_player.getParty() == null)break;
				SocketManager.GAME_SEND_cMK_PACKET_TO_PARTY(_player.getParty(), "$", _player.getActorId(), _player.getName(), msg);
			break;
			
			case ':'://Canal commerce
				if(!_player.getChannels().contains(packet.charAt(2)+""))return;
				long l;
				if((l = System.currentTimeMillis() - _timeLastTradeMsg) < Config.FLOOD_TIME)
				{
					l = (Config.FLOOD_TIME  - l)/1000;//On calcul la différence en secondes
					
					SocketManager.GAME_SEND_Im_PACKET(_player, "0115;"+l);
					return;
				}
				_timeLastTradeMsg = System.currentTimeMillis();
				SocketManager.GAME_SEND_cMK_PACKET_TO_ALL(":", _player.getActorId(), _player.getName(), msg);
			break;
			case '@'://Canal Admin
				if(_player.getAccount().getGmLvl() ==0)return;
				SocketManager.GAME_SEND_cMK_PACKET_TO_ADMIN("@", _player.getActorId(), _player.getName(), msg);
			break;
			case '?'://Canal recrutement
				if(!_player.getChannels().contains(packet.charAt(2)+""))return;
				
				long L;
				if((L = System.currentTimeMillis() - _timeLastRecrutmentMsg) < Config.FLOOD_TIME)
				{
					L = (Config.FLOOD_TIME  - L)/1000;//On calcul la différence en secondes
					
					SocketManager.GAME_SEND_Im_PACKET(_player, "0115;"+L);
					return;
				}
				_timeLastRecrutmentMsg = System.currentTimeMillis();
				
				SocketManager.GAME_SEND_cMK_PACKET_TO_ALL("?", _player.getActorId(), _player.getName(), msg);
			break;
			case '%'://Canal guilde
				if(!_player.getChannels().contains(packet.charAt(2)+""))return;
				if(_player.getGuild() == null)return;
				SocketManager.GAME_SEND_cMK_PACKET_TO_GUILD(_player.getGuild(),"%", _player.getActorId(), _player.getName(), msg);
			break;
			case '!'://Canal Alignement
				if(!_player.getChannels().contains(packet.charAt(2)+""))return;
				if(_player.getAlign() == Constants.ALIGNEMENT_NEUTRE)return;
				
				SocketManager.GAME_SEND_cMK_PACKET_TO_ALIGN(_player.getAlign(), _player.getActorId(), _player.getName(), msg);
			break;
			case 0xC2://Canal 
			break;
			default:
				final String nom = packet.substring(2).split("\\|")[0];
				if(nom.length() <= 1)
					Log.addToErrorLog("ChatHandler: Chanel non géré : "+nom);
				else
				{
					final Player target = World.getPersoByName(nom);
					if(target == null)//si le personnage n'existe pas
					{
						SocketManager.GAME_SEND_CHAT_ERROR_PACKET(_out, nom);
						return;
					}
					if(target.getAccount() == null)
					{
						SocketManager.GAME_SEND_CHAT_ERROR_PACKET(_out, nom);
						return;
					}
					if(target.getAccount().getGameThread() == null)//si le perso n'est pas co
					{
						SocketManager.GAME_SEND_CHAT_ERROR_PACKET(_out, nom);
						return;
					}
					if(!target.isDispo(_player) && !(_player.getAccount().getGmLvl() > 0))//Si le personnage est "/away" ou "/invisible" et que l'envoyeur n'est pas un ami
					{
						SocketManager.GAME_SEND_Im_PACKET(_player, "114;"+target.getName());
						return;
					}
					SocketManager.GAME_SEND_cMK_PACKET(target, "F", _player.getActorId(), _player.getName(), msg);
					SocketManager.GAME_SEND_cMK_PACKET(_player, "T", target.getActorId(), target.getName(), msg);
				}
			break;
		}
	}

	private void Basic_send_Date_Hour()
	{
		SocketManager.GAME_SEND_SERVER_DATE(_out);
		SocketManager.GAME_SEND_SERVER_HOUR(_out);
	}

	private void parseGamePacket(final String packet)
	{
		switch(packet.charAt(1))
		{
			case 'A':
				if(_player == null)return;
					parseGameActionPacket(packet);
			break;
			case 'C':
				if(_player == null)return;
				_player.sendGameCreate();
			break;
			case 'F':
				_player.freeMySoul();
			break;
			case 'f':
				Game_on_showCase(packet);
			break;
			case 'I':
				Game_on_GI_packet();
			break;
			case 'K':
				Game_on_GK_packet(packet);
			break;
			case 'P'://PvP Toogle
				_player.toogleWings(packet.charAt(2));
			break;
			case 'p':
				Game_on_ChangePlace_packet(packet);
			break;
			case 'Q':
				Game_onLeftFight(packet);
			break;
			case 'R':
				Game_on_Ready(packet);
			break;
			case 't':
				if(_player.getFight() == null)return;
				_player.getFight().playerPass(_player);
			break;
			default:
				Console.printlnError("Unknown packet recv : "+packet);
				SocketManager.GAME_SEND_BN(_out);
			break;
		}
	}

	
	private void Game_onLeftFight(String packet)
	{
		int guid = -1;
		if(!packet.substring(2).isEmpty()) {			
			guid = Integer.parseInt(packet.substring(2));
		}
		Player player = World.getPlayer(guid);
		Fight fight = _player.getFight();
		if(fight == null)return;
		if(player != null && player.getFight() != null) {
			if(fight.getTeamID(_player.getActorId()) == fight.getTeamID(player.getActorId()))
				return;
			fight.leftFight(_player, player);
		} else {
			fight.leftFight(_player, null);
		}
	}

	private void Game_on_showCase(final String packet)
	{
		if(_player == null)return;
		if(_player.getFight() == null)return;
		if(_player.getFight().getState() != Constants.FIGHT_STATE_ACTIVE)return;
		int cellID = -1;
		try
		{
			cellID = Integer.parseInt(packet.substring(2));
		}catch(final Exception e){};
		if(cellID == -1)return;
		_player.getFight().showCaseToTeam(_player.getActorId(),cellID);
	}

	private void Game_on_Ready(final String packet)
	{
		if(_player.getFight() == null)return;
		if(_player.getFight().getState() != Constants.FIGHT_STATE_PLACE)return;
		_player.setReady(packet.substring(2).equalsIgnoreCase("1"));
		_player.getFight().verifIfAllReady();
		SocketManager.GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(_player.getFight(),3,_player.getActorId(),packet.substring(2).equalsIgnoreCase("1"));
	
	}

	private void Game_on_ChangePlace_packet(final String packet)
	{
		if(_player.getFight() == null)return;
		try
		{
			final int cell = Integer.parseInt(packet.substring(2));
			_player.getFight().changePlace( _player, cell);
		}catch(final NumberFormatException e){return;};
	}

	private void Game_on_GK_packet(final String packet)
	{	
		int GameActionId = -1;
		final String[] infos = packet.substring(3).split("\\|");
		try
		{
			GameActionId = Integer.parseInt(infos[0]);
		}catch(final Exception e){return;};
		if(GameActionId == -1)return;
		final GameAction GA = _actions.get(GameActionId);
		if(GA == null)return;
		final boolean isOk = packet.charAt(2) == 'K';
		
		switch(GA._actionID)
		{
			case 1://Deplacement
				if(isOk)
				{
					//Hors Combat
					if(_player.getFight() == null)
					{
						_player.getCurCell().removePlayer(_player.getActorId());
						SocketManager.GAME_SEND_BN(_out);
						final String path = GA._args;
						//On prend la case ciblée
						final DofusCell nextCell = _player.getCurMap().getCell(CryptManager.cellCodeToID(path.substring(path.length()-2)));
						final DofusCell targetCell = _player.getCurMap().getCell(CryptManager.cellCodeToID(GA._packet.substring(GA._packet.length()-2)));
						
						//On définie la case et on ajoute le personnage sur la case
						_player.setCurCell(nextCell);
						_player.setOrientation(CryptManager.getIntByHashedValue(path.charAt(path.length()-3)));
						_player.getCurCell().addPerso(_player);
						_player.setAway(false);
						if(targetCell.getObject() != null)
						{
							//Si c'est une "borne" comme Emotes, ou Création guilde
							if(targetCell.getObject().getID() == 1324)
							{
								Constants.applyPlotIOAction(_player,_player.getCurMap().getId(),targetCell.getId());
							}else if(targetCell.getObject().getID() == 542)
							{
								if(_player.getGfxID() == 8004) {
									_player.backToLife();
								}
							}
						}
						_player.getCurMap().onPlayerArriveOnCell(_player,_player.getCurCell().getId());
					}
					else//En combat
					{
						_player.getFight().onGK(_player);
						return;
					}
					
				}
				else
				{
					//Si le joueur s'arrete sur une case
					int newCellID = -1;
					try
					{
						newCellID = Integer.parseInt(infos[1]);
					}catch(final Exception e){return;};
					if(newCellID == -1)return;
					final String path = GA._args;
					_player.getCurCell().removePlayer(_player.getActorId());
					_player.setCurCell(_player.getCurMap().getCell(newCellID));
					_player.setOrientation(CryptManager.getIntByHashedValue(path.charAt(path.length()-3)));
					_player.getCurCell().addPerso(_player);
					SocketManager.GAME_SEND_BN(_out);
				}
			break;
			
			case 500://Action Sur Map
				_player.finishActionOnCell(GA);
			break;

		}
		removeAction(GA);
	}

	private void Game_on_GI_packet() 
	{
		SocketManager.start_buffering(_out);
		if(_player.getFight() != null)
		{
			SocketManager.GAME_SEND_GDK_PACKET(_out);//Carte chargé
			if(_player.isDecoFromFight())
			{
				_player.getFight().reconnectFighter(_player);
				_player.setIsDecoFromFight(false);
			}
			SocketManager.stop_buffering(_out);
			return;
		}
		DofusMap map = _player.getCurMap();
		SocketManager.GAME_SEND_Rp_PACKET(_player, map.getMountPark());//Enclos
		SocketManager.GAME_SEND_MAP_PLAYERS_GMS_PACKETS(map, _player);
		SocketManager.GAME_SEND_MAP_MOBS_GMS_PACKETS(_out, map);//Monstres
		SocketManager.GAME_SEND_MAP_NPCS_GMS_PACKETS(_out, map);//Pnj
		SocketManager.GAME_SEND_MAP_OBJECTS_GDS_PACKETS(_out, map);//Objets interactifs
		SocketManager.GAME_SEND_MAP_PERCEPTEUR_GM_PACKET(_out, map);//Percepteur
		SocketManager.GAME_SEND_MAP_PRISM_GM_PACKET(map);//Prisme
		SocketManager.GAME_SEND_GDK_PACKET(_out);//Carte chargé
		SocketManager.GAME_SEND_MAP_OBJECT_DROPPED_PACKETS(_player, map);//Objets posé au sol
		SocketManager.GAME_SEND_MAP_FIGHT_FLAG(_out, map);//Epées de combats
		SocketManager.GAME_SEND_MAP_FIGHT_COUNT(_out, map);//Nombre de combats
		SocketManager.send(_out, "EW+"+_player.getActorId()+"|");//Exchange.onCraftPublicMode() ...
		SocketManager.stop_buffering(_out);
		map = null;//flush the data
		//TODO : Monstres PNJ suivant quête du personnage, refresh de la capacité à inviter à un atelier, Maison...
	}

	private void parseGameActionPacket(final String packet)
	{
		int actionID;
		try
		{
			actionID = Integer.parseInt(packet.substring(2,5));
		}catch(final NumberFormatException e){return;};
		
		int nextGameActionID = 0;
		if(_actions.size() > 0)
		{
			//On prend le plus haut GameActionID + 1
			nextGameActionID = (Integer)(_actions.keySet().toArray()[_actions.size()-1])+1;
		}
		final GameAction GA = new GameAction(nextGameActionID,actionID,packet);
		
		switch(actionID)
		{
			case 1://Deplacement
				game_parseDeplacementPacket(GA);
			break;
			case 300://Sort
				game_tryCastSpell(packet);
			break;
			case 303://Attaque CaC
				game_tryCac(packet);
			break;
			case 500://Action Sur Map
				game_action(GA);
			break;
			case 512://Utiliser un prisme
				game_use_prism();
			break;
			case 900://Demande Defie
				game_ask_duel(packet);
			break;
			case 901://Accepter Defie
				game_accept_duel(packet);
			break;
			case 902://Refus/Anuler Defie
				game_cancel_duel(packet);
			break;
			case 903://Rejoindre combat
				game_join_fight(packet);
			break;
			case 906://Agresser
				game_aggro(packet);
			break;
			case 909://Perco
				game_aggro_percepteur(packet);
			break;
			default:
				Console.printlnError("Unknown actionId: "+actionID);
				SocketManager.GAME_SEND_BN(_out);
			break;
		}	
	}

	private void game_use_prism() {
		final StringBuilder packet = new StringBuilder(10*World.getPrisms().size());
		final Prism prism = _player.getCurMap().getPrism();
		if(prism == null || prism.getAlign() != _player.getAlign() || prism.getFight() != null)
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "1143");
			return;
		}
		if(!_player.isShowingWings())
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "1144");
			return;
		}
		final String subAreaImpossible = ",161,162,163,164,165,166,167,";
		for(final Prism curPrism : World.getPrisms().values()) {
			if(curPrism == null) 
				continue;
			final DofusMap mapPrism = World.getMap(curPrism.getMapId());
			final int subArea = World.getMap(curPrism.getMapId()).getSubArea().getId();
			if(curPrism.getId() == prism.getId() || mapPrism.hasEndFightAction(0)) //FIXME Si c'est une map de donjon (pas forcément car des maps peuvent avoir des actions de tp sans pour autant être un dj)
				continue;
			if(subAreaImpossible.contains(","+subArea+","))
				continue;
			if(curPrism.getAlign() == _player.getAlign())
			{
				packet.append('|').append(curPrism.getMapId()).append(';')
				.append(Formulas.calculPrismCost(_player.getCurMap(), mapPrism, 
						_player.getALvl()));
			}
		}
		SocketManager.GAME_SEND_SUBWAY_PRISM_CREATE(_player, packet.toString());
	}

	private void game_aggro_percepteur(final String packet) {
		if(_player == null)return;
		if(_player.getFight() != null)return;
		if(_player.getIsTalkingWith() != 0 ||
		   _player.getIsTradingWith() != 0 ||
		   _player.getCurJobAction() != null ||
		   _player.getCurExchange() != null)
		{
			return;
		}
		final int id = Integer.parseInt(packet.substring(5));
		final TaxCollector target = World.getTaxCollector(id);
		if(target.getFight() != null) return;
		if(target.isInExchange())
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "1180");
			return;
		}
		SocketManager.GAME_SEND_GUILD_ON_PERCEPTEUR_ATTACKED(target);
		SocketManager.GAME_SEND_GA_PACKET_TO_MAP(_player.getCurMap(), "", 909, _player.getActorId()+"", id+"");
		_player.getCurMap().startFightVersusPercepteur(_player, target);
    }

	private void game_aggro(final String packet)
	{
		if(_player == null)return;
		if(_player.getFight() != null)return;
		final int id = Integer.parseInt(packet.substring(5));
		final Player target = World.getPlayer(id);
		if(target == null || !target.isOnline() || target.getFight() != null
				|| target.getCurMap().getId() != _player.getCurMap().getId()
				|| target.getAlign() == _player.getAlign()
				|| World.getTaxCollector(target.getIsTradingWith()) != null
				|| _player.getCurMap().getPlacesStr().equalsIgnoreCase("|"))
					return;
		
		if(target.getRestrictions().isTombe())
		{
			SocketManager.GAME_SEND_BN(_out);
			return;
		}

		_player.toogleWings('+');
		SocketManager.GAME_SEND_GA_PACKET_TO_MAP(_player.getCurMap(),"", 906, _player.getActorId()+"", id+"");
		_player.getCurMap().newFight(_player, target, Constants.FIGHT_TYPE_AGRESSION);
		/*if(target.getAlign() == 0)
		{
			_player.getFight().waitKnights(_player, target);
		}
		if(target.getAlign() == 0)
		{
			SocketManager.GAME_SEND_Im_PACKET(_player, "084;1");
			if(target.getCurCarte().getSubArea().getAlignement() < 1)
			{
				Fight curFight = target.getFight();
				int countKnight = 0;
				while(curFight.getState() == Constants.FIGHT_STATE_PLACE) // Freeze le joueur, impossible de bouger etc trouver une méthode qui ne freeze pas
				{
					ArrayList<Fighter> teamEnnemy = curFight.getFighters(curFight.getTeamID(_player.getGUID()));
					int nbEnnemies = teamEnnemy.size();
					while(countKnight < nbEnnemies && countKnight < 7)
					{
						if(curFight.GetNumbCellEmptys(curFight.getTeamID(target.getGUID())) > 0)
						{
							Fighter knight = new Fighter(target.getFight(), World.getMonstre(394).getGrades().get(Formulas.getKnightGradeByLvlEnnemy(teamEnnemy)));
							target.getFight().joinFight(knight, target.getGUID(), target.getCurCarte());
							countKnight++;
						}
					}
					ArrayList<Fighter> teamTarget = curFight.getFighters(curFight.getTeamID(target.getGUID()));
					ArrayList<Fighter> teamEnnemy = curFight.getFighters(curFight.getTeamID(_player.getGUID()));
					int tempcountKnight = 0, countEnnemies = teamEnnemy.size();
					for(Fighter f : teamTarget)
					{
						if(f.isMob() && f.getMob().getTemplate().getID() == 394)
						{
							tempcountKnight++;
						}
					}
					if(tempcountKnight < countKnight)
					{
						while(tempcountKnight < countEnnemies-1 || countKnight == 0)
						{
							if(curFight.GetNumbCellEmptys(curFight.getTeamID(target.getGUID())) > 0)
							{
								Fighter knight = new Fighter(target.getFight(), World.getMonstre(394).getGrades().get(Formulas.getKnightGradeByLvlEnnemy(teamEnnemy)));
								target.getFight().joinFight(knight, target.getGUID(), target.getCurCarte());
								countKnight++;
							}
						}
					}
				}
			}
		}*/
	}

	private void game_action(final GameAction GA)
	{
		final String packet = GA._packet.substring(5);
		int cellID = -1;
		int actionID = -1;
		
		try
		{
			cellID = Integer.parseInt(packet.split(";")[0]);
			actionID = Integer.parseInt(packet.split(";")[1]);
		}catch(final Exception e){}
		//Si packet invalide, ou cellule introuvable
		if(cellID == -1 || actionID == -1 || _player == null || _player.getCurMap() == null ||
				_player.getCurMap().getCell(cellID) == null)
			return;
		GA._args = cellID+";"+actionID;
		_player.getAccount().getGameThread().addAction(GA);
		_player.startActionOnCell(GA);
	}

	private void game_tryCac(final String packet)
	{
		try
		{
			if(_player.getFight() ==null)return;
			int cellID = -1;
			try
			{
				cellID = Integer.parseInt(packet.substring(5));
			}catch(final Exception e){return;};
			if(_player.getFight().getCurFighter().getGUID() 
					!= _player.getFight().getFighterByPerso(_player).getGUID() 
					|| !_player.getFight().getFighterByPerso(_player).canPlay()) {
				SocketManager.REALM_SEND_MESSAGE(_out, "07|");
				kick();
				return;
			}
			_player.getFight().tryCaC(_player,cellID);
		}catch(final Exception e){};
	}

	private void game_tryCastSpell(final String packet)
	{
		try
		{
			final String[] splt = packet.split(";");
			final int spellID = Integer.parseInt(splt[0].substring(5));
			final int caseID = Integer.parseInt(splt[1]);
			if(_player.getFight() != null)
			{
				if(_player.getFight().getCurFighter().getGUID() 
						!= _player.getFight().getFighterByPerso(_player).getGUID() 
						|| !_player.getFight().getFighterByPerso(_player).canPlay()) {
					SocketManager.REALM_SEND_MESSAGE(_out, "07|");
					kick();
					return;
				}
				final SpellStat SS = _player.getSortStatBySortIfHas(spellID);
				if(SS == null)return;
				_player.getFight().tryCastSpell(_player.getFight().getFighterByPerso(_player),SS,caseID);
			}
		}catch(final NumberFormatException e){return;};
	}

	private void game_join_fight(final String packet)
	{
		System.out.println("Pack "+packet);
		final String[] infos = packet.substring(5).split(";");
		if(infos.length == 1)
		{
			try
			{
				if(_player.getFight() != null) {
					SocketManager.GAME_SEND_GA903_ERROR_PACKET(_out, 'o', 0);
					return;
				}
				final Fight F = _player.getCurMap().getFight(Integer.parseInt(infos[0]));
				F.joinAsSpect(_player);
			}catch(final Exception e){return;};
		}else
		{
			int guid = 0;
			try
			{
				guid = Integer.parseInt(infos[1]);
			}catch(final NumberFormatException e){
				e.printStackTrace();
				return;
			}
			if(_player.getFight() != null) {
				SocketManager.GAME_SEND_GA903_ERROR_PACKET(_out, 'o', guid);
				return;
			}
			if(_player.isAway()) {
				SocketManager.GAME_SEND_GA903_ERROR_PACKET(_out,'o',guid);
				return;
			}
			if(World.getPlayer(guid) == null)
				return;
			Fight fight = World.getPlayer(guid).getFight();
			if(fight == null)
				return;
			if(fight.getState() == Constants.FIGHT_STATE_PLACE)
			{
				fight.joinFight(_player,guid);
			}
		}
	}

	private void game_accept_duel(final String packet)
	{
		int guid = -1;
		try{guid = Integer.parseInt(packet.substring(5));}catch(final NumberFormatException e){return;};
		if(_player.getDuelID() != guid || _player.getDuelID() == -1)return;
		SocketManager.GAME_SEND_MAP_START_DUEL_TO_MAP(_player.getCurMap(),_player.getDuelID(),_player.getActorId());
		final Fight fight = _player.getCurMap().newFight(World.getPlayer(_player.getDuelID()),_player,Constants.FIGHT_TYPE_CHALLENGE);
		_player.setFight(fight);
		World.getPlayer(_player.getDuelID()).setFight(fight);
		
	}

	private void game_cancel_duel(final String packet)
	{
		try
		{
			if(_player.getDuelID() == -1)return;
			SocketManager.GAME_SEND_CANCEL_DUEL_TO_MAP(_player.getCurMap(),_player.getDuelID(),_player.getActorId());
			World.getPlayer(_player.getDuelID()).setAway(false);
			World.getPlayer(_player.getDuelID()).setDuelID(-1);
			_player.setAway(false);
			_player.setDuelID(-1);	
		}catch(final NumberFormatException e){return;};
	}

	private void game_ask_duel(final String packet)
	{
		if(_player.getCurMap().getPlacesStr().equalsIgnoreCase("|"))
		{
			SocketManager.GAME_SEND_DUEL_Y_AWAY(_out, _player.getActorId());
			return;
		}
		try
		{
			final int guid = Integer.parseInt(packet.substring(5));
			if(_player.isAway() || _player.getFight() != null){SocketManager.GAME_SEND_DUEL_Y_AWAY(_out, _player.getActorId());return;}
			final Player challenger = World.getPlayer(guid);
			if(challenger == null) return;
			if(challenger.isAway() || challenger.getFight() != null || challenger.getCurMap().getId() != _player.getCurMap().getId()){SocketManager.GAME_SEND_DUEL_E_AWAY(_out, _player.getActorId());return;}
			_player.setDuelID(guid);
			_player.setAway(true);
			challenger.setDuelID(_player.getActorId());
			challenger.setAway(true);
			SocketManager.GAME_SEND_MAP_NEW_DUEL_TO_MAP(_player.getCurMap(),_player.getActorId(),guid);
		}catch(final NumberFormatException e){return;}
	}

	private void game_parseDeplacementPacket(final GameAction GA)
	{
		String path = GA._packet.substring(5);
		if(_player.getFight() == null)
		{
			if(_player.getPodUsed() > _player.getMaxPod())
			{
				SocketManager.GAME_SEND_Im_PACKET(_player, "112");
				removeAction(GA);
				return;
			}
			if(_player.getRestrictions().isTombe())
			{
				SocketManager.GAME_SEND_BN(_out);
				removeAction(GA);
				return;
			}
			final AtomicReference<String> pathRef = new AtomicReference<String>(path);
			int result = Pathfinding.isValidPath(_player.getCurMap(),_player.getCurCell().getId(),pathRef, null);
			
			//Si déplacement inutile
			if(result == 0)
			{
				SocketManager.GAME_SEND_GA_PACKET(_out,"", "0", "", "");
				removeAction(GA);
				return;
			}
			if(result != -1000 && result < 0)result = -result;
			
			//On prend en compte le nouveau path
			path = pathRef.get();
			//Si le path est invalide
			if(result == -1000)
			{
				Log.addToErrorLog(_player.getName()+"("+_player.getActorId()+") Tentative de  déplacement avec un path invalide");
				path = CryptManager.getHashedValueByInt(_player.getOrientation())+CryptManager.cellIDToCode(_player.getCurCell().getId());	
			}
			//On sauvegarde le path dans la variable
			GA._args = path;
			if (_player.getCurGameAction() != null) _player.setCurGameAction(null);
			
			//TODO MAKE 4VIEW
			SocketManager.GAME_SEND_GA_PACKET_TO_MAP(_player.getCurMap(), ""+GA._id, 1, _player.getActorId()+"", "a"+CryptManager.cellIDToCode(_player.getCurCell().getId())+path);
			addAction(GA);
			if(_player.isSitted())_player.setSitted(false);
			_player.setAway(true);
		}else
		{
			final Fighter F = _player.getFight().getFighterByPerso(_player);
			if(F == null)return;
			if(_player.getFight().getCurFighter().getGUID() != F.getGUID() || !F.canPlay()) {
				SocketManager.REALM_SEND_MESSAGE(_out, "07|");
				kick();
				return;
			}
			GA._args = path;
			_player.getFight().fighterDeplace(F,GA);
		}
	}

	public PrintWriter getOut() {
		return _out;
	}
	
	public void kick()
	{
		try
		{		
			Main.gameServer.delClient(this);
    		
    		if(_player != null)
				_player.closeLogger();
    		
    		if(_account != null)
    		{
    			_account.setWaiting(false);
				_account.setReloadedServ(false);
				_account.setValidQueue(false);
    			_account.disconnection();
    			//World.deleteAccount(_account.getGUID());
    		}
    		
    		if(!_socket.isClosed())
				_socket.close();
			_in.close();
    		_out.close();
    		Utils.refreshTitle();
		}catch(final IOException e1){
			e1.printStackTrace();
			Console.printlnError(e1.getMessage());
		};
	}

	private void parseAccountPacket(final String packet)
	{
		switch(packet.charAt(1))
		{
			case 'A':
				String[] infos = packet.substring(2).split("\\|");
				if(SQLManager.persoExist(infos[0]))
				{
					SocketManager.GAME_SEND_NAME_ALREADY_EXIST(_out);
					return;
				}
				//Validation du nom du personnage
				boolean isValid = true;
				final String name = infos[0].toLowerCase();
				//Vérifie d'abord si il contient des termes définit
				if(name.length() > 20
						|| name.contains("mj")
						|| name.contains("modo")
						|| name.contains("admin"))
				{
					isValid = false;
				}
				//Si le nom passe le test, on vérifie que les caractère entré sont correct.
				if(isValid)
				{
					int tiretCount = 0;
					for(char curLetter : name.toCharArray())
					{
						if(!((curLetter >= 'a' && curLetter <= 'z')
								|| curLetter == '-'))
						{
							isValid = false;
							break;
						}
						if(curLetter == '-')
						{
							if(tiretCount >= 2)
							{
								isValid = false;
								break;
							}
							else
							{
								tiretCount++;
							}
						}
					}
				}
				//Si le nom est invalide
				if(!isValid)
				{
					SocketManager.GAME_SEND_NAME_ALREADY_EXIST(_out);
					return;
				}
				if(_account.getPersoNumber() >= Config.CONFIG_MAX_PERSOS)
				{
					SocketManager.GAME_SEND_CREATE_PERSO_FULL(_out);
					return;
				}
				if(_account.createPerso(infos[0], Integer.parseInt(infos[2]), Integer.parseInt(infos[1]), Integer.parseInt(infos[3]),Integer.parseInt(infos[4]), Integer.parseInt(infos[5])))
				{
					SocketManager.GAME_SEND_CREATE_OK(_out);
					Main.linkServer.addCharacter(_account.getGUID());
					Gift.ParseToAgPacket(_out, _account);
					SocketManager.GAME_SEND_PERSO_LIST(_out, _account.getPlayers());
				}else
				{
					SocketManager.GAME_SEND_CREATE_FAILED(_out);
				}
			break;
			
			case 'B':
				int stat = -1;
				try
				{
					stat = Integer.parseInt(packet.substring(2).split("/u000A")[0]);
					_player.boostStat(stat);
				}catch(final NumberFormatException e){return;};
			break;
			case 'D':
				final String[] split = packet.substring(2).split("\\|");
				final int GUID = Integer.parseInt(split[0]);
				
				String reponse = split.length>1?split[1]:"";
				reponse = Formulas.parseResponse(reponse);
				
				if(_account.getPlayers().containsKey(GUID))
				{
					if(_account.getPlayers().get(GUID).getLvl() <20 ||(_account.getPlayers().get(GUID).getLvl() >=20 && reponse.equals(_account.getReponse())))
					{
						_account.deletePerso(GUID);
						Main.linkServer.delCharacter(_account.getGUID());
						Gift.ParseToAgPacket(_out, _account);
						SocketManager.GAME_SEND_PERSO_LIST(_out, _account.getPlayers());
					}
					else
						SocketManager.GAME_SEND_DELETE_PERSO_FAILED(_out);
				}else
					SocketManager.GAME_SEND_DELETE_PERSO_FAILED(_out);
			break;
			
			case 'f':
				final int queueID = 1;
				final int position = 1;
				SocketManager.MULTI_SEND_Af_PACKET(_out,position,1,1,0,queueID);
			break;
			
			case 'g':
				_account.setClientLanguage(packet.substring(2));
			break;
			
			case 'G':
				final String[] Arg = packet.substring(2).split("\\|");
				AddGift(Integer.parseInt(Arg[0]), Integer.parseInt(Arg[1]), _account);
				Gift.ParseToAgPacket(_out, _account);
				SocketManager.GAME_SEND_PERSO_LIST(_out, _account.getPlayers());
			break;
			
			case 'i':
				_account.setClientIdentity(packet.substring(2));
			break;
			
			case 'L':
				SocketManager.GAME_SEND_PERSO_LIST(_out, _account.getPlayers());
				//SocketManager.GAME_SEND_HIDE_GENERATE_NAME(_out);
			break;
			
			case 'k':
				_account.setClientKey(packet.substring(2));
			break;
			
			case 'P':
				SocketManager.GAME_SEND_GENERATE_NAME(_out);
			break;
			
			case 'R':
				final int playerGuid = Integer.parseInt(packet.substring(2));
				_account.getPlayers().get(playerGuid).revive();
				SocketManager.GAME_SEND_PERSO_LIST(_out, _account.getPlayers());
			break;
			
			case 'S':
				final int charID = Integer.parseInt(packet.substring(2));
				if(_account.getPlayers().get(charID) != null)
				{
					_account.setGameClient(this);
					_player = _account.getPlayers().get(charID);
					if(_player != null)
					{
						_player.OnJoinGame();
						return;
					}
				}
				SocketManager.GAME_SEND_PERSO_SELECTION_FAILED(_out);
			break;
				
			/*case 'T':
				final int guid = Integer.parseInt(packet.substring(2));
				_account = Main.gameServer.getWaitingAccount(guid);
				if(_account != null)
				{
					String ip = _socket.getInetAddress().getHostAddress();
					if(World.getAccount(guid) == null)
					{
						World.addAccount(_account);
						World.reassignAccountToChar(_account);
					}
					_account.setGameClient(this);
					_account.setCurIP(ip);
					SQLManager.SET_CUR_IP(ip, _account.getGUID());
					Main.gameServer.delWaitingAccount(_account);
					
					SocketManager.GAME_SEND_ATTRIBUTE_SUCCESS(_out);
				}else
				{
					SocketManager.GAME_SEND_ATTRIBUTE_FAILED(_out);
				}
			break;*/
			
			case 'T':
				final int guid = Integer.parseInt(packet.substring(2));
				
				_account = World.getAccount(guid);
				if(_account == null)
				{
					_account = Main.gameServer.getWaitingAccount(guid);
					World.addAccount(_account);
					World.reassignAccountToChar(_account);
				}
				if(_account != null)
				{
					String ip = _socket.getInetAddress().getHostAddress();
					_account.setGameClient(this);
					_account.setCurIP(ip);
					SQLManager.SET_CUR_IP(ip, _account.getGUID());
					Main.gameServer.delWaitingAccount(_account);
					SocketManager.GAME_SEND_ATTRIBUTE_SUCCESS(_out);
				}else
				{
					SocketManager.GAME_SEND_ATTRIBUTE_FAILED(_out);
				}
			break;
			
			case 'V':
				SocketManager.GAME_SEND_AV0(_out);
			break;
			
			default:
				Console.printlnError("Unknown packet recv: "+packet);
				SocketManager.GAME_SEND_BN(_out);
			break;
		}
	}
	
	public void AddGift(final int GiftID, final int PGUID, final Account _compte)
	{
		try {
			final Player P = World.getPlayer(PGUID);
			final Account C = P.getAccount();
			if (_compte != C) return; //Security
			if (!C.hasGift(GiftID))return;
                        
			final Gift Cadeau = C.getGiftbyID(GiftID);
            final int lineID = C.getLineGift(GiftID);
			for (final PackObject Obj : Cadeau.get_objects())
			{
				final Item obj = Obj.getTemplate().createNewItem(Obj.getQua(), Obj.isMax());
				if (P.addItem(obj, true)) 
				{
					World.addItem(obj, true);
				}
			}
			C.removeGift(lineID);
			SQLManager.REMOVE_GIFT(lineID);
			SocketManager.REALM_SEND_CLOSE_GIFTS_UI(_out);
		} catch (final Exception e) {
			SocketManager.REALM_SEND_CLOSE_GIFTS_UI(_out);
			return;
		}
	}

	public void removeAction(final GameAction GA)
	{
		//* DEBUG
		System.out.println("Supression de la GameAction id = "+GA._id);
		//*/
		_actions.remove(GA._id);
	}
	
	public void addAction(final GameAction GA)
	{
		_actions.put(GA._id, GA);
		//* DEBUG
		System.out.println("Ajout de la GameAction id = "+GA._id);
		System.out.println("Packet: "+GA._packet);
		//*/
	}
}

package common;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import objects.item.Item;
import objects.item.ItemTemplate;
import objects.job.JobStat;
import objects.map.DofusCell;
import objects.map.DofusMap;
import objects.map.InteractiveObject;
import objects.map.MountPark;
import objects.monster.MonsterGroup;
import objects.npc.Npc;
import objects.quest.Quest;
import objects.quest.QuestObjective;

import common.World.ItemSet;
import common.console.Log;
import common.utils.CryptManager;
import common.utils.Formulas;
import common.utils.Utils;

public class SocketManager {
	
	/*public static class PacketManager {
		
		static String lastPacket = "";
		static long lastTimePacket = 0;
		static Map<Integer, StringBuilder> buffer = new TreeMap<Integer, StringBuilder>();
		
		public static void registerClient(PrintWriter client) {
			int id = client.hashCode();
			if(!buffer.containsKey(id))
			{
				buffer.put(id, new StringBuilder());
			}
		}
		
		public static void append(PrintWriter client, String packet) {
			int id = client.hashCode();
			if(buffer.containsKey(id))
			{
				StringBuilder packets = buffer.get(id);
				packets.append(packet);
				buffer.remove(id);
				buffer.put(id, packets);
			}
		}
		
		public static void flushData(PrintWriter client) {
			int id = client.hashCode();
			if(buffer.containsKey(id) && buffer.get(id).length() > 0)
			{
				String packet = buffer.get(id).toString();
				client.print(packet + (char)0x00);
				client.flush();
				buffer.get(id).delete(0, buffer.get(id).length());
			}
		}
		
		public static void updateLastTimePacket() {
			lastTimePacket = System.currentTimeMillis();
		}
		
		public static boolean hasClient(PrintWriter client) {
			int id = client.hashCode();
			return buffer.containsKey(id);
		}
	}*/
	
	static String last_packet = "";
	static long last_time_packet = 0;
	static Map<Integer, String> buffer_packets = new TreeMap<Integer, String>();
	
	public static void start_buffering(final PrintWriter printWriter)
	{
		if(!buffer_packets.containsKey(printWriter.hashCode()))
		{
			final String empty_packet = "";
			buffer_packets.put(printWriter.hashCode(), empty_packet);
		}
	}
	
	public static void stop_buffering(final PrintWriter printWriter)
	{
		if(buffer_packets.containsKey(printWriter.hashCode()))
		{
			final String packet_bufferized = buffer_packets.remove(printWriter.hashCode());
			send(printWriter, packet_bufferized);
		}
	}
	
	public static void send(final Player player, final String packet)
	{
		if(player.getAccount() == null)return;
		if(player.getAccount().getGameThread() == null)return;
		final PrintWriter out = player.getAccount().getGameThread().getOut();
		send(out, packet);
	}
	
	public static void send(final PrintWriter out, String packet)
	{
		if(out != null && !packet.isEmpty() && !packet.equals(""+(char)0x00))
		{
			if(buffer_packets.containsKey(out.hashCode()))
			{
				final String packet_already_bufferized = buffer_packets.get(out.hashCode()) + (char)0x00;
				buffer_packets.put(out.hashCode(), packet_already_bufferized+packet);
			}else
			{
				packet = CryptManager.toUtf(packet);	//MARTHIEUBEAN
				out.print(packet+(char)0x00);
				out.flush();
				if(!(packet.equalsIgnoreCase(last_packet) && (System.currentTimeMillis() - last_time_packet) < 500))
				{
					if(Config.CONFIG_DEBUG)
						Log.addToSockLog("Game: Send >> "+packet);
				}
				last_packet = packet;
				last_time_packet = System.currentTimeMillis();
			}
		}
	}

	public static void MULTI_SEND_Af_PACKET(final PrintWriter out,final int position, final int totalAbo, final int totalNonAbo, final int subscribe, final int queueID)
	{
		final String packet = "Af"+position+'|'+totalAbo+'|'+totalNonAbo+'|'+subscribe+'|'+queueID;
		send(out,packet);
	}

	public static void GAME_SEND_HELLOGAME_PACKET(final PrintWriter out)
	{
		final String packet = "HG";
		send(out,packet);
	}

	public static void GAME_SEND_ATTRIBUTE_FAILED(final PrintWriter out)
	{
		final String packet = "ATE";
		send(out,packet);
	}
	
	public static void GAME_SEND_ATTRIBUTE_SUCCESS(final PrintWriter out)
	{
		final String packet = "ATK0";
		send(out,packet);
	}

	public static void GAME_SEND_AV0(final PrintWriter out)
	{
		final String packet = "AV0";
		send(out,packet);
	}
	
	public static void GAME_SEND_HIDE_GENERATE_NAME(final PrintWriter out)
	{
		final String packet = "APE2";
		send(out,packet);
	}
	
	public static void GAME_SEND_PERSO_LIST(final PrintWriter out, final Map<Integer, Player> persos)
	{
		final StringBuilder packet = new StringBuilder(35);
		packet.append("ALK").append(60*60*24*365*1000).append('|').append(persos.size());
		for(final Entry<Integer,Player > entry : persos.entrySet())
			packet.append(entry.getValue().parseALK());
		send(out,packet.toString());	
	}

	public static void GAME_SEND_NAME_ALREADY_EXIST(final PrintWriter out)
	{
		final String packet = "AAEa";
		send(out,packet);
	}
	
	public static void GAME_SEND_CREATE_PERSO_FULL(final PrintWriter out)
	{
		final String packet = "AAEf";
		send(out,packet);
	}

	public static void GAME_SEND_CREATE_OK(final PrintWriter out)
	{
		final String packet = "AAK";
		send(out,packet);
	}
	
	public static void SEND_ALREADY_CONNECTED(final PrintWriter out) 
	{
		final String packet = "AlEc";
		send(out, packet);
	}

	public static void GAME_SEND_DELETE_PERSO_FAILED(final PrintWriter out)
	{
		final String packet = "ADE";
		send(out,packet);
	}

	public static void GAME_SEND_CREATE_FAILED(final PrintWriter out)
	{
		final String packet = "AAEF";
		send(out,packet);
	}

	public static void GAME_SEND_PERSO_SELECTION_FAILED(final PrintWriter out)
	{
		final String packet = "ASE";
		send(out,packet);
	}

	public static void GAME_SEND_STATS_PACKET(final Player perso)
	{
		final String packet = perso.getAsPacket();
		send(perso,packet);
	}
	
	public static void GAME_SEND_CHOOSE_PSEUDO(final PrintWriter out)
	{
		final String packet = "AlEr";
		send(out, packet);
	}
	
	public static void GAME_SEND_Rx_PACKET(final Player out)
	{
		final String packet = "Rx"+out.getMountXpGive();
		send(out,packet);
	}
	public static void GAME_SEND_Rn_PACKET(final Player out,final String name)
	{
		final String packet = "Rn"+name;
		send(out,packet);
	}
	public static void GAME_SEND_Re_PACKET(final Player out,final String sign,final Mount DD)
	{
		String packet = "Re"+sign;
		if(sign.equals("+"))packet += DD.parseInfos();
		
		send(out,packet);
	}
	public static void GAME_SEND_ASK(final PrintWriter out,final Player perso)
	{
		final StringBuilder packet = new StringBuilder().append("ASK|");
		packet.append(perso.getActorId()).append('|');
		packet.append(perso.getName()).append('|');
		packet.append(perso.getLvl()).append('|');
		packet.append(perso.getBreedId()).append('|');
		packet.append(perso.getSexe()).append('|');
		packet.append(perso.getGfxID()).append('|');
		packet.append((perso.getColor1()==-1?"-1":Integer.toHexString(perso.getColor1()))).append('|');
		packet.append((perso.getColor2()==-1?"-1":Integer.toHexString(perso.getColor2()))).append('|');
		packet.append((perso.getColor3()==-1?"-1":Integer.toHexString(perso.getColor3()))).append('|');
		packet.append(perso.parseItemToASK());
		
		send(out,packet.toString());
	}
	
	public static void GAME_SEND_ALIGNEMENT(final PrintWriter out,final int alliID)
	{
		final String packet = "ZS"+alliID;
		send(out,packet);
	}

	public static void GAME_SEND_ADD_CANAL(final PrintWriter out, final String chans)
	{
		final String packet = "cC+"+chans;
		send(out,packet);
	}

	public static void GAME_SEND_ZONE_ALLIGN_STATUT(final PrintWriter out)
	{
		final String packet = "al|"+World.getSousZoneStateString();
		send(out,packet);
	}

	public static void GAME_SEND_SEESPELL_OPTION(final PrintWriter out, final boolean spells)
	{
		final String packet = "SLo"+(spells?'+':'-');
		send(out,packet);
	}
	
	public static void GAME_SEND_RESTRICTIONS(final PrintWriter out)
	{
		final String packet =  "AR6bk";
		send(out,packet);
	}
	
	public static void GAME_SEND_RESTRICTIONS(final PrintWriter out, final String rights) {
		final String packet = "AR"+rights;
		send(out,packet);
    }
	
	public static void GAME_SEND_Ow_PACKET(final Player perso)
	{
		final StringBuilder packet = new StringBuilder("Ow");
		packet.append(perso.getPodUsed());
		packet.append('|');
		packet.append(perso.getMaxPod());
		send(perso, packet.toString());
	}
	public static void GAME_SEND_OT_PACKET(final PrintWriter out, final int id)
	{
		String packet =  "OT";
		if(id > 0) packet += id;
		send(out,packet);
	}
	public static void GAME_SEND_SEE_FRIEND_CONNEXION(final PrintWriter out,final boolean see)
	{
		final String packet = "FO"+(see?'+':'-');
		send(out,packet);
	}

	public static void GAME_SEND_GAME_CREATE(final PrintWriter out, final String _name)
	{
		final String packet = "GCK|1|"+_name;
		send(out,packet);
	}
	
	public static void GAME_SEND_SERVER_HOUR(final PrintWriter out)
	{
		final String packet = Utils.getServerTime();
		send(out,packet);
	}
	
	public static void GAME_SEND_SERVER_DATE(final PrintWriter out)
	{
		final String packet = Utils.getServerDate();
		send(out,packet);
	}

	public static void GAME_SEND_MAPDATA(final PrintWriter out, final int id, final String date,final String key)
	{
		final String packet = "GDM|"+id+'|'+date+'|'+key;
		send(out,packet);
	}
	
	public static void GAME_SEND_GDK_PACKET(final PrintWriter out)
	{
		final String packet = "GDK";
		send(out,packet);
	}

	public static void GAME_SEND_MAP_MOBS_GMS_PACKETS(final PrintWriter out, final DofusMap carte)
	{
		final String packet = carte.getMobGroupGMsPackets();
		if(packet.isEmpty())return;
		send(out,packet);
	}
	
	public static void GAME_SEND_MAP_OBJECTS_GDS_PACKETS(final PrintWriter out, final DofusMap carte)
	{
		final String packet = carte.getObjectsGDsPackets();
		if(packet.isEmpty())return;
		send(out,packet);
	}
	
	public static void GAME_SEND_MAP_NPCS_GMS_PACKETS(final PrintWriter out, final DofusMap carte)
	{
		final String packet = carte.getNpcsGMsPackets();
		if(packet.isEmpty())return;
		send(out,packet);
	}
	
	public static void GAME_SEND_MAP_GMS_PACKETS(final PrintWriter out, final DofusMap carte)
	{
		final String packet = carte.getGMsPackets();
		send(out,packet);
	}

	public static void GAME_SEND_ERASE_ON_MAP_TO_MAP(final DofusMap map,final int guid)
	{
		final String packet = "GM|-"+guid;
		for(int z=0;z < map.getPersos().size();z++)
		{
			if(map.getPersos().get(z).getAccount().getGameThread() == null)continue;
			send(map.getPersos().get(z).getAccount().getGameThread().getOut(),packet);
		}
	}

	public static void GAME_SEND_ADD_PLAYER_TO_MAP(final DofusMap map, final Player perso)
	{
		final String packet = "GM|+"+perso.parseToGM();
		synchronized(map.getPersos()) {
			for(final Player P : map.getPersos())
				send(P,packet);		
		}
	}

	public static void GAME_SEND_DUEL_Y_AWAY(final PrintWriter out, final int guid)
	{
		final String packet = "GA;903;"+guid+";o";
		send(out,packet);
	}

	public static void GAME_SEND_DUEL_E_AWAY(final PrintWriter out, final int guid)
	{
		final String packet = "GA;903;"+guid+";z";
		send(out,packet);
	}

	public static void GAME_SEND_MAP_NEW_DUEL_TO_MAP(final DofusMap map,final int guid, final int guid2)
	{
		final String packet = "GA;900;"+guid+";"+guid2;
		for(final Player P : map.getPersos())send(P,packet);
	}
	
	public static void GAME_SEND_CANCEL_DUEL_TO_MAP(final DofusMap map, final int guid,final int guid2)
	{
		final String packet = "GA;902;"+guid+";"+guid2;
		for(final Player P : map.getPersos())
			send(P,packet);
	}
	
	public static void GAME_SEND_MAP_START_DUEL_TO_MAP(final DofusMap map,final int guid, final int guid2)
	{
		final String packet = "GA;901;"+guid+";"+guid2;
		for(final Player P : map.getPersos())
			send(P,packet);
	}

	public static void GAME_SEND_MAP_FIGHT_COUNT(final PrintWriter out,final DofusMap map)
	{
		final String packet = "fC"+map.getNbrFight();
		send(out,packet);
	}
	
	public static void GAME_SEND_MAP_FIGHT_COUNT(final DofusMap map)
	{
		final String packet = "fC"+map.getNbrFight();
		for(Player out : map.getPersos()) {
			if(out != null && out.isOnline()) {
				send(out,packet);
			}
		}
	}

	public static void GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(final Fight fight, final int teams,final int state, final int cancelBtn, final int duel, final int spec, final int time, final int type)
	{
		final String packet = "GJK"+state+'|'+cancelBtn+'|'+duel+'|'+spec+'|'+time+'|'+type;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			send(f.getPlayer(),packet);
		}
	}
	
	public static void GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(final Fight fight,final int teams, final String places, final int team)
	{
		final String packet = "GP"+places+'|'+team;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}

	public static void GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(final DofusMap map)
	{
		final String packet = "fC"+map.getNbrFight();
		for(int z=0;z < map.getPersos().size();z++)
			send(map.getPersos().get(z),packet);
	}
	
	public static void GAME_SEND_GAME_ADDFLAG_PACKET(final PrintWriter out, final DofusMap map,final int arg1, final int guid1,final int guid2,final int cell1,final String str1,final int cell2,final String str2)
	{
		final StringBuilder packet = new StringBuilder();
		packet.append("Gc+").append(guid1).append(';').append(arg1).append('|').append(guid1).append(';')
		.append(cell1).append(';').append(str1).append('|').append(guid2).append(';').append(cell2).append(';').append(str2);
		send(out,packet.toString());
	}

	
	public static void GAME_SEND_REMOVE_IN_TEAM_PACKET(final DofusMap map,final int teamID,final Fighter fighter)
	{
		final StringBuilder packet = new StringBuilder();
		packet.append("Gt").append(teamID).append("|-").append(fighter.getGUID());
		for(Player out : map.getPersos()) {
			if(out != null && out.isOnline()) {
				send(out,packet.toString());
			}
		}
	}
	
	public static void GAME_SEND_ADD_IN_TEAM_PACKET(final PrintWriter out, final DofusMap map,final int teamID,final Fighter fighter)
	{
		final StringBuilder packet = new StringBuilder();
		packet.append("Gt").append(teamID).append("|+").append(fighter.getGUID()).append(';').append(fighter.getPacketsName()).append(';').append(fighter.getLvl());
		send(out,packet.toString());
	}

	public static void GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(final DofusMap map,final int arg1, final int guid1,final int guid2,final int cell1,final String str1,final int cell2,final String str2)
	{
		final String packet = "Gc+"+guid1+';'+arg1+'|'+guid1+';'+cell1+';'+str1+'|'+guid2+';'+cell2+';'+str2;
		for(int z=0;z < map.getPersos().size();z++)
			send(map.getPersos().get(z),packet);
	}
	
	public static void GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(final DofusMap map, final int guid)
	{
		final String packet = "Gc-"+guid;
		for(int z=0;z < map.getPersos().size();z++)
			send(map.getPersos().get(z),packet);
	}
	
	public static void GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(final DofusMap map,final int teamID,final Fighter perso)
	{
		final String packet = "Gt"+teamID+"|+"+perso.getGUID()+';'+perso.getPacketsName()+';'+perso.getLvl();
		for(int z=0;z < map.getPersos().size();z++)
			send(map.getPersos().get(z),packet);
	}

	
	public static void GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(final DofusMap map)
	{
		final String packet = map.getMobGroupGMsPackets(); // Un par un comme sa lors du respawn :)
		for(final Player z:map.getPersos())
			send(z,packet);
	}
	
	public static void GAME_SEND_MAP_MOBS_GM_PACKET(final DofusMap map, final MonsterGroup current_Mobs)
	{
		final StringBuilder packet = new StringBuilder(5+30*current_Mobs.getSize());
		packet.append("GM");
		packet.append(current_Mobs.parseGM()); // Un par un comme sa lors du respawn :)
		for(final Player z:map.getPersos())
			send(z,packet.toString());
	}
	
	public static void GAME_SEND_MAP_PLAYERS_GMS_PACKETS(final DofusMap map, final Player _perso)
	{
		final String packet = map.getGMsPackets();
		send(_perso, packet);
	}
	
	public static void GAME_SEND_ON_EQUIP_ITEM(final DofusMap map, final Player _perso)
	{
		final String packet = _perso.parseToOa();
		for(final Player z:map.getPersos())
			send(z,packet);
	}

	public static void GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(final Fight fight, final int teams, final DofusMap map, final int guid, final int cell)
	{
		final String packet = "GIC|"+guid+';'+cell+";1";
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
				send(f.getPlayer(),packet);
		}
	}

	public static void GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(final DofusMap map,final char s,final char option, final int guid)
	{
		final String packet = "Go"+s+option+guid;
		for(final Player z:map.getPersos())
			send(z,packet);
	}
	
	public static void GAME_SEND_FIGHT_PLAYER_READY_TO_FIGHT(final Fight fight,final int teams, final int guid, final boolean b)
	{
		final String packet = "GR"+(b?'1':'0')+guid;
		if(fight.getState() != 2)return;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			if(f.hasLeft())continue;
				send(f.getPlayer(),packet);
		}
	}

	public static void GAME_SEND_GJK_PACKET(final Player out,final int state,final int cancelBtn,final int duel,final int spec,final int time,final int unknown)
	{
		final String packet = "GJK"+state+'|'+cancelBtn+'|'+duel+'|'+spec+'|'+time+'|'+unknown;
		send(out,packet);
	}

	public static void GAME_SEND_FIGHT_PLACES_PACKET(final PrintWriter out,final String places, final int team)
	{
		final String packet = "GP"+places+'|'+team;
		send(out,packet);
	}
	
	public static void GAME_SEND_Im_PACKET(final Player out,final String str)
	{
		final String packet = "Im"+str;
		send(out,packet);
	}
	public static void GAME_SEND_ILS_PACKET(final Player out,final int i)
	{
		final String packet = "ILS"+i;
		send(out,packet);
	}public static void GAME_SEND_ILF_PACKET(final Player P,final int i)
	{
		final String packet = "ILF"+i;
		send(P,packet);
	}
	public static void GAME_SEND_Im_PACKET_TO_MAP(final DofusMap map, final String id)
	{
		final String packet = "Im"+id;
		for(final Player z:map.getPersos())
			send(z,packet);
	}
	public static void GAME_SEND_eUK_PACKET_TO_MAP(final DofusMap map, final int guid, final int emote)
	{
		final String packet = "eUK"+guid+'|'+emote;
		for(final Player z:map.getPersos())
			send(z,packet);
	}
	public static void GAME_SEND_Im_PACKET_TO_FIGHT(final Fight fight,final int teams, final String id)
	{
		final String packet = "Im"+id;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}
	
	public static void GAME_SEND_MESSAGE(final Player out,final String mess, final String color)
	{
		final String packet = "cs<font color='#"+color+"'>"+mess+"</font>";
		send(out,packet);
	}
	
	public static void GAME_SEND_MESSAGE_TO_MAP(final DofusMap map,final String mess, final String color)
	{
		final String packet = "cs<font color='#"+color+"'>"+mess+"</font>";
		for(final Player perso : map.getPersos())
			send(perso,packet);
	}

	public static void GAME_SEND_GA903_ERROR_PACKET(final PrintWriter out, final char c,final int guid)
	{
		final String packet = "GA;903;"+guid+";"+c;
		send(out,packet);
	}
	public static void GAME_SEND_GIC_PACKETS_TO_FIGHT(final Fight fight,final int teams)
	{
		final StringBuilder packet = new StringBuilder("GIC|");
		for(final Fighter p : fight.getFighters(3))
		{
			if(p.getFightCell() == null)continue;
			packet.append(p.getGUID()).append(';').append(p.getFightCell().getId()).append(";1|");
		}
		for(final Fighter perso:fight.getFighters(teams))
		{
			if(perso.hasLeft())continue;
			if(perso.getPlayer() == null || !perso.getPlayer().isOnline())continue;
			send(perso.getPlayer(),packet.toString());
		}
	}
	public static void GAME_SEND_GIC_PACKETS(final Fight fight, final Player perso)
	{
		final StringBuilder packet = new StringBuilder("GIC|");
		for(final Fighter p : fight.getFighters(3))
		{
			if(p.getFightCell() == null)continue;
			packet.append(p.getGUID()).append(';').append(p.getFightCell().getId()).append(";1|");
		}
		send(perso,packet.toString());
	}
	public static void GAME_SEND_GIC_PACKET_TO_FIGHT(final Fight fight,final int teams,final Fighter f)
	{
		final StringBuilder packet = new StringBuilder("GIC|");
		packet.append(f.getGUID()).append(';').append(f.getFightCell().getId()).append(";1|");

		for(final Fighter perso:fight.getFighters(teams))
		{
			if(perso.hasLeft())continue;
			if(perso.getPlayer() == null || !perso.getPlayer().isOnline())continue;
			send(perso.getPlayer(),packet.toString());
		}
	}
	public static void GAME_SEND_GS_PACKET_TO_FIGHT(final Fight fight,final int teams)
	{
		final String packet = "GS";
		for(final Fighter f:fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			f.initBuffStats();
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}
	public static void GAME_SEND_GS_PACKET(final Player out)
	{
		final String packet = "GS";
		send(out,packet);
	}
	public static void GAME_SEND_GTL_PACKET_TO_FIGHT(final Fight fight, final int teams)
	{
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),fight.getGTL());
		}
	}
	public static void GAME_SEND_GTL_PACKET(final Player out,final Fight fight)
	{
		final String packet = fight.getGTL();
		send(out,packet);
	}
	public static void GAME_SEND_GTM_PACKET_TO_FIGHT(final Fight fight, final int teams)
	{
		final StringBuilder packet = new StringBuilder("GTM");
		for(final Fighter f : fight.getFighters(3))
		{
			packet.append('|').append(f.getGUID()).append(';');
			if(f.isDead())
			{
				packet.append('1');
				continue;
			}else
			packet.append("0;")
			.append(f.getLife()).append(';')
			.append(f.getPA()).append(';')
			.append(f.getPM()).append(';')
			.append((f.isHide()?"-1":f.getFightCell().getId())).append(';')//On envoie pas la cell d'un invisible :p
			.append(';')//??
			.append(f.getMaxLife());
		}
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet.toString());
		}
	}
	public static void GAME_SEND_GTM_PACKET(final Player player, final Fight fight)
	{
		final StringBuilder packet = new StringBuilder("GTM");
		for(final Fighter f : fight.getFighters(3))
		{
			packet.append('|').append(f.getGUID()).append(';');
			if(f.isDead())
			{
				packet.append('1');
				continue;
			}else
			packet.append("0;")
			.append(f.getLife()).append(';')
			.append(f.getPA()).append(';')
			.append(f.getPM()).append(';')
			.append((f.isHide()?"-1":f.getFightCell().getId())).append(';')//On envoie pas la cell d'un invisible :p
			.append(';')//??
			.append(f.getMaxLife());
		}
		send(player,packet.toString());
	}
	public static void GAME_SEND_GAMETURNSTART_PACKET_TO_FIGHT(final Fight fight,final int teams, final int guid, final int time)
	{
		final String packet = "GTS"+guid+'|'+time;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}
	public static void GAME_SEND_GAMETURNSTART_PACKET(final Player P,final int guid, final int time)
	{
		final String packet = "GTS"+guid+'|'+time;
		send(P,packet);
	}
	public static void GAME_SEND_GV_PACKET(final Player P)
	{
		final String packet = "GV";
		send(P,packet);
	}
	public static void GAME_SEND_PONG(final PrintWriter out)
	{
		final String packet = "pong";
		send(out,packet);
	}
	public static void GAME_SEND_QPONG(final PrintWriter out)
	{
		final String packet = "qpong";
		send(out,packet);
	}
	public static void GAME_SEND_GAS_PACKET_TO_FIGHT(final Fight fight,final int teams, final int guid)
	{
		final String packet = "GAS"+guid;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}
	
	public static void GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(final Fight fight,final int teams)
	{
		String packet = "GA;0";
		
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}
	
	public static void GAME_SEND_GA_PACKET_TO_FIGHT(final Fight fight,final int teams, final int actionID,final String s1, final String s2)
	{
		String packet = "GA;"+actionID+';'+s1;
		if(!s2.isEmpty())
			packet+=';'+s2;
		
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}
	
	public static void GAME_SEND_GA_PACKET(final PrintWriter out, final String actionID,final String s0,final String s1, final String s2)
	{
		String packet = "GA"+actionID+';'+s0;
		if(!s1.isEmpty())
			packet += ';'+s1;
		if(!s2.isEmpty())
			packet+=';'+s2;
		
		send(out,packet);
	}
	
	public static void GAME_SEND_GA_PACKET_TO_FIGHT(final Fight fight,final int teams,final int gameActionID,final String s1, final String s2,final String s3)
	{
		final String packet = "GA"+gameActionID+';'+s1+';'+s2+';'+s3;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}
	
	public static void GAME_SEND_GAMEACTION_TO_FIGHT(final Fight fight, final int teams,final String packet)
	{
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}

	public static void GAME_SEND_GAF_PACKET_TO_FIGHT(final Fight fight, final int teams, final int i1,final int guid)
	{
		final String packet = "GAF"+i1+'|'+guid;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}

	public static void GAME_SEND_BN(final Player out)
	{
		final String packet = "BN";
		send(out,packet);
	}
	
	public static void GAME_SEND_BN(final PrintWriter out)
	{
		final String packet = "BN";
		send(out,packet);
	}

	public static void GAME_SEND_GAMETURNSTOP_PACKET_TO_FIGHT(final Fight fight,final int teams, final int guid)
	{
		final String packet = "GTF"+guid;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}

	public static void GAME_SEND_GTR_PACKET_TO_FIGHT(final Fight fight, final int teams,final int guid)
	{
		final String packet = "GTR"+guid;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}

	public static void GAME_SEND_EMOTICONE_TO_MAP(final DofusMap map,final int guid, final int id)
	{
		final String packet = "cS"+guid+'|'+id;
		for(final Player perso : map.getPersos())
			send(perso,packet);
	}

	public static void GAME_SEND_SPELL_UPGRADE_FAILED(final PrintWriter _out)
	{
		final String packet = "SUE";
		send(_out,packet);
	}
	
	public static void GAME_SEND_SPELL_UPGRADE_SUCCED(final PrintWriter _out,final int spellID,final int level)
	{
		final String packet = "SUK"+spellID+'~'+level;
		send(_out,packet);
	}
	
	public static void GAME_SEND_SPELL_LIST(final Player perso)
	{
		final String packet = perso.parseSpellList();
		send(perso,packet);
	}

	public static void GAME_SEND_FIGHT_PLAYER_DIE_TO_FIGHT(final Fight fight, final int teams,final int guid)
	{
		final String packet = "GA;103;"+guid+';'+guid;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft() || f.getPlayer() == null)continue;
			if(f.getPlayer().isOnline())
				send(f.getPlayer(),packet);
		}
	}

	public static void GAME_SEND_FIGHT_GE_PACKET_TO_FIGHT(final Fight fight, final int teams, final int win)
	{
		final String packet = fight.GetGE(win);
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft() || f.getPlayer() == null)continue;
			if(f.getPlayer().isOnline())
				send(f.getPlayer(),packet);
		}
	}
	
	public static void GAME_SEND_FIGHT_GE_PACKET_TO_FIGHT(final Fight fight, final int teams, final String packetGE)
	{
		final String packet = packetGE;
		for(final Fighter f : fight.getFighters(teams))
		{
			final Player out = f.getPlayer();
			if(f.hasLeft() || out == null)continue;
			if(out.isOnline())
				send(out,packet);
		}
	}
	
	public static void GAME_SEND_FIGHT_GE_PACKET(final PrintWriter out,final Fight fight, final int win)
	{
		final String packet = fight.GetGE(win);
		send(out,packet);
	}
	
	public static void GAME_SEND_FIGHT_GIE_TO_FIGHT(final Fight fight, final int teams,final int mType,final int cible,final int value,final String mParam2,final String mParam3,final String mParam4, final int turn,final int spellID)
	{
		final String packet = "GIE"+mType+';'+cible+';'+value+';'+mParam2+';'+mParam3+';'+mParam4+';'+turn+';'+spellID;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft() || f.getPlayer() == null)continue;
			if(f.getPlayer().isOnline())
			send(f.getPlayer(),packet);
		}
	}
	
	public static void GAME_SEND_FIGHT_GIE(final Player fighter, final int mType,final int cible,final int value,final String mParam2,final String mParam3,final String mParam4, final int turn,final int spellID)
	{
		final String packet = "GIE"+mType+';'+cible+';'+value+';'+mParam2+';'+mParam3+';'+mParam4+';'+turn+';'+spellID;
		send(fighter,packet);
	}
	
	public static void GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(final Fight fight, final int teams,final DofusMap map)
	{
		final String packet = map.getFightersGMsPackets();
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}

	public static void GAME_SEND_MAP_FIGHT_GMS_PACKETS(final Fight fight,final DofusMap map, final Player _perso)
	{
		final String packet = map.getFightersGMsPackets();
		send(_perso, packet);
	}
	
	public static void GAME_SEND_FIGHT_PLAYER_JOIN(final Fight fight,final int teams, final Fighter _fighter) {
		final String packet = _fighter.getGmPacket('+');
		
		for(final Fighter f : fight.getFighters(teams))
		{
			if (f != _fighter)
			{
				final Player player = f.getPlayer();
				if(player == null || !player.isOnline())continue;
				if(player.getAccount().getGameThread() != null)
					send(player,packet);
			}
		}
	}
	
	public static void GAME_SEND_FIGHT_PLAYER_KICK(final Fight fight,final int teams, final int guid) {
		final String packet = "GM|-"+guid;
		
		for(final Fighter f : fight.getFighters(teams))
		{
			final Player player = f.getPlayer();
			if(player == null || !player.isOnline())continue;
			if(player.getAccount().getGameThread() != null)
				send(player,packet);
		}
	}
	
	public static void GAME_SEND_cMK_PACKET(final Player perso,final String suffix,final int guid,final String name,final String msg)
	{
		final String packet = "cMK"+suffix+'|'+guid+'|'+name+'|'+msg;
		send(perso,packet);
	}
	
	public static void GAME_SEND_FIGHT_LIST_PACKET(final PrintWriter out,final DofusMap map)
	{
		final StringBuilder packet = new StringBuilder(2+(1*map.getFights().size())+(25*map.getFights().size())).append("fL");
		for(final Entry<Integer,Fight> entry : map.getFights().entrySet())
		{
			if(packet.length()>2)
			{
				packet.append('|');
			}
			packet.append(entry.getValue().parseFightInfos());
		}
		send(out,packet.toString());
	}
	
	public static void GAME_SEND_cMK_PACKET_TO_MAP(final DofusMap map,final String suffix,final int guid,final String name,final String msg)
	{
		final String packet = "cMK"+suffix+'|'+guid+'|'+name+'|'+msg;
		for(final Player perso : map.getPersos())
			send(perso,packet);
	}
	public static void GAME_SEND_cMK_PACKET_TO_GUILD(final Guild g,final String suffix,final int guid,final String name,final String msg)
	{
		final String packet = "cMK"+suffix+'|'+guid+'|'+name+'|'+msg;
		for(final Player perso : g.getMembers())
		{
			if(perso == null || !perso.isOnline())continue;
			send(perso,packet);
		}
	}
	public static void GAME_SEND_cMK_PACKET_TO_ALL(final String suffix,final int guid,final String name,final String msg)
	{
		final String packet = "cMK"+suffix+'|'+guid+'|'+name+'|'+msg;
		for(final Player perso : World.getOnlinePersos())
			send(perso,packet);
	}
	public static void GAME_SEND_cMK_PACKET_TO_ADMIN(final String suffix,final int guid,final String name,final String msg)
	{
		final String packet = "cMK"+suffix+'|'+guid+'|'+name+'|'+msg;
		for(final Player perso : World.getOnlinePersos())if(perso.isOnline())if(perso.getAccount() != null)if(perso.getAccount().getGmLvl()>0)
			send(perso,packet);
	}
	public static void GAME_SEND_cMK_PACKET_TO_FIGHT(final Fight fight,final int teams,final String suffix,final int guid,final String name,final String msg)
	{
		final String packet = "cMK"+suffix+'|'+guid+'|'+name+'|'+msg;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}
	public static void GAME_SEND_cMK_PACKET_TO_ALIGN(final byte align,final int guid,final String name,final String msg)
	{
		final String packet = "cMK!|"+guid+'|'+name+'|'+msg;	//'!' = Canal alignement
		for(final Player perso : World.getOnlinePersos())
		{
			if(perso.getAlign() != align)continue;
			send(perso,packet);
		}
	}
	
	public static void GAME_SEND_GDZ_PACKET_TO_FIGHT(final Fight fight,final int teams,final String suffix,final int cell,final int size,final int unk)
	{
		final String packet = "GDZ"+suffix+cell+';'+size+';'+unk;
		
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}
	
	public static void GAME_SEND_GDC_PACKET_TO_FIGHT(final Fight fight,final int teams,final int cell)
	{
		final String packet = "GDC"+cell;
		
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
			send(f.getPlayer(),packet);
		}
	}
	
	public static void GAME_SEND_GA2_PACKET(final PrintWriter out, final int guid)
	{
		final String packet = "GA;2;"+guid+';';
		send(out,packet);
	}
	
	public static void GAME_SEND_CHAT_ERROR_PACKET(final PrintWriter out,final String name)
	{
		final String packet = "cMEf"+name;
		send(out,packet);
	}

	public static void GAME_SEND_eD_PACKET_TO_MAP(final DofusMap map,final int guid, final int dir)
	{
		final String packet = "eD"+guid+'|'+dir;
		for(final Player perso : map.getPersos())
			send(perso,packet);
	}

	public static void GAME_SEND_ECK_PACKET(final Player out, final int type,final String str)
	{
		String packet = "ECK"+type;
		if(!str.isEmpty())packet += '|'+str;
		send(out,packet);
	}
	
	public static void GAME_SEND_ECK_PACKET(final PrintWriter out, final int type,final String str)
	{
		String packet = "ECK"+type;
		if(!str.isEmpty())packet += '|'+str;
		send(out,packet);
	}
	
	public static void GAME_SEND_ITEM_VENDOR_LIST_PACKET(final PrintWriter out, final Npc npc)
	{
		final String packet = "EL"+npc.getTemplate().getItemVendorList();
		send(out,packet);
	}

	public static void GAME_SEND_EV_PACKET(final PrintWriter out)
	{
		final String packet = "EV";
		send(out,packet);
	}

	public static void GAME_SEND_DCK_PACKET(final PrintWriter out, final int id)
	{
		final String packet = "DCK"+id;
		send(out,packet);	
	}

	public static void GAME_SEND_QUESTION_PACKET(final PrintWriter out,final String str)
	{
		final String packet = "DQ"+str;
		send(out,packet);
	}

	public static void GAME_SEND_END_DIALOG_PACKET(final PrintWriter out)
	{
		final String packet = "DV";
		send(out,packet);
	}

	public static void GAME_SEND_CONSOLE_MESSAGE_PACKET(final PrintWriter out, final String mess)
	{
		final String packet = "BAT2"+mess;
		send(out,packet);
	}

	public static void GAME_SEND_CONSOLE_MESSAGE_TO_ADMIN(final String mess,final int minGmLvl)
	{
		final String packet = "BAT2"+mess;
		
		final ArrayList<Player> perso = new ArrayList<Player>();
		perso.addAll(World.getOnlinePersos());
		
		for(final Player p : perso)
		{
			if(!p.isOnline())continue;
			if(p.getAccount().getGmLvl() < minGmLvl)continue;
			
			send(p,packet);
		}
	}

	public static void GAME_SEND_BUY_ERROR_PACKET(final PrintWriter out)
	{
		final String packet = "EBE";
		send(out,packet);
	}

	public static void GAME_SEND_SELL_ERROR_PACKET(final PrintWriter out)
	{
		final String packet = "ESE";
		send(out,packet);
	}
	
	public static void GAME_SEND_BUY_OK_PACKET(final PrintWriter out)
	{
		final String packet = "EBK";
		send(out,packet);
	}

	public static void GAME_SEND_OBJECT_QUANTITY_PACKET(final Player out, final Item obj)
	{
		final String packet = "OQ"+obj.getGuid()+'|'+obj.getQuantity();
		send(out,packet);
	}
	
	public static void GAME_SEND_OAKO_PACKET(final Player out, final Item obj)
	{
		final String packet = "OAKO"+obj.parseItem();
		send(out,packet);
	}
	
	public static void GAME_SEND_OCO_PACKET(final Player out, final Item obj) {
		final String packet = "OCO" + obj.parseItem();
		send(out, packet);
	}

	public static void GAME_SEND_ESK_PACKEt(final Player out)
	{
		final String packet = "ESK";
		send(out,packet);
	}

	public static void GAME_SEND_REMOVE_ITEM_PACKET(final Player out, final int guid)
	{
		final String packet = "OR"+guid;
		send(out,packet);
	}

	public static void GAME_SEND_DELETE_OBJECT_FAILED_PACKET(final PrintWriter out)
	{
		final String packet = "OdE";
		send(out,packet);
	}

	public static void GAME_SEND_OBJET_MOVE_PACKET(final Player out,final Item obj)
	{
		String packet = "OM"+obj.getGuid()+'|';
		if(obj.getPosition() != Constants.ITEM_POS_NO_EQUIPED)
			packet += obj.getPosition();
		
		send(out,packet);
	}

	public static void GAME_SEND_EMOTICONE_TO_FIGHT(final Fight fight, final int teams, final int guid, final int id)
	{
		final String packet = "cS"+guid+'|'+id;;
		for(final Fighter f : fight.getFighters(teams))
		{
			if(f.hasLeft())continue;
			if(f.getPlayer() == null || !f.getPlayer().isOnline())continue;
				send(f.getPlayer(),packet);
		}
	}

	public static void GAME_SEND_OAEL_PACKET(final PrintWriter out)
	{
		final String packet = "OAEL";
		send(out,packet);
	}

	public static void GAME_SEND_NEW_LVL_PACKET(final PrintWriter out, final int lvl)
	{
		final String packet = "AN"+lvl;
		send(out,packet);
	}

	public static void GAME_SEND_MESSAGE_TO_ALL(final String msg,final String color)
	{
		final String packet = "cs<font color='#"+color+"'>"+msg+"</font>";
		for(final Player P : World.getOnlinePersos())
		{
			send(P,packet);
		}
	}

	public static void GAME_SEND_EXCHANGE_REQUEST_OK(final PrintWriter out,	final int guid, final int guidT, final int msgID)
	{
		final String packet = "ERK"+guid+'|'+guidT+'|'+msgID;
		send(out,packet);
	}

	public static void GAME_SEND_EXCHANGE_REQUEST_ERROR(final PrintWriter out, final char c)
	{
		final String packet = "ERE"+c;
		send(out,packet);
	}

	public static void GAME_SEND_EXCHANGE_CONFIRM_OK(final PrintWriter out, final int type)
	{
		final String packet = "ECK"+type;
		send(out,packet);
	}
	
	public static void GAME_SEND_EXCHANGE_MOVE_OK(final Player out,final char type,final String signe,final String s1)
	{
		String packet = "EMK"+type+signe;
		if(!s1.isEmpty())
			packet += s1;
		send(out,packet);
	}

	public static void GAME_SEND_EXCHANGE_OTHER_MOVE_OK(final PrintWriter out,final char type,final String signe,final String s1)
	{
		String packet = "EmK"+type+signe;
		if(!s1.isEmpty())
			packet += s1;
		send(out,packet);
	}

	public static void GAME_SEND_EXCHANGE_OK(final PrintWriter out,final boolean ok, final int guid)
	{
		final String packet = "EK"+(ok?'1':'0')+guid;
		send(out,packet);
	}
	
	public static void GAME_SEND_EXCHANGE_VALID(final PrintWriter out, final char c)
	{
		final String packet = "EV"+c;
		send(out,packet);
	}

	public static void GAME_SEND_PARTY_INVITATION_ERROR(final PrintWriter out, final String s) {
		final String packet = "PIE"+s;
		send(out,packet);
	}

	public static void GAME_SEND_PARTY_INVITATION(final PrintWriter out,final String n1, final String n2)
	{
		final String packet = "PIK"+n1+'|'+n2;
		send(out,packet);
	}

	public static void GAME_SEND_PARTY_CREATE(final PrintWriter out, final Party g)
	{
		final String packet = "PCK"+g.getChief().getName();
		send(out,packet);
	}

	public static void GAME_SEND_PL_PACKET(final PrintWriter out, final Party g)
	{
		final String packet = "PL"+g.getChief().getActorId();
		send(out,packet);
	}
	
	public static void GAME_SEND_PR_PACKET(final Player out)
	{
		final String packet = "PR";
		send(out,packet);
	}

	public static void GAME_SEND_PV_PACKET(final PrintWriter out,final String s)
	{
		final String packet = "PV"+s;
		send(out,packet);
	}
	
	public static void GAME_SEND_ALL_PM_ADD_PACKET(final PrintWriter out,final Party g)
	{
		final StringBuilder packet = new StringBuilder(3 + 15 * g.getMembers().size());
		packet.append("PM+");
		boolean first = true;
		for(final Player p : g.getMembers())
		{
			if(!first) packet.append('|');
			packet.append(p.parseToPM());
			first = false;
		}
		send(out,packet.toString());
	}
	
	public static void GAME_SEND_PM_ADD_PACKET_TO_PARTY(final Party g, final Player p)
	{
		final String packet = "PM+"+p.parseToPM();
		for(final Player P : g.getMembers())send(P,packet);
	}
	
	public static void GAME_SEND_PM_MOD_PACKET_TO_PARTY(final Party g,final Player p)
	{
		final String packet = "PM~"+p.parseToPM();
		for(final Player P : g.getMembers())send(P,packet);
	}

	public static void GAME_SEND_PM_DEL_PACKET_TO_PARTY(final Party g, final int guid)
	{
		final String packet = "PM-"+guid;
		for(final Player P : g.getMembers())send(P,packet);
	}

	public static void GAME_SEND_cMK_PACKET_TO_PARTY(final Party g,final String s, final int guid, final String name, final String msg)
	{
		final String packet = "cMK"+s+'|'+guid+'|'+name+'|'+msg+'|';
		for(final Player P : g.getMembers())send(P,packet);
	}

	public static void GAME_SEND_FIGHT_DETAILS(final PrintWriter out, final Fight fight)
	{
		if(fight == null)return;
		String packet = "fD"+fight.getId()+'|';
		for(final Fighter f : fight.getFighters(1))packet += f.getPacketsName()+"~"+f.getLvl()+';';
		packet += '|';
		for(final Fighter f : fight.getFighters(2))packet += f.getPacketsName()+"~"+f.getLvl()+';';
		send(out,packet);
	}

	public static void GAME_SEND_IQ_PACKET(final Player perso, final int guid,	final int qua)
	{
		final String packet = "IQ"+guid+'|'+qua;
		send(perso,packet);
	}
	public static void GAME_SEND_JN_PACKET(final Player perso, final int jobID,	final int lvl)
	{
		final String packet = "JN"+jobID+'|'+lvl;
		send(perso,packet);
	}
	public static void GAME_SEND_GDF_PACKET_TO_MAP(final DofusMap map, final DofusCell cell)
	{
		final int cellID = cell.getId();
		final InteractiveObject object = cell.getObject();
		final String packet = "GDF|"+cellID+';'+object.getState()+';'+(object.isInteractive()?'1':'0');
		for(final Player perso : map.getPersos())send(perso,packet);
	}
	
	public static void GAME_SEND_GA_PACKET_TO_MAP(final DofusMap map, final String gameActionID, final int actionID,final String s1, final String s2)
	{
		String packet = "GA"+gameActionID+';'+actionID+';'+s1;
		if(!s2.isEmpty())packet += ';'+s2;
		
		for(final Player perso : map.getPersos())send(perso,packet);
	}

	public static void GAME_SEND_EL_BANK_PACKET(final Player perso)
	{
		final String packet = "EL"+perso.parseBankPacket();
		send(perso,packet);
	}

	public static void GAME_SEND_JX_PACKET(final Player perso,final ArrayList<JobStat> SMs)
	{
		final StringBuilder packet = new StringBuilder(15*SMs.size()).append("JX");
		for(final JobStat sm : SMs)
		{
			packet.append('|').append(sm.getTemplate().getId());
			packet.append(';').append(sm.getLvl()).append(';');
			packet.append(sm.getXpString(";")).append(';');
		}
		send(perso,packet.toString());
	}
	public static void GAME_SEND_JO_PACKET(final Player perso,final List<JobStat> SMs)
	{
		final StringBuilder packet = new StringBuilder(10*SMs.size());
		for(final JobStat sm : SMs)
		{
			packet.append("JO").append(sm.getID()).append('|');
			packet.append(sm.getOptBinValue()).append('|').append('2');
			packet.append((char)0x00);
		}
		send(perso,packet.toString());
	}
	public static void GAME_SEND_JS_PACKET(final Player perso,final List<JobStat> SMs)
	{
		final StringBuilder packet = new StringBuilder(10*SMs.size()).append("JS");
		for(final JobStat sm : SMs)
		{
			packet.append(sm.parseJS());
		}
		send(perso,packet.toString());
	}
	public static void GAME_SEND_JR_PACKET(final Player perso, final int id)
	{
		String packet = "JR";
		for(final JobStat sm : perso.getMetiers().values()) 
		{
			if(sm != null && sm.getTemplate().getId() == id) 
			{
				packet += id;
			}	
		}
		send(perso,packet);
	}
	public static void GAME_SEND_EsK_PACKET(final Player perso, final String str)
	{
		final String packet = "EsK"+str;
		send(perso,packet);
	}

	public static void GAME_SEND_FIGHT_SHOW_CASE(final ArrayList<PrintWriter> PWs, final int guid, final int cellID)
	{
		final String packet = "Gf"+guid+'|'+cellID;;
		for(final PrintWriter PW : PWs)
		{
			if(PW == null)
				continue;
			send(PW,packet);
		}
	}

	public static void GAME_SEND_Ea_PACKET(final Player perso, final String str)
	{
		final String packet = "Ea"+str;
		send(perso,packet);
	}
	public static void GAME_SEND_EA_PACKET(final Player perso, final String str)
	{
		final String packet = "EA"+str;
		send(perso,packet);
	}
	public static void GAME_SEND_Ec_PACKET(final Player perso, final String str)
	{
		final String packet = "Ec"+str;
		send(perso,packet);
	}
	public static void GAME_SEND_Em_PACKET(final Player perso, final String str)
	{
		final String packet = "Em"+str;
		send(perso,packet);
	}
	public static void GAME_SEND_IO_PACKET_TO_MAP(final DofusMap map,final int guid,final String str)
	{
		final String packet = "IO"+guid+'|'+str;
		for(final Player perso : map.getPersos())
			send(perso,packet);
	}

	public static void GAME_SEND_FRIENDLIST_PACKET(final Player perso)
	{
		final String packet = "FL"+perso.getAccount().parseFriendList();
		send(perso,packet);
	}

	public static void GAME_SEND_FA_PACKET(final Player perso, final String str)
	{
		final String packet = "FA"+str;
		send(perso,packet);
	}
	public static void GAME_SEND_FD_PACKET(final Player perso, final String str)
	{
		final String packet = "FD"+str;
		send(perso,packet);
	}
	public static void GAME_SEND_ADD_ENEMY(final Player out, final Player pr)
	{	
		final String packet = "iAK"+pr.getAccount().getName()+";2;"+pr.getName()+";36;10;0;100.FL.";
		send(out, packet);
	}
	
	public static void GAME_SEND_ENEMY_LIST(final Player perso)
	{
		
		final String packet = "iL"+perso.getAccount().parseEnemyList();
		send(perso, packet);
	}
	
	public static void GAME_SEND_iAEA_PACKET(final Player out)
	{
		
		final String packet = "iAEA.";
		send(out, packet);
	}
	
	public static void GAME_SEND_iD_COMMANDE(final Player perso, final String str)
	{
		final String packet = "iD"+str;
		send(perso, packet);
	}
	public static void GAME_SEND_Rp_PACKET(final Player perso, final MountPark MP)
	{
		if(MP == null)return;
		String packet = "Rp"+MP.getOwner()+';'+MP.getPrice()+';'+MP.getSize()+';'+MP.getObjectNumb()+';';
		final Guild G = MP.getGuild();
		//Si une guilde est definie
		if(G != null)
		{
			packet += G.getName()+';'+G.getEmblem();
		}else packet += ';';
		send(perso,packet);
	}
	public static void GAME_SEND_OS_PACKET(final Player perso, final int pano)
	{
		String packet = "OS";
		final int num = perso.getNumbEquipedItemOfSet(pano);
		if(num <= 0) packet += '-'+pano;
		else
		{
			packet += '+'+pano+'|';
			final ItemSet IS = World.getItemSet(pano);
			if(IS != null)
			{
				String items = "";
				//Pour chaque objet de la pano
				for(final ItemTemplate OT : IS.getItemTemplates())
				{
					//Si le joueur l'a équipé
					if(perso.hasEquiped(OT.getID()))
					{
						//On l'ajoute au packet
						if(items.length() >0)items+=';';
						items += OT.getID();
					}
				}
				packet += items+'|'+IS.getBonusStatByItemNumb(num).parseToItemSetStats();
			}
		}	
		send(perso,packet);
	}

	public static void GAME_SEND_MOUNT_DESCRIPTION_PACKET(final Player perso,final Mount DD)
	{
		final String packet = "Rd"+DD.parseInfos();
		send(perso,packet);
	}

	public static void GAME_SEND_Rr_PACKET(final Player perso, final String str)
	{
		final String packet = "Rr"+str;
		send(perso,packet);
	}
	
	public static void GAME_SEND_ITEM_LIST_MOUNT_PACKET(final PrintWriter _out, final Mount dinde) {
		final String packet = "EL"+dinde.getItemsPacket();
		send(_out,packet);
	}
	
	public static void GAME_SEND_ITEM_LIST_TAXCOLLECTOR_PACKET(final PrintWriter _out, final TaxCollector perco) {
		final String packet = "EL"+perco.getItemPercepteurList();
		send(_out,packet);
	}
	
	public static void GAME_SEND_MOUNT_PODS(final PrintWriter _out, final Mount dinde) {
		final String packet = "Ew"+dinde.getUsedPods()+';'+dinde.getTotalPods();
		send(_out,packet);
	}
	
	public static void GAME_SEND_MOUNT_PODS(final Player _out, final Mount dinde) {
		final String packet = "Ew"+dinde.getUsedPods()+';'+dinde.getTotalPods();
		send(_out,packet);
	}

	public static void GAME_SEND_ALTER_GM_PACKET(final DofusMap map,	final Player perso)
	{
		perso.clearCacheGMStuff();
		perso.clearCacheGM();
		final String packet = "GM|~"+perso.parseToGM();
		for(final Player z:map.getPersos())
			send(z,packet);
	}

	public static void GAME_SEND_Ee_PACKET(final Player perso, final char c,final String s)
	{
		final String packet = "Ee"+c+s;
		send(perso,packet);
	}
	public static void GAME_SEND_cC_PACKET(final Player perso, final char c,final String s)
	{
		final String packet = "cC"+c+s;
		send(perso,packet);
	}
	public static void GAME_SEND_ADD_NPC_TO_MAP(final DofusMap map, final Npc npc)
	{
		final String packet = "GM|"+npc.parseGM();
		for(final Player perso : map.getPersos())
			send(perso,packet);
	}
	
	public static void GAME_SEND_GDO_PACKET_TO_MAP(final DofusMap map, final char c,final int cell, final int itm, final int i, final int unk)
	{
		final String packet = "GDO"+c+cell+';'+itm+';'+i+';'+unk;
		for(final Player perso : map.getPersos())
			send(perso,packet);
	}

	public static void GAME_SEND_GDO_PACKET_TO_MAP(final DofusMap map, final char c,final int cell, final int itm, final int i)
	{
		final String packet = "GDO"+c+cell+';'+itm+';'+i;
		for(final Player perso : map.getPersos())
			send(perso,packet);
	}
	public static void GAME_SEND_GDO_PACKET(final Player p, final char c,final int cell, final int itm, final int i)
	{
		final String packet = "GDO"+c+cell+';'+itm+';'+i;
		send(p,packet);
	}
	
	public static void GAME_SEND_ZC_PACKET(final Player p,final int a)
	{
		final String packet = "ZC"+a;
		send(p,packet);
	}
	public static void GAME_SEND_GIP_PACKET(final Player p,final int a)
	{
		final String packet = "GIP"+a;
		send(p,packet);
	}
	
	public static void GAME_SEND_gIB_PACKET(final Player p)
	{
		final Guild guild = p.getGuild();
		if(guild != null) {
			final String packet = guild.parseStatsTaxCollectorToGuild();
			send(p,packet);
		}
	}
	public static void GAME_SEND_gn_PACKET(final Player p)
	{
		final String packet = "gn";
		send(p,packet);
	}
	public static void GAME_SEND_gC_PACKET(final Player p, final String s)
	{
		final String packet = "gC"+s;
		send(p,packet);
	}
	public static void GAME_SEND_gV_PACKET(final Player p)
	{
		final String packet = "gV";
		send(p,packet);
	}
	public static void GAME_SEND_gIM_PACKET(final Player p, final Guild g, final char c)
	{
		String packet = "gIM"+c;
		switch(c)
		{
			case '+':
				packet += g.parseMembersToGM();
			break;
		}
		send(p,packet);
	}
	public static void GAME_SEND_gS_PACKET(final Player p, final GuildMember gm)
	{
		final String packet = "gS"+gm.getGuild().getName()+'|'+gm.getGuild().getEmblem().replace(',', '|')+'|'+gm.parseRights();
		send(p,packet);
	}
	public static void GAME_SEND_gJ_PACKET(final Player p, final String str)
	{
		final String packet = "gJ"+str;
		send(p,packet);
	}
	public static void GAME_SEND_gK_PACKET(final Player p, final String str)
	{
		final String packet = "gK"+str;
		send(p,packet);
	}
	public static void GAME_SEND_gIG_PACKET(final Player p, final Guild g)
	{
		final long xpMin = World.getExpLevel(g.getLvl()).guilde;
		final long xpMax = World.getExpLevel(g.getLvl()>=Config.MAX_LEVEL?Config.MAX_LEVEL:g.getLvl()+1).guilde;
		final String packet = "gIG"+(g.getSize()>9?1:0)+'|'+g.getLvl()+'|'+xpMin+'|'+g.getXp()+'|'+xpMax;
		send(p,packet);
	}
	public static void GAME_SEND_M_PACKET(final PrintWriter out, final String id, final String args)
	{
		final String packet = "M"+id+'|'+args;
		send(out,packet);
	}

	public static void GAME_SEND_WC_PACKET(final Player perso)
	{
		final String packet = "WC"+perso.parseZaapList();
		send(perso.getAccount().getGameThread().getOut(),packet);
	}
	
	public static void GAME_SEND_Wc_PACKET(final Player perso)
	{
		final String packet = "Wc"+perso.parseZaapiList(perso.getCurMap().getSubArea().getArea().getId());
		send(perso.getAccount().getGameThread().getOut(),packet);
	}

	public static void GAME_SEND_WV_PACKET(final Player out)
	{
		final String packet = "WV";
		send(out,packet);
	}
	
	public static void GAME_SEND_Wv_PACKET(final Player out)
	{
		final String packet = "Wv";
		send(out,packet);
	}
	
	public static void GAME_SEND_WUE_PACKET(final Player out)
	{
		final String packet = "WUE";
		send(out,packet);
	}
	public static void GAME_SEND_EMOTE_LIST(final Player perso,final int emoteList, final String s1)
	{
		final String packet = "eL"+emoteList+'|'+s1;
		send(perso, packet);
	}
	
	public static void GAME_SEND_NO_EMOTE(final Player out)
	{
		final String packet = "eUE";
		send(out, packet);
	}

	public static void REALM_SEND_TOO_MANY_PLAYER_ERROR(final PrintWriter out)
	{
		final String packet = "AlEw";
		send(out, packet);
	}
	
	/*---Packet utiliser par l'HDV---*/
	public static void GAME_SEND_EHL_PACKET(final Player out, final int categ, final String templates)	//Packet de listage des templates dans une catégorie (En réponse au packet EHT)
	{
		final String packet = "EHL"+categ+'|'+templates;
		
		send(out,packet);
	}
	public static void GAME_SEND_EHL_PACKET(final Player out, final String items)	//Packet de listage des objets en vente
	{
		final String packet = "EHL"+items;
		
		send(out,packet);
	}
	
	public static void GAME_SEND_EHP_PACKET(final Player out, final int templateID)	//Packet d'envoie du prix moyen du template (En réponse a un packet EHP)
	{
		
		final String packet = "EHP"+templateID+'|'+World.getItemTemplate(templateID).getAvgPrice();
		
		send(out,packet);
	}
	
	public static void GAME_SEND_EHl(final Player out, final BigStore seller,final int templateID)
	{
		final long time1 = System.currentTimeMillis();	//TIME
		final String packet = "EHl" + seller.parseToEHl(templateID);
		System.out.println (System.currentTimeMillis() - time1 + "pour lister les prix d'un template");	//TIME
		send(out,packet);
	}
	
	public static void GAME_SEND_EHm_PACKET(final Player out, final String sign,final String str)
	{
		final String packet = "EHm"+sign + str;
		send(out,packet);
	}
	
	public static void GAME_SEND_EHM_PACKET(final Player out, final String sign,final String str)
	{
		final String packet = "EHM"+sign + str;
		
		send(out,packet);
	}
	
	public static void GAME_SEND_HDVITEM_SELLING(final Player perso)
	{
		final StringBuilder packet = new StringBuilder();
		packet.append("EL");
		
		final BigStoreEntry[] entries = perso.getAccount().getHdvItems(Math.abs(perso.getIsTradingWith()));	//Récupère un tableau de tout les items que le personnage à en vente dans l'HDV où il est
		
		boolean isFirst = true;
		for(final BigStoreEntry curEntry : entries)
		{
			if(curEntry == null)
				break;
			if(!isFirst)
				packet.append('|');
			packet.append(curEntry.parseToEL());
			
		isFirst = false;
		}
		
		send(perso,packet.toString());
	}
	/*--------------------------------*/
	
	public static void GAME_SEND_FORGETSPELL_INTERFACE(final char sign,final Player perso)
	{
		final String packet = "SF"+sign;
		send(perso, packet);
	}

	public static void REALM_SEND_MESSAGE(final PrintWriter out, final String args) {
		final String packet = "M"+args;
		send(out,packet);
    }
	
	public static void REALM_SEND_MESSAGE(final Player out, final String args) {
		final String packet = "M"+args;
		send(out,packet);
    }

	public static void REALM_SEND_GIFT(final PrintWriter out, final String Gift) {
		final String packet = (new StringBuilder("Ag1|").append(Gift)).toString();
		send(out, packet);
    }

	public static void REALM_SEND_CLOSE_GIFTS_UI(final PrintWriter out) {
		final String packet = "AGK";
		send(out, packet);
    }

	public static void GAME_SEND_ALTER_FIGHTER_MOUNT(final Fight fight, final Fighter fighter, final int guid, final int team, final int otherteam) {
		final StringBuilder packet = new StringBuilder();
		fighter.getPlayer().clearCacheGMStuff();
		fighter.getPlayer().clearCacheGM();
		packet.append("GM|-").append(guid).append((char)0x00).append(fighter.getGmPacket('~'));
		for(final Fighter F : fight.getFighters(team))
		{
			if(F.getPlayer() == null || F.getPlayer().getAccount().getGameThread() == null || !F.getPlayer().isOnline())
				continue;
			send(F.getPlayer().getAccount().getGameThread().getOut(),packet.toString());
		}
		if(otherteam > -1)
	 	{
			for(final Fighter F : fight.getFighters(otherteam))
			{
				if(F.getPlayer() == null || F.getPlayer().getAccount().getGameThread() == null || !F.getPlayer().isOnline())
					continue;
				send(F.getPlayer().getAccount().getGameThread().getOut(),packet.toString());
			}
	 	}
    }

	public static void GAME_SEND_GUILD_INFO_BOOST_PACKET(final Player perso, final String infos) {
	  	final String packet = "gIB"+infos;
	    send(perso, packet);
    }

	public static void GAME_SEND_GUILD_PERCEPTEUR_INFOS_PACKET(final Player perso, final String infos) {
		final String packet = "gT"+infos;
	    send(perso, packet);
    }

	public static void GAME_SEND_GUILD_PERCEPTEUR_MOVEMENT_PACKET(final Player perso, final String infos) {
		final String packet = "gITM"+infos;
	    send(perso, packet);
    }
	
	public static void GAME_SEND_ATTACK_PERCEPTEUR(final Player attacker, final int percepteurId) {
		 final StringBuilder packet = new StringBuilder();
		 packet.append("gITp");
		 final TaxCollector percepteur = World.getTaxCollector(percepteurId);
		 packet.append(percepteur.getActorId());
		 for(final Player curAttacker : percepteur.getAttackers())
		 {
			 packet.append('|');
			 packet.append(Integer.toString(curAttacker.getActorId(), 36)).append(';');
			 packet.append(curAttacker.getName()).append(';');
			 packet.append(curAttacker.getLvl()).append(';');
			 packet.append("0;"); //FIXME : ??
		 }
		 send(attacker, packet.toString());
	}

	public static void GAME_SEND_DEFENSE_PERCEPTEUR(final Player member, final int percepteurId) {
	    final StringBuilder packet = new StringBuilder();
	    packet.append("gITP");
	    final TaxCollector percepteur = World.getTaxCollector(percepteurId);
	    packet.append(percepteur.getActorId());
	    for(final Player curDefender : percepteur.getDefenders())
	    {
	    	packet.append('|');
			packet.append(Integer.toString(curDefender.getActorId(), 36)).append(';');
			packet.append(curDefender.getName()).append(';');
			packet.append(curDefender.getGfxID()).append(';');
			packet.append(curDefender.getLvl()).append(';');
			packet.append(Integer.toString(curDefender.getColor1(), 36)).append(';');
			packet.append(Integer.toString(curDefender.getColor2(), 36)).append(';');
			packet.append(Integer.toString(curDefender.getColor3(), 36)).append(';');
			packet.append("0;"); //FIXME : ??
	    }
	    send(member, packet.toString());
    }

	public static void GAME_SEND_MAP_PERCEPTEUR_GM_PACKET(final PrintWriter out, final DofusMap map) {
	    if(map.getPercepteur() != null) { 
	    	final String packet = map.getPercepteur().parseGM();
	    	send(out, packet);
	    }
    }
	
	public static void GAME_SEND_MAP_PERCEPTEUR_GM_PACKET(final DofusMap map) {
		final TaxCollector taxCollector = map.getPercepteur();
	    if(taxCollector != null) { 
	    	final String packet = taxCollector.parseGM();
	    	for(final Player player : map.getPersos())
	    	{
	    		if(player != null && player.isOnline())
	    		{
	    			send(player, "GM|-"+taxCollector.getActorId());
	    			send(player, packet);
	    		}
	    	}
	    }
    }

	public static void GAME_SEND_MAP_OBJECT_DROPPED_PACKETS(final Player perso, final DofusMap map) 
	{
		for(final DofusCell c : map.getCases().values())
		{
			if(c.getDroppedItem() != null)
			SocketManager.GAME_SEND_GDO_PACKET(perso,'+',c.getId(),c.getDroppedItem().getTemplate().getID(),0);
		}
    }

	public static void GAME_SEND_INFO_HIGHLIGHT_PACKET(final Player perso, final String args) {
	    final String packet = "IH"+args;
		send(perso, packet);
    }

	public static void GAME_SEND_GAME_OVER_PACKET(final Player perso) {
	    final String packet = "GO";
	    send(perso, packet);
    }

	public static void GAME_SEND_MESSAGE_SERVER(final Player perso, final String id) {
		final String packet = "M"+id;
		send(perso, packet);
    }
	
	public static void GAME_SEND_MESSAGE_SERVER(final Player perso, final String id, final String arg) {
		final String packet = "M"+id+'|'+arg;
		send(perso, packet);
    }

	public static void GAME_SEND_INFOS_COMPASS_PACKET(final Player perso, final String args) {
		final String packet = "IC"+args;
		send(perso, packet);
    }
	
	public static void GAME_SEND_INFOS_COMPASS_PACKET(final Player perso, final DofusMap map) {
		final String packet = "IC"+map.getX()+'|'+map.getY()+"|0";
		send(perso, packet);
    }
	
	public static void GAME_SEND_INFOS_COMPASS_PACKET(final Player perso, final Player target) {
		SocketManager.GAME_SEND_INFOS_COMPASS_PACKET(perso, target.getCurMap());
    }

	public static void GAME_SEND_QUEST_LIST_PACKET(final Player player) {
		final StringBuilder packet = new StringBuilder();
		packet.append("QL1");
		boolean isFirst = true;
		int sortOrder = 0;
		for(final Quest quest : player.getQuestList())
		{
			if(!isFirst) 
				packet.append('|');
			sortOrder++;
			if(!quest.isFinished())
				packet.append(quest.getId()).append(";0;").append(sortOrder);
			else
				packet.append(quest.getId()).append(";1;").append(sortOrder);
			isFirst = false;
		}
		send(player, packet.toString());
	}

	public static void GAME_SEND_QUEST_STEP_PACKET(final Player player, final String preparePacket) {
		final StringBuilder packet = new StringBuilder();
		packet.append("QS");
		boolean isFirst = true;
		final Quest quest = player.getQuest(Integer.parseInt(preparePacket));
		if(quest == null)
			return;
		if(quest.getCurStep().getId() <= 0)
			return;
		packet.append(quest.getId()).append('|');
		packet.append(quest.getCurStep()).append('|');
		for(final QuestObjective objective : quest.getSteps().get(quest.getCurStep().getId()).getObjectives())
		{
			if(!isFirst) 
				packet.append(';');
			packet.append(objective.getId()).append(',');
			packet.append(objective.isFinished()?1:0).append(';');
			isFirst = false;
		}
		packet.append('|');
		packet.append(player.getStepsFinished(quest)).append('|');
		packet.append(player.getStepsToRealise(quest)).append('|');
		packet.append(quest.getCurStep().getDialogId());
		send(player, packet.toString());
    }

	public static void GAME_SEND_Im_PACKET_TO_ALL(final String str) {
		final String packet = "Im"+str; 
		for(final Player perso : World.getOnlinePersos())
			send(perso,packet);
    }

	public static void SEND_CLOSE_QUEUE(final PrintWriter out) {
		final String packet = "Arf";
		send(out, packet);
    }

	public static void GAME_SEND_ON_EQUIP_ITEM_FIGHT(final Player _perso, final Fighter f, final Fight F) {
		final String packet = _perso.parseToOa();
		for(final Fighter z : F.getFighters(7)) {
			if (z.getPlayer() != null)
				send(z.getPlayer(), packet);
		}
    }

	public static void GAME_SEND_IM_MESSAGE_RED(final Player player, final String str) {
		final String packet = "Im116;"+str;
		send(player, packet);
    }

	public static void GAME_SEND_ITEM_LIST_PACKET_SELLER(final Player p, final Player out)
	{
		final String packet = "EL"+p.parseStoreItemsList();
		send(out,packet);
	}

	public static void GAME_SEND_ASK_OFFLINE_EXCHANGE_PACKET(final Player perso, final double per, final long kamas) {
		final String packet = "Eq|"+per+'|'+kamas;
		send(perso,packet);
    }

	public static void GAME_SEND_MERCHANT_LIST(final Player P, final int mapId) {
		final StringBuilder packet = new StringBuilder();
    	packet.append("GM|~");
    	if(World.getSeller(mapId) == null) return;
        for (final Integer pID : World.getSeller(mapId)) 
        {
        	if(!World.getPlayer(pID).isOnline() && World.getPlayer(pID).isShowSeller())
        	{
        		packet.append(World.getPlayer(pID).parseToMerchantGM()).append('|');
            }
        }
        if(packet.length() < 5) return;
        send(P, packet.toString());
    }
	
	public static void GAME_SEND_GUILD_ON_PERCEPTEUR_ATTACKED(final TaxCollector percepteur)
	{
		final DofusMap map = World.getMap(percepteur.getMapId());
		final String packet = "gAA" + percepteur.getName1() + ',' + percepteur.getName2() + "|-1|" + map.getX() + '|' + map.getY();
		final Guild guild = percepteur.getGuild();
		for(final Player z : guild.getMembers())
    	{
    		if(z == null || !z.isOnline()) continue;
    		SocketManager.GAME_SEND_GUILD_PERCEPTEUR_MOVEMENT_PACKET(z, guild.parseInfosPerco());
			send(z, packet);
    	}
	}

	public static void GAME_SEND_GUILD_ON_PERCEPTEUR_SURVIVED(final TaxCollector percepteur)
	{
		final DofusMap map = World.getMap(percepteur.getMapId());
		final String packet = "gAS" + percepteur.getName1() + ',' + percepteur.getName2() + "|-1|" + map.getX() + '|' + map.getY();
		final Guild guild = percepteur.getGuild();
		for(final Player z : guild.getMembers())
    	{
    		if(z == null || !z.isOnline()) continue;
    		SocketManager.GAME_SEND_GUILD_PERCEPTEUR_MOVEMENT_PACKET(z, guild.parseInfosPerco());
			send(z, packet);
    	}
	}
	
	public static void GAME_SEND_GUILD_ON_PERCEPTEUR_DIED(final TaxCollector percepteur)
	{
		final DofusMap map = World.getMap(percepteur.getMapId());
		final String packet = "gAD" + percepteur.getName1() + ',' + percepteur.getName2() + "|-1|" + map.getX() + '|' + map.getY();
		final Guild guild = percepteur.getGuild();
		for(final Player z : guild.getMembers())
    	{
    		if(z == null || !z.isOnline()) continue;
    		SocketManager.GAME_SEND_GUILD_PERCEPTEUR_MOVEMENT_PACKET(z, guild.parseInfosPerco());
			send(z, packet);
    	}
	}

	public static void GAME_SEND_GENERATE_NAME(final PrintWriter _out) {
	    final String packet = "APK"+Utils.randomPseudo();
	    /*StringBuilder name = new StringBuilder();
        char[] VOWELS = {'a', 'e', 'i', 'o', 'u', 'y'};
        char[] CONSONANTS = {'b','c','d','f','g','h','j','k','l','m','n','p','q','r','s','t','v','w','x','z'};
	    Random random = new Random();
        int length = Formulas.getRandomValue(4, 7);
        boolean flag = Formulas.getRandomValue(0, 2) == 1;
        for (int i = 0; i < length; ++i)
        {
            name.append(flag ? VOWELS[random.nextInt(VOWELS.length)] : CONSONANTS[random.nextInt(CONSONANTS.length)]);
            flag = !flag;
        }
        packet += name.substring(0, 1).toUpperCase();*/
	    
        send(_out, packet);
    }

	public static void GAME_SEND_MOB_EMOTE(final int guid, final ArrayList<Fighter> fighters, final boolean loseMember) {
		final int[] loseEmotes = {3, 7, 8, 12};
		final int[] winEmotes = {1, 4, 6, 11, 14, 15};
		int emoteToSend = 0;
		if(loseMember)
			emoteToSend = loseEmotes[Formulas.getRandomValue(0, loseEmotes.length-1)];
		else
			emoteToSend = winEmotes[Formulas.getRandomValue(0, winEmotes.length-1)];
		final String packet = "cS"+guid+'|'+emoteToSend;
		for(final Fighter fighter : fighters)
		{
			final Player player = fighter.getPlayer();
			if(player != null)
				send(player, packet);
		}
	}

	public static void GAME_SEND_TUTORIAL_ON_GAME_BEGIN(final Player perso) {
		final String packet = "TB";
		send(perso, packet);
	}

	public static void GAME_SEND_CONQUEST_ON_INFO_JOIN(final Player player, final String infos) {
		final String packet = "CIJ"+infos;
		send(player, packet);
	}

	public static void GAME_SEND_CONQUEST_ON_BALANCE(final Player player, final float balance1, final float balance2) {
		final String packet  = "Cb"+balance1+';'+balance2;
		send(player, packet);
	}

	public static void GAME_SEND_MAP_PRISM_GM_PACKET(final DofusMap map) {
		if(map == null || map.getPrism() == null)
			return;
		final String packet = map.getPrism().parseGM();
		for(final Player player : map.getPersos())
		{
			if(player != null && player.isOnline())
				send(player, packet);
		}
	}

	public static void GAME_SEND_SUBWAY_PRISM_CREATE(final Player player, final String data) {
		final String packet = "Wp"+data;
		send(player, packet);
	}

	public static void GAME_SEND_EMOTE_TO_MAP(final DofusMap map, final int guid, final int emoteId) {
		final String packet = "eUK"+guid+'|'+emoteId+"|360000";
		for(final Player player : map.getPersos())
			send(player, packet);
	}

	public static void GAME_SEND_SUBWAY_PRISM_EXIT(final Player player) 
	{
		final String packet = "Ww";
		send(player, packet);
	}
	
	public static void GAME_SEND_Wp_PACKET(final Player perso, final int mapId)
	{
		final String packet = "Wp"+mapId;
		send(perso, packet);
	}
	
	public static void GAME_SEND_ANIMATION(final Player player, final int animationId)
	{
		String packet = "GA;2;"+player.getActorId()+";"+animationId;
		send(player, packet);
	}

	public static void GAME_SEND_MAP_FIGHT_FLAG(PrintWriter output, final DofusMap map) 
	{
		if(output == null) {
			for(Player player : map.getPersos()) {
				output = player.getAccount().getGameThread().getOut();
				for(final Fight fight : map.getFights().values())
				{
					if(fight.getState() == Constants.FIGHT_STATE_PLACE)
					{
						if(fight.getType() == Constants.FIGHT_TYPE_CHALLENGE)
						{
							SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET(output, fight.getInit0().getPlayer().getCurMap(),0,fight.getInit0().getGUID(),fight.getInit1().getGUID(),fight.getInit0().getPlayer().getCurCell().getId(),"0;-1", fight.getInit1().getPlayer().getCurCell().getId(), "0;-1");
							SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit0().getPlayer().getCurMap(),fight.getInit0().getGUID(), fight.getInit0());
							SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit1().getPlayer().getCurMap(),fight.getInit1().getGUID(), fight.getInit1());
						}else if(fight.getType() == Constants.FIGHT_TYPE_AGRESSION)
						{			
							SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET(output, fight.getInit0().getPlayer().getCurMap(),0,fight.getInit0().getGUID(),fight.getInit1().getGUID(),fight.getInit0().getPlayer().getCurCell().getId(),"0;"+fight.getInit0().getPlayer().getAlign(), fight.getInit1().getPlayer().getCurCell().getId(), "0;"+fight.getInit1().getPlayer().getAlign());
							SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit0().getPlayer().getCurMap(),fight.getInit0().getGUID(), fight.getInit0());
							SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit1().getPlayer().getCurMap(),fight.getInit1().getGUID(), fight.getInit1());
						}else if(fight.getType() == Constants.FIGHT_TYPE_PVM)
						{
							SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET(output, fight.getInit0().getPlayer().getCurMap(),4,fight.getInit0().getGUID(),fight.getMobGroup().getActorId(),(fight.getInit0().getPlayer().getCurCell().getId()+1),"0;-1",fight.getMobGroup().getCellId(),"1;-1");
							SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit0().getPlayer().getCurMap(),fight.getInit0().getGUID(), fight.getInit0());
							for(final Entry<Integer, Fighter> F : fight.getTeam1().entrySet())
							{
								SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getMap(),fight.getMobGroup().getActorId(), F.getValue());
							}
						}else if(fight.getType() == Constants.FIGHT_TYPE_PVT)
						{
							SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET(output, fight.getInit0().getPlayer().getCurMap(),5,fight.getInit0().getGUID(),fight.getTaxCollector().getActorId(),(fight.getInit0().getPlayer().getCurCell().getId()+1),"0;-1",fight.getTaxCollector().getCellId(),"3;-1");
							SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit0().getPlayer().getCurMap(),fight.getInit0().getGUID(), fight.getInit0());
							for(final Entry<Integer, Fighter> F : fight.getTeam1().entrySet())
							{
								SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getMap(),fight.getTaxCollector().getActorId(), F.getValue());
							}
						}/*else if(fight.getType() == Constants.FIGHT_TYPE_PVMA)
						{																												  //0 => 2?
							SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET(output, fight.getInit0().getPlayer().getCurMap(),0,fight.getInit0().getGUID(),fight._prism.getGUID(),(fight.getInit0().getPlayer().get_curCell().getID()+1),"0;"+fight.getInit0().getPlayer().get_align(),fight._prism.getCellID(),"0;"+fight._prism.getFaction());//_prism.getCellID() => getGUID() ?
							SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit0().getPlayer().getCurMap(),fight.getInit0().getGUID(), fight.getInit0());
							for(Entry<Integer, Fighter> F : fight.getTeam1().entrySet())
							{
								SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getMap(),fight._prism.getGUID(), F.getValue());
							}
						}*/
					}
				}
			}
		} else {
			for(final Fight fight : map.getFights().values())
			{
				if(fight.getState() == Constants.FIGHT_STATE_PLACE)
				{
					if(fight.getType() == Constants.FIGHT_TYPE_CHALLENGE)
					{
						SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET(output, fight.getInit0().getPlayer().getCurMap(),0,fight.getInit0().getGUID(),fight.getInit1().getGUID(),fight.getInit0().getPlayer().getCurCell().getId(),"0;-1", fight.getInit1().getPlayer().getCurCell().getId(), "0;-1");
						SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit0().getPlayer().getCurMap(),fight.getInit0().getGUID(), fight.getInit0());
						SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit1().getPlayer().getCurMap(),fight.getInit1().getGUID(), fight.getInit1());
					}else if(fight.getType() == Constants.FIGHT_TYPE_AGRESSION)
					{			
						SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET(output, fight.getInit0().getPlayer().getCurMap(),0,fight.getInit0().getGUID(),fight.getInit1().getGUID(),fight.getInit0().getPlayer().getCurCell().getId(),"0;"+fight.getInit0().getPlayer().getAlign(), fight.getInit1().getPlayer().getCurCell().getId(), "0;"+fight.getInit1().getPlayer().getAlign());
						SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit0().getPlayer().getCurMap(),fight.getInit0().getGUID(), fight.getInit0());
						SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit1().getPlayer().getCurMap(),fight.getInit1().getGUID(), fight.getInit1());
					}else if(fight.getType() == Constants.FIGHT_TYPE_PVM)
					{
						SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET(output, fight.getInit0().getPlayer().getCurMap(),4,fight.getInit0().getGUID(),fight.getMobGroup().getActorId(),(fight.getInit0().getPlayer().getCurCell().getId()+1),"0;-1",fight.getMobGroup().getCellId(),"1;-1");
						SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit0().getPlayer().getCurMap(),fight.getInit0().getGUID(), fight.getInit0());
						for(final Entry<Integer, Fighter> F : fight.getTeam1().entrySet())
						{
							SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getMap(),fight.getMobGroup().getActorId(), F.getValue());
						}
					}else if(fight.getType() == Constants.FIGHT_TYPE_PVT)
					{
						SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET(output, fight.getInit0().getPlayer().getCurMap(),5,fight.getInit0().getGUID(),fight.getTaxCollector().getActorId(),(fight.getInit0().getPlayer().getCurCell().getId()+1),"0;-1",fight.getTaxCollector().getCellId(),"3;-1");
						SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit0().getPlayer().getCurMap(),fight.getInit0().getGUID(), fight.getInit0());
						for(final Entry<Integer, Fighter> F : fight.getTeam1().entrySet())
						{
							SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getMap(),fight.getTaxCollector().getActorId(), F.getValue());
						}
					}/*else if(fight.getType() == Constants.FIGHT_TYPE_PVMA)
					{																												  //0 => 2?
						SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET(output, fight.getInit0().getPlayer().getCurMap(),0,fight.getInit0().getGUID(),fight._prism.getGUID(),(fight.getInit0().getPlayer().get_curCell().getID()+1),"0;"+fight.getInit0().getPlayer().get_align(),fight._prism.getCellID(),"0;"+fight._prism.getFaction());//_prism.getCellID() => getGUID() ?
						SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getInit0().getPlayer().getCurMap(),fight.getInit0().getGUID(), fight.getInit0());
						for(Entry<Integer, Fighter> F : fight.getTeam1().entrySet())
						{
							SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET(output, fight.getMap(),fight._prism.getGUID(), F.getValue());
						}
					}*/
				}
			}
		}
	}

	public static void GAME_SEND_SPOUSE_PACKET(final Player player) {
			final String packet = "FS"+player.getSpouse().parse();
			send(player, packet);
	}

	public static void GAME_SEND_CHALLENGE_FILLED(List<Fighter> fighters, int id) {
		String packet = "GdOK"+id;
		for(Fighter fighter : fighters) {
			if(fighter.getPlayer() != null) {
				send(fighter.getPlayer(), packet);
			}
		}
	}

	public static void GAME_SEND_CHALLENGE_FAILED(List<Fighter> fighters, int id, String failer) {
		String packet = "GdKO"+id;
		String infoMessage = "Im0188;"+failer;
		for(Fighter fighter : fighters) {
			if(fighter.getPlayer() != null) {
				send(fighter.getPlayer(), packet+(char)0x00+infoMessage);
			}
		}
	}

	public static void GAME_SEND_ERASE_FIGHTER_TO_FIGHT(Fight fight, int guid, int team) {
		String packet = "GM|-"+guid;
		for(Fighter f : fight.getFighters(7))
		{
			if(f.isPlayer() && f.getPlayer().isOnline() && f.getPlayer().getActorId() != guid)
			{
				send(f.getPlayer(), packet);
			}
		}
	}
	
}
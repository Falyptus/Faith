package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import objects.account.Account;
import objects.action.Action;
import objects.alignment.Prism;
import objects.bigstore.BigStore;
import objects.bigstore.BigStoreEntry;
import objects.character.Breed;
import objects.character.BreedSpell;
import objects.character.Mount;
import objects.character.Player;
import objects.guild.Guild;
import objects.guild.GuildMember;
import objects.guild.TaxCollector;
import objects.item.Gift;
import objects.item.Item;
import objects.item.ItemTemplate;
import objects.item.Pet;
import objects.item.Speaking;
import objects.job.Job;
import objects.map.DofusMap;
import objects.map.MountPark;
import objects.monster.Monster;
import objects.npc.NpcQuestion;
import objects.npc.NpcResponse;
import objects.npc.NpcTemplate;
import objects.quest.Quest;
import objects.quest.QuestObjective;
import objects.quest.QuestStep;
import objects.spell.SpellStat;
import objects.spell.Spell;

import com.mysql.jdbc.PreparedStatement;
import common.World.Area;
import common.World.Couple;
import common.World.Drop;
import common.World.IOTemplate;
import common.World.ItemSet;
import common.World.MonsterFollower;
import common.World.SubArea;
import common.console.Console;
import common.console.Log;

public class SQLManager {
	
	private static Connection serverConn;
	private static Connection commonConn;
	private static Connection loginConn;
	
	private static Timer timerCommit;
	private static boolean needCommit;
	
	private static Object playerSyncLock = new Object();
	private static Object syncLock = new Object();

	public synchronized static ResultSet executeQuery(final String query,final String DBNAME) throws SQLException
	{
		if(!Main.isInit)
			return null;
		
		Connection DB;
		if(DBNAME.equals(Config.SERVER_DB_NAME))
			DB = serverConn;
		else if(DBNAME.equals(Config.COMMON_DB_NAME))
			DB = commonConn;
		else
			DB = loginConn;
		
		final Statement stat = DB.createStatement();
		final ResultSet RS = stat.executeQuery(query);
		stat.setQueryTimeout(300);
		return RS;
	}

	public synchronized static PreparedStatement newTransact(final String baseQuery,final Connection dbCon) throws SQLException
	{
		final PreparedStatement toReturn = (PreparedStatement) dbCon.prepareStatement(baseQuery);
		
		needCommit = true;
		return toReturn;
	}
	public synchronized static void commitTransacts()
	{
		try
		{
			if(serverConn.isClosed() || commonConn.isClosed() || loginConn.isClosed())
			{
				closeCons();
				setUpConnexion();
			}
			
			commonConn.commit();
			serverConn.commit();
			loginConn.commit();
		}catch(final SQLException e)
		{
			Log.addToErrorLog("SQL ERROR:"+e.getMessage());
			e.printStackTrace();
			//rollBack(con); //Pas de rollBack, la BD sauvegarde ce qu'elle peut
		}
	}
	public synchronized static void rollBack(final Connection con)
	{
		try
		{
			con.rollback();
		}
		catch(final SQLException e)
		{
			System.out.println("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}
	public synchronized static void closeCons()
	{
		try
		{
			commitTransacts();
			
			serverConn.close();
			commonConn.close();
			loginConn.close();
		}catch (final Exception e)
		{
			System.out.println("Erreur à la fermeture des connexions SQL:"+e.getMessage());
			e.printStackTrace();
		}
	}
	public static final boolean setUpConnexion()
	{
		try
		{
			loginConn = DriverManager.getConnection("jdbc:mysql://"+Config.LOGIN_DB_HOST+"/"+Config.LOGIN_DB_NAME,Config.LOGIN_DB_USER,Config.LOGIN_DB_PASS);
			loginConn.setAutoCommit(false);
			
			serverConn = DriverManager.getConnection("jdbc:mysql://"+Config.SERVER_DB_HOST+"/"+Config.SERVER_DB_NAME,Config.SERVER_DB_USER,Config.SERVER_DB_PASS);
			serverConn.setAutoCommit(false);
			
			commonConn = DriverManager.getConnection("jdbc:mysql://"+Config.COMMON_DB_HOST+"/"+Config.COMMON_DB_NAME,Config.COMMON_DB_USER,Config.COMMON_DB_PASS);
			commonConn.setAutoCommit(false);
			
			if(!commonConn.isValid(10000) || !serverConn.isValid(10000))// || loginConn.isValid(10000))
			{
				Log.addToErrorLog("SQL ERROR : Connection to database invalid !");
				return false;
			}
			
			needCommit = false;
			TIMER(true);
			
			return true;
		}catch(final SQLException e)
		{
			System.out.println("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	public static void UPDATE_ACCOUNT_DATA(final Account acc)
	{
		synchronized(syncLock)
		{
			try
			{
				final String baseQuery = "UPDATE accounts SET " +
										"`bankKamas` = ?,"+
										"`bank` = ?,"+
										"`level` = ?,"+
										"`stable` = ?,"+
										"`banned` = ?,"+
										"`friends` = ?,"+
										"`enemies` = ?,"+
										"`seeFriend`= ?"+
										" WHERE `guid` = ?;";
				final PreparedStatement p = newTransact(baseQuery, loginConn);
				
				p.setLong(1, acc.getBankKamas());
				p.setString(2, acc.parseBankObjetsToDB());
				p.setInt(3, acc.getGmLvl());
				p.setString(4, acc.parseStableIDs());
				p.setInt(5, (acc.isBanned()?1:0));
				p.setString(6, acc.parseFriendListToDB());
				p.setString(7, acc.parseEnemyListToDB());
				p.setInt(8, acc.isShowFriendConnection() ? 1 : 0);
				p.setInt(9, acc.getGUID());
				
				p.executeUpdate();
				closePreparedStatement(p);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void UPDATE_ACCOUNT_BANKKAMAS(final int compteID,final int toAdd)
	{
		synchronized(syncLock)
		{
			try
			{
				final String baseQuery = "UPDATE `accounts` SET" +
						" bankKamas = bankKamas + ?" +
						" WHERE guid = ?;";
				final PreparedStatement p = newTransact(baseQuery, loginConn);
				
				p.setInt(1, toAdd);
				p.setInt(2, compteID);
				
				p.executeUpdate();
				closePreparedStatement(p);
				
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void LOAD_CRAFTS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from crafts;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					final ArrayList<Couple<Integer,Integer>> m = new ArrayList<Couple<Integer,Integer>>();
					
					boolean cont = true;
					for(final String str : RS.getString("craft").split(";"))
					{
						try
						{
								final int tID = Integer.parseInt(str.split("\\*")[0]);
								final int qua =  Integer.parseInt(str.split("\\*")[1]);
								m.add(new Couple<Integer,Integer>(tID,qua));
						}catch(final Exception e){e.printStackTrace();cont = false;};
					}
					//s'il y a eu une erreur de parsing, on ignore cette recette
					if(!cont)continue;
					
					World.addCraft
					(
						RS.getInt("id"),
						m
					);
				}
				closeResultSet(RS);;
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void LOAD_GUILDS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from guilds;",Config.SERVER_DB_NAME);
				while(RS.next())
				{
					World.addGuild
					(
					new Guild(
							RS.getInt("id"),
							RS.getString("name"),
							RS.getString("emblem"),
							RS.getInt("lvl"),
							RS.getLong("xp"),
							RS.getString("sorts"),
							RS.getString("stats")
					),false
					);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void LOAD_GUILD_MEMBERS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from guild_members;",Config.SERVER_DB_NAME);
				while(RS.next())
				{
					//Personnage P = World.getPersonnage(RS.getInt("guid"));
					final Guild G = World.getGuild(RS.getInt("guild"));
					if(G == null)continue;
					/*GuildMember GM = */G.addMember(RS.getInt("guid"), RS.getString("name"), RS.getInt("level"), RS.getInt("gfxid"), RS.getInt("rank"), RS.getByte("pxp"), RS.getLong("xpdone"), RS.getInt("rights"), RS.getByte("align"),RS.getDate("lastConnection").toString().replaceAll("-","~"));
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void LOAD_MOUNTS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from mounts_data;",Config.SERVER_DB_NAME);
				while(RS.next())
				{
					World.addDragodinde
					(
						new Mount
						(
							RS.getInt("id"),
							RS.getInt("color"),
							RS.getInt("sexe"),
							RS.getInt("amour"),
							RS.getInt("endurance"),
							RS.getInt("level"),
							RS.getLong("xp"),
							RS.getString("name"),
							RS.getInt("fatigue"),
							RS.getInt("energie"),
							RS.getInt("reproductions"),
							RS.getInt("maturite"),
							RS.getInt("serenite"),
							RS.getString("items"),
							RS.getString("ancetres"),
							RS.getString("capacites")
						)
					);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void LOAD_DROPS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from drops;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					final Monster MT = World.getMonstre(RS.getInt("mob"));
					MT.addDrop(new Drop(
							RS.getInt("item"),
							RS.getInt("seuil"),
							RS.getFloat("taux"),
							RS.getInt("max")
					));
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void LOAD_ITEMSETS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from itemsets;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					World.addItemSet(
								new ItemSet
								(
									RS.getInt("id"),
									RS.getString("items"),
									RS.getString("bonus")
								)
							);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void LOAD_IOTEMPLATE()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from interactive_objects_data;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					World.addIOTemplate(
								new IOTemplate
								(
									RS.getInt("id"),
									RS.getInt("respawn"),
									RS.getInt("duration"),
									RS.getInt("unknow"),
									RS.getInt("walkable")==1
								)
							);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void LOAD_MOUNTPARKS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from mountpark_data;",Config.SERVER_DB_NAME);
				while(RS.next())
				{
					final DofusMap map = World.getMap(RS.getInt("mapid"));
					if(map == null)continue;
					new MountPark(
							RS.getInt("owner"),
							map,
							RS.getInt("size"),
							RS.getString("data"),
							RS.getInt("guild"),
							RS.getInt("price")
					);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void LOAD_JOBS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from jobs_data;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					World.addJob(
							new Job(
								RS.getInt("id"),
								RS.getString("tools"),
								RS.getString("crafts")
								)
							);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void LOAD_AREA()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from area_data;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					final Area A = new Area
						(
							RS.getInt("id"),
							RS.getInt("superarea"),
							RS.getString("name"),
							RS.getString("cemetery")
						);
					World.addArea(A);
					//on ajoute la zone au continent
					A.getSuperArea().addArea(A);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void LOAD_SUBAREA()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from subarea_data;",Config.SERVER_DB_NAME);
				while(RS.next())
				{
					final SubArea SA = new SubArea
						(
							RS.getInt("id"),
							RS.getInt("area"),
							RS.getInt("alignement"),
							RS.getString("name"),
							RS.getInt("prismId")
						);
					World.addSubArea(SA);
					//on ajoute la sous zone a la zone
					if(SA.getArea() != null)
						SA.getArea().addSubArea(SA);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static int LOAD_NPCS()
	{
		synchronized(syncLock)
		{
			int nbr = 0;
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from npcs;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					final DofusMap map = World.getMap(RS.getInt("mapid"));
					if(map == null)continue;
					map.addNpc(RS.getInt("npcid"), RS.getInt("cellid"), RS.getInt("orientation"));
					nbr ++;
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
				nbr = 0;
			}
			return nbr;
		}
	}
	
	/*public static void LOAD_COMPTES()
	{
		try
		{
			final ResultSet RS = SQLManager.executeQuery("SELECT * from accounts;",Config.SERVER_DB_NAME);
			final String baseQuery = "UPDATE accounts " +
								"SET `reload_needed` = 0 " +
								"WHERE guid = ?;";
			final PreparedStatement p = newTransact(baseQuery, serverConn);
			while(RS.next())
			{
				World.addAccount(new Account(
				RS.getInt("guid"),
				RS.getString("account"),
				RS.getString("pass"),
				RS.getString("pseudo"),
				RS.getString("question"),
				RS.getString("reponse"),
				RS.getInt("level"),
				(RS.getInt("banned") == 1),
				RS.getString("lastIP"),
				RS.getString("lastConnectionDate"),
				RS.getString("bank"),
				RS.getInt("bankKamas"),
				RS.getString("friends"),
				RS.getString("stable"),
				RS.getString("enemys"), 
				(RS.getInt("seeFriend") == 1)
				));
				
				p.setInt(1, RS.getInt("guid"));
				p.executeUpdate();
			}
			closePreparedStatement(p);
			closeResultSet(RS);
		}catch(final SQLException e)
		{
			Log.addToErrorLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}*/
	public static int getNextPersonnageGuid()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = executeQuery("SELECT guid FROM personnages ORDER BY guid DESC LIMIT 1;",Config.SERVER_DB_NAME);
				RS.first();
				int guid = RS.getInt("guid");
				guid++;
				closeResultSet(RS);
				return guid;
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
				Main.closeServers();
			}
			return 0;
		}
	}
	public static void LOAD_PERSO_BY_ACCOUNT(final int accID)
	{
		try
		{
			final ResultSet RS = SQLManager.executeQuery("SELECT * FROM personnages WHERE account = '"+accID+"';", Config.SERVER_DB_NAME);
			while(RS.next())
			{
				final TreeMap<Integer,Integer> stats = new TreeMap<Integer,Integer>();
				stats.put(Constants.STATS_ADD_VITA, RS.getInt("vitalite"));
				stats.put(Constants.STATS_ADD_FORC, RS.getInt("force"));
				stats.put(Constants.STATS_ADD_SAGE, RS.getInt("sagesse"));
				stats.put(Constants.STATS_ADD_INTE, RS.getInt("intelligence"));
				stats.put(Constants.STATS_ADD_CHAN, RS.getInt("chance"));
				stats.put(Constants.STATS_ADD_AGIL, RS.getInt("agilite"));
				
				final Player perso = new Player(
						RS.getInt("guid"),
						RS.getString("name"),
						RS.getInt("sexe"),
						RS.getInt("class"),
						RS.getInt("color1"),
						RS.getInt("color2"),
						RS.getInt("color3"),
						RS.getLong("kamas"),
						RS.getInt("spellboost"),
						RS.getInt("capital"),
						RS.getInt("energy"),
						RS.getInt("level"),
						RS.getLong("xp"),
						RS.getInt("size"),
						RS.getInt("gfx"),
						RS.getByte("alignement"),
						RS.getInt("account"),
						stats,
						RS.getInt("seeFriend"),
						RS.getByte("showWings"),
						RS.getString("canaux"),
						RS.getInt("map"),
						RS.getInt("cell"),
						RS.getString("objets"),
						RS.getInt("pdvper"),
						RS.getString("spells"),
						RS.getString("savepos"),
						RS.getString("jobs"),
						RS.getInt("mountxpgive"),
						RS.getInt("mount"),
						RS.getInt("honor"),
						RS.getInt("deshonor"),
						RS.getInt("alvl"),
						RS.getString("zaaps"),
						RS.getString("mobsfollower"),
						RS.getInt("restrictions"),
						RS.getInt("rights"),
						RS.getInt("emotes")
						);
				perso.getBaseStats().addOneStat(Constants.STATS_ADD_PA,RS.getInt("pa"));	//MARTHIEUBEAN
				perso.getBaseStats().addOneStat(Constants.STATS_ADD_PM,RS.getInt("pm"));	//MARTHIEUBEAN
				perso.linkItems();
				World.addPersonnage(perso);
				/*Afféctation d'une guilde au membre s'il y a lieu*/
				final int guildId = isPersoInGuild(RS.getInt("guid"));
				if(guildId >= 0)
				{
					perso.setGuildMember(World.getGuild(guildId).getMember(RS.getInt("guid")));
				}
				/*FIN*/
				if(World.getAccount(accID) != null)
					World.getAccount(accID).addPerso(perso);
			}
			
			closeResultSet(RS);
		}catch(final SQLException e)
		{
			Log.addToErrorLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			Main.closeServers();
		}
	}
	public static void LOAD_PERSO(final int persoID)
	{
		synchronized(playerSyncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * FROM personnages WHERE guid = '"+persoID+"';", Config.SERVER_DB_NAME);
				int accID;
				while(RS.next())
				{
					final TreeMap<Integer,Integer> stats = new TreeMap<Integer,Integer>();
					stats.put(Constants.STATS_ADD_VITA, RS.getInt("vitalite"));
					stats.put(Constants.STATS_ADD_FORC, RS.getInt("force"));
					stats.put(Constants.STATS_ADD_SAGE, RS.getInt("sagesse"));
					stats.put(Constants.STATS_ADD_INTE, RS.getInt("intelligence"));
					stats.put(Constants.STATS_ADD_CHAN, RS.getInt("chance"));
					stats.put(Constants.STATS_ADD_AGIL, RS.getInt("agilite"));
					
					accID = RS.getInt("account");
					
					final Player perso = new Player(
							RS.getInt("guid"),
							RS.getString("name"),
							RS.getInt("sexe"),
							RS.getInt("class"),
							RS.getInt("color1"),
							RS.getInt("color2"),
							RS.getInt("color3"),
							RS.getLong("kamas"),
							RS.getInt("spellboost"),
							RS.getInt("capital"),
							RS.getInt("energy"),
							RS.getInt("level"),
							RS.getLong("xp"),
							RS.getInt("size"),
							RS.getInt("gfx"),
							RS.getByte("alignement"),
							accID,
							stats,
							RS.getInt("seeFriend"),
							RS.getByte("showWings"),
							RS.getString("canaux"),
							RS.getInt("map"),
							RS.getInt("cell"),
							RS.getString("objets"),
							RS.getInt("pdvper"),
							RS.getString("spells"),
							RS.getString("savepos"),
							RS.getString("jobs"),
							RS.getInt("mountxpgive"),
							RS.getInt("mount"),
							RS.getInt("honor"),
							RS.getInt("deshonor"),
							RS.getInt("alvl"),
							RS.getString("zaaps"),
							RS.getString("mobsfollower"),
							RS.getInt("restrictions"),
							RS.getInt("rights"),
							RS.getInt("emotes")
							);
					perso.getBaseStats().addOneStat(Constants.STATS_ADD_PA,RS.getInt("pa"));	//MARTHIEUBEAN
					perso.getBaseStats().addOneStat(Constants.STATS_ADD_PM,RS.getInt("pm"));	//MARTHIEUBEAN
					perso.linkItems();
					World.addPersonnage(perso);
					/*Afféctation d'une guilde au membre s'il y a lieu*/
					final int guildId = isPersoInGuild(RS.getInt("guid"));
					if(guildId >= 0)
					{
						perso.setGuildMember(World.getGuild(guildId).getMember(RS.getInt("guid")));
					}
					/*FIN*/
					if(World.getAccount(accID) != null)
						World.getAccount(accID).addPerso(perso);
				}
				
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
				Main.closeServers();
			}
		}
	}
	
	public static void LOAD_PERSOS() 
	{
		synchronized(playerSyncLock)
		{
			try {
				final ResultSet RS = executeQuery("SELECT * FROM personnages;", Config.SERVER_DB_NAME);

				while (RS.next()) {
					final Map<Integer, Integer> stats = new TreeMap<Integer, Integer>();
					stats.put(Constants.STATS_ADD_VITA, RS.getInt("vitalite"));
					stats.put(Constants.STATS_ADD_FORC, RS.getInt("force"));
					stats.put(Constants.STATS_ADD_SAGE, RS.getInt("sagesse"));
					stats.put(Constants.STATS_ADD_INTE, RS.getInt("intelligence"));
					stats.put(Constants.STATS_ADD_CHAN, RS.getInt("chance"));
					stats.put(Constants.STATS_ADD_AGIL, RS.getInt("agilite"));

					final Player perso = new Player(RS.getInt("guid"),
					        RS.getString("name"), 
					        RS.getInt("sexe"),
					        RS.getInt("class"), 
					        RS.getInt("color1"),
					        RS.getInt("color2"), 
					        RS.getInt("color3"),
					        RS.getLong("kamas"), 
					        RS.getInt("spellboost"),
					        RS.getInt("capital"),
					        RS.getInt("energy"),
					        RS.getInt("level"), 
					        RS.getLong("xp"),
					        RS.getInt("size"), 
					        RS.getInt("gfx"),
					        RS.getByte("alignement"), 
					        RS.getInt("account"), 
					        stats,
					        RS.getByte("seeFriend"), 
					        RS.getByte("showWings"),
					        RS.getString("canaux"),
					        RS.getShort("map"), 
					        RS.getInt("cell"),
					        RS.getString("objets"), 
					        RS.getInt("pdvper"),
					        RS.getString("spells"), 
					        RS.getString("savepos"),
					        RS.getString("jobs"), 
					        RS.getInt("mountxpgive"),
					        RS.getInt("mount"), 
					        RS.getInt("honor"),
					        RS.getInt("deshonor"), 
					        RS.getInt("alvl"),
					        RS.getString("zaaps"),
							RS.getString("mobsfollower"),
							RS.getInt("restrictions"),
							RS.getInt("rights"),
							RS.getInt("emotes"));
					perso.getBaseStats().addOneStat(Constants.STATS_ADD_PA, RS.getInt("pa"));	//MARTHIEUBEAN
					perso.getBaseStats().addOneStat(Constants.STATS_ADD_PM, RS.getInt("pm"));	//MARTHIEUBEAN
					perso.linkItems();
					World.addPersonnage(perso);
					/*Afféctation d'une guilde au membre s'il y a lieu*/
					final int guildId = isPersoInGuild(RS.getInt("guid"));
					if(guildId >= 0)
					{
						perso.setGuildMember(World.getGuild(guildId).getMember(RS.getInt("guid")));
					}
					if (World.getAccount(RS.getInt("account")) != null) {
						World.getAccount(RS.getInt("account")).addPerso(perso);
					}
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
				Main.closeServers();
			}
		}
	}

	public static boolean DELETE_PERSO_IN_BDD(final Player perso)
	{
		synchronized(playerSyncLock)
		{
			final int guid = perso.getActorId();
			String baseQuery = "DELETE FROM personnages WHERE guid = ?;";
			
			try {
				PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1, guid);
				
				p.execute();
				
				if(!perso.getItemsIDSplitByChar(",").equals(""))
				{
					baseQuery = "DELETE FROM items WHERE guid IN (?);";
					p = newTransact(baseQuery, serverConn);
					p.setString(1, perso.getItemsIDSplitByChar(","));
					
					p.execute();
				}
				if(perso.getMount() != null)
				{
					baseQuery = "DELETE FROM mounts_data WHERE id = ?";
					p = newTransact(baseQuery, serverConn);
					p.setInt(1, perso.getMount().getId());
					
					p.execute();
					World.delDragoByID(perso.getMount().getId());
				}
				if(perso.getGuildMember() != null)
				{
					perso.getGuild().removeMember(guid);
					baseQuery = "DELETE FROM guild_members WHERE guid = ?";
					p = newTransact(baseQuery, serverConn);
					p.setInt(1, guid);
					
					p.execute();
				}
				
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
				Log.addToErrorLog("Supression du personnage echouee");
				return false;
			}
		}
	}
	
	public static boolean ADD_PERSO_IN_BDD(final Player perso)
	{
		synchronized(playerSyncLock)
		{
			final String baseQuery = "INSERT INTO personnages(`guid` , `name` , `sexe` , `class` , `color1` , `color2` , `color3` , `kamas` , `spellboost` , `capital` , `energy` , `level` , `xp` , `size` , `gfx` , `account`,`cell`,`map`,`spells`,`objets`)" +
					" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'');";
			
			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				
				p.setInt(1,perso.getActorId());
				p.setString(2, perso.getName());
				p.setInt(3,perso.getSexe());
				p.setInt(4,perso.getBreedId());
				p.setInt(5,perso.getColor1());
				p.setInt(6,perso.getColor2());
				p.setInt(7,perso.getColor3());
				p.setLong(8,perso.getKamas());
				p.setInt(9,perso.getSpellPts());
				p.setInt(10,perso.getCapital());
				p.setInt(11,perso.getEnergy());
				p.setInt(12,perso.getLvl());
				p.setLong(13,perso.getCurExp());
				p.setInt(14,perso.getSize());
				p.setInt(15,perso.getGfxID());
				p.setInt(16,perso.getAccID());
				p.setInt(17,perso.getCurCell().getId());
				p.setInt(18,perso.getCurMap().getId());
				p.setString(19, perso.parseSpellToDB());
				
				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
				Log.addToErrorLog("Creation du personnage echouee");
				return false;
			}
		}
	}

	public static void LOAD_EXP()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from experience;", Config.COMMON_DB_NAME);
				while(RS.next())World.addExpLevel(RS.getInt("lvl"),new World.ExpLevel(RS.getLong("perso"),RS.getInt("metier"),RS.getInt("dinde"),RS.getInt("pvp"),RS.getInt("obvijevan")));
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
		}
	}
	
	
	public static int LOAD_TRIGGERS()
	{
		synchronized(syncLock)
		{
			try
			{
				int nbr = 0;
				final ResultSet RS = SQLManager.executeQuery("SELECT * FROM `scripted_cells`",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					if(World.getMap(RS.getInt("MapID")) == null) continue;
					if(World.getMap(RS.getInt("MapID")).getCell(RS.getInt("CellID")) == null) continue;
					
					switch(RS.getInt("EventID"))
					{
						case 1://Stop sur la case(triggers)
							World.getMap(RS.getInt("MapID")).getCell(RS.getInt("CellID")).addOnCellStopAction(RS.getInt("ActionID"), RS.getString("ActionsArgs"), RS.getString("Conditions"));	
						break;
							
						default:
							Log.addToErrorLog("Action Event "+RS.getInt("EventID")+" non implanté");
						break;
					}
					nbr++;
				}
				closeResultSet(RS);
				return nbr;
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
			return 0;
		}
	}

	public static void LOAD_MAPS()
	{
		synchronized(syncLock)
		{
			try
			{
				ResultSet RS;
				/*if(!Ancestra.CONFIG_DEBUG)
				{
					RS = SQLManager.executeQuery("SELECT  * from maps LIMIT "+Constants.DEBUG_MAP_LIMIT+";",Ancestra.STATIC_DB_NAME);
				}
				else
				{
					/*DEBUG
					String divers = "250000,675,7411,10109";
					String houseLac = "9015,10853,10854,10855,10858,10862,10865,10869,10875,10881,10883,10885,10890,10894,10900,10901";
					String hdv = "4216,4271,8759,4287,2221,4232,4178,4183,8760,4098,4179,6159,4299,4247,4262,8757,4174,4172,8478";
					RS = SQLManager.executeQuery("SELECT  * from maps WHERE id IN("+divers+",10291,8747,4216,10129,10130,10131,10132,10133,10134,"+hdv+","+houseLac+");",Ancestra.STATIC_DB_NAME);
					/*
					//RS = executeQuery("SELECT m.* FROM maps AS m,maps_stat AS ms WHERE ms.demandes > 3 AND m.id = ms.map", Ancestra.STATIC_DB_NAME);
				}*/
				
				RS = SQLManager.executeQuery("SELECT * from maps LIMIT "+Constants.DEBUG_MAP_LIMIT+";",Config.COMMON_DB_NAME);
				
				while(RS.next())
				{
						World.addDofusMap(
								new DofusMap(
								RS.getInt("id"),
								RS.getString("date"),
								RS.getInt("width"),
								RS.getInt("heigth"),
								RS.getString("key"),
								RS.getString("places"),
								RS.getString("mapData"),
								RS.getString("monsters"),
								RS.getString("mappos"),
								RS.getInt("numgroup"),
								RS.getInt("groupmaxsize")
								));
				}
				SQLManager.closeResultSet(RS);
				RS = SQLManager.executeQuery("SELECT * from mobgroups_fix;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
						final DofusMap c = World.getMap(RS.getInt("mapid"));
						if(c == null)continue;
						if(c.getCell(RS.getInt("cellid")) == null)continue;
						c.addStaticGroup(RS.getInt("cellid"), RS.getString("groupData"));
				}
				SQLManager.closeResultSet(RS);
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
		}
	}
	
	public static void SAVE_PERSONNAGE(final Player _perso, final boolean saveItem)
	{
		String baseQuery = "UPDATE `personnages` SET "+
						"`seeFriend`= ?,"+
						"`canaux`= ?,"+
						"`pdvper`= ?,"+
						"`map`= ?,"+
						"`cell`= ?,"+
						"`vitalite`= ?,"+
						"`force`= ?,"+
						"`sagesse`= ?,"+
						"`intelligence`= ?,"+
						"`chance`= ?,"+
						"`agilite`= ?,"+
						"`alignement`= ?,"+
						"`honor`= ?,"+
						"`deshonor`= ?,"+
						"`alvl`= ?,"+
						"`gfx`= ?,"+
						"`xp`= ?,"+
						"`level`= ?,"+
						"`energy`= ?,"+
						"`capital`= ?,"+
						"`spellboost`= ?,"+
						"`kamas`= ?,"+
						"`size` = ?," +
						"`spells` = ?," +
						"`objets` = ?,"+
						"`savepos` = ?,"+
						"`jobs` = ?,"+
						"`mountxpgive` = ?,"+
						"`zaaps` = ?,"+
						"`mount` = ?,"+	
						"`mobsfollower` = ?,"+
						"`showWings` = ?,"+
						"`emotes` = ?,"+
						"`restrictions` = ?,"+
						"`rights` = ?"+
						" WHERE `personnages`.`guid` = ? LIMIT 1;";
		
		PreparedStatement p = null;
		
		try
		{
			p = newTransact(baseQuery, serverConn);
			
			p.setInt(1,(_perso.isShowFriendConnection()?1:0));
			p.setString(2,_perso.getChannels());
			p.setInt(3,_perso.getPdvPer());
			p.setInt(4,_perso.getCurMap().getId());
			p.setInt(5,_perso.getCurCell().getId());
			p.setInt(6,_perso.getBaseStats().getEffect(Constants.STATS_ADD_VITA));
			p.setInt(7,_perso.getBaseStats().getEffect(Constants.STATS_ADD_FORC));
			p.setInt(8,_perso.getBaseStats().getEffect(Constants.STATS_ADD_SAGE));
			p.setInt(9,_perso.getBaseStats().getEffect(Constants.STATS_ADD_INTE));
			p.setInt(10,_perso.getBaseStats().getEffect(Constants.STATS_ADD_CHAN));
			p.setInt(11,_perso.getBaseStats().getEffect(Constants.STATS_ADD_AGIL));
			p.setInt(12,_perso.getAlign());
			p.setInt(13,_perso.getHonor());
			p.setInt(14,_perso.getDeshonor());
			p.setInt(15,_perso.getALvl());
			p.setInt(16,_perso.getGfxID());
			p.setLong(17,_perso.getCurExp());
			p.setInt(18,_perso.getLvl());
			p.setInt(19,_perso.getEnergy());
			p.setInt(20,_perso.getCapital());
			p.setInt(21,_perso.getSpellPts());
			p.setLong(22,_perso.getKamas());
			p.setInt(23,_perso.getSize());
			p.setString(24,_perso.parseSpellToDB());
			p.setString(25,_perso.parseObjetsToDB());
			p.setString(26,_perso.getSavePos());
			p.setString(27,_perso.parseJobData());
			p.setInt(28,_perso.getMountXpGive());
			p.setString(29,_perso.parseZaaps());
			p.setInt(30, (_perso.getMount()!=null?_perso.getMount().getId():-1));
			p.setString(31, _perso.parseFollowers());
			p.setInt(32, _perso.isShowingWings() ? 1 : 0);
			p.setInt(33, _perso.getEmotes().get());
			p.setInt(34, _perso.getRestrictions().get());
			p.setInt(35, _perso.getRights().get());
			p.setInt(36,_perso.getActorId());
			
			p.executeUpdate();
			
			if(_perso.getGuildMember() != null)
				UPDATE_GUILDMEMBER(_perso.getGuildMember());
			if(_perso.getMount() != null)
				UPDATE_MOUNT_INFOS(_perso.getMount());
			if(Main.isRunning)Log.addToErrorLog("Personnage "+_perso.getName()+" sauvegardé");
		}catch(final SQLException e)
		{
			System.out.println("Game: SQL ERROR: "+e.getMessage());
			System.out.println("Requete: "+baseQuery);
			System.out.println("Le personnage n'a pas été sauvegardé");
			System.exit(1);
		};
		if(saveItem)
		{
			baseQuery = "UPDATE `items` SET qua = ?, pos= ?, stats = ?"+
			" WHERE guid = ?;";
			try {
				p = newTransact(baseQuery, serverConn);
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
			
			for(final String idStr : _perso.getItemsIDSplitByChar(":").split(":"))
			{
				try
				{
					final int guid = Integer.parseInt(idStr);
					final Item obj = World.getObjet(guid);
					if(obj == null)continue;
					if (obj.isSpeaking())
						UPDATE_SPEAKING_ITEM(obj);
					if(obj.isPet())
						UPDATE_PET_ITEM(obj);
					p.setInt(1, obj.getQuantity());
					p.setInt(2, obj.getPosition());
					p.setString(3, obj.parseToSave());
					p.setInt(4, Integer.parseInt(idStr));
					
					p.execute();
				}catch(final Exception e){continue;};
				
			}
			
			if(_perso.getAccount() == null)
				return;
			for(final String idStr : _perso.getBankItemsIDSplitByChar(":").split(":"))
			{
				try
				{
					final int guid = Integer.parseInt(idStr);
					final Item obj = World.getObjet(guid);
					if(obj == null)continue;
					if (obj.isSpeaking())
						UPDATE_SPEAKING_ITEM(obj);
					if(obj.isPet())
						UPDATE_PET_ITEM(obj);
					p.setInt(1, obj.getQuantity());
					p.setInt(2, obj.getPosition());
					p.setString(3, obj.parseToSave());
					p.setInt(4, Integer.parseInt(idStr));
					
					p.execute();
				}catch(final Exception e){continue;};
				
			}
		}
		
		closePreparedStatement(p);
	}

	/*public static void SAVE_PERSONNAGE(Player _perso, boolean saveItem)
	{
		synchronized(playerSyncLock)
		{
			String baseQuery = "UPDATE `personnages` SET "+
					"`seeFriend`= ?,"+
					"`canaux`= ?,"+
					"`pdvper`= ?,"+
					"`map`= ?,"+
					"`cell`= ?,"+
					"`vitalite`= ?,"+
					"`force`= ?,"+
					"`sagesse`= ?,"+
					"`intelligence`= ?,"+
					"`chance`= ?,"+
					"`agilite`= ?,"+
					"`alignement`= ?,"+
					"`honor`= ?,"+
					"`deshonor`= ?,"+
					"`alvl`= ?,"+
					"`gfx`= ?,"+
					"`xp`= ?,"+
					"`level`= ?,"+
					"`energy`= ?,"+
					"`capital`= ?,"+
					"`spellboost`= ?,"+
					"`kamas`= ?,"+
					"`size` = ?," +
					"`spells` = ?," +
					"`objets` = ?,"+
					"`savepos` = ?,"+
					"`jobs` = ?,"+
					"`mountxpgive` = ?,"+
					"`zaaps` = ?,"+
					"`mount` = ?,"+	
					"`mobsfollower` = ?,"+
					"`showWings` = ?,"+
					"`emotes` = ?,"+
					"`restrictions` = ?,"+
					"`rights` = ?"+
					" WHERE `personnages`.`guid` = ? LIMIT 1;";

			PreparedStatement p = null;

			try
			{
				p = newTransact(baseQuery, othCon);

				p.setInt(1,(_perso.isShowFriendConnection()?1:0));
				p.setString(2,_perso.getChannels());
				p.setInt(3,_perso.getPdvPer());
				p.setInt(4,_perso.getCurMap().getId());
				p.setInt(5,_perso.getCurCell().getID());
				p.setInt(6,_perso.getBaseStats().getEffect(Constants.STATS_ADD_VITA));
				p.setInt(7,_perso.getBaseStats().getEffect(Constants.STATS_ADD_FORC));
				p.setInt(8,_perso.getBaseStats().getEffect(Constants.STATS_ADD_SAGE));
				p.setInt(9,_perso.getBaseStats().getEffect(Constants.STATS_ADD_INTE));
				p.setInt(10,_perso.getBaseStats().getEffect(Constants.STATS_ADD_CHAN));
				p.setInt(11,_perso.getBaseStats().getEffect(Constants.STATS_ADD_AGIL));
				p.setInt(12,_perso.getAlign());
				p.setInt(13,_perso.getHonor());
				p.setInt(14,_perso.getDeshonor());
				p.setInt(15,_perso.getALvl());
				p.setInt(16,_perso.getGfxID());
				p.setLong(17,_perso.getCurExp());
				p.setInt(18,_perso.getLvl());
				p.setInt(19,_perso.getEnergy());
				p.setInt(20,_perso.getCapital());
				p.setInt(21,_perso.getSpellPts());
				p.setLong(22,_perso.getKamas());
				p.setInt(23,_perso.getSize());
				p.setString(24,_perso.parseSpellToDB());
				p.setString(25,_perso.parseObjetsToDB());
				p.setString(26,_perso.getSavePos());
				p.setString(27,_perso.parseJobData());
				p.setInt(28,_perso.getMountXpGive());
				p.setString(29,_perso.parseZaaps());
				p.setInt(30, (_perso.getMount()!=null?_perso.getMount().getId():-1));
				p.setString(31, _perso.parseMFToDB());
				p.setByte(32, _perso.isShowingWings() ? (byte)1 : (byte)0);
				p.setInt(33, _perso.getEmotes().get());
				p.setInt(34, _perso.getRestrictions().get());
				p.setInt(35, _perso.getRight().get());
				p.setInt(36,_perso.getActorId());

				p.executeUpdate();

				if(_perso.getGuildMember() != null)
					UPDATE_GUILDMEMBER(_perso.getGuildMember());
				if(_perso.getMount() != null)
					UPDATE_MOUNT_INFOS(_perso.getMount());
				if(!Faith.isLoading)Log.addToErrorLog("Personnage "+_perso.getName()+" sauvegardé");
			}catch(SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.out.println("Requête: "+baseQuery);
				System.out.println("Le personnage n'a pas été sauvegardé");
				System.exit(1);
			};
			if(saveItem)
			{
				baseQuery = "UPDATE `items` SET qua = ?, pos= ?, stats = ?"+
						" WHERE guid = ?;";
				try {
					p = newTransact(baseQuery, othCon);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}

				for(String idStr : _perso.getItemsIDSplitByChar(":").split(":"))
				{
					try
					{
						int guid = Integer.parseInt(idStr);
						Item obj = World.getObjet(guid);
						if(obj == null)continue;
						if (obj.isSpeaking())
							UPDATE_SPEAKING_ITEM(obj);
						if(obj.isPet())
							UPDATE_PET_ITEM(obj);
						p.setInt(1, obj.getQuantity());
						p.setInt(2, obj.getPosition());
						p.setString(3, obj.parseToSave());
						p.setInt(4, Integer.parseInt(idStr));

						p.execute();
					}catch(Exception e){continue;};

				}

				if(_perso.getAccount() == null)
					return;
				for(String idStr : _perso.getBankItemsIDSplitByChar(":").split(":"))
				{
					try
					{
						int guid = Integer.parseInt(idStr);
						Item obj = World.getObjet(guid);
						if(obj == null)continue;
						if (obj.isSpeaking())
							UPDATE_SPEAKING_ITEM(obj);
						if (obj.isPet())
							UPDATE_PET_ITEM(obj);
						p.setInt(1, obj.getQuantity());
						p.setInt(2, obj.getPosition());
						p.setString(3, obj.parseToSave());
						p.setInt(4, Integer.parseInt(idStr));

						p.execute();
					}catch(Exception e){continue;};

				}
			}
			closePreparedStatement(p);
		}
	}*/

	public static void LOAD_SORTS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT  * from sorts;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					final int id = RS.getInt("id");
					final Spell sort = new Spell(id,RS.getInt("sprite"),RS.getString("spriteInfos"),RS.getString("effectTarget"));
					final SpellStat l1 = parseSortStats(id,1,RS.getString("lvl1"));
					final SpellStat l2 = parseSortStats(id,2,RS.getString("lvl2"));
					final SpellStat l3 = parseSortStats(id,3,RS.getString("lvl3"));
					final SpellStat l4 = parseSortStats(id,4,RS.getString("lvl4"));
					SpellStat l5 = null;
					if(!RS.getString("lvl5").equalsIgnoreCase("-1"))
						l5 = parseSortStats(id,5,RS.getString("lvl5"));
					SpellStat l6 = null;
					if(!RS.getString("lvl6").equalsIgnoreCase("-1"))
							l6 = parseSortStats(id,6,RS.getString("lvl6"));
					sort.addSortStats(1,l1);
					sort.addSortStats(2,l2);
					sort.addSortStats(3,l3);
					sort.addSortStats(4,l4);
					sort.addSortStats(5,l5);
					sort.addSortStats(6,l6);
					World.addSort(sort);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
		}
	}

	public static void LOAD_OBJ_TEMPLATE()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT  * from item_template;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
						World.addObjTemplate
						(
							new ItemTemplate
							(
								RS.getInt("id"),
								RS.getString("statsTemplate"),
								RS.getString("name"),
								RS.getInt("type"),
								RS.getInt("level"),
								RS.getInt("pod"),
								RS.getInt("prix"),
								RS.getInt("panoplie"),
								RS.getString("condition"),
								RS.getString("armesInfos")
							)
						);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
		}
	}
	
	private static SpellStat parseSortStats(final int id,final int lvl,final String str)
	{
		try
		{
			SpellStat stats = null;
			final String[] stat = str.split(",");
			final String effets = stat[0];
			final String CCeffets = stat[1];
			int PACOST = 6;
			try
			{
				PACOST = Integer.parseInt(stat[2].trim());
			}catch(final NumberFormatException e){};
			
			final int POm = Integer.parseInt(stat[3].trim());
			final int POM = Integer.parseInt(stat[4].trim());
			final int TCC = Integer.parseInt(stat[5].trim());
			final int TEC = Integer.parseInt(stat[6].trim());
			final boolean line = stat[7].trim().equalsIgnoreCase("true");
			final boolean LDV = stat[8].trim().equalsIgnoreCase("true");
			final boolean emptyCell = stat[9].trim().equalsIgnoreCase("true");
			final boolean MODPO = stat[10].trim().equalsIgnoreCase("true");
			//int unk = Integer.parseInt(stat[11]);//All 0
			final int MaxByTurn = Integer.parseInt(stat[12].trim());
			final int MaxByTarget = Integer.parseInt(stat[13].trim());
			final int CoolDown = Integer.parseInt(stat[14].trim());
			final String type = stat[15].trim();
			final String statesNeeded = stat[16].trim();
			final String statesProhibited = stat[17].trim();
			final int level = Integer.parseInt(stat[stat.length-2].trim());
			final boolean endTurn = stat[19].trim().equalsIgnoreCase("true");
			stats = new SpellStat(id,lvl,PACOST,POm, POM, TCC, TEC, line, LDV, emptyCell, MODPO, MaxByTurn, MaxByTarget, CoolDown, level, endTurn, effets, CCeffets,type,statesNeeded,statesProhibited);
			return stats;
		}catch(final Exception e)
		{
			e.printStackTrace();
			int nbr = 0;
			System.out.println("[DEBUG]Sort "+id+" lvl "+lvl);
			for(final String z:str.split(","))
			{
				System.out.println("[DEBUG]"+nbr+" "+z);
				nbr++;
			}
			System.exit(1);
			return null;
		}
	}

	public static void LOAD_MOB_TEMPLATE() 
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * FROM monsters;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					final int id = RS.getInt("id");
					final int gfxID = RS.getInt("gfxID");
					final int align = RS.getInt("align");
					final String colors = RS.getString("colors");
					final String grades = RS.getString("grades");
					final String spells = RS.getString("spells");
					final String stats = RS.getString("stats");
					final String pdvs = RS.getString("pdvs");
					final String pts = RS.getString("points");
					final String inits = RS.getString("inits");
					final int mK = RS.getInt("minKamas");
					final int MK = RS.getInt("maxKamas");
					final int IAType = RS.getInt("AI_Type");
					final String xp = RS.getString("exps");
					final boolean capturable = RS.getString("capturable").equalsIgnoreCase("true");
					//String drop = RS.getString("drop");
					World.addMobTemplate
					(
						id,
						new Monster
						(
							id,
							gfxID,
							align,
							colors,
							grades,
							spells,
							stats,
							pdvs,
							pts,
							inits,
							mK,
							MK,
							xp,
							IAType,
							capturable
						)
					);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
		}
	}

	public static void LOAD_NPC_TEMPLATE()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * FROM npc_template;", Config.COMMON_DB_NAME);
				while(RS.next())
				{
					final int id = RS.getInt("id");
					final int bonusValue = RS.getInt("bonusValue");
					final int gfxID = RS.getInt("gfxID");
					final int scaleX = RS.getInt("scaleX");
					final int scaleY = RS.getInt("scaleY");
					final int sex = RS.getInt("sex");
					final int color1 = RS.getInt("color1");
					final int color2 = RS.getInt("color2");
					final int color3 = RS.getInt("color3");
					final String access = RS.getString("accessories");
					final int extraClip = RS.getInt("extraClip");
					final int customArtWork = RS.getInt("customArtWork");
					final int initQId = RS.getInt("initQuestion");
					final String ventes = RS.getString("ventes");
					World.addNpcTemplate
					(
						new NpcTemplate
						(
							id,
							bonusValue,
							gfxID,
							scaleX,
							scaleY,
							sex,
							color1,
							color2,
							color3,
							access,
							extraClip,
							customArtWork,
							initQId,
							ventes
						)
					);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
		}
	}

	public static void SAVE_NEW_ITEM(final Item item)
	{
		synchronized(syncLock)
		{
			try {
				final String baseQuery = "REPLACE INTO `items` VALUES(?,?,?,?,?);";
				
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				
				p.setInt(1,item.getGuid());
				p.setInt(2,item.getTemplate().getID());
				p.setInt(3,item.getQuantity());
				p.setInt(4,item.getPosition());
				p.setString(5,item.parseToSave());
				
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void SAVE_NEW_ITEM_SPEAKING(final Item obj) 
	{
		synchronized(syncLock)
		{
			try {
				final String baseQuery = "REPLACE INTO `items_speaking` VALUES(?,?,?,?,?,?,?,?,?);";

				final PreparedStatement p = newTransact(baseQuery, serverConn);

				final Speaking item = Speaking.toSpeaking(obj);

				p.setInt(1, item.getGuid());
				p.setInt(2, item.getSelectedLevel());
				p.setInt(3, item.getType());
				p.setInt(4, item.getLvl());
				p.setInt(5, item.getXp());
				p.setInt(6, item.getState());
				p.setInt(7, item.getMealsXp());
				p.setString(8, item.getLastEat());
				p.setInt(9, item.getLinkedID());

				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void SAVE_NEW_ITEM_PETS(final Item obj) 
	{
		synchronized(syncLock)
		{
			try {
				final String baseQuery = "REPLACE INTO `items_pets` VALUES(?,?,?,?,?,?,?,?,?,?,?,?);";
				final PreparedStatement p = newTransact(baseQuery, serverConn);

				final Pet item = (Pet)obj;

				p.setInt(1, item.getGuid());
				p.setInt(2, item.getTemplate().getID());
				p.setString(3, item.parseStatsString());
				p.setString(4, item.getLastTimeAte());
				p.setInt(5, item.getNbMeal());
				p.setInt(6, item.getMissMeal());
				p.setInt(7, item.getLastFoodId());
				p.setInt(8, item.getState());
				p.setInt(9, item.getType());
				p.setString(10, item.parseSoulToDB());
				p.setInt(11, item.getLife());
				p.setInt(12, item.isHasStatsBoosted() ? 1 : 0);
				
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static boolean SAVE_NEW_FIXGROUP(final int mapID,final int cellID,final String groupData)
	{
		synchronized(syncLock)
		{
			try 
			{
				final String baseQuery = "REPLACE INTO `mobgroups_fix` VALUES(?,?,?)";
				final PreparedStatement p = newTransact(baseQuery, commonConn);
				
				p.setInt(1, mapID);
				p.setInt(2, cellID);
				p.setString(3, groupData);
				
				p.execute();
				closePreparedStatement(p);
				
				return true;
			} catch (final SQLException e) 
			{
				e.printStackTrace();
			}
			return false;
		}
	}
	public static void LOAD_NPC_QUESTIONS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * FROM npc_questions;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					World.addNPCQuestion
					(
						new NpcQuestion
						(
							RS.getInt("ID"),
							RS.getString("responses"),
							RS.getString("params"),
							RS.getString("cond"),
							RS.getInt("ifFalse")
						)
					);
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
		}
	}

	public static void LOAD_NPC_ANSWERS()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * FROM npc_reponses_actions;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					final int id = RS.getInt("ID");
					final int type = RS.getInt("type");
					final String args = RS.getString("args");
					if(World.getNPCreponse(id) == null)
						World.addNPCreponse(new NpcResponse(id));
					World.getNPCreponse(id).addAction(new Action(type,args,""));
					
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
		}
	}
	
	public static int LOAD_ENDFIGHT_ACTIONS()
	{
		synchronized(syncLock)
		{
			int nbr = 0;
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * FROM endfight_action;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					final DofusMap map = World.getMap(RS.getInt("map"));
					if(map == null)continue;
					map.addEndFightAction(RS.getInt("fighttype"),
							new Action(RS.getInt("action"),RS.getString("args"),RS.getString("cond")));
					nbr++;
				}
				closeResultSet(RS);
				return nbr;
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
			return nbr;
		}
	}
	
	public static int LOAD_ITEM_ACTIONS()
	{
		synchronized(syncLock)
		{
			int nbr = 0;
			try
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * FROM use_item_actions;",Config.COMMON_DB_NAME);
				while(RS.next())
				{
					final int id = RS.getInt("template");
					final int type = RS.getInt("type");
					final String args = RS.getString("args");
					if(World.getItemTemplate(id) == null)continue;
					World.getItemTemplate(id).addAction(new Action(type,args,""));
					nbr++;
				}
				closeResultSet(RS);
				return nbr;
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				System.exit(1);
			}
			return nbr;
		}
	}
	
	public static int LOAD_ITEMS() 
	{
		synchronized(syncLock)
		{
			short errCount = 0;
			try {
				final ResultSet RS = executeQuery("SELECT * FROM items;", Config.SERVER_DB_NAME);

				while (RS.next()) {
					final int guid = RS.getInt("guid");
					final int tempID = RS.getInt("template");
					final int qua = RS.getInt("qua");
					final int pos = RS.getInt("pos");
					final String stats = RS.getString("stats");
					final Item loaded = new Item(guid, tempID, qua, pos, stats);
					final int type = loaded.getTemplate().getType();
					if (type == 113) {
						final Item item = LOAD_SPEAKING(loaded);
						if (item != null)
							World.addItem(item, false);
						else
							Log.addToErrorLog("SQL Erreur : LOAD_ITEMS() => Item Speaking null");
					} else if (type == Constants.ITEM_TYPE_FAMILIER 
							|| type == Constants.ITEM_TYPE_FANTOME_FAMILIER
							|| type == Constants.ITEM_TYPE_CERTIFICAT_CHANIL) {
						final Item item = LOAD_PET(loaded);
						if (item != null)
							World.addItem(item, false);
						else
							errCount++;
					} else {
						World.addItem(loaded, false);
					}
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
			return errCount;
		}
	}
	
	public static void LOAD_ITEMS(final String ids)
	{
		synchronized(syncLock)
		{
			final String req = "SELECT * FROM items WHERE guid IN ("+ids+");";
			try
			{
				final ResultSet RS = SQLManager.executeQuery(req,Config.SERVER_DB_NAME);
				while(RS.next())
				{
					final int guid 	  = RS.getInt("guid");
					final int tempID  = RS.getInt("template");
					final int qua 	  = RS.getInt("qua");
					final int pos	  = RS.getInt("pos");
					final String stats= RS.getString("stats");
					final Item loaded = World.newObjet(guid, tempID, qua, pos, stats);
					if(loaded == null)
						continue;
					final int type    = loaded.getTemplate().getType();
					if (type == 113) {
						final Item item = LOAD_SPEAKING(loaded);
						if (item != null)
							World.addItem(item, false);
						else
							Log.addToErrorLog("Erreur : Objet Speaking null");
					} else if (type == Constants.ITEM_TYPE_FAMILIER || type == Constants.ITEM_TYPE_FANTOME_FAMILIER	|| type == Constants.ITEM_TYPE_CERTIFICAT_CHANIL) {
						final Item item = LOAD_PET(loaded);
						if (item != null)
							World.addItem(item, false);
						else
							Log.addToErrorLog("Erreur : Objet Pet null");
					} else {
						World.addItem(loaded, false);
					}
				}
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Console.printError("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
				Console.printError("Query: \n"+req);
				System.exit(1);
			}
		}
	}
	
	public static Speaking LOAD_SPEAKING(final Item obj) 
	{
		synchronized(syncLock)
		{
			final String req = "SELECT * FROM items_speaking WHERE `guid`= '" + obj.getGuid() + "';";
			try {
				final ResultSet RS = executeQuery(req, Config.SERVER_DB_NAME);
				if (RS.next()) {
					final int skin = RS.getInt("skin");
					final int lvl = RS.getInt("lvl");
					final int xp = RS.getInt("xp");
					final int meals = RS.getInt("state");
					final int winXP = RS.getInt("winXp");
					final String lastEat = RS.getString("lastEat");
					final int linked = RS.getInt("linked");
					final int type = RS.getInt("type");
					final Speaking loaded = Speaking.loadSpeakingItem(obj, skin, lvl, xp, meals, winXP, lastEat, linked, type);
					return loaded;
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Console.printError("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
				Console.printError("Requete: \n" + req);
				System.exit(1);
			}
			return null;
		}
	}
	
	public static Pet LOAD_PET(final Item obj)
	{
		synchronized(syncLock)
		{
			final String req = "SELECT * FROM items_pets WHERE `guid` = '" + obj.getGuid() + "';";
			try {
				final ResultSet RS = SQLManager.executeQuery(req, Config.SERVER_DB_NAME);
				while (RS.next()) {
					final String lastEat = RS.getString("lastTimeAte");
					final byte nbMeal = (byte) RS.getInt("nbMeal");
					final byte missMeal = (byte) RS.getInt("missMeal");
					final int lastFoodId = RS.getInt("lastFoodId");
					final byte state = (byte) RS.getInt("state");
					final String soulsEatten = RS.getString("soulsEatten");
					final int type = RS.getInt("type");
					final String stats = RS.getString("stats");
					final int life = RS.getInt("life");
					final byte surBoost = (byte) RS.getInt("surBoost");
					final Pet loaded = Pet.load_PetItem(obj, nbMeal, missMeal, lastFoodId, state, lastEat, soulsEatten, type, stats, life, surBoost);
					return loaded;
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Console.printError("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
				Console.printError("Query: " + req);
				System.exit(1);
			}
			return null;
		}
	}
	
	public static void LOAD_HDVS()
	{
		synchronized(syncLock)
		{
			try
			{
				ResultSet RS = executeQuery("SELECT * FROM `hdvs` ORDER BY id ASC",Config.SERVER_DB_NAME);
				
				while(RS.next())
				{
					World.addHdv(new BigStore(
									RS.getInt("map"),
									RS.getFloat("sellTaxe"),
									RS.getShort("sellTime"),
									RS.getShort("accountItem"),
									RS.getShort("lvlMax"),
									RS.getString("categories")));
					
				}
				
				RS = executeQuery("SELECT id MAX FROM `hdvs`",Config.SERVER_DB_NAME);
				RS.first();
				World.setNextHdvID(RS.getInt("MAX"));
				
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void LOAD_HDVS_ITEMS()
	{
		synchronized(syncLock)
		{
			try
			{
				//long time1 = System.currentTimeMillis();	//TIME
				ResultSet RS = null;
				/*ResultSet RS = executeQuery("SELECT i.*"+ //TODO //TEST
						" FROM `items` AS i,`hdvs_items` AS h"+
						" WHERE i.guid = h.itemID",Ancestra.OTHER_DB_NAME);
				
				//Load items
				while(RS.next())
				{
					int guid 	= RS.getInt("guid");
					int tempID 	= RS.getInt("template");
					int qua 	= RS.getInt("qua");
					int pos		= RS.getInt("pos");
					String stats= RS.getString("stats");
					World.addObjet
					(
						World.newObjet
						(
							guid,
							tempID,
							qua,
							pos,
							stats
						),
						false
					);
				}*/
				
				//Load HDV entry
				RS = executeQuery("SELECT * FROM `hdvs_items`",Config.SERVER_DB_NAME);
				while(RS.next())
				{
					final BigStore tempHdv = World.getHdv(RS.getInt("map"));
					if(tempHdv == null)continue;
					
					final Item obj = World.getObjet(RS.getInt("itemID"));
					
					if (obj == null || obj.isSpeaking() && Speaking.toSpeaking(obj).getLinkedID() > 0)
						continue;
					
					tempHdv.addEntry(new BigStoreEntry(
											RS.getInt("price"),
											RS.getByte("count"),
											RS.getInt("ownerGuid"),
											obj));
				}
				//System.out.println (System.currentTimeMillis() - time1 + "ms pour loader les HDVS items");	//TIME
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void DELETE_ITEM(final int guid)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM items WHERE guid = ?;";
			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1, guid);
				
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
		}
	}
	
	public static void DELETE_SPEAKING_ITEM(final int guid)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM items_speaking WHERE guid = ?;";
			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1, guid);

				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
				Log.addToErrorLog("Query: " + baseQuery);
			}
		}
	}

	public static void CREATE_MOUNT(final Mount DD)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "REPLACE INTO `mounts_data`(`id`,`color`,`sexe`,`name`,`xp`,`level`," +
					"`endurance`,`amour`,`maturite`,`serenite`,`reproductions`,`fatigue`,`items`," +
					"`ancetres`,`energie`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
			
			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1,DD.getId());
				p.setInt(2,DD.getColor());
				p.setInt(3,DD.getSex());
				p.setString(4,DD.getName());
				p.setLong(5,DD.getExp());
				p.setInt(6,DD.getLevel());
				p.setInt(7,DD.getStamina());
				p.setInt(8,DD.getLove());
				p.setInt(9,DD.getMaturity());
				p.setInt(10,DD.getSerenity());
				p.setInt(11,DD.getReprod());
				p.setInt(12,DD.getTired());
				p.setString(13,DD.getItemsId());
				p.setString(14,DD.getAncestors());
				p.setInt(15,DD.getEnergy());
				
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
		}
	}
	public static void SAVE_ITEM(final Item item)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "REPLACE INTO `items` VALUES (?,?,?,?,?);";
			
			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1, item.getGuid());
				p.setInt(2, item.getTemplate().getID());
				p.setInt(3, item.getQuantity());
				p.setInt(4, item.getPosition());
				p.setString(5,item.parseToSave());
				
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}	
		}
	}
	
	public static void UPDATE_SPEAKING_ITEM(final Item obj)
	{
		synchronized(syncLock)
		{
			try {
				final String baseQuery = "UPDATE `items_speaking` SET skin = ?, lvl= ?, xp = ?, state= ?, winXp= ?, lastEat= ?, linked= ?, type= ? WHERE guid = ?;";

				final PreparedStatement p = newTransact(baseQuery, serverConn);

				final Speaking item = Speaking.toSpeaking(obj);

				p.setInt(1, item.getSelectedLevel());
				p.setInt(2, item.getLvl());
				p.setInt(3, item.getXp());
				p.setInt(4, item.getState());
				p.setInt(5, item.getMealsXp());
				p.setString(6, item.getLastEat());
				p.setInt(7, item.getLinkedID());
				p.setInt(8, item.getType());

				p.setInt(9, item.getGuid());
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void UPDATE_SPEAKING(final Speaking item) 
	{
		synchronized(syncLock)
		{
			try {
				final String baseQuery = "UPDATE `items_speaking` SET skin = ?, lvl= ?, xp = ?, state= ?, winXp= ?, lastEat= ?, linked= ?, type= ? WHERE guid = ?;";

				final PreparedStatement p = newTransact(baseQuery, serverConn);

				p.setInt(1, item.getSelectedLevel());
				p.setInt(2, item.getLvl());
				p.setInt(3, item.getXp());
				p.setInt(4, item.getState());
				p.setInt(5, item.getMealsXp());
				p.setString(6, item.getLastEat());
				p.setInt(7, item.getLinkedID());
				p.setInt(8, item.getType());

				p.setInt(9, item.getGuid());
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void LOAD_ACCOUNT_BY_GUID(final int user)
	{
		try
		{
			final ResultSet RS = SQLManager.executeQuery("SELECT * from accounts WHERE `guid` = '"+user+"';",Config.SERVER_DB_NAME);
			
			final String baseQuery = "UPDATE accounts " +
								"SET `reload_needed` = 0 " +
								"WHERE guid = ?;";
			final PreparedStatement p = newTransact(baseQuery, loginConn);
			
			while(RS.next())
			{
				//Si le compte est déjà connecté, on zap
				if(World.getAccount(RS.getInt("guid")) != null)if(World.getAccount(RS.getInt("guid")).isOnline())continue;
				
				final Account C = new Account(
						RS.getInt("guid"), 
						RS.getString("account"), 
						RS.getString("pass"), 
						RS.getString("pseudo"), 
						RS.getInt("level"), 
						RS.getString("question"), 
						RS.getString("reponse"), 
						RS.getString("lastConnectionDate"), 
						RS.getString("lastIP"),
						RS.getString("curIP"), 
						RS.getInt("mute") == 1,
						RS.getInt("vip") == 1,
						RS.getString("friends"),
						RS.getString("enemies"),
						RS.getString("stable"),
						RS.getString("bank"),
						RS.getLong("bankKamas")
						);
				World.addAccount(C);
				World.reassignAccountToChar(C);
				
				p.setInt(1, RS.getInt("guid"));
				p.executeUpdate();
			}
			
			closePreparedStatement(p);
			closeResultSet(RS);
		}catch(final SQLException e)
		{
			Log.addToErrorLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}
	public static int LOAD_ACCOUNT_BY_PERSO(final String persoName)
	{
		try
		{
			final ResultSet RS = SQLManager.executeQuery("SELECT account from personnages WHERE `name` LIKE '"+persoName+"';",Config.SERVER_DB_NAME);
			
			int accID = -1;
			final boolean found = RS.first();
			
			if(found)
				accID = RS.getInt("account");
			
			if(accID != -1)
				LOAD_ACCOUNT_BY_GUID(accID);
			
			closeResultSet(RS);
			return accID;
		}catch(final SQLException e)
		{
			Log.addToErrorLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
		return -1;
	}
	private static void closeResultSet(final ResultSet RS)
	{
		try {
			RS.getStatement().close();
			RS.close();
		} catch (final SQLException e) {e.printStackTrace();}

		
	}
	private static void closePreparedStatement(final PreparedStatement p)
	{
		try {
			p.clearParameters();
			p.close();
		} catch (final SQLException e) {e.printStackTrace();}
	}

	/*public static Account LOAD_ACCOUNT_BY_USER(final String user)
	{
		try
		{
			final ResultSet RS = SQLManager.executeQuery("SELECT * from accounts WHERE `account` LIKE '"+user+"';",Config.SERVER_DB_NAME);
			
			final String baseQuery = "UPDATE accounts " +
								"SET `reload_needed` = 0 " +
								"WHERE guid = ?;";
			final PreparedStatement p = newTransact(baseQuery, serverConn);
			
			Account C = null;
			while(RS.next())
			{
				//Si le compte est déjà connecté, on zap
				if(World.getCompte(RS.getInt("guid")) != null)
					if(World.getCompte(RS.getInt("guid")).isOnline())
						continue;
				
				C = new Account(
						RS.getInt("guid"),
						RS.getString("account"),
						RS.getString("pass"),
						RS.getString("pseudo"),
						RS.getString("question"),
						RS.getString("reponse"),
						RS.getInt("level"),
						(RS.getInt("banned") == 1),
						RS.getString("lastIP"),
						RS.getString("lastConnectionDate"),
						RS.getString("bank"),
						RS.getInt("bankKamas"),
						RS.getString("friends"),
						RS.getString("stable"),
						RS.getString("enemys"), 
						(RS.getInt("seeFriend") == 1)
						);
				World.addAccount(C);
				World.reassignAccountToChar(C);
				
				p.setInt(1, RS.getInt("guid"));
				p.executeUpdate();
			}
			
			closePreparedStatement(p);
			closeResultSet(RS);
			return C;
		}catch(final SQLException e)
		{
			Log.addToErrorLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
		return null;
	}*/

	public static void UPDATE_LASTCONNECTION_INFO(final Account compte)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "UPDATE accounts SET " +
					"`lastIP` = ?," +
					"`lastConnectionDate` = ?" +
					" WHERE `guid` = ?;";

			try
			{
				final PreparedStatement p = newTransact(baseQuery, loginConn);

				p.setString(1, compte.getCurIP());
				p.setString(2, compte.getLastConnectionDate());
				p.setInt(3, compte.getGUID());

				p.executeUpdate();
				closePreparedStatement(p);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
				e.printStackTrace();
			}
		}
	}
	
	public static void UPDATE_MOUNT_INFOS(final Mount DD)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "UPDATE mounts_data SET " +
					"`name` = ?," +
					"`xp` = ?," +
					"`level` = ?," +
					"`endurance` = ?," +
					"`amour` = ?," +
					"`maturite` = ?," +
					"`serenite` = ?," +
					"`reproductions` = ?," +
					"`fatigue` = ?," +
					"`energie` = ?," +
					"`ancetres` = ?," +
					"`items` = ?" +
					" WHERE `id` = ?;";

			try
			{
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setString(1,DD.getName());
				p.setLong(2,DD.getExp());
				p.setInt(3,DD.getLevel());
				p.setInt(4,DD.getStamina());
				p.setInt(5,DD.getLove());
				p.setInt(6,DD.getMaturity());
				p.setInt(7,DD.getSerenity());
				p.setInt(8,DD.getReprod());
				p.setInt(9,DD.getTired());
				p.setInt(10,DD.getEnergy());
				p.setString(11,DD.getAncestors());
				p.setString(12,DD.getItemsId());
				p.setInt(13,DD.getId());

				p.execute();
				closePreparedStatement(p);

			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
				e.printStackTrace();
			}
		}
	}

	public static void SAVE_MOUNTPARK(final MountPark MP)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "REPLACE INTO `mountpark_data`( `mapid` , `size` , `owner` , `guild` , `price` , `data` )" +
					" VALUES (?,?,?,?,?,?);";
					
			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1,MP.getMap().getId());
				p.setInt(2,MP.getSize());
				p.setInt(3,MP.getOwner());
				p.setInt(4,(MP.getGuild()==null?-1:MP.getGuild().get_id()));
				p.setInt(5,MP.getPrice());
				p.setString(6,MP.parseData());
				
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
		}
	}

	public static boolean SAVE_TRIGGER(final int mapID1, final int cellID1, final int action, final int event,final String args, final String cond)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "REPLACE INTO `scripted_cells`" +
					" VALUES (?,?,?,?,?,?);";
			
			try {
				final PreparedStatement p = newTransact(baseQuery, commonConn);
				p.setInt(1,mapID1);
				p.setInt(2,cellID1);
				p.setInt(3,action);
				p.setInt(4,event);
				p.setString(5,args);
				p.setString(6,cond);
				
				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
			return false;
		}
	}
	
	public static boolean REMOVE_TRIGGER(final int mapID, final int cellID)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM `scripted_cells` WHERE "+
					"`MapID` = ? AND "+
					"`CellID` = ?;";
			try {
				final PreparedStatement p = newTransact(baseQuery, commonConn);
				p.setInt(1, mapID);
				p.setInt(2, cellID);

				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
			return false;
		}
	}
	
	public static boolean SAVE_MAP_DATA(final DofusMap map)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "UPDATE `maps` SET "+
					"`places` = ?, "+
					"`numgroup` = ? "+
					"WHERE id = ?;";

			try {
				final PreparedStatement p = newTransact(baseQuery, commonConn);
				p.setString(1,map.getPlacesStr());
				p.setInt(2, map.getMaxGroupNumb());
				p.setInt(3, map.getId());

				p.executeUpdate();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
			return false;
		}
	}
	public static boolean DELETE_NPC_ON_MAP(final int m,final int c)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM npcs WHERE mapid = ? AND cellid = ?;";
			try {
				final PreparedStatement p = newTransact(baseQuery, commonConn);
				p.setInt(1, m);
				p.setInt(2, c);
				
				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
			return false;
		}
	}
	public static boolean ADD_NPC_ON_MAP(final int m,final int id,final int c,final int o)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "INSERT INTO `npcs`" +
					" VALUES (?,?,?,?);";
			try {
				final PreparedStatement p = newTransact(baseQuery, commonConn);
				p.setInt(1, m);
				p.setInt(2, id);
				p.setInt(3, c);
				p.setInt(4, o);
				
				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
			return false;
		}
	}

	public static boolean ADD_ENDFIGHTACTION(final int mapID, final int type, final int Aid,final String args,final String cond)
	{
		synchronized(syncLock)
		{
			if(!DEL_ENDFIGHTACTION(mapID,type,Aid))return false;
			final String baseQuery = "INSERT INTO `endfight_action` " +
					"VALUES (?,?,?,?,?);";
			try {
				final PreparedStatement p = newTransact(baseQuery, commonConn);
				p.setInt(1, mapID);
				p.setInt(2, type);
				p.setInt(3, Aid);
				p.setString(4,args);
				p.setString(5, cond);
				
				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
			return false;
		}
	}

	public static boolean DEL_ENDFIGHTACTION(final int mapID, final int type, final int aid)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM `endfight_action` " +
					"WHERE map = ? AND " +
					"fighttype = ? AND " +
					"action = ?;";
			try {
				final PreparedStatement p = newTransact(baseQuery, commonConn);
				p.setInt(1, mapID);
				p.setInt(2, type);
				p.setInt(3, aid);
				
				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
				return false;
			}
		}
	}

	public static void SAVE_NEWGUILD(final Guild g)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "INSERT INTO `guilds` " +
					"VALUES (?,?,?,1,0);";
			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1, g.get_id());
				p.setString(2, g.getName());
				p.setString(3, g.getEmblem());
				
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
		}
	}
	public static void DEL_GUILD(final int id)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM `guilds` " +
					"WHERE `id` = ?;";
			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1, id);
				
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
		}
	}
	public static void DEL_GUILDMEMBER(final int id)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM `guild_members` " +
					"WHERE `guid` = ?;";
			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1, id);
				
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
		}
	}
	public static void UPDATE_GUILD(final Guild g)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "UPDATE `guilds` SET "+
					"`lvl` = ?,"+
					"`xp` = ?," +
					"`capital` = ?," +
					"`nbrPercoMax` = ?," +
					"`sorts` = ?," +
					"`stats` = ?" +
					" WHERE id = ?;";

			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1, g.getLvl());
				p.setInt(2, g.get_id());
				p.setInt(3, g.getCapital());
				p.setInt(4, g.getNbrTaxCollector());
				p.setString(5, g.compileSpell());
				p.setString(6, g.compileStats());
				p.setLong(7, g.getXp());

				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
		}
	}
	public static void UPDATE_GUILDMEMBER(final GuildMember gm)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "REPLACE INTO `guild_members` " +
					"VALUES(?,?,?,?,?,?,?,?,?,?,?);";

			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1,gm.getGuid());
				p.setInt(2,gm.getGuild().get_id());
				p.setString(3,gm.getName());
				p.setInt(4,gm.getLvl());
				p.setInt(5,gm.getGfx());
				p.setInt(6,gm.getRank());
				p.setLong(7,gm.getXpGave());
				p.setInt(8,gm.getPXpGive());
				p.setInt(9,gm.getRights());
				p.setInt(10,gm.getAlign());
				p.setString(11,gm.getLastCo());

				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
		}
	}
	public static int isPersoInGuild(final int guid)
	{
		synchronized(syncLock)
		{
			int guildId = -1;
			
			try
			{
				final ResultSet GuildQuery = SQLManager.executeQuery("SELECT guild FROM `guild_members` WHERE guid="+guid+";", Config.SERVER_DB_NAME);
				
				final boolean found = GuildQuery.first();
				
				if(found)
					guildId = GuildQuery.getInt("guild");
				
				closeResultSet(GuildQuery);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
			
			return guildId;
		}
	}
	public static int[] isPersoInGuild(final String name)
	{
		synchronized(syncLock)
		{
			int guildId = -1;
			int guid = -1;
			try
			{
				final ResultSet GuildQuery = SQLManager.executeQuery("SELECT guild,guid FROM `guild_members` WHERE name='"+name+"';", Config.SERVER_DB_NAME);
				final boolean found = GuildQuery.first();
				
				if(found)
				{
					guildId = GuildQuery.getInt("guild");
					guid = GuildQuery.getInt("guid");
				}
				
				closeResultSet(GuildQuery);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
			final int[] toReturn = {guid,guildId};
			return toReturn;
		}
	}
	
	public static boolean ADD_REPONSEACTION(final int repID, final int type, final String args)
	{
		synchronized(syncLock)
		{
			String baseQuery = "DELETE FROM `npc_reponses_actions` " +
					"WHERE `ID` = ? AND " +
					"`type` = ?;";
			PreparedStatement p; 
			try {
				p = newTransact(baseQuery, commonConn);
				p.setInt(1, repID);
				p.setInt(2, type);

				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
			baseQuery = "INSERT INTO `npc_reponses_actions` " +
					"VALUES (?,?,?);";
			try {
				p = newTransact(baseQuery, commonConn);
				p.setInt(1, repID);
				p.setInt(2, type);
				p.setString(3, args);

				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
			return false;
		}
	}

	public static boolean UPDATE_INITQUESTION(final int id, final int q)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "UPDATE `npc_template` SET " +
					"`initQuestion` = ? " +
					"WHERE `id` = ?;";
			try {
				final PreparedStatement p = newTransact(baseQuery, commonConn);
				p.setInt(1, q);
				p.setInt(2, id);

				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
			return false;
		}
	}

	public static boolean UPDATE_NPCREPONSES(final int id, final String reps)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "UPDATE `npc_questions` SET " +
					"`responses` = ? " +
					"WHERE `ID` = ?;";
			try {
				final PreparedStatement p = newTransact(baseQuery, commonConn);
				p.setString(1, reps);
				p.setInt(2, id);

				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				Log.addToErrorLog("Query: "+baseQuery);
			}
			return false;
		}
	}
	
	/*-------------LIGNE PAR MARTHIEUBEAN------------------*/
	//Ajoute des points (pour le site internet) au compte
	public static void ADD_POINT(final int _nombre,final Account _compte)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "UPDATE accounts" +
					" SET point = point + ?" +
					" WHERE guid = ?";
			try
			{
				final PreparedStatement p = newTransact(baseQuery, loginConn);
				
				p.setInt(1, _nombre);
				p.setInt(2, _compte.getGUID());
				
				p.executeUpdate();
				closePreparedStatement(p);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	/*----------------------FIN----------------------------*/
	
	/*MARTHIEUBEAN*/
	
	public static void SAVE_HDVS_ITEMS(final ArrayList<BigStoreEntry> liste)
	{
		synchronized(syncLock)
		{
			final long time1 = System.currentTimeMillis();	//TIME
			PreparedStatement queries = null;
			try
			{
				final String emptyQuery = "TRUNCATE TABLE `hdvs_items`";
				final PreparedStatement emptyTable = newTransact(emptyQuery, serverConn);
				emptyTable.execute();
				
				final String baseQuery = "INSERT INTO `hdvs_items` "+
									"(`map`,`ownerGuid`,`price`,`count`,`itemID`) "+
									"VALUES(?,?,?,?,?);";
				queries = newTransact(baseQuery, serverConn);
				
				for(final BigStoreEntry curEntry : liste)
				{
					if(curEntry.getOwner() == -1)continue;
					queries.setInt(1, curEntry.getHdvID());
					queries.setInt(2, curEntry.getOwner());
					queries.setInt(3, curEntry.getPrice());
					queries.setInt(4, curEntry.getAmount(false));
					queries.setInt(5, curEntry.getObjet().getGuid());
					
					queries.execute();
				}
				
				closePreparedStatement(queries);
				SAVE_HDV_AVGPRICE();
				System.out.println("Sauvegarde HDV en "+(System.currentTimeMillis()-time1)+"ms");	//TIME
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR:"+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void SAVE_HDV_AVGPRICE()
	{
		synchronized(syncLock)
		{
			final String baseQuery = "UPDATE `item_template`"+
					" SET sold = ?,avgPrice = ?"+
					" WHERE id = ?;";
			PreparedStatement queries = null;

			final Map<Integer, ItemTemplate> templates = World.getObjTemplates();
			try
			{
				queries = newTransact(baseQuery, commonConn);

				for(final ItemTemplate curTemp : templates.values())
				{
					if(curTemp.getSold() == 0)
						continue;

					queries.setLong(1, curTemp.getSold());
					queries.setInt(2, curTemp.getAvgPrice());
					queries.setInt(3, curTemp.getID());
					queries.executeUpdate();
				}
				closePreparedStatement(queries);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR:"+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	public static void TIMER(final boolean start)
	{
		if(start)
		{
			timerCommit = new Timer();
			timerCommit.schedule(new TimerTask() {
				
				public void run() {
					if(!needCommit)return;
					
					commitTransacts();
					needCommit = false;
					
				}
			}, Config.CONFIG_DB_COMMIT, Config.CONFIG_DB_COMMIT);
		}
		else
			timerCommit.cancel();
	}
	
	public static int getNextObjetID()
	{
		synchronized(syncLock)
		{
			try
			{
				final ResultSet RS = executeQuery("SELECT MAX(guid) AS max FROM items;",Config.SERVER_DB_NAME);
				
				int guid = 0;
				final boolean found = RS.first();
				
				if(found)
					guid = RS.getInt("max");
				
				closeResultSet(RS);
				return guid;
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
				Main.closeServers();
			}
			return 0;
		}
	}
	
	public static boolean needReloadAccount(final String login)
	{
		boolean reload_needed = false;
		try
		{
			final String query = "SELECT reload_needed " +
							"FROM accounts " +
							"WHERE account LIKE '" + login + "'" +
							";";
			final ResultSet RS = executeQuery(query,Config.LOGIN_DB_NAME);
			
			final boolean found = RS.first();
			
			if(found)
			{
				if(RS.getInt("reload_needed") == 1)
					reload_needed = true;
			}
			
			closeResultSet(RS);
		}catch(final SQLException e)
		{
			Log.addToErrorLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
			Main.closeServers();
		}
		return reload_needed;
	}
	public static boolean persoExist(String name)
	{
		synchronized(syncLock)
		{
			boolean exist = false;
			try
			{
				String query = "SELECT COUNT(*) AS exist FROM personnages WHERE name LIKE '" + name + "';";
				
				ResultSet RS = executeQuery(query,Config.SERVER_DB_NAME);
				
				boolean found = RS.first();
				
				if(found)
				{
					if(RS.getInt("exist") != 0)
						exist = true;
				}
				
				closeResultSet(RS);
			}catch(final SQLException e)
			{
				Log.addToErrorLog("SQL ERROR: "+e.getMessage());
				e.printStackTrace();
			}
			return exist;
		}
	}
	
	public static boolean LOAD_GIFT_BY_ACCOUNT(final Account C) 
	{
		synchronized(syncLock)
		{
			try {
				C.deleteGifts();
				final ResultSet RS = SQLManager.executeQuery("SELECT * from gifts WHERE account_id = " + C.getGUID() + " ORDER BY id;", Config.SERVER_DB_NAME);

				while (RS.next()) {
					final int lineID = RS.getInt("id");
	                final int giftID = RS.getInt("gift_id");
					if (!World.giftContain(giftID)) {
						REMOVE_GIFT(lineID);
					} else {
						C.addGift(lineID, World.getGift(giftID));
					}
				}
				closeResultSet(RS);
				return true;
			} catch (final SQLException e) {
			}
			return false;
		}
	}

	public static boolean LOAD_GIFTS()
	{
		synchronized(syncLock)
		{
			try {
				final ResultSet RS = SQLManager.executeQuery("SELECT * from gifts;", Config.SERVER_DB_NAME);

				while (RS.next()) {
					final Gift newGift = new Gift(
							RS.getInt("id"), 
							RS.getString("title"), 
							RS.getString("desc"), 
							RS.getString("items"), 
							RS.getString("URL"), 
							RS.getInt("cost"));
					World.addGift(newGift);
				}
				closeResultSet(RS);
				return true;
			} catch (final SQLException e) {
				
			}
			return false;
		}
	}

	public static boolean REMOVE_GIFT(final int id)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM gifts WHERE id = ?;";

			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1, id);

				p.execute();
				closePreparedStatement(p);
				return true;
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				Log.addToErrorLog("Query: " + baseQuery);
			}
			return false;
		}
	}

	public static void DELETE_MOUNT(final Player perso)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM mounts_data WHERE id = ?";
			PreparedStatement p;
			try {
				p = newTransact(baseQuery, serverConn);
				p.setInt(1, perso.getMount().getId());
				p.execute();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			World.delDragoByID(perso.getMount().getId());
			perso.setMountGiveXp(0);
			perso.setMount(null);
			SAVE_PERSONNAGE(perso, true);
		}
    }
	
	public static void LOAD_PERCEPTEURS() 
	{
		synchronized(syncLock)
		{
			try {
				final ResultSet RS = SQLManager.executeQuery("SELECT * from percepteurs;", Config.SERVER_DB_NAME);
				while (RS.next()) {
					final DofusMap map = World.getMap(RS.getInt("mapid"));
					if (map == null)
						continue;

					World.addTaxCollector(new TaxCollector(
							RS.getInt("guid"), 
							RS.getShort("nom1"), 
							RS.getShort("nom2"), 
							RS.getInt("guildId"), 
							RS.getInt("mapId"), 
							RS.getInt("cellId"), 
							RS.getByte("orientation"), 
							RS.getString("objets"), 
							RS.getLong("kamas"), 
							RS.getLong("xp")));
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void DELETE_PERCEPTEUR(final int id)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM percepteurs WHERE id = ?";
			PreparedStatement p;
			try {
				p = newTransact(baseQuery, serverConn);
				p.setInt(1, id);
				p.execute();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			
			World.getTaxCollector(id).remove();
			World.delTaxCollector(id);
		}
    }

	public static void ADD_PERCEPTEUR(final int guid, final short nom1, final short nom2, final int guildId, final int mapId, final int cellId, final byte orientation) 
	{
		synchronized(syncLock)
		{
			final String baseQuery = "INSERT INTO `percepteurs` VALUES (?,?,?,?,?,?,?,?,?,?);";
			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setInt(1, guid);
				p.setShort(2, nom1);
				p.setShort(3, nom2);
				p.setInt(4, guildId);
				p.setInt(5, mapId);
				p.setInt(6, cellId);
				p.setByte(7, orientation);
				p.setString(8, "");
				p.setLong(9, 0);
				p.setLong(10, 0);
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				Log.addToErrorLog("Query: " + baseQuery);
				return;
			}
		}
	}
	
	public static void UPDATE_PERCEPTEUR(final TaxCollector P)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "UPDATE `percepteurs` SET `objets` = ?,`kamas` = ?," + "`xp` = ? WHERE guid = ?;";

			try {
				final PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setString(1, P.parseItemToDB());
				p.setLong(2, P.getKamas());
				p.setLong(3, P.getXp());
				p.setInt(4, P.getActorId());

				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				Log.addToErrorLog("Query: " + baseQuery);
			}
		}
	}
	
	public static void UPDATE_PET_ITEM(final Item pet) 
	{
		synchronized(syncLock)
		{
			try {
				final String baseQuery = "UPDATE `pets_items` SET state = ?, lastFoodId = ?, missMeal = ?, nbMeal = ?, lastTimeAte = ?, " +
						"stats = ?, templateID = ?, type = ?, soulsEatten = ?, life = ?, surBoost = ? WHERE guid = ?;";

				final Pet item = (Pet) pet;
				final PreparedStatement p = newTransact(baseQuery, serverConn);

				p.setInt(1, item.getState());
				p.setInt(2, item.getLastFoodId());
				p.setInt(3, item.getMissMeal());
				p.setInt(4, item.getNbMeal());
				p.setString(5, item.getLastTimeAte());
				p.setString(6, item.parseStatsMapString());
				p.setInt(7, item.getTemplate().getID());
				p.setInt(8, item.getType());
				p.setString(9, item.parseSoulToDB());
				p.setInt(10, item.getLife());
				p.setInt(11, item.isHasStatsBoosted() ? 1 : 0);
				p.setInt(12, item.getGuid()); // Target
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void UPDATE_PET_ITEM(final Pet item) 
	{
		synchronized(syncLock)
		{
			try {
				final String baseQuery = "UPDATE `pets_items` SET state = ?, lastFoodId = ?, missMeal = ?, nbMeal = ?, lastTimeAte = ?, " +
				"stats = ?, templateID = ?, type = ?, soulsEatten = ?, life = ?, surBoost = ? WHERE guid = ?;";

				final PreparedStatement p = newTransact(baseQuery, serverConn);

				p.setInt(1, item.getState());
				p.setInt(2, item.getLastFoodId());
				p.setInt(3, item.getMissMeal());
				p.setInt(4, item.getNbMeal());
				p.setString(5, item.getLastTimeAte());
				p.setString(6, item.parseStatsMapString());
				p.setInt(7, item.getTemplate().getID());
				p.setInt(8, item.getType());
				p.setString(9, item.parseSoulToDB());
				p.setInt(10, item.getLife());
				p.setInt(11, item.isHasStatsBoosted() ? 1 : 0);
				p.setInt(12, item.getGuid()); // Target
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static Account GET_WAITING_ACCOUNT(final int guid) 
	{
		synchronized(syncLock)
		{
			Account acc = null;
			try {
				final ResultSet RS = executeQuery("SELECT * FROM waitings WHERE `guid`=" + guid + ";", Config.SERVER_DB_NAME);

				final String bquery = "DELETE FROM waitings WHERE `guid` = ?;";
				final PreparedStatement p = newTransact(bquery, serverConn);
				p.setInt(1, guid);
				while (RS.next()) {
					final String[] datas = RS.getString("infos").split(";");
					final int aId = Integer.parseInt(datas[0]), aGmLvl = Integer.parseInt(datas[3]);
					final String aName = datas[1], aPassword = datas[2], aIp = datas[5], aLastConnectionDate = datas[6], aQuestion = datas[7],
							aResponse = datas[8], aNickname = datas[9], aCurIp = datas[11], aFriendList = datas[12], aEnemyList = datas[13],
							aStable = datas[14], aBankItems = datas[15];
					final boolean aMute = Boolean.parseBoolean(datas[10]), aVip = Boolean.parseBoolean(datas[4]);
					final long aBankKamas = Long.parseLong(datas[16]);
					acc = new Account(aId, aName, aPassword, aNickname, aGmLvl, aQuestion, aResponse, aLastConnectionDate, aIp, 
							aCurIp, aMute, aVip, aFriendList, aEnemyList, aStable, aBankItems, aBankKamas);
				}
				p.execute();

				closeResultSet(RS);
				closePreparedStatement(p);
			} catch (final SQLException e) {
				return null;
			}
			return acc;

		}
	}

	public static void SET_CUR_IP(final String ip, final int guid)
	{
		synchronized(syncLock)
		{
			final String bquery = "UPDATE accounts SET `curIP`=? WHERE `guid`=? ;";
			try {
				final PreparedStatement p = newTransact(bquery, loginConn);
				p.setString(1, ip);
				p.setInt(2, guid);
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void SET_ONLINE(final int pID) 
	{
		synchronized(syncLock)
		{
			final String query = "UPDATE `personnages` SET logged=1 WHERE `guid`="+pID+";";
			try {
				final PreparedStatement p = newTransact(query, serverConn);
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
				Log.addToErrorLog("Query: " + query);
			}
		}
	}

	public static void SET_OFFLINE(final int pID) 
	{
		synchronized(syncLock)
		{
			final String query = "UPDATE `personnages` SET logged=0 WHERE `guid`="+pID+";";
			try {
				final PreparedStatement p = newTransact(query, serverConn);
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
				Log.addToErrorLog("Query: " + query);
			}
		}
	}
	
	public static void LOAD_MONSTERSFOLLOWERS() 
	{
		synchronized(syncLock)
		{
			try {
				final ResultSet RS = SQLManager.executeQuery("SELECT * from followers_template;", Config.COMMON_DB_NAME);
				while (RS.next()) {
					World.addMonsterFollower(new MonsterFollower(RS.getInt("mobid"), RS.getInt("itemid"), RS.getInt("turns")));
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}	
		}
	}

	public static void LOAD_QUEST_DATA() 
	{
		synchronized(syncLock)
		{
			try {
				final ResultSet RS = SQLManager.executeQuery("SELECT * from quests_data;", Config.COMMON_DB_NAME);
				while (RS.next()) {
					World.addQuest(new Quest(RS.getInt("id"),  
							RS.getString("conditions"), 
							RS.getString("steps")));
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void LOAD_QUEST_STEP()
	{
		synchronized(syncLock)
		{
			try {
				final ResultSet RS = SQLManager.executeQuery("SELECT * from quest_steps;", Config.COMMON_DB_NAME);
				while (RS.next()) {
					World.addQuestSteps(new QuestStep(RS.getInt("id"), 
							RS.getInt("dialogue"), 
							RS.getInt("gainkamas"), 
							RS.getInt("gainxp"), 
							RS.getString("gainobjet"), 
							RS.getString("objectifs")));
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void LOAD_QUEST_OBJECTIFS()
	{
		synchronized(syncLock)
		{
			try {
				final ResultSet RS = SQLManager.executeQuery("SELECT * from quest_objectifs;", Config.COMMON_DB_NAME);
				while (RS.next()) {
					World.addQuestObjectives(new QuestObjective(RS.getInt("id"), 
							RS.getInt("type"), 
							RS.getString("arguments")));
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static void LOAD_PRISMS()
	{
		synchronized(syncLock)
		{
			try 
			{
				final ResultSet RS = SQLManager.executeQuery("SELECT * from prisms;", Config.SERVER_DB_NAME);
				while (RS.next()) 
				{
					World.addPrism(new Prism(RS.getInt("Id"), 
							RS.getByte("align"), 
							RS.getByte("grade"), 
							RS.getInt("mapid"), 
							RS.getInt("cellid")
							));
					
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				e.printStackTrace();
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				Log.addToErrorLog("Query: SELECT * from prisms;");
			}
		}
	}
	
	public static void UPDATE_PRISM(final Prism prism)
	{
		synchronized(syncLock)
		{
			String baseQuery = "UPDATE `prisms` SET `align` = ? WHERE id = ?;";

			try {
				PreparedStatement p = newTransact(baseQuery, serverConn);
				p.setByte(1, prism.getAlign());
				p.setInt(4, prism.getId());
				p.execute();
				closePreparedStatement(p);
				baseQuery = "UPDATE `subarea_data` SET `alignement` = ? WHERE id = ?";
				p = newTransact(baseQuery, serverConn);
				p.setInt(1, prism.getAlign());
				p.setInt(2, World.getMap(prism.getMapId()).getSubArea().getId());
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				e.printStackTrace();
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				Log.addToErrorLog("Query: " + baseQuery);
			}
		}
	}

	public static void SAVE_PRISM(final Prism prism) 
	{
		synchronized(syncLock)
		{
			String baseQuery = "REPLACE INTO `prisms`(`id`, `align`, `grade`, `mapid`, `cellid`) VALUES (?,?,?,?,?);";
			try {
				PreparedStatement p = SQLManager.newTransact(baseQuery, SQLManager.serverConn);
				p.setInt(1, prism.getId());
				p.setByte(2, prism.getAlign());
				p.setByte(3, prism.getGrade());
				p.setInt(4, prism.getMapId());
				p.setInt(5, prism.getCellId());
				p.execute();
				closePreparedStatement(p);
				
				baseQuery = "UPDATE `subarea_data` SET `alignement` = ?, `prismId` = ? WHERE id = ?";
				p = newTransact(baseQuery, serverConn);
				p.setInt(1, prism.getAlign());
				p.setInt(2, prism.getId());
				p.setInt(3, World.getMap(prism.getMapId()).getSubArea().getId());
				p.execute();
				closePreparedStatement(p);
			} catch (final SQLException e) {
				e.printStackTrace();
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				Log.addToErrorLog("Query: " + baseQuery);
			}
		}
	}
	
	public static void DELETE_PRISM(final int id)
	{
		synchronized(syncLock)
		{
			final String baseQuery = "DELETE FROM prisms WHERE id = ?";
			PreparedStatement p;
			try {
				p = newTransact(baseQuery, serverConn);
				p.setInt(1, id);
				p.execute();
			} catch (final SQLException e) {
				e.printStackTrace();
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				Log.addToErrorLog("Query: " + baseQuery);
			}
		}
	}
	
	public static void LOAD_BREED_SPELLS()
	{
		synchronized(syncLock)
		{
			try {
				final ResultSet RS = SQLManager.executeQuery("SELECT * from breed_spells;", Config.COMMON_DB_NAME);
				while (RS.next()) 
				{
					World.addRaceSpell(new BreedSpell(
							RS.getInt("Breed"), 
							RS.getInt("Level"), 
							RS.getInt("SpellId"), 
							RS.getInt("Pos")));
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				System.exit(1);
			}
		}
	}
	
	public static void LOAD_BREEDS() 
	{
		synchronized(syncLock)
		{
			try {
				final ResultSet RS = SQLManager.executeQuery("SELECT * from breed_data;", Config.COMMON_DB_NAME);
				while (RS.next()) 
				{
					World.addBreed(new Breed(
							RS.getInt("Breed"), 
							RS.getInt("StartLife"), 
							RS.getInt("StartPA"), 
							RS.getInt("StartPM"), 
							RS.getInt("StartInitiative"), 
							RS.getInt("StartProspecting"), 
							RS.getInt("StartMap"), 
							RS.getInt("StartCell"), 
							RS.getString("Intelligence"), 
							RS.getString("Wisdom"), 
							RS.getString("Chance"),
							RS.getString("Agility"), 
							RS.getString("Strenght"), 
							RS.getString("Vitality")));
				}
				closeResultSet(RS);
			} catch (final SQLException e) {
				Log.addToErrorLog("SQL ERROR: " + e.getMessage());
				System.exit(1);
			}
		}
	}
	
}
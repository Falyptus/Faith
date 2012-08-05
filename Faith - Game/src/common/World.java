package common;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import common.command.FastCommand;
import common.console.Console;
import common.console.Log;
import common.utils.CryptManager;
import common.utils.Formulas;
import common.utils.Pathfinding;

import objects.account.Account;
import objects.alignment.Alignment;
import objects.alignment.Prism;
import objects.bigstore.BigStore;
import objects.bigstore.BigStoreEntry;
import objects.character.Breed;
import objects.character.BreedSpell;
import objects.character.Mount;
import objects.character.Player;
import objects.character.Stats;
import objects.guild.Guild;
import objects.guild.TaxCollector;
import objects.item.Gift;
import objects.item.Item;
import objects.item.ItemTemplate;
import objects.item.Pet;
import objects.item.SoulStone;
import objects.item.Pet.PetTemplate;
import objects.item.Speaking;
import objects.job.Job;
import objects.map.DofusMap;
import objects.monster.MonsterGroup;
import objects.monster.Monster;
import objects.npc.NpcQuestion;
import objects.npc.NpcResponse;
import objects.npc.NpcTemplate;
import objects.quest.Quest;
import objects.quest.QuestObjective;
import objects.quest.QuestStep;
import objects.spell.Spell;

public class World {

	private static Map<Integer,Account> 	Accounts	= new TreeMap<Integer,Account>();
	private static Map<Integer,Player> 	Players	= new TreeMap<Integer,Player>();
	private static Map<Integer,DofusMap> 	DofusMaps	= new TreeMap<Integer,DofusMap>();
	private static Map<Integer,Item> 	Items	= new TreeMap<Integer,Item>();
	private static Map<Integer, Integer> PlayerItems = new TreeMap<Integer, Integer>();
	private static Map<Integer,ExpLevel> ExpLevels = new TreeMap<Integer, ExpLevel>();
	private static Map<Integer,Spell>	Spells = new TreeMap<Integer,Spell>();
	private static Map<Integer,ItemTemplate> ObjTemplates = new TreeMap<Integer,ItemTemplate>();
	private static Map<Integer,Monster> MobTemplates = new TreeMap<Integer,Monster>();
	private static Map<Integer,NpcTemplate> NPCTemplates = new TreeMap<Integer,NpcTemplate>();
	private static Map<Integer,NpcQuestion> NPCQuestions = new TreeMap<Integer,NpcQuestion>();
	private static Map<Integer,NpcResponse> NPCReponses = new TreeMap<Integer,NpcResponse>();
	private static Map<Integer,IOTemplate> IOTemplates = new TreeMap<Integer,IOTemplate>();
	private static Map<Integer,Mount> Mounts = new TreeMap<Integer,Mount>();
	private static Map<Integer,SuperArea> SuperAreas = new TreeMap<Integer,SuperArea>();
	private static Map<Integer,Area> Areas = new TreeMap<Integer,Area>();
	private static Map<Integer,SubArea> SubAreas = new TreeMap<Integer,SubArea>();
	private static Map<Integer,Job> Jobs = new TreeMap<Integer,Job>();
	private static Map<Integer,ArrayList<Couple<Integer,Integer>>> Crafts = new TreeMap<Integer,ArrayList<Couple<Integer,Integer>>>();
	private static Map<Integer,ItemSet> ItemSets = new TreeMap<Integer,ItemSet>();
	private static Map<Integer,Guild> Guilds = new TreeMap<Integer,Guild>();
	private static Map<Integer,BigStore> BigStores = new TreeMap<Integer,BigStore>();
	private static Map<Integer,Gift> Gifts = new TreeMap<Integer,Gift>();
	private static Map<Integer,TaxCollector> TaxCollectors = new TreeMap<Integer,TaxCollector>();
	private static Map<String,FastCommand> Commands = new TreeMap<String,FastCommand>();
	private static Map<Integer,Quest> Quests = new TreeMap<Integer,Quest>();
	private static Map<Integer,QuestStep> QuestSteps = new TreeMap<Integer,QuestStep>();
	private static Map<Integer,QuestObjective> QuestObjectives = new TreeMap<Integer,QuestObjective>();
	private static Map<Integer,MonsterFollower> monstersFollower = new TreeMap<Integer,MonsterFollower>();
	private static Map<Integer,Collection<Integer>> Sellers = new TreeMap<Integer,Collection<Integer>>();
	private static Map<Integer,Map<Integer,ArrayList<BigStoreEntry>>> BigStoreItems = new HashMap<Integer,Map<Integer,ArrayList<BigStoreEntry>>>();	//Contient tout les items en ventes des comptes dans le format<compteID,<hdvID,items<>>>
	private static Map<Integer,PetTemplate> PetTemplates = new TreeMap<Integer,PetTemplate>();
	private static Map<Integer, Breed> Breeds = new TreeMap<Integer, Breed>();
	private static ArrayList<BreedSpell> BreedSpells = new ArrayList<BreedSpell>();
	private static Map<Integer, Prism> Prisms = new HashMap<Integer, Prism>();
	private static Map<Integer, Alignment> Alignments = new HashMap<Integer, Alignment>();
	private static List<String> NickNames = new ArrayList<String>(1000);
	private static Map<Integer, Rune> Runes = new HashMap<Integer, Rune>();
	
	private static int nextHdvID;	//Contient le derniere ID utilisé pour crée un HDV, pour obtenir un ID non utilisé il faut impérativement l'incrémenter
	private static int nextLineID;	//Contient le derniere ID utilisé pour crée une ligne dans un HDV
	private static int nextItemID; //Contient le derniere ID utilisé pour crée un Objet
	private static int nextTaxCollectorID = -49; //Contient le dernier ID utilisé pour crée un Percepteur ; débute à -50 pour ne pas géner les NPC
	
	private static int saveTry = 1;	//Contient le nombre de fois que le jeux a essayer de sauvegardé
	private static int _state = 1;
	public static char _stateID;
	public static int _Reqlevel;
	private static int nextPrismId;
	
	public static class Rune
	{
		private final int itemId;
		private final String statId;
		private final int power;
		private final int weight;
		private boolean isPotion = false;
		
		public Rune(final int itemId, final String statId, final int power, final int weight)
		{
			this.itemId = itemId;
			this.statId = statId;
			this.power = power;
			this.weight = weight;
			if(power == -1 && weight == -1)
				isPotion = true;
		}
		
		public int getItemId() {
			return itemId;
		}
		
		public String getStatId() {
			return statId;
		}
		
		public int getPower() {
			return power;
		}
		
		public int getWeight() {
			return weight;
		}

		public boolean isPotion() {
			return isPotion;
		}		
	}
		
	public static class Drop
	{
		private final int _itemID;
		private int _prosp;
		private float _rate;
		private int _max;
		private int _guid;
		private boolean _isSpecial = false;
		
		public Drop(final int guid, final int itm, final boolean iS)
		{
			_guid = guid;
			_itemID = itm;
			_isSpecial = iS;
		}
		public Drop(final int itm,final int p,final float r,final int m)
		{
			_itemID = itm;
			_prosp = p;
			_rate = r;
			_max = m;
		}
		public void setMax(final int m)
		{
			_max = m;
		}
		public int getItemID() {
			return _itemID;
		}

		public int getMinProsp() {
			return _prosp;
		}

		public float getRate() {
			return _rate;
		}

		public int getMax() {
			return _max;
		}
		
		public int getGuid() {
			return _guid;
		}
		
		public boolean isSpecial() {
			return _isSpecial;
		}
	}

	public static class ItemSet
	{
		private final int _id;
		private final ArrayList<ItemTemplate> _itemTemplates = new ArrayList<ItemTemplate>();
		private final ArrayList<Stats> _bonuses = new ArrayList<Stats>();
		
		public ItemSet (final int id,final String items, final String bonuses)
		{
			_id = id;
			//parse items String
			for(final String str : items.split(","))
			{
				try
				{
					final ItemTemplate t = World.getItemTemplate(Integer.parseInt(str.trim()));
					if(t == null)continue;
					_itemTemplates.add(t);
				}catch(final Exception e){};
			}
			
			//on ajoute un bonus vide pour 1 item
			_bonuses.add(new Stats());
			//parse bonuses String
			for(final String str : bonuses.split(";"))
			{
				final Stats S = new Stats();
				//séparation des bonus pour un même nombre d'item
				for(final String str2 : str.split(","))
				{
					try
					{
						final String[] infos = str2.split(":");
						final int stat = Integer.parseInt(infos[0]);
						final int value = Integer.parseInt(infos[1]);
						//on ajoute a la stat
						S.addOneStat(stat, value);
					}catch(final Exception e){};
				}
				//on ajoute la stat a la liste des bonus
				_bonuses.add(S);
			}
		}

		public int getId()
		{
			return _id;
		}
		
		public Stats getBonusStatByItemNumb(final int numb)
		{
			if(numb>_bonuses.size())return new Stats();
			return _bonuses.get(numb-1);
		}
		
		public ArrayList<ItemTemplate> getItemTemplates()
		{
			return _itemTemplates;
		}
	}
	
	public static class SuperArea
	{
		private final int _id;
		private final ArrayList<Area> _areas = new ArrayList<Area>();
		
		public SuperArea(final int a_id)
		{
			_id = a_id;
		}
		
		public void addArea(final Area A)
		{
			_areas.add(A);
		}
		
		public int getId()
		{
			return _id;
		}
	}
	
	public static class Area
	{
		private final int _id;
		private SuperArea _superArea;
		private final String _name;
		private final ArrayList<SubArea> _subAreas = new ArrayList<SubArea>();
		private final int[] _cemetery;
		
		public Area(final int id, final int superArea,final String name,final String cemetery)
		{
			_id = id;
			_name = name;
			_superArea = World.getSuperArea(superArea);
			//Si le continent n'est pas encore créer, on le créer et on l'ajoute au monde
			if(_superArea == null)
			{
				_superArea = new SuperArea(superArea);
				World.addSuperArea(_superArea);
			}
			_cemetery = new int[]{Integer.parseInt(cemetery.split(";")[0]), Integer.parseInt(cemetery.split(";")[1])};
		}
		public String getName()
		{
			return _name;
		}
		public int getId()
		{
			return _id;
		}
		
		public SuperArea getSuperArea()
		{
			return _superArea;
		}
		
		public void addSubArea(final SubArea sa)
		{
			_subAreas.add(sa);
		}
		
		public ArrayList<DofusMap> getMaps()
		{
			final ArrayList<DofusMap> maps = new ArrayList<DofusMap>();
			for(final SubArea SA : _subAreas)maps.addAll(SA.getMaps());
			return maps;
		}
		
		public int[] getCemetery() 
		{
	        return _cemetery;
        }
	}
	
	public static class SubArea
	{
		private final int _id;
		private final Area _area;
		private int _alignement;
		private final String _name;
		private final ArrayList<DofusMap> _maps = new ArrayList<DofusMap>();
		private Prism _prism;
		
		public SubArea(final int id, final int areaID, final int alignement,final String name, final int prismId)
		{
			_id = id;
			_name = name;
			_area =  World.getArea(areaID);
			_alignement = alignement;
			_prism = World.getPrism(prismId);
		}
		
		public String getName()
		{
			return _name;
		}
		public int getId() {
			return _id;
		}
		public Area getArea() {
			return _area;
		}
		public int getAlignement() {
			return _alignement;
		}
		public ArrayList<DofusMap> getMaps() {
			return _maps;
		}

		public void addMap(final DofusMap carte)
		{
			_maps.add(carte);
		}

		public Prism getPrism() 
		{
			return _prism;
		}
		
		public void setPrism(final Prism prism)
		{
			_prism = prism;
		}
		
		public synchronized ArrayList<Player> getPlayers()
		{
			final ArrayList<Player> players = new ArrayList<Player>();
			for(final DofusMap curMap : _maps)
			{
				players.addAll(curMap.getPersos());
			}
			return players;
		}

		public void setAlignement(final byte align) {
			_alignement = align;
		}
		
	}
	
	public static class Couple<L,R>
	{
	    public L first;
	    public R second;

	    public Couple(final L s, final R i)
	    {
	         this.first = s;
	         this.second = i;
	    }
	}

	public static class IOTemplate
	{
		private final int _id;
		private final int _respawnTime;
		private final int _duration;
		private final int _unk;
		private final boolean _walkable;
		
		public IOTemplate(final int a_i,final int a_r,final int a_d,final int a_u, final boolean a_w)
		{
			_id = a_i;
			_respawnTime = a_r;
			_duration = a_d;
			_unk = a_u;
			_walkable = a_w;
		}
		
		public int getId() {
			return _id;
		}	
		public boolean isWalkable() {
			return _walkable;
		}

		public int getRespawnTime() {
			return _respawnTime;
		}
		public int getDuration() {
			return _duration;
		}
		public int getUnk() {
			return _unk;
		}
	}
	
	public static class Exchange
	{
		private final Player perso1;
		private final Player perso2;
		private long kamas1 = 0;
		private long kamas2 = 0;
		private final ArrayList<Couple<Integer,Integer>> items1 = new ArrayList<Couple<Integer,Integer>>();
		private final ArrayList<Couple<Integer,Integer>> items2 = new ArrayList<Couple<Integer,Integer>>();
		private boolean ok1;
		private boolean ok2;
		
		public Exchange(final Player p1, final Player p2)
		{
			perso1 = p1;
			perso2 = p2;
		}
		
		synchronized public long getKamas(final int guid)
		{
			int i = 0;
			if(perso1.getActorId() == guid)
				i = 1;
			else if(perso2.getActorId() == guid)
				i = 2;
			
			if(i == 1)
				return kamas1;
			else if (i == 2)
				return kamas2;
			return 0;
		}
		
		synchronized public void toogleOK(final int guid)
		{
			int i = 0;
			if(perso1.getActorId() == guid)
				i = 1;
			else if(perso2.getActorId() == guid)
				i = 2;
			
			if(i == 1)
			{
				ok1 = !ok1;
				SocketManager.GAME_SEND_EXCHANGE_OK(perso1.getAccount().getGameThread().getOut(),ok1,guid);
				SocketManager.GAME_SEND_EXCHANGE_OK(perso2.getAccount().getGameThread().getOut(),ok1,guid);
			}
			else if (i == 2)
			{
				ok2 = !ok2;
				SocketManager.GAME_SEND_EXCHANGE_OK(perso1.getAccount().getGameThread().getOut(),ok2,guid);
				SocketManager.GAME_SEND_EXCHANGE_OK(perso2.getAccount().getGameThread().getOut(),ok2,guid);
			}
			else 
				return;
			
			
			if(ok1 && ok2)
				apply();
		}
		
		synchronized public void setKamas(final int guid, final long k)
		{
			ok1 = false;
			ok2 = false;
			
			int i = 0;
			if(perso1.getActorId() == guid)
				i = 1;
			else if(perso2.getActorId() == guid)
				i = 2;
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.getAccount().getGameThread().getOut(),ok1,perso1.getActorId());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.getAccount().getGameThread().getOut(),ok1,perso1.getActorId());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.getAccount().getGameThread().getOut(),ok2,perso2.getActorId());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.getAccount().getGameThread().getOut(),ok2,perso2.getActorId());
			
			if(i == 1)
			{
				kamas1 = k;
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'G', "", k+"");
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.getAccount().getGameThread().getOut(), 'G', "", k+"");
			}else if (i == 2)
			{
				kamas2 = k;
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.getAccount().getGameThread().getOut(), 'G', "", k+"");
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'G', "", k+"");	
			}
		}
		
		synchronized public void cancel()
		{
			if(perso1.getAccount() != null)if(perso1.getAccount().getGameThread() != null)SocketManager.GAME_SEND_EV_PACKET(perso1.getAccount().getGameThread().getOut());
			if(perso2.getAccount() != null)if(perso2.getAccount().getGameThread() != null)SocketManager.GAME_SEND_EV_PACKET(perso2.getAccount().getGameThread().getOut());
			
			/*Objet toAdd;
			int guid;
			
			for(Couple<Integer, Integer> curObj : items1)
			{
				guid = curObj.first;
				toAdd = World.getObjet(guid);
				
				if(perso1.hasItemGuid(guid))
				{
					int newQua = toAdd.getQuantity() + curObj.second;
					toAdd.setQuantity(newQua);
					continue;
				}else
				{
					perso1.addObjet(toAdd, true);
				}
			}
			for(Couple<Integer, Integer> curObj : items2)
			{
				guid = curObj.first;
				toAdd = World.getObjet(guid);
				
				if(perso2.hasItemGuid(guid))
				{
					int newQua = toAdd.getQuantity() + curObj.second;
					toAdd.setQuantity(newQua);
					continue;
				}else
				{
					perso2.addObjet(toAdd, true);
				}
			}*/
			
			perso1.setIsTradingWith(0);
			perso2.setIsTradingWith(0);
			perso1.setCurExchange(null);
			perso2.setCurExchange(null);
		}
		
		synchronized public void apply()
		{
			//Gestion des Kamas
			perso1.addKamas((-kamas1+kamas2));
			perso1.kamasLog(-kamas1+kamas2+"", "Echangé avec le personnage '" + perso2.getName() + "'");
			
			perso2.addKamas((-kamas2+kamas1));
			perso2.kamasLog(-kamas2+kamas1+"", "Echangé avec le personnage '" + perso1.getName() + "'");
			
			for(final Couple<Integer, Integer> couple : items1)
			{
				if(couple.second == 0)continue;
				/*if(!perso1.hasItemGuid(couple.first))//Si le perso n'a pas l'item (Ne devrait pas arriver)
				{
					couple.second = 0;//On met la quantité a 0 pour éviter les problemes
					continue;
				}*/	
				final Item obj = World.getObjet(couple.first);
				if((obj.getQuantity() - couple.second) <1)//S'il ne reste plus d'item apres l'échange
				{
					perso1.removeItem(couple.first);
					couple.second = obj.getQuantity();
					//perso1.objetLog(obj.getTemplate().getID(), -couple.second, "Echangé avec le personnage '" + perso2.get_name() + "'");
					
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(perso1, couple.first);
					if(!perso2.addItem(obj, true))//Si le joueur avait un item similaire
						World.removeItem(couple.first);//On supprime l'item inutile
					perso2.itemLog(obj.getTemplate().getID(), obj.getQuantity(), "Reçu en échange avec '" + perso1.getName() + "'");
				}else
				{
					obj.setQuantity(obj.getQuantity()-couple.second);
					//perso1.objetLog(obj.getTemplate().getID(), -couple.second, "Echangé avec le personnage '" + perso2.get_name() + "'");
					
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(perso1, obj);
					final Item newObj = Item.getCloneObjet(obj, couple.second);
					if(perso2.addItem(newObj, true))//Si le joueur n'avait pas d'item similaire
						World.addItem(newObj,true);//On ajoute l'item au World
					perso2.itemLog(obj.getTemplate().getID(), obj.getQuantity(), "Reçu en échange avec '" + perso1.getName() + "'");
				}
			}
			for(final Couple<Integer, Integer> couple : items2)
			{
				if(couple.second == 0)continue;
				/*if(!perso2.hasItemGuid(couple.first))//Si le perso n'a pas l'item (Ne devrait pas arriver)
				{
					couple.second = 0;//On met la quantité a 0 pour éviter les problemes
					continue;
				}*/	
				final Item obj = World.getObjet(couple.first);
				if((obj.getQuantity() - couple.second) <1)//S'il ne reste plus d'item apres l'échange
				{
					perso2.removeItem(couple.first);
					couple.second = obj.getQuantity();
					//perso2.objetLog(obj.getTemplate().getID(), -couple.second, "Echangé avec le personnage '" + perso1.get_name() + "'");
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(perso2, couple.first);
					if(!perso1.addItem(obj, true))//Si le joueur avait un item similaire
						World.removeItem(couple.first);//On supprime l'item inutile
					perso1.itemLog(obj.getTemplate().getID(), obj.getQuantity(), "Reçu en échange avec '" + perso2.getName() + "'");
				}else
				{
					obj.setQuantity(obj.getQuantity()-couple.second);
					//perso2.objetLog(obj.getTemplate().getID(), -couple.second, "Echangé avec le personnage '" + perso1.get_name() + "'");
					
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(perso2, obj);
					final Item newObj = Item.getCloneObjet(obj, couple.second);
					if(perso1.addItem(newObj, true))//Si le joueur n'avait pas d'item similaire
						World.addItem(newObj,true);//On ajoute l'item au World
					perso1.itemLog(obj.getTemplate().getID(), obj.getQuantity(), "Reçu en échange avec '" + perso2.getName() + "'");
				}
			}
			//Fin
			perso1.setIsTradingWith(0);
			perso2.setIsTradingWith(0);
			perso1.setCurExchange(null);
			perso2.setCurExchange(null);
			SocketManager.GAME_SEND_Ow_PACKET(perso1);
			SocketManager.GAME_SEND_Ow_PACKET(perso2);
			SocketManager.GAME_SEND_STATS_PACKET(perso1);
			SocketManager.GAME_SEND_STATS_PACKET(perso2);
			SocketManager.GAME_SEND_EXCHANGE_VALID(perso1.getAccount().getGameThread().getOut(),'a');
			SocketManager.GAME_SEND_EXCHANGE_VALID(perso2.getAccount().getGameThread().getOut(),'a');	
			SQLManager.SAVE_PERSONNAGE(perso1,true);
			SQLManager.SAVE_PERSONNAGE(perso2,true);
		}

		synchronized public void addItem(final int guid, final int qua, final int pguid)
		{
			ok1 = false;
			ok2 = false;
			
			int i = 0;
			if(perso1.getActorId() == pguid)
				i = 1;
			else if(perso2.getActorId() == pguid)
				i = 2;
			
			final String str = guid+"|"+qua;
			final Item obj = World.getObjet(guid);
			if(obj == null)return;
			final String add = "|"+obj.getTemplate().getID()+"|"+obj.parseStatsString();
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.getAccount().getGameThread().getOut(),ok1,perso1.getActorId());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.getAccount().getGameThread().getOut(),ok1,perso1.getActorId());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.getAccount().getGameThread().getOut(),ok2,perso2.getActorId());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.getAccount().getGameThread().getOut(),ok2,perso2.getActorId());
			
			if(i == 1)
			{
				final Couple<Integer,Integer> couple = getCoupleInList(items1,guid);
				if(couple != null)
				{
					couple.second += qua;
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", ""+guid+"|"+couple.second);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.getAccount().getGameThread().getOut(), 'O', "+", ""+guid+"|"+couple.second+add);
				return;
				}
				items1.add(new Couple<Integer,Integer>(guid,qua));
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", str);
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.getAccount().getGameThread().getOut(), 'O', "+", str+add);	
			}else if(i == 2)
			{
				final Couple<Integer,Integer> couple = getCoupleInList(items2,guid);
				if(couple != null)
				{
					couple.second += qua;
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.getAccount().getGameThread().getOut(), 'O', "+", ""+guid+"|"+couple.second+add);
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", ""+guid+"|"+couple.second);
				return;
				}
				items2.add(new Couple<Integer,Integer>(guid,qua));
				SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", str);
				SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.getAccount().getGameThread().getOut(), 'O', "+", str+add);	
			}
		}

		
		synchronized public void removeItem(final int guid, final int qua, final int pguid)
		{
			int i = 0;
			if(perso1.getActorId() == pguid)
				i = 1;
			else if(perso2.getActorId() == pguid)
				i = 2;
			ok1 = false;
			ok2 = false;
			
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.getAccount().getGameThread().getOut(),ok1,perso1.getActorId());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.getAccount().getGameThread().getOut(),ok1,perso1.getActorId());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso1.getAccount().getGameThread().getOut(),ok2,perso2.getActorId());
			SocketManager.GAME_SEND_EXCHANGE_OK(perso2.getAccount().getGameThread().getOut(),ok2,perso2.getActorId());
			
			final Item obj = World.getObjet(guid);
			if(obj == null)return;
			final String add = "|"+obj.getTemplate().getID()+"|"+obj.parseStatsString();
			if(i == 1)
			{
				final Couple<Integer,Integer> couple = getCoupleInList(items1,guid);
				final int newQua = couple.second - qua;
				if(newQua <1)//Si il n'y a pu d'item
				{
					items1.remove(couple);
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "-", ""+guid);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.getAccount().getGameThread().getOut(), 'O', "-", ""+guid);
				}else
				{
					couple.second = newQua;
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso1, 'O', "+", ""+guid+"|"+newQua);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso2.getAccount().getGameThread().getOut(), 'O', "+", ""+guid+"|"+newQua+add);
				}
			}else if(i ==2)
			{
				final Couple<Integer,Integer> couple = getCoupleInList(items2,guid);
				final int newQua = couple.second - qua;
				
				if(newQua <1)//Si il n'y a pu d'item
				{
					items2.remove(couple);
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.getAccount().getGameThread().getOut(), 'O', "-", ""+guid);
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "-", ""+guid);
				}else
				{
					couple.second = newQua;
					SocketManager.GAME_SEND_EXCHANGE_OTHER_MOVE_OK(perso1.getAccount().getGameThread().getOut(), 'O', "+", ""+guid+"|"+newQua+add);
					SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(perso2, 'O', "+", ""+guid+"|"+newQua);
				}
			}
		}

		synchronized private Couple<Integer, Integer> getCoupleInList(final ArrayList<Couple<Integer, Integer>> items,final int guid)
		{
			for(final Couple<Integer, Integer> couple : items)
			{
				if(couple.first == guid)
					return couple;
			}
			return null;
		}
		
		public synchronized int getQuaItem(final int itemID, final int playerGuid)
		{
			ArrayList<Couple<Integer, Integer>> items;
			if(perso1.getActorId() == playerGuid)
				items = items1;
			else
				items = items2;
			
			for(final Couple<Integer, Integer> curCoupl : items)
			{
				if(curCoupl.first == itemID)
				{
					return curCoupl.second;
				}
			}
			
			return 0;
		}
		
	}
	
	public static class ExpLevel
	{
		public long perso;
		public int metier;
		public int dinde;
		public int pvp;
		public long guilde;
		public int obvijevan;
		
		public ExpLevel(final long c, final int m, final int d, final int p, final int o)
		{
			perso = c;
			metier = m;
			dinde = d;
			pvp = p;
			guilde = perso*10;
			obvijevan = o;
		}
		
	}
	
	public static class MonsterFollower {
		
		private final int itemId;
		private final int mobId;
		private final int turns;
		
		public MonsterFollower(final int mobId, final int itemId, final int turns)
		{
			this.mobId = mobId;
			this.itemId = itemId;
			this.turns = turns;
		}
		
		public int getMobId() 
		{
			return this.mobId;
		}
		
		public int getItemId()
		{
			return this.itemId;
		}
			
		public int getTurns()
		{
			return this.turns;
		}
	}
	
	public static void createWorld()
	{
		Console.printlnDebug("====>Static DATA<====");
		
		Console.printDebug("Loading levels of experience: ");
		SQLManager.LOAD_EXP();
		Config.MAX_LEVEL = ExpLevels.size();	//MARTHIEUBEAN
		Console.printlnDebug(ExpLevels.size()+" levels were loaded");
		
		Console.printDebug("Loading spells: ");
		SQLManager.LOAD_SORTS();
		Console.printlnDebug(Spells.size()+" spells were loaded");
		
		Console.printDebug("Loading monster templates: ");
		SQLManager.LOAD_MOB_TEMPLATE();
		Console.printlnDebug(MobTemplates.size()+" templates monster were loaded");
		
		Console.printDebug("Loading item template: ");
		SQLManager.LOAD_OBJ_TEMPLATE();
		Console.printlnDebug(ObjTemplates.size()+" templates item were loaded");
		
		Console.printDebug("Loading NPC templates: ");
		SQLManager.LOAD_NPC_TEMPLATE();
		Console.printlnDebug(NPCTemplates.size()+" templates NPC were loaded");
		
		Console.printDebug("Loading questions NPC:");
		SQLManager.LOAD_NPC_QUESTIONS();
		Console.printlnDebug(NPCQuestions.size()+" questions NPC were loaded");
		
		Console.printDebug("Loading answers NPC: ");
		SQLManager.LOAD_NPC_ANSWERS();
		Console.printlnDebug(NPCReponses.size()+" answers NPC were loaded");
		
		Console.printDebug("Loading areas: ");
		SQLManager.LOAD_AREA();
		Console.printlnDebug(Areas.size()+" areas were loaded");
		
		Console.printDebug("Loading subareas: ");
		SQLManager.LOAD_SUBAREA();
		Console.printlnDebug(SubAreas.size()+" subareas were loaded");
		
		Console.printDebug("Loading interactive object templates: ");
		SQLManager.LOAD_IOTEMPLATE();
		Console.printlnDebug(IOTemplates.size()+" IO templates were loaded");
		
		Console.printDebug("Loading crafts: ");
		SQLManager.LOAD_CRAFTS();
		Console.printlnDebug(Crafts.size()+" crafts were loaded");
		
		Console.printDebug("Loading jobs: ");
		SQLManager.LOAD_JOBS();
		//Constants.initRune();	//Remplissage du HashMap qui contient le poids des runes pour la FM
		Console.printlnDebug(Jobs.size()+" jobs were loaded");
		
		Console.printDebug("Loading itemsets: ");
		SQLManager.LOAD_ITEMSETS();
		Console.printlnDebug(ItemSets.size()+" itemsets were loaded");
		
		Console.printDebug("Loading maps: ");
		SQLManager.LOAD_MAPS();
		Console.printlnDebug(DofusMaps.size()+" maps were loaded");
		
		Console.printDebug("Loading triggers: ");
		int nbr = SQLManager.LOAD_TRIGGERS();
		Console.printlnDebug(nbr+" triggers were loaded");
		
		Console.printDebug("Loading end fight actions:");
		nbr = SQLManager.LOAD_ENDFIGHT_ACTIONS();
		Console.printlnDebug(nbr+" end fight actions were loaded");
		
		Console.printDebug("Loading NPCS: ");
		nbr = SQLManager.LOAD_NPCS();
		Console.printlnDebug(nbr+" NPCS were loaded");
		
		Console.printDebug("Loading item actions: ");
		nbr = SQLManager.LOAD_ITEM_ACTIONS();
		Console.printlnDebug(nbr+" item actions were loaded");
		
		Console.printDebug("Loading drops: ");
		SQLManager.LOAD_DROPS();
		loadDrops();
		Console.printlnDebug("drops were loaded");
		
		Console.printDebug("Loading gifts: ");
		SQLManager.LOAD_GIFTS();
		Console.printlnDebug("gifts were loaded");
		
		Console.printDebug("Loading quest objectives: ");
		SQLManager.LOAD_QUEST_OBJECTIFS();
		Console.printlnDebug(QuestObjectives.size() + " quest objectives were loaded");
		
		Console.printDebug("Loading quest steps: ");
		SQLManager.LOAD_QUEST_STEP();
		Console.printlnDebug(QuestSteps.size() + " quest steps were loaded");
		
		Console.printDebug("Loading quests: ");
		SQLManager.LOAD_QUEST_DATA();
		Console.printlnDebug(Quests.size() + " quests were loaded");
		
		Console.printDebug("Loading monster followers: ");
		SQLManager.LOAD_MONSTERSFOLLOWERS();
		Console.printlnDebug("monster followers were loaded");
		
		Console.printDebug("Loading breed data: ");
		SQLManager.LOAD_BREEDS();
		Console.printlnDebug("breed data were loaded");
		
		Console.printDebug("Loading breed spells: ");
		SQLManager.LOAD_BREED_SPELLS();
		Console.printlnDebug("breed spells were loaded");
		
		Console.printlnDebug("====>Dynamic DATA<====");
		Console.printDebug("Loading items: ");
		final int errCount = SQLManager.LOAD_ITEMS();
		Console.printlnDebug("items were loaded"+(errCount > 0 ? " with "+errCount+" errors" : ""));
		
		Console.printDebug("Loading mounts: ");
		SQLManager.LOAD_MOUNTS();
		Console.printlnDebug("mounts were loaded");
		
		Console.printDebug("Loading players: ");
		SQLManager.LOAD_PERSOS();
		Console.printlnDebug("players were loaded");
		
		Console.printDebug("Loading guilds: ");
		SQLManager.LOAD_GUILDS();
		Console.printlnDebug("guilds were loaded");
		
		Console.printDebug("Loading guild members: ");
		SQLManager.LOAD_GUILD_MEMBERS();
		Console.printlnDebug("guild members were loaded");
		
		Console.printDebug("Loading mountparks: ");
		SQLManager.LOAD_MOUNTPARKS();
		Console.printlnDebug("mountparks were loaded");
		
		Console.printDebug("Loading bigstores: ");
		SQLManager.LOAD_HDVS();
		SQLManager.LOAD_HDVS_ITEMS();
		Console.printlnDebug("bigstores were loaded");
		
		Console.printlnDebug("Loading other contents...");
		loadNicknames();
		Console.printlnDefault("World created !");
		
		nextItemID = SQLManager.getNextObjetID();
	}
	
	private static void loadNicknames() {
		BufferedReader reader = null;
		String line = "";
		try {
			reader = new BufferedReader(new FileReader("nicknames.dic"));
			while ((line=reader.readLine())!=null) {
				for(final String nickname : line.split(";")) {
					NickNames.add(nickname);
				}
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void loadDrops() {    
		final int[] dropsId = {724, 725, 726, 727, 728, 729, 730, 8087};
		int dropId = -1;
		for(int index = 0; index < dropsId.length; index++)
		{
			dropId = dropsId[index];
			for(final Monster monster : MobTemplates.values())
			{		
				final int monsterId = monster.getID();
				if(dropId == 724 && monsterId == 159
				|| dropId == 725 && monsterId == 154
				|| dropId == 726 && monsterId == 74
				|| dropId == 727 && monsterId == 108
				|| dropId == 728 && monsterId == 34
				|| dropId == 729 && monsterId == 442
				|| dropId == 730 && monsterId == 56
				|| dropId == 8087 && monsterId == 534)
					monster.addDrop(new Drop(dropId, 100, 0.1f, 1));
				else
					monster.addDrop(new Drop(dropId, 100, 0.01f, 1));
			}
		}
	}

	public static void changePlayerNameUI(final Player player, final String name) {
		final PrintWriter out = player.getAccount().getGameThread().getOut();
		if(SQLManager.persoExist(name)) {
			player.changePseudo(false);
			SocketManager.GAME_SEND_IM_MESSAGE_RED(player, "Ce nom n'est pas disponible.");
			SocketManager.GAME_SEND_CHOOSE_PSEUDO(out);
			SocketManager.GAME_SEND_CHOOSE_PSEUDO(out);
			return;
		}
		boolean isValid = true;
		final String nameC = name.toLowerCase();
		if(nameC.length() > 20
				|| nameC.contains("mj") || nameC.contains("modo")	|| nameC.contains("admin")
				|| nameC.contains("pd") || nameC.contains("chatte") || nameC.contains("salope")
				|| nameC.contains("pute") || nameC.contains("encule")	|| nameC.contains("baise")
				|| nameC.contains("baize") || nameC.contains("cra") || nameC.contains("feca")
				|| nameC.contains("eniripsa") || nameC.contains("sadida")	|| nameC.contains("pandawa")
				|| nameC.contains("enutrof")	|| nameC.contains("iop")	|| nameC.contains("osamodas")
				|| nameC.contains("roublard") || nameC.contains("zobal") || nameC.contains("ecaflip")
				|| nameC.contains("sram") || nameC.contains("sacrieur") || nameC.contains("xelor"))
		{
			isValid = false;
		}
		if(isValid)
		{
			int tiretCount = 0;
			for(final char curLetter : name.toCharArray()) {
				if(!((curLetter >= 'a' && curLetter <= 'z')	|| curLetter == '-')) {
					isValid = false;
					break;
				}
				if(curLetter == '-') {
					if(tiretCount >= 2)	{
						isValid = false;
						break;
					} else {
						tiretCount++;
					}
				}
			}
		}
		if(!isValid) {
			player.changePseudo(false);
			SocketManager.GAME_SEND_IM_MESSAGE_RED(player, "Ce nom n'est pas valide.");
			SocketManager.GAME_SEND_CHOOSE_PSEUDO(out);
			SocketManager.GAME_SEND_CHOOSE_PSEUDO(out);
			return;
		}
		player.setName(name);
		SocketManager.GAME_SEND_CHOOSE_PSEUDO(out);
		SocketManager.GAME_SEND_IM_MESSAGE_RED(player, "Vous êtes désormais perçu par les autres joueurs comme étant : <b>"+name+"</b>.~)");
	}
	
	public static Area getArea(final int areaID)
	{
		return Areas.get(areaID);
	}

	public static SuperArea getSuperArea(final int areaID)
	{
		return SuperAreas.get(areaID);
	}
	
	public static SubArea getSubArea(final int areaID)
	{
		return SubAreas.get(areaID);
	}
	
	public static void addArea(final Area area)
	{
		Areas.put(area.getId(), area);
	}
	
	public static void addSuperArea(final SuperArea SA)
	{
		SuperAreas.put(SA.getId(), SA);
	}
	
	public static void addSubArea(final SubArea SA)
	{
		SubAreas.put(SA.getId(), SA);
	}
	
	public static void addNPCreponse(final NpcResponse rep)
	{
		NPCReponses.put(rep.getId(), rep);
	}
	
	public static NpcResponse getNPCreponse(final int guid)
	{
		return NPCReponses.get(guid);
	}
	
	public static void addExpLevel(final int lvl,final ExpLevel exp)
	{
		ExpLevels.put(lvl, exp);
	}
	
	public static Account getAccount(final int guid)
	{
		return Accounts.get(guid);
	}
	
	public static void addNPCQuestion(final NpcQuestion quest)
	{
		NPCQuestions.put(quest.get_id(), quest);
	}
	
	public static NpcQuestion getNPCQuestion(final int guid)
	{
		return NPCQuestions.get(guid);
	}
	public static NpcTemplate getNPCTemplate(final int guid)
	{
		return NPCTemplates.get(guid);
	}
	
	public static void addNpcTemplate(final NpcTemplate temp)
	{
		NPCTemplates.put(temp.getId(), temp);
	}
	
	public static DofusMap getMap(final int id)
	{
		return DofusMaps.get(id);
	}
	
	public static  void addDofusMap(final DofusMap map)
	{
		if(!DofusMaps.containsKey(map.getId()))
			DofusMaps.put(map.getId(),map);
	}
	
	public static Account getCompteByName(final String name)
	{
		for(int a = 0; a< Accounts.keySet().size(); a++)
		{
			if(Accounts.get(Accounts.keySet().toArray()[a]).getName().equalsIgnoreCase(name))
				return Accounts.get(Accounts.keySet().toArray()[a]);
		}
		return null;
	}
	
	public static Player getPlayer(final int guid)
	{
		return Players.get(guid);
	}
	
	public static void addAccount(final Account compte)
	{
		Accounts.put(compte.getGUID(), compte);
	}

	public static void addPersonnage(final Player perso)
	{
		Players.put(perso.getActorId(), perso);
	}

	public static Player getPersoByName(final String name)
	{
		final ArrayList<Player> Ps = new ArrayList<Player>();
		Ps.addAll(Players.values());
		
		/*if(name.contains("["))
			name = name.substring(name.indexOf("]")+1);*/
		
		for(final Player P : Ps)
			if(P.getName().equalsIgnoreCase(name))
				return P;
		return null;
	}

	public static void deletePerso(final Player perso)
	{
		SQLManager.DELETE_PERSO_IN_BDD(perso);
		World.unloadPerso(perso.getActorId());
	}

	public static String getSousZoneStateString()
	{
		final String data = "";
		/* TODO: Sous Zone Alignement */
		return data;
	}
	
	public static long getPersoXpMin(int _lvl)
	{
		if(_lvl > Config.MAX_LEVEL) 	_lvl = Config.MAX_LEVEL;
		if(_lvl < 1) 	_lvl = 1;
		return ExpLevels.get(_lvl).perso;
	}
	
	public static long getPersoXpMax(int _lvl)
	{
		if(_lvl >= Config.MAX_LEVEL) 	_lvl = Config.MAX_LEVEL-1;
		if(_lvl <= 1)	 	_lvl = 1;
		return ExpLevels.get(_lvl+1).perso;
	}
	public static int getObviXpMax(int _lvl) {
		if (_lvl >= 20) _lvl = 19;
		if (_lvl <= 1) _lvl = 1;
		return (int) (ExpLevels.get(_lvl + 1)).obvijevan;
	}

	public static int getObviXpMin(int _lvl) {
		if (_lvl > 20) _lvl = 20;
		if (_lvl < 1) _lvl = 1;
		return (int) (ExpLevels.get(_lvl)).obvijevan;
	}
	
	public static void addSort(final Spell sort)
	{
		Spells.put(sort.getSpellID(), sort);
	}

	public static void addObjTemplate(final ItemTemplate obj)
	{
		ObjTemplates.put(obj.getID(), obj);
	}
	
	public static Spell getSpell(final int id)
	{
		return Spells.get(id);
	}

	public static ItemTemplate getItemTemplate(final int id)
	{
		return ObjTemplates.get(id);
	}
	
	public synchronized static int getNewItemGuid()
	{
		nextItemID++;
		return nextItemID;
		/*int id = 0;
		for(Entry<Integer,Objet> entry : Objets.entrySet())
		{
			if(entry.getKey() > id)
				id = entry.getKey();
		}
		id++;
		return id;*/
	}
	
	public synchronized static int getNewPercoGuid()
	{
		nextTaxCollectorID--;
		return nextTaxCollectorID;
	}

	public static void addMobTemplate(final int id,final Monster mob)
	{
		MobTemplates.put(id, mob);
	}

	public static Monster getMonstre(final int id)
	{
		return MobTemplates.get(id);
	}

	public static List<Player> getOnlinePersos()
	{
		final List<Player> online = new ArrayList<Player>();
		for(final Entry<Integer,Player> perso : Players.entrySet())
		{
			if(perso.getValue().isOnline() && perso.getValue().getAccount().getGameThread() != null)
			{
				if(perso.getValue().getAccount().getGameThread().getOut() != null)
				{
					online.add(perso.getValue());
				}
			}
		}
		return online;
	}
	
	public static List<Item> getSpeakingItems() {
		final ArrayList<Item> list = new ArrayList<Item>();
		for (final Item p : Items.values()) {
			if (p.isSpeaking()) {
				list.add(p);
			}
		}
		return list;
	}
	
	public static List<Item> getPetItems() {
		final ArrayList<Item> list = new ArrayList<Item>();
		for (final Item p : Items.values()) {
			if (p.isPet()) {
				list.add(p);
			}
		}
		return list;
	}
	
	public static Pet getPet(final int itemGuid)
	{
		for(final Item itm : getPetItems())
		{
			if(itm.getGuid() == itemGuid)
			{
				return (Pet) itm;
			}
		}
		return null;
	}

	public static void addItem(final Item item, final boolean saveSQL)
	{
		Items.put(item.getGuid(), item);
		if (saveSQL) {
			SQLManager.SAVE_NEW_ITEM(item);
			if (item.isSpeaking())
				SQLManager.SAVE_NEW_ITEM_SPEAKING(item);
		}
	}
	
	public static Item getObjet(final int guid)
	{
		return Items.get(guid);
	}

	public static void removeItem(final int guid)
	{
		if (Items.get(guid) != null && Items.get(guid).isSpeaking()) {
			Items.remove(guid);
			SQLManager.DELETE_ITEM(guid);
			SQLManager.DELETE_SPEAKING_ITEM(guid);
		} else {
			Items.remove(guid);
			SQLManager.DELETE_ITEM(guid);
		}
	}

	public static void addIOTemplate(final IOTemplate IOT)
	{
		IOTemplates.put(IOT.getId(), IOT);
	}
	
	public static Mount getDragoByID(final int id)
	{
		return Mounts.get(id);
	}
	public static void addDragodinde(final Mount DD)
	{
		Mounts.put(DD.getId(), DD);
	}
	public static void saveAll(final Player saver)
	{
		PrintWriter _out = null;
		if(saver != null)
			_out = saver.getAccount().getGameThread().getOut();
		
		setState(2);
		
		try
		{
			Main.isSaving = true;
			
			SQLManager.commitTransacts();
			SQLManager.TIMER(false);	//Arrête le timer d'enregistrement SQL
			
			Thread.sleep(10000);
			Log.addToLog("Saving players...");
			for(final Player perso : Players.values())
			{
				if(!perso.isOnline())continue;
				Thread.sleep(100);
				SQLManager.SAVE_PERSONNAGE(perso,true);//sauvegarde des persos et de leurs items
				perso.commitLogger();	//Ecriture des actions kamas/objet
			}
			Thread.sleep(10000);
			Log.addToLog("Saving guilds...");
			for(final Guild guilde : Guilds.values())
			{
				Thread.sleep(100);
				SQLManager.UPDATE_GUILD(guilde);
			}
			Thread.sleep(10000);
			Log.addToLog("Saving taxcollectors...");
			for (final TaxCollector perco : TaxCollectors.values()) {
				if (perco.getFight() != null)
					continue;
				Thread.sleep(100);
				SQLManager.UPDATE_PERCEPTEUR(perco);
			}
			Thread.sleep(10000);
			Log.addToLog("Saving bigstores...");
			final ArrayList<BigStoreEntry> toSave = new ArrayList<BigStoreEntry>();
			for(final BigStore curHdv : BigStores.values())
			{
				Thread.sleep(100);
				toSave.addAll(curHdv.getAllEntry());
			}
			SQLManager.SAVE_HDVS_ITEMS(toSave);
			Log.addToLog("Save performed !");
			
			setState(1);
			
		}catch(final ConcurrentModificationException e)
		{
			if(saveTry < 10)
			{
				Log.addToErrorLog("New attempt of saving...");
				if(saver != null && _out != null)
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, "Error. New attempt of saving...");
				saveTry++;
				saveAll(saver);
			}
			else
			{
				final String mess = "Save failed after " + saveTry + " attempts";
				if(saver != null && _out != null)
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_PACKET(_out, mess);
				else
					SocketManager.GAME_SEND_CONSOLE_MESSAGE_TO_ADMIN(mess, 5);
				Log.addToErrorLog(mess);
				saveTry = 0;
				setState(1);
			}
				
		}catch(final Exception e)
		{
			Log.addToErrorLog("Error when saving: " + e.getMessage());
			e.printStackTrace();
			setState(1);
		}
		finally
		{
			SQLManager.commitTransacts();
			SQLManager.TIMER(true); //Redémarre le timer d'enregistrement SQL
			Main.isSaving = false;
			saveTry = 1;
			setState(1);
		}
	}
	public static void resetSave() throws Exception
	{
		SQLManager.commitTransacts();
		SQLManager.TIMER(true); //Redémarre le timer d'enregistrement SQL
		Main.isSaving = false;
		saveTry = 1;
	}

	public static ExpLevel getExpLevel(final int lvl)
	{
		return ExpLevels.get(lvl);
	}
	public static IOTemplate getIOTemplate(final int id)
	{
		return IOTemplates.get(id);
	}
	public static Job getMetier(final int id)
	{
		return Jobs.get(id);
	}

	public static void addJob(final Job metier)
	{
		Jobs.put(metier.getId(), metier);
	}

	public static void addCraft(final int id, final ArrayList<Couple<Integer, Integer>> m)
	{
		Crafts.put(id,m);
	}
	
	public static ArrayList<Couple<Integer,Integer>> getCraft(final int i)
	{
		return Crafts.get(i);
	}

	public static int getObjectByIngredientForJob( final ArrayList<Integer> list, final Map<Integer, Integer> ingredients)
	{
		if(list == null)return -1;
		for(final int tID : list)
		{
			final ArrayList<Couple<Integer,Integer>> craft = World.getCraft(tID);
			if(craft == null)
			{
				Log.addToErrorLog("/!\\Recette pour l'objet "+tID+" non existante !");
				continue;
			}
			if(craft.size() != ingredients.size())continue;
			boolean ok = true;
			for(final Couple<Integer,Integer> c : craft)
			{
				//si ingredient non présent ou mauvaise quantité
				if(ingredients.get(c.first) != c.second)ok = false;
			}
			if(ok)return tID;
		}
		return -1;
	}
	public static Account getCompteByPseudo(final String p)
	{
		for(final Account C : Accounts.values())if(C.getPseudo().equals(p))return C;
		return null;
	}

	public static void addItemSet(final ItemSet itemSet)
	{
		ItemSets.put(itemSet.getId(), itemSet);
	}

	public static ItemSet getItemSet(final int tID)
	{
		return ItemSets.get(tID);
	}

	public static int getItemSetNumber()
	{
		return ItemSets.size();
	}

	public synchronized static int getNextIdForMount()
	{
		int max = 1;
		for(final int a : Mounts.keySet())if(a > max)max = a;
		return max+1;
	}

	public static DofusMap getCarteByPosAndCont(final int mapX, final int mapY, final int contID)
	{
		for(final DofusMap map : DofusMaps.values())
		{
			if( map.getX() == mapX
			&&	map.getY() == mapY
			&&	map.getSubArea().getArea().getSuperArea().getId() == contID)
				return map;
		}
		return null;
	}
	public static void addGuild(final Guild g,final boolean save)
	{
		Guilds.put(g.get_id(), g);
		if(save)SQLManager.SAVE_NEWGUILD(g);
	}
	public synchronized static int getNextHighestGuildID()
	{
		if(Guilds.size() == 0)return 1;
		int n = 0;
		for(final int x : Guilds.keySet())if(n<x)n = x;
		return n+1;
	}

	public static boolean guildNameIsUsed(final String name)
	{
		for(final Guild g : Guilds.values())
			if(g.getName().equalsIgnoreCase(name))
				return true;
		
		return false;
	}
	public static boolean guildEmblemIsUsed(final String emb)
	{
		for(final Guild g : Guilds.values())
		{
			if(g.getEmblem().equals(emb))return true;
		}
		return false;
	}
	public static Guild getGuild(final int i)
	{
		return Guilds.get(i);
	}
	public static long getGuildXpMin(int _lvl)
	{
		if(_lvl > Config.MAX_LEVEL) 	_lvl = Config.MAX_LEVEL;
		if(_lvl < 1) 	_lvl = 1;
		return ExpLevels.get(_lvl).guilde;
	}
	public static long getGuildXpMax(int _lvl)
	{
		if(_lvl >= Config.MAX_LEVEL) 	_lvl = Config.MAX_LEVEL-1;
		if(_lvl <= 1)	 	_lvl = 1;
		return ExpLevels.get(_lvl+1).guilde;
	}
	
	public static void reassignAccountToChar(final Account C)
	{
		C.getPlayers().clear();
		SQLManager.LOAD_PERSO_BY_ACCOUNT(C.getGUID());
		for(final Player P : Players.values())
		{
			if(P.getAccID() == C.getGUID())
			{
				C.addPerso(P);
				P.setAccount(C);
			}
		}
	}
	
	/*public static void reassignAccountToCharacter(final Account C)
	{
		C.getPlayers().clear();
		for(final Player P : Players.values())
		{
			if(P.getAccID() == C.getGUID())
			{
				C.addPerso(P);
				P.setAccount(C);
			}
		}
	}*/
	
	public static int getZaapCellIdByMapId(final int i)
	{
		for(final int[] zaap : Constants.AMAKNA_ZAAPS)
		{
			if(zaap[0] == i)return zaap[1];
		}
		for(final int[] zaap : Constants.INCARNAM_ZAAPS)
		{
			if(zaap[0] == i)return zaap[1];
		}
		return -1;
	}
	public static int getZaapiCellIdByMapId(final int i)
	{
		for(final int[] zaapi : Constants.BONTA_ZAAPI)
		{
			if(zaapi[0] == i)return zaapi[1];
		}
		for(final int[] zaapi : Constants.BRAKMAR_ZAAPI)
		{
			if(zaapi[0] == i)return zaapi[1];
		}
		return -1;
	}

	public static void delDragoByID(final int getId)
	{
		Mounts.remove(getId);
	}

	public static void removeGuild(final int id)
	{
		Guilds.remove(id);
		SQLManager.DEL_GUILD(id);
	}

	public static int ipIsUsed(final String ip)
	{
		int used = 0;
		for(final Account c : Accounts.values())
			if(c.getCurIP().equals(ip) && (c.getGameThread() != null) /*|| c.getRealmThread() != null)*/)
				used++;
		
		return used;
	}

	public static void unloadPerso(final int g)
	{
		Player toRem = Players.get(g);
		for(final Entry<Integer,Item> curObj : toRem.getItems().entrySet())
		{
			Items.remove(curObj.getKey());
		}
		toRem = null;
		Players.remove(g);
	}
	
	public static void addHdv(final BigStore toAdd)
	{
		BigStores.put(toAdd.getHdvID(),toAdd);
	}
	
	public static BigStore getHdv(final int mapID)
	{
		return BigStores.get(mapID);
	}
	
	public synchronized static int getNextHdvID()//ATTENTION A NE PAS EXECUTER POUR RIEN CETTE METHODE CHANGE LE PROCHAIN ID DE L'HDV LORS DE SON EXECUTION
	{
		nextHdvID++;
		return nextHdvID;
	}
	public synchronized static void setNextHdvID(final int nextID)
	{
		nextHdvID = nextID;
	}
	public synchronized static int getNextLigneID()
	{
		nextLineID++;
		return nextLineID;
	}
	public synchronized static void setNextLigneID(final int ligneID)
	{
		nextLineID = ligneID;
	}
	public synchronized static int getNewPrismId() {
		nextPrismId++;
		return nextPrismId;
	}

	public static boolean isArenaMap(final int mapID)
	{
		for(final int curID : Config.arenaMap)
		{
			if(curID == mapID)
				return true;
		}
		return false;
	}
	
	public static Map<Integer,ArrayList<BigStoreEntry>> getMyItems(final int compteID)
	{
		if(BigStoreItems.get(compteID) == null)	//Si le compte n'est pas dans la memoire
			BigStoreItems.put(compteID,new HashMap<Integer,ArrayList<BigStoreEntry>>());	//Ajout du compte clé:compteID et un nouveau map<hdvID,items
			
		return BigStoreItems.get(compteID);
	}
	public static void addHdvItem(final int compteID, final int hdvID, final BigStoreEntry toAdd)
	{
		if(BigStoreItems.get(compteID) == null)	//Si le compte n'est pas dans la memoire
			BigStoreItems.put(compteID,new HashMap<Integer,ArrayList<BigStoreEntry>>());	//Ajout du compte clé:compteID et un nouveau map<hdvID,items<>>
			
		if(BigStoreItems.get(compteID).get(hdvID) == null)
			BigStoreItems.get(compteID).put(hdvID,new ArrayList<BigStoreEntry>());
			
		BigStoreItems.get(compteID).get(hdvID).add(toAdd);
	}
	public static void removeHdvItem(final int compteID,final int hdvID,final BigStoreEntry toDel)
	{
		BigStoreItems.get(compteID).get(hdvID).remove(toDel);
	}
	public static void addKamasToAcc(final int compteID,final int amount)
	{
		if(getAccount(compteID) != null)
		{
			final Account toAdd = getAccount(compteID);
			toAdd.addBankKamas(amount);
		}
		else
		{
			SQLManager.UPDATE_ACCOUNT_BANKKAMAS(compteID,amount);
		}
	}
	public static Item newObjet(final int Guid, final int template,final int qua, final int pos, final String strStats)
	{
		if(World.getItemTemplate(template).getType() == 85)
			return new SoulStone(Guid, qua, template, pos, strStats);
		else
			return new Item(Guid, template, qua, pos, strStats);
	}
	
	public static Map<Integer,ItemTemplate> getObjTemplates()
	{
		return ObjTemplates;
	}
	
	public static int getMapNumber()
	{
		return DofusMaps.size();
	}
	public static int getGuildNumber()
	{
		return Guilds.size();
	}
	public static int getHdvNumber()
	{
		return BigStores.size();
	}
	public static int getHdvObjetsNumber()
	{
		int size = 0;
		
		for(final Map<Integer,ArrayList<BigStoreEntry>> curCompte : BigStoreItems.values())
		{
			for(final ArrayList<BigStoreEntry> curHdv : curCompte.values())
			{
				size += curHdv.size();
			}
		}
		return size;
	}
	public static void addGift(final Gift gift) {
		Gifts.put(gift.get_id(), gift);
	}

	public static Gift getGift(final int id) {
		return Gifts.get(id);
	}

	public static boolean giftContain(final int id) {
		return Gifts.containsKey(id);
	}
	
	public static TaxCollector getTaxCollector(final int Id) {
		return TaxCollectors.get(Id);
	}

	public static Collection<TaxCollector> getTaxCollectors() {
	    return TaxCollectors.values();
    }

	public static void delTaxCollector(final int id) {
		TaxCollectors.remove(id);
    }

	public static void addTaxCollector(final TaxCollector percepteur) {
		TaxCollectors.put(percepteur.getActorId(), percepteur);
    }
	
	public static void removeTaxCollector(final int percepteurId) {
		TaxCollectors.remove(percepteurId);
	}

	public static void addQuest(final Quest quest) {
	    Quests.put(quest.getId(), quest);
    }

	public static Map<Integer, Quest> getQuests() {
	    return Quests;
    }

	public static void addQuestSteps(final QuestStep questStep) {
	    QuestSteps.put(questStep.getId(), questStep);
    }

	public static Map<Integer, QuestStep> getQuestSteps() {
	    return QuestSteps;
    }

	public static void addQuestObjectives(final QuestObjective questObjective) {
	    QuestObjectives.put(questObjective.getId(), questObjective);
    }

	public static Map<Integer, QuestObjective> getQuestObjectives() {
	    return QuestObjectives;
    }

	public static void setState(final int state) {
	    _state  = state;
	    char c = 'O';
	    if(state == 0)
	    	c = 'D';
	    else if(state == 1)
	    	c = 'O';
	    else if(state == 2)
	    	c = 'S';
	    _stateID = c;
	    Main.linkServer.sendChangeState(c);
    }

	public static int getState() {
	    return _state;
    }
	
	public static String getTextState()
	{
		String textState = "";
		switch (_stateID) {
		case 'D':
			textState = "Unavailable";
			break;
		case 'O':
			textState = "Online";
			break;
		case 'S':
			textState = "Saving";
			break;
		case 'L':
			textState = "Loading";
			break;
		case 'A':
			textState = "Server stopped";
			break;
		default:
			textState = "Unknown status";
			break;
		}
		return textState;
	}

	public static Map<Integer, Player> GetPersosByCompte(final int guid) {
		final Map<Integer, Player> list = new TreeMap<Integer, Player>();
		for (final Player P : Players.values()) {
			if (P.getAccID() == guid) {
				list.put(P.getActorId(), P);
			}
		}
		return list;
    }

	public static void deleteAccount(final int guid) {
	    Accounts.remove(guid);
    }

	public static void addtoPlayerItems_List(final int ObjectID, final int PlayerID) {
		PlayerItems.put(ObjectID, PlayerID);
	}

	public static Map<Integer, Integer> getPlayerItems() {
		return PlayerItems;
	}
	
	public static void deletePlayerItems(final int playerGuid) {
		final ArrayList<Integer> toRem = new ArrayList<Integer>();
		for(final Entry<Integer, Integer> entry : PlayerItems.entrySet())
		{
			if(entry.getValue() == playerGuid)
			{
				toRem.add(entry.getKey());
			}
		}
		for(final int guid : toRem)
		{
			PlayerItems.remove(guid);
			Items.get(guid);
		}
	}
	
	public static boolean isItemFollower(final int itemId)
	{
		for(final MonsterFollower monsterFollower : monstersFollower.values())
		{
			if(monsterFollower.getItemId() == itemId)
			{
				return true;
			}
		}
		return false;
	}

	public static void addMonsterFollower(final MonsterFollower monsterFollower) {
		monstersFollower.put(monsterFollower.getMobId(), monsterFollower);
    }

	public static Collection<Integer> getSeller(final int mapID) {
		return Sellers.get(mapID);
	}

	public static void removeSeller(final int pID, final int mapID) {
		Sellers.get(mapID).remove(pID);
	}

	public static void addSeller(final Player p) {
		if (Sellers.get(p.getCurMap().getId()) == null) {
			final ArrayList<Integer> PersoID = new ArrayList<Integer>();
			PersoID.add(p.getActorId());
			Sellers.put(p.getCurMap().getId(), PersoID);
		} else {
			final ArrayList<Integer> PersoID = new ArrayList<Integer>();
			PersoID.addAll(Sellers.get(p.getCurMap().getId()));
			PersoID.add(p.getActorId());
			Sellers.remove(p.getCurMap().getId());
			Sellers.put(p.getCurMap().getId(), PersoID);
		}
    }

	public static Collection<DofusMap> getCartes() {
		return DofusMaps.values();
    }

	public static void moveEntities() {
		synchronized(DofusMaps.values())
		{
			for(final DofusMap curCarte : DofusMaps.values())
			{
				if(curCarte != null)
				{
					final char[] dirs = {'b','d','f','h'};
					if(curCarte.getMobGroups() != null && curCarte.getMobGroups().size() >0)
					{
						final MonsterGroup mobGroup = curCarte.getMobGroups().get(Formulas.getRandomValue(0, curCarte.getMobGroups().size()-1));
						if(mobGroup != null)
						{
							if(mobGroup.getMobs().get(0).getTemplate().getID() == 494) //On ne fait pas se déplacer les poutchs
							{
								continue;
							}
							final char dir = dirs[Formulas.getRandomValue(0, 3)];
							final Couple<Integer, String> couplePath = Pathfinding.getSpecialPath(curCarte, 
									dir, mobGroup.getCellId(), Formulas.getRandomValue(2, 8));
							SocketManager.GAME_SEND_GA_PACKET_TO_MAP(curCarte, "0", 1, String.valueOf(mobGroup.getActorId()), 
									"a"+CryptManager.getHashedValueByInt(mobGroup.getCellId())+couplePath.second);
							mobGroup.setCellID(couplePath.first);
							mobGroup.setOrientation((byte)Pathfinding.getOrientationFromDir(dir));
						}
					}
					if(curCarte.getPercepteur() != null)
					{
						final char dir = dirs[Formulas.getRandomValue(0, 3)];
						final TaxCollector percepteur = curCarte.getPercepteur();
						final Couple<Integer, String> couplePath = Pathfinding.getSpecialPath(curCarte, 
								dir, percepteur.getCellId(), Formulas.getRandomValue(2, 8));
						SocketManager.GAME_SEND_GA_PACKET_TO_MAP(curCarte, "0", 1, String.valueOf(percepteur.getActorId()), 
								"a"+CryptManager.getHashedValueByInt(percepteur.getCellId())+couplePath.second);
						percepteur.setCellId(couplePath.first);
						percepteur.setOrientation((byte)Pathfinding.getOrientationFromDir(dir));
					}
				}
			}
		}
    }

	public static void addPetTemplates(final PetTemplate petTemplate) {
	    PetTemplates.put(petTemplate.getTemplateId(), petTemplate);
    }

	public static Map<Integer,PetTemplate> getPetTemplates() {
	    return PetTemplates;
    }
	
	public static PetTemplate getPetTemplate(final int id)
	{
		if(PetTemplates.containsKey(id)) 
		{
			return PetTemplates.get(id);
		}
		return null;
	}

	public static ArrayList<BreedSpell> getBreedSpells() {
	    return BreedSpells;
    }
	
	public static void addRaceSpell(final BreedSpell breedSpell) {
		BreedSpells.add(breedSpell);
	}

	public static Breed getBreed(final int breed) {
		return Breeds.get(breed);
	}

	public static void addBreed(final Breed breed) {
		Breeds.put(breed.getId(), breed);
	}

	public static void increaseStarMonsterGroup() {
		synchronized(DofusMaps.values())
		{
			for(final DofusMap map : DofusMaps.values())
			{
				for(final MonsterGroup monsterGroup : map.getMobGroups().values())
				{
					monsterGroup.addStarPercent();
				}
			}
		}
	}

	public static void updateSQLDatas() {
		// TODO Auto-generated method stub
		
	}

	public static Map<Integer, Prism> getPrisms() {
		return Prisms;
	}
	
	public static Prism getPrism(final int id) {
		return Prisms.get(id);
	}

	public static void addPrism(final Prism prism) {
		Prisms.put(prism.getId(), prism);
	}

	public static Collection<SubArea> getSubAreas() {
		return SubAreas.values();
	}

	public static Alignment getAlignment(final int id) {
		return Alignments.get(id);
	}

	public static void initAlignments() {
		Alignments.put(-1, new Alignment(-1));
		Alignments.put(0, new Alignment(0));
		Alignments.put(1, new Alignment(1));
		Alignments.put(2, new Alignment(2));
		Alignments.put(3, new Alignment(3));
	}
	
	public static FastCommand getCommand(final String name)
	{
		for(final Entry<String, FastCommand> curCmd : Commands.entrySet())
		{
			if(curCmd.getKey() == name)
			{
				return curCmd.getValue();
			}
		}
		return null;
	}
	
	public static boolean isCommand(final String name)
	{
		return Commands.containsKey(name);
	}

	public static String getNickName(final int index) {
		return NickNames.get(index) != null ? NickNames.get(index) : "null";
	}

	public static List<String> getNickNames() {
		return NickNames;
	}

	public static Rune getRune(final int itemId) {
		return Runes.get(itemId);
	}

	public static void addRune(final Rune rune) {
		Runes.put(rune.getItemId(), rune);
	}

	public static void manageMeals() 
	{
		synchronized(getSpeakingItems())
		{
			for (final Item obj : getSpeakingItems()) 
			{
				final Speaking obv = Speaking.toSpeaking(obj);
				try 
				{
					String Date = obv.getLastEat();
					if (Date.contains("-"))
					{
						if (Formulas.compareTime(Date, Constants.ITEM_TIME_FEED_MAX))
							continue;
						obv.setState(0);
					} else {
						Date = Formulas.getDate(("325#" + obv.getTxtStat().get(Constants.EFFECT_RECEIVED_DATE)).split("#"));
						if (Formulas.compareTime(Date, Constants.ITEM_TIME_FEED_MAX))
							continue;
						obv.setState(0);
					}
				} catch (final Exception e) 
				{
					Log.addToLog("Erreur Speaking: " + e.getMessage());
				}
			}
		}
		synchronized(getPetItems())
		{
			for(final Item obj : getPetItems())
			{
				final Pet pet = (Pet)obj;
				try
				{
					final String Date = pet.getLastTimeAte();
					if (Date.contains("-")) 
					{
						if (Formulas.compareTime(Date, pet.getPetTemplate().getTimeBetweenMeals().second))
							continue;
						pet.setState((byte)0);
					}
				} catch (final Exception e) 
				{
					Log.addToLog("Erreur Pet: " + e.getMessage());
				}
			}
		}
	}

	public static Player getPlayer(String name) {
		for(Player player : Players.values()) {
			if(player.getName() == name) {
				return player;
			}
		}
		return null;
	}
	
}

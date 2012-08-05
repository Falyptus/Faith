package objects.character;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.Timer;

import objects.GameActor;
import objects.account.Account;
import objects.action.GameAction;
import objects.fight.Fight;
import objects.fight.Fighter;
import objects.guild.Guild;
import objects.guild.GuildMember;
import objects.item.Item;
import objects.item.ItemTemplate;
import objects.item.Pet;
import objects.item.Speaking;
import objects.job.Job;
import objects.job.JobAction;
import objects.job.JobStat;
import objects.map.DofusCell;
import objects.map.DofusMap;
import objects.map.InteractiveObject;
import objects.map.MountPark;
import objects.quest.Quest;
import objects.quest.QuestObjective;
import objects.spell.SpellEffect;
import objects.spell.SpellStat;

import common.Config;
import common.Constants;
import common.Logger;
import common.Main;
import common.SQLManager;
import common.SocketManager;
import common.World;
import common.World.Couple;
import common.World.Exchange;
import common.World.ItemSet;
import common.World.MonsterFollower;
import common.console.Log;
import common.utils.CryptManager;
import common.utils.Formulas;

public class Player implements GameActor {
	
	private int _GUID;
	private String _name;
	private int _sex;
	private int _breedId;
	private int _color1;
	private int _color2;
	private int _color3;
	private long _kamas;
	private int _spellPts;
	private int _capital;
	private int _energy;
	private int _lvl;
	private long _curExp;
	private int _size;
	private int _gfxId;
	//private int _isMerchant = 0;
	private int _orientation = 1;
	private Account _account;
	private int _accID;
	private boolean _canAggro = true;
	private Emote _emotes = null;
	
	//Variables d'ali
	private byte _align = 0;
	private int _deshonor = 0;
	private int _honor = 0;
	private boolean _showWings = false;
	private int _aLvl = 0;
	//Fin ali
	
	private GuildMember _guildMember;
	private boolean _showFriendConnection;
	private String _channels;
	private Stats _baseStats;
	private Fight _fight;
	private boolean _away;
	private DofusMap _curMap;
	private DofusCell _curCell;
	private int _PDV;
	private boolean _isInBank;
	private int _PDVMAX;
	private boolean _sitted;
	private boolean _ready = false;
	private boolean _isOnline  = false;
	private Party _party;
	private int _isTradingWith = 0;
	private Exchange _curExchange;
	private int _isTalkingWith = 0;
	private int _inviting = 0;
	private int _duelID = -1;
	private Map<Integer,SpellStat> _spells = new TreeMap<Integer,SpellStat>();
	private Map<Integer,Character> _spellsPlaces = new TreeMap<Integer,Character>();
	
	private Map<Integer,SpellEffect> _buffs = new TreeMap<Integer,SpellEffect>(); 
	private Map<Integer,Item> _items = new TreeMap<Integer,Item>();
	private Map<Integer,JobStat> _jobs = new TreeMap<Integer,JobStat>();
	private Timer _sitTimer;
	private String _savePos;
	private int _exPdv;
	private MountPark _inMountPark;//Enclos
	private int emoteActive = 0;
	private int emoteTime = 360000;
	private JobAction _curJobAction;
	private Mount _mount;
	private int _mountXpGive = 0;
	private boolean _onMount = false;
	//Zaap
	private boolean _isZaaping = false;
	private ArrayList<Integer> _zaaps = new ArrayList<Integer>();
	private boolean _isZaapiing = false;
	//Disponibilité
	public boolean isAway = false;
	public boolean isInvisible = false;
	//Logger
	private Logger logger;
	//Interface d'oublie de sort
	private boolean isForgetingSpell = false;
	//Title
	private byte title = 0;
	//Emotes 		
	private Timer emoteTimer = null;
	private String _tempSavePos; //Sauvegarde temporaire pour récupérer une position après un combat percepteur/prisme/autres...
	private Player _track; //Personnage à traquer
	private long _trackTime; //Dernière traque lancée (en ms)
	private ArrayList<Quest> _questList = new ArrayList<Quest>();
	private int _lastItemUsed;
	private Restriction _restrictions = null;
	private boolean _changePseudo = false;
	private long _lastPacketTime;
	private Map<Integer, MonsterFollower> monstersFollower = new TreeMap<Integer, MonsterFollower>();
	private Map<Integer, Integer> _storeItems = new TreeMap<Integer, Integer>();
	private boolean _seeSeller = false;
	private Right _rights = null;
	private boolean _isDecoFromFight = false;
	private byte _isDead = 0;
	private byte _deathCount = 0;
	private int _levelReached = 0;
	private long _lastTimeShowWings;
	private String cacheGM = "";
	private String cacheGMStuff = "";
	private String cacheAS = "";
	private GameAction _curGameAction = null;
	private Spouse _spouse;
	//Anti flood/Spam
	private String lastSentence = "";
	private int sameSentences = 0;
	private int sentencesExcla = 0;
	private Timer timerAntiFlood;
	private int timeAntiFlood;
	private Timer timerAntiSpam;
	protected int nbrSentences;
	protected boolean isSpammer;
	private Timer timerBreakAntiSpam;
	private boolean _isGhost = false;
			
	public Player(int _guid, String _name, int _sexe, int _classe,
			int _color1, int _color2, int _color3,long _kamas, int pts, int _capital, int _energy, int _lvl, long exp,
			int _size, int _gfxid, byte alignement, int _compte, Map<Integer,Integer> stats,
			int seeFriend, int showWings, String canaux, int map, int cell,String stuff,int pdvPer,String spells, String savePos,String jobs,
			int mountXp,int mount,int honor,int deshonor,int alvl,String z, String monstersFollower, int restrictions, int rights, int emotes)
	{
		this._GUID = _guid;
		this._name = _name;
		this._sex = _sexe;
		this._breedId = _classe;
		this._color1 = _color1;
		this._color2 = _color2;
		this._color3 = _color3;
		this._kamas = _kamas;
		this._spellPts = pts;
		this._capital = _capital;
		this._align = alignement;
		this._honor = honor;
		this._deshonor = deshonor;
		this._aLvl = alvl;
		this._energy = _energy;
		this._lvl = _lvl;
		this._curExp = exp;
		if(mount != -1)this._mount = World.getDragoByID(mount);
		this._size = _size;
		this._gfxId = _gfxid;
		this._mountXpGive = mountXp;
		this._baseStats = new Stats(stats,true,this);
		this._accID = _compte;
		this._account = World.getAccount(_compte);
		this._showFriendConnection = seeFriend==1;
		this._showWings = showWings == 1;
		this._channels = canaux;
		this._curMap = World.getMap(map);
		this._savePos = savePos;
		if(_curMap == null && World.getMap(Config.CONFIG_START_MAP) != null)
		{
			this._curMap = World.getMap(Config.CONFIG_START_MAP);
			this._curCell = _curMap.getCell(Config.CONFIG_START_CELL);
		}else if (_curMap == null && World.getMap(Config.CONFIG_START_MAP) == null)
		{
			Log.addToLog("Personnage mal positione, et position de départ non valide. Fermeture du serveur.");
			Main.closeServers();
		}
		else if(_curMap != null)
		{
			this._curCell = _curMap.getCell(cell);
			if(_curCell == null)
			{
				this._curMap = World.getMap(Config.CONFIG_START_MAP);
				this._curCell = _curMap.getCell(Config.CONFIG_START_CELL);
			}
		}
		for(String str : z.split(","))
		{
			try
			{
				_zaaps.add(Integer.parseInt(str));
			}catch(Exception e){};
		}
		if(_curMap == null || _curCell == null)
		{
			Log.addToErrorLog("Start Map or Start Cell of player "+_name+" is invalid !");
			Log.addToErrorLog("Server is closing");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}
			Main.closeServers();
		}

		if(!stuff.equals(""))
		{
			if(stuff.charAt(stuff.length()-1) == '|')
				stuff = stuff.substring(0,stuff.length()-1);
			SQLManager.LOAD_ITEMS(stuff.replace("|",","));
		}
		for(String item : stuff.split("\\|"))
		{
			if(item.equals(""))continue;
			String[] infos = item.split(":");
			int guid = Integer.parseInt(infos[0]);
			Item obj = World.getObjet(guid);
			if( obj == null)continue;
			_items.put(obj.getGuid(), obj);
		}
		
		_PDVMAX = (_lvl-1)*5+Constants.getBasePdv(_breedId)+getTotalStats().getEffect(Constants.STATS_ADD_VITA)+getTotalStats().getEffect(Constants.STATS_ADD_VIE);
		this._PDV = (_PDVMAX*pdvPer)/100;
		parseSpells(spells);

		_sitTimer = new Timer(2000,new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				regenLife();
			}
		});
		_exPdv = _PDV;
		
		
		//Chargement des métiers
		if(!jobs.equals(""))
		{
			for(String aJobData : jobs.split(";"))
			{
				String[] infos = aJobData.split(",");
				try
				{
					int jobID = Integer.parseInt(infos[0]);
					long xp = Long.parseLong(infos[1]);
					Job m = World.getMetier(jobID);
					JobStat SM = _jobs.get(learnJob(m));
					SM.addXp(this, xp);
				}catch(Exception e){e.getStackTrace();}
			}
		}
		
		if(monstersFollower != null)
		{
			if(!monstersFollower.isEmpty() || monstersFollower.length() > 0)
			for(String aMonsterData : monstersFollower.split(";"))
			{
				String[] infos = aMonsterData.split(",");
				MonsterFollower mf = new MonsterFollower(Integer.parseInt(infos[0]), Integer.parseInt(infos[1]), Integer.parseInt(infos[2]));
				this.monstersFollower.put(mf.getItemId(), mf);
			}
		}
		
		_restrictions = new Restriction(restrictions);
		_rights = new Right(rights);
		_emotes = new Emote(emotes);
	}

	public Player() 
	{
	    
    }

	public void regenLife()
	{
		//Joueur pas en jeu
		if(_curMap == null)return;
		//Pas de regen en combat
		if(_fight != null)return;
		//Déjà Full PDV
		if(_PDV == _PDVMAX)return;
		_PDV++;
	}
	
	public static Player CREATE_PLAYER(String name, int sexe, int classe, int color1, int color2, int color3,Account compte)
	{
		StringBuilder z = new StringBuilder();
		if(Config.CONFIG_ZAAP_ANK)
		{
			for(int[] i : Constants.AMAKNA_ZAAPS)
			{
				if(z.length() != 0)z.append(",");
				z.append(i[0]);
			}
		}
		if(Config.CONFIG_ZAAP_INC)
		{
			for(int[] i : Constants.INCARNAM_ZAAPS)
			{
				if(z.length() != 0)z.append(",");
				z.append(i[0]);
			}
		}
		Player perso = new Player(
				SQLManager.getNextPersonnageGuid(),
				name,
				sexe,
				classe,
				color1,
				color2,
				color3,
				Config.CONFIG_START_KAMAS,
				((Config.CONFIG_START_LEVEL-1)*1),
				((Config.CONFIG_START_LEVEL-1)*5),
				Config.SERVER_ID == 22 ? 1 : 10000,
				Config.CONFIG_START_LEVEL,
				World.getPersoXpMin(Config.CONFIG_START_LEVEL),
				100,
				Integer.parseInt(classe+""+sexe),
				(byte)0,
				compte.getGUID(),
				new TreeMap<Integer,Integer>(),
				1,
				(byte)0,
				"*#%!pi$:?",
				World.getBreed(classe).getStartMap(),
				World.getBreed(classe).getStartCell(),
				"",
				100,
				"",
				World.getBreed(classe).getStartMap()+","+World.getBreed(classe).getStartCell(),
				"",
				0,
				-1,
				0,
				0,
				0,
				z.toString(),
				"",
				79,
				8196,
				1
				);
		perso.learnBreedSpell();
		perso.spellPlacement();
		perso.setRestrictions(new Restriction(0));
		perso.setRights(new Right(8192));
		perso.setEmotes(perso.initEmotes());
		/*---------------LIGNE PAR MARTHIEUBEAN-------------------*/
		//Ajoute un objet à la création du personnage
		if(Config.CONFIG_START_ITEM != 0)
		{
			ItemTemplate t = World.getItemTemplate(Config.CONFIG_START_ITEM);
							
			Item obj = t.createNewItem(1,false); //Si mis à "true" l'objet à des jets max. Sinon ce sont des jets aléatoire
			if(perso.addItem(obj, true))//Si le joueur n'avait pas d'item similaire
				World.addItem(obj,true);
		}
		/*----------------------FIN-------------------------------*/
		
		if(!SQLManager.ADD_PERSO_IN_BDD(perso))
			return null;
		
		World.addPersonnage(perso);
		
		SocketManager.GAME_SEND_TUTORIAL_ON_GAME_BEGIN(perso);
	
		return perso;
	}

	public void setOnline(boolean d)
	{
		_isOnline = d;
		if (d)
			SQLManager.SET_ONLINE(this._GUID);
		else
			SQLManager.SET_OFFLINE(this._GUID);
	}
	
	public boolean isOnline()
	{
		return _isOnline;
	}
	
	public void setParty(Party p)
	{
		_party = p;
	}

	public Party getParty()
	{
		return _party;
	}
	
	public String parseSpellToDB()
	{
		if(_spells.size() == 0)return "";
		StringBuilder sorts = new StringBuilder();
		for(int key : _spells.keySet())
		{
			//3;1;a,4;3;b
			SpellStat SS = _spells.get(key);
			sorts.append(SS.getSpellID()).append(';').append(SS.getLevel()).append(';');
			if(_spellsPlaces.get(key)!=null)
				sorts.append(_spellsPlaces.get(key));
			else
				sorts.append("_");
			sorts.append(",");
		}
		//sorts = sorts.substring(0, sorts.length()-1);
		return sorts.substring(0, sorts.length()-1);//sorts;
	}
	
	private void parseSpells(String str)
	{
		String[] spells = str.split(",");
		for(String e : spells)
		{
			try
			{
				int id = Integer.parseInt(e.split(";")[0]);
				int lvl = Integer.parseInt(e.split(";")[1]);
				char place = e.split(";")[2].charAt(0);
				learnSpell(id,lvl,false,false);
				_spellsPlaces.put(id, place);
			}catch(NumberFormatException e1){continue;};
		}
	}
	
	public String getSavePos() {
		return _savePos;
	}

	public void setSavePos(String savePos) {
		_savePos = savePos;
	}

	public int getIsTradingWith() {
		return _isTradingWith;
	}

	public void setIsTradingWith(int tradingWith) {
		_isTradingWith = tradingWith;
	}

	public int getIsTalkingWith() {
		return _isTalkingWith;
	}

	public void setIsTalkingWith(int talkingWith) {
		_isTalkingWith = talkingWith;
	}

	public long getKamas() {
		return _kamas;
	}

	public Map<Integer, SpellEffect> getBuffs() {
		return _buffs;
	}

	public void setKamas(long newKamas) {
		this._kamas = newKamas;
		clearCacheAS();
	}

	public Account getAccount() {
		return _account;
	}

	public int getSpellPts() {
		return _spellPts;
	}

	public void setSpellPts(int pts) {
		_spellPts = pts;
		clearCacheAS();
	}

	public Guild getGuild()
	{
		if(_guildMember == null)return null;
		return _guildMember.getGuild();
	}

	public void setGuildMember(GuildMember _guild) {
		this._guildMember = _guild;
	}
	
	public boolean isReady() {
		return _ready;
	}

	public void setReady(boolean _ready) {
		this._ready = _ready;
	}

	public int getDuelID() {
		return _duelID;
	}

	public Fight getFight() {
		return _fight;
	}

	public void setDuelID(int _duelid) {
		_duelID = _duelid;
	}
	
	public void loseEnergy(int energyToLose) {
	    _energy -= energyToLose;
	    _PDV = 1;
	    SocketManager.GAME_SEND_Im_PACKET(this, "034;"+energyToLose);
	    if(_energy <= 0)
	    {
	    	becomeSoulStone();
	    	return;
	    }
	    teleportToSavePos();
    }

	public int getEnergy() {
		return _energy;
	}

	public boolean isShowFriendConnection() {
		return _showFriendConnection;
	}
	
	public void setShowFriendConnection(boolean bool) {
		_showFriendConnection = bool;
	}

	public String getChannels() {
		return _channels;
	}

	public void setEnergy(int _energy) {
		this._energy = _energy;
		clearCacheAS();
	}

	public int getLvl() {
		return _lvl;
	}

	public void setLvl(int _lvl) {
		this._lvl = _lvl;
	}

	public long getCurExp() {
		return _curExp;
	}

	public DofusCell getCurCell() {
		return _curCell;
	}

	public void setCurCell(DofusCell cell) {
		_curCell = cell;
	}

	public void setCurExp(long exp) {
		_curExp = exp;
		clearCacheAS();
	}

	public int getSize() {
		return _size;
	}

	public void setSize(int _size) {
		this._size = _size;
		clearCacheGM();
	}

	public void setFight(Fight _fight) {
		this._fight = _fight;
	}

	public int getGfxID() {
		return _gfxId;
	}

	public void setGfxID(int _gfxid) {
		_gfxId = _gfxid;
		clearCacheGM();
	}

	public DofusMap getCurMap() {
		return _curMap;
	}
	
	public void setName(String name) {
		_name = name;
		clearCacheGM();
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_curMap, _GUID);
		SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(_curMap, this);
		SQLManager.SAVE_PERSONNAGE(this, false);
	}

	public String getName() {
		return _name;
	}

	public boolean isAway() {
		return _away;
	}

	public void setAway(boolean _away) {
		this._away = _away;
	}

	public boolean isSitted() {
		return _sitted;
	}

	public int getSexe() {
		return _sex;
	}

	public int getBreedId() {
		return _breedId;
	}
	
	public void setBreedId(int _breedId) {
		this._breedId = _breedId;
		clearCacheGM();
	}

	public int getColor1() {
		return _color1;
	}

	public int getColor2() {
		return _color2;
	}

	public Stats getBaseStats() {
		return _baseStats;
	}

	public int getColor3() {
		return _color3;
	}

	public int getCapital() {
		return _capital;
	}
	
	public void setColors(int color1, int color2, int color3) {
		if(color1 != -1) _color1 = color1;
		if(color2 != -1) _color2 = color2;
		if(color3 != -1) _color3 = color3;
		clearCacheGM();
	}
	
	public Map<Integer, SpellStat> getSpells() {
		return _spells;
	}
	
	public Map<Integer, Character> getSpellPlaces() {
		return _spellsPlaces;
	}
	
	public void spellPlacement() 
	{
		TreeMap<Integer, Character> toPlace = new TreeMap<Integer, Character>();
		try 
		{
			for(SpellStat spell : _spells.values()) 
			{
				for(BreedSpell breedSpell : World.getBreedSpells())
				{
					if(breedSpell.getSpellId() == spell.getSpellID() && breedSpell.getBreed() == _breedId) 
					{
						char pos = CryptManager.getHashedValueByInt(breedSpell.getPos());
						for(Entry<Integer, Character> entry : _spellsPlaces.entrySet()) 
						{
							if(entry != null && entry.getValue() == pos) 
							{
								continue;
							}
							toPlace.put(spell.getSpellID(), pos);
						}
					}
				}
			}
		} catch(Exception e) 
		{
			System.out.println("Player "+_name+" error when placing spell");
		}
		_spellsPlaces = toPlace;
	}
	
	public void learnBreedSpell() 
	{
		ArrayList<BreedSpell> toLearn = new ArrayList<BreedSpell>();
		for(BreedSpell breedSpell : World.getBreedSpells()) 
		{
			if(breedSpell.getBreed() == _breedId && breedSpell.getLvl() <= _lvl && !hasSpell(breedSpell.getSpellId()))
			{
				toLearn.add(breedSpell);
			}
		}
		for(BreedSpell breedSpell : toLearn)
		{
			addSpell(breedSpell.getSpellId(), 1, breedSpell.getPos());
		}
	}
	
	public synchronized void addSpell(int spellID, int lvl, int pos) 
	{
		if(!hasSpell(spellID)) 
		{
			if(lvl < 1) lvl = 1;
			if(lvl > 6) lvl = 6;
			if(pos < 1) pos = 1;
			if(pos > 25) pos = 25;
			if(World.getSpell(spellID).getStatsByLevel(lvl)==null)
			{
				Log.addToLog("[ERROR]Sort "+spellID+" lvl "+lvl+" non trouvé.");
				return;
			}
			_spells.put(spellID, World.getSpell(spellID).getStatsByLevel(lvl));
		}
	}
	
	public boolean hasSpell(int spellID)
	{
		return (getSortStatBySortIfHas(spellID) == null ? false : true);
    }
	
	public synchronized boolean learnSpell(int spellID,int level,boolean save,boolean send)
	{
		if(World.getSpell(spellID).getStatsByLevel(level)==null)
		{
			Log.addToLog("[ERROR]Sort "+spellID+" lvl "+level+" non trouvé.");
			return false;
		}
		_spells.put(spellID, World.getSpell(spellID).getStatsByLevel(level));
		if(send)
		{
			SocketManager.GAME_SEND_SPELL_LIST(this);
			SocketManager.GAME_SEND_Im_PACKET(this, "03;"+spellID);
		}
		if(save)SQLManager.SAVE_PERSONNAGE(this,false);
		return true;
	}
	
	public boolean boostSpell(int spellID)
	{
		if(getSortStatBySortIfHas(spellID)== null)
		{
			Log.addToLog(_name+" n'a pas le sort "+spellID);
			return false;
		}
		int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
		if(AncLevel == 6)return false;
		if(_spellPts>=AncLevel && World.getSpell(spellID).getStatsByLevel(AncLevel+1).getReqLevel() <= _lvl)
		{
			if(learnSpell(spellID,AncLevel+1,true,false))
			{
				_spellPts -= AncLevel;
				SQLManager.SAVE_PERSONNAGE(this,false);
				return true;
			}else
			{
				Log.addToLog(_name+" : Echec LearnSpell "+spellID);
				return false;
			}
		}
		else//Pas le niveau ou pas les Points
		{
			if(_spellPts<AncLevel)
				Log.addToLog(_name+" n'a pas les points requis pour booster le sort "+spellID+" "+_spellPts+"/"+AncLevel);
			if(World.getSpell(spellID).getStatsByLevel(AncLevel+1).getReqLevel() > _lvl)
				Log.addToLog(_name+" n'a pas le niveau pour booster le sort "+spellID+" "+_lvl+"/"+World.getSpell(spellID).getStatsByLevel(AncLevel+1).getReqLevel());
			return false;
		}
	}
	
	public boolean forgetSpell(int spellID)
	{
		if(getSortStatBySortIfHas(spellID)== null)
		{
			Log.addToLog(_name+" n'a pas le sort "+spellID);
			return false;
		}
		int AncLevel = getSortStatBySortIfHas(spellID).getLevel();
		if(AncLevel <= 1)return false;
		
		if(learnSpell(spellID,1,true,false))
		{
			_spellPts += Formulas.spellCost(AncLevel);
			
			SQLManager.SAVE_PERSONNAGE(this,false);
			return true;
		}else
		{
			Log.addToLog(_name+" : Echec LearnSpell "+spellID);
			return false;
		}
		
	}
	
	public String parseSpellList()
	{
		StringBuilder packet = new StringBuilder(2+(13*_spells.size())).append("SL");
		for (Iterator<SpellStat> i = _spells.values().iterator() ; i.hasNext();)
		{
		    SpellStat SS = i.next();
			packet.append(SS.getSpellID()).append('~').append(SS.getLevel()).append('~').append(_spellsPlaces.get(SS.getSpellID())).append(';');
		}
		return packet.toString();
	}

	public void setSpellPlace(int SpellID, char Place)
	{
		replaceSpellInBook(Place);
		_spellsPlaces.remove(SpellID);	
		_spellsPlaces.put(SpellID, Place);
		SQLManager.SAVE_PERSONNAGE(this,false);//On sauvegarde les changements
	}

	private void replaceSpellInBook(char Place)
	{
		for(int key : _spells.keySet())
		{
			if(_spellsPlaces.get(key)!=null)
			{
				if (_spellsPlaces.get(key).equals(Place))
				{
					_spellsPlaces.remove(key);
				}
			}
		}
	}
	
	public SpellStat getSortStatBySortIfHas(int spellID)
	{
		return _spells.get(spellID);
	}
	
	public String parseALK()
	{
		StringBuilder perso = new StringBuilder(20);
		perso.append('|');
		perso.append(this._GUID).append(';');
		perso.append(this._name).append(';');
		perso.append(this._lvl).append(';');
		perso.append(this._gfxId).append(';');
		perso.append((this._color1!= -1?Integer.toHexString(this._color1):"-1")).append(';');
		perso.append((this._color2!= -1?Integer.toHexString(this._color2):"-1")).append(';');
		perso.append((this._color3!= -1?Integer.toHexString(this._color3):"-1")).append(';');
		perso.append(getGMStuffString()).append(';');
		perso.append(_seeSeller ? 1 : 0).append(';');//merchant
		perso.append(Config.SERVER_ID).append(';');
		perso.append(';');//DeathCount	this.deathCount;
		perso.append(';');//LevelMax
		return perso.toString();
	}

	/*public void remove()
	{
		SQLManager.DELETE_PERSO_IN_BDD(this);
	}*/

	public void OnJoinGame()
	{
		SocketManager.start_buffering(_account.getGameThread().getOut());
		if (_account == null || _account.getGameThread() == null)
			return;
		PrintWriter out = _account.getGameThread().getOut();
		_account.setCurPerso(this);
		setOnline(true);
		
		manageSpeakingsOnStartup();
		if(_mount != null)SocketManager.GAME_SEND_Re_PACKET(this,"+",_mount);
		SocketManager.GAME_SEND_Rx_PACKET(this);
		
		SocketManager.GAME_SEND_ASK(out, this);
		//Envoie des bonus pano si besoin
		for(int a = 1;a<World.getItemSetNumber();a++)
		{
			int num =getNumbEquipedItemOfSet(a);
			if(num == 0)continue;
			SocketManager.GAME_SEND_OS_PACKET(this, a);
		}
		
		//envoie des données de métier
		if(_jobs.size() >0)
		{
			ArrayList<JobStat> list = new ArrayList<JobStat>();
			list.addAll(_jobs.values());
			//packet JS
			SocketManager.GAME_SEND_JS_PACKET(this, list);
			//packet JX
			SocketManager.GAME_SEND_JX_PACKET(this, list);
			//Packet JO (Job Option)
			SocketManager.GAME_SEND_JO_PACKET(this, list);
			Item obj = getObjetByPos(Constants.ITEM_POS_ARME);
			if(obj != null)
			{
				for(JobStat sm : list)
					if(sm.getTemplate().isValidTool(obj.getTemplate().getID()))
						SocketManager.GAME_SEND_OT_PACKET(_account.getGameThread().getOut(),sm.getTemplate().getId());
			}
		}
		//Fin métier
		SocketManager.GAME_SEND_ALIGNEMENT(out, _align);
		SocketManager.GAME_SEND_ADD_CANAL(out,_channels+"^"+(_account.getGmLvl()>0?"@¤":""));
		if(_guildMember != null)SocketManager.GAME_SEND_gS_PACKET(this,_guildMember);
		SocketManager.GAME_SEND_ZONE_ALLIGN_STATUT(out);
		SocketManager.GAME_SEND_SPELL_LIST(this);
		SocketManager.GAME_SEND_EMOTE_LIST(this,_emotes.get(),"0");
		SocketManager.GAME_SEND_RESTRICTIONS(out, _rights.toBase36());
		SocketManager.GAME_SEND_Ow_PACKET(this);
		SocketManager.GAME_SEND_SEE_FRIEND_CONNEXION(out,_account.isShowFriendConnection());
		sendOnlineToFriends();
		//Messages de bienvenue
		SocketManager.GAME_SEND_Im_PACKET(this, "189");
		if(!_account.getLastConnectionDate().equals("") && !_account.getLastIP().equals(""))
			SocketManager.GAME_SEND_Im_PACKET(this, "0152;"+_account.getLastConnectionDate()+"~"+_account.getLastIP());
		SocketManager.GAME_SEND_Im_PACKET(this, "0153;"+_account.getCurIP());
		//Fin messages
		//Actualisation de l'ip
		_account.setLastIP(_account.getCurIP());
		
		//Mise a jour du lastConnectionDate
		Date actDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd");
		String jour = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("MM");
		String mois = dateFormat.format(actDate);	
		dateFormat = new SimpleDateFormat("yyyy");
		String annee = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("HH");
		String heure = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("mm");
		String min = dateFormat.format(actDate);
		_account.setLastConnectionDate(annee+"~"+mois+"~"+jour+"~"+heure+"~"+min);
		if(_guildMember != null)
			_guildMember.setLastCo(annee+"~"+mois+"~"+jour+"~"+heure+"~"+min);
		
		//Actualisation dans la DB
		SQLManager.UPDATE_LASTCONNECTION_INFO(_account);
		
		if(!Config.CONFIG_MOTD.equals(""))//Si le motd est notifié
		{
			String color = Config.CONFIG_MOTD_COLOR;
			if(color.equals(""))color = "000000";//Noir
			
			SocketManager.GAME_SEND_MESSAGE(this, Config.CONFIG_MOTD, color);
		}
		
		if(_align > 0 && _align != 3 && _curMap.getSubArea().getAlignement() > 0 && _curMap.getSubArea().getAlignement() != _align) 
		{
			SocketManager.GAME_SEND_Im_PACKET(this, "091");
			teleportToSavePos();
		}
		
		if(Config.SERVER_ID != 22 && _energy > 0 && _energy <= 2000)
		{
			SocketManager.GAME_SEND_MESSAGE_SERVER(this, "111", ""+_energy);
		}
		//on démarre le Timer pour la Regen de Pdv
		_sitTimer.start();
		//on le demarre coté client
		SocketManager.GAME_SEND_ILS_PACKET(this, 2000);
		
		SocketManager.GAME_SEND_INFOS_COMPASS_PACKET(this, "|"); //On remet à zéro les drapeaux.
		initLogger();
		SocketManager.stop_buffering(_account.getGameThread().getOut());
	}
	
	private void sendOnlineToFriends() {
		// TODO Auto-generated method stub
		
	}

	public void sendGameCreate()
	{
		PrintWriter out = _account.getGameThread().getOut();
		SocketManager.GAME_SEND_GAME_CREATE(out,_name);
		SocketManager.GAME_SEND_STATS_PACKET(this);
		SocketManager.GAME_SEND_MAPDATA(out,_curMap.getId(),_curMap.getDate(),_curMap.getKey());
		SocketManager.GAME_SEND_MAP_FIGHT_COUNT(out,this.getCurMap());
		_curMap.addPlayer(this);
		
	}
	
	public void freeMySoul() 
	{
	    if(_energy > 0) //Sécurité
	    	return;
	    if(Config.SERVER_ID == 22)//Héroic Server
	    {
	    	SocketManager.GAME_SEND_GAME_OVER_PACKET(this);
	    	return;
	    }
	    _gfxId = 8004;
	    _PDV = 1;
	    setIsGhost(true);
	    _restrictions.setIsTombe(false);
	    _restrictions.setSlow(true);
	    _restrictions.setForceWalk(true);
	    _restrictions.setCanBeChallenge(false);
	    _restrictions.setCanExchange(false);
	    _restrictions.setCanBeAssault(false);

	    _rights.setAssault(false);
	    _rights.setCanExchange(false);
	    _rights.setCantSpeakWithNPC(false);
	    _rights.setCanBeMerchant(false);
	    _rights.setCantInteractWithTaxCollector(false);
	    _rights.setCantInteractWithPrism(false);
	    _rights.setCanUseObject(false);
	    _rights.setCanAttack(false);
	    _rights.setCanUseInteractiveObject(false);
	    _rights.setCanAttackMonstersAnyWhereWhenMutant(false);
	    _rights.setCantSpeakWithNPC(false);
	    
	    SocketManager.GAME_SEND_ALTER_GM_PACKET(_curMap, this);
	    SocketManager.GAME_SEND_RESTRICTIONS(_account.getGameThread().getOut(), _rights.toBase36());
	    teleportToCemetery();
	    SocketManager.GAME_SEND_INFO_HIGHLIGHT_PACKET(this, Constants.PHOENIX);
	    SocketManager.GAME_SEND_MESSAGE_SERVER(this, "115");
    }
	
	public void backToLife()
	{
		
	}
	
	public void becomeSoulStone() 
	{
		_gfxId = _breedId*10+_sex+3;
		_canAggro = false;
		_energy = 0;
		_restrictions.setCanBeAssault(false);
		_restrictions.setCanBeAttack(false);
		_restrictions.setCanBeChallenge(false);
		_restrictions.setCanExchange(false);
		_restrictions.setCanSwitchToCreature(false);
		_restrictions.setForceWalk(false);
		_restrictions.setIsTombe(false);
		_restrictions.setSlow(false);
		
		if(isOnMount()) toogleOnMount();
		SocketManager.GAME_SEND_MESSAGE_SERVER(this, "112");
		SocketManager.GAME_SEND_ALTER_GM_PACKET(_curMap, this);
		if(_curMap.hasEndFightAction(Constants.FIGHT_TYPE_PVM)) 
		{
			SocketManager.GAME_SEND_Im_PACKET(this, "1142");
			teleportToSavePos();
		} 
	}
	
	private void teleportToCemetery() 
	{
	    int[] cemeteryInfos = _curMap.getSubArea().getArea().getCemetery();
	    if(cemeteryInfos.length <= 0)
	    	return;
	    teleport(cemeteryInfos[0], cemeteryInfos[1]);
    }

	public String parseToOa()
	{
		StringBuilder packetOa = new StringBuilder();
		packetOa.append("Oa");
		
		packetOa.append(_GUID).append('|');
		packetOa.append(getGMStuffString());
			
		return packetOa.toString();
	}
	
	public String parseToGM()
	{
		if(cacheGM == "")
		{
			StringBuilder str = new StringBuilder(30);
			
			/*String name = "";
			if(showRank && _compte.get_gmLvl() > 0)
				name += "[" + Constants.getRankName(_compte.get_gmLvl()) + "]";
			name += _name;*/
			
			if(_fight == null)// Hors combat
			{
				str.append(getActorType()).append(';');
				str.append(_GUID).append(';').append(_name).append(';').append(_breedId);
				str.append((title>0?(","+title+";"):(';')));
				str.append(_gfxId).append('^').append(_size).append(';');
				str.append(_sex).append(';').append(_align).append(',');
				str.append(_align).append(",").append((_showWings?getGrade():"0")).append(',');
				str.append(_lvl+_GUID).append(',');
				str.append(_deshonor > 0 ? 1 : 0).append(';');
				str.append((_color1==-1?"-1":Integer.toHexString(_color1))).append(';');
				str.append((_color2==-1?"-1":Integer.toHexString(_color2))).append(';');
				str.append((_color3==-1?"-1":Integer.toHexString(_color3))).append(';');
				str.append(getGMStuffString()).append(';');
				str.append((_lvl>99?(_lvl>199?(getNumbEquipedItemOfSet(130)==6?(3):(2)):(1)):(0))).append(';');
				str.append(emoteActive).append(';');
				str.append(emoteTime).append(';'); //Emote timer
				if(this._guildMember!=null && this._guildMember.getGuild().getMembers().size()>9)
				{
					str.append(this._guildMember.getGuild().getName()).append(';').append(this._guildMember.getGuild().getEmblem()).append(';');
				}
				else str.append(';').append(';');
				str.append(_restrictions.toBase36()).append(';');
				str.append((_onMount&&_mount!=null?_mount.getStrColor():"")).append(';');
				str.append(';');
			}
			cacheGM = str.toString();
		}
		return new StringBuilder("").append(getCellId()).append(';').append(getOrientation()).append(';').append(cacheGM).toString();
	}
	
	public void clearCacheGM()
	{
		cacheGM = "";
	}
	
	public String getGMStuffString()
	{
		if(cacheGMStuff == "")
		{
			StringBuilder str = new StringBuilder(20);
			if (getObjetByPos(Constants.ITEM_POS_ARME) != null) 
				str.append(getGMStuffintTemplate(Constants.ITEM_POS_ARME));
			str.append(',');
			if (getObjetByPos(Constants.ITEM_POS_COIFFE) != null)
				str.append(getGMStuffintTemplate(Constants.ITEM_POS_COIFFE));
			str.append(',');
			if (getObjetByPos(Constants.ITEM_POS_CAPE) != null)
				str.append(getGMStuffintTemplate(Constants.ITEM_POS_CAPE));
			str.append(',');
			if (getObjetByPos(Constants.ITEM_POS_FAMILIER) != null)
				str.append(getGMStuffintTemplate(Constants.ITEM_POS_FAMILIER));
			str.append(',');
			if (getObjetByPos(Constants.ITEM_POS_BOUCLIER) != null)
				str.append(getGMStuffintTemplate(Constants.ITEM_POS_BOUCLIER));
			cacheGMStuff = str.toString();
		}
		return cacheGMStuff;
	}
	
	public void clearCacheGMStuff()
	{
		cacheGMStuff = "";
	}
	
	public String getGMStuffintTemplate(int pos)
	{
		StringBuilder str = new StringBuilder(10);
		Item Obj = getObjetByPos(pos);
		if (Obj.is_linked()) 
		{
			Speaking Obv = Obj.get_linkedItem();
			str.append(Integer.toHexString(Obv.getTemplate().getID())).append('~').append(Obv.getType()).append('~').append(Obv.getSelectedLevel());
		} else 
		{
			str.append(Integer.toHexString(Obj.getTemplate().getID()));
		}
		return str.toString();
	}

	public String getAsPacket()
	{
		refreshStats();
		if(cacheAS == "")
		{
			StringBuilder ASData = new StringBuilder(500);
			ASData.append("As").append(xpString(",")).append('|');
			ASData.append(_kamas).append('|').append(_capital).append('|').append(_spellPts).append('|');
			ASData.append(_align).append('~').append(_align).append(',').append(_aLvl).append(',').append(getGrade()).append(',').append(_honor).append(',').append(_deshonor).append(',').append((_showWings?"1":"0")).append('|');
			
			int pdv = getPDV();
			int pdvMax = getPDVMAX();
			
			if(_fight != null)
			{
				Fighter f = _fight.getFighterByPerso(this);
				if(f!= null)
				{
					pdv = f.getLife();
					pdvMax = f.getMaxLife();
				}
			}
			
			Stats stuffStats = getStuffStats();
			Stats giftStats = getDonsStats();
			Stats buffStats = getBuffsStats();
			ASData.append(pdv).append(',').append(pdvMax).append('|');
			ASData.append(_energy).append(',').append(Config.SERVER_ID == 22 ? 1 : 10000).append('|');
			
			ASData.append(getInitiative()).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_PROS)+stuffStats.getEffect(Constants.STATS_ADD_PROS)+((int)Math.ceil(_baseStats.getEffect(Constants.STATS_ADD_CHAN)/10))+buffStats.getEffect(Constants.STATS_ADD_PROS)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_PA)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_PA)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_PA)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_PA)).append(',').append(getTotalStats().getEffect(Constants.STATS_ADD_PA)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_PM)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_PM)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_PM)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_PM)).append(',').append(getTotalStats().getEffect(Constants.STATS_ADD_PM)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_FORC)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_FORC)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_FORC)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_FORC)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_VITA)+_baseStats.getEffect(Constants.STATS_ADD_VIE)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_VITA)+stuffStats.getEffect(Constants.STATS_ADD_VIE)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_VITA)+giftStats.getEffect(Constants.STATS_ADD_VIE)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_VITA)+buffStats.getEffect(Constants.STATS_ADD_VIE)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_SAGE)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_SAGE)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_SAGE)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_SAGE)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_CHAN)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_CHAN)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_CHAN)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_CHAN)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_AGIL)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_AGIL)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_AGIL)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_AGIL)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_INTE)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_INTE)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_INTE)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_INTE)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_PO)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_PO)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_PO)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_PO)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_CREATURE)).append(',').append(stuffStats.getEffect(Constants.STATS_CREATURE)).append(',').append(giftStats.getEffect(Constants.STATS_CREATURE)).append(',').append(buffStats.getEffect(Constants.STATS_CREATURE)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_DOMA)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_DOMA)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_DOMA)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_DOMA)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_PDOM)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_PDOM)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_PDOM)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_PDOM)).append('|');
			ASData.append("0,0,0,0|");//Maitrise ?
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_PERDOM)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_PERDOM)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_PERDOM)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_PERDOM)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_SOIN)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_SOIN)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_SOIN)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_SOIN)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_TRAPDOM)).append(',').append(stuffStats.getEffect(Constants.STATS_TRAPDOM)).append(',').append(giftStats.getEffect(Constants.STATS_TRAPDOM)).append(',').append(buffStats.getEffect(Constants.STATS_TRAPDOM)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_TRAPPER)).append(',').append(stuffStats.getEffect(Constants.STATS_TRAPPER)).append(',').append(giftStats.getEffect(Constants.STATS_TRAPPER)).append(',').append(buffStats.getEffect(Constants.STATS_TRAPPER)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_RETDOM)).append(',').append(stuffStats.getEffect(Constants.STATS_RETDOM)).append(',').append(giftStats.getEffect(Constants.STATS_RETDOM)).append(',').append(buffStats.getEffect(Constants.STATS_RETDOM)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_CC)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_CC)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_CC)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_CC)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_EC)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_EC)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_EC)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_EC)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_AFLEE)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_AFLEE)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_AFLEE)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_AFLEE)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_AFLEE)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_MFLEE)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_MFLEE)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_MFLEE)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_MFLEE)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_MFLEE)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_R_NEU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_R_NEU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_R_NEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_NEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_NEU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_RP_NEU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_RP_NEU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_RP_NEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_NEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_NEU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_R_PVP_NEU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_R_PVP_NEU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_R_PVP_NEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_PVP_NEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_PVP_NEU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_RP_PVP_NEU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_RP_PVP_NEU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_RP_PVP_NEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_PVP_NEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_PVP_NEU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_R_TER)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_R_TER)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_R_TER)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_TER)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_TER)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_RP_TER)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_RP_TER)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_RP_TER)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_TER)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_TER)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_R_PVP_TER)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_R_PVP_TER)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_R_PVP_TER)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_PVP_TER)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_PVP_TER)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_RP_PVP_TER)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_RP_PVP_TER)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_RP_PVP_TER)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_PVP_TER)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_PVP_TER)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_R_EAU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_R_EAU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_R_EAU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_EAU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_EAU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_RP_EAU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_RP_EAU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_RP_EAU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_EAU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_EAU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_R_PVP_EAU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_R_PVP_EAU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_R_PVP_EAU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_PVP_EAU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_PVP_EAU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_RP_PVP_EAU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_RP_PVP_EAU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_RP_PVP_EAU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_PVP_EAU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_PVP_EAU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_R_AIR)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_R_AIR)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_R_AIR)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_AIR)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_AIR)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_RP_AIR)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_RP_AIR)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_RP_AIR)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_AIR)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_AIR)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_R_PVP_AIR)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_R_PVP_AIR)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_R_PVP_AIR)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_PVP_AIR)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_PVP_AIR)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_RP_PVP_AIR)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_RP_PVP_AIR)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_RP_PVP_AIR)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_PVP_AIR)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_PVP_AIR)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_R_FEU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_R_FEU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_R_FEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_FEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_FEU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_RP_FEU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_RP_FEU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_RP_FEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_FEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_FEU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_R_PVP_FEU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_R_PVP_FEU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_R_PVP_FEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_PVP_FEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_R_PVP_FEU)).append('|');
			ASData.append(_baseStats.getEffect(Constants.STATS_ADD_RP_PVP_FEU)).append(',').append(stuffStats.getEffect(Constants.STATS_ADD_RP_PVP_FEU)).append(',').append(giftStats.getEffect(Constants.STATS_ADD_RP_PVP_FEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_PVP_FEU)).append(',').append(buffStats.getEffect(Constants.STATS_ADD_RP_PVP_FEU)).append('|');
			
			cacheAS = ASData.toString();
		}
		return cacheAS;
	}
	
	public void clearCacheAS()
	{
		cacheAS = "";
	}
	
	public int getGrade()
	{
		if(_align == Constants.ALIGNEMENT_NEUTRE)return 0;
		if(_honor >= 17500)return 10;
		for(int n = 1; n <=10; n++)
		{
			if(_honor < World.getExpLevel(n).pvp)return n-1;
		}
		return 0;
	}
	
	public String xpString(String c)
	{
		return _curExp+c+World.getPersoXpMin(_lvl)+c+World.getPersoXpMax(_lvl);
	}
	
	public int emoteActive() {
		return emoteActive;
	}

	public void setEmoteActive(int emoteActive) 
	{
		this.emoteActive = emoteActive;
		if(emoteTimer == null)
			initEmoteTimer();
		if(emoteTimer.isRunning()) {
			emoteTime = 360000;
			emoteTimer.restart();
		} else {
			emoteTimer.start();
		}
	}

	private void initEmoteTimer() {
		emoteTimer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(emoteTime > 0) {
					emoteTime = emoteTime - 1000;
				}
				if(emoteTime <= 0) {
					emoteTime = 360000;
					emoteActive = 0;
					emoteTimer.stop();
				}
			}
		});
	}

	public Stats getStuffStats()
	{
		Stats stats = new Stats(false, null);
		ArrayList<Integer> itemSetApplied = new ArrayList<Integer>();
		
		for(Entry<Integer,Item> entry : _items.entrySet())
		{
			if(entry.getValue().getPosition() != Constants.ITEM_POS_NO_EQUIPED)
			{
				stats = Stats.cumulStat(stats,entry.getValue().getStats());
				int panID = entry.getValue().getTemplate().getPanopID();
				//Si panoplie, et si l'effet de pano n'a pas encore été ajouté
				if(panID>0 && !itemSetApplied.contains(panID))
				{
					itemSetApplied.add(panID);
					ItemSet IS = World.getItemSet(panID);
					//Si la pano existe
					if(IS != null)
					{
						//on ajoute le bonus de pano en fonction du nombre d'item
						stats = Stats.cumulStat(stats,IS.getBonusStatByItemNumb(this.getNumbEquipedItemOfSet(panID)));
					}
				}
			}
		}
		if(_onMount && _mount != null)
		{
			stats = Stats.cumulStat(stats, _mount.getStats());
		}
		return stats;
	}

	private Stats getBuffsStats()
	{
		Stats stats = new Stats(false,null);
		for(Map.Entry<Integer, SpellEffect> entry : _buffs.entrySet())
		{
			stats.addOneStat(entry.getValue().getEffectID(), entry.getValue().getValue());
		}
		if(_fight != null)
		{
			Fighter fighter = _fight.getFighterByPerso(this);
			if(fighter != null)
			{
				ArrayList<SpellEffect> fightBuffs = new ArrayList<SpellEffect>();
				fightBuffs.addAll(fighter.getFightBuff().values());
				for(SpellEffect buff : fightBuffs) 
				{
					stats.addOneStat(buff.getEffectID(), buff.getValue());
				}
			}
		}
		return stats;
	}

	public byte getOrientation() {
		return (byte) _orientation;
	}

	public void setOrientation(int _orientation) {
		this._orientation = _orientation;
	}

	public int getInitiative()
	{
		int fact = 4;
		int pvmax = _PDVMAX - Constants.getBasePdv(_breedId);
		int pv = _PDV - Constants.getBasePdv(_breedId);
		if(_breedId == Constants.CLASS_SACRIEUR)fact = 8;
		double coef = pvmax/fact;
		
		coef += getStuffStats().getEffect(Constants.STATS_ADD_INIT);
		coef += getTotalStats().getEffect(Constants.STATS_ADD_AGIL);
		coef += getTotalStats().getEffect(Constants.STATS_ADD_CHAN);
		coef += getTotalStats().getEffect(Constants.STATS_ADD_INTE);
		coef += getTotalStats().getEffect(Constants.STATS_ADD_FORC);
		
		int init = 1;
		if(pvmax != 0)
		 init = (int)(coef*((double)pv/(double)pvmax));
		if(init <0)
			init = 0;
		return init;
	}

	public Stats getTotalStats()
	{
		Stats total = new Stats(false,null);
		total = Stats.cumulStat(total,_baseStats);
		total = Stats.cumulStat(total,getStuffStats());
		total = Stats.cumulStat(total,getDonsStats());
		if(_fight == null)
			total = Stats.cumulStat(total,getBuffsStats());
		
		return total;
	}

	private Stats getDonsStats()
	{
		/* TODO*/
		Stats stats = new Stats(false,null);
		return stats;
	}

	public int getPodUsed()
	{
		int pod = 0;
		for(Entry<Integer,Item> entry : _items.entrySet())
		{
			pod += entry.getValue().getTemplate().getPod() * entry.getValue().getQuantity();
		}
		return pod;
	}

	public int getMaxPod() {
		int pods = getTotalStats().getEffect(Constants.STATS_ADD_PODS);
		pods += getTotalStats().getEffect(Constants.STATS_ADD_FORC)*5;
		for(JobStat SM : _jobs.values())
		{
			pods += SM.getLvl()*5;
			if(SM.getLvl() == 100) pods += 1000;
		}
		return pods;
	}

	public int getPDV() {
		return _PDV;
	}

	public void setPDV(int _pdv) {
		_PDV = _pdv;
		clearCacheAS();
		if(_party != null)
		{
			SocketManager.GAME_SEND_PM_MOD_PACKET_TO_PARTY(_party,this);
		}
	}

	public int getPDVMAX() {
		return _PDVMAX;
	}

	public void setPDVMAX(int _pdvmax) 
	{
		_PDVMAX = _pdvmax;
		clearCacheAS();
		if(_party != null)
		{
			SocketManager.GAME_SEND_PM_MOD_PACKET_TO_PARTY(_party,this);
		}
	}

	public void setSitted(boolean b)
	{
		_sitted = b;
		int diff = _PDV - _exPdv;
		int time = (b?1000:2000);
		
		_exPdv = _PDV;
		if(_isOnline)
		{	//On envoie le message "Vous avez recuperer X pdv"
			SocketManager.GAME_SEND_ILF_PACKET(this, diff);
			//On envoie la modif du Timer de regenPdv coté client
			SocketManager.GAME_SEND_ILS_PACKET(this, time);
		}
		//on modifie le delay coté Serveur du timer de regenPDV
		_sitTimer.setDelay(time);
		//Si on se leve, on desactive l'émote
		if((emoteActive == 1 || emoteActive == 19) && b == false)emoteActive = 0;
	}

	public byte getAlign()
	{
		return _align;
	}
	
	public int getPdvPer() {
		int pdvper = 100;
		pdvper = (100*_PDV)/_PDVMAX;
		return pdvper;
	}

	public void useEmote(String str) 
	{
		try
		{
			int id = Integer.parseInt(str);
			if(_fight == null)
				SocketManager.GAME_SEND_EMOTICONE_TO_MAP(_curMap,_GUID,id);
			else
				SocketManager.GAME_SEND_EMOTICONE_TO_FIGHT(_fight,7,_GUID,id);
			clearCacheGM();
		}catch(NumberFormatException e){return;};
	}

	public void refreshMapAfterFight()
	{
		_curMap.addPlayer(this);
		if(_account != null && _account.getGameThread() != null && _account.getGameThread().getOut() != null)
		{
			clearCacheAS();
			SocketManager.GAME_SEND_STATS_PACKET(this);
			SocketManager.GAME_SEND_ILS_PACKET(this, 1000);
		}
		_fight = null;
		_away = false;
	}
	
	public void refreshSpecialItems() {
	    int pos = 20;
	    while(pos <= 23)
	    {
		    Item item = getObjetByPos(pos);
		    if(item == null)
		    {
		    	pos++;
		    	continue;
		    }
		    int guid = item.getGuid();
		    int tours = 0;
		    if(item.getStats().getMap().containsKey(811))
		    {
		    	for(Entry<Integer, Integer> stat : item.getStats().getMap().entrySet())
			    {
			    	if(stat.getKey() == 811) 
			    	{
			    		tours = stat.setValue(stat.getValue() - 1);
			    		if(tours > 0)
			 		    {
			 		    	SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
			 				SocketManager.GAME_SEND_OAKO_PACKET(this, item);
			 		    }
			 		    else
			 		    {
			 				removeItem(guid);
			 				World.removeItem(guid);
			 				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
			 		    }
			    	}
			    }
		    }
		    if(item.getTxtStat().get(811) != null)
		    {
		    	String stats = item.parseStatsString();
		    	for(String effet : stats.split(","))
		    	{
		    		if(Integer.parseInt(effet.split("#")[0], 16) == 811)
		    		{
		    			tours = Integer.parseInt(effet.split("#")[3], 16);
		    			if(tours - 1 > 0)
			    		{
			    			stats.replace("32b#0#0#"+tours, "32b#0#0#"+(tours-1));
			    			item.clearStats();
			    			item.parseStringToStats(stats);
			    			SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
			    			SocketManager.GAME_SEND_OAKO_PACKET(this, item);
			    		} else
			    		{
			    			if(World.isItemFollower(item.getTemplate().getID()))
			    			{
			    				monstersFollower.remove(item.getTemplate().getID());
			    			}
			    			removeItem(guid);
			    			World.removeItem(guid);
			    			SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
			    		}
		    		}
		    	}
		    }
		    pos++;
	    }
    }
	
	public double requiredPoints(int stat)
	{
		Couple<Integer, Integer> cost;
		Breed breed = World.getBreed(_breedId);
		switch(stat)
		{
			case 10://Force
				cost = breed.getStrenght().getPoints(_baseStats.getEffect(Constants.STATS_ADD_FORC));
				return (double)cost.second/(double)cost.first;
			case 11://Vitalité
				cost = breed.getVitality().getPoints(_baseStats.getEffect(Constants.STATS_ADD_VITA));
				return (double)cost.second/(double)cost.first;
			case 12://Sagesse
				cost = breed.getWisdom().getPoints(_baseStats.getEffect(Constants.STATS_ADD_SAGE));
				return (double)cost.second/(double)cost.first;
			case 13://Chance
				cost = breed.getChance().getPoints(_baseStats.getEffect(Constants.STATS_ADD_CHAN));
				return (double)cost.second/(double)cost.first;
			case 14://Agilité
				cost = breed.getAgility().getPoints(_baseStats.getEffect(Constants.STATS_ADD_AGIL));
				return (double)cost.second/(double)cost.first;
			case 15://Intelligence
				cost = breed.getIntelligence().getPoints(_baseStats.getEffect(Constants.STATS_ADD_INTE));
				return (double)cost.second/(double)cost.first;
		}
		return 5;
	}

	public void boostStat(int stat)
	{
		Log.addToLog("Perso "+_name+": tentative de boost stat "+stat);
		int value = 0;
		switch(stat)
		{
			case 10://Force
				value = _baseStats.getEffect(Constants.STATS_ADD_FORC);
			break;
			case 13://Chance
				value = _baseStats.getEffect(Constants.STATS_ADD_CHAN);
			break;
			case 14://Agilité
				value = _baseStats.getEffect(Constants.STATS_ADD_AGIL);
			break;
			case 15://Intelligence
				value = _baseStats.getEffect(Constants.STATS_ADD_INTE);
			break;
		}
		int cout = Constants.getReqPtsToBoostStatsByClass(_breedId, stat, value);
		//double cout = requiredPoints(stat);
		if(cout <= _capital)
		{
			switch(stat)
			{
				case 11://Vita
					if(_breedId != Constants.CLASS_SACRIEUR)
						_baseStats.addOneStat(Constants.STATS_ADD_VITA, 1);
					else
						_baseStats.addOneStat(Constants.STATS_ADD_VITA, 2);
					//_PDV++;
				break;
				case 12://Sage
					_baseStats.addOneStat(Constants.STATS_ADD_SAGE, 1);
				break;
				case 10://Force
					_baseStats.addOneStat(Constants.STATS_ADD_FORC, 1);
				break;
				case 13://Chance
					_baseStats.addOneStat(Constants.STATS_ADD_CHAN, 1);
				break;
				case 14://Agilité
					_baseStats.addOneStat(Constants.STATS_ADD_AGIL, 1);
				break;
				case 15://Intelligence
					_baseStats.addOneStat(Constants.STATS_ADD_INTE, 1);
				break;
				default:
					return;
			}
			_capital -= cout;
		}
		clearCacheAS();
		SocketManager.GAME_SEND_STATS_PACKET(this);
		//SQLManager.SAVE_PERSONNAGE(this,false);
	}

	public boolean isMuted()
	{
		return _account.isMuted();
	}
	public void setCurMap(DofusMap carte)
	{
		_curMap = carte;
	}

	public String parseObjetsToDB()
	{
		StringBuilder str = new StringBuilder(10*_items.size());
		for (Entry<Integer, Item> entry : _items.entrySet()) 
		{
			Item obj = entry.getValue();
			if (obj.is_linked())
				str.append(obj.get_linkedItem_id()).append('|');
			str.append(obj.getGuid()).append('|');
		}
		return str.toString();
	}
	
	public synchronized void addItem(Item newObj)	//Return false quand il Stack
	{
		_items.put(newObj.getGuid(), newObj);
		World.addtoPlayerItems_List(newObj.getGuid(), _GUID);
	}
	
	public synchronized boolean addItem(Item newObj,boolean stackIfSimilar)	//Return false quand il Stack
	{
		for(Entry<Integer,Item> entry : _items.entrySet())
		{
			Item obj = entry.getValue();
			if(obj.getTemplate().getID() == newObj.getTemplate().getID()
				&& obj.getStats().isSameStats(newObj.getStats())
				&& stackIfSimilar
				&& newObj.getTemplate().getType() != 85
				&& obj.getPosition() == Constants.ITEM_POS_NO_EQUIPED)//Si meme Template et Memes Stats et Objet non équipé
			{
				if (obj.isSpeaking() && newObj.isSpeaking()) {
					Speaking obj_s = Speaking.toSpeaking(obj);
					Speaking exObj_s = Speaking.toSpeaking(newObj);
					if (obj_s.isSimmilar(exObj_s)) {
						obj.setQuantity(obj.getQuantity() + newObj.getQuantity());
						SQLManager.SAVE_ITEM(obj);
						if (_isOnline)	SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
						return false;
					}
				} else if (!obj.isSpeaking() && !newObj.isSpeaking()) {
					obj.setQuantity(obj.getQuantity() + newObj.getQuantity());
					SQLManager.SAVE_ITEM(obj);
					if (_isOnline)	SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);//On ajoute QUA item a la quantité de l'objet existant
					return false;
				}
			}
		}
		_items.put(newObj.getGuid(), newObj);
		World.addtoPlayerItems_List(newObj.getGuid(), _GUID);
		SocketManager.GAME_SEND_OAKO_PACKET(this,newObj);
		return true;
	}
	public Map<Integer,Item> getItems()
	{
		return _items;
	}
	
	public String parseItemToASK()
	{
		StringBuilder str = new StringBuilder();
		for (Item obj : _items.values()) 
		{
			if (obj.isSpeaking() && Speaking.toSpeaking(obj).hasLinkedItem())
				str.append("");
			else
				str.append(obj.parseItem());
		}
		return str.toString();
	}

	public String getBankItemsIDSplitByChar(String splitter)
	{
		StringBuilder str = new StringBuilder();
		for(int entry : _account.getBankItems().keySet())str.append(entry).append(splitter);
		return str.toString();
	}
	
	public String getItemsIDSplitByChar(String splitter)
	{
		StringBuilder str = new StringBuilder();
		for(int entry : _items.keySet())
		{
			if(str.length() != 0) str.append(splitter);
			str.append(entry);
		}
		return str.toString();
	}

	public boolean hasItemGuid(int guid)
	{
		return _items.get(guid) != null?_items.get(guid).getQuantity()>0:false;
	}
	
	public void sellItem(int guid,int qua)
	{
		if(qua <= 0)
			return;
		if(_items.get(guid).getQuantity() < qua)//Si il a moins d'item que ce qu'on veut Del
			qua = _items.get(guid).getQuantity();
		
		int prix = qua * (_items.get(guid).getTemplate().getPrix()/10);//Calcul du prix de vente (prix d'achat/10)
		int newQua =  _items.get(guid).getQuantity() - qua;
		
		int templateID = _items.get(guid).getTemplate().getID();	//Utilisé pour le log
		
		if(newQua <= 0)//Ne devrait pas etre <0, S'il n'y a plus d'item apres la vente 
		{
			_items.remove(guid);
			//objetLog(templateID, -qua, "Vente à un PNJ");
			
			SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this,guid);
		}else//S'il reste des items apres la vente
		{
			_items.get(guid).setQuantity(newQua);
			//objetLog(templateID, -qua, "Vente à un PNJ");
			
			SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, _items.get(guid));
		}
		_kamas = _kamas + prix;
		kamasLog(prix+"", "Vente de l'item '" + World.getItemTemplate(templateID).getName() + "' à un PNJ");	//Enregistrement de la transaction
		clearCacheAS();
		SocketManager.GAME_SEND_STATS_PACKET(this);
		SocketManager.GAME_SEND_Ow_PACKET(this);
		SocketManager.GAME_SEND_ESK_PACKEt(this);
	}

	public void removeItem(int guid)
	{
		_items.remove(guid);
		World.getPlayerItems().remove(guid);
	}
	public void removeItem(int guid, int nombre,boolean send,boolean deleteFromWorld)
	{
		Item obj = _items.get(guid);
		
		if(nombre > obj.getQuantity())
			nombre = obj.getQuantity();
		
		if(obj.getQuantity() >= nombre)
		{
			int newQua = obj.getQuantity() - nombre;
			if(newQua >0)
			{
				obj.setQuantity(newQua);
				if(send && _isOnline)
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
			}else
			{
				//on supprime de l'inventaire et du Monde
				_items.remove(obj.getGuid());
				if(deleteFromWorld)
					World.removeItem(obj.getGuid());
				//on envoie le packet si connecté
				if(send && _isOnline)
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
			}
		}
	}
	public synchronized void deleteItem(int guid)
	{
		_items.remove(guid);
		World.removeItem(guid);
	}
	
	public Item getObjetByPos(int pos)
	{
		if(pos == Constants.ITEM_POS_NO_EQUIPED)return null;
		
		for(Entry<Integer,Item> entry : _items.entrySet())
		{
			Item obj = entry.getValue();
			if(obj.getPosition() == pos)
				return obj;
		}
		return null;
	}

	public void refreshStats()
	{
		double actPdvPer = (100*(double)_PDV)/(double)_PDVMAX;
		_PDVMAX = (_lvl-1)*5+Constants.getBasePdv(_breedId)+getTotalStats().getEffect(Constants.STATS_ADD_VITA)+getTotalStats().getEffect(Constants.STATS_ADD_VIE);
		_PDV = (int) Math.round(_PDVMAX*actPdvPer/100);
	}
	
	public void manageSpeakingsOnStartup() {
		for (Item obj : this._items.values()) {
			if (!obj.isSpeaking()) {
				continue;
			}

			Speaking obv = Speaking.toSpeaking(obj);
			try {
				String Date = obv.getLastEat();
				if (Date.contains("-")) {
					if (!Formulas.compareTime(Date, Constants.ITEM_TIME_FEED_MAX)) //Au bout de 3 jours il est Maigrichon
					{
						obv.setState(0);
					}
				} else {
					Date = Formulas.getDate(("325#" + obv.getTxtStat().get(Constants.EFFECT_RECEIVED_DATE)).split("#"));
					if (!Formulas.compareTime(Date, Constants.ITEM_TIME_FEED_MAX)) //Au bout de 3 jours il est Maigrichon
					{
						obv.setState(0);
						int guid = obv.getGuid();
						if (obv.is_linked()) {
							guid = obv.getLinkedID();
						}
						Player Own = World.getPlayer(World.getPlayerItems().get(guid));
						if (Own != null && Own.isOnline()) {
							SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(Own, guid);
							SocketManager.GAME_SEND_OAKO_PACKET(Own, World.getObjet(guid));
						}
					}
				}
			} catch (Exception e) {
				Log.addToLog("Erreur Speaking: " + e.getMessage());
			}

		}
	}
	
	public void managePetsOnStartup()
	{
		for (Item obj : _items.values()) 
		{
			if (!obj.isPet())
				continue;
			try {
				Pet pet = (Pet)obj;
				String Date = pet.getLastTimeAte();
				if (Date.contains("-")) 
				{
					if (!Formulas.compareTime(Date, pet.getPetTemplate().getTimeBetweenMeals().second))
					{
						if(!pet.getPetTemplate().isSoulEater())
						{
							pet.setState((byte)0);
							pet.loseLife();
							if(pet.getLife() <= 0)
							{
								SocketManager.GAME_SEND_Im_PACKET(this, "154");
								continue;
							}
						}
					}
				}
				SocketManager.GAME_SEND_Im_PACKET(this, "025");
			} catch (Exception e) {
				Log.addToLog("Erreur Pet: " + e.getMessage());
			}
		}
	}

	public void levelUp(boolean send,boolean addXp)
	{
		/*-------------------LIGNE PAR MARTHIEUBEAN-------------------------------
		if(Config.CONFIG_POINT_PER_LEVEL > 0 && _lvl % Config.CONFIG_LEVEL_FOR_POINT == 0)	
		{
			SQLManager.ADD_POINT(Config.CONFIG_POINT_PER_LEVEL,_account);
			SocketManager.GAME_SEND_MESSAGE(this,"Vous gagner 1 point pour la boutique du site internet","DF0101");
		}
		/*------------------------------FIN-----------------------------------------*/
		
		if(_lvl == Config.MAX_LEVEL)return;
		_lvl++;
		_capital+=5;
		_spellPts++;
		_PDVMAX += 5;
		_PDV = _PDVMAX;
		if(_lvl == 100)
			_baseStats.addOneStat(Constants.STATS_ADD_PA, 1);
		//Constants.onLevelUpSpells(this,_lvl);
		learnBreedSpell();
		clearCacheAS();
		if(addXp)_curExp = World.getExpLevel(_lvl).perso;
		if(send && _isOnline)
		{
			SocketManager.GAME_SEND_NEW_LVL_PACKET(_account.getGameThread().getOut(),_lvl);
			SocketManager.GAME_SEND_STATS_PACKET(this);
		}
		
		if(this._guildMember != null)
			this._guildMember.setLevel(_lvl);
	}
	
	/*-------------------------------Surcharge pour le site internet MARTHIEUBEAN-----------------*/
	public void levelUp(boolean send,boolean addXp, boolean fromWeb)
	{
		
		_lvl++;
		_capital+=5;
		_spellPts++;
		_PDVMAX += 5;
		_PDV = _PDVMAX;
		
		/*if(!(fromWeb))
		{
			SQLManager.ADD_POINT(Config.CONFIG_POINT_PER_LEVEL,_account);		//LIGNE PAR MARTHIEUBEAN
			SocketManager.GAME_SEND_MESSAGE(this,"Vous gagnez 1 point pour la boutique du site internet","DF0101");		//LIGNE PAR MARTHIEUBEAN
		}*/
		
		if(_lvl == 100)
			_baseStats.addOneStat(Constants.STATS_ADD_PA, 1);
		//Constants.onLevelUpSpells(this,_lvl);
		learnBreedSpell();
		clearCacheAS();
		if(addXp)_curExp = World.getExpLevel(_lvl).perso;
		if(send && _isOnline)
		{
			SocketManager.GAME_SEND_NEW_LVL_PACKET(_account.getGameThread().getOut(),_lvl);
			SocketManager.GAME_SEND_STATS_PACKET(this);
		}
		if(this._guildMember != null)
			this._guildMember.setLevel(_lvl);
	}
	
	public synchronized void addXp(long winxp,boolean fromWeb)
	{
		_curExp += winxp;
		int exLevel = _lvl;
		while(_curExp >= World.getPersoXpMax(_lvl) && _lvl<Config.MAX_LEVEL)
			levelUp(false,false,fromWeb);
		clearCacheAS();
		if(_isOnline)
		{
			if(exLevel < _lvl)SocketManager.GAME_SEND_NEW_LVL_PACKET(_account.getGameThread().getOut(),_lvl);
			SocketManager.GAME_SEND_STATS_PACKET(this);
		}
	}
	/*-----------------------------------FIN------------------------------------------------------*/
	
	public synchronized void addXp(long winxp)
	{
		_curExp += winxp;
		int exLevel = _lvl;
		while(_curExp >= World.getPersoXpMax(_lvl) && _lvl<Config.MAX_LEVEL)
			levelUp(false,false);
		clearCacheAS();
		if(_isOnline)
		{
			if(exLevel < _lvl)SocketManager.GAME_SEND_NEW_LVL_PACKET(_account.getGameThread().getOut(),_lvl);
			SocketManager.GAME_SEND_STATS_PACKET(this);
		}
	}
	
	public synchronized void addKamas (long k)
	{
		_kamas += k;
		clearCacheAS();
	}
	
	public synchronized void remKamas (long k)
	{
		_kamas -= k;
		clearCacheAS();
	}

	public Item getSimilarItem(Item exObj)
	{
		for(Entry<Integer,Item> entry : _items.entrySet())
		{
			Item obj = entry.getValue();
			if (exObj.is_linked()) {
				return null;
			}
			if(obj.getTemplate().getID() == exObj.getTemplate().getID()
				&& obj.getStats().isSameStats(exObj.getStats())
				&& obj.getGuid() != exObj.getGuid()
				&& !obj.is_linked()
				&& obj.getPosition() == Constants.ITEM_POS_NO_EQUIPED) {
				if (obj.isSpeaking() && exObj.isSpeaking()) {
					Speaking obj_s = Speaking.toSpeaking(obj);
					Speaking exObj_s = Speaking.toSpeaking(exObj);
					if (obj_s.isSimmilar(exObj_s)) {
						return obj;
					}
				} else if (!obj.isSpeaking() && !exObj.isSpeaking()) {
					return obj;
				}
			}
		}
		return null;
	}

	public void setCurExchange(Exchange echg)
	{
		_curExchange = echg;
	}
	
	public Exchange getCurExchange()
	{
		return _curExchange;
	}

	public int learnJob(Job m)
	{
		for(Entry<Integer,JobStat> entry : _jobs.entrySet())
		{
			if(entry.getValue().getTemplate().getId() == m.getId())//Si le joueur a déjà le métier
				return -1;
		}
		int Msize = _jobs.size();
		if(Msize == 6)//Si le joueur a déjà 6 métiers
			return -1;
		int pos = 0;
		if(Constants.isMageJob(m.getId()))
		{
			if(_jobs.get(3) == null) pos = 3;
			if(_jobs.get(4) == null) pos = 4;
			if(_jobs.get(5) == null) pos = 5;
		}else
		{
			if(_jobs.get(0) == null) pos = 0;
			if(_jobs.get(1) == null) pos = 1;
			if(_jobs.get(2) == null) pos = 2;
		}
		
		JobStat sm = new JobStat(pos,m,1,0);
		_jobs.put(pos, sm);//On apprend le métier lvl 1 avec 0 xp
		if(_isOnline)
		{
			//on créer la listes des statsMetier a envoyer (Seulement celle ci)
			ArrayList<JobStat> list = new ArrayList<JobStat>();
			list.add(sm);
			
			SocketManager.GAME_SEND_Im_PACKET(this, "02;"+m.getId());
			//packet JS
			SocketManager.GAME_SEND_JS_PACKET(this, list);
			//packet JX
			SocketManager.GAME_SEND_JX_PACKET(this, list);
			//Packet JO (Job Option)
			SocketManager.GAME_SEND_JO_PACKET(this,list);
			
			Item obj = getObjetByPos(Constants.ITEM_POS_ARME);
			if(obj != null)
				if(sm.getTemplate().isValidTool(obj.getTemplate().getID()))
					SocketManager.GAME_SEND_OT_PACKET(_account.getGameThread().getOut(),m.getId());
		}
		return pos;
	}

	public boolean hasEquiped(int id)
	{
		for(Entry<Integer,Item> entry : _items.entrySet())
			if(entry.getValue().getTemplate().getID() == id && entry.getValue().getPosition() != Constants.ITEM_POS_NO_EQUIPED)
				return true;
		return false;
	}

	public void setInvitation(int target)
	{
		_inviting = target;
	}
	
	public int getInvitation()
	{
		return _inviting;
	}
	
	public String parseToPM()
	{
		StringBuilder str = new StringBuilder(20);
		str.append(_GUID).append(';');
		str.append(_name).append(';');
		str.append(_gfxId).append(';');
		str.append(_color1).append(';');
		str.append(_color2).append(';');
		str.append(_color3).append(';');
		str.append(getGMStuffString()).append(';');
		str.append(_PDV).append(',').append(_PDVMAX).append(';');
		str.append(_lvl).append(';');
		str.append(getInitiative()).append(';');
		str.append(getTotalStats().getEffect(Constants.STATS_ADD_PROS)).append(';');
		str.append('1');//Side = ?
		return str.toString();
	}
	
	public int getNumbEquipedItemOfSet(int panID)
	{
		int nb = 0;
		for(Entry<Integer, Item> i : _items.entrySet())
		{
			//On ignore les objets non équipés
			if(i.getValue().getPosition() == Constants.ITEM_POS_NO_EQUIPED)continue;
			//On prend que les items de la pano demandée, puis on augmente le nombre si besoin
			if(i.getValue().getTemplate().getPanopID() == panID)nb++;
		}
		return nb;
	}

	public void startActionOnCell(GameAction GA)
	{
		int cellID = -1;
		int action = -1;
		try
		{
			cellID = Integer.parseInt(GA._args.split(";")[0]);
			action = Integer.parseInt(GA._args.split(";")[1]);
		}catch(Exception e){};
		if(cellID == -1 || action == -1)return;
		//Si case invalide
		if(!_curMap.getCell(cellID).canDoAction(action))return;
		_curMap.getCell(cellID).startAction(this,GA);
	}

	public void finishActionOnCell(GameAction GA)
	{
		int cellID = -1;
		try
		{
			cellID = Integer.parseInt(GA._args.split(";")[0]);
		}catch(Exception e){};
		if(cellID == -1)return;
		_curMap.getCell(cellID).finishAction(this,GA);
	}
	
	public void teleport(int newMapID, int newCellID)
	{
		PrintWriter PW = _account.getGameThread().getOut();
		if(World.getMap(newMapID) == null)return;
		if(World.getMap(newMapID).getCell(newCellID) == null)return;
		if(PW != null)
		{
			clearCacheGM();
			SocketManager.GAME_SEND_GA2_PACKET(PW,_GUID);
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_curMap, _GUID);
		}
		_curCell.removePlayer(_GUID);
		_curMap = World.getMap(newMapID);
		_curCell = _curMap.getCell(newCellID);
		
		if(PW != null)
		{
		SocketManager.GAME_SEND_MAPDATA(
				PW,
				newMapID,
				_curMap.getDate(),
				_curMap.getKey());
		_curMap.addPlayer(this);
		}
	}
	public int getBankCost()
	{
		return _account.getBankItems().size();
	}
	
	public String getStringVar(String str)
	{
		//TODO completer
		if(str.equals("name"))return _name;
		if(str.equals("bankCost"))
		{
			return getBankCost()+"";
		}
		return "";
	}

	public void setBankKamas(long i)
	{
		_account.setBankKamas(i);
		SQLManager.UPDATE_ACCOUNT_DATA(_account);
	}
	
	public long getBankKamas()
	{
		return _account.getBankKamas();
	}

	public void setInBank(boolean b)
	{
		_isInBank = b;
	}
	public boolean isInBank()
	{
		return _isInBank;
	}

	public String parseBankPacket()
	{
		StringBuilder packet = new StringBuilder();
		for(Entry<Integer, Item> entry : _account.getBankItems().entrySet())
			packet.append('O').append(entry.getValue().parseItem()).append(';');
		if(getBankKamas() != 0)
			packet.append('G').append(getBankKamas());
		return packet.toString();
	}

	public void addCapital(int pts)
	{
		_capital += pts;
		clearCacheAS();
	}

	public void addSpellPoint(int pts)
	{
		_spellPts += pts;
		clearCacheAS();
	}

	public void addInBank(int guid, int qua)
	{
		Item PersoObj = World.getObjet(guid);
		//Si le joueur n'a pas l'item dans son sac ...
		if(_items.get(guid) == null)
		{
			Log.addToLog("Le joueur "+_name+" a tenter d'ajouter un objet en banque qu'il n'avait pas.");
			return;
		}
		//Si c'est un item équipé ...
		if(PersoObj.getPosition() != Constants.ITEM_POS_NO_EQUIPED)return;
		
		Item BankObj = getSimilarBankItem(PersoObj);
		int newQua = PersoObj.getQuantity() - qua;
		if(BankObj == null)//S'il n'y pas d'item du meme Template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				removeItem(PersoObj.getGuid());
				//On met l'objet du sac dans la banque, avec la meme quantité
				_account.getBankItems().put(PersoObj.getGuid(), PersoObj);
				String str = "O+"+PersoObj.getGuid()+"|"+PersoObj.getQuantity()+"|"+PersoObj.getTemplate().getID()+"|"+PersoObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
				
			}
			else//S'il reste des objets au joueur
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				//On ajoute l'objet a la banque et au monde
				BankObj = Item.getCloneObjet(PersoObj, qua);
				World.addItem(BankObj, true);
				_account.getBankItems().put(BankObj.getGuid(), BankObj);
				
				//Envoie des packets
				String str = "O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getID()+"|"+BankObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				
			}
		}else // S'il y avait un item du meme template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				removeItem(PersoObj.getGuid());
				//On enleve l'objet du monde
				World.removeItem(PersoObj.getGuid());
				//On ajoute la quantité a l'objet en banque
				BankObj.setQuantity(BankObj.getQuantity() + PersoObj.getQuantity());
				//on envoie l'ajout a la banque de l'objet
				String str = "O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getID()+"|"+BankObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				//on envoie la supression de l'objet du sac au joueur
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
				
			}else //S'il restait des objets
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				BankObj.setQuantity(BankObj.getQuantity() + qua);
				String str = "O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getID()+"|"+BankObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				
			}
		}
		SocketManager.GAME_SEND_Ow_PACKET(this);
		SQLManager.UPDATE_ACCOUNT_DATA(_account);
	}

	private Item getSimilarBankItem(Item obj)
	{
		for(Item value : _account.getBankItems().values())
		{
			if(value.getTemplate().getType() == 85)
				continue;
			if (value.is_linked())
				continue;
			if(value.getTemplate().getID() == obj.getTemplate().getID() && value.getStats().isSameStats(obj.getStats())) {
				if (value.isSpeaking() && obj.isSpeaking()) {
					Speaking obj_s = Speaking.toSpeaking(value);
					Speaking exObj_s = Speaking.toSpeaking(obj);
					if (obj_s.isSimmilar(exObj_s)) {
						return value;
					}
				} else if (!value.isSpeaking() && !obj.isSpeaking()) {
					return value;
				}
			}
		}
		return null;
	}

	public void removeFromBank(int guid, int qua)
	{
		Item BankObj = World.getObjet(guid);
		//Si le joueur n'a pas l'item dans sa banque ...
		if(_account.getBankItems().get(guid) == null)
		{
			Log.addToLog("Le joueur "+_name+" a tenter de retirer un objet en banque qu'il n'avait pas.");
			return;
		}
		
		Item PersoObj = getSimilarItem(BankObj);
		
		int newQua = BankObj.getQuantity() - qua;
		
		if(PersoObj == null)//Si le joueur n'avait aucun item similaire
		{
			//S'il ne reste rien en banque
			if(newQua <= 0)
			{
				//On retire l'item de la banque
				_account.getBankItems().remove(guid);
				//On l'ajoute au joueur
				_items.put(guid, BankObj);
				itemLog(BankObj.getTemplate().getID(), BankObj.getQuantity(), "Retrait de la banque");
				
				//On envoie les packets
				SocketManager.GAME_SEND_OAKO_PACKET(this,BankObj);
				String str = "O-"+guid;
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				
			}else //S'il reste des objets en banque
			{
				//On crée une copy de l'item en banque
				PersoObj = Item.getCloneObjet(BankObj, qua);
				//On l'ajoute au monde
				World.addItem(PersoObj, true);
				//On retire X objet de la banque
				BankObj.setQuantity(newQua);
				//On l'ajoute au joueur
				_items.put(PersoObj.getGuid(), PersoObj);
				itemLog(PersoObj.getTemplate().getID(), PersoObj.getQuantity(), "Retrait de la banque");
				
				//On envoie les packets
				SocketManager.GAME_SEND_OAKO_PACKET(this,PersoObj);
				String str = "O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getID()+"|"+BankObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				
			}
		}
		else
		{
			//S'il ne reste rien en banque
			if(newQua <= 0)
			{
				//On retire l'item de la banque
				_account.getBankItems().remove(BankObj.getGuid());
				World.removeItem(BankObj.getGuid());
				//On Modifie la quantité de l'item du sac du joueur
				PersoObj.setQuantity(PersoObj.getQuantity() + BankObj.getQuantity());
				
				//On envoie les packets
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				String str = "O-"+guid;
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				
			}
			else//S'il reste des objets en banque
			{
				//On retire X objet de la banque
				BankObj.setQuantity(newQua);
				//On ajoute X objets au joueurs
				PersoObj.setQuantity(PersoObj.getQuantity() + qua);
				
				//On envoie les packets
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,PersoObj);
				String str = "O+"+BankObj.getGuid()+"|"+BankObj.getQuantity()+"|"+BankObj.getTemplate().getID()+"|"+BankObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				
			}
		}
		SocketManager.GAME_SEND_Ow_PACKET(this);
		SQLManager.UPDATE_ACCOUNT_DATA(_account);
	}
	
	public void addInMount(int guid, int qua)
	{
		Item PersoObj = World.getObjet(guid);
		//Si le joueur n'a pas l'item dans son sac ...
		if(_items.get(guid) == null)
		{
			Log.addToLog("Le joueur "+_name+" a tenter d'ajouter un objet dans le sac de sa dragodinde qu'il n'avait pas.");
			return;
		}
		//Si c'est un item équipé ...
		if(PersoObj.getPosition() != Constants.ITEM_POS_NO_EQUIPED)return;
		
		Item mountObj = getSimilarMountItem(PersoObj);
		int newQua = PersoObj.getQuantity() - qua;
		if(mountObj == null)//S'il n'y pas d'item du meme Template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				removeItem(PersoObj.getGuid());
				//On met l'objet du sac dans le sac de sa dragodinde, avec la meme quantité
				_mount.getItems().add(PersoObj);
				String str = "O+"+PersoObj.getGuid()+"|"+PersoObj.getQuantity()+"|"+PersoObj.getTemplate().getID()+"|"+PersoObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
				
			}
			else//S'il reste des objets au joueur
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				//On ajoute l'objet dans le sac de sa dragodinde et au monde
				mountObj = Item.getCloneObjet(PersoObj, qua);
				World.addItem(mountObj, true);
				_mount.getItems().add(mountObj);
				
				//Envoie des packets
				String str = "O+"+mountObj.getGuid()+"|"+mountObj.getQuantity()+"|"+mountObj.getTemplate().getID()+"|"+mountObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				
			}
		}else // S'il y avait un item du meme template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				removeItem(PersoObj.getGuid());
				//On enleve l'objet du monde
				World.removeItem(PersoObj.getGuid());
				//On ajoute la quantité a l'objet dans le sac de sa dragodinde
				mountObj.setQuantity(mountObj.getQuantity() + PersoObj.getQuantity());
				//on envoie l'ajout dans le sac de sa dragodinde de l'objet
				String str = "O+"+mountObj.getGuid()+"|"+mountObj.getQuantity()+"|"+mountObj.getTemplate().getID()+"|"+mountObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				//on envoie la supression de l'objet du sac au joueur
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, guid);
				
			}else //S'il restait des objets
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				mountObj.setQuantity(mountObj.getQuantity() + qua);
				String str = "O+"+mountObj.getGuid()+"|"+mountObj.getQuantity()+"|"+mountObj.getTemplate().getID()+"|"+mountObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				
			}
		}
		SocketManager.GAME_SEND_MOUNT_PODS(this, _mount);
		SocketManager.GAME_SEND_Ow_PACKET(this);
		SQLManager.UPDATE_MOUNT_INFOS(_mount);
	}

	private Item getSimilarMountItem(Item obj)
	{
		for(Item value : _mount.getItems())
		{
			if(value.getTemplate().getType() == 85)
				continue;
			if (value.is_linked())
				continue;
			if(value.getTemplate().getID() == obj.getTemplate().getID() && value.getStats().isSameStats(obj.getStats())) {
				if (value.isSpeaking() && obj.isSpeaking()) {
					Speaking obj_s = Speaking.toSpeaking(value);
					Speaking exObj_s = Speaking.toSpeaking(obj);
					if (obj_s.isSimmilar(exObj_s)) {
						return value;
					}
				} else if (!value.isSpeaking() && !obj.isSpeaking()) {
					return value;
				}
			}
		}
		return null;
	}

	public void removeFromMount(int guid, int qua)
	{
		Item mountObj = World.getObjet(guid);
		//Si le joueur n'a pas l'item dans sa banque ... 
		if(_mount.getItems().indexOf(mountObj) == -1)
		{
			Log.addToLog("Le joueur "+_name+" a tenter de retirer un objet du sac de sa dragodinde qu'il n'avait pas.");
			return;
		}
		
		Item PersoObj = getSimilarItem(mountObj);
		
		int newQua = mountObj.getQuantity() - qua;
		
		if(PersoObj == null)//Si le joueur n'avait aucun item similaire
		{
			//S'il ne reste rien en banque
			if(newQua <= 0)
			{
				//On retire l'item de la banque
				_mount.getItems().remove(mountObj);
				//On l'ajoute au joueur
				_items.put(guid, mountObj);
				itemLog(mountObj.getTemplate().getID(), mountObj.getQuantity(), "Retrait du sac de la dragodinde");
				
				//On envoie les packets
				SocketManager.GAME_SEND_OAKO_PACKET(this,mountObj);
				String str = "O-"+guid;
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				
			}else //S'il reste des objets en banque
			{
				//On crée une copy de l'item en banque
				PersoObj = Item.getCloneObjet(mountObj, qua);
				//On l'ajoute au monde
				World.addItem(PersoObj, true);
				//On retire X objet de la banque
				mountObj.setQuantity(newQua);
				//On l'ajoute au joueur
				_items.put(PersoObj.getGuid(), PersoObj);
				itemLog(PersoObj.getTemplate().getID(), PersoObj.getQuantity(), "Retrait du sac de la dragodinde");
				
				//On envoie les packets
				SocketManager.GAME_SEND_OAKO_PACKET(this,PersoObj);
				String str = "O+"+mountObj.getGuid()+"|"+mountObj.getQuantity()+"|"+mountObj.getTemplate().getID()+"|"+mountObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				
			}
		}
		else
		{
			//S'il ne reste rien en banque
			if(newQua <= 0)
			{
				//On retire l'item de la banque
				_mount.getItems().remove(mountObj);
				World.removeItem(mountObj.getGuid());
				//On Modifie la quantité de l'item du sac du joueur
				PersoObj.setQuantity(PersoObj.getQuantity() + mountObj.getQuantity());
				
				//On envoie les packets
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				String str = "O-"+guid;
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				
			}
			else//S'il reste des objets en banque
			{
				//On retire X objet de la banque
				mountObj.setQuantity(newQua);
				//On ajoute X objets au joueurs
				PersoObj.setQuantity(PersoObj.getQuantity() + qua);
				
				//On envoie les packets
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this,PersoObj);
				String str = "O+"+mountObj.getGuid()+"|"+mountObj.getQuantity()+"|"+mountObj.getTemplate().getID()+"|"+mountObj.parseStatsString();
				SocketManager.GAME_SEND_EsK_PACKET(this, str);
				
			}
		}
		SocketManager.GAME_SEND_MOUNT_PODS(this, _mount);
		SocketManager.GAME_SEND_Ow_PACKET(this);
		SQLManager.UPDATE_MOUNT_INFOS(_mount);
	}

	public void openMountPark()
	{
		if(_deshonor >= 5)
		{
			SocketManager.GAME_SEND_Im_PACKET(this, "183");
			return;
		}
		_inMountPark = _curMap.getMountPark();
		_away = true;
		String str = parseDragoList();
		SocketManager.GAME_SEND_ECK_PACKET(this, 16, str);
	}
	
	/*--------------------LIGNE PAR MARTHIEUBEAN------------------------*/
	//Presque même methode que openMountPark, à la différence qu'elle ouvre toujours l'enclose publique de la map 8747
	public void openPublicMountPark()
	{
		_inMountPark = World.getMap(8747).getMountPark();
		_away = true;
		String str = parseDragoList();
		SocketManager.GAME_SEND_ECK_PACKET(this, 16, str);
	}
	/*--------------------------FIN--------------------------------------*/
	
	private String parseDragoList()
	{
		if(_account.getStable().size() == 0)return "~";
		StringBuilder packet = new StringBuilder();
		for(Mount DD : _account.getStable())
		{
			if(packet.length() >0)packet.append(';');
			packet.append(DD.parseInfos());
		}
		return packet.toString();
	}

	public void leftMountPark()
	{
		if(_inMountPark == null)return;
		_inMountPark = null;
	}

	public MountPark getInMountPark()
	{
		return _inMountPark;
	}

	public void fullPDV()
	{
		_PDV = _PDVMAX;
	}

	public void teleportToSavePos()
	{
		try
		{
			String[] infos = _savePos.split(",");
			teleport(Integer.parseInt(infos[0]), Integer.parseInt(infos[1]));
		}catch(Exception e){};
	}
	
	
	private void removeByTemplateID(int id) {
		int qua = 0;
		ArrayList<Item> toRemove = new ArrayList<Item>();
		for(Item curObj : _items.values())
		{
			if(curObj.getTemplate().getID() == id)
			{
				qua++;
				toRemove.add(curObj);
			}
		}
		for(Item curRem : toRemove)
		{
			_items.remove(curRem);
			World.removeItem(curRem.getGuid());
			if(_isOnline)
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, curRem.getGuid());
		}
		SocketManager.GAME_SEND_Im_PACKET(this, "022;"+-qua+"~"+id);
    }
	
	public void removeByTemplateID(int tID, int count)
	{
		//Copie de la liste pour eviter les modif concurrentes
		ArrayList<Item> list = new ArrayList<Item>();
		list.addAll(_items.values());
		
		ArrayList<Item> remove = new ArrayList<Item>();
		int tempCount = count;
		
		//on verifie pour chaque objet
		for(Item obj : list)
		{
			//Si mauvais TemplateID, on passe
			if(obj.getTemplate().getID() != tID)continue;
			
			if(obj.getQuantity() >= count)
			{
				int newQua = obj.getQuantity() - count;
				if(newQua >0)
				{
					obj.setQuantity(newQua);
					if(_isOnline)
						SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
				}else
				{
					//on supprime de l'inventaire et du Monde
					_items.remove(obj.getGuid());
					World.removeItem(obj.getGuid());
					//on envoie le packet si connecté
					if(_isOnline)
						SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, obj.getGuid());
				}
				return;
			}
			else//Si pas assez d'objet
			{
				if(obj.getQuantity() >= tempCount)
				{
					int newQua = obj.getQuantity() - tempCount;
					if(newQua > 0)
					{
						obj.setQuantity(newQua);
						if(_isOnline)
							SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, obj);
					}
					else remove.add(obj);
					
					for(Item o : remove)
					{
						//on supprime de l'inventaire et du Monde
						_items.remove(o.getGuid());
						World.removeItem(o.getGuid());
						//on envoie le packet si connecté
						if(_isOnline)
							SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, o.getGuid());
					}
				}else
				{
					// on réduit le compteur
					tempCount -= obj.getQuantity();
					remove.add(obj);
				}
			}
		}
	}

	public Map<Integer,JobStat> getMetiers()
	{
		return _jobs;
	}

	public void doJobAction(int actionID, InteractiveObject object, GameAction GA,DofusCell cell)
	{
		JobStat SM = getMetierBySkill(actionID);
		if(SM == null)return;
		SM.startAction(actionID,this, object,GA,cell);
	}
	public void finishJobAction(int actionID, InteractiveObject object, GameAction GA,DofusCell cell)
	{
		JobStat SM = getMetierBySkill(actionID);
		if(SM == null)return;
		SM.endAction(actionID,this, object,GA,cell);
	}

	public String parseJobData()
	{
		StringBuilder str = new StringBuilder();
		for(JobStat SM : _jobs.values())
		{
			if(str.length() >0)str.append(';');
			str.append(SM.getTemplate().getId()).append(',').append(SM.getXp());
		}
		return str.toString();
	}
	public boolean canAggro() {
		return _canAggro;
	}

	public void setCanAggro(boolean canAggro) {
		_canAggro = canAggro;
	}

	public void setCurJobAction(JobAction JA)
	{
		_curJobAction = JA;
	}
	public JobAction getCurJobAction()
	{
		return _curJobAction;
	}

	public JobStat getMetierBySkill(int skID)
	{
		for(JobStat SM : _jobs.values())
			if(SM.isValidMapAction(skID))return SM;
		return null;
	}

	public String parseToFriendList(int guid)
	{
		Account acc = World.getAccount(guid);
		StringBuilder str = new StringBuilder();
		str.append(';');
		str.append(_fight != null ? ('2') : ('1')).append(';');
		str.append(_name).append(';');
		str.append(_account.isFriendWith(acc.getGUID()) ? _lvl : '?').append(';');
		str.append(_account.isFriendWith(acc.getGUID()) ? _align : "-1").append(';');
		str.append(_breedId).append(';');
		str.append(_sex).append(';');
		str.append(_gfxId);
		return str.toString();
	}
	
	public String parseToEnemyList(int guid)
	{
		Account acc = World.getAccount(guid);
		StringBuilder str = new StringBuilder();
		str.append(';');
		str.append(_fight != null ? ('2') : ('1')).append(';');
		str.append(_name).append(';');
		str.append(_account.isEnemyWith(acc.getGUID()) ? _lvl : '?').append(';');
		str.append(_account.isEnemyWith(acc.getGUID()) ? _align : "-1").append(';');
		str.append(_breedId).append(';');
		str.append(_sex).append(';');
		str.append(_gfxId);
		return str.toString();
	}

	public JobStat getMetierByID(int job)
	{
		for(JobStat SM : _jobs.values())if(SM.getTemplate().getId() == job)return SM;
		return null;
	}

	public boolean isOnMount()
	{
		return _onMount;
	}
	public void toogleOnMount()
	{
		if(_mount.getEnergy()<= 0){
			SocketManager.GAME_SEND_Im_PACKET(this, "1113");
            _mount.setEnergy(0);
            return;
        }
		_onMount = !_onMount;
		Item obj = getObjetByPos(Constants.ITEM_POS_FAMILIER);
		if(_onMount && obj != null)
		{
			obj.setPosition(Constants.ITEM_POS_NO_EQUIPED);
			SocketManager.GAME_SEND_OBJET_MOVE_PACKET(this, obj);
			SocketManager.GAME_SEND_Im_PACKET(this, "188");
			return;
		}
		//on envoie les packets
		if(!_mount.isTireless())
		{
			_mount.setEnergy(_mount.getEnergy() - 10);
		}
		SocketManager.GAME_SEND_Re_PACKET(this, "+", _mount);
		if(_fight != null && _fight.getState() == 2)
		{
			SocketManager.GAME_SEND_ALTER_FIGHTER_MOUNT(_fight, _fight.getFighterByPerso(this), _GUID, _fight.getTeamID(_GUID), _fight.getEnemyTeamID(_GUID));
		}else
		{
			SocketManager.GAME_SEND_ALTER_GM_PACKET(_curMap,this);
		}
		SocketManager.GAME_SEND_Rr_PACKET(this,_onMount?"+":"-");
		clearCacheAS();
		SocketManager.GAME_SEND_STATS_PACKET(this);
	}
	public int getMountXpGive()
	{
		return _mountXpGive;
	}

	public Mount getMount()
	{
		return _mount;
	}

	public void setMount(Mount DD)
	{
		_mount = DD;
	}

	public void setMountGiveXp(int parseInt)
	{
		_mountXpGive = parseInt;
	}

	public void resetVars()
	{
		clearCacheAS();
		clearCacheGM();
		clearCacheGMStuff();
		_isTradingWith = 0;
		_isTalkingWith = 0;
		_away = false;
		emoteActive = 0;
		_duelID = 0;
		_ready = false;
		_curExchange = null;
		_party = null;
		_isInBank = false;
		_inviting = -1;
		_sitted = false;
		_curJobAction = null;
		_isZaaping = false;
		_inMountPark = null;
		_onMount = false;
		_tempSavePos = null;
		_track = null;
		_trackTime = 0;
		_lastItemUsed = 0;
		_changePseudo = false;
		_lastPacketTime = 0;
		_seeSeller = false;
		_curGameAction = null;
	}
	
	public void addChannel(String chan)
	{
		if(_channels.indexOf(chan) >=0)return;
		_channels += chan;
		SocketManager.GAME_SEND_cC_PACKET(this, '+', chan);
	}
	
	public void removeChannel(String chan)
	{
		_channels = _channels.replace(chan, "");
		SocketManager.GAME_SEND_cC_PACKET(this, '-', chan);
	}
	
	/*---------------------LIGNE PAR MARTHIEUBEAN-----------------*/
	//Appel la fonction de l'obet stats afin de mettre tout les stats à Zéro
	public void resetStats()
	{
		this._baseStats.resetStat();
	}
	
	//Fait seulement l'intermédiaire entre le monde extérieur et la fonction dans l'objets stats.
	public void setStat(int stat,int _nbr)
	{
		this._baseStats.setStat(stat,_nbr);
	}
	
	//Fonction pour définir de façon absolue le nombre de point de capital. Utilisé pour le restats des capitaux entre autres
	public void setCapital(int pts)
	{
		_capital = pts;
		clearCacheAS();
	}
	/*----------------------FIN------------------------------------*/

	public void modifAlignement(byte a)
	{
		//Reset Variables
		_honor = 0;
		_deshonor = 0;
		_align = a;
		_aLvl = 1;
		//envoies des packets
		//Im022;10~42 ?
		SocketManager.GAME_SEND_ZC_PACKET(this, a);
		clearCacheAS();
		clearCacheGM();
		SocketManager.GAME_SEND_STATS_PACKET(this);
		//Im045;50 ?
	}

	public void setDeshonor(int _deshonor)
	{
		this._deshonor = _deshonor;
		clearCacheAS();
		clearCacheGM();
	}

	public int getDeshonor()
	{
		return _deshonor;
	}

	public boolean isShowingWings()
	{
		return _showWings;
	}

	public void setShowWings(boolean showWings) {
		_showWings = showWings;
		if(_showWings)
			_lastTimeShowWings = System.currentTimeMillis();
	}

	public int getHonor()
	{
		return _honor;
	}

	public void setHonor(int honor)
	{
		_honor = honor;
		clearCacheAS();
		clearCacheGM();
	}
	public void setALvl(int a)
	{
		_aLvl = a;
		clearCacheAS();
		clearCacheGM();
	}
	public int getALvl()
	{
		return _aLvl;
	}

	public void toogleWings(char c)
	{
		if(_align == Constants.ALIGNEMENT_NEUTRE)return;
		int hloose = _honor/100*5;
		switch(c)
		{
		case '*':
			SocketManager.GAME_SEND_GIP_PACKET(this,hloose);
		return;
		case '+':
			_showWings = true;
			_lastTimeShowWings = System.currentTimeMillis();
		break;
		case '-':
			_showWings = false;
			_honor -= hloose;
		break;
		}
		clearCacheAS();
		clearCacheGM();
		SocketManager.GAME_SEND_STATS_PACKET(this);
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_curMap, _GUID);
		SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(_curMap, this);
		//SocketManager.GAME_SEND_ALTER_GM_PACKET(_curCarte, this);
	}

	public void addHonor(int winH)
	{
		int g = getGrade();
		_honor += winH;
		SocketManager.GAME_SEND_Im_PACKET(this, "080;"+winH);
		clearCacheAS();
		//Changement de grade
		if(getGrade() != g)
		{
			clearCacheGM();
			SocketManager.GAME_SEND_Im_PACKET(this, "082;"+getGrade());
		}
	}
	
	public void remHonor(int losePH)
	{
		if(_align == 0) return;
		int g = getGrade();
		_honor -= losePH;
		SocketManager.GAME_SEND_Im_PACKET(this, "081;"+losePH);
		clearCacheAS();
		//Changement de grade
		if(getGrade() != g)
		{
			clearCacheGM();
			SocketManager.GAME_SEND_Im_PACKET(this, "083;"+getGrade());
		}
	}

	public GuildMember getGuildMember()
	{
		return _guildMember;
	}

	public int getAccID()
	{
		return _accID;
	}

	public void setAccount(Account c)
	{
		_account = c;
	}
	
	public String parseZaapList()//Pour le packet WC
	{
		String map = _curMap.getId()+"";
		try
		{
			map = _savePos.split(",")[0];
		}catch(Exception e){};
		StringBuilder str = new StringBuilder(50);
		str.append(map);
		int SubAreaID = _curMap.getSubArea().getArea().getSuperArea().getId();
		for(int i : _zaaps)
		{
			if(World.getMap(i) == null)continue;
			if(World.getMap(i).getSubArea().getArea().getSuperArea().getId() != SubAreaID)continue;
			int cost = Formulas.calculZaapCost(_curMap, World.getMap(i));
			if(i == _curMap.getId()) cost = 0;
			str.append('|').append(i).append(';').append(cost);
		}
		return str.toString();
	}
	public String parseZaapiList(int city)//Pour packet Wc
	{
		String map = getCurMap().getId()+"";

		StringBuilder str = new StringBuilder(50);
		str.append(map);
		int cost = 20;
		if(_align == 1 && city == Constants.AREA_BONTA)
			cost = 10;
		else if(_align == 2 && city == Constants.AREA_BRAKMAR)
			cost = 10;
		for(int[] i : (city == Constants.AREA_BONTA?Constants.BONTA_ZAAPI:Constants.BRAKMAR_ZAAPI))
		{
			if(World.getMap(i[0]) == null)continue;
			if(i[0] == Integer.parseInt(map))continue;
			str.append('|').append(i[0]).append(';').append(cost);
		}
		return str.toString();
	}
	
	public boolean hasZaap(int mapID)
	{
		for(int i : _zaaps)if( i == mapID)return true;
		return false;
	}

	public void openZaapiMenu()
	{
		_isZaapiing = true;
		SocketManager.GAME_SEND_Wc_PACKET(this);
	}
	public void useZaapi(short id)
	{
		if(!_isZaapiing)return;//S'il n'a pas ouvert l'interface Zaap(hack?)
		int cost = Config.CONFIG_ZAAPI_COST;
		if(_kamas < cost)return;//S'il n'a pas les kamas (verif coté client)
		int mapID = id;
		int SubAreaID = _curMap.getSubArea().getArea().getSuperArea().getId();
		int cellID = World.getZaapiCellIdByMapId(id);
		if(World.getMap(mapID) == null)
		{
			Log.addToLog("La map "+id+" n'est pas implantee, Zaapi refuse");
			SocketManager.GAME_SEND_WUE_PACKET(this);
			return;
		}
		if(World.getMap(mapID).getCell(cellID) == null)
		{
			Log.addToLog("La cellule associee au zaapi "+id+" n'est pas implantee, Zaapi refuse");
			SocketManager.GAME_SEND_WUE_PACKET(this);
			return;
		}
		if(!World.getMap(mapID).getCell(cellID).isWalkable(true))
		{
			Log.addToLog("La cellule associee au zaapi "+id+" n'est pas 'walkable', Zaapi refuse");
			SocketManager.GAME_SEND_WUE_PACKET(this);
			return;
		}
		if(World.getMap(mapID).getSubArea().getArea().getSuperArea().getId() != SubAreaID)
		{
			SocketManager.GAME_SEND_WUE_PACKET(this);
			return;
		}
		_kamas -= cost;
		kamasLog(-cost+"", "Utilisation du Zaapi");
		
		SocketManager.GAME_SEND_Wv_PACKET(this);//On ferme l'interface Zaap
		clearCacheAS();
		SocketManager.GAME_SEND_STATS_PACKET(this);//On envoie la perte de kamas
		teleport(mapID,cellID);
		_isZaapiing = false;
	}
	public void openZaapMenu()
	{
		_isZaaping = true;
		if(!hasZaap(_curMap.getId()))//Si le joueur ne connaissait pas ce zaap
		{
			_zaaps.add(_curMap.getId());
			SocketManager.GAME_SEND_Im_PACKET(this, "024");
		}
		SocketManager.GAME_SEND_WC_PACKET(this);
	}
	public void useZaap(short id)
	{
		if(!_isZaaping)return;//S'il n'a pas ouvert l'interface Zaap(hack?)
		if(!hasZaap(id))return;//S'il n'a pas le zaap demandé(ne devrais pas arriver)
		int cost = Formulas.calculZaapCost(_curMap, World.getMap(id));
		if(_kamas < cost)return;//S'il n'a pas les kamas (verif coté client)
		int mapID = id;
		int SubAreaID = _curMap.getSubArea().getArea().getSuperArea().getId();
		int cellID = World.getZaapCellIdByMapId(id);
		if(World.getMap(mapID) == null)
		{
			Log.addToLog("La map "+id+" n'est pas implantee, Zaap refuse");
			SocketManager.GAME_SEND_WUE_PACKET(this);
			return;
		}
		if(World.getMap(mapID).getCell(cellID) == null)
		{
			Log.addToLog("La cellule associee au zaap "+id+" n'est pas implantee, Zaap refuse");
			SocketManager.GAME_SEND_WUE_PACKET(this);
			return;
		}
		if(!World.getMap(mapID).getCell(cellID).isWalkable(true))
		{
			Log.addToLog("La cellule associee au zaap "+id+" n'est pas 'walkable', Zaap refuse");
			SocketManager.GAME_SEND_WUE_PACKET(this);
			return;
		}
		if(World.getMap(mapID).getSubArea().getArea().getSuperArea().getId() != SubAreaID)
		{
			SocketManager.GAME_SEND_WUE_PACKET(this);
			return;
		}
		_kamas -= cost;
		kamasLog(-cost+"", "Utilisation du Zaap");
		
		teleport(mapID,cellID);
		clearCacheAS();
		SocketManager.GAME_SEND_STATS_PACKET(this);//On envoie la perte de kamas
		SocketManager.GAME_SEND_WV_PACKET(this);//On ferme l'interface Zaap
		_isZaaping = false;
	}
	public String parseZaaps()
	{
		StringBuilder str = new StringBuilder(5*_zaaps.size());
		boolean first = true;
		for(int i : _zaaps)
		{
			if(!first) str.append(',');
			first = false;
			str.append(i);
		}
		return str.toString();
	}
	public void stopZaaping()
	{
		if(!_isZaaping)return;
		_isZaaping = false;
		SocketManager.GAME_SEND_WV_PACKET(this);
	}
	public void stopZaapiing()
	{
		if(!_isZaapiing)return;
		_isZaapiing = false;
		SocketManager.GAME_SEND_Wv_PACKET(this);
	}
	
	public boolean hasItemTemplate(int i, int q)
	{
		for(Item obj : _items.values())
		{
			if(obj.getPosition() != Constants.ITEM_POS_NO_EQUIPED)continue;
			if(obj.getTemplate().getID() != i)continue;
			if(obj.getQuantity() >= q)return true;
		}
		return false;
	}
	
	public boolean isDispo(Player sender)
	{
		if(isAway)
			return false;
		
		if(isInvisible)
		{
			return _account.isFriendWith(sender.getAccount().getGUID());
		}
		
		return true;
	}
	
	public boolean canTrack()
	{
		return _track == null && _trackTime < (System.currentTimeMillis() - 600000) && _lvl >= 50;
	}
	
	public void addTrackItem()
	{
		ItemTemplate T = World.getItemTemplate(10085);
		Item newObj = T.createNewItem(20, false);
		newObj.addTxtStat(960, ""+_track.getAlign());
		newObj.addTxtStat(961, ""+_track.getGrade());
		newObj.addTxtStat(962, ""+_track.getLvl());
		newObj.addTxtStat(963, "0");//TODO Créer depuis (value) jour(s).
		newObj.addTxtStat(989, _track.getName());
		removeByTemplateID(T.getID());
		if (addItem(newObj, true)) 
		{
			World.addItem(newObj, true);
		}
		SocketManager.GAME_SEND_Im_PACKET(this, "021;20~"+T.getID());
		ItemTemplate template = World.getItemTemplate(10621);
		newObj = null;
		newObj = T.createNewItem(1, false);
		newObj.addTxtStat(805, Formulas.getDateStr('#'));
		removeByTemplateID(template.getID());
		if (addItem(newObj, true)) 
		{
			World.addItem(newObj, true);
		}
		SocketManager.GAME_SEND_Im_PACKET(this, "021;20~"+template.getID());
	}


	public void setTraque(Player traque)
	{
		_track = traque;
		_trackTime = System.currentTimeMillis();
		SocketManager.GAME_SEND_INFOS_COMPASS_PACKET(this, _track.getCurMap().getX()+"|"+_track.getCurMap().getY());
	}
	
	/*public void setShowRank(boolean show)
	{
		showRank = show;
		/*if(show)
		{
			String rank;
			if(_compte.get_gmLvl() > 0)
				rank = "[" + Constants.getRankName(_compte.get_gmLvl()) + "]";
			else
				rank = "";
			
			_name = rank + _name;
		}
		else
		{
			if(!_name.contains("]"))
				return;
			_name = _name.substring(_name.indexOf("]")+1);
		}
		
	}*/
		
	public void initLogger()
	{
		//Instanciation du logger
		logger = new Logger("Perso_logs/"+_name+".txt", Config.LOGGER_BUFFER_SIZE);
		
		int mois = Calendar.getInstance().get(Calendar.MONTH);
		int jour = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 1;	//+1, janvier = 0
		int annee = Calendar.getInstance().get(Calendar.YEAR);
		
		logger.newLine();logger.newLine();	//Passe deux ligne
		logger.toWrite("Connexion le " + annee + "-" + jour + "-" + mois);
	}
	public synchronized void closeLogger()
	{
		if(logger != null)
			logger.close();
	}
	public synchronized void kamasLog(String qte,String raison)
	{
		if(logger == null)return;
		
		String toLog = "Kamas:" + qte + "k Raison: "+raison;	//-500k Raison: Achat de l'item '7754' à un PNJ
		logger.toWrite(toLog);
	}
	public synchronized void itemLog(int obj, int qte, String raison)
	{
		if(logger == null)return;
		
		String nomObj = World.getItemTemplate(obj).getName();
		String toLog = "Objet: " + qte + "x " + nomObj + " Raison: " + raison; //-5x 7754 Raison: Vente à un PNJ
		logger.toWrite(toLog);
	}
	public synchronized void commitLogger()
	{
		if(logger != null)		
			logger.write();
	}
	public synchronized void sockLog(String packet)
	{
		if(logger == null)return;
		logger.toWrite(packet);
		
	}

	public void setIsForgetingSpell(boolean isForgetingSpell) {
		this.isForgetingSpell = isForgetingSpell;
	}

	public boolean isForgetingSpell() {
		return isForgetingSpell;
	}
	
	public void setTitle(byte title) {
	    this.title = title;
	    clearCacheGM();
    }

	public byte getTitle() {
	    return title;
    }

	public void savePos() {
		_tempSavePos = _curMap.getId()+","+_curCell.getId();
    }

	public String getTempSavePos() {
	    return _tempSavePos;
    }
	
	public void teleportToTempSavePos()
	{
		String savePos = getTempSavePos();
		teleport(Integer.parseInt(savePos.split(",")[0]), Integer.parseInt(savePos.split(",")[1]));
	}

	public void setTraqueTime(long _traqueTime) {
	    this._trackTime = _traqueTime;
    }

	public long getTraqueTime() {
	    return _trackTime;
    }

	public String getLastTeamFight() {
	    return null;
    }

	public ArrayList<Quest> getQuestList() {
	    return _questList;
    }

	public Quest getQuest(int id) {
		for(Quest quest : _questList)
		{
			if(quest.getId() == id)
			{
				return quest;
			}
		}
		return null;
    }

	public String getStepsFinished(Quest quest) {
		StringBuilder packet = new StringBuilder(5*quest.getSteps().size());
		boolean isFirst = true;
		for (int i = 0; i < quest.getCurStep().getOrder(); i--)
		{
			if(!isFirst) 
				packet.append(';');
			
			packet.append(quest.getSteps().get(i).getId());
			isFirst = false;
		}
		return packet.toString();
    }

	public String getStepsToRealise(Quest quest) {
		StringBuilder packet = new StringBuilder(5*quest.getSteps().size());
		boolean isFirst = true;
		for (int i = 0; i < quest.getCurStep().getOrder(); i++)
		{
			if(!isFirst) 
				packet.append(';');

			packet.append(quest.getSteps().get(i).getId());			
			isFirst = false;
		}
		return packet.toString();
    }

	public void checkQuests() {
	    for(Quest quest : _questList)
	    {
	    	if(quest.isFinished()) 
	    	{
	    		continue;
	    	}
	    	quest.check(this);
	    }
    }
	
	public void applyQuest(Object object) {//On ne spécifie pas le type de l'objet car c'soit un string soit une liste
		for(Quest quest : _questList)
	    {
	    	if(quest.isFinished()) 
	    	{
	    		continue;
	    	}
	    	for(QuestObjective objective : quest.getCurStep().getObjectives())
	    	{
	    		objective.fill(this, object);
	    	}
	    }
    }
	
	public int returnPosToolTip()
	{
		int pos = 20;
		while(getObjetByPos(pos) != null && pos < 26)
		{
			pos++;
		}
		return pos;
	}

	public void setLastItemUsed(int _lastItemUsed) {
	    this._lastItemUsed = _lastItemUsed;
    }

	public int getLastItemUsed() {
	    return _lastItemUsed;
    }
	
	private void setRestrictions(Restriction restrictions) {
		_restrictions = restrictions;
    }

	public Restriction getRestrictions() {
	    return _restrictions;
    }

	public void changePseudo(boolean b) {
	    _changePseudo  = b;
    }

	public boolean isChangePseudo() {
	    return _changePseudo;
    }
	
	public long getLastPacketTime()
	{
		return _lastPacketTime;
	}

	public void refreshLastPacketTime() {
		_lastPacketTime = System.currentTimeMillis();
    }
	
	public String parseFollowers()
	{
		if(monstersFollower.size() == 0)
			return "";
		StringBuilder parse = new StringBuilder(15*monstersFollower.size());
		boolean isFirst = true;
		for(MonsterFollower e : monstersFollower.values())
		{
			if(!isFirst)
				parse.append(';');
			parse.append(e.getMobId()).append(',').append(e.getItemId()).append(',').append(e.getTurns());
			isFirst = false;
		}
		return parse.toString();
	}

	public void addMonstersFollower(MonsterFollower monsterFollower) {
	    this.monstersFollower.put(monsterFollower.getItemId(), monsterFollower);
	    Item toolTip = World.getItemTemplate(monsterFollower.getItemId()).createNewItem(1, true);
	    toolTip.setPosition(returnPosToolTip());
	    if(!toolTip.getTxtStat().containsKey(811))toolTip.addTxtStat(811, monsterFollower.getTurns()+"");//Nb de tours
	    if(!toolTip.getTxtStat().containsKey(148))toolTip.addTxtStat(148, "");//Quelqu'un vous suit !
	    if(addItem(toolTip, true))
	    	World.addItem(toolTip, true);
	    SQLManager.SAVE_PERSONNAGE(this, true);
	    SocketManager.GAME_SEND_ALTER_GM_PACKET(_curMap, this);
    }

	public Map<Integer, MonsterFollower> getMonstersFollower() {
	    return monstersFollower;
    }
	
	public Map<Integer, Integer> getStoreItems()
	{
		return _storeItems ;
	}
	
	public String parseStoreItemstoBD()
	{
		StringBuilder str = new StringBuilder(10*_storeItems.size());
		for(Entry<Integer, Integer> _storeObjets : _storeItems.entrySet())
		{
			str.append(_storeObjets.getKey()).append(',').append(_storeObjets.getValue()).append('|');
		}
		return str.toString();
	}

	public String parseStoreItemsList() 
    {
    	StringBuilder list = new StringBuilder();
        if(_storeItems.isEmpty())return "";
        for(Entry<Integer,Integer> obj : _storeItems.entrySet()) 
        {
        	Item O = World.getObjet(obj.getKey());
        	if(O == null) continue;
        	list.append(O.getGuid()).append(';').append(O.getQuantity()).append(';').append(O.getTemplate().getID()).append(';').append(O.parseStatsString()).append(';').append(obj.getValue()).append('|');
        }
        return (list.length()>0?list.toString().substring(0, list.length()-1):list.toString());
    }
	
	public void addinStore(int ObjID, int price, int qua)
    {
		Item PersoObj = World.getObjet(ObjID);
		//Si le joueur n'a pas l'item dans son sac ...
		if(_storeItems.get(ObjID) != null)
		{
			_storeItems.remove(ObjID);
			_storeItems.put(ObjID, price);
			SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
			return;
		}
		if(_items.get(ObjID) == null)
		{
			Log.addToLog("Le joueur "+_name+" a tenter d'ajouter un objet au store qu'il n'avait pas.");
			return;
		}
		//Si c'est un item équipé ...
		if(PersoObj.getPosition() != Constants.ITEM_POS_NO_EQUIPED)return;
		
		Item SimilarObj = getSimilarStoreItem(PersoObj);
		int newQua = PersoObj.getQuantity() - qua;
		if(SimilarObj == null)//S'il n'y pas d'item du meme Template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				removeItem(PersoObj.getGuid());
				//On met l'objet du sac dans le store, avec la meme quantité
				_storeItems.put(PersoObj.getGuid(), price);
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, PersoObj.getGuid());
                SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
			}
			else//S'il reste des objets au joueur
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				//On ajoute l'objet a la banque et au monde
				SimilarObj = Item.getCloneObjet(PersoObj, qua);
				World.addItem(SimilarObj, true);
				_storeItems.put(SimilarObj.getGuid(), price);
				
				//Envoie des packets
				SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				
			}
		}else // S'il y avait un item du meme template
		{
			//S'il ne reste pas d'item dans le sac
			if(newQua <= 0)
			{
				//On enleve l'objet du sac du joueur
				removeItem(PersoObj.getGuid());
				//On enleve l'objet du monde
				World.removeItem(PersoObj.getGuid());
				//On ajoute la quantité a l'objet en banque
				SimilarObj.setQuantity(SimilarObj.getQuantity() + PersoObj.getQuantity());
				_storeItems.remove(SimilarObj.getGuid());
				_storeItems.put(SimilarObj.getGuid(), price);
				//on envoie l'ajout a la banque de l'objet
				SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
				//on envoie la supression de l'objet du sac au joueur
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, PersoObj.getGuid());
			}else //S'il restait des objets
			{
				//on modifie la quantité d'item du sac
				PersoObj.setQuantity(newQua);
				SimilarObj.setQuantity(SimilarObj.getQuantity() + qua);
				_storeItems.remove(SimilarObj.getGuid());
				_storeItems.put(SimilarObj.getGuid(), price);
				SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				
			}
		}
		SocketManager.GAME_SEND_Ow_PACKET(this);
		SQLManager.SAVE_PERSONNAGE(this, true);
    }

	private Item getSimilarStoreItem(Item obj)
	{
		for(Entry<Integer, Integer> value : _storeItems.entrySet())
		{
			Item obj2 = World.getObjet(value.getKey());
			if(obj2.getTemplate().getType() == 85)
				continue;
			if(obj2.getTemplate().getID() == obj.getTemplate().getID() && obj2.getStats().isSameStats(obj.getStats()))
				return obj2;
		}
		return null;
	}
	
	public void removeFromStore(int guid, int qua)
	{
		Item SimilarObj = World.getObjet(guid);
		//Si le joueur n'a pas l'item dans son store ...
		if(_storeItems.get(guid) == null)
		{
			Log.addToLog("Le joueur "+_name+" a tenter de retirer un objet du store qu'il n'avait pas.");
			return;
		}
		
		Item PersoObj = getSimilarItem(SimilarObj);
		
		int newQua = SimilarObj.getQuantity() - qua;
		
		if(PersoObj == null)//Si le joueur n'avait aucun item similaire
		{
			//S'il ne reste rien en store
			if(newQua <= 0)
			{
				//On retire l'item du store
				_storeItems.remove(guid);
				//On l'ajoute au joueur
				_items.put(guid, SimilarObj);
				
				//On envoie les packets
				SocketManager.GAME_SEND_OAKO_PACKET(this,SimilarObj);
				SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
				
			}
		}
		else
		{
			//S'il ne reste rien en store
			if(newQua <= 0)
			{
				//On retire l'item de la banque
				_storeItems.remove(SimilarObj.getGuid());
				World.removeItem(SimilarObj.getGuid());
				//On Modifie la quantité de l'item du sac du joueur
				PersoObj.setQuantity(PersoObj.getQuantity() + SimilarObj.getQuantity());
				
				//On envoie les packets
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(this, PersoObj);
				SocketManager.GAME_SEND_ITEM_LIST_PACKET_SELLER(this, this);
				
			}
		}
		SocketManager.GAME_SEND_Ow_PACKET(this);
		SQLManager.SAVE_PERSONNAGE(this, true);
	}

	public boolean isShowSeller() {
		return _seeSeller;
	}
	
	public void setShowSeller(boolean is) {
		_seeSeller  = is;
	}
	
	public int GetOfflineType()
	{
		/*int weaponStuff = 0, various = 0, resources = 0;
		String e = "";
		
		for(Entry<Integer, Integer> entry : _storeItems.entrySet())
		{
			int type = World.getObjet(entry.getKey()).getTemplate().getType();
			if(Constants.isWeaponOrStuffItem(type))
			{
				if(!e.contains(weaponStuff+""))
				{
					if(e.length() >0) e+=";";
					e+=weaponStuff;
				}
			} else if(Constants.isVariousItem(type))
			{
				if(!e.contains(various+""))
				{
					if(e.length() >0) e+=";";
					e+=various;
				}
			} else
			{
				if(!e.contains(resources+""))
				{
					if(e.length() >0) e+=";";
					e+=resources;
				}
			}
		}
		int toReturn = 0;
		try
		{
			toReturn += Integer.parseInt(e.split(";")[0]);
			toReturn += Integer.parseInt(e.split(";")[1]);
			toReturn += Integer.parseInt(e.split(";")[2]);
		} catch(Exception ex) {}
		return toReturn;*/
		return 0;
	}

	public String parseToMerchantGM() {
		StringBuilder str = new StringBuilder(30);
    	str.append(_curCell.getId()).append(';');
    	str.append(_orientation).append(';');
    	str.append('0').append(';');
    	str.append(_GUID).append(';');
    	str.append(_name).append(';');
    	str.append("-5").append(';');//Merchant identifier
    	str.append(_gfxId).append('^').append(_size).append(';');
		str.append((_color1==-1?"-1":Integer.toHexString(_color1))).append(';');
		str.append((_color2==-1?"-1":Integer.toHexString(_color2))).append(';');
		str.append((_color3==-1?"-1":Integer.toHexString(_color3))).append(';');
    	str.append(getGMStuffString()).append(';');//acessories
    	str.append((_guildMember != null ? _guildMember.getGuild().getName() : "")).append(';');//guildName
    	str.append((_guildMember != null ? _guildMember.getGuild().getEmblem() : "")).append(';');//emblem
    	str.append(GetOfflineType()).append(';');//offlineType
        return str.toString();
    }

	public boolean isDecoFromFight() {
		return _isDecoFromFight;
	}

	public void setIsDecoFromFight(boolean b) {
		_isDecoFromFight = b;
	}
	
	public void setRights(Right _rights) {
	    this._rights = _rights;
    }

	public Right getRights() {
	    return _rights;
    }
	
	public Emote getEmotes()
	{
		return _emotes;
	}
	
	private void setEmotes(Emote emotes) 
	{
		_emotes = emotes;
	}
	
	public Emote initEmotes()
	{
		Emote init = new Emote(1);
		return init;
	}
	
	public void linkItems() {
		ArrayList<Integer> dustbin = new ArrayList<Integer>();
		for (Item obj : _items.values()) {
			if (obj.isSpeaking()) {
				Speaking obv = Speaking.toSpeaking(obj);
				int guid = obv.getLinkedID();
				if (guid > 0) {
					Item linked = World.getObjet(guid);
					obv.setLinkedItem(linked);
					linked.setLinkedItem(obv);
					dustbin.add(obv.getGuid()); //on delete tout a la fin du for (car meme list)
				}
			}
		}
		for (int guid : dustbin) {
			_items.remove(guid);
		}
		SQLManager.SAVE_PERSONNAGE(this, false);
	}

	public void revive() {
		if(getDeathCount() == 5)
		{
			SQLManager.DELETE_PERSO_IN_BDD(this);
			World.unloadPerso(_GUID);
			return;
		}
		int mapId = World.getBreed(_breedId).getStartMap();//Constants.getStartMap(_breedId);
		int cellId = World.getBreed(_breedId).getStartCell();//Constants.getStartCell(_breedId);
		_size = 100;
		_curExp = 0;
		_levelReached = _lvl;
		_lvl = 1;
		_spellPts = 0;
		_capital = 0;
		_energy = 1;
		_PDVMAX = Constants.getBasePdv(_breedId);
		_PDV = _PDVMAX;
		_gfxId = _breedId*10+_sex;
		_spells.clear();
		_spellsPlaces.clear();
		learnBreedSpell();
		_spellsPlaces = Constants.getStartSortsPlaces(_breedId);
		_baseStats.resetStat();
		_jobs.clear();
		_zaaps.clear();
		_curMap = World.getMap(mapId);
		_curCell = _curMap.getCell(cellId);
		_savePos = mapId+","+cellId;
		_align = 0;
		_honor = 0;
		_deshonor = 0;
		_aLvl = 1;
		_isDead = 0;
		_restrictions.setDefault();
		_rights.setDefault();
		SQLManager.SAVE_PERSONNAGE(this, false);
	}
	
	public synchronized void restoreItems() //A faire lors d'une mort en mode héroïque
	{
		for(SpellStat spell : getSpells().values())
		{
			Item obj = null;
			int spellID = spell.getSpellID();
			switch(spellID)
			{
			case 422: //Mise en garde
				obj = World.getItemTemplate(9201).createNewItem(1, false);
			break;
			case 423: //Ivresse
				obj = World.getItemTemplate(10510).createNewItem(1, false);
			break;				
			case 424: //Raulebaque
				obj = World.getItemTemplate(10513).createNewItem(1, false);
			break;				
			case 425: //Retraite anticipé
				obj = World.getItemTemplate(10507).createNewItem(1, false);
			break;				
			case 426: //Arbre de vie
				obj = World.getItemTemplate(10512).createNewItem(1, false);
			break;				
			case 427: //Mot lotof
				obj = World.getItemTemplate(10509).createNewItem(1, false);
			break;				
			case 420: //Laisse spirituelle
				obj = World.getItemTemplate(9916).createNewItem(1, false);
			break;			
			case 421: //Douleur partagé
				obj = World.getItemTemplate(10511).createNewItem(1, false);
			break;		
			case 416: //Poisse
				obj = World.getItemTemplate(6966).createNewItem(1, false);
			break;	
			case 418: //Flèche de dispersion
				obj = World.getItemTemplate(10506).createNewItem(1, false);
			break;	
			case 412: //Félintion
				obj = World.getItemTemplate(6664).createNewItem(1, false);
			break;		
			case 410: //Brokle
				obj = World.getItemTemplate(731).createNewItem(1, false);
			break;		
			case 367: //Cawotte
				obj = World.getItemTemplate(9201).createNewItem(1, false);
			break;	
			case 370: //Arakne
				obj = World.getItemTemplate(721).createNewItem(1, false);
			break;	
			case 369: // Foudroiment
				obj = World.getItemTemplate(720).createNewItem(1, false);
			break;		
			case 373: //Chaferfu
				obj = World.getItemTemplate(9200).createNewItem(1, false);
			break;	
			case 350: //Flamiche
				obj = World.getItemTemplate(718).createNewItem(1, false);
			break;	
			case 368: //Libération
				obj = World.getItemTemplate(719).createNewItem(1, false);
			break;
			case 390: //Maîtrise du bâton
				obj = World.getItemTemplate(724).createNewItem(1, false);
			break;	
			case 391: //Maîtrise de l'épée
				obj = World.getItemTemplate(725).createNewItem(1, false);
			break;
			case 392: //Maîtrise des arcs
				obj = World.getItemTemplate(726).createNewItem(1, false);
			break;
			case 393: //Maîtrise des marteaux
				obj = World.getItemTemplate(727).createNewItem(1, false);
			break;
			case 394: //Maîtrise des baguettes
				obj = World.getItemTemplate(728).createNewItem(1, false);
			break;	
			case 395: //Maîtrise des dagues
				obj = World.getItemTemplate(729).createNewItem(1, false);
			break;
			case 396: //Maîtrise des pelles
				obj = World.getItemTemplate(730).createNewItem(1, false);
			break;
			case 397: //Maîtrise des haches
				obj = World.getItemTemplate(8087).createNewItem(1, false);
			break;
			}
			if(obj != null)
			{
				if(addItem(obj, true))
					World.addItem(obj, true);
			}
		}
		Item skull = null;
		switch(_breedId)
		{
			case Constants.CLASS_FECA:
				skull = World.getItemTemplate(9077).createNewItem(1, false);
			break;
			case Constants.CLASS_SRAM:
				skull = World.getItemTemplate(9080).createNewItem(1, false);
			break;
			case Constants.CLASS_IOP:
				skull = World.getItemTemplate(9084).createNewItem(1, false);
			break;
			case Constants.CLASS_SACRIEUR:
				skull = World.getItemTemplate(9087).createNewItem(1, false);
			break;
			case Constants.CLASS_XELOR:
				skull = World.getItemTemplate(9081).createNewItem(1, false);
			break;
			case Constants.CLASS_ENUTROF:
				skull = World.getItemTemplate(9079).createNewItem(1, false);
			break;
			case Constants.CLASS_ENIRIPSA:
				skull = World.getItemTemplate(9083).createNewItem(1, false);
			break;
			case Constants.CLASS_PANDAWA:
				skull = World.getItemTemplate(9088).createNewItem(1, false);
			break;
			case Constants.CLASS_ECAFLIP:
				skull = World.getItemTemplate(9082).createNewItem(1, false);
			break;
			case Constants.CLASS_SADIDA:
				skull = World.getItemTemplate(9086).createNewItem(1, false);
			break;
			case Constants.CLASS_CRA:
				skull = World.getItemTemplate(9085).createNewItem(1, false);
			break;
			case Constants.CLASS_OSAMODAS:
				skull = World.getItemTemplate(9078).createNewItem(1, false);
			break;			
		}
		if(addItem(skull, true)) World.addItem(skull, true);
		if(_mount != null)
		{
			for(Item item : _mount.getItems())
			{
				if(addItem(item, true))
					World.addItem(item, true);
			}
			_mount.getItems().clear();
			Item obj = Constants.getParchoTemplateByMountColor(_mount.getColor()).createNewItem(1, false);
			obj.clearStats();
			obj.getStats().addOneStat(995, _mount.getId());
			obj.addTxtStat(996, getName());
			obj.addTxtStat(997, _mount.getName());
			if(addItem(obj, true))
				World.addItem(obj, true);
		}
		for(int Guid : _storeItems.keySet())
		{
			Item item = World.getObjet(Guid);
			if(addItem(item, true)) World.addItem(item, true);
		}
	}
	
	public synchronized void deleteAllItems()
	{
		for(Item itm : _items.values())
		{
			if(itm.getQuantity() != -1)
			{
				itm.setPosition(-1);
			}
			SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(this, itm.getGuid());
		}
		_items.clear();
		_storeItems.clear();
		World.deletePlayerItems(_GUID);
	}
	
	public byte isDead() {
		return _isDead;
	}

	public void setIsDead(byte _isDead) {
		this._isDead = _isDead;
	}

	public byte getDeathCount() {
		return _deathCount;
	}

	public void setDeathCount(byte _deathCount) {
		this._deathCount = _deathCount;
	}

	public int getLevelReached() {
		return _levelReached;
	}

	public void setLevelReached(int _levelReached) {
		this._levelReached = _levelReached;
	}

	public long getLastTimeShowWings() {
		return _lastTimeShowWings;
	}

	public GameAction getCurGameAction() {
		return _curGameAction;
	}

	public void setCurGameAction(GameAction curGameAction) {
		_curGameAction  = curGameAction;
	}

	public void forgetJob(int id) {
		_jobs.remove(id);
		clearCacheAS();
	}

	@Override
	public int getActorType() {
		return GameActorTypeEnum.TYPE_CHARACTER.getActorType();
	}

	@Override
	public int getMapId() {
		return _curMap.getId();
	}

	@Override
	public int getCellId() {
		return _curCell.getId();
	}

	@Override
	public int getActorId() {
		return _GUID;
	}
	
	public boolean haveSpouse() {
		return _spouse != null;
	}

	public Spouse getSpouse() {
		return _spouse;
	}

	public int getSameSentences() {
		return sameSentences;
	}

	public boolean analyzeMessage(String msg) {
		boolean correct = true;
		
		if(isSpammer) {
			tryBreakAntiSpamTimer();
			if(isSpammer) {
				SocketManager.GAME_SEND_MESSAGE_SERVER(this, "10");
				return false;
			}
		}
		
		nbrSentences++;
		startAntiSpamTimer();
		
		int nb_excla = 0;
		for(char character : msg.toCharArray()) {
			if(character == '!')
				nb_excla++;
			if(nb_excla == 5)
				break;
		}
		if(nb_excla == 5) {
			correct = false;
			SocketManager.GAME_SEND_Im_PACKET(this, "1122");
			sentencesExcla++;
		}
		if(sentencesExcla == 3) {
			sentencesExcla = 0;
			_account.mute(true, 30);
		}
		
		if(lastSentence == msg)
			sameSentences++;
		if(sameSentences == 5) {
			correct = false;
			timeAntiFlood = 60;
			startAntiFloodTimer();
			SocketManager.GAME_SEND_Im_PACKET(this, "184");
		}
		lastSentence = msg;
		
		return correct;
	}

	private void startAntiSpamTimer() {
		final Player thiz = this;
		if(timerAntiSpam == null) {
			timerAntiSpam = new Timer(2000, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(nbrSentences >= 3) {
						isSpammer = true;
					} else if(nbrSentences >= 5) {
						SocketManager.GAME_SEND_MESSAGE_SERVER(thiz, "10");
					} else if(nbrSentences >= 10) {
						_account.getGameThread().kick();
					}
					nbrSentences = 0;
					timerAntiSpam.stop();
					timerAntiSpam = null;
				}
			});
			timerAntiSpam.start();
		}
	}
	
	private void tryBreakAntiSpamTimer() {
		if(timerBreakAntiSpam == null) {
			timerBreakAntiSpam = new Timer(10000, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					isSpammer = false;
					timerBreakAntiSpam.stop();
					timerBreakAntiSpam = null;
				}
			});
			timerBreakAntiSpam.start();
		}
	}

	private void startAntiFloodTimer() {
		if(timerAntiFlood == null && timeAntiFlood > 0) {
			timerAntiFlood = new Timer(timeAntiFlood*1000, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					lastSentence = "";
					sameSentences = 0;
					timerAntiFlood.stop();
				}
			});
			timerAntiFlood.start();
		} else {
			timerAntiFlood.setDelay(this.timeAntiFlood*1000);
			timerAntiFlood.restart();
		}
	}

	public boolean isGhost() {
		return _isGhost;
	}

	public void setIsGhost(boolean _isGhost) {
		this._isGhost = _isGhost;
	}

}
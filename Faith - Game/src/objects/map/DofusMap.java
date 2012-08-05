package objects.map;


import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import objects.action.Action;
import objects.alignment.Prism;
import objects.character.Player;
import objects.fight.Fight;
import objects.fight.Fighter;
import objects.guild.TaxCollector;
import objects.item.Item;
import objects.monster.MonsterGrade;
import objects.monster.MonsterGroup;
import objects.npc.Npc;
import objects.npc.NpcTemplate;

import common.ConditionParser;
import common.Config;
import common.Constants;
import common.SocketManager;
import common.World;
import common.World.SubArea;
import common.console.Log;
import common.utils.CryptManager;
import common.utils.Formulas;
import common.utils.Pathfinding;

public class DofusMap {
	private int _id;
	private String _date;
	private int _w;
	private int _h;
	private String _key;
	private String _placesStr;
	private Map<Integer,DofusCell> 		_cases 			= new TreeMap<Integer,DofusCell>();
	private final Map<Integer,Fight> 		_fights 		= new TreeMap<Integer,Fight>();
	private final ArrayList<MonsterGrade> 	_mobPossibles 	= new ArrayList<MonsterGrade>();
	private final Map<Integer,MonsterGroup> 	_mobGroups 		= new TreeMap<Integer,MonsterGroup>();
	private final Map<Integer,MonsterGroup> 	_fixMobGroups 		= new TreeMap<Integer,MonsterGroup>();
	private final Map<Integer,Npc>		_npcs	 		= new TreeMap<Integer, Npc>();
	public int _nextObjectID = -1;
	private int _X = 0;
	private int _Y = 0;
	private SubArea _subArea;
	private MountPark _mountPark;
	private int _maxGroup = 3;
	private final Map<Integer,ArrayList<Action>> _endFightAction = new TreeMap<Integer,ArrayList<Action>>();
	
	/*MARTHIEUBEAN*/
	private int _maxSize;	//Indique nombre maximal de monstre possible dans un groupe
	private TaxCollector perco;
	/*FIN*/
	private Prism prism;
	
	public DofusMap(final int _id, final String _date, final int _w, final int _h, final String _key, final String places,final String dData,final String monsters,final String mapPos,final int maxGroup,final int maxSize)	//MARTHIEUBEAN
	{
		this._id = _id;
		this._date = _date;
		this._w = _w;
		this._h = _h;
		this._key = _key;
		this._placesStr = places;
		this._maxGroup = maxGroup;
		this._maxSize = maxSize;	//MARTHIEUBEAN
		final String[] mapInfos = mapPos.split(",");
		try
		{
			this._X = Integer.parseInt(mapInfos[0]);
			this._Y = Integer.parseInt(mapInfos[1]);
			final int subArea = Integer.parseInt(mapInfos[2]);
			_subArea = World.getSubArea(subArea);
			if(_subArea != null)_subArea.addMap(this);
		}catch(final Exception e)
		{
			Log.addToErrorLog("Erreur de chargement de la map "+_id+": Le champ MapPos est invalide");
			System.exit(0);
		}
		
		if(Config.CONFIG_COMPILED_MAP)
		{
			_cases = CryptManager.decompileMapData(this,dData);
		}
		else
		{
			final String cellsData = dData;
			final String[] cellsDataArray = cellsData.split("\\|");
			
			for(final String o : cellsDataArray)
			{
				
				boolean Walkable = true;
				boolean LineOfSight = true;
				int Number = -1;
				int obj = -1;
				final String[] cellInfos = o.split(",");
				try
				{
					Walkable = cellInfos[2].equals("1");
					LineOfSight = cellInfos[1].equals("1");
					Number = Integer.parseInt(cellInfos[0]);
					if(!cellInfos[3].trim().equals(""))
					{
						obj = Integer.parseInt(cellInfos[3]);
					}
				}catch(final Exception d){};
				if(Number == -1)continue;
				
	            _cases.put(Number, new DofusCell(this,Number,Walkable,LineOfSight,obj));	
			}
		}
		
		for(final String mob : monsters.split("\\|"))
		{
			if(mob.equals(""))continue;
			int id = 0;
			int lvl = 0;
			
			try
			{
				id = Integer.parseInt(mob.split(",")[0]);
				lvl = Integer.parseInt(mob.split(",")[1]);
			}catch(final NumberFormatException e){continue;};
			if(id == 0 || lvl == 0)continue;
			if(World.getMonstre(id) == null)continue;
			if(World.getMonstre(id).getGradeByLevel(lvl) == null)continue;
			_mobPossibles.add(World.getMonstre(id).getGradeByLevel(lvl));
		}
		if(_cases.size() == 0)return;
		
		for(final int id : Constants.NO_MOBS_MAP)
		{
			if(id == _id)return;
		}
		
		if (Config.CONFIG_USE_MOBS)
		{
			spawnGroup(Constants.ALIGNEMENT_NEUTRE,_maxGroup,false,-1);//Spawn des groupes d'alignement neutre 
			spawnGroup(Constants.ALIGNEMENT_BONTARIEN,1,false,-1);//Spawn du groupe de gardes bontarien s'il y a
			spawnGroup(Constants.ALIGNEMENT_BRAKMARIEN,1,false,-1);//Spawn du groupe de gardes brakmarien s'il y a
		}
	}
	
	public Map<Integer, ArrayList<Action>> getEndFightActions()
	{
		return _endFightAction;
	}
	public boolean hasEndFightAction(final int type) 
	{
		return (_endFightAction.get(type) == null ? false : true);
	}
	public void applyEndFightAction(final int type,final Player perso)
	{
		System.out.println("Type: "+type+" Perso: "+perso.getName());
		if(_endFightAction.get(type) == null)return;
		for(final Action A : _endFightAction.get(type))A.apply(perso,-1);
	}
	public void addEndFightAction(final int type,final Action A)
	{
		if(_endFightAction.get(type) == null)_endFightAction.put(type, new ArrayList<Action>());
		//On retire l'action si elle existait déjà
		delEndFightAction(type,A.getID());
		_endFightAction.get(type).add(A);
	}
	public void delEndFightAction(final int type,final int aType)
	{
		if(_endFightAction.get(type) == null)return;
		final ArrayList<Action> copy = new ArrayList<Action>();
		copy.addAll(_endFightAction.get(type));
		for(final Action A : copy)if(A.getID() == aType)_endFightAction.get(type).remove(A);
	}
	public void setMountPark(final MountPark mountPark)
	{
		_mountPark = mountPark;
	}
	public MountPark getMountPark()
	{
		return _mountPark;
	}
	public DofusMap(final int id, final String date, final int w, final int h, final String key, final String places)
	{
		_id = id;
		_date = date;
		_w = w;
		_h = h;
		_key = key;
		_placesStr = places;
		_cases = new TreeMap<Integer,DofusCell>();
	}
	
	public SubArea getSubArea()
	{
		return _subArea;
	}
	
	public int getX() {
		return _X;
	}

	public int getY() {
		return _Y;
	}
	
	public Map<Integer, Npc> get_npcs() {
		return _npcs;
	}

	public Npc addNpc(final int npcID,final int cellID, final int dir)
	{
		final NpcTemplate temp = World.getNPCTemplate(npcID);
		if(temp == null)return null;
		if(getCell(cellID) == null)return null;
		final Npc npc = new Npc(temp,_nextObjectID,cellID,(byte)dir);
		_npcs.put(_nextObjectID, npc);
		_nextObjectID--;
		return npc;
	}
	
	public void spawnGroup(final int align, final int nbr,final boolean log,final int cellID)
	{
		if(nbr<1)return;
		if(_mobGroups.size() - _fixMobGroups.size() >= _maxGroup)return;
		for(int a = 1; a<=nbr;a++)
		{
			final MonsterGroup group  = new MonsterGroup(_nextObjectID,align,_mobPossibles,this,cellID,this._maxSize);
			if(group.getMobs().size() == 0)continue;
			_mobGroups.put(_nextObjectID, group);
			if(log)
			{
				Log.addToLog("Groupe de monstres ajoutés sur la map: "+_id+" alignement: "+align+" ID: "+_nextObjectID);
				SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
			}
			_nextObjectID--;
		}
	}
	public void spawnGroup(final boolean timer,final boolean log,final int cellID,final String groupData,final String condition)
	{
		final MonsterGroup group = new MonsterGroup(_nextObjectID, cellID, groupData);
		if(group.getMobs().size() == 0)return;
		_mobGroups.put(_nextObjectID, group);
		group.setCondition(condition);
		group.setIsFix(false);
		
		if(log)
		{
			Log.addToLog("Groupe de monstres ajoutés sur la map: "+_id+" ID: "+_nextObjectID);
		}
		SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
		_nextObjectID--;
		
		if(timer)
			group.startCondTimer();
	}
	
	public void addStaticGroup(final int cellID,final String groupData)
	{
		final MonsterGroup group = new MonsterGroup(_nextObjectID,cellID,groupData);
		if(group.getMobs().size() == 0)return;
		_mobGroups.put(_nextObjectID, group);
		_nextObjectID--;
		_fixMobGroups.put(-1000+_nextObjectID, group);
		SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, group);
	}
	
	public void setPlaces(final String place)
	{
		_placesStr = place;
	}
	public synchronized void removeFight(final int id)
	{
		_fights.remove(id);
	}

	public Npc getNPC(final int id)
	{
		return _npcs.get(id);
	}
	
	public DofusCell getCell(final int id)
	{
		return _cases.get(id);
	}
	
	public Map<Integer, DofusCell> getCases() 
	{
	    return _cases;
    }
	
	public synchronized ArrayList<Player> getPersos()
	{
		final ArrayList<Player> persos = new ArrayList<Player>();
		for(final DofusCell c : _cases.values())
			for(final Player entry : c.getPersos().values())
				persos.add(entry);
		return persos;
	}
	public int getId() {
		return _id;
	}

	public String getDate() {
		return _date;
	}

	public int getW() {
		return _w;
	}

	public int getH() {
		return _h;
	}

	public String getKey() {
		return _key;
	}

	public String getPlacesStr() {
		return _placesStr;
	}

	public void addPlayer(final Player perso)
	{
		perso.getCurCell().addPerso(perso);
		SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(this,perso);
	}

	public String getGMsPackets()
	{
		final StringBuilder packets = new StringBuilder(10 + 30*getPersos().size());

		for(final DofusCell cell : _cases.values())
		{
			for(final Player perso : cell.getPersos().values())
			{
				packets.append("GM|+").append(perso.parseToGM()).append('\u0000');
			}
		}
		return packets.toString();
	}
	public String getFightersGMsPackets()
	{
		final StringBuilder packets = new StringBuilder();
		for(final Entry<Integer,DofusCell> cell : _cases.entrySet())
		{
			for(final Entry<Integer,Fighter> f : cell.getValue().getFighters().entrySet())
			{
				packets.append(f.getValue().getGmPacket('+')).append('\u0000');
			}
		}
		return packets.toString();
	}
	public String getMobGroupGMsPackets()
	{
		if(_mobGroups.size() == 0)return "";
		final StringBuilder packet = new StringBuilder();
		packet.append("GM|");
		boolean isFirst = true;
		for(final MonsterGroup entry : _mobGroups.values())
		{
			final String GM = entry.parseGM();
			if(GM.equals(""))continue;
			
			if(!isFirst)
				packet.append("|");
			
			packet.append(GM);
			isFirst = false;
		}
		return packet.toString();
	}
	
	public String getNpcsGMsPackets()
	{
		if(_npcs.size() == 0)return "";
		final StringBuilder packet = new StringBuilder(3+(25*_npcs.size())).append("GM|");
		boolean isFirst = true;
		for(final Entry<Integer,Npc> entry : _npcs.entrySet())
		{
			final String GM = entry.getValue().parseGM();
			if(GM.equals(""))continue;
			
			if(!isFirst)
				packet.append("|");
			
			packet.append(GM);
			isFirst = false;
		}
		return packet.toString();
	}
	
	public String getObjectsGDsPackets()
	{
		final StringBuilder packets = new StringBuilder(20);
		boolean first = true;
		for(final Entry<Integer,DofusCell> entry : _cases.entrySet())
		{
			if(entry.getValue().getObject() != null)
			{
				if(!first)packets.append((char)0x00);
				first = false;
				final int cellID = entry.getValue().getId();
				final InteractiveObject object = entry.getValue().getObject();
				packets.append("GDF|").append(cellID).append(';').append(object.getState()).append(';').append((object.isInteractive()?"1":"0"));
			}
		}
		return packets.toString();
	}
	
	public int getNbrFight()
	{
		return _fights.size();
	}
	
	public synchronized Map<Integer, Fight> getFights() {
		return _fights;
	}

	public synchronized Fight newFight(final Player init1,final Player init2,final int type)
	{
		int id = 1;
		if(_fights.size() != 0)
			id = ((Integer)(_fights.keySet().toArray()[_fights.size()-1]))+1;
		
		final Fight f = new Fight(type,id,this,init1,init2);
		_fights.put(id,f);
		SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
		return f;
	}
	
	public int getRandomFreeCellID()
	{
		final ArrayList<Integer> freecell = new ArrayList<Integer>();
		for(final Entry<Integer,DofusCell> entry : _cases.entrySet())
		{
			//Si la case n'est pas marchable
			if(!entry.getValue().isWalkable(true))continue;
			if(entry.getValue().isTrigger())continue;
			//Si la case est prise par un groupe de monstre
			boolean ok = true;
			for(final Entry<Integer,MonsterGroup> mgEntry : _mobGroups.entrySet())
			{
				if(mgEntry.getValue().getCellId() == entry.getValue().getId())
					ok = false;
			}
			if(!ok)continue;
			//Si la case est prise par un npc
			ok = true;
			for(final Entry<Integer,Npc> npcEntry : _npcs.entrySet())
			{
				if(npcEntry.getValue().getCellId() == entry.getValue().getId())
					ok = false;
			}
			if(!ok)continue;
			//Si la case est prise par un joueur
			if(entry.getValue().getPersos().size() != 0)continue;
			//Sinon
			freecell.add(entry.getValue().getId());
		}
		if(freecell.size() == 0)
		{
			Log.addToErrorLog("Aucune cellulle libre n'a ete trouve sur la map "+_id+" : groupe non spawn");
			return -1;
		}
		final int rand = Formulas.getRandomValue(0, freecell.size()-1);
		return freecell.get(rand);
		/*
		int max =  _cases.size()-_w;
		int rand = 0;
		int lim = 0;
		boolean isOccuped;
		
		do
		{
			isOccuped = false;
			rand = Formulas.getRandomValue(_w,max);
			if(lim >50)
				return 0;
			for(Entry<Integer,MobGroup> group : _mobGroups.entrySet())
			{
				if (group.getValue().getCellID() != 0)
				{
					if(group.getValue().getCellID() == _cases.get(_cases.keySet().toArray()[rand]).getID())
						isOccuped = true;
				}
			}
			for(Entry<Integer,NPC> npc : _npcs.entrySet())
			{
				if(npc.getValue().get_cellID() == _cases.get(_cases.keySet().toArray()[rand]).getID())
					isOccuped = true;
			}
			
			if (_cases.get(_cases.keySet().toArray()[rand]).isWalkable() && !isOccuped)
			{
				return _cases.get(_cases.keySet().toArray()[rand]).getID();
			}
			
			lim++;
		}while(!_cases.get(_cases.keySet().toArray()[rand]).isWalkable() && !isOccuped);
		
		return 0;
		//*/
	}
	
	public synchronized void refreshSpawns()
	{
		for(final int id : _mobGroups.keySet())
		{
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(this, id);
		}
		_mobGroups.clear();
		_mobGroups.putAll(_fixMobGroups);
		for(final MonsterGroup mg : _fixMobGroups.values())SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, mg);

		spawnGroup(Constants.ALIGNEMENT_NEUTRE,_maxGroup,true,-1);//Spawn des groupes d'alignement neutre 
		spawnGroup(Constants.ALIGNEMENT_BONTARIEN,1,true,-1);//Spawn du groupe de gardes bontarien s'il y a
		spawnGroup(Constants.ALIGNEMENT_BRAKMARIEN,1,true,-1);//Spawn du groupe de gardes brakmarien s'il y a
	}
	
	public synchronized void onPlayerArriveOnCell(final Player perso,final int caseID)
	{
		if(_cases.get(caseID) == null)return;
		final Item obj = _cases.get(caseID).getDroppedItem();
		if(obj != null)
		{
			if(perso.addItem(obj, true))
				World.addItem(obj, true);
			perso.itemLog(obj.getTemplate().getID(), obj.getQuantity(), "Ramassé sur le sol");
			
			SocketManager.GAME_SEND_GDO_PACKET_TO_MAP(this,'-',caseID,0,0);
			SocketManager.GAME_SEND_Ow_PACKET(perso);
			_cases.get(caseID).clearDroppedItem();
		}
		_cases.get(caseID).applyOnCellStopActions(perso);
		
		if(_placesStr.equalsIgnoreCase("|")) return;
		//Si le joueur a changer de map ou ne peut etre aggro
		if(perso.getCurMap().getId() != _id || !perso.canAggro())return;
		
		for(final MonsterGroup group : _mobGroups.values())
		{
			if(Pathfinding.getDistanceBetween(this,caseID,group.getCellId()) <= group.getAggroDistance())//S'il y aggro
			{
				if((group.getAlignement() == -1 
						|| ((perso.getAlign() == 1 || perso.getAlign() == 2) && (perso.getAlign() != group.getAlignement())))
						&& ConditionParser.validConditions(perso, group.getCondition()))
				{
					
					Log.addToLog(perso.getName()+" lance un combat contre le groupe "+group.getActorId()+" sur la map "+_id);
					startFightVersusMonstres(perso,group);
					return;
				}
			}
		}
	}
	
	public void startFightVersusMonstres(final Player perso, final MonsterGroup group)
	{
		int id = 1;
		if(_fights.size() != 0)
			id = ((Integer)(_fights.keySet().toArray()[_fights.size()-1]))+1;
		
		if(!group.isFix())_mobGroups.remove(group.getActorId());
		else SocketManager.GAME_SEND_MAP_MOBS_GMS_PACKETS_TO_MAP(this);
		_fights.put(id, new Fight(id,this,perso,group));
		SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
	}

	public DofusMap getMapCopy()
	{
		final Map<Integer,DofusCell> cases = new TreeMap<Integer,DofusCell>();
		
		final DofusMap map = new DofusMap(_id,_date,_w,_h,_key,_placesStr);
		
		for(final Entry<Integer,DofusCell> entry : _cases.entrySet())
			cases.put(entry.getKey(),
					new DofusCell(
							map,
							entry.getValue().getId(),
							entry.getValue().isWalkable(false),
							entry.getValue().isLoS(),
							(entry.getValue().getObject()==null?-1:entry.getValue().getObject().getID())
							)
						);
		map.setCases(cases);
		return map;
	}

	private void setCases(final Map<Integer, DofusCell> cases)
	{
		_cases = cases;
	}

	public InteractiveObject getMountParkDoor()
	{
		for(final DofusCell c : _cases.values())
		{
			if(c.getObject() == null)continue;
			//Si enclose
			if(c.getObject().getID() == 6763
			|| c.getObject().getID() == 6766
			|| c.getObject().getID() == 6767
			|| c.getObject().getID() == 6772)
				return c.getObject();

		}
		return null;
	}

	public Map<Integer, MonsterGroup> getMobGroups()
	{
		return _mobGroups;
	}

	public void removeNpcOrMobGroup(final int id)
	{
		if(perco != null && perco.getActorId() == id)perco = null;
		_npcs.remove(id);
		_mobGroups.remove(id);
	}

	public int getMaxGroupNumb()
	{
		return _maxGroup;
	}

	public void setMaxGroup(final int id)
	{
		_maxGroup = id;
	}

	public synchronized Fight getFight(final int id)
	{
		return _fights.get(id);
	}

	public void sendFloorItems(final Player perso)
	{
		for(final DofusCell c : _cases.values())
		{
			if(c.getDroppedItem() != null)
			SocketManager.GAME_SEND_GDO_PACKET(perso,'+',c.getId(),c.getDroppedItem().getTemplate().getID(),0);
		}
	}

	public void setPercepteur(final TaxCollector perco) {
		this.perco = perco;
	}

	public TaxCollector getPercepteur() {
		return perco;
	}

	public void startFightVersusPercepteur(final Player perso, final TaxCollector perco) {
		int id = 1;
		if(!_fights.isEmpty())
			id = ((Integer)(_fights.keySet().toArray()[_fights.size()-1]))+1;

		_fights.put(id, new Fight(id,this,perso,perco));
		SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(this);
    }

	public int getStoreCount()
	{
		return (World.getSeller(getId()) == null?0:World.getSeller(getId()).size());
	}

	public synchronized void addMobGroup(final MonsterGroup mobGroup) {
		_mobGroups.put(_mobGroups.size()+1, mobGroup);
		SocketManager.GAME_SEND_MAP_MOBS_GM_PACKET(this, mobGroup);
	}

	public Prism getPrism() {
		return prism;
	}

	public void setPrism(final Prism prism) {
		this.prism = prism;
	}	
	
	public int getLastCellId() {
		int cell = (_w * _h * 2) - (_h + _w);
		return cell;
	}
	
	public boolean isCellLeftSide(int cell) {
		int leftSide = _w;
		for (int i = 0; i < _h; i++) {
			if (cell == leftSide)
				return true;
			leftSide = leftSide + (_w * 2) - 1;
		}
		return false;
	}
	
	public boolean isCellRightSide(int cell) {
		int rightSide = 2 * (_w - 1);
		for (int i = 0; i < _h; i++) {
			if (cell == rightSide)
				return true;
			rightSide = rightSide + (_w * 2) - 1;
		}
		return false;
	}
	
	public boolean isCellOutOfLateral(int cell1, int cell2) {
		if (isCellLeftSide(cell1))
			if (cell2 == cell1 + (_w - 1) || cell2 == cell1 - _w)
				return true;
		if (isCellRightSide(cell1))
			if (cell2 == cell1 + _w || cell2 == cell1 - (_w - 1))
				return true;
		return false;
	} 
}

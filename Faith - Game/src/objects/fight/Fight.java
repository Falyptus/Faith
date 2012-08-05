package objects.fight;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Timer;

import objects.action.GameAction;
import objects.character.Party;
import objects.character.Player;
import objects.guild.Guild;
import objects.guild.TaxCollector;
import objects.item.Item;
import objects.item.ItemTemplate;
import objects.item.SoulStone;
import objects.map.DofusCell;
import objects.map.DofusMap;
import objects.monster.MonsterGrade;
import objects.monster.MonsterGroup;
import objects.spell.SpellEffect;
import objects.spell.SpellStat;

import common.Config;
import common.Constants;
import common.IA;
import common.SQLManager;
import common.SocketManager;
import common.World;
import common.World.Couple;
import common.World.Drop;
import common.console.Log;
import common.utils.CryptManager;
import common.utils.Formulas;
import common.utils.Pathfinding;

public class Fight
{
	private int _id;
	private Map<Integer,Fighter> _team0 = new TreeMap<Integer,Fighter>();
	private Map<Integer,Fighter> _team1 = new TreeMap<Integer,Fighter>();
	private final Map<Integer,Player> _spec  = new TreeMap<Integer,Player>();
	private DofusMap _map;
	private Fighter _init0;
	private Fighter _init1;
	private ArrayList<DofusCell> _start0 = new ArrayList<DofusCell>();
	private ArrayList<DofusCell> _start1 = new ArrayList<DofusCell>();
	private int _state =0;
	private int _type = -1;
	private boolean locked0 = false;
	private boolean onlyGroup0 = false;
	private boolean locked1 = false;
	private boolean onlyGroup1 = false;
	private boolean specOk = true;
	private boolean help1 = false;
	private boolean help2 = false;
	private int _st2;
	private int _st1;
	private int _curPlayer;
	private long _startTime = 0;
	private long _startTimeTurn = 0;
	private int _curFighterPA;
	private int _curFighterPM;
	private int _curFighterUsedPA;
	private int _curFighterUsedPM;
	private String _curAction = "";
	private List<Fighter> _ordreJeu = new ArrayList<Fighter>();
	private Timer _turnTimer;
	private final List<Glyph> _glyphs = new ArrayList<Glyph>();
	private final List<Trap> _traps = new ArrayList<Trap>();
	private MonsterGroup _mobGroup;
	private TaxCollector _taxCollector;
	
	private final ArrayList<Fighter> _captureur = new ArrayList<Fighter>(8);	//Création d'une liste de longueur 8. Les combats contiennent un max de 8 Attaquant
	private boolean isCapturable = false;
	private int captWinner = 0;
	private SoulStone pierrePleine;
	private final Map<Integer, Fighter> _deadList = new TreeMap<Integer, Fighter>();
	private final Map<Integer, Integer> _initialPosition = new TreeMap<Integer, Integer>();
	
	public Fight(final int type, final int id,final DofusMap map, final Player init1, final Player init2)
	{
		_type = type; //1: Défie (2: Pvm) 3:PVP
		_id = id;
		setMap(map.getMapCopy());
		setInit0(new Fighter(this,init1));
		setInit1(new Fighter(this,init2));
		getTeam0().put(init1.getActorId(), getInit0());
		getTeam1().put(init2.getActorId(), getInit1());
		SocketManager.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,7,2,_type==Constants.FIGHT_TYPE_CHALLENGE?1:0,1,0,_type==Constants.FIGHT_TYPE_CHALLENGE?0:Config.CONFIG_BEGIN_TIME,_type);
		//on desactive le timer de regen coté client
		SocketManager.GAME_SEND_ILF_PACKET(init1, 0);
		SocketManager.GAME_SEND_ILF_PACKET(init2, 0);
		
		_startTimeTurn = System.currentTimeMillis();
		
		if(_type!=Constants.FIGHT_TYPE_CHALLENGE)
		{
			_turnTimer = new Timer(Config.CONFIG_BEGIN_TIME,new ActionListener()
			{
				public void actionPerformed(final ActionEvent e)
				{
					startFight();
					_turnTimer.stop();
					return;
				}
			});
			_turnTimer.start();
		}
		final Random teams = new Random();
		if(teams.nextBoolean())
		{
			_start0 = parsePlaces(0);
			_start1 = parsePlaces(1);
			SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlacesStr(),0);
			SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,2,getMap().getPlacesStr(),1);
			_st1 = 0;
			_st2 = 1;
		}else
		{
			_start0 = parsePlaces(1);
			_start1 = parsePlaces(0);
			_st1 = 1;
			_st2 = 0;
			SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlacesStr(),1);
			SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,2,getMap().getPlacesStr(),0);
		}
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init1.getActorId()+"", init1.getActorId()+","+Constants.ETAT_PORTE+",0");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init1.getActorId()+"", init1.getActorId()+","+Constants.ETAT_PORTEUR+",0");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init2.getActorId()+"", init2.getActorId()+","+Constants.ETAT_PORTE+",0");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init2.getActorId()+"", init2.getActorId()+","+Constants.ETAT_PORTEUR+",0");
		
		getInit0().setFightCell(getRandomCell(_start0));
		getInit1().setFightCell(getRandomCell(_start1));
		
		getInit0().getPlayer().getCurCell().removePlayer(getInit0().getGUID());
		getInit1().getPlayer().getCurCell().removePlayer(getInit1().getGUID());
		
		getInit0().getFightCell().addFighter(getInit0());
		getInit1().getFightCell().addFighter(getInit1());
		getInit0().getPlayer().setFight(this);
		getInit0().setTeam(0);
		getInit1().getPlayer().setFight(this);
		getInit1().setTeam(1);
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getGUID());
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit1().getPlayer().getCurMap(), getInit1().getGUID());
		
		SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(),0,getInit0().getGUID(),getInit1().getGUID(),
				getInit0().getPlayer().getCurCell().getId(), _type == Constants.FIGHT_TYPE_CHALLENGE ? "0;-1" : "0;"+getInit0().getPlayer().getAlign(), 
				getInit1().getPlayer().getCurCell().getId(), _type == Constants.FIGHT_TYPE_CHALLENGE ? "0;-1" : "0;"+getInit1().getPlayer().getAlign()
		);
		SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(),getInit0().getGUID(), getInit0());
		SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(),getInit1().getGUID(), getInit1());
		
		SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,getMap());
		
		setState(Constants.FIGHT_STATE_PLACE);
	}
	
	public void waitKnights(final Player agressor, final Player defensor) 
	{
		SocketManager.GAME_SEND_Im_PACKET(agressor, "084;1");
		if(defensor.getCurMap().getSubArea().getAlignement() < 1)
		{
			new Thread(new Runnable() {
				@Override
				public void run() {
					final Fight curFight = defensor.getFight();
					int countKnight = 0;
					while(curFight.getState() == Constants.FIGHT_STATE_PLACE)
					{
						final ArrayList<Fighter> teamEnnemy = curFight.getFighters(curFight.getTeamID(agressor.getActorId()));
						final int nbEnnemies = teamEnnemy.size();
						while(countKnight < nbEnnemies && countKnight < 7)
						{
							if(curFight.GetNumbCellEmptys(curFight.getTeamID(defensor.getActorId())) > 0)
							{
								final Fighter knight = new Fighter(defensor.getFight(), World.getMonstre(394).getGrades().get(Formulas.getKnightGradeByLvlEnnemy(teamEnnemy)));
								defensor.getFight().joinFight(knight, defensor.getActorId(), defensor.getCurMap());
								countKnight++;
							}
						}
					}
				}
			}).start();
		}
	}

	public Fight(final int id,final DofusMap map,final Player init1, final MonsterGroup group)
	{
		setMobGroup(group);
		_type = Constants.FIGHT_TYPE_PVM; //(1: Défie) 2: Pvm (3:PVP)
		_id = id;
		setMap(map.getMapCopy());
		setInit0(new Fighter(this,init1));
		
		getTeam0().put(init1.getActorId(), getInit0());
		for(final Entry<Integer, MonsterGrade> entry : group.getMobs().entrySet())
		{
			entry.getValue().setInFightID(entry.getKey());
			final Fighter mob = new Fighter(this,entry.getValue());
			getTeam1().put(entry.getKey(), mob);
		}
		
		SocketManager.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,1,2,0,1,0,Config.CONFIG_BEGIN_TIME,_type);
		
		//on desactive le timer de regen coté client
		SocketManager.GAME_SEND_ILF_PACKET(init1, 0);
		
		_startTimeTurn = System.currentTimeMillis();
		
		_turnTimer = new Timer(Config.CONFIG_BEGIN_TIME,new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				startFight();
				_turnTimer.stop();
				return;
			}
		});
		_turnTimer.start();
		final Random teams = new Random();
		if(teams.nextBoolean())
		{
			_start0 = parsePlaces(0);
			_start1 = parsePlaces(1);
			SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlacesStr(),0);
			_st1 = 0;
			_st2 = 1;
		}else
		{
			_start0 = parsePlaces(1);
			_start1 = parsePlaces(0);
			_st1 = 1;
			_st2 = 0;
			SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlacesStr(),1);
		}
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init1.getActorId()+"", init1.getActorId()+","+Constants.ETAT_PORTE+",0");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, init1.getActorId()+"", init1.getActorId()+","+Constants.ETAT_PORTEUR+",0");
		
		final List<Entry<Integer, Fighter>> e = new ArrayList<Entry<Integer,Fighter>>();
		e.addAll(getTeam1().entrySet());
		for(final Entry<Integer,Fighter> entry : e)
		{
			final Fighter f = entry.getValue();
			final DofusCell cell = getRandomCell(_start1);
			if(cell == null)
			{
				getTeam1().remove(f.getGUID());
				continue;
			}
			
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, f.getGUID()+"", f.getGUID()+","+Constants.ETAT_PORTE+",0");
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, f.getGUID()+"", f.getGUID()+","+Constants.ETAT_PORTEUR+",0");
			f.setFightCell(cell);
			f.getFightCell().addFighter(f);
			f.setTeam(1);
			f.fullPDV();
		}
		getInit0().setFightCell(getRandomCell(_start0));
		
		getInit0().getPlayer().getCurCell().removePlayer(getInit0().getPlayer().getActorId());
		
		getInit0().getFightCell().addFighter(getInit0());
		
		getInit0().getPlayer().setFight(this);
		getInit0().setTeam(0);
		
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getGUID());
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), group.getActorId());
		
		SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(),4,getInit0().getGUID(),group.getActorId(),(getInit0().getPlayer().getCurCell().getId()+1),"0;-1",group.getCellId(),"1;-1");
		SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(),getInit0().getGUID(), getInit0());
		
		for(final Fighter f : getTeam1().values())
		{
			SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(),group.getActorId(), f);
		}
		
		SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,getMap());
		
		setState(2);
	}

	public Fight(final int id, final DofusMap map, final Player perso, final TaxCollector perco) {
		_type = Constants.FIGHT_TYPE_PVT; //1: Défie (2: Pvm) 3:PVP
		_id = id;
		setMap(map.getMapCopy());
		setInit0(new Fighter(this,perso));
		setInit1(new Fighter(this,perco));
		setTaxCollector(perco);
		getTeam0().put(perso.getActorId(), getInit0());
		getTeam1().put(-1, getInit1());
		SocketManager.GAME_SEND_FIGHT_GJK_PACKET_TO_FIGHT(this,7,2,0,1,0,Config.CONFIG_BEGIN_TIME,_type);
		//on desactive le timer de regen coté client
		SocketManager.GAME_SEND_ILF_PACKET(perso, 0);
		
		_startTimeTurn = System.currentTimeMillis();
		
		_turnTimer = new Timer(1000,new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				if(_taxCollector.getTimer() != 0)
				{
					_taxCollector.decrTimer();
				}else
				{
					_taxCollector.setFullTimer();
					startFight();
					_turnTimer.stop();
				}
			}
		});
		_turnTimer.start();
		
		final Random teams = new Random();
		if(teams.nextBoolean())
		{
			_start0 = parsePlaces(0);
			_start1 = parsePlaces(1);
			SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlacesStr(),0);
			_st1 = 0;
			_st2 = 1;
		}else
		{
			_start0 = parsePlaces(1);
			_start1 = parsePlaces(0);
			_st1 = 1;
			_st2 = 0;
			SocketManager.GAME_SEND_FIGHT_PLACES_PACKET_TO_FIGHT(this,1,getMap().getPlacesStr(),1);
		}
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getActorId()+"", perso.getActorId()+","+Constants.ETAT_PORTE+",0");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getActorId()+"", perso.getActorId()+","+Constants.ETAT_PORTEUR+",0");

		getInit0().setFightCell(getRandomCell(_start0));
		getInit1().setFightCell(getRandomCell(_start1));
		
		getInit0().getPlayer().getCurCell().removePlayer(getInit0().getGUID());
		
		getInit0().getFightCell().addFighter(getInit0());
		getInit1().getFightCell().addFighter(getInit1());
		getInit0().getPlayer().setFight(this);
		getInit0().setTeam(0);
		getInit1().getTaxCollector().setFight(this);
		getInit1().setTeam(1);
	
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getInit0().getPlayer().getCurMap(), getInit0().getGUID());
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(World.getMap(getInit1().getTaxCollector().getMapId()), getInit1().getGUID());
		
		SocketManager.GAME_SEND_GAME_ADDFLAG_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(),5,getInit0().getGUID(),getInit1().getGUID(),getInit0().getPlayer().getCurCell().getId(),"0;-1", getInit1().getTaxCollector().getCellId(), "3;-1");
		SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(),getInit0().getGUID(), getInit0());
		SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(),getInit1().getGUID(), getInit1());
		
		SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS_TO_FIGHT(this,7,getMap());
		
		setState(Constants.FIGHT_STATE_PLACE);
    }

	public DofusMap getMap() {
		return _map;
	}

	public List<Trap> getTraps() {
		return _traps;
	}

	public List<Glyph> getGlyphs() {
		return _glyphs;
	}

	private DofusCell getRandomCell(final List<DofusCell> cells)
	{
		final Random rand = new Random();
		DofusCell cell;
		if(cells.size() == 0)return null;
		int limit = 0;
		do
		{
			final int id = rand.nextInt(cells.size()-1);
			cell = cells.get(id);
			limit++;
		}while((cell == null || cell.getFighters().size() != 0) && limit < 80);
		if(limit == 80)
		{
			Log.addToErrorLog("Case non trouvé dans la liste");
			return null;
		}
		return cell;		
	}
	
	public int GetNumbCellEmptys(final int teamId)
	{
		int toReturn = 0;
		if(teamId == 0)
		{
			for(final DofusCell cell : _start0)
			{
				if(cell.getFighters().isEmpty() && cell.getPersos().isEmpty())
					toReturn++;
			}
		}
		else
		{
			for(final DofusCell cell : _start0)
			{
				if(cell.getFighters().isEmpty() && cell.getPersos().isEmpty())
					toReturn++;
			}
		}
		return toReturn;
	}
	
	private ArrayList<DofusCell> parsePlaces(final int num)
	{
		return CryptManager.parseStartCell(getMap(), num);
	}
	
	public int getId() {
		return _id;
	}

	public ArrayList<Fighter> getFighters(int teams)//teams entre 0 et 7, binaire([spec][t2][t1]);
	{
		final ArrayList<Fighter> fighters = new ArrayList<Fighter>();
		
		if(teams - 4 >= 0)
		{
			for(final Entry<Integer,Player> entry : _spec.entrySet())
			{
				fighters.add(new Fighter(this,entry.getValue()));
			}
			teams -= 4;
		}
		if(teams -2 >= 0)
		{
			for(final Entry<Integer,Fighter> entry : getTeam1().entrySet())
			{
				fighters.add(entry.getValue());
			}
			teams -= 2;
		}
		if(teams -1 >=0)
		{	
			for(final Entry<Integer,Fighter> entry : getTeam0().entrySet())
			{
				fighters.add(entry.getValue());
			}
		}
		return fighters;
	}
	
	public synchronized void changePlace(final Player perso,final int cell)
	{
		final Fighter fighter = getFighterByPerso(perso);
		final int team = getTeamID(perso.getActorId()) -1;
		if(fighter == null)return;
		if(getState() != 2 || isOccuped(cell) || perso.isReady() || (team == 0 && !groupCellContains(_start0,cell)) || (team == 1 && !groupCellContains(_start1,cell)))return;

		fighter.getFightCell().getFighters().clear();
		fighter.setFightCell(getMap().getCell(cell));
		
		getMap().getCell(cell).addFighter(fighter);
		SocketManager.GAME_SEND_FIGHT_CHANGE_PLACE_PACKET_TO_FIGHT(this,3,getMap(),perso.getActorId(),cell);
	}

	public boolean isOccuped(final int cell)
	{
		return getMap().getCell(cell).getFighters().size() > 0;
	}

	private boolean groupCellContains(final ArrayList<DofusCell> cells, final int cell)
	{
		for(int a = 0; a<cells.size();a++)
		{
			if(cells.get(a).getId() == cell)
				return true;
		}
		return false;
	}

	public void verifIfAllReady()
	{
		boolean val = true;
		for(int a=0;a<getTeam0().size();a++)
		{
			if(!getTeam0().get(getTeam0().keySet().toArray()[a]).getPlayer().isReady())
				val = false;
		}
		if(_type != Constants.FIGHT_TYPE_PVM)
		{
			for(int a=0;a<getTeam1().size();a++)
			{
				if(getTeam1().get(getTeam1().keySet().toArray()[a]).getPlayer() != null && !getTeam1().get(getTeam1().keySet().toArray()[a]).getPlayer().isReady())
					val = false;
				else if(getTeam1().get(getTeam1().keySet().toArray()[a]).isMob())
					continue;
			}
		}
		if(val)
		{
			startFight();
		}
	}

	private synchronized void startFight()
	{
		if(_state >= Constants.FIGHT_STATE_ACTIVE)return;
		_state = Constants.FIGHT_STATE_ACTIVE;
		_startTime = System.currentTimeMillis();
		_startTimeTurn = 0;
		SocketManager.GAME_SEND_GAME_REMFLAG_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(),getInit0().getGUID());
		if(_type == Constants.FIGHT_TYPE_PVM)
		{
			final int align = -1;
			if(getTeam1().size() >0)
			{
				 getTeam1().get(getTeam1().keySet().toArray()[0]).getMob().getTemplate().getAlign();
			}
			//Si groupe non fixe
			if(!getMobGroup().isFix())World.getMap(getMap().getId()).spawnGroup(align, 1, true,getMobGroup().getCellId());//Respawn d'un groupe
		}
		SocketManager.GAME_SEND_GIC_PACKETS_TO_FIGHT(this, 7);
		SocketManager.GAME_SEND_GS_PACKET_TO_FIGHT(this, 7);
		InitOrdreJeu();
		_curPlayer = -1;
		SocketManager.GAME_SEND_GTL_PACKET_TO_FIGHT(this,7);
		SocketManager.GAME_SEND_GTM_PACKET_TO_FIGHT(this, 7);
		if(_turnTimer  != null)_turnTimer.stop();
		_turnTimer = null;
		_turnTimer = new Timer(Config.TIME_BY_TURN,new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				endTurn();
			}
		});
		Log.addToLog("Début du combat");
		for(final Fighter F : getFighters(3))
		{
			final Player perso = F.getPlayer();
			if(perso == null)continue;
			if(perso.isOnMount())
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getActorId()+"", perso.getActorId()+","+Constants.ETAT_CHEVAUCHANT+",1");
			
		}
		for(final Fighter f : _ordreJeu)
		{
			if(_type == Constants.FIGHT_TYPE_CHALLENGE)
			{
				f.setPdvBeforeFight(f.getLife());
			}
			_initialPosition.put(f.getGUID(), f.getFightCell().getId());
		}
		//TODO : Process Challenge
		try
		{
			Thread.sleep(200);
		}catch(final Exception e){};
		startTurn();
	}

	private synchronized void startTurn()
	{
		if(!verifyStillInFight())
			verifIfTeamAllDead();
		
		if(_state >= Constants.FIGHT_STATE_FINISHED)return;
		
		try {
			Thread.sleep(100);
		} catch (final InterruptedException e1) {e1.printStackTrace();}
		
		_curPlayer++;
		_curAction = "";
		if(_curPlayer >= _ordreJeu.size())_curPlayer = 0;
		if(_ordreJeu.get(_curPlayer) == null)
		{
			endTurn();
			return;
		}
		final Fighter curFighter = _ordreJeu.get(_curPlayer);
		_curFighterPA = curFighter.getPA();
		_curFighterPM = curFighter.getPM();
		_curFighterUsedPA = 0;
		_curFighterUsedPM = 0;
		curFighter.actualizeCooldown();
		if(!curFighter.isDead())
		{
			curFighter.applyBeginningTurnBuff(this);
			if(_state == Constants.FIGHT_STATE_FINISHED)return;
			if(curFighter.getLife()<=0)onFighterDie(curFighter);
		}
		
		if(curFighter.isDead())//Si joueur mort
		{
			Log.addToLog("("+_curPlayer+") Fighter ID=  "+curFighter.getGUID()+" est mort");
			endTurn();
			return;
		}
		//reset des Max des Chatis
		curFighter.getChatiValue().clear();
		//Gestion des glyphes
		final ArrayList<Glyph> glyphs = new ArrayList<Glyph>();//Copie du tableau
		glyphs.addAll(_glyphs);
		for(final Glyph g : glyphs)
		{
			if(_state >= Constants.FIGHT_STATE_FINISHED)return;
			//Si c'est ce joueur qui l'a lancé
			if(g.getCaster().getGUID() == curFighter.getGUID())
			{
				//on réduit la durée restante, et si 0, on supprime
				if(g.decrementDuration() == 0)
				{
					_glyphs.remove(g);
					g.desapear();
					continue;//Continue pour pas que le joueur active le glyphe s'il était dessus
				}
			}
			//Si dans le glyphe
			final int dist = Pathfinding.getDistanceBetween(getMap(),curFighter.getFightCell().getId() , g.getCell().getId());
			if(dist <= g.getSize())
			{
				//Alors le joueur est dans le glyphe
				g.onGlyph(curFighter);
			}
		}
		if(_ordreJeu == null)return;
		if(_ordreJeu.size() < _curPlayer)return;
		if(curFighter.isDead())//Si joueur mort
		{
			Log.addToLog("("+_curPlayer+") Fighter ID=  "+curFighter.getGUID()+" est mort");
			endTurn();
			return;
		}
		
		if(curFighter.getPlayer() != null) {
			curFighter.getPlayer().clearCacheAS();
			SocketManager.GAME_SEND_STATS_PACKET(curFighter.getPlayer());
		}
		
		if(curFighter.hasBuff(Constants.EFFECT_PASS_TURN))//Si il doit passer son tour
		{
			Log.addToLog("("+_curPlayer+") Fighter ID= "+curFighter.getGUID()+" passe son tour");
			endTurn();
			return;
		}
		Log.addToLog("("+_curPlayer+")Début du tour de Fighter ID= "+curFighter.getGUID());
		if(curFighter.isDisconnected())
		{
			if(curFighter.getTeam() == 0) 
			{
				if(getTeam0().size() >1)
					endTurn();
			} else
			{
				if(getTeam1().size() >1)
					endTurn();
			}
		}
		_startTimeTurn = System.currentTimeMillis();
		curFighter.setCanPlay(true);
		SocketManager.GAME_SEND_GAMETURNSTART_PACKET_TO_FIGHT(this,7,curFighter.getGUID(),Config.TIME_BY_TURN);
		_turnTimer.restart();
		
		if(curFighter.getPlayer() == null)//Si ce n'est pas un joueur
		{
			IA.launchIA(curFighter, this);	
			endTurn();
		}
	}

	public synchronized void endTurn()
	{
		try {
			Thread.sleep(500); //On patiente un minimum sinon il va y avoir des bugs d'affichages
		} catch (InterruptedException e) {}
		if(_state >= Constants.FIGHT_STATE_FINISHED)return;
		final Fighter curFighter = _ordreJeu.get(_curPlayer);		
		if(_curPlayer == -1)return;
		try
		{
			_turnTimer.stop();
			SocketManager.GAME_SEND_GAMETURNSTOP_PACKET_TO_FIGHT(this,7,curFighter.getGUID());
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {}
			if(_ordreJeu == null || curFighter == null)return;
			if(!curFighter.hasLeft())
			{
				curFighter.setCanPlay(false);
				_curAction = "";
				if(!curFighter.isDead())
				{
					//Si empoisonné (Créer une fonction applyEndTurnbuff si d'autres effets existent)
					for(final SpellEffect SE : curFighter.getBuffsByEffectID(131))
					{
						final int pas = SE.getValue();
						int val = -1;
						try
						{
							val = Integer.parseInt(SE.getArgs().split(";")[1]);
						}catch(final Exception e){};
						if(val == -1)continue;
						
						final int nbr = (int) Math.floor((double)_curFighterUsedPA/(double)pas);
						int dgt = val * nbr;
	
						//Si poison paralysant
						int inte = 0;
						int pdom = SE.getCaster().getTotalStats().getEffect(Constants.STATS_ADD_PERDOM);
						if(pdom < 0)
							pdom = 0;
						int dom = SE.getCaster().getTotalStats().getEffect(Constants.STATS_ADD_DOMA);
						if(SE.getSpell() == 200)
						{
							inte = SE.getCaster().getTotalStats().getEffect(Constants.STATS_ADD_INTE);
							if(inte < 0)
								inte = 0;
							dom = 0;
						}
						//on applique le boost
						dgt = (int)(((100+inte+pdom)/100) * dgt)+dom;
						if(dgt == 0)continue;
						
						if(dgt>curFighter.getLife())dgt = curFighter.getLife();//va mourrir
						curFighter.removeLife(dgt);
						dgt = -(dgt);
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 100, SE.getCaster().getGUID()+"", curFighter.getGUID()+","+dgt);
						
					}
					ArrayList<Glyph> glyphs = new ArrayList<Glyph>();
					glyphs.addAll(_glyphs);
					for(Glyph glyph : glyphs) {
						if(glyph.getSpellId() == 476) {
							final int dist = Pathfinding.getDistanceBetween(getMap(),glyph.getCell().getId(),curFighter.getFightCell().getId());
							if(dist <= glyph.getSize()) {
								glyph.onGlyph(curFighter);
							}
						}
					}
					if(curFighter.getLife() <= 0)
						onFighterDie(curFighter);
				}
				//reset des valeurs
				_curFighterUsedPA = 0;
				_curFighterUsedPM = 0;
				_curFighterPA = curFighter.getTotalStats().getEffect(Constants.STATS_ADD_PA);
				_curFighterPM = curFighter.getTotalStats().getEffect(Constants.STATS_ADD_PM);
				curFighter.refreshFightBuff();
				
				if(curFighter.getPlayer() != null) {
					if(curFighter.getPlayer().isOnline()){
						curFighter.getPlayer().clearCacheAS();
						SocketManager.GAME_SEND_STATS_PACKET(curFighter.getPlayer());
					}
				}
			}
			SocketManager.GAME_SEND_GTM_PACKET_TO_FIGHT(this, 7);
			SocketManager.GAME_SEND_GTR_PACKET_TO_FIGHT(this, 7, _ordreJeu.get(_curPlayer==_ordreJeu.size()?0:_curPlayer).getGUID());
			if(curFighter.isDisconnected())
			{
				curFighter.decrementRemainingTurns();
				if(curFighter.getRemainingTurns() == 0)
				{
					leftFight(curFighter.getPlayer(), null);
					verifIfTeamAllDead();
				} else
				{
					SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this, 7, "0162;"+curFighter.getPlayer().getName()+"~"+curFighter.getRemainingTurns());
				}
			}
			Log.addToLog("("+_curPlayer+")Fin du tour de Fighter ID= "+curFighter.getGUID());
			startTurn();
		}catch(final NullPointerException e)
		{
			e.printStackTrace();
			endTurn();
		}
	}

	private void InitOrdreJeu()
	{
		int curMaxIni = 0;
		Fighter curMax = null;
		boolean team0_ready = false;
		boolean team1_ready = false;
		byte actTeam = -1;
		do
		{
			if((actTeam == -1 || actTeam == 0 || team1_ready) && !team0_ready)
			{
				team0_ready = true;
				for(final Entry<Integer,Fighter> entry : getTeam0().entrySet())
				{
					if(_ordreJeu.contains(entry.getValue()))
						continue;
					team0_ready = false;
					if(entry.getValue().getInitiative() >= curMaxIni)
					{
						curMaxIni = entry.getValue().getInitiative();
						curMax = entry.getValue();
					}
				}
			}       

			if((actTeam == -1 || actTeam == 1 || team0_ready) && !team1_ready)
			{
				team1_ready = true;
				for(final Entry<Integer,Fighter> entry : getTeam1().entrySet())
				{
					if(_ordreJeu.contains(entry.getValue()))
						continue;
					team1_ready = false;
					if(entry.getValue().getInitiative() >= curMaxIni)
					{
						curMaxIni = entry.getValue().getInitiative();
						curMax = entry.getValue();
					}
				}
			}

			// si il y a bien un joueur a ajouter
			if(curMax != null)
				_ordreJeu.add(curMax);

			// inversement des équipes
			if(actTeam == 0)
				actTeam = 1;
			else if (actTeam == 1)
				actTeam = 0;
			else 
			{
				if(curMax.getTeam() == 1)
					actTeam = 0;
				else
					actTeam = 1;
			}

			// on ré-initialise les variables pour les ré-utiliser
			curMaxIni = 0;
			curMax = null;

		} while(_ordreJeu.size() != getFighters(3).size());
	}

	public synchronized void joinFight(final Player perso, final int guid)
	{	
		Fighter current_Join = null;
		if(getTeam0().containsKey(guid))
		{
			final DofusCell cell = getRandomCell(_start0);
			if(cell == null)return;
			
			if(locked0)
			{
				SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getAccount().getGameThread().getOut(),'f',guid);
				return;
			}
			
			if(onlyGroup0)
			{
				final Party g = getInit0().getPlayer().getParty();
				if(g != null)
				{
					if(!g.getMembers().contains(perso))
					{
						SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getAccount().getGameThread().getOut(),'f',guid);
						return;
					}
				}
			}
			if(_type == Constants.FIGHT_TYPE_AGRESSION)
			{
				if(perso.getAlign() != getInit0().getPlayer().getAlign())
				{
					SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getAccount().getGameThread().getOut(),'f',guid);
					return;
				}
			}
			
			//Désactive le timer de regen
			SocketManager.GAME_SEND_ILF_PACKET(perso, 0);
			
			SocketManager.GAME_SEND_GJK_PACKET(perso, 2, _type == Constants.FIGHT_TYPE_CHALLENGE ? 1 : 0, 1, 0, 0, _type);
			SocketManager.GAME_SEND_FIGHT_PLACES_PACKET(perso.getAccount().getGameThread().getOut(), getMap().getPlacesStr(), _st1);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getActorId()+"", perso.getActorId()+","+Constants.ETAT_PORTE+",0");
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getActorId()+"", perso.getActorId()+","+Constants.ETAT_PORTEUR+",0");
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getActorId());
			
			final Fighter f = new Fighter(this, perso);
			current_Join = f;
			f.setTeam(0);
			getTeam0().put(perso.getActorId(), f);
			perso.setFight(this);
			f.setFightCell(cell);
			f.getFightCell().addFighter(f);
		}else if(getTeam1().containsKey(guid))
		{
			final DofusCell cell = getRandomCell(_start1);
			if(cell == null)return;
			
			if(locked1)
			{
				SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getAccount().getGameThread().getOut(),'f',guid);
				return;
			}
			if(onlyGroup1)
			{
				final Party g = getInit1().getPlayer().getParty();
				if(g != null)
				{
					if(!g.getMembers().contains(perso))
					{
						SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getAccount().getGameThread().getOut(),'f',guid);
						return;
					}
				}
			}
			if(_type == Constants.FIGHT_TYPE_AGRESSION)
			{
				if(perso.getAlign() != getInit1().getPlayer().getAlign())
				{
					SocketManager.GAME_SEND_GA903_ERROR_PACKET(perso.getAccount().getGameThread().getOut(),'f',guid);
					return;
				}
			}
			
			//Désactive le timer de regen
			SocketManager.GAME_SEND_ILF_PACKET(perso, 0);
			
			SocketManager.GAME_SEND_GJK_PACKET(perso, 2, _type == Constants.FIGHT_TYPE_CHALLENGE ? 1 : 0, 1, 0, 0, _type);
			SocketManager.GAME_SEND_FIGHT_PLACES_PACKET(perso.getAccount().getGameThread().getOut(), getMap().getPlacesStr(), _st2);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getActorId()+"", perso.getActorId()+","+Constants.ETAT_PORTE+",0");
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getActorId()+"", perso.getActorId()+","+Constants.ETAT_PORTEUR+",0");
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getActorId());
			final Fighter f = new Fighter(this, perso);
			current_Join = f;
			f.setTeam(1);
			getTeam1().put(perso.getActorId(), f);
			perso.setFight(this);
			f.setFightCell(cell);
			f.getFightCell().addFighter(f);
		}
		if(_taxCollector != null)
			_taxCollector.addAttacker(perso);
		perso.getCurCell().removePlayer(perso.getActorId());
		SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(perso.getCurMap(),(current_Join.getTeam()==0?getInit0():getInit1()).getGUID(), current_Join);
		SocketManager.GAME_SEND_FIGHT_PLAYER_JOIN(this,7,current_Join);
		SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,getMap(),perso);
	}
	
	public synchronized void joinFight(final Fighter f, final int guid, final DofusMap c)
	{	
		if(_startTime != 0)return;
		if(getTeam0().containsKey(guid))
		{
			final DofusCell cell = getRandomCell(_start0);
			if(cell == null)return;			
			f.setTeam(0);
			getTeam0().put(f.getGUID(), f);
			f.setFightCell(cell);
			f.getFightCell().addFighter(f);
		}else if(getTeam1().containsKey(guid))
		{
			final DofusCell cell = getRandomCell(_start1);
			if(cell == null)return;
			f.setTeam(1);
			getTeam1().put(f.getGUID(), f);
			f.setFightCell(cell);
			f.getFightCell().addFighter(f);
		}
		SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(c,(f.getTeam()==0?getInit0():getInit1()).getGUID(), f);
		SocketManager.GAME_SEND_FIGHT_PLAYER_JOIN(this,7,f);
	}	
	
	public synchronized void joinPercepteurFight(final Player perso, final int percoID)
	{	
		if(_startTime != 0)return;
		Fighter current_Join = null;
		final DofusCell cell = getRandomCell(_start1);
		if(cell == null)return;
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {};
		SocketManager.GAME_SEND_GJK_PACKET(perso, 2, 0, 1, 0, _taxCollector.getTimer(), _type);
		SocketManager.GAME_SEND_FIGHT_PLACES_PACKET(perso.getAccount().getGameThread().getOut(), getMap().getPlacesStr(), _st2);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getActorId()+"", perso.getActorId()+","+Constants.ETAT_PORTE+",0");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 3, 950, perso.getActorId()+"", perso.getActorId()+","+Constants.ETAT_PORTEUR+",0");
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getActorId());
		final Fighter f = new Fighter(this, perso);
		current_Join = f;
		f.setTeam(1);
		getTeam1().put(perso.getActorId(), f);
		perso.setFight(this);
		f.setFightCell(cell);
		f.getFightCell().addFighter(f);
		SocketManager.GAME_SEND_ILF_PACKET(perso, 0);
		perso.getCurCell().removePlayer(perso.getActorId());
		SocketManager.GAME_SEND_ADD_IN_TEAM_PACKET_TO_MAP(perso.getCurMap(), percoID, current_Join);
		SocketManager.GAME_SEND_FIGHT_PLAYER_JOIN(this,7,current_Join);
		SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,getMap(),perso);
	}

	public void toggleLockTeam(final int guid)
	{
		if(getInit0().getGUID() == guid)
		{
			locked0 = !locked0;
			Log.addToLog(locked0 ? "L'équipe 1 devient bloquée" : "L'équipe 1 n'est plus bloquée");
			SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), locked0 ? '+' : '-', 'A', guid);
			SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,1,locked0?"095":"096");
		}else if(getInit1().getGUID() == guid)
		{
			locked1 = !locked1;
			Log.addToLog(locked1 ? "L'équipe 2 devient bloquée" : "L'équipe 2 n'est plus bloquée");
			SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit1().getPlayer().getCurMap(), locked1 ? '+' : '-', 'A', guid);
			SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,2,locked1 ? "095" : "096");
		}
	}

	public void toggleOnlyGroup(final int guid)
	{
		if(getInit0().getGUID() == guid)
		{
			onlyGroup0 = !onlyGroup0;
			Log.addToLog(locked0?"L'équipe 1 n'accepte que les membres du groupe":"L'équipe 1 n'est plus bloquée");
			SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), onlyGroup0?'+':'-', 'P', guid);
			SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,1,onlyGroup0?"093":"094");
		}else if(getInit1().getGUID() == guid)
		{
			onlyGroup1 = !onlyGroup1;
			Log.addToLog(locked1?"L'équipe 2 n'accepte que les membres du groupe":"L'équipe 2 n'est plus bloquée");
			SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit1().getPlayer().getCurMap(), onlyGroup1?'+':'-', 'P', guid);
			SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,2,onlyGroup1?"095":"096");
		}
		
		if(!onlyGroup0)
		{
			synchronized(getTeam0()) {
				final Party group = getInit0().getPlayer().getParty();
				for(final Fighter f : getFighters(1))
				{
					final Player player = f.getPlayer();
					if(player == null) continue;
					if(player.getParty() == null || player.getParty() != group)
					{
						SocketManager.GAME_SEND_FIGHT_PLAYER_KICK(this, 7, f.getGUID());
						getTeam0().remove(f.getGUID());
						f.getFightCell().getFighters().remove(f.getGUID());
						player.setDuelID(-1);
						player.setReady(false);
						player.setSitted(false);
						player.refreshMapAfterFight();
					}
				}
			}
		} else if(!onlyGroup1)
		{
			synchronized(getTeam1()) {
				final Party group = getInit1().getPlayer().getParty();
				for(final Fighter f : getFighters(2))
				{
					final Player player = f.getPlayer();
					if(player == null) continue;
					if(player.getParty() == null || player.getParty() != group)
					{
						SocketManager.GAME_SEND_FIGHT_PLAYER_KICK(this, 7, f.getGUID());
						getTeam1().remove(f.getGUID());
						f.getFightCell().getFighters().remove(f.getGUID());
						player.setDuelID(-1);
						player.setReady(false);
						player.setSitted(false);
						player.refreshMapAfterFight();
					}
				}
			}
		}
	}
	
	public void toggleLockSpec(final int guid)
	{
		if(getInit0().getGUID() == guid || getInit1().getGUID() == guid)
		{
			specOk = !specOk;
			Log.addToLog(specOk ? "Le combat accepte les spectateurs" : "Le combat n'accepte plus les spectateurs");
			SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), specOk?'+':'-', 'S', getInit0().getGUID());
			SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), specOk?'+':'-', 'S', getInit1().getGUID());
			SocketManager.GAME_SEND_Im_PACKET_TO_MAP(getMap(),specOk?"039":"040");
		}
		synchronized(_spec) {
			final Map<Integer, Player> spectators = new TreeMap<Integer, Player>();
			spectators.putAll(_spec);
			for(final Entry<Integer, Player> entry : spectators.entrySet()) {
				_spec.remove(entry.getKey());
				entry.getValue().refreshMapAfterFight();
				SocketManager.GAME_SEND_GV_PACKET(entry.getValue());
			}
		}
	}

	public void toggleHelp(final int guid)
	{
		if(getInit0().getGUID() == guid)
		{
			help1 = !help1;
			Log.addToLog(help2?"L'équipe 1 demande de l'aide":"L'équipe 1s ne demande plus d'aide");
			SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit0().getPlayer().getCurMap(), locked0?'+':'-', 'H', guid);
			SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,1,help1?"0103":"0104");
		}else if(getInit1().getGUID() == guid)
		{
			help2 = !help2;
			Log.addToLog(help2?"L'équipe 2 demande de l'aide":"L'équipe 2 ne demande plus d'aide");
			SocketManager.GAME_SEND_FIGHT_CHANGE_OPTION_PACKET_TO_MAP(getInit1().getPlayer().getCurMap(), locked1?'+':'-', 'H', guid);
			SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this,2,help2?"0103":"0104");
		}
	}
	
	public void setState(final int _state) {
		this._state = _state;
	}

	public int getState() {
		return _state;
	}

	public List<Fighter> getListFighter() {
		return _ordreJeu;
	}

	public boolean fighterDeplace(final Fighter f, final GameAction GA)
	{
		final String path = GA._args;
		if(path.equals(""))
		{
			Log.addToErrorLog("Echec du deplacement: chemin vide");
			return false;
		}
		final Fighter curFighter = _ordreJeu.get(_curPlayer);
		if(_ordreJeu.size() <= _curPlayer)return false;
		if(curFighter == null)return false;
		Log.addToLog("("+_curPlayer+")Tentative de déplacement de Fighter ID= "+f.getGUID()+" a partir de la case "+f.getFightCell().getId());
		Log.addToLog("Path: "+path);
		if(!_curAction.equals("")|| curFighter.getGUID() != f.getGUID() || _state!= Constants.FIGHT_STATE_ACTIVE)
		{
			if(!_curAction.equals(""))
				Log.addToErrorLog("Echec du deplacement: il y deja une action en cours");
			if(curFighter.getGUID() != f.getGUID())
				Log.addToErrorLog("Echec du deplacement: ce n'est pas a ce joueur de jouer");
			if(_state != Constants.FIGHT_STATE_ACTIVE)
				Log.addToErrorLog("Echec du deplacement: le combat n'est pas en cours");
			return false;
		}
		
		final ArrayList<Fighter> tacle = Pathfinding.getEnnemiesFighterArround(f.getFightCell().getId(), getMap(), this);
		if(tacle != null && f.hasState(6))//Tentative de Tacle
		{
			for(Fighter T : tacle)
			{ 
				if(T.hasState(6)) 
				{ 
					tacle.remove(T); 
				} 
			}
			if(!tacle.isEmpty())//Si tous les tacleur ne sont pas stabilisés
			{
				Log.addToLog("Le personnage est a cote de ("+tacle.size()+") ennemi(s)");
				final int chance = Formulas.getTacleChance(f, tacle);
				final int rand = Formulas.getRandomValue(0, 99);
				if(rand > chance)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7,GA._id, "104",curFighter.getGUID()+";", "");//Joueur taclé
					int pertePA = _curFighterPA * chance / 100 / 2;
					
					if(pertePA < 0)pertePA = -pertePA;
					if(_curFighterPM < 0)_curFighterPM = 0;
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7,GA._id,"129", f.getGUID()+"", f.getGUID()+",-"+_curFighterPM);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7,GA._id,"102", f.getGUID()+"", f.getGUID()+",-"+pertePA);
					
					_curFighterPM = 0;
					_curFighterPA -= pertePA;
					Log.addToLog("Echec du deplacement: fighter tacle");
					return false;
				}
			}
		}
		
		//*
		final AtomicReference<String> pathRef = new AtomicReference<String>(path);
		int nStep = Pathfinding.isValidPath(getMap(), f.getFightCell().getId(), pathRef, this);
		final String newPath = pathRef.get();
		if( nStep > _curFighterPM || nStep == -1000)
		{
			Log.addToLog("("+_curPlayer+") Fighter ID= "+curFighter.getGUID()+" a demander un chemin inaccessible ou trop loin");
			return false;
		}
		
		_curFighterPM -= nStep;
		_curFighterUsedPM += nStep;
		
		final int nextCellID = CryptManager.cellCodeToID(newPath.substring(newPath.length() - 2));
		//les monstres n'ont pas de GAS//GAF
		if(curFighter.getPlayer() != null)
			SocketManager.GAME_SEND_GAS_PACKET_TO_FIGHT(this,7,curFighter.getGUID());
        //Si le joueur n'est pas invisible
        if(!curFighter.isHide())
	        SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, GA._id, "1", curFighter.getGUID()+"", "a"+CryptManager.cellIDToCode(f.getFightCell().getId())+newPath);
        else//Si le joueur est planqué x)
        {
        	if(curFighter.getPlayer() != null)
        	{
        		//On envoie le path qu'au joueur qui se déplace
        		final PrintWriter out = curFighter.getPlayer().getAccount().getGameThread().getOut();
        		SocketManager.GAME_SEND_GA_PACKET(out,  GA._id+"", "1", curFighter.getGUID()+"", "a"+CryptManager.cellIDToCode(f.getFightCell().getId())+newPath);
        	}
        }
       
        //Si porté
        Fighter po = curFighter.getHoldedBy();
        if(po != null && curFighter.hasState(Constants.ETAT_PORTE) && po.hasState(Constants.ETAT_PORTEUR))
        {
        	System.out.println("Porteur: "+po.getPacketsName());
        	System.out.println("NextCellID "+nextCellID);
        	System.out.println("Cell du Porteur "+po.getFightCell().getId());
        	
        	//si le joueur va bouger
       		if(nextCellID != po.getFightCell().getId())
       		{
       			//on retire les états
       			po.setState(Constants.ETAT_PORTEUR, 0);
       			curFighter.setState(Constants.ETAT_PORTE,0);
       			//on retire dé lie les 2 fighters
       			po.setIsHolding(null);
       			curFighter.setHoldedBy(null);
       			//La nouvelle case sera définie plus tard dans le code
       			//On envoie les packets
       			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 950, po.getGUID()+"", po.getGUID()+","+Constants.ETAT_PORTEUR+",0");
    			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 950, curFighter.getGUID()+"", curFighter.getGUID()+","+Constants.ETAT_PORTE+",0");
       		}
      	}
        
		curFighter.getFightCell().getFighters().clear();
        Log.addToLog("("+_curPlayer+") Fighter ID= "+f.getGUID()+" se déplace de la case "+curFighter.getFightCell().getId()+" vers "+CryptManager.cellCodeToID(newPath.substring(newPath.length() - 2)));
        curFighter.setFightCell(getMap().getCell(nextCellID));
        curFighter.getFightCell().addFighter(curFighter);
        if(po != null) po.getFightCell().addFighter(po);
        if(nStep < 0) 
        {
     	   if(Config.CONFIG_DEBUG) Log.addToLog("("+_curPlayer+") Fighter ID= "+f.getGUID()+" nStep negatives, reconversion");
     	   nStep = nStep*(-1);
        }
        _curAction = "GA;129;"+curFighter.getGUID()+";"+curFighter.getGUID()+",-"+nStep;
        
        //Si porteur
        po = curFighter.getIsHolding();
        if(po != null
        && curFighter.hasState(Constants.ETAT_PORTEUR)
        && po.hasState(Constants.ETAT_PORTE))
        {
       		//on déplace le porté sur la case
        	po.setFightCell(curFighter.getFightCell());
        	Log.addToLog(po.getPacketsName()+" se deplace vers la case "+nextCellID);
      	}
        
        if(f.getPlayer() == null)
        {
        	try {
    			Thread.sleep(900+100*nStep);//Estimation de la durée du déplacement
    		} catch (final InterruptedException e) {};
        	SocketManager.GAME_SEND_GAMEACTION_TO_FIGHT(this,7,_curAction);
    		_curAction = "";
    		final ArrayList<Trap> P = new ArrayList<Trap>();
    		P.addAll(_traps);
    		for(final Trap p : P)
    		{
    			final Fighter F = curFighter;
    			final int dist = Pathfinding.getDistanceBetween(getMap(),p.getCell().getId(),F.getFightCell().getId());
    			//on active le piege
    			if(dist <= p.getSize())p.onTraped(F);
    		}
    		return true;
        }
        //*/
        f.getPlayer().getAccount().getGameThread().addAction(GA);
        return true;
    }

	public void onGK(final Player perso)
	{
		if(_curAction.equals("")|| _ordreJeu.get(_curPlayer).getGUID() != perso.getActorId() || _state!= Constants.FIGHT_STATE_ACTIVE)return;
		Log.addToLog("("+_curPlayer+")Fin du déplacement de Fighter ID= "+perso.getActorId());
		SocketManager.GAME_SEND_GAMEACTION_TO_FIGHT(this,7,_curAction);
		SocketManager.GAME_SEND_GAF_PACKET_TO_FIGHT(this,7,2,_ordreJeu.get(_curPlayer).getGUID());
		//copie
		final ArrayList<Trap> P = (new ArrayList<Trap>());
		P.addAll(_traps);
		for(final Trap p : P)
		{
			final Fighter F = getFighterByPerso(perso);
			final int dist = Pathfinding.getDistanceBetween(getMap(),p.getCell().getId(),F.getFightCell().getId());
			//on active le piege
			if(dist <= p.getSize())p.onTraped(F);
			if(_state == Constants.FIGHT_STATE_FINISHED)break;
		}
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {};
		
		_curAction = "";
	}

	public void playerPass(final Player _perso)
	{
		final Fighter f = getFighterByPerso(_perso);
		if(f == null)return;
		if(!f.canPlay())return;
		endTurn();
	}

	public int tryCastSpell(final Fighter fighter,final SpellStat Spell, final int caseID)
	{
		if(!_curAction.equals(""))return 10;
		if(Spell == null)return 10;
		
		final DofusCell Cell = getMap().getCell(caseID);
		_curAction = "casting";
		if(CanCastSpell(fighter,Spell,Cell, -1))
		{
			if(fighter.getPlayer() != null)
			{
				fighter.getPlayer().clearCacheAS();
				SocketManager.GAME_SEND_STATS_PACKET(fighter.getPlayer());
			}
			
			Log.addToLog(fighter.getPacketsName()+" tentative de lancer le sort "+Spell.getSpellID()+" sur la case "+caseID);
			_curFighterPA -= Spell.getPACost();
			_curFighterUsedPA += Spell.getPACost();
			SocketManager.GAME_SEND_GAS_PACKET_TO_FIGHT(this, 7, fighter.getGUID());
			final boolean isEc = Spell.getTauxEC() != 0 && Formulas.getRandomValue(1, Spell.getTauxEC()) == Spell.getTauxEC();
			if(isEc)
			{
				Log.addToLog(fighter.getPacketsName()+" Echec critique sur le sort "+Spell.getSpellID());
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 302, fighter.getGUID()+"", Spell.getSpellID()+"");
			}else
			{
				final boolean isCC = fighter.testIfCC(Spell.getTauxCC());
				final String sort = Spell.getSpellID()+","+caseID+","+Spell.getSpriteID()+","+Spell.getLevel()+","+Spell.getSpriteInfos();
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 300, fighter.getGUID()+"", sort);	
				if(isCC)
				{
					Log.addToLog(fighter.getPacketsName()+" Coup critique sur le sort "+Spell.getSpellID());
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 301, fighter.getGUID()+"", sort);
				}
				//Si le joueur est invi, on montre la case
				if(fighter.isHide())
				{
					if(Spell.getSpellID() == 0)// si coup de poing
					{
						fighter.unHide(-666);
					}else
					{
						showCaseToAll(fighter.getGUID(), fighter.getFightCell().getId());
					}
				}
				//on applique les effets de l'arme
				Spell.applySpellEffectToFight(this,fighter,Cell,isCC);
			}
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102,fighter.getGUID()+"",fighter.getGUID()+",-"+Spell.getPACost());
			SocketManager.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, fighter.getGUID());
			if(!isEc) fighter.addCooldown(Cell.getFirstFighter(), Spell);
			/*//Refresh des Stats
			refreshCurPlayerInfos();*/
			
			try 
			{
				Thread.sleep(500);
			} catch (final InterruptedException e) {};
			if((isEc && Spell.isEcEndTurn()))
			{
				_curAction = "";
				try {
					Thread.sleep(500);
				} catch (final InterruptedException e) {};
				endTurn();
				return 5;
			}
			verifIfTeamAllDead();
		} else
		{
			SocketManager.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, fighter.getGUID());
		}
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {};
		_curAction = "";
		return 0;
	}

	public boolean CanCastSpell(final Fighter fighter, final SpellStat spell, final DofusCell cell, int cellTarget)
	{
		int launcherCell;
		if(cellTarget <= -1)
		{
			launcherCell = fighter.getFightCell().getId();
		}else
		{
			launcherCell = cellTarget;
		}
		final Fighter curFighter = _ordreJeu.get(_curPlayer);
		if(curFighter == null)
			return false;
		final Player player = fighter.getPlayer();
		//Si le sort n'est pas existant
		if(spell == null)
		{
			Log.addToErrorLog("Sort non existant");
			if(player != null)
            {
				SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
                SocketManager.GAME_SEND_Im_PACKET(player, "1169");
            }
			return false;
		}
		for(int state : spell.getStatesNeeded())
		{
			if(state == -1)
				break;
			if(!fighter.hasState(state))
			{
				if(player != null)
				{
					SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
					SocketManager.GAME_SEND_Im_PACKET(player, "1175");
				}
				return false;
			}
		}
		for(int state : spell.getStatesProhibited())
		{
			if(fighter.hasState(state))
			{
				if(player != null)
				{
					SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
					SocketManager.GAME_SEND_Im_PACKET(player, "1175");
				}
				return false;
			}
		}
		//Si ce n'est pas au joueur de jouer
		if(curFighter.getGUID() != fighter.getGUID())
		{
			Log.addToErrorLog("Ce n'est pas au joueur de jouer");
			if(player != null)
			{
				SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
				SocketManager.GAME_SEND_Im_PACKET(player, "1175");
			}
			return false;
		}
		//Si le joueur n'a pas assez de PA
		if(_curFighterPA < spell.getPACost())
		{
			Log.addToErrorLog("Le joueur n'a pas assez de PA ("+_curFighterPA+"/"+spell.getPACost()+")");
			if(player != null)
			{
				SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
				SocketManager.GAME_SEND_Im_PACKET(player, "1170;"+_curFighterPA+"~"+spell.getPACost());
			}
			return false;
		}
		//Si la cellule visée n'existe pas
		if(cell == null)
		{
			Log.addToErrorLog("La cellule visée n'existe pas");
			if(player != null)
            {
				SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
                SocketManager.GAME_SEND_Im_PACKET(player, "1193");
            }
			return false;
		}
		//Si la cellule visée n'est pas alignée avec le joueur alors que le sort le demande
		if(spell.isLineLaunch() && !Pathfinding.casesAreInSameLine(getMap(), launcherCell, cell.getId(), 'z'))
		{
			Log.addToErrorLog("Le sort demande un lancer en ligne, or la case n'est pas alignée avec le joueur");
			if(player != null)
            {
				SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
                SocketManager.GAME_SEND_Im_PACKET(player, "1173");
            }
			return false;
		}
		//Si le sort demande une ligne de vue et que la case demandée n'en fait pas partie
		if(spell.hasLDV() && !Pathfinding.checkLoS(getMap(),launcherCell,cell.getId(),fighter))
		{
			Log.addToErrorLog("Le sort demande une ligne de vue, mais la case visée n'est pas visible pour le joueur");
			if(player != null)
            {
				SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
                SocketManager.GAME_SEND_Im_PACKET(player, "1174");
            }
			return false;
		}
		final int dist = Pathfinding.getDistanceBetween(getMap(), launcherCell, cell.getId());
		int MaxPO = spell.getMaxPO();
		if(spell.isModifPO())
			MaxPO += fighter.getTotalStats().getEffect(Constants.STATS_ADD_PO);
		//Vérification Portée mini / maxi
		if(dist < spell.getMinPO() || dist > MaxPO)
		{
			Log.addToErrorLog("La case est trop proche ou trop éloignée Min: "+spell.getMinPO()+" Max: "+spell.getMaxPO()+" Dist: "+dist);
			if(player != null)
			{
				SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
				SocketManager.GAME_SEND_Im_PACKET(player, "1171;"+spell.getMinPO()+"~"+spell.getMaxPO()+"~"+dist);				
			}
			return false;
		}
		if(!CooldownSpell.cooldownElasped(fighter, spell.getSpellID()))
		{
			if(player != null)
			{
				SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
				SocketManager.GAME_SEND_Im_PACKET(player, "1175");
			}
			return false;
		}
		if(spell.getMaxLaunchbyTurn() - CooldownSpell.getNumbCastSpell(fighter, spell.getSpellID()) <= 0 && spell.getMaxLaunchbyTurn() >0)
		{
			if(player != null)
			{
				SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
				SocketManager.GAME_SEND_Im_PACKET(player, "1175");
			}
			return false;
		}
		if(spell.getMaxLaunchbyByTarget() - CooldownSpell.getNumbCastSpellForTarget(fighter, cell.getFirstFighter(), spell.getSpellID()) <= 0 && spell.getMaxLaunchbyByTarget() >0)
		{
			if(player != null)
			{
				SocketManager.GAME_SEND_GA_CLEAR_PACKET_TO_FIGHT(this, 7);
				SocketManager.GAME_SEND_Im_PACKET(player, "1175");
			}
			return false;
		}
		return true;
	}
	
	public String GetGE(final int win)
    {
		final long time = System.currentTimeMillis() - _startTime;

		final int initGUID = getInit0().getGUID();		
		int type = Constants.FIGHT_TYPE_CHALLENGE;// toujours 0
		if(_type == Constants.FIGHT_TYPE_AGRESSION)//Sauf si gain d'honneur
			type = _type;
		boolean winTeamAreMobs = false;
        final StringBuilder Packet = new StringBuilder();
        Packet.append("GE").append(time);
        if(_type == Constants.FIGHT_TYPE_PVM) Packet.append(';').append(getMobGroup().getStarPercent());
        Packet.append('|').append(initGUID).append('|').append(type).append('|');
        final ArrayList<Fighter> TEAM1 = new ArrayList<Fighter>();
        final ArrayList<Fighter> TEAM2 = new ArrayList<Fighter>();
        
        if(win == 1)
        {
        	TEAM1.addAll(getTeam0().values());
        	TEAM2.addAll(getTeam1().values());
        }
        else
        {
        	TEAM1.addAll(getTeam1().values());
        	TEAM2.addAll(getTeam0().values());
        }
        //Calculs des niveaux de groupes
        /*int TEAM1lvl = 0;
        int TEAM2lvl = 0;
        for(Fighter F : TEAM1)
        {
        	if(F.isInvocation())continue;
        	TEAM1lvl += F.getLvl();
        }
        for(Fighter F : TEAM2)
        {
        	if(F.isInvocation())continue;
        	TEAM2lvl += F.getLvl();
        }*/
        //fin
        /* DEBUG
        System.out.println("TEAM1: lvl="+TEAM1lvl);
        System.out.println("TEAM2: lvl="+TEAM2lvl);
        //*/
        //détermination si le groupe est un monstre - mode héroique
        if(_type == Constants.FIGHT_TYPE_PVM && Config.SERVER_ID == 22)
        {
        	boolean hasMob = false;
        	for(final Fighter f : TEAM1)
        	{
        		if(!f.isInvocation() && f.isMob()) {
        			hasMob = true;
        			break;
        		}
        	}
        	winTeamAreMobs = hasMob;
        }
        //DROP SYSTEM
        //Calcul de la PP de groupe
        int groupPP = 0;
        long minkamas = 0,maxkamas = 0;
        for(final Fighter F : TEAM1)
        {
        	if(!F.isInvocation() || (F.getMob() != null && F.getMob().getTemplate().getID() == 258))
        		groupPP += F.getTotalStats().getEffect(Constants.STATS_ADD_PROS);
        }
        if(groupPP <0)groupPP =0;
        //Calcul des drops possibles
        final ArrayList<Drop> possibleDrops = new ArrayList<Drop>();
        float divider = 2.4F;
		if(TEAM1.size()>1)
			divider -= 0.2F*(TEAM1.size()-1);
        for(final Fighter F : TEAM2)
        {
        	final MonsterGrade mob = F.getMob();
        	if(mob != null && !F.isInvocation())
        	{
        		minkamas += mob.getTemplate().getMinKamas();
            	maxkamas += mob.getTemplate().getMaxKamas();
            	final ArrayList<Drop> drops = new ArrayList<Drop>(mob.getDrops());
            	drops.addAll(mob.getLoots());
            	/*for(Drop D : F.getMob().getDrops())
            	{
            		if(D.getMinProsp() <= groupPP)
            		{
            			//On augmente le taux en fonction de la PP
            			int taux = (int)((groupPP * D.getRate()*Ancestra.CONFIG_DROP)/100);
            			possibleDrops.add(new Drop(D.getItemID(),0,taux,D.getMax()));
            		}
            	}*/
            	
            	for(final Drop D : drops)
            	{
            		if(D.isSpecial())
            		{
            			possibleDrops.add(new Drop(D.getGuid(), D.getItemID(), true));
            			continue;
            		}
            		if(D.getMinProsp() <= groupPP)
            		{
            			//On augmente le taux en fonction de la PP
            			//int taux = (int)((groupPP * D.getRate()*Config.CONFIG_DROP)/100);
    					final float taux = ((groupPP / 100) * D.getRate() / divider) * Config.CONFIG_DROP;
            			possibleDrops.add(new Drop(D.getItemID(),0,taux,D.getMax()));
            		}
            	}
            	continue;
        	}
        	if(Config.SERVER_ID == 22 && F.getPlayer() != null && _type == Constants.FIGHT_TYPE_AGRESSION)
        	{
        		final Player p = F.getPlayer();
        		final long kamas = p.getKamas();
        		minkamas += kamas;
        		maxkamas += kamas;
        		p.restoreItems();
        		for(final Item I : p.getItems().values())
        		{
        			possibleDrops.add(new Drop(I.getGuid(), I.getTemplate().getID(), true));
        		}
        		p.deleteAllItems();
        		p.setKamas(0);
        	}
        }
        
        if(_type == Constants.FIGHT_TYPE_PVT)
        {
        	minkamas = (int)getTaxCollector().getKamas() / TEAM1.size();
        	maxkamas = minkamas;
        	possibleDrops.addAll(getTaxCollector().getDrops());     
        }

        //On Réordonne la liste en fonction de la PP
        final ArrayList<Fighter> Temp = new ArrayList<Fighter>();
        Fighter curMax = null;
        while(Temp.size() < TEAM1.size())
        {
        	int curPP = -1;
        	for(final Fighter F : TEAM1)
        	{
        		//S'il a plus de PP et qu'il n'est pas listé
        		if(F.getTotalStats().getEffect(Constants.STATS_ADD_PROS) > curPP && !Temp.contains(F))
        		{
        			curMax = F;
        			curPP = F.getTotalStats().getEffect(Constants.STATS_ADD_PROS);
        		}
        	}
        	Temp.add(curMax);
        }
        //On enleve les invocs
        TEAM1.clear();
        TEAM1.addAll(Temp);
        /* DEBUG
	        System.out.println("DROP: PP ="+groupPP);
	        System.out.println("DROP: nbr="+possibleDrops.size());
	        System.out.println("DROP: Kam="+totalkamas);
	        //*/
        //FIN DROP SYSTEM
        //XP SYSTEM
        long totalXP = 0;
        for(final Fighter F : TEAM2)
        {
        	if(!F.isInvocation() && F.getMob() != null)
        	{
        		totalXP += F.getMob().getBaseXp();
        		totalXP += F.getMob().getXpWon();
        	}
        	if(F.getPlayer() != null && Config.SERVER_ID == 22 && _type == Constants.FIGHT_TYPE_AGRESSION)
        		totalXP += (long)(F.getPlayer().getCurExp()/10);
        }
        /* DEBUG
	    System.out.println("TEAM1: xpTotal="+totalXP);
	    //*/
        //FIN XP SYSTEM

        //Capture d'âmes
        boolean mobCapturable = true;
        for(final Fighter F : TEAM2)
        {
        	try
        	{
        		mobCapturable &= F.getMob().getTemplate().isCapturable();
        	}catch (final Exception e) {
        		mobCapturable = false;
        		break;
        	}
        }
        isCapturable |= mobCapturable;

        if(isCapturable)
        {
        	boolean isFirst = true;
        	int maxLvl = 0;
        	final StringBuilder pierreStats = new StringBuilder();


        	for(final Fighter F : TEAM2)	//Création de la pierre et verifie si le groupe peut être capturé TODO
        	{
        		if(!isFirst)
        			pierreStats.append('|');

        		pierreStats.append(F.getMob().getTemplate().getID()).append(',').append(F.getLvl());//Converti l'ID du monstre en Hex et l'ajoute au stats de la futur pierre d'âme

        		isFirst = false;

        		if(F.getLvl() > maxLvl)	//Trouve le monstre au plus haut lvl du groupe (pour la puissance de la pierre)
        			maxLvl = F.getLvl();
        	}
        	pierrePleine = new SoulStone(World.getNewItemGuid(),1,7010,Constants.ITEM_POS_NO_EQUIPED,pierreStats.toString());	//Crée la pierre d'âme

        	for(final Fighter F : TEAM1)	//Récupère les captureur
        	{
        		if(!F.isInvocation() && F.hasState(Constants.ETAT_CAPT_AME))
        			_captureur.add(F);
        	}

        	if(_captureur.size() > 0 && !World.isArenaMap(getMap().getId()))	//S'il y a des captureurs
        	{
        		for (int i = 0; i < _captureur.size(); i++)
        		{
        			try
        			{
        				final Fighter f = _captureur.get(Formulas.getRandomValue(0, _captureur.size()-1));	//Récupère un captureur au hasard dans la liste
        				if(!(f.getPlayer().getObjetByPos(Constants.ITEM_POS_ARME).getTemplate().getType() == Constants.ITEM_TYPE_PIERRE_AME)) //Avoir une pierre d'ame vide d'équipé
        				{
        					_captureur.remove(f);
        					continue;
        				}
        				final Couple<Integer,Integer> pierreJoueur = Formulas.decompSoulStone(f.getPlayer().getObjetByPos(Constants.ITEM_POS_ARME));//Récupère les stats de la pierre équippé

        				if(pierreJoueur.second < maxLvl)	//Si la pierre est trop faible
        				{
        					_captureur.remove(f);
        					continue;
        				}

        				final int captChance = Formulas.totalCaptChance(pierreJoueur.first, f.getPlayer());

        				if(Formulas.getRandomValue(1, 100) <= captChance)	//Si le joueur obtiens la capture
        				{
        					//Retire la pierre vide au personnage et lui envoie ce changement
        					final int pierreVide = f.getPlayer().getObjetByPos(Constants.ITEM_POS_ARME).getGuid();
        					f.getPlayer().deleteItem(pierreVide);
        					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(f.getPlayer(), pierreVide);

        					captWinner = f._id;
        					break;
        				}
        			}
        			catch(final NullPointerException e)
        			{
        				continue;
        			}
        		}
        	}
        }

        //Fin Capture
        
        final Random random = new Random();
        
        long xpForMob = 0, kamasForMob = 0;
        if(Config.SERVER_ID == 22 && winTeamAreMobs)
        {
        	xpForMob = (long)(totalXP / getMobGroup().getSize());
        	kamasForMob = (long)(maxkamas / getMobGroup().getSize());
        }
        
        for(final Fighter i : TEAM1)
        {
        	if(winTeamAreMobs)
        	{
        		final MonsterGrade mob = i.getMob();
        		mob.setXpWon(xpForMob);
        		mob.setKamasWon(kamasForMob);
        		final StringBuilder drops = new StringBuilder();
        		//Drop system
        		final ArrayList<Drop> temp = new ArrayList<Drop>();
        		temp.addAll(possibleDrops);
        		for(final Drop D : temp)
        		{
        			if(random.nextBoolean())
        			{
        				if(drops.length() >0)
        					drops.append(',');
            			drops.append(D.getItemID()).append('~').append(World.getObjet(D.getGuid()).getQuantity());
            			mob.getLoots().add(D);
            			possibleDrops.remove(D);
        			}
        		}
        		Packet.append("2;").append(i.getGUID()).append(';').append(i.getPacketsName()).append(';').append(i.getLvl()).append(';').append((i.isDead() ?  "1" : "0" )).append(';');
        		Packet.append(i.xpString(";")).append(';');
        		Packet.append((xpForMob == 0?"":xpForMob)).append(';');
        		Packet.append(';');
        		Packet.append(';');
        		Packet.append(drops).append(';');//Drop
        		Packet.append(kamasForMob == 0?"":kamasForMob).append('|');
        		continue;
        	}
        	if(type == Constants.FIGHT_TYPE_CHALLENGE)
        	{
        		final Player player = i.getPlayer();
        		if(i.isInvocation() && i.getMob() != null && i.getMob().getTemplate().getID() != 258)continue;
        		long winxp 	= Formulas.getXpWinPvm2(i,TEAM1,TEAM2,totalXP);
        		final AtomicReference<Long> XP = new AtomicReference<Long>();
        		XP.set(winxp);

        		final long guildxp = Formulas.getGuildXpWin(i,XP);
        		long mountxp = 0;
        		if(player != null && player.isOnMount())
        		{
        			mountxp = Formulas.getMountXpWin(i,XP);
        			player.getMount().addXp(mountxp);
        			SocketManager.GAME_SEND_Re_PACKET(player,"+",player.getMount());
        		}
        		final long winKamas = Formulas.getKamasWin(i,TEAM1,minkamas,maxkamas);
        		final StringBuilder drops = new StringBuilder();
        		//Drop system
        		final ArrayList<Drop> temp = new ArrayList<Drop>();
        		temp.addAll(possibleDrops);
        		final Map<Integer,Integer> itemWon = new TreeMap<Integer,Integer>();
        		ArrayList<Item> lootsWon = null;

        		for(final Drop D : temp)
        		{
        			final int t = (int)(D.getRate()*100);//Permet de gerer des taux>0.01
        			final int jet = Formulas.getRandomValue(0, 10000);
        			if(jet < t)
        			{
        				if(random.nextBoolean()) //fix rapide à refaire 
        				{
                			if(D.isSpecial())
                			{
                				if(lootsWon == null) lootsWon = new ArrayList<Item>();
                				lootsWon.add(World.getObjet(D.getGuid()));
                				possibleDrops.remove(D);
                				continue;
                			}
        					final ItemTemplate OT = World.getItemTemplate(D.getItemID());
            				if(OT == null)continue;
            				//on ajoute a la liste
            				itemWon.put(OT.getID(),(itemWon.get(OT.getID())==null?0:itemWon.get(OT.getID()))+1);

            				D.setMax(D.getMax()-1);
            				if(D.getMax() == 0)possibleDrops.remove(D);
        				}
        			}
        		}

        		if(i._id == captWinner)	//S'il à capturé le groupe
        		{
        			if(drops.length() >0)drops.append(',');
        			drops.append(pierrePleine.getTemplate().getID()).append('~').append(1);
        			if(player.addItem(pierrePleine, false))
        				World.addItem(pierrePleine, true);
        			player.itemLog(pierrePleine.getTemplate().getID(), 1, "Capturé");
        		}

        		for(final Entry<Integer,Integer> entry : itemWon.entrySet())
        		{
        			final ItemTemplate OT = World.getItemTemplate(entry.getKey());
        			if(OT == null)continue;
        			if(drops.length() >0)drops.append(',');
        			drops.append(entry.getKey()).append('~').append(entry.getValue());
        			final Item obj = OT.createNewItem(entry.getValue(), false);
        			if(player.addItem(obj, true))
        				World.addItem(obj, true);
        			else if (i.isInvocation() && i.getMob().getTemplate().getID() == 285 
        					&& i.getInvocator().getPlayer().addItem(obj, true)) 
        				World.addItem(obj, true);
        			player.itemLog(obj.getTemplate().getID(), obj.getQuantity(), "Droppé");
        		}
        		
        		if(lootsWon != null)
        		{
        			for(final Item loot : lootsWon)      			
        			{
        				if(drops.length()>0)drops.append(',');
        				drops.append(loot.getTemplate().getID()).append('~').append(loot.getQuantity());
        				if(player.addItem(loot, true))
            				World.addItem(loot, true);
            			else if (i.isInvocation() && i.getMob().getTemplate().getID() == 285 
            					&& i.getInvocator().getPlayer().addItem(loot, true)) 
            				World.addItem(loot, true);
        				player.itemLog(loot.getTemplate().getID(), loot.getQuantity(), "Droppé");
        			}
        		}

        		//fin drop system
        		winxp = XP.get();
        		if(Config.SERVER_ID == 22)
        			winxp *= 3;
        		if(Config.SERVER_ID == 22 && player.getLevelReached() < player.getLvl())
        			winxp *= 2;
        		if(winxp != 0 && player != null)
        			player.addXp(winxp);
        		
        		if(i.getKamasStolen() > 0 && player != null)
        			player.addKamas(i.getKamasStolen());
        		if(winKamas != 0 && player != null)
        		{
        			player.addKamas(winKamas);
        			player.kamasLog(winKamas+"", "Gagnés dans un combat");
        		} else if(i.isInvocation() && i.getMob().getTemplate().getID() == 285 && i.getInvocator().getPlayer() != null) 
        		{
        			i.getInvocator().getPlayer().addKamas(winKamas);
        			i.getInvocator().getPlayer().kamasLog(winKamas+"", "Gagnés dans un combat");
        		}
        		if(guildxp > 0 && player.getGuildMember() != null)
        			player.getGuildMember().giveXpToGuild(guildxp);


        		Packet.append("2;").append(i.getGUID()).append(';').append(i.getPacketsName()).append(';').append(i.getLvl()).append(';').append((i.isDead() ?  '1' : '0')).append(';');
        		Packet.append(i.xpString(";")).append(';');
        		Packet.append((winxp == 0?"":winxp)).append(';');
        		Packet.append((guildxp == 0?"":guildxp)).append(';');
        		Packet.append((mountxp == 0?"":mountxp)).append(';');
        		Packet.append(drops).append(';');//Drop
        		Packet.append((winKamas == 0?"":winKamas)).append('|');
        	}else
        	{
        		//Calcul honeur
        		int winH = Formulas.calculHonorWin(TEAM1,TEAM2,i);
        		final int winD = Formulas.calculDeshonorWin(TEAM1,TEAM2,i);

        		final StringBuilder drops = new StringBuilder();
        		final ArrayList<Drop> temp = new ArrayList<Drop>();
        		temp.addAll(possibleDrops);
        		ArrayList<Item> lootsWon = null;
        		long winxp 	= 0;
        		long winKamas = 0;
        		final Player P = i.getPlayer();
        		if(Config.SERVER_ID == 22)
        		{
        			winxp = totalXP/TEAM1.size();
            		winKamas = maxkamas/TEAM1.size();
            		
        			for(final Drop D : temp)
            		{
            			if(random.nextBoolean()) //fix rapide à refaire 
        				{
        					if(lootsWon == null) lootsWon = new ArrayList<Item>();
            				lootsWon.add(World.getObjet(D.getGuid()));
            				possibleDrops.remove(D);
            				continue;
        				}
            		}
            		
            		if(lootsWon != null)
            		{
            			for(final Item loot : lootsWon)      			
            			{
            				if(drops.length()>0)drops.append(',');
            				drops.append(loot.getTemplate().getID()).append('~').append(loot.getQuantity());
            				if(P.addItem(loot, true))
                				World.addItem(loot, true);
                			else if (i.isInvocation() && i.getMob().getTemplate().getID() == 285 
                					&& i.getInvocator().getPlayer().addItem(loot, true)) 
                				World.addItem(loot, true);
            				P.itemLog(loot.getTemplate().getID(), loot.getQuantity(), "Droppé");
            			}
            		}
            		
            		P.addXp(winxp);
            		P.addKamas(winKamas);
        		}
        		
        		if(P.getHonor()+winH<0)winH = -P.getHonor();
        		P.addHonor(winH);
        		P.setDeshonor(P.getDeshonor()+winD);
        		Packet.append("2;").append(i.getGUID()).append(';').append(i.getPacketsName()).append(';').append(i.getLvl()).append(';').append((i.isDead() ?  '1' : '0')).append(';');
        		Packet.append((P.getAlign()!=Constants.ALIGNEMENT_NEUTRE?World.getExpLevel(P.getGrade()).pvp:0)).append(';');
        		Packet.append(P.getHonor()).append(';');
        		int maxHonor = World.getExpLevel(P.getGrade()+1).pvp;
        		if(maxHonor == -1)maxHonor = World.getExpLevel(P.getGrade()).pvp;
        		Packet.append((P.getAlign()!=Constants.ALIGNEMENT_NEUTRE?maxHonor:0)).append(';');
        		Packet.append(winH).append(';');
        		Packet.append(P.getGrade()).append(';');
        		Packet.append(P.getDeshonor()).append(';');
        		Packet.append(winD).append(';');
        		Packet.append(drops.toString()).append(';');
        		Packet.append(winKamas).append(';');
        		Packet.append("0;0;0;0;0|");
        	}
        }
        for(final Fighter i : TEAM2)
        {
        	if(i.isInvocation() && i.getMob().getTemplate().getID() != 285)continue;//On affiche pas les invocs
        	if(_type != Constants.FIGHT_TYPE_AGRESSION)
        		Packet.append("0;").append(i.getGUID()).append(';').append(i.getPacketsName()).append(';').append(i.getLvl()).append(';').append((i.isDead() ?  1 : 0 )+';'+i.xpString(";")).append(";;;;|");
        	else
        	{
        		//Calcul honeur
        		int loseH = Formulas.calculHonorWin(TEAM1,TEAM2,i);
        		int winD = Formulas.calculDeshonorWin(TEAM1,TEAM2,i);

        		final Player P = i.getPlayer();
        		if(P.getHonor()+loseH<0)loseH = P.getHonor();
        		P.remHonor(loseH);
        		if(P.getDeshonor()+winD<0)winD = -P.getDeshonor();
        		if(P.getDeshonor()+winD>100)winD = 100-P.getDeshonor();
        		P.setDeshonor(P.getDeshonor()+winD);
        		Packet.append("0;").append(i.getGUID()).append(';').append(i.getPacketsName()).append(';').append(i.getLvl()).append(';').append((i.isDead() ? 1 : 0)).append(';');
        		Packet.append((P.getAlign()!=Constants.ALIGNEMENT_NEUTRE?World.getExpLevel(P.getGrade()).pvp:0)).append(';');
        		Packet.append(P.getHonor()).append(';');
        		int maxHonor = World.getExpLevel(P.getGrade()+1).pvp;
        		if(maxHonor == -1)maxHonor = World.getExpLevel(P.getGrade()).pvp;
        		Packet.append((P.getAlign()!=Constants.ALIGNEMENT_NEUTRE?maxHonor:0)).append(';');
        		Packet.append(loseH*-1).append(';');
        		Packet.append(P.getGrade()).append(';');
        		Packet.append(P.getDeshonor()).append(';');
        		Packet.append(winD);
        		Packet.append(";;0;0;0;0;0|");
        	}
        }
        if(getMap().getPercepteur() != null && _type == Constants.FIGHT_TYPE_PVM)
		{
			final TaxCollector p = getMap().getPercepteur();
			final long winxp = (int)Math.floor(Formulas.getXpWinPerco(p,TEAM1,TEAM2,totalXP)/100);
			final long winkamas = (int)Math.floor(Formulas.getKamasWinPerco(minkamas,maxkamas)/100);
			p.setXp(p.getXp()+winxp);
			p.setKamas(p.getKamas()+winkamas);
			final Guild G = p.getGuild();
			Packet.append("5;").append(p.getActorId()).append(';').append(p.getName1()).append(',').append(p.getName2()).append(';').append(G.getLvl()).append(";0;");
			Packet.append(G.getLvl()).append(';');
			Packet.append(G.getXp()).append(';');
			Packet.append(World.getGuildXpMax(G.getLvl())).append(';');
			Packet.append(';');//XpGagner
			Packet.append(winxp).append(';');//XpGuilde
			Packet.append(';');//Monture
			
			final StringBuilder drops = new StringBuilder();
    		final ArrayList<Drop> temp = new ArrayList<Drop>();
    		temp.addAll(possibleDrops);
    		final Map<Integer,Integer> itemWon = new TreeMap<Integer,Integer>();
    		for(final Drop D : temp)
    		{
    			final int t = (int)(D.getRate()*100);//Permet de gerer des taux>0.01
    			final int jet = Formulas.getRandomValue(0, 100*100);
    			if(jet < t)
    			{
    				final ItemTemplate OT = World.getItemTemplate(D.getItemID());
    				if(OT == null)continue;
    				//on ajoute a la liste
    				itemWon.put(OT.getID(),(itemWon.get(OT.getID())==null?0:itemWon.get(OT.getID()))+1);
    				
    				D.setMax(D.getMax()-1);
    				if(D.getMax() == 0)possibleDrops.remove(D);
    			}
    		}
    		for(final Entry<Integer,Integer> entry : itemWon.entrySet())
    		{
    			final ItemTemplate OT = World.getItemTemplate(entry.getKey());
    			if(OT == null)continue;
    			if(drops.length() >0)drops.append(',');
    			drops.append(entry.getKey()).append('~').append(entry.getValue());
    			final Item obj = OT.createNewItem(entry.getValue(), false);
    			p.addItem(obj);
    			World.addItem(obj, true);
    		}
    		Packet.append(drops.toString()).append(';');//Drop
			Packet.append(winkamas).append('|');
			
			SQLManager.UPDATE_PERCEPTEUR(p);
		}
        return Packet.toString();
    }
	
	/*public String GetGE(int win)
    {
		long time = System.currentTimeMillis() - _startTime;

		int initGUID = _init0.getGUID();		
		int type = Constants.FIGHT_TYPE_CHALLENGE;// toujours 0
		if(_type == Constants.FIGHT_TYPE_AGRESSION)//Sauf si gain d'honneur
			type = _type;
		//FIXME : Etoiles de combat
        StringBuilder Packet = new StringBuilder();
        Packet.append("GE").append(time).append("|").append(initGUID).append("|").append(type).append("|");
        ArrayList<Fighter> TEAM1 = new ArrayList<Fighter>();
        ArrayList<Fighter> TEAM2 = new ArrayList<Fighter>();
        
        if(win == 1)
        {
        	TEAM1.addAll(_team0.values());
        	TEAM2.addAll(_team1.values());
        }
        else
        {
        	TEAM1.addAll(_team1.values());
        	TEAM2.addAll(_team0.values());
        }
        //Calculs des niveaux de groupes
        /*int TEAM1lvl = 0;
        int TEAM2lvl = 0;
        for(Fighter F : TEAM1)
        {
        	if(F.isInvocation())continue;
        	TEAM1lvl += F.getLvl();
        }
        for(Fighter F : TEAM2)
        {
        	if(F.isInvocation())continue;
        	TEAM2lvl += F.getLvl();
        }*/
        //fin
        /* DEBUG
        System.out.println("TEAM1: lvl="+TEAM1lvl);
        System.out.println("TEAM2: lvl="+TEAM2lvl);
        ///
        //DROP SYSTEM
        //Calcul de la PP de groupe
        int groupPP = 0,minkamas = 0,maxkamas = 0;
        for(Fighter F : TEAM1)
        {
        	if(!F.isInvocation() || (F.getMob() != null && F.getMob().getTemplate().getID() == 258))
        		groupPP += F.getTotalStats().getEffect(Constants.STATS_ADD_PROS);
        }
        if(groupPP <0)groupPP =0;
        //Calcul des drops possibles
        ArrayList<Drop> possibleDrops = new ArrayList<Drop>();


        for(Fighter F : TEAM2)
        {
        	if(F.isInvocation() || F.getMob() == null)continue;
        	minkamas += F.getMob().getTemplate().getMinKamas();
        	maxkamas += F.getMob().getTemplate().getMaxKamas();
        	for(Drop D : F.getMob().getDrops())
        	{
        		if(D.getMinProsp() <= groupPP)
        		{
        			//On augmente le taux en fonction de la PP
        			int taux = (int)((groupPP * D.getRate()*Ancestra.CONFIG_DROP)/100);
        			possibleDrops.add(new Drop(D.getItemID(),0,taux,D.getMax()));
        		}
        	}

        }


        //On Réordonne la liste en fonction de la PP
        ArrayList<Fighter> Temp = new ArrayList<Fighter>();
        Fighter curMax = null;
        while(Temp.size() < TEAM1.size())
        {
        	int curPP = -1;
        	for(Fighter F : TEAM1)
        	{
        		//S'il a plus de PP et qu'il n'est pas listé
        		if(F.getTotalStats().getEffect(Constants.STATS_ADD_PROS) > curPP && !Temp.contains(F))
        		{
        			curMax = F;
        			curPP = F.getTotalStats().getEffect(Constants.STATS_ADD_PROS);
        		}
        	}
        	Temp.add(curMax);
        }
        //On enleve les invocs
        TEAM1.clear();
        TEAM1.addAll(Temp);
        /* DEBUG
	        System.out.println("DROP: PP ="+groupPP);
	        System.out.println("DROP: nbr="+possibleDrops.size());
	        System.out.println("DROP: Kam="+totalkamas);
	        ///
        //FIN DROP SYSTEM
        //XP SYSTEM
        long totalXP = 0;
        for(Fighter F : TEAM2)
        {
        	if(F.isInvocation() || F.getMob() == null)continue;
        	totalXP += F.getMob().getBaseXp();
        }
        /* DEBUG
	    System.out.println("TEAM1: xpTotal="+totalXP);
	    ///
        //FIN XP SYSTEM

        //Capture d'âmes
        boolean mobCapturable = true;
        for(Fighter F : TEAM2)
        {
        	try
        	{
        		mobCapturable &= F.getMob().getTemplate().isCapturable();
        	}catch (Exception e) {
        		mobCapturable = false;
        		break;
        	}
        }
        isCapturable |= mobCapturable;

        if(isCapturable)
        {
        	boolean isFirst = true;
        	int maxLvl = 0;
        	StringBuilder pierreStats = new StringBuilder();


        	for(Fighter F : TEAM2)	//Création de la pierre et verifie si le groupe peut être capturé
        	{
        		if(!isFirst)
        			pierreStats.append("|");

        		pierreStats.append(F.getMob().getTemplate().getID()).append(",").append(F.getLvl());//Converti l'ID du monstre en Hex et l'ajoute au stats de la futur pierre d'âme

        		isFirst = false;

        		if(F.getLvl() > maxLvl)	//Trouve le monstre au plus haut lvl du groupe (pour la puissance de la pierre)
        			maxLvl = F.getLvl();
        	}
        	pierrePleine = new SoulStone(World.getNewItemGuid(),1,7010,Constants.ITEM_POS_NO_EQUIPED,pierreStats.toString());	//Crée la pierre d'âme

        	for(Fighter F : TEAM1)	//Récupère les captureur
        	{
        		if(!F.isInvocation() && F.isState(Constants.ETAT_CAPT_AME))
        			_captureur.add(F);
        	}

        	if(_captureur.size() > 0 && !World.isArenaMap(getMap().get_id()))	//S'il y a des captureurs
        	{
        		for (int i = 0; i < _captureur.size(); i++)
        		{
        			try
        			{
        				Fighter f = _captureur.get(Formulas.getRandomValue(0, _captureur.size()-1));	//Récupère un captureur au hasard dans la liste
        				if(!(f.getPlayer().getObjetByPos(Constants.ITEM_POS_ARME).getTemplate().getType() == Constants.ITEM_TYPE_PIERRE_AME)) //Avoir une pierre d'ame vide d'équipé
        				{
        					_captureur.remove(f);
        					continue;
        				}
        				Couple<Integer,Integer> pierreJoueur = Formulas.decompSoulStone(f.getPlayer().getObjetByPos(Constants.ITEM_POS_ARME));//Récupère les stats de la pierre équippé

        				if(pierreJoueur.second < maxLvl)	//Si la pierre est trop faible
        				{
        					_captureur.remove(f);
        					continue;
        				}

        				int captChance = Formulas.totalCaptChance(pierreJoueur.first, f.getPlayer());

        				if(Formulas.getRandomValue(1, 100) <= captChance)	//Si le joueur obtiens la capture
        				{
        					//Retire la pierre vide au personnage et lui envoie ce changement
        					int pierreVide = f.getPlayer().getObjetByPos(Constants.ITEM_POS_ARME).getGuid();
        					f.getPlayer().deleteItem(pierreVide);
        					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(f.getPlayer(), pierreVide);

        					captWinner = f._id;
        					break;
        				}
        			}
        			catch(NullPointerException e)
        			{
        				continue;
        			}
        		}
        	}
        }

        //Fin Capture

        for(Fighter i : TEAM1)
        {
        	if(type == Constants.FIGHT_TYPE_CHALLENGE)
        	{
        		if(i.isInvocation() && i.getMob() != null && i.getMob().getTemplate().getID() != 258)continue;
        		long winxp 	= Formulas.getXpWinPvm2(i,TEAM1,TEAM2,totalXP);
        		AtomicReference<Long> XP = new AtomicReference<Long>();
        		XP.set(winxp);

        		long guildxp = Formulas.getGuildXpWin(i,XP);
        		long mountxp = 0;
        		if(i.getPlayer() != null && i.getPlayer().isOnMount())
        		{
        			mountxp = Formulas.getMountXpWin(i,XP);
        			i.getPlayer().getMount().addXp(mountxp);
        			SocketManager.GAME_SEND_Re_PACKET(i.getPlayer(),"+",i.getPlayer().getMount());
        		}
        		int winKamas = Formulas.getKamasWin(i,TEAM1,minkamas,maxkamas);
        		StringBuilder drops = new StringBuilder();
        		//Drop system
        		ArrayList<Drop> temp = new ArrayList<Drop>();
        		temp.addAll(possibleDrops);
        		Map<Integer,Integer> itemWon = new TreeMap<Integer,Integer>();

        		for(Drop D : temp)
        		{
        			int t = (int)(D.getRate()*100);//Permet de gerer des taux>0.01
        			int jet = Formulas.getRandomValue(0, 100*100);
        			if(jet < t)
        			{
        				ItemTemplate OT = World.getObjTemplate(D.getItemID());
        				if(OT == null)continue;
        				//on ajoute a la liste
        				itemWon.put(OT.getID(),(itemWon.get(OT.getID())==null?0:itemWon.get(OT.getID()))+1);

        				D.setMax(D.getMax()-1);
        				if(D.getMax() == 0)possibleDrops.remove(D);
        			}
        		}

        		if(i._id == captWinner)	//S'il à capturé le groupe
        		{
        			if(drops.length() >0)drops.append(",");
        			drops.append(pierrePleine.getTemplate().getID()).append("~").append(1);
        			if(i.getPlayer().addObjet(pierrePleine, false))
        				World.addObjet(pierrePleine, true);
        			i.getPlayer().objetLog(pierrePleine.getTemplate().getID(), 1, "Capturé");
        		}

        		for(Entry<Integer,Integer> entry : itemWon.entrySet())
        		{
        			ItemTemplate OT = World.getObjTemplate(entry.getKey());
        			if(OT == null)continue;
        			if(drops.length() >0)drops.append(",");
        			drops.append(entry.getKey()).append("~").append(entry.getValue());
        			Item obj = OT.createNewItem(entry.getValue(), false);
        			if(i.getPlayer().addObjet(obj, true))
        				World.addObjet(obj, true);
        			else if (i.isInvocation() && i.getMob().getTemplate().getID() == 285 
        					&& i.getInvocator().getPlayer().addObjet(obj, true)) 
        				World.addObjet(obj, true);
        			i.getPlayer().objetLog(obj.getTemplate().getID(), obj.getQuantity(), "Droppé");
        		}

        		//fin drop system
        		winxp = XP.get();
        		if(winxp != 0 && i.getPlayer() != null)
        			i.getPlayer().addXp(winxp);
        		if(winKamas != 0 && i.getPlayer() != null)
        		{
        			i.getPlayer().addKamas(winKamas);
        			i.getPlayer().kamasLog(winKamas+"", "Gagnés dans un combat");
        		} else if(i.isInvocation() && i.getMob().getTemplate().getID() == 285 && i.getInvocator().getPlayer() != null) 
        		{
        			i.getInvocator().getPlayer().addKamas(winKamas);
        			i.getInvocator().getPlayer().kamasLog(winKamas+"", "Gagnés dans un combat");
        		}
        		if(guildxp > 0 && i.getPlayer().getGuildMember() != null)
        			i.getPlayer().getGuildMember().giveXpToGuild(guildxp);


        		Packet.append("2;").append(i.getGUID()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ?  "1" : "0" )).append(";");
        		Packet.append(i.xpString(";")).append(";");
        		Packet.append((winxp == 0?"":winxp)).append(";");
        		Packet.append((guildxp == 0?"":guildxp)).append(";");
        		Packet.append((mountxp == 0?"":mountxp)).append(";");
        		Packet.append(drops).append(";");//Drop
        		Packet.append((winKamas == 0?"":winKamas)).append("|");
        	}else
        	{
        		//Calcul honeur
        		int winH = Formulas.calculHonorWin(TEAM1,TEAM2,i);
        		int winD = Formulas.calculDeshonorWin(TEAM1,TEAM2,i);

        		Player P = i.getPlayer();
        		if(P.get_honor()+winH<0)winH = -P.get_honor();
        		P.addHonor(winH);
        		P.setDeshonor(P.getDeshonor()+winD);
        		Packet.append("2;").append(i.getGUID()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ?  "1" : "0" )).append(";");
        		Packet.append((P.get_align()!=Constants.ALIGNEMENT_NEUTRE?World.getExpLevel(P.getGrade()).pvp:0)).append(";");
        		Packet.append(P.get_honor()).append(";");
        		int maxHonor = World.getExpLevel(P.getGrade()+1).pvp;
        		if(maxHonor == -1)maxHonor = World.getExpLevel(P.getGrade()).pvp;
        		Packet.append((P.get_align()!=Constants.ALIGNEMENT_NEUTRE?maxHonor:0)).append(";");
        		Packet.append(winH).append(";");
        		Packet.append(P.getGrade()).append(";");
        		Packet.append(P.getDeshonor()).append(";");
        		Packet.append(winD);
        		Packet.append(";;0;0;0;0;0|");
        	}
        }
        for(Fighter i : TEAM2)
        {
        	if(i.isInvocation() && i.getMob().getTemplate().getID() != 285)continue;//On affiche pas les invocs
        	if(_type != Constants.FIGHT_TYPE_AGRESSION)
        		Packet.append("0;").append(i.getGUID()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.getPDV()==0 ?  "1" : "0" )+";"+i.xpString(";")).append(";;;;|");
        	else
        	{
        		//Calcul honeur
        		int winH = Formulas.calculHonorWin(TEAM1,TEAM2,i);
        		int winD = Formulas.calculDeshonorWin(TEAM1,TEAM2,i);

        		Player P = i.getPlayer();
        		if(P.get_honor()+winH<0)winH = P.get_honor();
        		P.delHonor(winH);
        		if(P.getDeshonor()+winD<0)winD = -P.getDeshonor();
        		if(P.getDeshonor()+winD>100)winD = 100-P.getDeshonor();
        		P.setDeshonor(P.getDeshonor()+winD);
        		Packet.append("0;").append(i.getGUID()).append(";").append(i.getPacketsName()).append(";").append(i.getLvl()).append(";").append((i.isDead() ? "1" : "0" )).append(";");
        		Packet.append((P.get_align()!=Constants.ALIGNEMENT_NEUTRE?World.getExpLevel(P.getGrade()).pvp:0)).append(";");
        		Packet.append(P.get_honor()).append(";");
        		int maxHonor = World.getExpLevel(P.getGrade()+1).pvp;
        		if(maxHonor == -1)maxHonor = World.getExpLevel(P.getGrade()).pvp;
        		Packet.append((P.get_align()!=Constants.ALIGNEMENT_NEUTRE?maxHonor:0)).append(";");
        		Packet.append(winH*-1).append(";");
        		Packet.append(P.getGrade()).append(";");
        		Packet.append(P.getDeshonor()).append(";");
        		Packet.append(winD);
        		Packet.append(";;0;0;0;0;0|");
        	}
        }
        if(_map.getPercepteur() != null && _type == Constants.FIGHT_TYPE_PVM)
		{
			TaxCollector p = _map.getPercepteur();
			long winxp 	= (int)Math.floor(Formulas.getXpWinPerco(p,TEAM1,TEAM2,totalXP)/100);
			long winkamas 	= (int)Math.floor(Formulas.getKamasWinPerco(minkamas,maxkamas)/100);
			p.setXp(p.getXp()+winxp);
			p.setKamas(p.getKamas()+winkamas);
			Guild G = p.getGuild();
			Packet.append("5;").append(p.getID()).append(";").append(p.getNom1()).append(",").append(p.getNom2()).append(";").append(G.get_lvl()).append(";0;");
			Packet.append(G.get_lvl()).append(";");
			Packet.append(G.get_xp()).append(";");
			Packet.append(World.getGuildXpMax(G.get_lvl())).append(";");
			Packet.append(";");//XpGagner
			Packet.append(winxp).append(";");//XpGuilde
			Packet.append(";");//Monture
			
			StringBuilder drops = new StringBuilder();
    		ArrayList<Drop> temp = new ArrayList<Drop>();
    		temp.addAll(possibleDrops);
    		Map<Integer,Integer> itemWon = new TreeMap<Integer,Integer>();
    		for(Drop D : temp)
    		{
    			int t = (int)(D.getRate()*100);//Permet de gerer des taux>0.01
    			int jet = Formulas.getRandomValue(0, 100*100);
    			if(jet < t)
    			{
    				ItemTemplate OT = World.getObjTemplate(D.getItemID());
    				if(OT == null)continue;
    				//on ajoute a la liste
    				itemWon.put(OT.getID(),(itemWon.get(OT.getID())==null?0:itemWon.get(OT.getID()))+1);
    				
    				D.setMax(D.getMax()-1);
    				if(D.getMax() == 0)possibleDrops.remove(D);
    			}
    		}
    		for(Entry<Integer,Integer> entry : itemWon.entrySet())
    		{
    			ItemTemplate OT = World.getObjTemplate(entry.getKey());
    			if(OT == null)continue;
    			if(drops.length() >0)drops.append(",");
    			drops.append(entry.getKey()).append("~").append(entry.getValue());
    			Item obj = OT.createNewItem(entry.getValue(), false);
    			p.addObjet(obj);
    			World.addObjet(obj, true);
    		}
    		Packet.append(drops.toString()).append(";");//Drop
			Packet.append(winkamas).append("|");
			
			SQLManager.UPDATE_PERCEPTEUR(p);
		}
        return Packet.toString();
    }*/
    
	/*public void verifIfTeamAllDead()
	{
		if(_state >=Constants.FIGHT_STATE_FINISHED)return;
		boolean team0 = true;
		boolean team1 = true;
		for(Entry<Integer,Fighter> entry : _team0.entrySet())
		{
			if(entry.getValue().isInvocation())continue;
			if(!entry.getValue().isDead())
			{
				team0 = false;
				break;
			}
		}
		for(Entry<Integer,Fighter> entry : _team1.entrySet())
		{
			if(entry.getValue().isInvocation())continue;
			if(!entry.getValue().isDead())
			{
				team1 = false;
				break;
			}
		}
		if(team0 || team1 || !verifyStillInFight())
		{
			_state = Constants.FIGHT_STATE_FINISHED;
			int winner = team0?2:1;
			GameServer.addToLog("L'equipe "+winner+" gagne !");
			_turnTimer.stop();
			//On despawn tous le monde
			_curPlayer = -1;
			for(Entry<Integer, Fighter> entry : _team0.entrySet())
			{
				SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_map, entry.getValue().getGUID());
			}
			for(Entry<Integer, Fighter> entry : _team1.entrySet())
			{
				SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(_map, entry.getValue().getGUID());
			}
			this._init0.getPlayer().get_curCarte().removeFight(this._id);
			SocketManager.GAME_SEND_FIGHT_GE_PACKET_TO_FIGHT(this,7,winner);
			
			for(Entry<Integer, Fighter> entry : _team0.entrySet())
			{
				Player perso = entry.getValue().getPlayer();
				if(perso == null)
					continue;
				perso.set_duelID(-1);
				perso.set_ready(false);
				perso.set_fight(null);
				if(_type==Constants.FIGHT_TYPE_CHALLENGE)
					perso.set_PDV(entry.getValue().getPdvBeforeFight());
			}
			switch(_type)
			{
				
				case Constants.FIGHT_TYPE_AGRESSION://Aggro
				case Constants.FIGHT_TYPE_CHALLENGE://Défie
				for(Entry<Integer, Fighter> entry : _team1.entrySet())
				{
					Player perso = entry.getValue().getPlayer();
					if(perso == null)
						continue;
					perso.set_duelID(-1);
					perso.set_ready(false);
					perso.set_fight(null);
					if(_type == Constants.FIGHT_TYPE_CHALLENGE)
						perso.set_PDV(entry.getValue().getPdvBeforeFight());
				}
				break;
				case Constants.FIGHT_TYPE_PVM://PvM
					if(_team1.get(-1) == null)return;				
				break;
			}
			
			//on vire les spec du combat
			for(Player perso: _spec.values())
			{
				perso.refreshMapAfterFight();
			}
			_spec.clear();
			
			DofusMap map = World.getCarte(_map.get_id());		
			map.removeFight(_id);
			SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(map);
			_map = null;
			_ordreJeu = null;
			ArrayList<Fighter> winTeam = new ArrayList<Fighter>();
			ArrayList<Fighter> looseTeam = new ArrayList<Fighter>();
			if(team0)
			{
				looseTeam.addAll(_team0.values());
				winTeam.addAll(_team1.values());
			}
			else
			{
				winTeam.addAll(_team0.values());
				looseTeam.addAll(_team1.values());
			}
			try
			{
				Thread.sleep(1200);
			}catch(Exception E){};
			//Pour les gagnants, on active les endFight actions
			for(Fighter F : winTeam)
			{
				Player player = F.getPlayer();
				if(F.hasLeft())continue;
				if(player == null)continue;
				if(F.isInvocation())continue;
				if(!player.isOnline())continue;
				player.set_PDV(F.getPDV());
				try
				{
					Thread.sleep(200);
				}catch(Exception E){};
				
				if(player.get_questList().size() >0) player.applyQuest(looseTeam);
				if(_type != Constants.FIGHT_TYPE_CHALLENGE) player.refreshSpecialItems();
				player.get_curCarte().applyEndFightAction(_type, player);
				player.refreshMapAfterFight();
			}
			//Pour les perdant ont TP au point de sauvegarde
			for(Fighter F : looseTeam)
			{
				Player player = F.getPlayer();
				if(F.hasLeft())continue;
				if(player == null)continue;
				if(F.isInvocation())continue;
				if(!player.isOnline())continue;
				try
				{
					Thread.sleep(200);
				}catch(Exception E){};
				if(_type == Constants.FIGHT_TYPE_CHALLENGE)
				{
					player.set_PDV(F.getPdvBeforeFight());
					player.refreshMapAfterFight();
					continue;
				}
				player.loseEnergy(Formulas.getEnergyToLose(player, _type));
			}
		}
	}*/
	
	public synchronized void verifIfTeamAllDead()
	{
		if(_state >=Constants.FIGHT_STATE_FINISHED)return;
		boolean team0 = true;
		boolean team1 = true;
		
		for(final Entry<Integer,Fighter> entry : getTeam0().entrySet())
		{
			if(entry.getValue().isInvocation())continue;
			if(!entry.getValue().isDead())
			{
				team0 = false;
				break;
			}
		}
		
		for(final Entry<Integer,Fighter> entry : getTeam1().entrySet())
		{
			if(entry.getValue().isInvocation())continue;
			if(!entry.getValue().isDead())
			{
				team1 = false;
				break;
			}
		}
		
		if(team0 || team1 || !verifyStillInFight())
		{
			_state = Constants.FIGHT_STATE_FINISHED;
			final int winner = team0?2:1;
			Log.addToLog("L'equipe "+winner+" gagne !");
			_turnTimer.stop();
			//On despawn tous le monde
			_curPlayer = -1;
			for(final Fighter fighter : getFighters(7))
			{
				final Player player = fighter.getPlayer();
				if(player == null)
					continue;
				if(fighter.isDisconnected())
				{
					player.resetVars();
					player.setOnline(false);
					SQLManager.SAVE_PERSONNAGE(player, true);
				}
				player.setDuelID(-1);
				player.setReady(false);
				player.setFight(null);
				if(_type == Constants.FIGHT_TYPE_CHALLENGE)
					player.setPDV(fighter.getPdvBeforeFight());
				else
					player.setPDV(fighter.getLife());
			}
			
			final String packetGE = GetGE(winner);
			
			try {
				Thread.sleep(700+(int)((getTeam0().size()+getTeam1().size())*15)); //Le temps d'appliquer à tous les personnages
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			
			SocketManager.GAME_SEND_FIGHT_GE_PACKET_TO_FIGHT(this,7,packetGE);	
			
			//on vire les spec du combat
			for(final Player perso: _spec.values())
			{
				if(perso == null)
					continue;
				perso.refreshMapAfterFight();
			}
			_spec.clear();
			
			final DofusMap map = World.getMap(getMap().getId());		
			map.removeFight(_id);
			SocketManager.GAME_SEND_MAP_FIGHT_COUNT_TO_MAP(map);
			setMap(null);
			_ordreJeu = null;
			
			final ArrayList<Fighter> winTeam = new ArrayList<Fighter>();
			final ArrayList<Fighter> looseTeam = new ArrayList<Fighter>();
			
			if(team0)
			{
				looseTeam.addAll(getTeam0().values());
				winTeam.addAll(getTeam1().values());
			}
			else
			{
				winTeam.addAll(getTeam0().values());
				looseTeam.addAll(getTeam1().values());
			}
			
			try
			{
				Thread.sleep(1200);
			}catch(final Exception E){};
			//Pour les gagnants, on active les endFight actions
			
			if(winTeam.get(0).isMob() && !winTeam.get(0).isInvocation())
			{
				map.addMobGroup(getMobGroup());
			}
			for(final Fighter F : winTeam)
			{
				final TaxCollector taxCollector = F.getTaxCollector();
				if(taxCollector != null)
				{
					CopyOnWriteArrayList<Player> defenders = null;
					if(!taxCollector.getDefenders().isEmpty())
					{
						defenders = new CopyOnWriteArrayList<Player>(taxCollector.getDefenders());
						for(final Player defender : defenders)
						{
							defender.teleportToTempSavePos();
						}
					}
					taxCollector.getAttackers().clear();
					taxCollector.getDefenders().clear();
					taxCollector.setFight(null);
					taxCollector.setEmptyTimer();
					SocketManager.GAME_SEND_GUILD_ON_PERCEPTEUR_SURVIVED(taxCollector);
					SocketManager.GAME_SEND_MAP_PERCEPTEUR_GM_PACKET(World.getMap(taxCollector.getMapId()));
					continue;
				}
				final Player player = F.getPlayer();
				if(F.hasLeft())continue;
				if(player == null)continue;
				if(F.isInvocation())continue;
				if(!player.isOnline())continue;

				if(player.getQuestList().size() >0) 
					player.applyQuest(looseTeam);
				if(_type != Constants.FIGHT_TYPE_CHALLENGE) 
					player.refreshSpecialItems();
				player.refreshMapAfterFight();
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {}
				player.getCurMap().applyEndFightAction(_type, player);
			}
			//Pour les perdant ont TP au point de sauvegarde
			for(final Fighter F : looseTeam)
			{
				final TaxCollector taxCollector = F.getTaxCollector();
				if(taxCollector != null)
				{
					taxCollector.getAttackers().clear();
					taxCollector.getDefenders().clear();
					taxCollector.setFight(null);
					taxCollector.setEmptyTimer();
					SocketManager.GAME_SEND_GUILD_ON_PERCEPTEUR_DIED(taxCollector);
					SQLManager.DELETE_PERCEPTEUR(taxCollector.getActorId());
					continue;
				}
				final Player player = F.getPlayer();
				if(F.hasLeft())continue;
				if(player == null)continue;
				if(F.isInvocation())continue;
				if(!player.isOnline())continue;
				try
				{
					Thread.sleep(250);
				}catch(final InterruptedException E){};
				if(_type == Constants.FIGHT_TYPE_CHALLENGE)
				{
					player.refreshMapAfterFight();
					continue;
				}
				player.loseEnergy(Formulas.getEnergyToLose(player.getLvl(), _type));
				if(_type == Constants.FIGHT_TYPE_PVM && player.getObjetByPos(8) != null)
					World.getPet(player.getObjetByPos(8).getGuid()).loseLife();
				if(_type == Constants.FIGHT_TYPE_AGRESSION && player.isShowingWings())
				{
					final byte playerAlign = player.getAlign();
					final int subAreaAlign = player.getCurMap().getSubArea().getAlignement();
					if(playerAlign == 1 || playerAlign == 2 && subAreaAlign == 1 || subAreaAlign == 2)
					{
						if(playerAlign != subAreaAlign)
						{
							if(playerAlign == 1)
								player.teleport(6171, 383);
							else
								player.teleport(6164, 252);
						}
					}
				}
			}
		}
	}


	public void onFighterDie(final Fighter target) 
	{
		target.setDead(true);
		SocketManager.GAME_SEND_FIGHT_PLAYER_DIE_TO_FIGHT(this,7,target.getGUID());
		target.getFightCell().getFighters().clear();
		
		if(!_deadList.containsKey(target.getGUID()))
			_deadList.put(target.getGUID(), target);
			
		if(target.getTeam() == 0)
		{
			final TreeMap<Integer,Fighter> team = new TreeMap<Integer,Fighter>();
			team.putAll(getTeam0());
			for(final Entry<Integer,Fighter> entry : team.entrySet())
			{
				final Fighter curFighter = entry.getValue();
				if(curFighter.getInvocator() == null)continue;
				if(curFighter.getLife() == 0)continue;
				if(curFighter.isDead())continue;
				if(curFighter.getInvocator().getGUID() == target.getGUID())//si il a été invoqué par le joueur mort
				{
					onFighterDie(curFighter);
					
					/*final int index = _ordreJeu.indexOf(curFighter);
					if(index != -1)_ordreJeu.remove(index);
					
					if(getTeam0().containsKey(curFighter.getGUID()))
						getTeam0().remove(curFighter.getGUID());
					else if (getTeam1().containsKey(curFighter.getGUID()))
						getTeam1().remove(curFighter.getGUID());
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 999, target.getGUID()+"", getGTL());*/
				}
			}
		}else if(target.getTeam() == 1)
		{
			final TreeMap<Integer,Fighter> team = new TreeMap<Integer,Fighter>();
			team.putAll(getTeam1());
			for(final Entry<Integer,Fighter> entry : team.entrySet())
			{
				final Fighter curFighter = entry.getValue();
				if(curFighter.getInvocator() == null)continue;
				if(curFighter.getLife() == 0)continue;
				if(curFighter.isDead())continue;
				if(curFighter.getInvocator().getGUID() == target.getGUID())//si il a été invoqué par le joueur mort
				{
					onFighterDie(curFighter);
					
					/*final int index = _ordreJeu.indexOf(curFighter);
					if(index != -1)_ordreJeu.remove(index);		
					
					if(getTeam0().containsKey(curFighter.getGUID()))
						getTeam0().remove(curFighter.getGUID());
					else if (getTeam1().containsKey(curFighter.getGUID()))
						getTeam1().remove(curFighter.getGUID());
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 999, target.getGUID()+"", getGTL());*/
				}
			}
		}
		if(target.getMob() != null)
		{
			//Si c'est une invocation, on la retire de la liste
			try
			{
				boolean isStatic = false;
				for(final int id : Constants.STATIC_INVOCATIONS)if(id == target.getMob().getTemplate().getID())isStatic = true;
				if(target.isInvocation() && !isStatic)
				{
					target.getInvocator().decrNumbInvocations();
					//Il ne peut plus jouer, et est mort on revient au joueur précedent pour que le startTurn passe au suivant
					if(!target.canPlay() && _ordreJeu.get(_curPlayer).getGUID() == target.getGUID())
					{
						_curPlayer--;
					}
					//Il peut jouer, et est mort alors on passe son tour pour que l'autre joue, puis on le supprime de l'index sans problèmes
					if(target.canPlay() && _ordreJeu.get(_curPlayer).getGUID() == target.getGUID())
					{
	    				endTurn();
					}
					final int index = _ordreJeu.indexOf(target);
					//Si le joueur courant a un index plus élevé, on le diminue pour éviter le outOfBound
					if(index != -1) {
						if(_curPlayer > index && _curPlayer > 0)
							_curPlayer--;
						_ordreJeu.remove(index);
					}
					if(getTeam0().containsKey(target.getGUID()))getTeam0().remove(target.getGUID());
					else if (getTeam1().containsKey(target.getGUID()))getTeam1().remove(target.getGUID());
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 999, target.getGUID()+"", getGTL());
					mobsAppendEmote(true);
					/*if(target.canPlay() && killer.getGUID() == target.getGUID()) {
						_curAction = "";
						//endTurn();
					}
					Thread.sleep(500);*/
				}
			}catch(final Exception e){e.printStackTrace();};
		}
		//on supprime les glyphes du joueur
		final ArrayList<Glyph> glyphs = new ArrayList<Glyph>();//Copie du tableau
		glyphs.addAll(_glyphs);
		for(final Glyph g : glyphs)
		{
			//Si c'est ce joueur qui l'a lancé
			if(g.getCaster().getGUID() == target.getGUID())
			{
				SocketManager.GAME_SEND_GDZ_PACKET_TO_FIGHT(this, 7, "-", g.getCell().getId(), g.getSize(), 4);
				SocketManager.GAME_SEND_GDC_PACKET_TO_FIGHT(this, 7, g.getCell().getId());
				_glyphs.remove(g);
			}
		}
		
		//on supprime les pieges du joueur
		final ArrayList<Trap> Ps = new ArrayList<Trap>();
		Ps.addAll(_traps);
		for(final Trap p : Ps)
		{
			if(p.getCaster().getGUID() == target.getGUID())
			{
				p.disappear();
				_traps.remove(p);
			}
		}
		for(Fighter fighter : getFighters(3))
		{
			Map<Integer, SpellEffect> newBuffs = new TreeMap<Integer, SpellEffect>();
			
			for(Entry<Integer, SpellEffect> entry : fighter.getFightBuff().entrySet())
			{
				int casterId = entry.getKey();
				SpellEffect SE = entry.getValue();
				if(casterId == SE.getCaster().getGUID() && casterId == target.getGUID())
					continue;
				newBuffs.put(casterId, SE);
			}
			
			fighter.getFightBuff().clear();
			fighter.getFightBuff().putAll(newBuffs);
		}
		try {
			Thread.sleep(500);
		} catch (final InterruptedException e) {};
		verifIfTeamAllDead();
	}

	public void mobsAppendEmote(final boolean loseMember) 
	{
		if(_type != Constants.FIGHT_TYPE_PVM)
			return;
		final int[] emotes = {1, 3, 4, 6, 7, 11, 12, 14, 15};
		int chance = Formulas.getRandomValue(1, 45);
		for(int index = 0; index < emotes.length; ++index)
		{
			if(chance < emotes[index])
			{
				for(final Fighter mob : getFighters(2))
				{
					if(!mob.isMob() || mob.isDead())
						continue;
					chance = Formulas.getRandomValue(1, 15);
					if(chance < emotes[index])
					{
						SocketManager.GAME_SEND_MOB_EMOTE(mob.getGUID(), getFighters(7), loseMember);
					}
				}
			}
		}
	}

	public int getTeamID(final int guid)
	{
		if(getTeam0().containsKey(guid))
			return 1;
		if(getTeam1().containsKey(guid))
			return 2;
		if(_spec.containsKey(guid))
			return 4;
		return -1;
	}

	public int getEnemyTeamID(final int guid)
	{
		if(getTeam0().containsKey(guid))
			return 2;
		if(getTeam1().containsKey(guid))
			return 1;
		return -1;
	}
	
	public void tryCaC(final Player perso, final int cellID)
	{
		final Fighter caster = getFighterByPerso(perso);
		
		if(caster == null)return;
		
		if(_ordreJeu.get(_curPlayer).getGUID() != caster.getGUID())//Si ce n'est pas a lui de jouer
			return;
		
		if(perso.getObjetByPos(Constants.ITEM_POS_ARME) == null)//S'il n'a pas de CaC
		{
			tryCastSpell(caster, World.getSpell(0).getStatsByLevel(1), cellID);
			/*if(_curFighterPA < 4)//S'il n'a pas assez de PA
				return;
			SocketManager.GAME_SEND_GAS_PACKET_TO_FIGHT(this, 7, perso.getGUID());
			//Si le joueur est invisible
			if(caster.isHide())caster.unHide();
			
			Fighter target = _map.getCase(cellID).getFirstFighter();
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 303, perso.getGUID()+"", cellID+"");
			
			if(target != null)
			{
				int dmg = Formulas.getRandomJet("1d5+0");
				int finalDommage = Formulas.calculFinalDamage(this,caster, target,Constants.ELEMENT_NEUTRE, dmg,false);
				
				finalDommage = SpellEffect.applyOnHitBuffs(finalDommage,target,caster,this);//S'il y a des buffs spéciaux
				
				if(finalDommage>target.getPDV())finalDommage = target.getPDV();//Target va mourrir
				target.removePDV(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
			}
			_curFighterPA-= 4;
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102,perso.getGUID()+"",perso.getGUID()+",-4");
			SocketManager.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, perso.getGUID());*/
			
			/*if(target.getPDV() <=0)
				onFighterDie(target);
			verifIfTeamAllDead();*/
		}else
		{
			final Item arme = perso.getObjetByPos(Constants.ITEM_POS_ARME);
			
			final int dist = Pathfinding.getDistanceBetween(getMap(), caster.getFightCell().getId(), cellID);
			final int MaxPO = arme.getTemplate().getPOmax();
			final int MinPO = arme.getTemplate().getPOmin();
			if(dist < MinPO || dist > MaxPO)
			{
				//TODO: Y'a t'il un IM ?
				return;
			}
			//Pierre d'âmes = EC
			if(arme.getTemplate().getType() == 83)
			{
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 305, perso.getActorId()+"", "");//Echec Critique Cac
				SocketManager.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, perso.getActorId());//Fin de l'action
				try{
					Thread.sleep(500);
				}catch(final Exception e){}
				endTurn();
			}
			
			final int PACost = arme.getTemplate().getPACost();
			
			if(_curFighterPA < PACost)//S'il n'a pas assez de PA
			{
				
				return;
			}
			SocketManager.GAME_SEND_GAS_PACKET_TO_FIGHT(this, 7, perso.getActorId());
			final boolean isEc = arme.getTemplate().getTauxEC() != 0 && Formulas.getRandomValue(1, arme.getTemplate().getTauxEC()) == arme.getTemplate().getTauxEC();
			if(isEc)
			{
				Log.addToLog(perso.getName()+" Echec critique sur le CaC ");
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 305, perso.getActorId()+"", "");//Echec Critique Cac
				SocketManager.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, perso.getActorId());//Fin de l'action
				endTurn();
			}else
			{
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 303, perso.getActorId()+"", cellID+"");
				final boolean isCC = caster.testIfCC(arme.getTemplate().getTauxCC());
				if(isCC)
				{
					Log.addToLog(perso.getName()+" Coup critique sur le CaC");
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 301, perso.getActorId()+"", "0");
				}
				
				//Si le joueur est invisible
				if(caster.isHide())caster.unHide(-1);
				
				ArrayList<SpellEffect> effets = arme.getEffects();
				if(isCC)
				{
					effets = arme.getCritEffects();
				}
				for(final SpellEffect SE : effets)
				{
					if(_state != Constants.FIGHT_STATE_ACTIVE)break;
					final ArrayList<Fighter> cibles = Pathfinding.getCiblesByZoneByWeapon(this,arme.getTemplate().getType(),getMap().getCell(cellID),caster.getFightCell().getId());
					SE.setTurn(0);
					SE.applyToFight(this, caster, cibles, null, true);
				}
				_curFighterPA-= PACost;
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(this, 7, 102,perso.getActorId()+"",perso.getActorId()+",-"+PACost);
				SocketManager.GAME_SEND_GAF_PACKET_TO_FIGHT(this, 7, 0, perso.getActorId());
				verifIfTeamAllDead();
			}
		}
	}
	
	public Fighter getFighterByPerso(final Player perso)
	{
		Fighter fighter = null;
		if(getTeam0().get(perso.getActorId()) != null)
			fighter = getTeam0().get(perso.getActorId());
		if(getTeam1().get(perso.getActorId()) != null)
			fighter = getTeam1().get(perso.getActorId());
		return fighter;
	}

	public Fighter getCurFighter()
	{
		return _ordreJeu.get(_curPlayer);
	}

	public void refreshCurPlayerInfos()
	{
		_curFighterPA = _ordreJeu.get(_curPlayer).getTotalStats().getEffect(Constants.STATS_ADD_PA) - _curFighterUsedPA;
		_curFighterPM = _ordreJeu.get(_curPlayer).getTotalStats().getEffect(Constants.STATS_ADD_PM) - _curFighterUsedPM;
	}

	public synchronized void leftFight(final Player perso, Player target)
	{
		if(perso == null)return;
		Fighter kicker = getFighterByPerso(perso);
		Fighter kicked = null;
		
		if(target != null)
			kicked = getFighterByPerso(target);
				
		if(kicker != null)
		{
			if(_state >= Constants.FIGHT_STATE_ACTIVE)
			{
				onFighterDie(kicker);
				//si le combat est terminé
				if(_state == Constants.FIGHT_STATE_FINISHED)
					return;
				verifIfTeamAllDead();
				//si le combat n'est pas terminé
				if(_state == Constants.FIGHT_STATE_ACTIVE)
				{
					//si c'était a son tour de jouer
					if(_ordreJeu.get(_curPlayer) == null)return;
					if(_ordreJeu.get(_curPlayer).getGUID() == kicker.getGUID())
					{
						endTurn();
					}
				}
				kicker.setLeft(true);
			}
			else if(_state >= Constants.FIGHT_STATE_PLACE)
			{
				boolean canKick = false;
				if(_type != Constants.FIGHT_TYPE_PVT)
				{
					if(_init0 != null && _init0.isPlayer())
					{
						if(kicker.getGUID() == _init0.getGUID())
						{
							canKick = true;
						}
					}
					if(_init1 != null && _init1.isPlayer())
					{
						if(kicker.getGUID() == _init1.getGUID())
						{
							canKick = true;
						}
					}
					if(kicked != null && canKick)
					{
						if(kicked.getTeam() == kicker.getTeam())
						{
							if(kicked.getGUID() != kicker.getGUID())
							{
								SocketManager.GAME_SEND_ERASE_FIGHTER_TO_FIGHT(this, target.getActorId(), 7);
							}
						}
						kicked.setLeft(true);
					}
					SocketManager.GAME_SEND_REMOVE_IN_TEAM_PACKET(_map, _team0.containsKey(kicked.getGUID()) ? _init0.getGUID() : _init1.getGUID(), kicked);
				}
			}
			if(getTeam0().containsKey(kicked.getGUID()))
				getTeam0().remove(kicked.getGUID());
			else if(getTeam1().containsKey(kicked.getGUID()))
				getTeam1().remove(kicked.getGUID());
			kicked.getFightCell().removeFighter(kicked.getGUID());
			SocketManager.GAME_SEND_MAP_FIGHT_FLAG(null, _map);
		}else//Si perso en spec
		{
			_spec.remove(perso.getActorId());
		}
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(getMap(), kicked.getGUID());
		SocketManager.GAME_SEND_GV_PACKET(perso);
		perso.refreshMapAfterFight();
		_map.removeFight(_id);
		SocketManager.GAME_SEND_MAP_FIGHT_COUNT(_map);
		if(target != null)
			Log.addToLog(perso.getName()+" a expulsé "+target.getName());
		else
			Log.addToLog(perso.getName()+" a quitter le combat");
	}
	
	public String getGTL()
	{
		final StringBuilder packet = new StringBuilder(3 + 5 * getListFighter().size());
		packet.append("GTL");
		for(final Fighter f: getListFighter())
		{
			packet.append('|').append(f.getGUID());
		}
		return packet.toString()+(char)0x00;
	}

	public int getNextLowerFighterGuid()
	{
		int g = -1;
		for(final Fighter f : getFighters(3))
		{
			if(f.getGUID() < g)
				g = f.getGUID();
		}
		g--;
		return g;
	}

	public void addFighterInTeam(final Fighter f, final int team)
	{
		if(team == 0)
			getTeam0().put(f.getGUID(), f);
		else if (team == 1)
			getTeam1().put(f.getGUID(), f);
	}

	public String parseFightInfos()
	{
		if(_state >= Constants.FIGHT_STATE_FINISHED)
		{
			getMap().removeFight(_id);
			return "";
		}
		final StringBuilder infos = new StringBuilder(25).append(_id);
		infos.append(';');
		final long time = (new Date().getTime() + 3600000) - (System.currentTimeMillis()-_startTime);
		infos.append((_startTime  == 0?"-1":time)).append(';');
		infos.append("0,");//0 car toujours joueur :)
		switch(_type)
		{
			case Constants.FIGHT_TYPE_CHALLENGE:
				infos.append("0,");
				infos.append(getTeam0().size()).append(';');
				infos.append("0,");
				infos.append("0,").append(',');
				infos.append(getTeam1().size()).append(';');
			break;
			
			case Constants.FIGHT_TYPE_AGRESSION:
				infos.append(getInit0().getPlayer().getAlign()).append(',');
				infos.append(getTeam0().size()).append(';');
				infos.append("0,");
				infos.append(getInit1().getPlayer().getAlign()).append(',');
				infos.append(getTeam1().size()).append(';');
			break;
			
			case Constants.FIGHT_TYPE_PVM:
				infos.append("0,");
				infos.append(getTeam0().size()).append(';');
				infos.append("1,");
				infos.append(getTeam1().get(getTeam1().keySet().toArray()[0]).getMob().getTemplate().getAlign()).append(',');
				infos.append(getTeam1().size()).append(';');
			break;
			
			case Constants.FIGHT_TYPE_PVT:
				infos.append("0,");
				infos.append(getTeam0().size()).append(';');
				infos.append("3,");
				infos.append(getTeam1().get(getTeam1().keySet().toArray()[0]).getMob().getTemplate().getAlign()).append(',');
				infos.append(getTeam1().size()).append(';');
			break;
		}
		return infos.toString();
	}

	public void showCaseToTeam(final int guid, final int cellID)
	{
		final int teams = getTeamID(guid)-1;
		if(teams == 4)return;//Les spectateurs ne montrent pas
		final ArrayList<PrintWriter> PWs = new ArrayList<PrintWriter>();
		if(teams == 0)
		{
			for(final Entry<Integer,Fighter> e : getTeam0().entrySet())
			{
				if(e.getValue().getPlayer() != null)
					PWs.add(e.getValue().getPlayer().getAccount().getGameThread().getOut());
			}
		}
		else if(teams == 1)
		{
			for(final Entry<Integer,Fighter> e : getTeam1().entrySet())
			{
				if(e.getValue().getPlayer() != null)
					PWs.add(e.getValue().getPlayer().getAccount().getGameThread().getOut());
			}
		}
		SocketManager.GAME_SEND_FIGHT_SHOW_CASE(PWs, guid, cellID);
	}
	
	public void showCaseToAll(final int guid, final int cellID)
	{
		final ArrayList<PrintWriter> PWs = new ArrayList<PrintWriter>();
		for(final Entry<Integer,Fighter> e : getTeam0().entrySet())
		{
			Player player = e.getValue().getPlayer();
			if(player != null)
			if(e.getValue().getPlayer() != null)
			if(e.getValue().getPlayer().getAccount() != null)
			if(e.getValue().getPlayer().getAccount().getGameThread() != null)
			if(e.getValue().getPlayer().getAccount().getGameThread().getOut() != null)
			PWs.add(e.getValue().getPlayer().getAccount().getGameThread().getOut());
		}
		for(final Entry<Integer,Fighter> e : getTeam1().entrySet())
		{
			Player player = e.getValue().getPlayer();
			if(player != null)
			if(e.getValue().getPlayer().getAccount() != null)
			if(e.getValue().getPlayer().getAccount().getGameThread() != null)
			if(e.getValue().getPlayer().getAccount().getGameThread().getOut() != null)
			PWs.add(e.getValue().getPlayer().getAccount().getGameThread().getOut());
		}
		for(final Entry<Integer,Player> e : _spec.entrySet())
		{
			Player player = e.getValue();
			if(player != null)
			if(player.getAccount() != null)
			if(player.getAccount().getGameThread() != null)
			if(player.getAccount().getGameThread().getOut() != null)
			PWs.add(player.getAccount().getGameThread().getOut());
		}
		SocketManager.GAME_SEND_FIGHT_SHOW_CASE(PWs, guid, cellID);
	}

	public void joinAsSpect(final Player p)
	{
		if(!specOk  || _state != Constants.FIGHT_STATE_ACTIVE)
		{
			SocketManager.GAME_SEND_Im_PACKET(p, "157");
			return;
		}
		p.getCurCell().removePlayer(p.getActorId());
		SocketManager.GAME_SEND_GJK_PACKET(p, _state, 0, 0, 1, 0, _type);
		SocketManager.GAME_SEND_GS_PACKET(p);
		SocketManager.GAME_SEND_GTL_PACKET(p,this);
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(p.getCurMap(), p.getActorId());
		SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,getMap(),p);
		SocketManager.GAME_SEND_GAMETURNSTART_PACKET(p,_ordreJeu.get(_curPlayer).getGUID(),Config.TIME_BY_TURN);
		_spec.put(p.getActorId(), p);
		p.setFight(this);
		SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this, 7, "036;"+p.getName());
	}
	
	public boolean verifyStillInFight()
	{
		for(final Fighter f : _team0.values())
		{
			if(f.isPerco() && f.isDead())
				return false;
			if(f.isDead() || f.isInvocation())
				continue;
			return true;
		}
		for(final Fighter f : _team1.values())
		{
			if(f.isPerco() && f.isDead())
				return false;
			if(f.isDead() || f.isInvocation())
				continue;
			return true;
		}
		return false;
	}
	
	/*public boolean verifyStillInFight()	//Return true si au moins un joueur est encore dans le combat
	{
		//boolean delFight = false;
		
		for(final Fighter f : getTeam0().values())
		{
			if(f.isPerco()) return true;
			if(!f.isInvocation() || !f.isDead() || f.getPlayer() == null || f.getMob() != null)	//Si c'est un groupe de monstre...
			{
				break;
			}
			if(f.getPlayer() != null && f.getPlayer().getFight() != null && f.getPlayer().getFight().getId() == this.getId()) //Si il n'est plus dans ce combat
			{
				return true;
			}
		}
		for(final Fighter f : getTeam1().values())
		{
			if(f.isPerco()) return true;
			if(!f.isInvocation() && !f.isDead() && f.getPlayer() == null && f.getMob() != null)	//Si c'est un groupe de monstre...
			{
				break;
			}
			if(f.getPlayer().getFight() != null	&& f.getPlayer().getFight().getId() == this.getId()) //Si il n'est plus dans ce combat
			{
				return true;
			}
		}
		
		return false;
	}*/
	
	

	public int getType() {
	    return _type;
    }

	public void disconnectFighter(final Player player) {
		final Fighter fighter = getFighterByPerso(player);
		if(fighter == null)
			return;
		fighter.setDisconnected(true);
		SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this, 7, "1182;"+player.getName()+"~20");
		if(fighter.canPlay())
			endTurn();
	}
	
	public synchronized void reconnectFighter(final Player player) {
		player.getCurCell().removePlayer(player.getActorId());
		SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(player.getCurMap(), player.getActorId());
		SocketManager.GAME_SEND_GJK_PACKET(player, _state, 0, 1, 0, 0, _type);
		SocketManager.GAME_SEND_MAP_FIGHT_GMS_PACKETS(this,getMap(),player);
		SocketManager.GAME_SEND_GIC_PACKETS(this, player);
		SocketManager.GAME_SEND_GS_PACKET(player);
		SocketManager.GAME_SEND_GAMETURNSTART_PACKET(player,_ordreJeu.get(_curPlayer).getGUID(),(int) (29000 - (System.currentTimeMillis() - _startTimeTurn)));
		SocketManager.GAME_SEND_GTL_PACKET(player,this);
		SocketManager.GAME_SEND_GTM_PACKET(player, this);
		try {
			Thread.sleep(200);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		getFighterByPerso(player).setDisconnected(false);
		getFighterByPerso(player).showBuffsNStates();
		SocketManager.GAME_SEND_Im_PACKET_TO_FIGHT(this, 7, "1184;"+player.getName());
	}

	public void setCurFighterPM(final int pm) {
		_curFighterPM = pm;
	}
	
	public void setCurFighterPA(final int pa) {
		_curFighterPA = pa;
	}

	public Fighter getInit0() {
		return _init0;
	}

	public void setInit0(final Fighter _init0) {
		this._init0 = _init0;
	}

	public Fighter getInit1() {
		return _init1;
	}

	public void setInit1(final Fighter _init1) {
		this._init1 = _init1;
	}

	public Map<Integer,Fighter> getTeam0() {
		return _team0;
	}

	public void setTeam0(final Map<Integer,Fighter> _team0) {
		this._team0 = _team0;
	}

	public Map<Integer,Fighter> getTeam1() {
		return _team1;
	}

	public void setTeam1(final Map<Integer,Fighter> _team1) {
		this._team1 = _team1;
	}

	public MonsterGroup getMobGroup() {
		return _mobGroup;
	}

	public void setMobGroup(final MonsterGroup _mobGroup) {
		this._mobGroup = _mobGroup;
	}

	public void setMap(final DofusMap _map) {
		this._map = _map;
	}

	public TaxCollector getTaxCollector() {
		return _taxCollector;
	}

	public void setTaxCollector(final TaxCollector _taxCollector) {
		this._taxCollector = _taxCollector;
	}

	public Map<Integer, Fighter> getDeadList() {
		return _deadList;
	}
	
	public void removeFighterDeadList(final Fighter target) {
		_deadList.remove(target.getGUID());
	}

	public Map<Integer, Integer> getInitialPosition() {
		return _initialPosition;
	}
	
	public int getCurFighterPA() {
		return _curFighterPA;
	}

	public int getCurFighterPM() {
		return _curFighterPM;
	}
}

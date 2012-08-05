package objects.fight;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import objects.character.Player;
import objects.character.Stats;
import objects.guild.TaxCollector;
import objects.map.DofusCell;
import objects.monster.MonsterGrade;
import objects.spell.SpellEffect;
import objects.spell.SpellStat;

import common.Config;
import common.Constants;
import common.SocketManager;
import common.World;
import common.console.Log;
import common.utils.Formulas;

public class Fighter
{
	int _id = 0;
	private boolean _canPlay = false;
	private final Fight _fight;
	private int _type = 0; // 1 Personnage, 2 : Mob
	private MonsterGrade _mob = null;
	private Player _player = null;
	private TaxCollector _taxCollector = null;
	private int _team = -2;
	private DofusCell _cell;
	private final Map<Integer, SpellEffect> _fightBuffs = new TreeMap<Integer, SpellEffect>();
	private final Map<Integer,Integer> _chatiValue = new TreeMap<Integer,Integer>();
	private int _orientation; 
	private Fighter _invocator;
	private int _maxLife;
	private int _life;
	private boolean _isDead;
	private boolean _hasLeft;
	private int _gfxID;
	private final Map<Integer,Integer> _states = new TreeMap<Integer,Integer>();
	private Fighter _isHolding;
	private Fighter _holdedBy;
	private int _pdvBeforeFight;
	private boolean _isDisconnected = false;
	private short _remainingTurns = 20;
	public int _nbInvoc = 0;
	private int _kamasStolen;
	private ArrayList<CooldownSpell> _cooldownSpell = new ArrayList<CooldownSpell>();
	
	public Fighter(final Fight f, final MonsterGrade mob)
	{
		_fight = f;
		_type = 2;
		_mob = mob;
		_id = mob.getInFightID();
		_maxLife = mob.getPDVMAX();
		_life = mob.getPDV();
		_gfxID = getDefaultGfx();
	}
	
	public Fighter(final Fight f, final Player perso)
	{
		_fight = f;
		_type = 1;
		_player = perso;
		_id = perso.getActorId();
		_maxLife = perso.getPDVMAX();
		_life = perso.getPDV();
		_gfxID = getDefaultGfx();
	}
	
	public Fighter(final Fight f, final TaxCollector perco) {
		_fight = f;
		_type = 5;
		_taxCollector = perco;
		_id = -1;
		_maxLife = _taxCollector.getGuild().getLvl()*100;
		_life = _taxCollector.getGuild().getLvl()*100;
		_gfxID = 6000;
    }

	public int getGUID()
	{
		return _id;
	}
	public Fighter getIsHolding() {
		return _isHolding;
	}

	public void setIsHolding(final Fighter isHolding) {
		_isHolding = isHolding;
	}

	public Fighter getHoldedBy() {
		return _holdedBy;
	}

	public void setHoldedBy(final Fighter holdedBy) {
		_holdedBy = holdedBy;
	}

	public int getGfxID() {
		return _gfxID;
	}

	public void setGfxID(final int gfxID) {
		_gfxID = gfxID;
	}

	public Map<Integer, SpellEffect> getFightBuff()
	{
		return _fightBuffs;
	}
	public void setFightCell(final DofusCell cell)
	{
		_cell = cell;
	}
	public boolean isHide()
	{
		return hasBuff(150);
	}
	public DofusCell getFightCell()
	{		
		return _cell;
	}
	public void setTeam(final int i)
	{
		_team = i;
	}
	public boolean isDead() {
		return _isDead;
	}

	public void setDead(final boolean isDead) {
		_isDead = isDead;
	}

	public boolean hasLeft() {
		return _hasLeft;
	}

	public void setLeft(final boolean hasLeft) {
		_hasLeft = hasLeft;
	}

	public Player getPlayer()
	{
		if(_type == 1)
			return _player;
		return null;
	}

	public TaxCollector getTaxCollector() {
		if(_type == 5)
			return _taxCollector;
		return null;
    }

	public boolean testIfCC(int tauxCC)
	{
		if(tauxCC < 2)return false;
		int agi = getTotalStats().getEffect(Constants.STATS_ADD_AGIL);
		if(agi <0)agi =0;
		tauxCC -= getTotalStats().getEffect(Constants.STATS_ADD_CC);
		tauxCC = (int)((tauxCC * 2.9901) / Math.log(agi +12));//Influence de l'agi
		if(tauxCC<2)tauxCC = 2;
		final int jet = Formulas.getRandomValue(1, tauxCC);
		return (jet == tauxCC);
	}
	
	public Stats getTotalStats()
	{
		Stats stats = new Stats(new TreeMap<Integer,Integer>());
		if(_type == 1)
			stats = _player.getTotalStats();
		if(_type == 2)
			stats =_mob.getStats();
		if(_type == 5)
			stats = _taxCollector.getGuild().getStats();
		
		stats = Stats.cumulStat(stats,getFightBuffStats());
		return stats;
	}
	
	
	public void initBuffStats()
	{
		if(_type == 1)
		{
			for(final Map.Entry<Integer, SpellEffect> entry : _player.getBuffs().entrySet())
			{
				_fightBuffs.put(_player.getActorId(), entry.getValue());
			}
		}
	}
	
	private Stats getFightBuffStats()
	{
		final Stats stats = new Stats();
		for(final SpellEffect entry : _fightBuffs.values())
		{
			stats.addOneStat(entry.getEffectID(), entry.getValue());
		}
		return stats;
	}
	
	public String getGmPacket(final char c)
	{
		final StringBuilder str = new StringBuilder();
		str.append("GM|").append(c);
		str.append(_cell.getId()).append(';');
		_orientation = 1;
		str.append(_orientation).append(';');
		str.append("0;");
		str.append(getGUID()).append(';');
		str.append(getPacketsName()).append(';');
		switch(_type)
		{
			case 1://Perso
				str.append(_player.getBreedId()).append(';');
				str.append(_player.getGfxID()).append('^').append(_player.getSize()).append(';');
				str.append(_player.getSexe()).append(';');
				str.append(_player.getLvl()).append(';');
				str.append(_player.getAlign()).append(',');
				str.append(_player.getLvl()+_player.getActorId()).append(',');
				str.append((_player.isShowingWings()?-_player.getGrade():"0")).append(',');
				str.append(_player.getActorId()+_player.getLvl()).append(';');
				str.append((_player.getColor1()==-1?"-1":Integer.toHexString(_player.getColor1()))).append(';');
				str.append((_player.getColor2()==-1?"-1":Integer.toHexString(_player.getColor2()))).append(';');
				str.append((_player.getColor3()==-1?"-1":Integer.toHexString(_player.getColor3()))).append(';');
				str.append(_player.getGMStuffString()).append(';');
				str.append(getLife()).append(';');
				str.append(getTotalStats().getEffect(Constants.STATS_ADD_PA)).append(';');
				str.append(getTotalStats().getEffect(Constants.STATS_ADD_PM)).append(';');
				str.append(getTotalStats().getEffect(Constants.STATS_ADD_RP_NEU)).append(';');
				str.append(getTotalStats().getEffect(Constants.STATS_ADD_RP_TER)).append(';');
				str.append(getTotalStats().getEffect(Constants.STATS_ADD_RP_FEU)).append(';');
				str.append(getTotalStats().getEffect(Constants.STATS_ADD_RP_EAU)).append(';');	
				str.append(getTotalStats().getEffect(Constants.STATS_ADD_RP_AIR)).append(';');
				str.append(getTotalStats().getEffect(Constants.STATS_ADD_AFLEE)).append(';');
				str.append(getTotalStats().getEffect(Constants.STATS_ADD_MFLEE)).append(';');
				str.append(_team).append(';');
				if(_player.isOnMount() && _player.getMount() != null)str.append(_player.getMount().getStrColor());
				str.append(';');
			break;
			case 2://Mob
				str.append("-2").append(';');
				str.append(_mob.getTemplate().getGfxID()).append("^").append(_mob.getSize()).append(';');
				str.append(_mob.getGrade()).append(';');
				str.append(_mob.getTemplate().getColors().replace(",", ";")).append(';');
				str.append("0,0,0,0;");
				str.append(getMaxLife()).append(';');
				str.append(_mob.getPA()).append(';');
				str.append(_mob.getPM()).append(';');
				str.append(_team);
			break;
			case 5://Perco
				final int res = _taxCollector.getGuild().getLvl()>=100?50:(int)Math.floor(_taxCollector.getGuild().getLvl()/2);
				str.append(_taxCollector.getActorType()).append(';');//Perco
				str.append("6000^100").append(';');//GFXID^Size
				str.append(_taxCollector.getGuild().getLvl()).append(';');
				str.append(_taxCollector.getGuild().getLvl()*100).append(';');//PDVMAX
				str.append('8').append(';');//PA
				str.append('5').append(';');//PM
				str.append(res).append(';');//Résistance
				str.append(res).append(';');//Résistance
				str.append(res).append(';');//Résistance
				str.append(res).append(';');//Résistance
				str.append(res).append(';');//Résistance
				str.append(res).append(';');//Résistance
				str.append(res).append(';');//Résistance
				str.append(_team);
			break;
		}
		
		return str.toString();
	}
	
	public void setState(final int id, final int t)
	{
		_states.remove(id);
		if(t != 0)
		_states.put(id, t);
	}
	
	private Map<Integer, Integer> getStates() {
		return _states;
	}
	
	public boolean hasState(final int id)
	{
		if(_states.get(id) == null)return false;
		return _states.get(id) != 0;
	}
	
	public void decrementStates()
	{
		Map<Integer, Integer> newStates = new TreeMap<Integer, Integer>();
		for(final Entry<Integer,Integer> e : _states.entrySet())
		{
			//Si la valeur est négative, on y touche pas
			if(e.getKey() <= 0)continue;
			
			final int nVal = e.getValue()-1;
			//Si 0 on ne remet pas la valeur dans le tableau
			if(nVal == 0)//ne pas mettre plus petit, -1 = infinie
			{
				//on envoie au client la desactivation de l'état
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 950, getGUID()+"", getGUID()+","+e.getKey()+",0");
				continue;
			}
			//Sinon on ajoute avec la nouvelle valeur
			newStates.put(e.getKey(), nVal);
		}
		_states.clear();
		_states.putAll(newStates);
	}
	
	public int getLife()
	{
		final int life = _life + getBuffValue(Constants.STATS_ADD_VITA) + getBuffValue(Constants.STATS_ADD_VIE);
		return life;
	}
	
	public void removeLife(final int pdv)
	{
		_life -= pdv;
		_maxLife -= (int) (Math.floor(pdv / 10));
	}
	
	public void heal(final int life) {
		_life += life;
	}
	
	public void applyBeginningTurnBuff(final Fight fight)
	{
		synchronized(_fightBuffs)
		{
			for(final int effectID : Constants.BEGIN_TURN_BUFF)
			{
				//On évite les modifications concurrentes
				final ArrayList<SpellEffect> buffs = new ArrayList<SpellEffect>();
				buffs.addAll(_fightBuffs.values());
				for(final SpellEffect entry : buffs)
				{
					if(entry.getEffectID() == effectID)
					{
						if(effectID == 127 && (entry.getSpell() != 69 || entry.getSpell() != 136))
							continue;
						if(effectID == 89 && (entry.getSpell() == 447))
							continue;
						Log.addToLog("Effet de début de tour : "+ effectID);
						entry.applyBeginingBuff(fight, this);
					}
				}
			}
		}
	}
	
	public SpellEffect getBuff(int id) {
		for(SpellEffect entry : _fightBuffs.values())
		{
			if(entry.getEffectID() == id && entry.getDuration() >0)
			{
				return entry;
			}
		}
		return null;
	}

	public boolean hasBuff(final int id)
	{
		for(final SpellEffect entry : _fightBuffs.values())
		{
			if(entry.getEffectID() == id && entry.getDuration() >0)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean hasBuffBySpellId(final int id)
	{
		for(final SpellEffect entry : _fightBuffs.values())
		{
			if(entry.getSpell() == id)
			{
				return true;
			}
		}
		return false;
	}
	
	public int getBuffValue(final int id)
	{
		int value = 0;
		for(final SpellEffect entry : _fightBuffs.values())
		{
			if(entry.getEffectID() == id)
				value += entry.getValue();
		}
		return value;
	}
	
	public int getBuffValueBySpellID(final int id)
	{
		int value = 0;
		for(final SpellEffect entry : _fightBuffs.values())
		{
			if(entry.getSpell() == id)
				value += entry.getValue();
		}
		return value;
	}

	public void refreshFightBuff()
	{
		//Copie pour contrer les modifications Concurentes
		final Map<Integer, SpellEffect> effects = new TreeMap<Integer, SpellEffect>();
		decrementStates();
		for(final Entry<Integer, SpellEffect> entry : _fightBuffs.entrySet())
		{
			SpellEffect buff = entry.getValue();
			if(buff.decrementDuration() != 0)//Si pas fin du buff
			{
				effects.put(entry.getKey(), buff);
			}else
			{
				Log.addToLog("Suppression du buff "+buff.getEffectID()+" sur le joueur Fighter ID= "+getGUID());
				int value = buff.getValue();
				switch(buff.getEffectID())
				{
					case 125://Vitalité
						if(buff.getSpell() == 441) {
							_maxLife -= value;
							if(_maxLife < 0)
								_maxLife = 0;
							_life -= value;
							if(_life < 0) {
								_life = 0;
								_fight.onFighterDie(this);
							}		
						}
					break;
					case 150://Invisibilité
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 150, buff.getCaster().getGUID()+"",getGUID()+",0");
					break;
				}
			}
		}
		_fightBuffs.clear();
		_fightBuffs.putAll(effects);
	}
	
	public void addBuff(final int id,int val,final int duration,final int turns,boolean debuffable,final int spellID,final String args,final Fighter caster, boolean isPoison)
	{
		debuffable = checkDebuff(spellID, debuffable);
		//Si c'est le jouer actif qui s'autoBuff, on ajoute 1 a la durée
		_fightBuffs.put(caster.getGUID(), new SpellEffect(id,val,(_canPlay?duration+1:duration),turns,debuffable,caster,args,spellID,isPoison));
		Log.addToLog("Ajout du Buff "+id+" sur le personnage Fighter ID = "+this.getGUID());
		addViewBuff(id, val, args, duration, spellID);
	}
	
	private void addViewBuff(int id, int val, String args, int duration, int spellID) {
		switch(id)
		{
			case 6://Renvoie de sort
			case 106://Renvoi de sort
				String valMax = args.split(";")[1];
				SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, valMax, "10", "", duration, spellID);
				//SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), -1, val+"", "10", "", duration, spellID);
				break;
			
			case 79://Chance éca
				val = Integer.parseInt(args.split(";")[0]);
				valMax = args.split(";")[1];
				final String chance = args.split(";")[2];
				SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, valMax, chance, "", duration, spellID);
				break;
			
			case 96: //Dégats eaux
			case 97: //Dégats terre
			case 98: //Dégats air
			case 99: //Dégats feu
			case 100: //Dégats neutre
			case 107: //Renvoi de dégats
			case 108: //Soin
			case 165: //Maîtrises
			case 781: //Minimise les effets aléatoire
			case 782: //Maximise les effets aléatoire		
				val = Integer.parseInt(args.split(";")[0]);
				valMax = args.split(";")[1];
				if(valMax.compareTo("-1") == 0 || spellID == 82 || spellID == 94 || spellID == 132)
				{
					SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, "", "", "", duration, spellID);		
				}else if(valMax.compareTo("-1") != 0)
				{
					SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, valMax, "", "", duration, spellID);
				}
				break;
			
			case 788://Fait apparaitre message le temps de buff sacri Chatiment de X sur Y tours
				val = Integer.parseInt(args.split(";")[1]);
				final String valMax2 = args.split(";")[2];
				if(Integer.parseInt(args.split(";")[0]) == 108)return;
				SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, ""+val, ""+valMax2, "", duration, spellID);
				break;
				
			case 950:
				SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), -1, "", val+"", "", duration, spellID);
				break;
			
			default:
				SocketManager.GAME_SEND_FIGHT_GIE_TO_FIGHT(_fight, 7, id, getGUID(), val, "", "", "", duration, spellID);
			break;
		}
		if(isPlayer()) {
			_player.clearCacheAS();
			SocketManager.GAME_SEND_STATS_PACKET(_player);
		}
	}

	private boolean checkDebuff(int spellID, boolean defaultValue) {
		boolean toReturn;
		switch (spellID)
		{
		case 1:
		case 4:
		case 5:
		case 6:
		case 7:
		case 14:
		case 18:
		case 20:
		case 89:
		case 99:
		case 115:
		case 126:
		case 127:
		case 192:
		case 197:
		case 284:
			toReturn = true;
			break;
			
		case 431:
		case 433:
		case 437:
		case 443:
			toReturn = false;
			break;
			
		default:
			toReturn = defaultValue;
			break;
		}
		return toReturn;
	}

	public int getInitiative()
	{
		if(_type == 1)
			return _player.getInitiative();
		if(_type == 2)
			return _mob.getInit();
		if(_type == 5)
			return _taxCollector.getGuild().getLvl();
		return 0;
	}
	public int getMaxLife()
	{
		return _maxLife + getBuffValue(Constants.STATS_ADD_VITA) + getBuffValue(Constants.STATS_ADD_VIE);
	}
	
	public int getLvl() {
		if(_type == 1)
			return _player.getLvl();
		if(_type == 2)
			return _mob.getLevel();
		if(_type == 5)
			return _taxCollector.getGuild().getLvl();
		return 0;
	}
	public String xpString(final String str)
	{
		if(_player != null)
		{
			int max = _player.getLvl()+1;
			if(max>Config.MAX_LEVEL)max = Config.MAX_LEVEL;
			return World.getExpLevel(_player.getLvl()).perso+str+_player.getCurExp()+str+World.getExpLevel(max).perso;		
		}
		return "0"+str+"0"+str+"0";
	}
	public String getPacketsName()
	{
		if(_type == 1)
			return _player.getName();
		if(_type == 2)
			return _mob.getTemplate().getID()+"";
		if(_type == 5)
			return _taxCollector.getName1()+","+_taxCollector.getName2();
		return "";
	}
	public MonsterGrade getMob()
	{
		if(_type == 2)
			return _mob;
		
		return null;
	}
	public int getTeam()
	{
		return _team;
	}
	public int getTeam2() {
		return _fight.getTeamID(_id);
	}
	public int getEnemyTeam()
	{
		return _fight.getEnemyTeamID(_id);
	}
	public boolean canPlay()
	{
		return _canPlay;
	}
	public void setCanPlay(final boolean b)
	{
		_canPlay = b;
	}
	public ArrayList<SpellEffect> getBuffsByEffectID(final int effectID)
	{
		final ArrayList<SpellEffect> buffs = new ArrayList<SpellEffect>();
		for(final SpellEffect buff : _fightBuffs.values())
		{
			if(buff.getEffectID() == effectID)
				buffs.add(buff);
		}
		return buffs;
	}
	public Stats getTotalStatsLessBuff()
	{
		Stats stats = new Stats(new TreeMap<Integer,Integer>());
		if(_type == 1)
			stats = _player.getTotalStats();
		if(_type == 2)
			stats =_mob.getStats();
		if(_type == 5)
			stats = _taxCollector.getGuild().getStats();
		return stats;
	}
	public int getPA()
	{
		if(_type == 1)
			return getTotalStats().getEffect(Constants.STATS_ADD_PA);
		if(_type == 2)
			return getTotalStats().getEffect(Constants.STATS_ADD_PA) + _mob.getPA();
		if(_type == 5)
			return getTotalStats().getEffect(Constants.STATS_ADD_PA) + 6;
		return 0;
	}
	public int getPM()
	{
		if(_type == 1)
			return getTotalStats().getEffect(Constants.STATS_ADD_PM);
		if(_type == 2)
			return getTotalStats().getEffect(Constants.STATS_ADD_PM) + _mob.getPM();
		if(_type == 5)
			return getTotalStats().getEffect(Constants.STATS_ADD_PM) + 3;
		return 0;
	}
	public int getBasePA()
	{
		if(_type == 1)
			return getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_PA);
		if(_type == 2)
			return getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_PA) + _mob.getPA();
		if(_type == 5)
			return getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_PA) + 6;
		return 0;
	}
	public int getBasePM()
	{
		if(_type == 1)
			return getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_PM);
		if(_type == 2)
			return getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_PM) + _mob.getPM();
		if(_type == 5)
			return getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_PM) + 3;
		return 0;
	}
	public void setInvocator(final Fighter caster)
	{
		_invocator = caster;
	}
	
	public Fighter getInvocator()
	{
		return _invocator;
	}
	
	public boolean isPlayer() {
		return (_player!=null);
	}
	
	public boolean isInvocation()
	{
		return (_invocator!=null);
	}
	
	public boolean isMob()
	{
		return (_mob!=null);
	}
	
	public boolean isPerco()
	{
		return (_taxCollector!=null);
	}
	
	public void debuff()
	{
		final Map<Integer, SpellEffect> newBuffs = new TreeMap<Integer, SpellEffect>();
		//on vérifie chaque buff en cours, si pas débuffable, on l'ajout a la nouvelle liste
		for(final Entry<Integer, SpellEffect> entry : _fightBuffs.entrySet())
		{
			SpellEffect SE = entry.getValue();
			if(!SE.isDebuffable())
				newBuffs.put(entry.getKey(), SE);
			//On envoie les Packets si besoin
			switch(SE.getEffectID())
			{
				case Constants.STATS_ADD_PA:
				case Constants.STATS_ADD_PA2:
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 101,getGUID()+"",getGUID()+",-"+SE.getValue());
				break;
				
				case Constants.STATS_ADD_PM:
				case Constants.STATS_ADD_PM2:
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 127,getGUID()+"",getGUID()+",-"+SE.getValue());
				break;
			}
		}
		_fightBuffs.clear();
		_fightBuffs.putAll(newBuffs);
		if(_player != null && !_hasLeft) {
			_player.clearCacheAS();
			SocketManager.GAME_SEND_STATS_PACKET(_player);
		}
	}

	public void fullPDV()
	{
		_life = _maxLife;
	}

	public void unHide(int spellId)
	{
		if(spellId != -1)// -1 : CAC
		{
			switch(spellId) 
			{ 
				case 66:
				case 71:
				case 181: 
				case 196: 
				case 200: 
				case 219:
				case -666:
				return; 
			}
		}
		//on retire le buff invi
		final ArrayList<SpellEffect> buffs = new ArrayList<SpellEffect>();
		buffs.addAll(getFightBuff().values());
		for(final SpellEffect SE : buffs)
		{
			if(SE.getEffectID() == 150)
				getFightBuff().remove(SE);
		}
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(_fight, 7, 150,getGUID()+"",getGUID()+",0");
		//On actualise la position
		SocketManager.GAME_SEND_GIC_PACKET_TO_FIGHT(_fight, 7,this);
	}

	public int getPdvMaxOutFight()
	{
		if(_player != null)return _player.getPDVMAX();
		if(_mob != null)return _mob.getPDVMAX();
		return 0;
	}

	public Map<Integer, Integer> getChatiValue() {
		return _chatiValue;
	}

	public int getDefaultGfx()
	{
		if(_player != null)return _player.getGfxID();
		if(_mob != null)return _mob.getTemplate().getGfxID();
		return 0;
	}

	public long getXpGive()
	{
		if(_mob != null)return _mob.getBaseXp();
		return 0;
	}

	public int getPdvBeforeFight() {
        return _pdvBeforeFight;
    }

	public void setPdvBeforeFight(final int pdv) {
		_pdvBeforeFight = pdv;
    }

	public boolean isDisconnected() {
		return _isDisconnected;
	}

	public void setDisconnected(final boolean _isDisconnected) {
		this._isDisconnected = _isDisconnected;
	}

	public short getRemainingTurns() {
		return _remainingTurns;
	}

	public void decrementRemainingTurns() {
		this._remainingTurns--;
	}

	public void decrNumbInvocations() {
		this._nbInvoc--;
	}
	
	public void incrNumbInvocations() {
		this._nbInvoc++;
	}

	public boolean isSpectator() {
		return _fight.getFighters(4).contains(this);
	}

	public void setMaxLife(int pdvMax) {
		_maxLife = pdvMax;
	}

	public void addKamasStolen(int val) {
		_kamasStolen += val;
	}

	public int getKamasStolen() {
		return _kamasStolen;
	}

	public void addCooldown(Fighter target, SpellStat spell) {
		CooldownSpell cooldownSpell = new CooldownSpell(target, spell);
		_cooldownSpell.add(cooldownSpell);
	}
	
	public void actualizeCooldown() {
		ArrayList<CooldownSpell> cooldowns = new ArrayList<CooldownSpell>();
		cooldowns.addAll(_cooldownSpell);
		int i = 0;
		for(CooldownSpell cooldown : cooldowns) {
			cooldown.decrement();
			if(cooldown.getCooldown() <= 0) {
				_cooldownSpell.remove(i);
				i--;
			}
			i++;
		}
	}
	
	public ArrayList<CooldownSpell> getCooldowns() {
		return _cooldownSpell;
	}

	public boolean isStatic() {
		if(_mob == null) 
			return false;
		for(int id : Constants.STATIC_INVOCATIONS)
			if(_mob.getTemplate().getID() == id)
				return true;
		return false;
	}

	public int getCurPM(Fight fight) {
		return fight.getCurFighterPM();
	}

	public int getCurPA(Fight fight) {
		return fight.getCurFighterPA();
	}

	public void showBuffsNStates() {
		for(Fighter fighter : _fight.getFighters(3)) {
			for(SpellEffect effect : fighter.getFightBuff().values()) {
				int id = effect.getEffectID();
				String args = effect.getArgs();
				int val = effect.getValue();
				int duration = effect.getDuration();
				int spellID = effect.getSpell();
				addViewBuff(id, val, args, duration, spellID);
			}
			for(final Entry<Integer,Integer> e : fighter.getStates().entrySet())
			{
				SocketManager.GAME_SEND_GA_PACKET(_player.getAccount().getGameThread().getOut(), "950", getGUID()+"", getGUID()+","+e.getKey()+","+e.getValue(), "");
			}
		}
	}
}
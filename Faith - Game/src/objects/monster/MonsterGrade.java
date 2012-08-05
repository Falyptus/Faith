package objects.monster;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import objects.character.Stats;
import objects.fight.Fighter;
import objects.map.DofusCell;
import objects.spell.SpellStat;
import objects.spell.Spell;
import objects.spell.SpellEffect;

import common.Constants;
import common.World;
import common.World.Drop;

public class MonsterGrade
{
	private final Monster template;
	private final int grade;
	int level;
	private int PDV;
	private int inFightID;
	private int PDVMAX;
	private int init;
	private final int PA;
	private final int PM;
	private DofusCell fightCell;
	private int baseXp = 10;
	private final ArrayList<SpellEffect> _fightBuffs = new ArrayList<SpellEffect>();
	private Map<Integer,Integer> stats = new TreeMap<Integer,Integer>();
	private Map<Integer,SpellStat> spells = new TreeMap<Integer,SpellStat>();
	private long _xpWon = 0;
	private final ArrayList<Drop> loots = new ArrayList<Drop>();
	private long _kamasWon;
	
	public MonsterGrade(final Monster aTemp, final int Agrade, final int Alevel,final int aPA,final int aPM, final String Aresist, final String Astats, final String Aspells,final int pdvMax,final int aInit, final int xp)
	{
		template = aTemp;
		grade = Agrade;
		level = Alevel;
		PDVMAX = pdvMax;
		PDV = PDVMAX;
		PA = aPA;
		PM = aPM;
		baseXp = xp;
		init = aInit;
		final String[] resists = Aresist.split(";");
		final String[] statsArray = Astats.split(",");
		int RN = 0,RF = 0, RE = 0, RA = 0, RT = 0, AF = 0, MF = 0,force = 0, intell = 0, sagesse = 0,chance = 0, agilite = 0;
		try
		{
			RN = Integer.parseInt(resists[0]);
			RT = Integer.parseInt(resists[1]);
			RF = Integer.parseInt(resists[2]);
			RE = Integer.parseInt(resists[3]);
			RA = Integer.parseInt(resists[4]);
			AF = Integer.parseInt(resists[5]);
			MF = Integer.parseInt(resists[6]);
			force = Integer.parseInt(statsArray[0]);
			sagesse = Integer.parseInt(statsArray[1]);
			intell = Integer.parseInt(statsArray[2]);
			chance = Integer.parseInt(statsArray[3]);
			agilite = Integer.parseInt(statsArray[4]);
		}catch(final Exception e){e.printStackTrace();};
		
		stats.clear();
		stats.put(Constants.STATS_ADD_FORC, force);
		stats.put(Constants.STATS_ADD_SAGE, getSagesse(AF, MF, sagesse));
		stats.put(Constants.STATS_ADD_INTE, intell);
		stats.put(Constants.STATS_ADD_CHAN, chance);
		stats.put(Constants.STATS_ADD_AGIL, agilite);
		stats.put(Constants.STATS_ADD_RP_NEU, RN);
		stats.put(Constants.STATS_ADD_RP_FEU, RF);
		stats.put(Constants.STATS_ADD_RP_EAU, RE);
		stats.put(Constants.STATS_ADD_RP_AIR, RA);
		stats.put(Constants.STATS_ADD_RP_TER, RT);
		stats.put(Constants.STATS_ADD_AFLEE, AF);
		stats.put(Constants.STATS_ADD_MFLEE, MF);
		stats.put(Constants.STATS_CREATURE, getNbInvoc());
		spells.clear();
		final String[] spellsArray = Aspells.split(";");
		for(final String str : spellsArray)
		{
			if(str.equals(""))continue;
			final String[] spellInfo = str.split("@");
			int spellID = 0;
			int spellLvl = 0;
			try
			{
				spellID = Integer.parseInt(spellInfo[0]);
				spellLvl = Integer.parseInt(spellInfo[1]);
			}catch(final Exception e){continue;};
			if(spellID == 0 || spellLvl == 0)continue;
			
			final Spell sort = World.getSpell(spellID);
			if(sort == null)continue;
			final SpellStat SpellStats = sort.getStatsByLevel(spellLvl);
			if(SpellStats == null)continue;
			
			spells.put(spellID, SpellStats);
		}
	}

	private MonsterGrade(final Monster template2, final int grade2, final int level2, final int pdv2,final int pdvmax2,final int aPA,final int aPM, final Map<Integer, Integer> stats2,final Map<Integer, SpellStat> spells2,final int xp)
	{
		template = template2;
		grade = grade2;
		level = level2;
		PDV = pdv2;
		PDVMAX = pdvmax2;
		PA = aPA;
		PM = aPM;
		stats = stats2;
		spells = spells2;
		inFightID = -1;
		baseXp = xp;
	}
	private int getSagesse(final int AF, final int MF, final int sagesseBase)
	{
		if(AF > 0 && MF > 0)
			return ((AF + MF) / 2) * 3;
		else if (AF > 0 && MF <= 0)
			return AF * 3;
		else if (MF > 0 && AF <= 0)
			return MF * 3;
		else
			return sagesseBase;
	}
	private int getNbInvoc()
	{
		if(template.getID() == 423)
			return 4;
		return 2;
	}
	public void modifyStatByInvocator(Fighter invocator, boolean isStatic)
	{
		float coef = 1 + invocator.getLvl() / 100;
		if(isStatic) coef = 1.3f;
		int strenght = Math.round(stats.remove(Constants.STATS_ADD_FORC) * coef);
		int wisdom = Math.round(stats.remove(Constants.STATS_ADD_SAGE) * coef);
		int intelligence = Math.round(stats.remove(Constants.STATS_ADD_INTE) * coef);
		int chance = Math.round(stats.remove(Constants.STATS_ADD_CHAN) * coef);
		int agility = Math.round(stats.remove(Constants.STATS_ADD_AGIL) * coef);
			
		stats.put(Constants.STATS_ADD_FORC, strenght);
		stats.put(Constants.STATS_ADD_SAGE, wisdom);
		stats.put(Constants.STATS_ADD_INTE, intelligence);
		stats.put(Constants.STATS_ADD_CHAN, chance);
		stats.put(Constants.STATS_ADD_AGIL, agility);
		
		PDVMAX = Math.round(PDVMAX * coef);
		PDV = PDVMAX;
	}
	public ArrayList<Drop> getDrops()
	{
		return template.getDrops();
	}
	public int getBaseXp()
	{
		return baseXp;
	}
	public int getInit() {
		return init;
	}

	public MonsterGrade getCopy()
	{
		final Map<Integer,Integer> newStats = new TreeMap<Integer,Integer>();
		newStats.putAll(stats);
		return new MonsterGrade(template,grade,level,PDV,PDVMAX,PA,PM,newStats,spells,baseXp);
	}

	public Stats getStats()
	{
		return new Stats(stats);
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public ArrayList<SpellEffect> getBuffs()
	{
		return _fightBuffs;
	}
	
	public DofusCell getFightCell()
	{
		return fightCell;
	}
	
	public void setFightCell(final DofusCell cell)
	{
		fightCell = cell;
	}
	
	public Map<Integer,SpellStat> getSpells()
	{
		return spells;
	}
	
	public Monster getTemplate()
	{
		return template;
	}
	
	public int getPDV() {
		return PDV;
	}

	public void setPDV(final int pdv) {
		PDV = pdv;
	}

	public int getPDVMAX() {
		return PDVMAX;
	}

	public int getGrade()
	{
		return grade;
	}

	public void setInFightID(final int i)
	{
		inFightID = i;
	}
	public int getInFightID()
	{
		return inFightID;
	}

	public int getPA()
	{
		return PA;
	}
	public int getPM()
	{
		return PM;
	}
	
	public int getSize()
	{
		return (int)(97.5+(grade*2.5));
	}
	
	public long getXpWon() {
		return _xpWon;
	}

	public void setXpWon(final long _xpWon) {
		this._xpWon = _xpWon;
	}

	public ArrayList<Drop> getLoots() {
		return loots;
	}

	public void setKamasWon(final long kamas) {
		_kamasWon = kamas;
	}

	public long getKamasWon() {
		return _kamasWon;
	}
}
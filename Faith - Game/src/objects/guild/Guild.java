package objects.guild;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import objects.character.Player;
import objects.character.Stats;


import common.Config;
import common.Constants;
import common.SQLManager;
import common.World;

public class Guild {
	private final int _id;
	private String _name = "";
	private String _emblem = "";
	private final Map<Integer,GuildMember> _members = new TreeMap<Integer,GuildMember>();
	private int _lvl;
	private long _xp;
	private int _nbrPercos;
	private int _capital;
	//Boost
	private final Map<Integer, Integer> sorts = new TreeMap<Integer, Integer>();	//<ID, Level>
	private final Map<Integer, Integer> stats = new TreeMap<Integer, Integer>(); //<Effet, Quantité>
	private final Map<Integer, TaxCollector> _taxCollectors = new TreeMap<Integer, TaxCollector>();
	
	public Guild(final Player owner,final String name,final String emblem)
	{
		_id = World.getNextHighestGuildID();
		_name = name;
		_emblem = emblem;
		_lvl = 1;
		_xp = 0;
		decompileSpell(Config.BASE_GUILD_SPELL);
		decompileStat(Config.BASE_GUILD_STAT);
	}
	public Guild(final int id,final String name, final String emblem,final int lvl,final long xp,
			final String sorts, final String stats)
	{
		_id = id;
		_name = name;
		_emblem = emblem;
		_xp = xp;
		_lvl = lvl;
		decompileSpell(sorts);
		decompileStat(stats);
	}

	public GuildMember addMember(final int guid,final String name,final int lvl,final int gfx,final int r,final byte pXp,final long x,final int ri,final byte a,final String lastCo)
	{
		final GuildMember GM = new GuildMember(guid,this,name,lvl,gfx,r,x,pXp,ri,a,lastCo);
		_members.put(guid,GM);
		return GM;
	}
	public GuildMember addNewMember(final Player p)
	{
		final GuildMember GM = new GuildMember(p.getActorId(),this,p.getName(),p.getLvl(),p.getGfxID(),0,0,(byte) 0,0,p.getAlign(),p.getAccount().getLastConnectionDate());
		_members.put(p.getActorId(),GM);
		return GM;
	}

	public int get_id()
	{
		return _id;
	}

	public Map<Integer, Integer> getSorts() {
		return sorts;
	}
	public Stats getStats() {
		return new Stats(stats);
	}
	public Map<Integer, TaxCollector> getPercepteurs() {
	    return _taxCollectors;
    }
	public void addStat(final int stat, final int qte)
	{
		final int old = stats.get(stat);
		
		stats.put(stat, old + qte);
	}
	public void boostSort(final int ID)
	{
		final int old = sorts.get(ID);
		
		sorts.put(ID, old + 1);
	}
	
	public String getName() {
		return _name;
	}
	public String getEmblem()
	{
		return _emblem;
	}
	public long getXp()
	{
		return _xp;
	}
	public int getLvl()
	{
		return _lvl;
	}
	public int getSize()
	{
		return _members.size();
	}
	public void addCapital(final int _capital) 
	{
	    this._capital += _capital;
    }
	public void removeCapital(final int i) 
	{
	    _capital -= i;
    }
	public int getCapital() 
	{
	    return _capital;
    }
	public void addNbrPercos(final int _nbrPercos) 
	{
	    this._nbrPercos += _nbrPercos;
    }
	public int getNbrTaxCollector() 
	{
		return _nbrPercos;
    }
	public int getCountTaxCollector() 
	{
		return _taxCollectors.size();
    }
	public String parseMembersToGM()
	{
		final StringBuilder str = new StringBuilder(25*_members.size());
		for(final GuildMember GM : _members.values())
		{
			String online = "0";
			if(GM.getPerso() != null)if(GM.getPerso().isOnline())online = "1";
			if(str.length() != 0)str.append('|');
			str.append(GM.getGuid()).append(';');
			str.append(GM.getName()).append(';');
			str.append(GM.getLvl()).append(';');
			str.append(GM.getGfx()).append(';');
			str.append(GM.getRank()).append(';');
			str.append(GM.getXpGave()).append(';');
			str.append(GM.getPXpGive()).append(';');
			str.append(GM.getRights()).append(';');
			str.append(online).append(';');
			str.append(GM.getAlign()).append(';');
			str.append(GM.getHoursFromLastCo());
		}
		return str.toString();
	}
	public ArrayList<Player> getMembers()
	{
		final ArrayList<Player> a = new ArrayList<Player>();
		for(final GuildMember GM : _members.values())a.add(GM.getPerso());
		return a;
	}
	public GuildMember getMember(final int guid)
	{
		return _members.get(guid);
	}
	public void removeMember(final int guid)
	{
		if(_members.get(guid).getRank() == 1 && _members.size() > 1)	//Si c'est le meneur et qu'il y a d'autre personne dans la guilde
		{
			GuildMember newMeneur = null;
			for(final GuildMember curGm : _members.values())
			{
				if(curGm.getGuid() == guid)continue;
				
				if(newMeneur == null)
				{
					newMeneur = curGm;
					continue;
				}
				if(curGm.getRank() == 2)	//Si bras droit
				{
					newMeneur = curGm;
					break;
				}
				if(curGm.getXpGave() > newMeneur.getXpGave())
					newMeneur = curGm;
			}
			if(newMeneur != null)
				newMeneur.setRank(1);
		}
		_members.remove(guid);
		SQLManager.DEL_GUILDMEMBER(guid);
	}
	public int getSubstituteId(final int oldLeaderGuid)
	{
		if(_members.get(oldLeaderGuid).getRank() == 1 && _members.size() > 1)	//Si c'est le meneur et qu'il y a d'autre personne dans la guilde
		{
			GuildMember newMeneur = null;
			for(final GuildMember curGm : _members.values())
			{
				if(curGm.getGuid() == oldLeaderGuid)continue;
				
				if(newMeneur == null)
				{
					newMeneur = curGm;
					continue;
				}
				if(curGm.getRank() == 2)	//Si bras droit
				{
					newMeneur = curGm;
					break;
				}
				if(curGm.getXpGave() > newMeneur.getXpGave())
					newMeneur = curGm;
			}
			return newMeneur.getGuid();
		}
		return 0;
	}
	public void substituteLeader(final int oldLeader, final int newLeader) {
		_members.get(newLeader).setRank(1);
		
		_members.remove(oldLeader);
		SQLManager.DEL_GUILDMEMBER(oldLeader);
    }
	public void addXp(final long xp)
	{
		this._xp+=xp;
		
		while(_xp >= World.getGuildXpMax(_lvl) && _lvl<Config.MAX_LEVEL)
			levelUp();
	}
	
	public void levelUp()
	{
		this._lvl++;
	}
	
	public void upgradeStats(final int statId, final int add)
	{
		final int actual = stats.get(statId);
		stats.remove(statId);
		stats.put(statId, (actual+add));
	}
	
	public int getStats(final int statId)
	{
		int value = 0;
		for(final Entry<Integer, Integer> curStat : stats.entrySet())
		{
			if(curStat.getKey() == statId)
			{
				value = curStat.getValue();
			}
		}
		return value;
	}
	
	public void decompileSpell(final String spellStr) //ID;lvl|ID;lvl|...
	{
		int id;
		int lvl;
		
		for(final String split : spellStr.split("\\|"))
		{
			id = Integer.parseInt(split.split(";")[0]);
			lvl = Integer.parseInt(split.split(";")[1]);
			
			sorts.put(id, lvl);
		}
	}
	
	private void decompileStat(final String statStr) //ID;value|ID;value|...
	{
		int id;
		int value;
		
		for(final String split : statStr.split("\\|"))
		{
			id = Integer.parseInt(split.split(";")[0]);
			value = Integer.parseInt(split.split(";")[1]);
			
			stats.put(id, value);
		}
    }
	
	public String compileSpell()
	{
		final StringBuilder toReturn = new StringBuilder(10*sorts.size());
		boolean isFirst = true;
		
		for(final Entry<Integer, Integer> curSpell : sorts.entrySet())
		{
			if(!isFirst)
				toReturn.append('|');
			
			toReturn.append(curSpell.getKey()).append(';').append(curSpell.getValue());
			
			isFirst = false;
		}
		return toReturn.toString();
	}
	
	public String compileStats()
	{
		final StringBuilder toReturn = new StringBuilder(10*stats.size());
		boolean isFirst = true;
		
		for(final Entry<Integer, Integer> curSpell : stats.entrySet())
		{
			if(!isFirst)
				toReturn.append('|');
			
			toReturn.append(curSpell.getKey()).append(';').append(curSpell.getValue());
			
			isFirst = false;
		}
		return toReturn.toString();
	}
	
	public String parseQuestionTaxCollector() {
		final StringBuilder packet = new StringBuilder(10);
		packet.append('1').append(';');
		packet.append(_name).append(',');
		packet.append(getStats(Constants.STATS_ADD_PODS)).append(',');
		packet.append(getStats(Constants.STATS_ADD_PROS)).append(',');
		packet.append(getStats(Constants.STATS_ADD_SAGE)).append(',');
		packet.append(_nbrPercos);
		return packet.toString();
	}
	
	public String parseStatsTaxCollectorToGuild() {
		//Percomax|0|100*level|level|perco_add_pods|perco_prospection|perco_sagesse|perco_max|perco_boost|1000+10*level|perco_spells
		final StringBuilder packet = new StringBuilder(20);
		packet.append(getNbrTaxCollector()).append('|');
		packet.append(getCountTaxCollector()).append('|');
		packet.append(100*getLvl()).append('|');
		packet.append(getLvl()).append('|');
		packet.append(getStats(158)).append('|');
		packet.append(getStats(176)).append('|');
		packet.append(getStats(124)).append('|');
		packet.append(getNbrTaxCollector()).append('|');
		packet.append(getCapital()).append('|');
		packet.append((1000+(10*getLvl()))).append('|');
		packet.append(compileSpell());
		return packet.toString();
    }
	
	public String parseInfosPerco() {
		final StringBuilder packet = new StringBuilder(30*_taxCollectors.size()).append('+');
		boolean isFirst = true;
		for(final TaxCollector taxCollector : _taxCollectors.values())
		{	
			if(!isFirst) packet.append('|');
			packet.append(taxCollector.parseInfos());
    		isFirst = false;
		}
		return packet.toString();
    }
	
}

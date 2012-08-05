package objects.character;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import common.Constants;
import common.World;

public class Stats
{
	private Map<Integer,Integer> Effects = new TreeMap<Integer,Integer>();
	
	public Stats(final boolean addBases,final Player perso)
	{
		Effects = new TreeMap<Integer,Integer>();
		if(!addBases)return;
		Effects.put(Constants.STATS_ADD_PA, perso.getLvl()<100?6:7);
		Effects.put(Constants.STATS_ADD_PM, 3);
		Effects.put(Constants.STATS_ADD_PROS, perso.getBreedId()==Constants.CLASS_ENUTROF?120:100);
		Effects.put(Constants.STATS_ADD_PODS, 1000);
		Effects.put(Constants.STATS_CREATURE, 1);
		Effects.put(Constants.STATS_ADD_INIT, 1);
	}
	public Stats(final Map<Integer, Integer> stats, final boolean addBases,final Player perso)
	{
		Effects = stats;
		if(!addBases)return;
		Effects.put(Constants.STATS_ADD_PA, perso.getLvl()<100?6:7);
		Effects.put(Constants.STATS_ADD_PM, 3);
		Effects.put(Constants.STATS_ADD_PROS, perso.getBreedId()==Constants.CLASS_ENUTROF?120:100);
		Effects.put(Constants.STATS_ADD_PODS, 1000);
		Effects.put(Constants.STATS_CREATURE, 1);
		Effects.put(Constants.STATS_ADD_INIT, 1);
	}
	
	public Stats(final Map<Integer, Integer> stats)
	{
		Effects = stats;
	}
	
	public Stats()
	{
		Effects = new TreeMap<Integer,Integer>();
	}
	
	public void initBreedStats(final Player player)
	{
		final Breed breed = World.getBreed(player.getBreedId());
		Effects.put(Constants.STATS_ADD_PA, breed.getStartPA());
		Effects.put(Constants.STATS_ADD_PM, breed.getStartPM());
		Effects.put(Constants.STATS_ADD_PROS, breed.getStartProspecting());
		Effects.put(Constants.STATS_ADD_PODS, 1000);
		Effects.put(Constants.STATS_CREATURE, 1);
		Effects.put(Constants.STATS_ADD_INIT, breed.getStartInitiative());
	}
	
	public int addOneStat(final int id, final int val)
	{
		if(Effects.get(id) == null || Effects.get(id) == 0)
			Effects.put(id,val);
		else
		{
			final int newVal = (Effects.get(id)+val);
			Effects.put(id, newVal);
		}
		return Effects.get(id);
	}
	
	/*---------------------LIGNE PAR MARTHIEUBEAN-------------------------*/
	//Utiliser pour remettre tout les stats de base à "0". Utiliser pour le restat sur le site entre autre
	public void resetStat()
	{
		setStat(Constants.STATS_ADD_INTE,0);
		setStat(Constants.STATS_ADD_FORC,0);
		setStat(Constants.STATS_ADD_AGIL,0);
		setStat(Constants.STATS_ADD_CHAN,0);
		setStat(Constants.STATS_ADD_SAGE,0);
		setStat(Constants.STATS_ADD_VITA,0);
	}
	public void setStat(final int stat, final int _nbr)
	{
		if(_nbr <= -1)return;
		if(Effects.get(stat) != null && Effects.get(stat) != _nbr)
			Effects.put(stat,_nbr);
	}
	public void setStat2(final int stat, final int _nbr)
	{
		if(Effects.get(stat) == null)
		{
			Effects.put(stat,_nbr);
		}
		else
		{
			Effects.remove(stat);
			Effects.put(stat,_nbr);
		}
	}
	/*-----------------------FIN---------------------------------------*/
	
	public boolean isSameStats(final Stats other)
	{
		for(final Entry<Integer,Integer> entry : Effects.entrySet())
		{
			//Si la stat n'existe pas dans l'autre map
			if(other.getMap().get(entry.getKey()) == null)return false;
			//Si la stat existe mais n'a pas la même valeur
			if(other.getMap().get(entry.getKey()) != entry.getValue())return false;	
		}
		for(final Entry<Integer,Integer> entry : other.getMap().entrySet())
		{
			//Si la stat n'existe pas dans l'autre map
			if(Effects.get(entry.getKey()) == null)return false;
			//Si la stat existe mais n'a pas la même valeur
			if(Effects.get(entry.getKey()) != entry.getValue())return false;	
		}
		return true;
	}
	
	public int getEffect(final int id)
	{
		int val;
		if(Effects.get(id) == null)
			 val=0;
		else
			val = Effects.get(id);
		
		switch(id)//Bonus/Malus TODO
		{
			case Constants.STATS_ADD_AFLEE:
				if(Effects.get(Constants.STATS_REM_AFLEE)!= null)
					val -= (int)(getEffect(Constants.STATS_REM_AFLEE));
				if(Effects.get(Constants.STATS_ADD_SAGE) != null)
					val += (int)(getEffect(Constants.STATS_ADD_SAGE)/4);
			break;
			case Constants.STATS_ADD_MFLEE:
				if(Effects.get(Constants.STATS_REM_MFLEE)!= null)
					val -= (int)(getEffect(Constants.STATS_REM_MFLEE));
				if(Effects.get(Constants.STATS_ADD_SAGE) != null)
					val += (int)(getEffect(Constants.STATS_ADD_SAGE)/4);
			break;
			case Constants.STATS_ADD_INIT:
				if(Effects.get(Constants.STATS_REM_INIT)!= null)
					val -= Effects.get(Constants.STATS_REM_INIT);
			break;
			case Constants.STATS_ADD_AGIL:
				if(Effects.get(Constants.STATS_REM_AGIL)!= null)
					val -= Effects.get(Constants.STATS_REM_AGIL);
			break;
			case Constants.STATS_ADD_FORC:
				if(Effects.get(Constants.STATS_REM_FORC)!= null)
					val -= Effects.get(Constants.STATS_REM_FORC);
			break;
			case Constants.STATS_ADD_CHAN:
				if(Effects.get(Constants.STATS_REM_CHAN)!= null)
					val -= Effects.get(Constants.STATS_REM_CHAN);
			break;
			case Constants.STATS_ADD_INTE:
				if(Effects.get(Constants.STATS_REM_INTE)!= null)
				val -= Effects.get(Constants.STATS_REM_INTE);
			break;
			case Constants.STATS_ADD_PA:
				if(Effects.get(Constants.STATS_ADD_PA2)!= null)
					val += Effects.get(Constants.STATS_ADD_PA2);
				if(Effects.get(Constants.STATS_REM_PA)!= null)
					val -= Effects.get(Constants.STATS_REM_PA);
				if(Effects.get(Constants.STATS_REM_PA2)!= null)//Non esquivable
					val -= Effects.get(Constants.STATS_REM_PA2);
			break;
			case Constants.STATS_ADD_PM:
				if(Effects.get(Constants.STATS_ADD_PM2)!= null)
					val += Effects.get(Constants.STATS_ADD_PM2);
				if(Effects.get(Constants.STATS_REM_PM)!= null)
					val -= Effects.get(Constants.STATS_REM_PM);
				if(Effects.get(Constants.STATS_REM_PM2)!= null)//Non esquivable
					val -= Effects.get(Constants.STATS_REM_PM2);
			break;
			case Constants.STATS_ADD_PO:
				if(Effects.get(Constants.STATS_REM_PO)!= null)
					val -= Effects.get(Constants.STATS_REM_PO);
			break;
			case Constants.STATS_ADD_VITA:
				if(Effects.get(Constants.STATS_REM_VITA)!= null)
					val -= Effects.get(Constants.STATS_REM_VITA);
			break;
			case Constants.STATS_ADD_DOMA:
				if(Effects.get(Constants.STATS_REM_DOMA)!= null)
					val -= Effects.get(Constants.STATS_REM_DOMA);
			break;
			case Constants.STATS_ADD_PODS:
				if(Effects.get(Constants.STATS_REM_PODS)!= null)
					val -= Effects.get(Constants.STATS_REM_PODS);
			break;
			case Constants.STATS_ADD_PROS:
				if(Effects.get(Constants.STATS_REM_PROS)!= null)
					val -= Effects.get(Constants.STATS_REM_PROS);
				if (Effects.get(Constants.STATS_ADD_CHAN) != null)
					val += Effects.get(Constants.STATS_ADD_CHAN) / 10;
			break;
			case Constants.STATS_ADD_R_TER:
				if(Effects.get(Constants.STATS_REM_R_TER)!= null)
					val -= Effects.get(Constants.STATS_REM_R_TER);
			break;
			case Constants.STATS_ADD_R_EAU:
				if(Effects.get(Constants.STATS_REM_R_EAU)!= null)
					val -= Effects.get(Constants.STATS_REM_R_EAU);
			break;
			case Constants.STATS_ADD_R_AIR:
				if(Effects.get(Constants.STATS_REM_R_AIR)!= null)
					val -= Effects.get(Constants.STATS_REM_R_AIR);
			break;
			case Constants.STATS_ADD_R_FEU:
				if(Effects.get(Constants.STATS_REM_R_FEU)!= null)
					val -= Effects.get(Constants.STATS_REM_R_FEU);
			break;
			case Constants.STATS_ADD_R_NEU:
				if(Effects.get(Constants.STATS_REM_R_NEU)!= null)
					val -= Effects.get(Constants.STATS_REM_R_NEU);
			break;
			case Constants.STATS_ADD_RP_TER:
				if(Effects.get(Constants.STATS_REM_RP_TER)!= null)
					val -= Effects.get(Constants.STATS_REM_RP_TER);
			break;
			case Constants.STATS_ADD_RP_EAU:
				if(Effects.get(Constants.STATS_REM_RP_EAU)!= null)
					val -= Effects.get(Constants.STATS_REM_RP_EAU);
			break;
			case Constants.STATS_ADD_RP_AIR:
				if(Effects.get(Constants.STATS_REM_RP_AIR)!= null)
					val -= Effects.get(Constants.STATS_REM_RP_AIR);
			break;
			case Constants.STATS_ADD_RP_FEU:
				if(Effects.get(Constants.STATS_REM_RP_FEU)!= null)
					val -= Effects.get(Constants.STATS_REM_RP_FEU);
			break;
			case Constants.STATS_ADD_RP_NEU:
				if(Effects.get(Constants.STATS_REM_RP_NEU)!= null)
					val -= Effects.get(Constants.STATS_REM_RP_NEU);
			break;
		}
		return val;
	}
	
	public static Stats cumulStat(final Stats s1,final Stats s2)
	{
		final TreeMap<Integer,Integer> effects = new TreeMap<Integer,Integer>();
		for(int a = 0; a <= Constants.MAX_EFFECTS_ID; a++)
		{
			if((s1.Effects.get(a) == null  || s1.Effects.get(a) == 0) && (s2.Effects.get(a) == null || s2.Effects.get(a) == 0))
				continue;
			int som = 0;
			if(s1.Effects.get(a) != null)
				som += s1.Effects.get(a);
			
			if(s2.Effects.get(a) != null)
				som += s2.Effects.get(a);
			
			effects.put(a, som);
		}
		return new Stats(effects,false,null);
	}
	
	public Map<Integer, Integer> getMap()
	{
		return Effects;
	}
	public String parseToItemSetStats()
	{
		final StringBuilder str = new StringBuilder();
		for(final Entry<Integer,Integer> entry : Effects.entrySet())
		{
			if(str.length() >0)
				str.append(',');
			str.append(Integer.toHexString(entry.getKey())).append('#').append(Integer.toHexString(entry.getValue())).append("#0#0");
		}
		return str.toString();
	}
}
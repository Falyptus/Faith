package objects.spell;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


public class Spell {
	private final int spellID;
	private final int spriteID;
	private final String spriteInfos;
	private final Map<Integer,SpellStat> sortStats = new TreeMap<Integer,SpellStat>();
	private final ArrayList<Integer> effectTargets = new ArrayList<Integer>();
	private final ArrayList<Integer> CCeffectTargets = new ArrayList<Integer>();
	
	public Spell(final int aspellID, final int aspriteID, final String aspriteInfos,final String ET)
	{
		spellID = aspellID;
		spriteID = aspriteID;
		spriteInfos = aspriteInfos;
		final String nET = ET.split(":")[0];
		String ccET = "";
		if(ET.split(":").length>1)ccET = ET.split(":")[1];
		for(final String num : nET.split(";"))
		{
			try
			{
				effectTargets.add(Integer.parseInt(num));
			}catch(final Exception e)
			{
				effectTargets.add(0);
				continue;
			};
		}
		for(final String num : ccET.split(";"))
		{
			try
			{
				CCeffectTargets.add(Integer.parseInt(num));
			}catch(final Exception e)
			{
				CCeffectTargets.add(0);
				continue;
			};
		}
	}
	
	public ArrayList<Integer> getEffectTargets()
	{
		return effectTargets;
	}

	public int getSpriteID() {
		return spriteID;
	}

	public String getSpriteInfos() {
		return spriteInfos;
	}

	public int getSpellID() {
		return spellID;
	}
	
	public SpellStat getStatsByLevel(final int lvl) {
		return sortStats.get(lvl);
	}
	
	public void addSortStats(final Integer lvl,final SpellStat stats) {
		if(sortStats.get(lvl) != null)return;
		sortStats.put(lvl,stats);
	}
	
}

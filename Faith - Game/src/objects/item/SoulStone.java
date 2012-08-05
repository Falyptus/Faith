package objects.item;
import java.util.ArrayList;


import common.Constants;
import common.World;
import common.World.Couple;

public class SoulStone extends Item{
	private final ArrayList<Couple<Integer, Integer>> _monsters;
	
	public SoulStone (final int Guid, final int qua,final int template, final int pos, final String strStats)
	{
		this.guid = Guid;
		this.template = World.getItemTemplate(template);	//7010 = Pierre d'ame pleine
		this.quantity = 1;
		this.position = Constants.ITEM_POS_NO_EQUIPED;
		
		_monsters = new ArrayList<Couple<Integer, Integer>>();	//Couple<MonstreID,Level>
		parseStringToStats(strStats);
	}
	
	public void parseStringToStats(final String monsters) //Dans le format "monstreID,lvl|monstreID,lvl..."
	{
		final String[] split = monsters.split("\\|");
		for(final String s : split)
		{	
			try
			{
				final int monstre = Integer.parseInt(s.split(",")[0]);
				final int level = Integer.parseInt(s.split(",")[1]);
				
				_monsters.add(new Couple<Integer, Integer>(monstre, level));
				
			}catch(final Exception e){continue;};
		}
	}
	
	public String parseStatsString()
	{
		final StringBuilder stats = new StringBuilder(15*_monsters.size());
		boolean isFirst = true;
		for(final Couple<Integer, Integer> coupl : _monsters)
		{
			if(!isFirst)
				stats.append(',');
			
			try
			{
				stats.append("26f#0#0#").append(Integer.toHexString(coupl.first));
			}catch(final Exception e)
			{
				e.printStackTrace();
				continue;
			};
			
			isFirst = false;
		}
		return stats.toString();
	}
	
	public String parseGroupData()//Format : id,lvlMin,lvlMax;id,lvlMin,lvlMax...
	{
		final StringBuilder toReturn = new StringBuilder(15*_monsters.size());
		boolean isFirst = true;
		
		for(final Couple<Integer, Integer> curMob : _monsters)
		{
			if(!isFirst)
				toReturn.append(';');
			toReturn.append(curMob.first).append(',').append(curMob.second).append(',').append(curMob.second);
			isFirst = false;
		}
		return toReturn.toString();
	}
	
	public String parseToSave()
	{
		final StringBuilder toReturn = new StringBuilder(10*_monsters.size());
		boolean isFirst = true;
		for(final Couple<Integer, Integer> curMob : _monsters)
		{
			if(!isFirst)
				toReturn.append(';');
			toReturn.append(curMob.first).append(',').append(curMob.second);
			isFirst = false;
		}
		return toReturn.toString();
	}
}

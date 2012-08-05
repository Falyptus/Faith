package objects.monster;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import common.World.Drop;

public class Monster
{
	private final int ID;
	private final int gfxID;
	private final int align;
	private final String colors;
	private final int IAType;
	private final int minKamas;
	private final int maxKamas;
	private final Map<Integer,MonsterGrade> grades = new TreeMap<Integer,MonsterGrade>();
	private final ArrayList<Drop> drops = new ArrayList<Drop>();
	private final boolean isCapturable;
	
	public Monster(final int Aid, final int agfx, final int Aalign, final String Acolors, final String Agrades, final String Aspells,final String Astats,final String aPdvs,final String aPoints,final String aInit,final int mK,final int MK,final String xpstr, final int IAtype,final boolean capturable)
	{
		ID = Aid;
		gfxID = agfx;
		align = Aalign;
		colors = Acolors;
		minKamas = mK;
		maxKamas = MK;
		IAType = IAtype;
		isCapturable = capturable;
		int G = 1;
		for(int n = 0; n<11; n++)
		{
			try
			{
				//Grades
				final String grade = Agrades.split("\\|")[n];
				final String[] infos = grade.split("@");
				final int level = Integer.parseInt(infos[0]);
				final String resists = infos[1];
				//Stats
				final String stats =  Astats.split("\\|")[n];
				//Spells
				String spells =  Aspells.split("\\|")[n];
				if(spells.equals("-1"))spells ="";
				//PDVMax//init
				int pdvmax = 1;
				int init = 1;
				try
				{
					pdvmax = Integer.parseInt(aPdvs.split("\\|")[n]);
					init = Integer.parseInt(aInit.split("\\|")[n]);
				}catch(final Exception e){};
				//PA / PM
				int PA = 3;
				int PM = 3;
				int xp = 10;
				try
				{
					final String[] pts = aPoints.split("\\|")[n].split(";");
					try
					{
						PA = Integer.parseInt(pts[0]);
					}catch(final Exception e1){};
					try
					{
						PM = Integer.parseInt(pts[1]);
					}catch(final Exception e1){};
					try
					{
						xp = Integer.parseInt(xpstr.split("\\|")[n]);
					}catch(final Exception e1){e1.printStackTrace();};
				}catch(final Exception e){e.printStackTrace();};
				grades.put
					(G,
						new MonsterGrade
						(
							this,
							G,
							level,
							PA,
							PM,
							resists,
							stats,
							spells,
							pdvmax,
							init,
							xp
						)
					);
				G++;
			}catch(final Exception e){continue;};	
		}	
	}
	
	public int getID() {
		return ID;
	}
	public void addDrop(final Drop D)
	{
		drops.add(D);
	}
	public ArrayList<Drop> getDrops()
	{
		return drops;
	}
	public int getGfxID() {
		return gfxID;
	}
	
	public int getMinKamas() {
		return minKamas;
	}

	public int getMaxKamas() {
		return maxKamas;
	}

	public int getAlign() {
		return align;
	}
	
	public String getColors() {
		return colors;
	}
	
	public int getIAType() {
		return IAType;
	}
	
	public Map<Integer, MonsterGrade> getGrades() {
		return grades;
	}

	public MonsterGrade getGradeByLevel(final int lvl)
	{
		for(final Entry<Integer,MonsterGrade> grade : grades.entrySet())
		{
			if(grade.getValue().getLevel() == lvl)
				return grade.getValue();
		}
		return null;
	}

	public MonsterGrade getRandomGrade()
	{
		final int randomgrade = (int)(Math.random() * (6-1)) + 1; 
		int graderandom=1;
		for(final Entry<Integer,MonsterGrade> grade : grades.entrySet())
		{
			if(graderandom == randomgrade)
			{
				return grade.getValue();
			}
			else{
				graderandom++;
				}
		}
		return null;
	}
	
	public boolean isCapturable()
	{
		return this.isCapturable;
	}
}

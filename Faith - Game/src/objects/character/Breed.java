package objects.character;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import common.World.Couple;

public class Breed {
	
	private final int id;
	private final int startLife;
	private final int startPA;
	private final int startPM;
	private final int startInitiative;
	private final int startProspecting;
	private final int startMap;
	private final int startCell;
	private final BreedStat intelligence;
	private final BreedStat wisdom;
	private final BreedStat chance;
	private final BreedStat agility;
	private final BreedStat strenght;
	private final BreedStat vitality;
	
	public Breed(final int id, final int startLife, final int startPA, final int startPM, final int startInitiative, final int startProspecting, final int startMap, final int startCell,
			final String intelligence, final String wisdom, final String chance, final String agility, final String strenght, final String vitality) {
		this.id = id;
		this.startLife = startLife;
		this.startPA = startPA;
		this.startPM = startPM;
		this.startInitiative = startInitiative;
		this.startProspecting = startProspecting;
		this.startMap = startMap;
		this.startCell = startCell;
		this.intelligence = BreedStat.parse(intelligence);
		this.wisdom = BreedStat.parse(wisdom);
		this.chance = BreedStat.parse(chance);
		this.agility = BreedStat.parse(agility);
		this.strenght = BreedStat.parse(strenght);
		this.vitality = BreedStat.parse(vitality);
	}
	

	public int getId() {
        return id;
    }

	public int getStartLife() {
        return startLife;
    }

	public int getStartPA() {
        return startPA;
    }

	public int getStartPM() {
        return startPM;
    }

	public int getStartInitiative() {
        return startInitiative;
    }

	public int getStartProspecting() {
        return startProspecting;
    }

	public BreedStat getIntelligence() {
        return intelligence;
    }

	public BreedStat getWisdom() {
        return wisdom;
    }

	public BreedStat getChance() {
        return chance;
    }

	public BreedStat getAgility() {
        return agility;
    }

	public BreedStat getStrenght() {
        return strenght;
    }

	public BreedStat getVitality() {
        return vitality;
    }

	public int getStartMap() {
		return startMap;
	}

	public int getStartCell() {
		return startCell;
	}
	
	/*public static class BreedStat
	{
		private Map<int[], int[]> floors;
		
		public BreedStat() 
		{
			floors = new TreeMap<int[], int[]>();
		}
		
		public int[] getPoints(int val) 
		{
			for(Entry<int[], int[]> entry : floors.entrySet()) 
			{
				if(entry.getKey()[0] <= val && (entry.getKey().length == 1 || entry.getKey()[1] >= val)) 
				{
					return entry.getValue();
				}
			}
			return null;
		}
		
		public static BreedStat parse(String data) 
		{
			BreedStat breedStat = new BreedStat();
			String[] split = data.split("\\|");
			for(String code : split) 
			{
				String[] floor = code.split(":");
				int[] quota = new int[2];
				int[] points = new int[2];
				if(quota.length > 0)
					quota[0] = Integer.parseInt(floor[0].split(",")[0]);
				if(quota.length > 1)
					quota[1] = Integer.parseInt(floor[0].split(",")[1]);
				points[0] = Integer.parseInt(floor[1].split("-")[0]);
				points[1] = Integer.parseInt(floor[1].split("-")[1]);
				breedStat.getFloors().put(quota, points);
			}
			return breedStat;
		}

		public Map<int[], int[]> getFloors() 
		{
	        return floors;
        }		
	}*/
	
	public static class BreedStat
	{
		private Map<Couple<Integer, Integer>, Couple<Integer, Integer>> floors;
		
		public BreedStat()
		{
			floors = new TreeMap<Couple<Integer, Integer>, Couple<Integer, Integer>>();
		}
		
		public static BreedStat parse(final String data) 
		{
			/*Map<Couple<Integer, Integer>, Couple<Integer, Integer>> floors =
					new TreeMap<Couple<Integer, Integer>, Couple<Integer, Integer>>();
			BreedStat breedFloor = new BreedStat();
			String[] split = data.split("\\|");
			for(String code : split) 
			{
				String[] floor = code.split(":");
				Couple<Integer, Integer> quota = new Couple<Integer, Integer>(-1, -1);
				Couple<Integer, Integer> points = new Couple<Integer, Integer>(-1, -1);
				if(quota.first == -1)
					quota.first = Integer.parseInt(floor[0].split(",")[0]);
				if(quota.second == -1)
					quota.second = Integer.parseInt(floor[0].split(",")[1]);
				points.first = Integer.parseInt(floor[1].split("-")[0]);
				points.second = Integer.parseInt(floor[1].split("-")[1]);
				floors.put(quota, points);
			}
			breedFloor.setFloors(floors);*/
			/*BreedStat breedFloor = new BreedStat();
			String[] split = data.split("\\|");
			for(String code : split) 
			{
				String[] floor = code.split(":");
				Couple<Integer, Integer> quota = new Couple<Integer, Integer>(-1, -1);
				Couple<Integer, Integer> points = new Couple<Integer, Integer>(-1, -1);
				if(quota.first == -1)
					quota.first = Integer.parseInt(floor[0].split(",")[0]);
				if(quota.second == -1)
					quota.second = Integer.parseInt(floor[0].split(",")[1]);
				points.first = Integer.parseInt(floor[1].split("-")[0]);
				points.second = Integer.parseInt(floor[1].split("-")[1]);
				breedFloor.getFloors().put(quota, points);
			}*/
			return new BreedStat();
		}
		
		public Couple<Integer, Integer> getPoints(final int val) 
		{
			for(final Entry<Couple<Integer, Integer>, Couple<Integer, Integer>> entry : floors.entrySet()) 
			{
				if(entry.getKey().first <= val && entry.getKey().second >= val)
				{
					return entry.getValue();
				}
			}
			return null;
		}

		public Map<Couple<Integer, Integer>, Couple<Integer, Integer>> getFloors() {
			return floors;
		}	
		
		public void setFloors(final Map<Couple<Integer, Integer>, Couple<Integer, Integer>> floors) {
			this.floors = floors;
		}
		
	}

}

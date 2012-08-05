package objects.monster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import objects.GameActor;
import objects.map.DofusMap;

import common.Config;
import common.Constants;
import common.World;
import common.utils.Formulas;

public class MonsterGroup implements GameActor
{
	private int id;
	private int cellID;
	private byte orientation = 2;
	private int align = -1;
	private int aggroDistance = 0;
	private boolean isFix = false;
	private final Map<Integer,MonsterGrade> _Mobs = new TreeMap<Integer,MonsterGrade>();
	private int _size = 0;
	private String condition = "";
	private Timer _condTimer;
	private int _starBonus = 0;
	private long _expLoot = 0;
	
	/*MARTHIEUBEAN*/
	public MonsterGroup(final int Aid,final int Aalign, final ArrayList<MonsterGrade> possibles,final DofusMap Map,final int cell,final int maxSize)
	{
		id = Aid;
		align = Aalign;
		//Détermination du nombre de mob du groupe
		int rand = 0;
		int nbr = 0;
		
		switch (maxSize)
		{
			case 0:
				return;
			case 1:
				nbr = 1;
				break;
			case 2:
				nbr = Formulas.getRandomValue(1,2);	//1:50%	2:50%
				break;
			case 3:
				nbr = Formulas.getRandomValue(1,3);	//1:33.3334%	2:33.3334%	3:33.3334%
				break;
			case 4:
				rand = Formulas.getRandomValue(0, 99);
				if(rand < 22)		//1:22%
					nbr = 1;
				else if(rand < 48)	//2:26%
					nbr = 2;
				else if(rand < 74)	//3:26%
					nbr = 3;
				else				//4:26%
					nbr = 4;
				break;
			case 5:
				rand = Formulas.getRandomValue(0, 99);
				if(rand < 15)		//1:15%
					nbr = 1;
				else if(rand < 35)	//2:20%
					nbr = 2;
				else if(rand < 60)	//3:25%
					nbr = 3;
				else if(rand < 85)	//4:25%
					nbr = 4;
				else				//5:15%
					nbr = 5;
				break;
			case 6:
				rand = Formulas.getRandomValue(0, 99);
				if(rand < 10)		//1:10%
					nbr = 1;
				else if(rand < 25)	//2:15%
					nbr = 2;
				else if(rand < 45)	//3:20%
					nbr = 3;
				else if(rand < 65)	//4:20%
					nbr = 4;
				else if(rand < 85)	//5:20%
					nbr = 5;
				else				//6:15%
					nbr = 6;
				break;
			case 7:
				rand = Formulas.getRandomValue(0, 99);
				if(rand < 9)		//1:9%
					nbr = 1;
				else if(rand < 20)	//2:11%
					nbr = 2;
				else if(rand < 35)	//3:15%
					nbr = 3;
				else if(rand < 55)	//4:20%
					nbr = 4;
				else if(rand < 75)	//5:20%
					nbr = 5;
				else if(rand < 91)	//6:16%
					nbr = 6;
				else				//7:9%
					nbr = 7;
				break;
			default:
				rand = Formulas.getRandomValue(0, 99);
				if(rand < 9)		//1:9%
					nbr = 1;
				else if(rand<20)	//2:11%
					nbr = 2;
				else if(rand<33)	//3:13%
					nbr = 3;
				else if(rand<50)	//4:17%
					nbr = 4;
				else if(rand<67)	//5:17%
					nbr = 5;
				else if(rand<80)	//6:13%
					nbr = 6;
				else if(rand<91)	//7:11%
					nbr = 7;
				else				//8:9%
					nbr = 8;
				break;
		}
		
		//On vérifie qu'il existe des monstres de l'alignement demandé pour éviter les boucles infinies
		boolean haveSameAlign = false;
		for(final MonsterGrade mob : possibles)
		{
			if(mob.getTemplate().getAlign() == align)
				haveSameAlign = true;
		}
		
		if(!haveSameAlign)return;//S'il n'y en a pas
		int guid = -1;
		
		int maxLevel = 0;
		for(int a =0; a<nbr;a++)
		{
			MonsterGrade Mob = null;
			do
			{
				final int random = Formulas.getRandomValue(0, possibles.size()-1);//on prend un mob au hasard dans le tableau
				Mob = possibles.get(random).getCopy();	
			}while(Mob.getTemplate().getAlign() != align);
			
			if(Mob.getLevel() > maxLevel)
				maxLevel = Mob.getLevel();
			
			_Mobs.put(guid, Mob);
			guid--;
		}
		aggroDistance = Constants.getAggroByLevel(maxLevel);
		
		if(align != Constants.ALIGNEMENT_NEUTRE)
			aggroDistance = 15;
		
		cellID = (cell==-1?Map.getRandomFreeCellID():cell);
		if(cellID == 0)return;
		_size = _Mobs.size();
		orientation = (byte) Formulas.getRandomOrientation();
		isFix = false;
	}
	/*FIN*/
	
	public MonsterGroup(final int Aid, final int cID, final String groupData)
	{
		final int maxLevel = 0;
		id = Aid;
		align = Constants.ALIGNEMENT_NEUTRE;
		cellID = cID;
		aggroDistance = Constants.getAggroByLevel(maxLevel);
		isFix = true;
		int guid = -1;
		
		for(final String data : groupData.split(";"))//Format : id,lvlMin,lvlMax;id,lvlMin,lvlMax...
		{
			final String[] infos = data.split(",");
			try
			{
				final int id = Integer.parseInt(infos[0]);
				final int min = Integer.parseInt(infos[1]);
				final int max = Integer.parseInt(infos[2]);
				final Monster m = World.getMonstre(id);
				final List<MonsterGrade> mgs = new ArrayList<MonsterGrade>();
				//on ajoute a la liste les grades possibles
				for(final MonsterGrade MG : m.getGrades().values())if(MG.level >=min && MG.level<=max)mgs.add(MG);
				if(mgs.size() == 0)continue;
				//On prend un grade au hasard entre 0 et size -1 parmis les mobs possibles
				_Mobs.put(guid, mgs.get(Formulas.getRandomValue(0, mgs.size()-1)));
				guid--;
			}catch(final Exception e){continue;};
		}
		orientation = (byte) Formulas.getRandomOrientation();
	}
	
	public byte getOrientation()
	{
		return orientation;
	}
	
	public int getAggroDistance()
	{
		return aggroDistance;
	}
	public boolean isFix()
	{
		return isFix;
	}
	public void setOrientation(final byte o)
	{
		orientation = o;
	}
	
	public void setCellID(final int id)
	{
		cellID = id;
	}
	
	public int getAlignement()
	{
		return align;
	}
	
	public MonsterGrade getMobGradeByID(final int id)
	{
		return _Mobs.get(id);
	}
	
	public int getSize()
	{
		return _size;
	}

	public String parseGM()
	{
		final StringBuilder mobIDs = new StringBuilder(2*getSize());
		final StringBuilder mobGFX = new StringBuilder(3*getSize());
		final StringBuilder mobLevels = new StringBuilder(2*getSize());
		final StringBuilder colors = new StringBuilder(15*getSize());
		boolean isFirst = true;
		if(getMobs().size() == 0)return "";
		
		for(final Entry<Integer,MonsterGrade> entry : getMobs().entrySet())
		{
			if(!isFirst)
			{
				mobIDs.append(',');
				mobGFX.append(',');
				mobLevels.append(',');
			}
			mobIDs.append(entry.getValue().getTemplate().getID());
			mobGFX.append(entry.getValue().getTemplate().getGfxID()).append('^').append(entry.getValue().getSize());
			mobLevels.append(entry.getValue().getLevel());
			colors.append(entry.getValue().getTemplate().getColors()).append(";0,0,0,0;");
			
			isFirst = false;
		}
		return new StringBuilder("|+").append(getCellId()).append(';').append(getOrientation()).append(';').append(getStarPercent()).append(';').append(getActorId()).append(';').append(mobIDs).append(";-3;").append(mobGFX).append(';').append(mobLevels).append(';').append(colors).toString();
	}

	public Map<Integer, MonsterGrade> getMobs() {
		return _Mobs;
	}
	
	public void setCondition(final String cond)
	{
		this.condition = cond;
	}
	public String getCondition()
	{
		return this.condition;
	}
	
	public void setIsFix(final boolean fix)
	{
		this.isFix = fix;
	}
	
	public void startCondTimer()
	{
		this._condTimer = new Timer();
		_condTimer.schedule(new TimerTask() 
		{
			public void run() 
			{
				condition = "";
			}
		}, Config.CONFIG_ARENA_TIMER);
	}
	public void stopConditionTimer()
	{
		try
		{
			this._condTimer.cancel();
		}catch(final Exception e)
		{
			
		}
	}

	public void addStarPercent() {
		_starBonus++;
	}
	
	public int getStarPercent()
	{
		return _starBonus;
	}

	public void addXP(final long totalXP) {
		setExpLoot(getExpLoot() + totalXP);
	}

	public long getExpLoot() {
		return _expLoot;
	}

	public void setExpLoot(final long _expLoot) {
		this._expLoot = _expLoot;
	}

	@Override
	public int getActorType() {
		return GameActorTypeEnum.TYPE_MONSTER.getActorType();
	}

	@Override
	public int getMapId() {
		return -1;
	}

	@Override
	public int getCellId() {
		return this.cellID;
	}

	@Override
	public int getActorId() {
		return id;
	}
}
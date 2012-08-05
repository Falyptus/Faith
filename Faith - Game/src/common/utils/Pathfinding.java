package common.utils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import objects.fight.Fight;
import objects.fight.Fighter;
import objects.fight.Trap;
import objects.map.DofusCell;
import objects.map.DofusMap;

import common.Constants;
import common.World.Couple;
import common.console.Log;

public class Pathfinding {

	private static Integer _nSteps = Integer.valueOf(0);

	public static int isValidPath(final DofusMap map, final int cellID, final AtomicReference<String> pathRef,final Fight fight)
	{
		synchronized(_nSteps)
		{
			_nSteps = 0;
			int newPos = cellID;
	        int Steps = 0;
	        final String path = pathRef.get();
	        final StringBuilder newPath = new StringBuilder();
	        for (int i = 0; i < path.length(); i += 3)
	        {
	        	final String SmallPath = path.substring(i, i+3);
	        	final char dir = SmallPath.charAt(0);
	        	final int dirCaseID = CryptManager.cellCodeToID(SmallPath.substring(1));
	        	_nSteps = 0;
	        	//Si en combat et Si Pas début du path, on vérifie tacle
	    		if(fight != null && i != 0 && getEnnemyFighterArround(newPos, map, fight) != null)
	    		{
	    			pathRef.set(newPath.toString());
	    			return Steps;
	    		}
	        	//Si en combat, et pas au début du path
	    		if(fight != null && i != 0)
	    		{
	    			for(final Trap p : fight.getTraps())
	    			{
	    				final int dist = getDistanceBetween(map,p.getCell().getId(),newPos);
	    				if(dist <= p.getSize())
	    				{
	    					//on arrete le déplacement sur la 1ere case du piege
	    					pathRef.set(newPath.toString());
	    	    			return Steps;
	    				}
	    			}
	    		}
	    		
	        	final String[] aPathInfos = validSinglePath(newPos, SmallPath, map, fight).split(":");
	    		if(aPathInfos[0].equalsIgnoreCase("stop"))
	    		{
	    			newPos = Integer.parseInt(aPathInfos[1]);
	    			Steps += _nSteps;
	    			newPath.append(dir).append(CryptManager.cellIDToCode(newPos));
	    			pathRef.set(newPath.toString());
	    			return -Steps;
	    		}else if (aPathInfos[0].equalsIgnoreCase("ok"))
	    		{
	    			newPos = dirCaseID;
	    			Steps += _nSteps;
	    		}
	    		else
	    		{
	    			pathRef.set(newPath.toString());
	    			return -1000;
	    		}
	    		newPath.append(dir).append(CryptManager.cellIDToCode(newPos));
	    	}
	        pathRef.set(newPath.toString());
	        return Steps;
		}
	}
	
	public static Fighter getEnnemyFighterArround(final int cellID,final DofusMap map,final Fight fight)
	{
		final char[] dirs = {'b','d','f','h'};
		for(final char dir : dirs)
		{
			final Fighter fighter = map.getCell(getCaseIDFromDirrection(cellID, dir, map, false)).getFirstFighter();
			if(fighter != null)
			{
				if(fighter.getTeam() != fight.getCurFighter().getTeam())
					return fighter;
			}
		}
		return null;
	}
	
	public static ArrayList<Fighter> getEnnemiesFighterArround(int cellID,DofusMap map,Fight fight)
	{
		char[] dirs = {'b','d','f','h'};
		ArrayList<Fighter> enemy = new ArrayList<Fighter>();
		
		for(char dir : dirs)
		{
			Fighter f = map.getCell(getCaseIDFromDirrection(cellID, dir, map, false)).getFirstFighter();
			if(f != null)
			{
				if(f.getTeam() != fight.getCurFighter().getTeam())
					enemy.add(f);
			}
		}
		if(enemy.size() == 0 || enemy.size() == 4) 
			return null;
		
		return enemy;
	}

	public static String validSinglePath(final int CurrentPos, final String Path, final DofusMap map, final Fight fight)
	{
		_nSteps = 0;
        final char dir = Path.charAt(0);
        final int dirCaseID = CryptManager.cellCodeToID(Path.substring(1));
        if(fight != null && fight.isOccuped(dirCaseID))
        	return "no:";
        int lastPos = CurrentPos;
        for (_nSteps = 1; _nSteps <= 64; _nSteps++)
        {
        	if (getCaseIDFromDirrection(lastPos, dir, map, fight!=null) == dirCaseID)
            {
            	if(fight != null && fight.isOccuped(dirCaseID))return "stop:"+lastPos;
            	if(fight == null && map.getCell(lastPos).isTrigger())return "stop:"+lastPos; //On force le trigger
            	if(map.getCell(dirCaseID).isWalkable(true))return "ok:";
            	else
            	{
            		_nSteps--;
            		return ("stop:"+lastPos);
            	}
            }
            else lastPos = getCaseIDFromDirrection(lastPos, dir, map, fight!=null);
        	
            if(fight != null && fight.isOccuped(lastPos))
            {
            	return "no:";
           	}
            if(fight != null)
            {
	            if(getEnnemyFighterArround(lastPos, map, fight) != null)//Si ennemie proche
	            {
	            	return "stop:"+lastPos;
	            }
    			for(final Trap p : fight.getTraps())
    			{
    				final int dist = getDistanceBetween(map,p.getCell().getId(),lastPos);
    				if(dist <= p.getSize())
    				{
    					//on arrete le déplacement sur la 1ere case du piege
    					return "stop:"+lastPos;
    				}
    			}
            }
            
        }
        return "no:";
	}

	public static int getCaseIDFromDirrection(final int CaseID, final char Direction,final DofusMap map, final boolean Combat)
	{
		switch (Direction)
        {
            case 'a':
                return Combat ? -1 : CaseID + 1;
            case 'b':
                return CaseID + map.getW();
            case 'c':
                return Combat ? -1 : CaseID + (map.getW() * 2 - 1);
            case 'd':
                return  CaseID + (map.getW() - 1);
            case 'e':
                return Combat ? -1 : CaseID - 1;
            case 'f':
                return CaseID - map.getW();
            case 'g':
                return Combat ? -1 : CaseID - (map.getW() * 2 - 1);
            case 'h':
                return  CaseID - map.getW() + 1;
            default:
            	break;
        }
        return -1; 
	}
	
	public static int getDistanceBetween(final DofusMap map,final int id1,final int id2)
	{
		if(id1 == id2)return 0;
		final int diffX = Math.abs(getCellXCoord(map, id1) - getCellXCoord(map,id2));
		final int diffY = Math.abs(getCellYCoord(map, id1) - getCellYCoord(map,id2));
		return (diffX + diffY);
	}
	
	public static boolean isNextTo (int cell1, int cell2, DofusMap map)
	{
		int width = map.getW();
		if(cell1 + (width-1) == cell2 || cell1 + width == cell2 || cell1 - (width-1) == cell2 || cell1 - width == cell2)
			return true;
		else
			return false;
	}

	public static int newCaseAfterPush(final DofusMap map, final DofusCell CCase,final DofusCell TCase, final int value, Fight fight)
	{
		int l_value = value;
		//Si c'est les memes case, il n'y a pas a bouger
		if(CCase.getId() == TCase.getId())return 0;
		char c = getDirBetweenTwoCase(CCase.getId(), TCase.getId(), map, true);
		int id = TCase.getId();
		if(l_value <0)
		{
			c = getOpositeDirection(c);
			l_value = -l_value;
		}
		for(int a = 0; a<l_value;a++)
		{
			final int nextCase = getCaseIDFromDirrection(id, c, map, true);
			if(map.getCell(nextCase) != null && map.getCell(nextCase).isWalkable(true) && map.getCell(nextCase).getFighters().size()==0)
			{
				id = nextCase;
				for(Trap trap : fight.getTraps()) 
				{
					int dist = Pathfinding.getDistanceBetween(map, trap.getCell().getId(), id);
					if(dist <= trap.getSize())
					{
						return id;
					}
				}
			}
			else
				return -(l_value-a);
		}
		
		if(id == TCase.getId())
			id = 0;
		return id;
	}
	
	public static Couple<Integer, String> getSpecialPath(final DofusMap carte, final char dir, final int curCell, final int nbCellMax)
	{
		int l_curCell = curCell;
		final StringBuilder path = new StringBuilder();
		int targetCell = 0;
		for(int i = 0; i < nbCellMax; i++)
		{
			targetCell = getCaseIDFromDirrection(l_curCell, dir, carte, false);
			if(carte.getCell(targetCell) == null)
			{
				continue;
			}
			if(carte.getCell(targetCell).isWalkable(true))
			{
				l_curCell = targetCell;
				path.append(dir).append(CryptManager.cellIDToCode(l_curCell));
			}else
			{
				break; //On s'arrête sur l'actuelle cellule
			}
		}
		return new Couple<Integer, String>(targetCell, path.toString());
	}
	
	public static int fourViewOrientation(final int o) {
		int FourView = o;
		switch (o) {
			case 0:
				FourView = 1;
				break;
			case 2:
				FourView = 3;
				break;
			case 4:
				FourView = 5;
				break;
			case 6:
				FourView = 7;
				break;
			default:
				break;
		}
		return FourView;
    }
	
	public static int getOrientationFromDir(final char c) {
		switch(c){
		case 'b' :
			return 1;
		case 'd' :
			return 3;
		case 'f' :
			return 5;
		case 'h' :
			return 7;
		default :
			return 3;
		}
	}
	
	private static char getOpositeDirection(final char c)
	{
		switch(c)
		{
			case 'a':
				return 'e';
			case 'b':
				return 'f';
			case 'c':
				return 'g';
			case 'd':
				return 'h';
			case 'e':
				return 'a';
			case 'f':
				return 'b';
			case 'g':
				return 'c';
			case 'h':
				return 'd';
			default:
				break;
		}
		return 0x00;
	}

	public static boolean casesAreInSameLine(final DofusMap map,final int c1,final int c2,final char dir)
	{
		int cell1 = c1;
		if(cell1 == c2)
			return true;
		
		if(dir != 'z')//Si la direction est définie
		{
			for(int a = 0;a<70;a++)
			{
				if(getCaseIDFromDirrection(cell1, dir, map, true) == c2)
					return true;
				if(getCaseIDFromDirrection(cell1, dir, map, true) == -1)
					break;
				cell1 = getCaseIDFromDirrection(cell1, dir, map, true);
			}
		}else//Si on doit chercher dans toutes les directions
		{
			final char[] dirs = {'b','d','f','h'};
			for(final char d : dirs)
			{
				int c = cell1;
				for(int a = 0;a<70;a++)
				{
					if(getCaseIDFromDirrection(c, d, map, true) == c2)
						return true;
					c = getCaseIDFromDirrection(c, d, map, true);
				}
			}
		}
		return false;
	}

	public static ArrayList<Fighter> getCiblesByZoneByWeapon(final Fight fight,final int type,final DofusCell cell,final int castCellID)
	{
		final ArrayList<Fighter> cibles = new ArrayList<Fighter>();
		final char c = getDirBetweenTwoCase(castCellID,cell.getId(),fight.getMap(),true);
		if(c == 0)
		{
			if(cell.getFirstFighter() != null) cibles.add(cell.getFirstFighter());
			return cibles;
		}
		
		switch(type)
		{
			//Cases devant celle ou l'on vise
			case Constants.ITEM_TYPE_MARTEAU:
				final Fighter f = getFighter2CellBefore(castCellID,c,fight.getMap());
				if(f != null)
					cibles.add(f);
				final Fighter g = get1StFighterOnCellFromDirection(fight.getMap(),castCellID,(char)(c-1)); 
				if(g != null)
					cibles.add(g);//Ajoute case a gauche
				final Fighter h = get1StFighterOnCellFromDirection(fight.getMap(),castCellID,(char)(c+1)); 
				if(h != null)
					cibles.add(h);//Ajoute case a droite
				final Fighter i = cell.getFirstFighter();
				if(i != null)
					cibles.add(i);
			break;
			case Constants.ITEM_TYPE_BATON:
				final Fighter j = get1StFighterOnCellFromDirection(fight.getMap(),castCellID,(char)(c-1)); 
				if(j != null)
					cibles.add(j);//Ajoute case a gauche
				final Fighter k = get1StFighterOnCellFromDirection(fight.getMap(),castCellID,(char)(c+1)); 
				if(k != null)
					cibles.add(k);//Ajoute case a droite
				
				final Fighter l = cell.getFirstFighter();
				if(l != null)
					cibles.add(l);//Ajoute case cible
			break;
			case Constants.ITEM_TYPE_PIOCHE:
			case Constants.ITEM_TYPE_EPEE:
			case Constants.ITEM_TYPE_FAUX:
			case Constants.ITEM_TYPE_DAGUES:
			case Constants.ITEM_TYPE_BAGUETTE:
			case Constants.ITEM_TYPE_PELLE:
			case Constants.ITEM_TYPE_ARC:
			case Constants.ITEM_TYPE_HACHE:
				final Fighter m = cell.getFirstFighter();
				if(m != null)
					cibles.add(m);
			break;
			default:
				break;
		}
		return cibles;
	}

	private static Fighter get1StFighterOnCellFromDirection(final DofusMap map, final int id, final char c)
	{ 
		char direction = c;
		if(direction == (char)('a'-1))
			direction = 'h';
		if(direction == (char)('h'+1))
			direction = 'a';
		return map.getCell(getCaseIDFromDirrection(id,direction,map,false)).getFirstFighter();
	}

	private static Fighter getFighter2CellBefore(final int CellID, final char c,final DofusMap map)
	{
		final int new2CellID = getCaseIDFromDirrection(getCaseIDFromDirrection(CellID,c,map,false),c,map,false);
		return map.getCell(new2CellID).getFirstFighter();
	}

	public static char getDirBetweenTwoCase(final int cell1ID, final int cell2ID,final DofusMap map, final boolean Combat)
	{
		final ArrayList<Character> dirs = new ArrayList<Character>();
		dirs.add('b');
		dirs.add('d');
		dirs.add('f');
		dirs.add('h');
		if(!Combat)
		{
			dirs.add('a');
			dirs.add('b');
			dirs.add('c');
			dirs.add('d');
		}
		for(final char c : dirs)
		{
			int cell = cell1ID;
			for(int i = 0; i <= 64; i++)
			{
				if(getCaseIDFromDirrection(cell, c, map, Combat) == cell2ID)
					return c;
				cell = getCaseIDFromDirrection(cell, c, map, Combat);
			}
		}
		return 0;
	}

	public static ArrayList<DofusCell> getCellListFromAreaString(final DofusMap map,final int cellID,final int castCellID, final String zoneStr, final int PONum, final boolean isCC)
	{
		int l_cellID = cellID;
		final ArrayList<DofusCell> cases = new ArrayList<DofusCell>();
		final int c = PONum;
		if(map.getCell(l_cellID) == null)return cases;
		cases.add(map.getCell(l_cellID));
		
		final int taille = CryptManager.getIntByHashedValue(zoneStr.charAt(c+1));
		switch(zoneStr.charAt(c))
		{
			case 'C'://Cercle
				for(int a = 0; a < taille;a++)
				{
					final char[] dirs = {'b','d','f','h'};
					final ArrayList<DofusCell> cases2 = new ArrayList<DofusCell>();//on évite les modifications concurrentes
					cases2.addAll(cases);
					for(final DofusCell aCell : cases2)
					{
						for(final char d : dirs)
						{
							final DofusCell cell = map.getCell(Pathfinding.getCaseIDFromDirrection(aCell.getId(), d, map, true));
							if(cell == null)continue;
							if(!cases.contains(cell))
								cases.add(cell);
						}
					}
				}
			break;
			
			case 'X'://Croix
				final char[] dirs = {'b','d','f','h'};
				for(final char d : dirs)
				{
					int cID = l_cellID;
					for(int a = 0; a< taille; a++)
					{
						cases.add(map.getCell(getCaseIDFromDirrection(cID, d, map, true)));
						cID = getCaseIDFromDirrection(cID, d, map, true);
					}
				}
			break;
			
			case 'L'://Ligne
				final char dir = Pathfinding.getDirBetweenTwoCase(castCellID, l_cellID, map,true);
				for(int a = 0; a< taille; a++)
				{
					cases.add(map.getCell(getCaseIDFromDirrection(l_cellID, dir, map, true)));
					l_cellID = getCaseIDFromDirrection(l_cellID, dir, map, true);
				}
			break;
			
			case 'P'://Player?
				
			break;
			
			default:
				Log.addToErrorLog("[FIXME]Type de portée non reconnue: "+zoneStr.charAt(0));
			break;
		}
		return cases;
	}

	public static int getCellXCoord(final DofusMap map, final int cellID)
	{
		final int w = map.getW();
		return ((cellID - (w -1) * getCellYCoord(map,cellID)) / w);
	}
	
	public static int getCellYCoord(final DofusMap map, final int cellID)
	{
		final int w = map.getW();
		final int loc5 = (int)(cellID/ ((w*2) -1));
		final int loc6 = cellID - loc5 * ((w * 2) -1);
		final int loc7 = loc6 % w;
		return (loc5 - loc7);
	}
	
	public static boolean checkLoS(final DofusMap map, final int cell1, final int cell2, final Fighter fighter)
	{
		if (fighter.isPlayer())
			return true;
		int dist = Pathfinding.getDistanceBetween(map, cell1, cell2);
		ArrayList<Integer> los = new ArrayList<Integer>();
		if (dist > 2)
			los = getLineOfSight(cell1, cell2, map);
		if (los != null && dist > 2) {
			for (int i : los) {
				if (i != cell1 && i != cell2 && !map.getCell(i).blockLoS())
					return false;
			}
		}
		if (dist > 2) {
			int cell = getNearestCellAround(map, cell2, cell1, null);
			if (cell != -1 && !map.getCell(cell).blockLoS())
				return false;
		}
		return true;
	}
	
	public static ArrayList<Integer> getLineOfSight(int cell1, int cell2, DofusMap map) {
		ArrayList<Integer> lineOfSight = new ArrayList<Integer>();
		int cell = cell1;
		boolean next = false;
		int width = map.getW();
		int height = map.getH();
		int lastCellId = map.getLastCellId();
		int[] dir1 = { 1, -1, (width + height), - (width + height), width, (width - 1), - (width), - (width - 1) };
		for (int i : dir1) {
			lineOfSight.clear();
			cell = cell1;
			lineOfSight.add(cell);
			next = false;
			while (!next) {
				cell += i;
				lineOfSight.add(cell);
				if (isBord1(cell) || isBord2(cell) || cell <= 0 || cell >= lastCellId)
					next = true;
				if (cell == cell2) {
					return lineOfSight;
				}
			}
		}
		return null;
	}
	
	public static boolean isBord1(int id)
	{
		int[] bords = {1,30,59,88,117,146,175,204,233,262,291,320,349,378,407,436,465,15,44,73,102,131,160,189,218,247,276,305,334,363,392,421,450,479};
		ArrayList <Integer> test = new ArrayList <Integer>();
		for(int i : bords)
		{
			test.add(i);
		}
		
		if(test.contains(id))
			return true;
		else 
			return false;
	}
	
	public static boolean isBord2(int id)
	{
		int[] bords = {16,45,74,103,132,161,190,219,248,277,306,335,364,393,422,451,29,58,87,116,145,174,203,232,261,290,319,348,377,406,435,464};
		ArrayList <Integer> test = new ArrayList <Integer>();
		for(int i : bords)
		{
			test.add(i);
		}
		
		if(test.contains(id))
			return true;
		else 
			return false;
	}

	public static int getNearestCellAround(final DofusMap map,final int startCell, final int endCell, ArrayList<DofusCell> forbidens)
	{
		//On prend la cellule autour de la cible, la plus proche
		int dist = 1000;
		int cellID = startCell;
		if(forbidens == null)forbidens = new ArrayList<DofusCell>();
		final char[] dirs = {'b','d','f','h'};
		for(final char d : dirs)
		{
			final int c = Pathfinding.getCaseIDFromDirrection(startCell, d, map, true);
			final int dis = Pathfinding.getDistanceBetween(map, endCell, c);
			if(dis < dist && map.getCell(c).isWalkable(true) && map.getCell(c).getFirstFighter() == null && !forbidens.contains(map.getCell(c)))
			{
				dist = dis;
				cellID = c;
			}
		}
		//On renvoie -1 si pas trouvé
		return cellID==startCell?-1:cellID;
	}
	public static int getNearestCellAround2(DofusMap map, int startCell, int finalCell, int minPO, int maxPO) {
		int dist = 1000;
		int cellId = startCell;
		char dir = getDirBetweenTwoCase(startCell, finalCell, map, false);
		int startCell2 = startCell;
		int nextCell = 0;
		int i = 0;
		while(i < maxPO)
		{
			nextCell = Pathfinding.getCaseIDFromDirrection(startCell2, dir, map, true);
			startCell2 = nextCell;
			i++;
			if (i > minPO) {
				DofusCell C = map.getCell(nextCell);
				if (C == null)
					continue;
				if (C.isWalkable(true) && C.getFirstFighter() == null)
					break;
			}
		}
		DofusCell C = map.getCell(nextCell);
		if (C == null)
			return -1;
		int dis = Pathfinding.getDistanceBetween(map, finalCell, nextCell);
		if (dis < dist && C.isWalkable(true) && C.getFirstFighter() == null) {
			dist = dis;
			cellId = nextCell;
		}
		return cellId == startCell ? -1 : cellId;
	}
	public static ArrayList<DofusCell> getShortestPathBetween(final DofusMap map, final int start, final int dest, final int distMax)
	{
		final ArrayList<DofusCell> curPath = new ArrayList<DofusCell>();
		final ArrayList<DofusCell> closeCells = new ArrayList<DofusCell>();
		final int limit = 1000;
		//int oldCaseID = start;
		DofusCell curCase = map.getCell(start);
		int stepNum = 0;
		final boolean stop = false;
		
		//Start
		
		while(!stop && stepNum++ <= limit)
		{
			//*
			final int nearestCell = getNearestCellAround(map,curCase.getId(),dest,closeCells);
			//Si pas trouvé : En cas de sans issus
			if(nearestCell == -1)
			{
				//On ajoute la case sans issus a la liste des interdites
				closeCells.add(curCase);
				//Si il ya une case avant
				if(curPath.size() > 0)
				{
					//on supprime le dernier element = curCase normalement
					curPath.remove(curPath.size()-1);
					//on prend la derniere case du chemin
					if(curPath.size()>0)curCase = curPath.get(curPath.size()-1);
					else curCase = map.getCell(start);
				}
				else//Si retour a zero
				{
					curCase = map.getCell(start);
				}
			}else if(distMax == 0 && nearestCell == dest)
			{
				curPath.add(map.getCell(dest));
				return curPath;
			}else if(distMax > Pathfinding.getDistanceBetween(map, nearestCell, dest))
			{
				curPath.add(map.getCell(dest));
				return curPath;
			}else//on continue
			{
				curCase = map.getCell(nearestCell);
				curPath.add(curCase);
			}
			// fin new IA */
			
			/* Ex code
			int nextID = getNearestCellAround(map,dest,curCase.getID(),closeCells);
			if(nextID == curCase.getID() || nextID == oldCaseID)//Si c'est un sans issus
			{
				if(curPath.size() == 0)//Si il n'y a pas de cells avant
				{
					stop = true;//on arrete la boucle => return null;
				}
				else
				{
					closeCells.add(curCase);//on ajoute la case courante a la liste des cases interdites
					curPath.remove(curPath.size()-1);//On remonte d'une case
					curCase = curPath.get(curPath.size()-1);
					if(curPath.size() >1)
						oldCaseID = curPath.get(curPath.size()-1).getID();
					else
						oldCaseID = start;
				}
			}else if(nextID == dest)//Si Path trouvé
			{
				curCase = map.getCase(nextID);
				curPath.add(curCase);
				return curPath;
			}else //Sinon, on continue
			{
				oldCaseID = curCase.getID();
				curCase = map.getCase(nextID);
				curPath.add(curCase);
			}
			//*/
			
		}
		return null;
	}

	public static ArrayList<Integer> listCellFighter(Fight fight, Fighter fighter) {
		ArrayList<Integer> cells = new ArrayList<Integer>();
		int celdaInicio = fighter.getFightCell().getId();
		int[] tempPath;
		int i = 0;
		if (fighter.getCurPM(fight) > 0)
			tempPath = new int[fighter.getCurPM(fight)];
		else
			return null;
		if (tempPath.length == 0)
			return null;
		while (tempPath[0] != 5) {
			tempPath[i]++;
			if (tempPath[i] == 5 && i != 0) {
				tempPath[i] = 0;
				i--;
			} else {
				int tempCellId = getPathCell(celdaInicio, tempPath, fight.getMap());
				DofusCell tempCell = fight.getMap().getCell(tempCellId);
				if (tempCell == null)
					continue;
				if (tempCell.isWalkable(true) && tempCell.getFirstFighter() == null) {
					if (!cells.contains(tempCellId)) {
						cells.add(tempCellId);
						if (i < tempPath.length - 1)
							i++;
					}
				}
			}
		}
		return listCellsByDistance(fight, fighter, cells);
	}
	
	public static int getPathCell(int start, int[] path, DofusMap map) {
		int cell = start, i = 0;
		int width = map.getW();
		while (i < path.length) {
			if (path[i] == 1)
				cell -= width;
			if (path[i] == 2)
				cell -= (width - 1);
			if (path[i] == 3)
				cell += width;
			if (path[i] == 4)
				cell += (width - 1);
			i++;
		}
		return cell;
	}
	
	public static ArrayList<Integer> listCellsByDistance(Fight fight, Fighter fighter, ArrayList<Integer> cells) {
		ArrayList<Integer> fightCells = new ArrayList<Integer>();
		ArrayList<Integer> newCells = cells;
		int dist = 100;
		int tempCell = 0;
		int tempIndex = 0;
		while (newCells.size() > 0) {
			dist = 200;
			for (int cell : newCells) {
				int dis = Pathfinding.getDistanceBetween(fight.getMap(), fighter.getFightCell().getId(), cell);
				if (dist > dis) {
					dist = dis;
					tempCell = cell;
					tempIndex = newCells.indexOf(cell);
				}
			}
			fightCells.add(tempCell);
			newCells.remove(tempIndex);
		}
		return fightCells;
	}
}

package common;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import objects.action.GameAction;
import objects.fight.CooldownSpell;
import objects.fight.Fight;
import objects.fight.Fighter;
import objects.map.DofusCell;
import objects.map.DofusMap;
import objects.spell.Spell;
import objects.spell.SpellEffect;
import objects.spell.SpellStat;

import common.utils.CryptManager;
import common.utils.Formulas;
import common.utils.Pathfinding;

public class IA {
	
	public static void launchIA(final Fighter fighter, final Fight fight) {
		if (fighter.isPerco()) {
			applyType_TaxCollector(fighter, fight);
			return;
		}
		switch (fighter.getMob().getTemplate().getIAType()) {
		case 0:
			break;
		case 1:
			applyType_1(fighter, fight);
			break;
		case 2:
			applyType_2(fighter, fight);
			break;
		case 3:
			applyType_3(fighter, fight);
			break;
		case 4:
			applyType_4(fighter, fight);
			break;
		case 5:
			applyType_5(fighter, fight);
			break;
		case 6:
			applyType_6(fighter, fight);
			break;
		case 7:
			applyType_7(fighter, fight);
			break;
		case 8:
			applyType_8(fighter, fight);
			break;
		case 9:
			applyType_9(fighter, fight);
			break;
		case 10:
			applyType_10(fighter, fight);
			break;
		case 11:
			applyType_11(fighter, fight);
			break;
		case 12:
			applyType_12(fighter, fight);
			break;
		case 13:
			applyType_13(fighter, fight);
			break;
		case 14:
			applyType_14(fighter, fight);
			break;
		case 15:
			applyType_15(fighter, fight);
			break;
		case 16:
			applyType_16(fighter, fight);
			break;
		case 17:
			applyType_17(fighter, fight);
			break;
		case 18:
			applyType_18(fighter, fight);
			break;
		case 19:
			applyType_19(fighter, fight);
			break;
		}
	}

	private static void applyType_1(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			int porcPDV = (fighter.getLife() * 100) / fighter.getMaxLife();
			Fighter enemy = getNearestEnemy(fight, fighter);
			Fighter friend = getNearestFriend(fight, fighter);
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			if (porcPDV > 15) {
				int attack = attackIfPossible1(fight, fighter);
				while (attack == 0 && !stop) {
					if (attack == 5)
						stop = true;
					attack = attackIfPossible1(fight, fighter);
				}
				if (!moveToAttackIfPossible(fight, fighter)) {
					if (!buffIfPossible1(fight, fighter, fighter)) {
						if (!healIfPossible(fight, fighter, true)) {
							if (!buffIfPossible1(fight, fighter, friend)) {
								enemy = getNearestEnemy(fight, fighter);
								if (enemy == null) {
									moveFarIfPossible(fight, fighter);
									return;
								}
								if (!moveNearIfPossible(fight, fighter, enemy)) {
									if (!invocIfPossible1(fight, fighter)) {
										stop = true;
									}
								}
							}
						}
					}
				} else {
					attack = attackIfPossible1(fight, fighter);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible1(fight, fighter);
					}
				}
			} else {
				if (!healIfPossible(fight, fighter, true)) {
					int attack = attackIfPossible1(fight, fighter);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible1(fight, fighter);
					}
					if (!buffIfPossible1(fight, fighter, fighter)) {
						if (!buffIfPossible1(fight, fighter, friend)) {
							if (!invocIfPossible1(fight, fighter)) {
								enemy = getNearestEnemy(fight, fighter);
								if (enemy == null) {
									moveFarIfPossible(fight, fighter);
									return;
								}
								if (!moveNearIfPossible(fight, fighter, enemy)) {
									stop = true;
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static void applyType_2(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			Fighter enemy = getNearestEnemy(fight, fighter);
			if (enemy == null)
				return;
			int attack = attackIfPossible2(fight, fighter);
			while (attack == 0 && !stop) {
				if (attack == 5)
					stop = true;
				attack = attackIfPossible2(fight, fighter);
			}
			stop = true;
		}
	}
	
	private static void applyType_3(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			Fighter enemy = getNearestEnemy(fight, fighter);
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			int attack = attackIfPossible2(fight, fighter);
			while (attack == 0 && !stop) {
				if (attack == 5)
					stop = true;
				attack = attackIfPossible2(fight, fighter);
			}
			enemy = getNearestEnemy(fight, fighter);
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			if (!moveNearIfPossible(fight, fighter, enemy))
				stop = true;
		}
	}
	
	private static void applyType_4(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			Fighter enemy = getNearestEnemy(fight, fighter);
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			int attack = attackIfPossible2(fight, fighter);
			if (attack == 0 && !stop) {
				while (attack == 0 && !stop) {
					if (attack == 5)
						stop = true;
					attack = attackIfPossible2(fight, fighter);
				}
			} else if (moveToAttackIfPossible(fight, fighter)) {
				attack = attackIfPossible2(fight, fighter);
				if (attack == 0 && !stop) {
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible2(fight, fighter);
					}
				}
			} else {
				enemy = getNearestEnemy(fight, fighter);
				if (enemy == null) {
					moveFarIfPossible(fight, fighter);
					return;
				}
				if (moveNearIfPossible(fight, fighter, enemy)) {
					attack = attackIfPossible2(fight, fighter);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible2(fight, fighter);
					}
				}
			}
			moveFarIfPossible(fight, fighter);
			stop = true;
		}
	}
	
	private static void applyType_5(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			Fighter enemy = getNearestEnemy(fight, fighter);
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			if (!moveNearIfPossible(fight, fighter, enemy)) {
				stop = true;
			}
		}
	}
	
	private static void applyType_6(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			Fighter friend = getNearestFriend(fight, fighter);
			if (!moveNearIfPossible(fight, fighter, friend)) {
				while (buffIfPossible2(fight, fighter, friend)) {}
				while (buffIfPossible2(fight, fighter, fighter)) {}
				stop = true;
			}
		}
	}
	
	private static void applyType_7(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			Fighter enemy = getNearestEnemy(fight, fighter);
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			int attack = attackIfPossible2(fight, fighter);
			while (attack == 0 && !stop) {
				if (attack == 5)
					stop = true;
				attack = attackIfPossible2(fight, fighter);
			}
			enemy = getNearestEnemy(fight, fighter);
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			if (!moveNearIfPossible(fight, fighter, enemy)) {
				stop = true;
			}
		}
	}
	
	private static void applyType_8(Fighter fighter, Fight fight) {
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			Fighter friend = getNearestFriend(fight, fighter);
			if (friend == null)
				return;
			
			if(!buffIfPossible1(fight, fighter, friend))
			{
				if(!moveNearIfPossible(fight, fighter, friend))
				{
					stop = true;
				}
			}
		}
	}
	
	private static void applyType_9(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			while (buffIfPossible2(fight, fighter, fighter)) {}
			stop = true;
		}
	}
	
	private static void applyType_10(Fighter fighter, Fight fight) {// cascara explosiva
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			int attack = attackIfPossible2(fight, fighter);
			while (attack == 0 && !stop) {
				if (attack == 5)
					stop = true;
				attack = attackIfPossible2(fight, fighter);
			}
			while (buffIfPossible2(fight, fighter, fighter)) {}
			stop = true;
		}
	}
	
	private static void applyType_11(Fighter fighter, Fight fight) { // chafer y chaferloko
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			Fighter enemy = getNearestEnemy(fight, fighter); // Enemigos
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			int attack = attackIfPossible3(fight, fighter);
			while (attack == 0 && !stop) {
				if (attack == 5)
					stop = true;
				attack = attackIfPossible3(fight, fighter);
			}
			while (buffIfPossible1(fight, fighter, fighter)) {}
			enemy = getNearestEnemy(fight, fighter);
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			if (!moveNearIfPossible(fight, fighter, enemy)) {
				stop = true;
			}
		}
	}
	
	private static void applyType_12(Fighter fighter, Fight fight) {// kralamar
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			int attack = 0;
			if (!invocIfPossible2(fight, fighter)) {
				if (!buffKralamour(fight, fighter, fighter)) {
					attack = attackIfPossible1(fight, fighter);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible1(fight, fighter);
					}
					stop = true;
				} else {
					attack = attackIfPossible1(fight, fighter);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible1(fight, fighter);
					}
					stop = true;
				}
			}
		}
	}
	
	private static void applyType_13(Fighter fighter, Fight fight) {// vasija
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			if (!buffIfPossible2(fight, fighter, fighter)) {// auto boost
				int attack = attackIfPossible2(fight, fighter);
				while (attack == 0 && !stop) {
					if (attack == 5)
						stop = true;
					attack = attackIfPossible2(fight, fighter);
				}
				stop = true;
			}
		}
	}
	
	private static void applyType_14(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			int porcPDV = (fighter.getLife() * 100) / fighter.getMaxLife();
			Fighter enemy = getNearestEnemy(fight, fighter); // Enemigos
			Fighter friend = getNearestFriend(fight, fighter); // Amigos
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			if (porcPDV > 15) {
				int attack = attackIfPossible1(fight, fighter);
				while (attack == 0 && !stop) {
					if (attack == 5)
						stop = true;
					attack = attackIfPossible1(fight, fighter);
				}
				if (!moveToAttackIfPossible(fight, fighter)) {
					if (!buffIfPossible1(fight, fighter, fighter)) {
						if (!healIfPossible(fight, fighter, false)) {// cura aliada
							if (!buffIfPossible1(fight, fighter, friend)) {
								enemy = getNearestEnemy(fight, fighter);
								if (enemy == null) {
									moveFarIfPossible(fight, fighter);
									return;
								}
								if (!moveNearIfPossible(fight, fighter, enemy)) {
									stop = true;
								}
							}
						}
					}
				} else {
					attack = attackIfPossible1(fight, fighter);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible1(fight, fighter);
					}
				}
			} else {
				if (!healIfPossible(fight, fighter, true)) {// auto-cura
					int attack = attackIfPossible1(fight, fighter);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible1(fight, fighter);
					}
					if (!buffIfPossible1(fight, fighter, fighter)) {
						if (!buffIfPossible1(fight, fighter, friend)) {// buff aliados
							enemy = getNearestEnemy(fight, fighter);
							if (enemy == null) {
								moveFarIfPossible(fight, fighter);
								return;
							}
							if (!moveNearIfPossible(fight, fighter, enemy)) {
								stop = true;
							}
						}
					}
				}
			}
		}
	}
	
	private static void applyType_15(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			int porcPDV = (fighter.getLife() * 100) / fighter.getMaxLife();
			Fighter enemy = getNearestEnemy(fight, fighter);
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			if (porcPDV > 15) {
				int attack = attackIfPossible1(fight, fighter);
				while (attack == 0 && !stop) {
					if (attack == 5)
						stop = true;
					attack = attackIfPossible1(fight, fighter);
				}
				if (!moveToAttackIfPossible(fight, fighter)) {
					if (!healIfPossible(fight, fighter, false)) {
						enemy = getNearestEnemy(fight, fighter);
						if (enemy == null) {
							moveFarIfPossible(fight, fighter);
							return;
						}
						if (!moveNearIfPossible(fight, fighter, enemy)) {
							if (!invocIfPossible1(fight, fighter)) {
								stop = true;
							}
						}
					}
				} else {
					attack = attackIfPossible1(fight, fighter);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible1(fight, fighter);
					}
				}
			} else {
				if (!healIfPossible(fight, fighter, true)) {
					int attack = attackIfPossible1(fight, fighter);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossible1(fight, fighter);
					}
					if (!invocIfPossible1(fight, fighter)) {
						enemy = getNearestEnemy(fight, fighter);
						if (enemy == null) {
							moveFarIfPossible(fight, fighter);
							return;
						}
						if (!moveNearIfPossible(fight, fighter, enemy)) {
							stop = true;
						}
					}
				}
			}
		}
	}
	
	private static void applyType_16(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			Fighter enemy = getNearestEnemy(fight, fighter);
			Fighter friend = getNearestFriend(fight, fighter);
			if (enemy == null) {
				moveFarIfPossible(fight, fighter);
				return;
			}
			int attack = attackIfPossible1(fight, fighter);
			while (attack == 0 && !stop) {
				if (attack == 5)
					stop = true;
				attack = attackIfPossible1(fight, fighter);
			}
			if (!moveToAttackIfPossible(fight, fighter)) {
				if (!buffIfPossible1(fight, fighter, fighter)) {
					if (!buffIfPossible1(fight, fighter, friend)) {
						enemy = getNearestEnemy(fight, fighter);
						if (enemy == null) {
							moveFarIfPossible(fight, fighter);
							return;
						}
						if (!moveNearIfPossible(fight, fighter, enemy)) {
							if (!invocIfPossible1(fight, fighter)) {
								stop = true;
							}
						}
					}
				}
			} else {
				attack = attackIfPossible1(fight, fighter);
				while (attack == 0 && !stop) {
					if (attack == 5)
						stop = true;
					attack = attackIfPossible1(fight, fighter);
				}
			}
		}
	}
	
	private static void applyType_17(Fighter fighter, Fight fight) {
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			int attack = attackIfPossible1(fight, fighter);
			if(!moveToAttackIfPossible(fight, fighter))
			{
				if(attack != 0)
				{
					if(attack == 5)
					{ 
						stop = true;
					}
					if(!moveFarIfPossible(fight, fighter))
					{
						stop = true;
					}
				}
			}
		}
	}
	
	private static void applyType_18(Fighter fighter, Fight fight) {
		boolean stop = false;
		Fighter friend = getNearestFriend(fight, fighter);
		if(friend == null) return;
		while (!stop && fighter.canPlay()) {
			if(!invocIfPossible1(fight, fighter)) {
				if(!buffIfPossible1(fight, fighter, friend)) {
					if(!moveFarIfPossible(fight, fighter)) {
						stop = true;
					}
				}
			}
		}
	}
	
	private static void applyType_19(Fighter F, Fight fight)
	{
		boolean stop = false;
		while(!stop && F.canPlay())
		{
			Fighter T = getNearestFriend(fight,F);
			if(!moveNearIfPossible(fight,F,T))//Avancer vers allié
			{
				if(!healIfPossible(fight,F,false))//soin allié
				{
					if(!buffIfPossible1(fight,F,T))//buff allié
					{
						if(!healIfPossible(fight,F,true))//auto-soin
						{
							if(!invocIfPossible1(fight,F))
							{
								if(!buffIfPossible1(fight,F,F))//auto-buff
								{
									stop = true;
								}
							}
						}
					}
				}
			}
		}		
	}
	
	private static void applyType_TaxCollector(Fighter fighter, Fight fight) {
		int nbLoop = 0;
		boolean stop = false;
		while (!stop && fighter.canPlay()) {
			if (++nbLoop >= 8)
				stop = true;
			if (nbLoop > 15)
				return;
			int porcPDV = (fighter.getLife() * 100) / fighter.getMaxLife();
			Fighter friend = getNearestFriend(fight, fighter);
			Fighter enemy = getNearestEnemy(fight, fighter);
			if (porcPDV > 15) {
				int attack = attackIfPossibleTaxCollector(fight, fighter);
				while (attack == 0 && !stop) {
					if (attack == 5)
						stop = true;
					attack = attackIfPossibleTaxCollector(fight, fighter);
				}
				if (!healIfPossibleTaxCollector(fight, fighter, false)) {
					if (!buffIfPossibleTaxCollector(fight, fighter, friend)) {
						enemy = getNearestEnemy(fight, fighter);
						if (enemy == null) {
							moveFarIfPossible(fight, fighter);
							return;
						}
						if (!moveNearIfPossible(fight, fighter, enemy)) {
							stop = true;
						}
					}
				}
			} else {
				if (!healIfPossibleTaxCollector(fight, fighter, true)) {
					int attack = attackIfPossibleTaxCollector(fight, fighter);
					while (attack == 0 && !stop) {
						if (attack == 5)
							stop = true;
						attack = attackIfPossibleTaxCollector(fight, fighter);
					}
					if (!moveFarIfPossible(fight, fighter)) {
						stop = true;
					}
				}
			}
		}
	}
	
	private static boolean moveFarIfPossible(Fight fight, Fighter fighter) {
		if (fighter.getCurPM(fight) <= 0)
			return false;
		int cellIdLauncher = fighter.getFightCell().getId();
		DofusMap map = fight.getMap();
		int dist[] = { 1000, 11000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000 }, cell[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0 };
		for (int i = 0; i < 10; i++) {
			for (Fighter curFighter : fight.getFighters(3)) {
				if (curFighter.isDead())
					continue;
				if (curFighter == fighter || curFighter.getTeam() == fighter.getTeam())
					continue;
				int enemyCell = curFighter.getFightCell().getId();
				if (enemyCell == cell[0] || enemyCell == cell[1] || enemyCell == cell[2]
						|| enemyCell == cell[3] || enemyCell == cell[4] || enemyCell == cell[5]
						|| enemyCell == cell[6] || enemyCell == cell[7] || enemyCell == cell[8]
						|| enemyCell == cell[9])
					continue;
				int d = 0;
				d = Pathfinding.getDistanceBetween(map, cellIdLauncher, enemyCell);
				if (d == 0)
					continue;
				if (d < dist[i]) {
					dist[i] = d;
					cell[i] = enemyCell;
				}
				if (dist[i] == 1000) {
					dist[i] = 0;
					cell[i] = cellIdLauncher;
				}
			}
		}
		if (dist[0] == 0)
			return false;
		int dist2[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		int width = map.getW();
		int PM = fighter.getCurPM(fight);
		int startCell = cellIdLauncher;
		int cellFinal = cellIdLauncher;
		int finalCell = map.getLastCellId();
		Random rand = new Random();
		int value = rand.nextInt(3);
		int[] moved;
		if (value == 0)
			moved = new int[] { 0, 1, 2, 3 };
		else if (value == 1)
			moved = new int[] { 1, 2, 3, 0 };
		else if (value == 1)
			moved = new int[] { 2, 3, 0, 1 };
		else
			moved = new int[] { 3, 0, 1, 2 };
		for (int i = 0; i <= PM; i++) {
			if (cellFinal > 0)
				startCell = cellFinal;
			int cellTemp = startCell;
			int infl = 0, inflF = 0;
			for (Integer x : moved) {
				switch (x) {
					case 0:
						cellTemp = cellTemp + width;
						break;
					case 1:
						cellTemp = startCell + (width - 1);
						break;
					case 2:
						cellTemp = startCell - width;
						break;
					case 3:
						cellTemp = startCell - (width - 1);
						break;
				}
				infl = 0;
				for (int a = 0; a < 10 && dist[a] != 0; a++) {
					dist2[a] = Pathfinding.getDistanceBetween(map, cellTemp, cell[a]);
					if (dist2[a] > dist[a])
						infl++;
				}
				if (infl > inflF && cellTemp > 0 && cellTemp < finalCell
						&& !map.isCellOutOfLateral(cellFinal, cellTemp)
						&& map.getCell(cellTemp).isWalkable(false)) {
					inflF = infl;
					cellFinal = cellTemp;
				}
			}
		}
		if (cellFinal < 0 || cellFinal > finalCell || cellFinal == cellIdLauncher
				|| !map.getCell(cellFinal).isWalkable(false))
			return false;
		ArrayList<DofusCell> path = Pathfinding.getShortestPathBetween(map, cellIdLauncher, cellFinal, 0);
		if (path == null)
			return false;
		ArrayList<DofusCell> finalPath = new ArrayList<DofusCell>();
		for (int a = 0; a < fighter.getCurPM(fight); a++) {
			if (path.size() == a)
				break;
			finalPath.add(path.get(a));
		}
		String pathstr = "";
		try {
			int cellIdTemp = cellIdLauncher;
			int tempDir = 0;
			for (DofusCell c : finalPath) {
				char d = Pathfinding.getDirBetweenTwoCase(cellIdTemp, c.getId(), map, true);
				if (d == 0)
					return false;
				if (tempDir != d) {
					if (finalPath.indexOf(c) != 0)
						pathstr += CryptManager.cellIDToCode(cellIdTemp);
					pathstr += d;
				}
				cellIdTemp = c.getId();
			}
			if (cellIdTemp != cellIdLauncher)
				pathstr += CryptManager.cellIDToCode(cellIdTemp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		GameAction GA = new GameAction(0, 1, "");
		GA._args = pathstr;
		boolean result = fight.fighterDeplace(fighter, GA);
		return result;
	}
	
	private static boolean moveNearIfPossible(Fight fight, Fighter fighter, Fighter target) {
		DofusMap map = fight.getMap();
		if (fighter.getCurPM(fight) <= 0)
			return false;
		if (target == null)
			target = getNearestEnemy(fight, fighter);
		if (target == null)
			return false;
		if (Pathfinding.isNextTo(fighter.getFightCell().getId(), target.getFightCell().getId(), map))
			return false;
		int cellID = Pathfinding.getNearestCellAround(map, target.getFightCell().getId(), fighter.getFightCell().getId(), null);
		if (cellID == -1) {
			ArrayList<Fighter> enemies = listLowHPEnemies(fight, fighter);
			for (Fighter enemy : enemies) {
				int cellID2 = Pathfinding.getNearestCellAround(map, enemy.getFightCell().getId(), fighter.getFightCell().getId(), null);
				if (cellID2 != -1) {
					cellID = cellID2;
					break;
				}
			}
		}
		ArrayList<DofusCell> path = Pathfinding.getShortestPathBetween(map, fighter.getFightCell().getId(), cellID, 0);
		if (path == null || path.isEmpty())
			return false;
		ArrayList<DofusCell> finalPath = new ArrayList<DofusCell>();
		for (int a = 0; a < fighter.getCurPM(fight); a++) {
			if (path.size() == a)
				break;
			finalPath.add(path.get(a));
		}
		String pathstr = "";
		try {
			int cellIdTemp = fighter.getFightCell().getId();
			int tempDir = 0;
			for (DofusCell c : finalPath) {
				char d = Pathfinding.getDirBetweenTwoCase(cellIdTemp, c.getId(), map, true);
				if (d == 0)
					return false;
				if (tempDir != d) {
					if (finalPath.indexOf(c) != 0)
						pathstr += CryptManager.cellIDToCode(cellIdTemp);
					pathstr += d;
				}
				cellIdTemp = c.getId();
			}
			if (cellIdTemp != fighter.getFightCell().getId())
				pathstr += CryptManager.cellIDToCode(cellIdTemp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		GameAction GA = new GameAction(0, 1, "");
		GA._args = pathstr;
		boolean result = fight.fighterDeplace(fighter, GA);
		return result;
	}
	
	private static boolean invocIfPossible1(Fight fight, Fighter invocator) {
		if (invocator._nbInvoc >= invocator.getTotalStats().getEffect(182))
			return false;
		Fighter enemyNearest = getNearestEnemy(fight, invocator);
		if (enemyNearest == null)
			return false;
		int cellNearest = Pathfinding.getNearestCellAround(fight.getMap(), invocator.getFightCell().getId(),
				enemyNearest.getFightCell().getId(), null);
		if (cellNearest == -1)
			return false;
		SpellStat spell = getInvocationSpell(fight, invocator, cellNearest);
		if (spell == null)
			return false;
		int invoc = fight.tryCastSpell(invocator, spell, cellNearest);
		if (invoc != 0)
			return false;
		fight.mobsAppendEmote(false);
		return true;
	}
	
	private static boolean invocIfPossible2(Fight fight, Fighter invocator) {
		if (invocator._nbInvoc >= invocator.getTotalStats().getEffect(182))
			return false;
		Fighter enemyNearest = getNearestEnemy(fight, invocator);
		if (enemyNearest == null)
			return false;
		int invoc = getInvocationSpell2(fight, invocator, enemyNearest);
		if (invoc != 0)
			return false;
		fight.mobsAppendEmote(false);
		return true;
	}
	
	private static SpellStat getInvocationSpell(Fight fight, Fighter invocator, int cellCercana) {
		if (invocator.getMob() == null)
			return null;
		for (Entry<Integer, SpellStat> SS : invocator.getMob().getSpells().entrySet()) {
			if (!fight.CanCastSpell(invocator, SS.getValue(), fight.getMap().getCell(cellCercana), -1))
				continue;
			for (SpellEffect EH : SS.getValue().getEffects()) {
				if (EH.getEffectID() == 181 || EH.getEffectID() == 185)
					return SS.getValue();
			}
		}
		return null;
	}
	
	private static int getInvocationSpell2(Fight fight, Fighter invocator, Fighter enemyNearest) {
		if (invocator.getMob() == null)
			return 5;
		ArrayList<SpellStat> spells = new ArrayList<SpellStat>();
		SpellStat SS = null;
		int cellNearest = -1;
		try {
			for (Entry<Integer, SpellStat> entry : invocator.getMob().getSpells().entrySet()) {
				SpellStat spell = entry.getValue();
				boolean continu = false;
				for (SpellEffect EH : spell.getEffects()) {
					if (continu)
						continue;
					if (EH.getEffectID() == 181 || EH.getEffectID() == 185) {
						cellNearest = Pathfinding.getNearestCellAround2(fight.getMap(), invocator.getFightCell().getId(), enemyNearest.getFightCell().getId(), spell.getMinPO(), spell.getMaxPO());
						if (cellNearest == -1)
							continue;
						if (!fight.CanCastSpell(invocator, spell, fight.getMap().getCell(cellNearest), -1))
							continue;
						spells.add(spell);
						continu = true;
					}
				}
			}
		} catch (NullPointerException e) {
			return 5;
		}
		if (spells.size() <= 0)
			return 5;
		if (spells.size() == 1)
			SS = spells.get(0);
		else
			SS = spells.get(Formulas.getRandomValue(0, spells.size() - 1));
		int invoca = fight.tryCastSpell(invocator, SS, cellNearest);
		return invoca;
	}
	
	private static boolean healIfPossible(Fight fight, Fighter fighter, boolean autoHeal) {
		if (autoHeal && (fighter.getLife() * 100) / fighter.getMaxLife() > 95)
			return false;
		Fighter target = null;
		SpellStat SS = null;
		if (autoHeal) {
			target = fighter;
			SS = getBestHealSpell(fight, fighter, target);
		} else {
			Fighter tempTarget = null;
			int porcPDVmin = 100;
			SpellStat tempSH = null;
			for (Fighter curFighter : fight.getFighters(3)) {
				if (curFighter.isDead() || curFighter == fighter)
					continue;
				if (curFighter.getTeam() == fighter.getTeam()) {
					int porcPDV = 0;
					int PDVMAX = curFighter.getMaxLife();
					if (PDVMAX == 0)
						porcPDV = 0;
					else
						porcPDV = (curFighter.getLife() * 100) / PDVMAX;
					if (porcPDV < porcPDVmin && porcPDV < 95) {
						int infl = 0;
						for (Entry<Integer, SpellStat> ss : fighter.getMob().getSpells().entrySet()) {
							int infHeal = calculHealInfluence(ss.getValue());
							if (infl < infHeal && infHeal != 0
									&& fight.CanCastSpell(fighter, ss.getValue(), curFighter.getFightCell(), -1)) {
								infl = infHeal;
								tempSH = ss.getValue();
							}
						}
						if (tempSH != SS && tempSH != null) {
							tempTarget = curFighter;
							SS = tempSH;
							porcPDVmin = porcPDV;
						}
					}
				}
			}
			target = tempTarget;
		}
		if (target == null)
			return false;
		if (SS == null)
			return false;
		int cura = fight.tryCastSpell(fighter, SS, target.getFightCell().getId());
		if (cura != 0)
			return false;
		return true;
	}
	
	private static boolean healIfPossibleTaxCollector(Fight fight, Fighter taxCollector, boolean autoHeal) {
		if (autoHeal && (taxCollector.getLife() * 100) / taxCollector.getMaxLife() > 95)
			return false;
		Fighter target = null;
		SpellStat SS = null;
		if (autoHeal) {
			target = taxCollector;
			SS = getBestHealSpellTaxCollector(fight, taxCollector, target);
		} else {
			Fighter tempTarget = null;
			int porcPDVmin = 100;
			SpellStat tempSH = null;
			if (fight.getFighters(taxCollector.getTeam()).size() <= 1)
				return false;
			for (Fighter curFighter : fight.getFighters(3)) {
				if (curFighter.isDead() || curFighter == taxCollector)
					continue;
				if (curFighter.getTeam() == taxCollector.getTeam()) {
					int porcPDV = (curFighter.getLife() * 100) / curFighter.getMaxLife();
					if (porcPDV < porcPDVmin && porcPDV < 95) {
						int infl = 0;
						for (Entry<Integer, Integer> entry : taxCollector.getTaxCollector().getGuild().getSorts().entrySet()) {
							SpellStat ss = World.getSpell(entry.getKey()).getStatsByLevel(entry.getValue());
							if (ss == null)
								continue;
							int infHeal = calculHealInfluence(ss);
							if (infl < infHeal && infHeal != 0
									&& fight.CanCastSpell(taxCollector, ss, curFighter.getFightCell(), -1)) {
								infl = infHeal;
								tempSH = ss;
							}
						}
						if (tempSH != SS && tempSH != null) {
							tempTarget = curFighter;
							SS = tempSH;
							porcPDVmin = porcPDV;
						}
					}
				}
			}
			target = tempTarget;
		}
		if (target == null)
			return false;
		if (SS == null)
			return false;
		int cura = fight.tryCastSpell(taxCollector, SS, target.getFightCell().getId());
		if (cura != 0)
			return false;
		return true;
	}
	
	private static boolean buffIfPossible1(Fight fight, Fighter fighter, Fighter target) {
		if (target == null)
			return false;
		SpellStat SS = getBestBuff1(fight, fighter, target);
		if (SS == null)
			return false;
		int buff = fight.tryCastSpell(fighter, SS, target.getFightCell().getId());
		if (buff != 0)
			return false;
		return true;
	}
	
	private static boolean buffIfPossible2(Fight fight, Fighter fighter, Fighter target) {
		if (target == null)
			return false;
		SpellStat SS = getBestBuff2(fight, fighter, target);
		if (SS == null)return false;
		int buff = fight.tryCastSpell(fighter, SS, target.getFightCell().getId());
		if (buff != 0)return false;
		return true;
	}
	
	private static boolean buffKralamour(Fight fight, Fighter fighter, Fighter target) {
		if (target == null)
			return false;
		Spell spell = World.getSpell(1106);
		SpellStat SS = spell.getStatsByLevel(1);
		if (SS == null)
			return false;
		int buff = 5;
		buff = fight.tryCastSpell(fighter, SS, target.getFightCell().getId());
		if (buff != 0)
			return false;
		return true;
	}
	
	private static boolean buffIfPossibleTaxCollector(Fight fight, Fighter taxCollector, Fighter target) {
		if (target == null)
			return false;
		try {
			SpellStat SS = getBestBuffTaxCollector(fight, taxCollector, target);
			if (SS == null)
				return false;
			int buff = fight.tryCastSpell(taxCollector, SS, target.getFightCell().getId());
			if (buff != 0)
				return false;
			return true;
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	private static SpellStat getBestBuff1(Fight fight, Fighter fighter, Fighter target) {
		int infl = 0;
		SpellStat spellStat = null;
		for (Entry<Integer, SpellStat> SS : fighter.getMob().getSpells().entrySet()) {
			int newInfl = calculInfluence(SS.getValue(), fighter, target);
			if (infl < newInfl && newInfl > 0 && fight.CanCastSpell(fighter, SS.getValue(), target.getFightCell(), -1)) {
				infl = newInfl;
				spellStat = SS.getValue();
			}
		}
		return spellStat;
	}
	
	private static SpellStat getBestBuff2(Fight fight, Fighter fighter, Fighter target) {
		ArrayList<SpellStat> spells = new ArrayList<SpellStat>();
		SpellStat spellStat = null;
		if (target == null)
			return null;
		DofusCell cellObj = target.getFightCell();
		for (Entry<Integer, SpellStat> SS : fighter.getMob().getSpells().entrySet()) {
			if (fight.CanCastSpell(fighter, SS.getValue(), cellObj, -1))
				spells.add(SS.getValue());
		}
		if (spells.size() <= 0)
			return null;
		if (spells.size() == 1)
			return spells.get(0);
		spellStat = spells.get(Formulas.getRandomValue(0, spells.size() - 1));
		return spellStat;
	}
	
	private static SpellStat getBestBuffTaxCollector(Fight fight, Fighter fighter, Fighter target) {
		int infl = 0;
		SpellStat spellStat = null;
		if (target == null)
			return null;
		try {
			for (Entry<Integer, Integer> entry : fighter.getTaxCollector().getGuild().getSorts().entrySet()) {
				SpellStat ss = World.getSpell(entry.getKey()).getStatsByLevel(entry.getValue());
				if (ss == null)
					continue;
				int infDaños = calculInfluence(ss, fighter, target);
				if (infl < infDaños && infDaños > 0
						&& fight.CanCastSpell(fighter, ss, target.getFightCell(), -1)) {
					infl = infDaños;
					spellStat = ss;
				}
			}
		} catch (NullPointerException e) {
			return null;
		}
		return spellStat;
	}
	
	private static SpellStat getBestHealSpell(Fight fight, Fighter fighter, Fighter target) {
		int infl = 0;
		SpellStat spellStat = null;
		if (target == null)
			return null;
		try {
			for (Entry<Integer, SpellStat> SS : fighter.getMob().getSpells().entrySet()) {
				int infHeal = calculHealInfluence(SS.getValue());
				if (infl < infHeal && infHeal != 0
						&& fight.CanCastSpell(fighter, SS.getValue(), target.getFightCell(), -1)) {
					infl = infHeal;
					spellStat = SS.getValue();
				}
			}
		} catch (NullPointerException e) {
			return null;
		}
		return spellStat;
	}
	
	private static SpellStat getBestHealSpellTaxCollector(Fight fight, Fighter fighter, Fighter target) {
		int infl = 0;
		SpellStat spellStat = null;
		if (target == null)
			return null;
		try {
			for (Entry<Integer, Integer> entry : fighter.getTaxCollector().getGuild().getSorts().entrySet()) {
				SpellStat ss = World.getSpell(entry.getKey()).getStatsByLevel(entry.getValue());
				if (ss == null)
					continue;
				int infHeal = calculHealInfluence(ss);
				if (infl < infHeal && infHeal != 0
						&& fight.CanCastSpell(fighter, ss, target.getFightCell(), -1)) {
					infl = infHeal;
					spellStat = ss;
				}
			}
		} catch (NullPointerException e) {
			return null;
		}
		return spellStat;
	}
	
	private static Fighter getNearestFriend(Fight fight, Fighter fighter) {
		int dist = 1000;
		Fighter tempTarget = null;
		for (Fighter target : fight.getFighters(3)) {
			if (target.isDead() || target == fighter || fighter.getTeam2() != target.getTeam2())
				continue;
			int d = Pathfinding.getDistanceBetween(fight.getMap(), fighter.getFightCell().getId(), target
					.getFightCell().getId());
			if (d < dist) {
				dist = d;
				tempTarget = target;
			}
		}
		return tempTarget;
	}
	
	private static Fighter getNearestEnemy(Fight fight, Fighter fighter) {
		int dist = 1000;
		Fighter tempTarget = null;
		for (Fighter target : fight.getFighters(3)) {
			if (target.isDead() || target.isHide() || fighter.getTeam2() == target.getTeam2())
				continue;
			int d = Pathfinding.getDistanceBetween(fight.getMap(), fighter.getFightCell().getId(), target
					.getFightCell().getId());
			if (d < dist) {
				dist = d;
				tempTarget = target;
			}
		}
		return tempTarget;
	}
	
	private static Fighter getFighterNearest(Fight fight, Fighter fighter) {
		int dist = 1000;
		Fighter tempTarget = null;
		for (Fighter target : fight.getFighters(3)) {
			if (target.isDead() || target == fighter)
				continue;
			int d = Pathfinding.getDistanceBetween(fight.getMap(), fighter.getFightCell().getId(), target
					.getFightCell().getId());
			if (d < dist) {
				dist = d;
				tempTarget = target;
			}
		}
		return tempTarget;
	}
	
	private static ArrayList<Fighter> listAllEnemies(Fight fight, Fighter fighter) {
		ArrayList<Fighter> listEnemies = new ArrayList<Fighter>();
		ArrayList<Fighter> enemiesWithoutInvo = new ArrayList<Fighter>();
		ArrayList<Fighter> enemiesInvo = new ArrayList<Fighter>();
		for (Fighter target : fight.getFighters(3)) {
			if (target.isDead() || target.isHide())
				continue;
			if(target.getTeam2() != fighter.getTeam2())
			{
				if (target.isInvocation()) {
					enemiesInvo.add(target);
				} else {
					enemiesWithoutInvo.add(target);
				}
			}
		}
		Random rand = new Random();
		if (rand.nextBoolean()) {
			listEnemies.addAll(enemiesInvo);
			listEnemies.addAll(enemiesWithoutInvo);
		} else {
			listEnemies.addAll(enemiesWithoutInvo);
			listEnemies.addAll(enemiesInvo);
		}
		return listEnemies;
	}
	
	private static ArrayList<Fighter> listLowHPEnemies(Fight fight, Fighter fighter) {
		ArrayList<Fighter> listEnemies = new ArrayList<Fighter>();
		ArrayList<Fighter> enemiesWithoutInvo = new ArrayList<Fighter>();
		ArrayList<Fighter> enemiesInvo = new ArrayList<Fighter>();
		for (Fighter target : fight.getFighters(3)) {
			if (target.isDead() || target.isHide())
				continue;
			if(fighter.getTeam2() != target.getTeam2())
			{
				if (target.isInvocation())
					enemiesInvo.add(target);
				else
					enemiesWithoutInvo.add(target);
			}
		}
		int i = 0;
		int tempPDV;
		Random rand = new Random();
		if (rand.nextBoolean()) {
			try {
				int i3 = enemiesWithoutInvo.size(), i2 = enemiesInvo.size();
				while (i < i2) {
					tempPDV = 200000;
					int index = 0;
					for (Fighter invo : enemiesInvo) {
						if (invo.getLife() <= tempPDV) {
							tempPDV = invo.getLife();
							index = enemiesInvo.indexOf(invo);
						}
					}
					Fighter test = enemiesInvo.get(index);
					if (test != null)
						listEnemies.add(test);
					enemiesInvo.remove(index);
					i++;
				}
				i = 0;
				while (i < i3) {
					tempPDV = 200000;
					int index = 0;
					for (Fighter invo : enemiesWithoutInvo) {
						if (invo.getLife() <= tempPDV) {
							tempPDV = invo.getLife();
							index = enemiesWithoutInvo.indexOf(invo);
						}
					}
					Fighter test = enemiesWithoutInvo.get(index);
					if (test != null)
						listEnemies.add(test);
					enemiesWithoutInvo.remove(index);
					i++;
				}
			} catch (NullPointerException e) {
				return listEnemies;
			}
		} else
			try {
				int i2 = enemiesWithoutInvo.size(), i3 = enemiesInvo.size();
				while (i < i2) {
					tempPDV = 200000;
					int index = 0;
					for (Fighter invo : enemiesWithoutInvo) {
						if (invo.getLife() <= tempPDV) {
							tempPDV = invo.getLife();
							index = enemiesWithoutInvo.indexOf(invo);
						}
					}
					Fighter test = enemiesWithoutInvo.get(index);
					if (test != null)
						listEnemies.add(test);
					enemiesWithoutInvo.remove(index);
					i++;
				}
				i = 0;
				while (i < i3) {
					tempPDV = 200000;
					int index = 0;
					for (Fighter invo : enemiesInvo) {
						if (invo.getLife() <= tempPDV) {
							tempPDV = invo.getLife();
							index = enemiesInvo.indexOf(invo);
						}
					}
					Fighter test = enemiesInvo.get(index);
					if (test != null)
						listEnemies.add(test);
					enemiesInvo.remove(index);
					i++;
				}
			} catch (NullPointerException e) {
				return listEnemies;
			}
		return listEnemies;
	}
	
	private static ArrayList<Fighter> listAllFighters(Fight fight, Fighter fighter) {
		Fighter enemyNearest = getFighterNearest(fight, fighter);
		ArrayList<Fighter> listEnemies = new ArrayList<Fighter>();
		ArrayList<Fighter> enemiesWithoutInvo = new ArrayList<Fighter>();
		ArrayList<Fighter> ennemisInvo = new ArrayList<Fighter>();
		for (Fighter target : fight.getFighters(3)) {
			if (target.isDead())
				continue;
			if (target.isInvocation())
				ennemisInvo.add(target);
			else
				enemiesWithoutInvo.add(target);
		}
		if (enemyNearest != null)
			listEnemies.add(enemyNearest);
		int i = 0;
		int tempPDV;
		Random rand = new Random();
		if (rand.nextBoolean()) {
			try {
				int i3 = enemiesWithoutInvo.size(), i2 = ennemisInvo.size();
				while (i < i2) {
					tempPDV = 200000;
					int index = 0;
					for (Fighter invo : ennemisInvo) {
						if (invo.getLife() <= tempPDV) {
							tempPDV = invo.getLife();
							index = ennemisInvo.indexOf(invo);
						}
					}
					Fighter test = ennemisInvo.get(index);
					if (test != null)
						listEnemies.add(test);
					ennemisInvo.remove(index);
					i++;
				}
				i = 0;
				while (i < i3) {
					tempPDV = 200000;
					int index = 0;
					for (Fighter invo : enemiesWithoutInvo) {
						if (invo.getLife() <= tempPDV) {
							tempPDV = invo.getLife();
							index = enemiesWithoutInvo.indexOf(invo);
						}
					}
					Fighter test = enemiesWithoutInvo.get(index);
					if (test != null)
						listEnemies.add(test);
					enemiesWithoutInvo.remove(index);
					i++;
				}
			} catch (NullPointerException e) {
				return listEnemies;
			}
		} else
			try {
				int i2 = enemiesWithoutInvo.size(), i3 = ennemisInvo.size();
				while (i < i2) {
					tempPDV = 200000;
					int index = 0;
					for (Fighter invo : enemiesWithoutInvo) {
						if (invo.getLife() <= tempPDV) {
							tempPDV = invo.getLife();
							index = enemiesWithoutInvo.indexOf(invo);
						}
					}
					Fighter test = enemiesWithoutInvo.get(index);
					if (test != null)
						listEnemies.add(test);
					enemiesWithoutInvo.remove(index);
					i++;
				}
				i = 0;
				while (i < i3) {
					tempPDV = 200000;
					int index = 0;
					for (Fighter invo : ennemisInvo) {
						if (invo.getLife() <= tempPDV) {
							tempPDV = invo.getLife();
							index = ennemisInvo.indexOf(invo);
						}
					}
					Fighter test = ennemisInvo.get(index);
					if (test != null)
						listEnemies.add(test);
					ennemisInvo.remove(index);
					i++;
				}
			} catch (NullPointerException e) {
				return listEnemies;
			}
		return listEnemies;
	}
	
	private static int attackIfPossibleTaxCollector(Fight fight, Fighter taxCollector) {
		ArrayList<Fighter> listEnemies = getNearestTarget(fight, taxCollector);
		SpellStat SS = null;
		Fighter target = null;
		for (Fighter curFighter : listEnemies) {
			SS = getBestSpellTaxCollector(fight, taxCollector, curFighter);
			if (SS != null) {
				target = curFighter;
				break;
			}
		}
		if (target == null || SS == null)
			return 666;
		int attack = fight.tryCastSpell(taxCollector, SS, target.getFightCell().getId());
		if (attack != 0)
			return attack;
		return 0;
	}
	
	private static int attackIfPossible1(Fight fight, Fighter fighter) {
		ArrayList<Fighter> listEnemies = getNearestTarget(fight, fighter);
		SpellStat SS = null;
		Fighter target = null;
		for (Fighter curFighter : listEnemies) {
			SS = getBestSpellForTarget1(fight, fighter, curFighter);
			if (SS != null) {
				target = curFighter;
				break;
			}
		}
		if (target == null || SS == null)
			return 666;
		int attack = fight.tryCastSpell(fighter, SS, target.getFightCell().getId());
		if (attack != 0)
			return attack;
		return 0;
	}
	
	private static int attackIfPossible2(Fight fight, Fighter fighter) {
		ArrayList<Fighter> listEnemies = getNearestTarget(fight, fighter);
		SpellStat SS = null;
		Fighter target = null;
		for (Fighter curFighter : listEnemies) {
			SS = getBestSpellForTarget2(fight, fighter, curFighter);
			if (SS != null) {
				target = curFighter;
				break;
			}
		}
		if (target == null || SS == null)
			return 666;
		int attack = fight.tryCastSpell(fighter, SS, target.getFightCell().getId());
		if (attack != 0)
			return attack;
		return 0;
	}
	
	private static int attackIfPossible3(Fight fight, Fighter fighter) {
		ArrayList<Fighter> listEnemies = listAllFighters(fight, fighter);
		SpellStat SS = null;
		Fighter target = null;
		for (Fighter curFighter : listEnemies) {
			SS = getBestSpellForTarget2(fight, fighter, curFighter);
			if (SS != null) {
				target = curFighter;
				break;
			}
		}
		if (target == null || SS == null)
			return 666;
		int attack = fight.tryCastSpell(fighter, SS, target.getFightCell().getId());
		if (attack != 0)
			return attack;
		return 0;
	}
	
	private static boolean moveToAttackIfPossible(Fight fight, Fighter fighter) {
		ArrayList<Integer> cells = Pathfinding.listCellFighter(fight, fighter);
		if (cells == null) {
			return false;
		}
		Fighter enemy = getNearestEnemy(fight, fighter);
		if (enemy == null) {
			return false;
		}
		SpellStat spell;
		int distMin = Pathfinding.getDistanceBetween(fight.getMap(), fighter.getFightCell().getId(), enemy
				.getFightCell().getId());
		ArrayList<SpellStat> spells = getLaunchableSpells(fighter, fight, distMin);
		if (spells == null || spells.isEmpty()) {
			return false;
		}
		if (spells.size() == 1)
			spell = spells.get(0);
		else
			spell = spells.get(Formulas.getRandomValue(0, spells.size() - 1));
		ArrayList<Fighter> targets = getNearestTargetForSpell(fight, fighter, spell);
		if (targets == null) {
			return false;
		}
		int cellFinal = 0;
		Fighter target = null;
		boolean found = false;
		for (int cell : cells) {
			for (Fighter O : targets) {
				if (fight.CanCastSpell(fighter, spell, O.getFightCell(), cell)) {
					cellFinal = cell;
					target = O;
					found = true;
				}
				if (found)
					break;
			}
			if (found)
				break;
		}
		if (cellFinal == 0) {
			return false;
		}
		ArrayList<DofusCell> path = Pathfinding.getShortestPathBetween(fight.getMap(), fighter.getFightCell().getId(),	cellFinal, 0);
		if (path == null) {
			return false;
		}
		String pathStr = "";
		try {
			int cellIdTemp = fighter.getFightCell().getId();
			int tempDir = 0;
			for (DofusCell c : path) {
				char dir = Pathfinding.getDirBetweenTwoCase(cellIdTemp, c.getId(), fight.getMap(), true);
				if (dir == 0) {
					return false;
				}
				if (tempDir != dir) {
					if (path.indexOf(c) != 0)
						pathStr += CryptManager.cellIDToCode(cellIdTemp);
					pathStr += dir;
				}
				cellIdTemp = c.getId();
			}
			if (cellIdTemp != fighter.getFightCell().getId())
				pathStr += CryptManager.cellIDToCode(cellIdTemp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		GameAction GA = new GameAction(0, 1, "");
		GA._args = pathStr;
		boolean result = fight.fighterDeplace(fighter, GA);
		if (result && target != null && spell != null) {
			fight.tryCastSpell(fighter, spell, target.getFightCell().getId());
		}
		return result;
	}
	
	private static ArrayList<SpellStat> getLaunchableSpells(Fighter fighter, Fight fight, int distMin) {
		ArrayList<SpellStat> spells = new ArrayList<SpellStat>();
		if (fighter.getMob() == null)
			return null;
		for (Entry<Integer, SpellStat> SS : fighter.getMob().getSpells().entrySet()) {
			SpellStat spell = SS.getValue();
			if (spell.getPACost() > fighter.getCurPA(fight))
				continue;
			if (!CooldownSpell.cooldownElasped(fighter, spell.getSpellID()))
				continue;
			if (spell.getMaxLaunchbyTurn() - CooldownSpell.getNumbCastSpell(fighter, spell.getSpellID()) <= 0
					&& spell.getMaxLaunchbyTurn() > 0)
				continue;
			if (calculInfluence(spell, fighter, fighter) >= 0)
				continue;
			spells.add(spell);
		}
		ArrayList<SpellStat> spellsFinals = SortSpellsInfluence(fighter, spells);
		return spellsFinals;
	}
	
	private static ArrayList<SpellStat> SortSpellsInfluence(Fighter fighter, ArrayList<SpellStat> spells) {
		if (spells == null)
			return null;
		ArrayList<SpellStat> spellsFinals = new ArrayList<SpellStat>();
		Map<Integer, SpellStat> copia = new TreeMap<Integer, SpellStat>();
		for (SpellStat SS : spells) {
			copia.put(SS.getSpellID(), SS);
		}
		int tempInfluence = 0;
		int tempID = 0;
		while (copia.size() > 0) {
			tempInfluence = 0;
			tempID = 0;
			for (Entry<Integer, SpellStat> SS : copia.entrySet()) {
				int influence = -calculInfluence(SS.getValue(), fighter, fighter);
				if (influence > tempInfluence) {
					tempID = SS.getValue().getSpellID();
					tempInfluence = influence;
				}
			}
			if (tempID == 0 || tempInfluence == 0)
				break;
			spellsFinals.add(copia.get(tempID));
			copia.remove(tempID);
		}
		return spellsFinals;
	}
	
	private static ArrayList<Fighter> getNearestTargetForSpell(Fight fight, Fighter fighter, SpellStat spell) {
		ArrayList<Fighter> targets = new ArrayList<Fighter>();
		ArrayList<Fighter> targets1 = new ArrayList<Fighter>();
		int distMax = spell.getMaxPO();
		distMax += fighter.getCurPM(fight);
		ArrayList<Fighter> targetsP = listAllEnemies(fight, fighter);
		for (Fighter entry : targetsP) {
			Fighter target = entry;
			int dist = Pathfinding.getDistanceBetween(fight.getMap(), fighter.getFightCell().getId(), target
					.getFightCell().getId());
			if (dist < distMax)
				targets.add(target);
		}
		while (targets.size() > 0) {
			int index = 0;
			int dista = 1000;
			for (Fighter target : targets) {
				int dist = Pathfinding.getDistanceBetween(fight.getMap(), fighter.getFightCell().getId(), target
						.getFightCell().getId());
				if (dist < dista) {
					dista = dist;
					index = targets.indexOf(target);
				}
			}
			targets1.add(targets.get(index));
			targets.remove(index);
		}
		return targets1;
	}
	
	private static ArrayList<Fighter> getNearestTarget(Fight fight, Fighter fighter) {
		ArrayList<Fighter> targets = new ArrayList<Fighter>();
		ArrayList<Fighter> targets1 = listAllEnemies(fight, fighter);
		while (targets.size() > 0) {
			int index = 0;
			int dista = 1000;
			for (Fighter target : targets) {
				int dist = Pathfinding.getDistanceBetween(fight.getMap(), fighter.getFightCell().getId(), target.getFightCell().getId());
				if (dist < dista) {
					dista = dist;
					index = targets.indexOf(target);
				}
			}
			targets1.add(targets.get(index));
			targets.remove(index);
		}
		return targets1;
	}
	
	private static SpellStat getBestSpellTaxCollector(Fight fight, Fighter taxCollector, Fighter target) {
		int influenceMax = 0;
		SpellStat spellStat = null;
		Map<Integer, Integer> taxCollectorSpells = taxCollector.getTaxCollector().getGuild().getSorts();
		if (target == null)
			return null;
		for (Entry<Integer, Integer> entry : taxCollectorSpells.entrySet()) {
			SpellStat spell1 = World.getSpell(entry.getKey()).getStatsByLevel(entry.getValue());
			if (spell1 == null)
				continue;
			int tempInfluence = 0, influence1 = 0, influence2 = 0;
			int PA = 6;
			int costePA[] = { 0, 0 };
			if (!fight.CanCastSpell(taxCollector, spell1, target.getFightCell(), -1))
				continue;
			tempInfluence = calculInfluence(spell1, taxCollector, target);
			if (tempInfluence == 0)
				continue;
			if (tempInfluence > influenceMax) {
				spellStat = spell1;
				costePA[0] = spellStat.getPACost();
				influence1 = tempInfluence;
				influenceMax = influence1;
			}
			for (Entry<Integer, Integer> SH2 : taxCollectorSpells.entrySet()) {
				SpellStat spell2 = World.getSpell(SH2.getKey()).getStatsByLevel(SH2.getValue());
				if (spell2 == null)
					continue;
				if ( (PA - costePA[0]) < spell2.getPACost())
					continue;
				if (!fight.CanCastSpell(taxCollector, spell2, target.getFightCell(), -1))
					continue;
				tempInfluence = calculInfluence(spell2, taxCollector, target);
				if (tempInfluence == 0)
					continue;
				if ( (influence1 + tempInfluence) > influenceMax) {
					spellStat = spell2;
					costePA[1] = spell2.getPACost();
					influence2 = tempInfluence;
					influenceMax = influence1 + influence2;
				}
				for (Entry<Integer, Integer> SH3 : taxCollectorSpells.entrySet()) {
					SpellStat spell3 = World.getSpell(SH3.getKey()).getStatsByLevel(SH3.getValue());
					if (spell3 == null)
						continue;
					if ( (PA - costePA[0] - costePA[1]) < spell3.getPACost())
						continue;
					if (!fight.CanCastSpell(taxCollector, spell3, target.getFightCell(), -1))
						continue;
					tempInfluence = calculInfluence(spell3, taxCollector, target);
					if (tempInfluence == 0)
						continue;
					if ( (tempInfluence + influence1 + influence2) > influenceMax) {
						spellStat = spell3;
						influenceMax = tempInfluence + influence1 + influence2;
					}
				}
			}
		}
		return spellStat;
	}
	
	private static SpellStat getBestSpellForTarget1(Fight fight, Fighter fighter, Fighter target) {
		int influenceMax = 0;
		SpellStat spellStat = null;
		Map<Integer, SpellStat> mobSpells = fighter.getMob().getSpells();
		if (target == null)
			return null;
		for (Entry<Integer, SpellStat> SS : mobSpells.entrySet()) {
			int tempInfluence = 0, influence1 = 0, influence2 = 0;
			int PA = fighter.getCurPA(fight);
			int costePA[] = { 0, 0 };
			SpellStat spell1 = SS.getValue();
			if (!fight.CanCastSpell(fighter, spell1, target.getFightCell(), -1))
				continue;
			tempInfluence = calculInfluence(spell1, fighter, target);
			if (tempInfluence == 0)
				continue;
			if (tempInfluence > influenceMax) {
				spellStat = spell1;
				costePA[0] = spellStat.getPACost();
				influence1 = tempInfluence;
				influenceMax = influence1;
			}
			for (Entry<Integer, SpellStat> SS2 : mobSpells.entrySet()) {
				SpellStat spell2 = SS2.getValue();
				if ( (PA - costePA[0]) < spell2.getPACost())
					continue;
				if (!fight.CanCastSpell(fighter, spell2, target.getFightCell(), -1))
					continue;
				tempInfluence = calculInfluence(spell2, fighter, target);
				if (tempInfluence == 0)
					continue;
				if ( (influence1 + tempInfluence) > influenceMax) {
					spellStat = spell2;
					costePA[1] = spell2.getPACost();
					influence2 = tempInfluence;
					influenceMax = influence1 + influence2;
				}
				for (Entry<Integer, SpellStat> SS3 : mobSpells.entrySet()) {
					SpellStat spell3 = SS3.getValue();
					if ( (PA - costePA[0] - costePA[1]) < spell3.getPACost())
						continue;
					if (!fight.CanCastSpell(fighter, spell3, target.getFightCell(), -1))
						continue;
					tempInfluence = calculInfluence(spell3, fighter, target);
					if (tempInfluence == 0)
						continue;
					if ( (tempInfluence + influence1 + influence2) > influenceMax) {
						spellStat = spell3;
						influenceMax = tempInfluence + influence1 + influence2;
					}
				}
			}
		}
		return spellStat;
	}
	
	private static SpellStat getBestSpellForTarget2(Fight fight, Fighter fighter, Fighter target) {
		SpellStat spellStat = null;
		ArrayList<SpellStat> possibles = new ArrayList<SpellStat>();
		if (target == null)
			return null;
		try {
			for (Entry<Integer, SpellStat> SS : fighter.getMob().getSpells().entrySet()) {
				SpellStat spell = SS.getValue();
				if (!fight.CanCastSpell(fighter, spell, target.getFightCell(), -1))
					continue;
				possibles.add(spell);
			}
		} catch (NullPointerException e) {
			return null;
		}
		if (possibles.isEmpty())
			return spellStat;
		if (possibles.size() == 1)
			return possibles.get(0);
		spellStat = possibles.get(Formulas.getRandomValue(0, possibles.size() - 1));
		return spellStat;
	}
	
	private static int calculHealInfluence(SpellStat SS) {
		int inf = 0;
		for (SpellEffect SE : SS.getEffects()) {
			int effectId = SE.getEffectID();
			if (effectId == 108 || effectId == 81)
				inf += 100 * Formulas.getMaxJet(SE.getJet());
		}
		return inf;
	}
	
	private static int calculInfluence(SpellStat SS, Fighter fighter, Fighter target) {
		int infTot = 0;
		for (SpellEffect SE : SS.getEffects()) {
			int inf = 0;
			switch (SE.getEffectID()) {
				case 5:
					inf = 500 * Formulas.getMaxJet(SE.getJet());
					break;
				case 6:
					inf = 500 * Formulas.getMaxJet(SE.getJet());
					break;
				case 77:
					inf = 1500 * Formulas.getMaxJet(SE.getJet());
					break;
				case 84:
					inf = 1500 * Formulas.getMaxJet(SE.getJet());
					break;
				case 89:
					inf = 200 * Formulas.getMaxJet(SE.getJet());
					break;
				case 91:
					inf = 150 * Formulas.getMaxJet(SE.getJet());
					break;
				case 92:
					inf = 150 * Formulas.getMaxJet(SE.getJet());
					break;
				case 93:
					inf = 150 * Formulas.getMaxJet(SE.getJet());
					break;
				case 94:
					inf = 150 * Formulas.getMaxJet(SE.getJet());
					break;
				case 95:
					inf = 150 * Formulas.getMaxJet(SE.getJet());
					break;
				case 96:
					inf = 100 * Formulas.getMaxJet(SE.getJet());
					break;
				case 97:
					inf = 100 * Formulas.getMaxJet(SE.getJet());
					break;
				case 98:
					inf = 100 * Formulas.getMaxJet(SE.getJet());
					break;
				case 99:
					inf = 100 * Formulas.getMaxJet(SE.getJet());
					break;
				case 100:
					inf = 100 * Formulas.getMaxJet(SE.getJet());
					break;
				case 101:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 108:
					inf = -1000;
					break;
				case 111:
					inf = -1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 112:
					inf = -1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 114:
					inf = -1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 117:
					inf = -500 * Formulas.getMaxJet(SE.getJet());
					break;
				case 121:
					inf = -100 * Formulas.getMaxJet(SE.getJet());
					break;
				case 122:
					inf = 200 * Formulas.getMaxJet(SE.getJet());
					break;
				case 123:
					inf = -200 * Formulas.getMaxJet(SE.getJet());
					break;
				case 124:
					inf = -200 * Formulas.getMaxJet(SE.getJet());
					break;
				case 125:
					inf = -200 * Formulas.getMaxJet(SE.getJet());
					break;
				case 126:
					inf = -200 * Formulas.getMaxJet(SE.getJet());
					break;
				case 127:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 128:
					inf = -1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 131:
					inf = 300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 132:
					inf = 2000;
					break;
				case 138:
					inf = -50 * Formulas.getMaxJet(SE.getJet());
					break;
				case 150:
					inf = -2000;
					break;
				case 152:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 153:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 154:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 155:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 156:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 157:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 162:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 163:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 168:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 169:
					inf = 1000 * Formulas.getMaxJet(SE.getJet());
					break;
				case 176:
					inf = -300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 178:
					inf = -300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 210:
					inf = -300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 211:
					inf = -300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 212:
					inf = -300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 213:
					inf = -300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 214:
					inf = -300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 215:
					inf = 300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 216:
					inf = 300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 217:
					inf = 300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 218:
					inf = 300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 219:
					inf = 300 * Formulas.getMaxJet(SE.getJet());
					break;
				case 265:
					inf = -250 * Formulas.getMaxJet(SE.getJet());
					break;
				case 765:
					inf = -1000;
					break;
				case 786:
					inf = -1000;
					break;
			}
			if (target == null)
				continue;
			if (fighter.getTeam() == target.getTeam())
				infTot -= inf;
			else
				infTot += inf;
		}
		return infTot;
	}
}
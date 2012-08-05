package objects.spell;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import objects.character.Stats;
import objects.fight.Fight;
import objects.fight.Fighter;
import objects.fight.Glyph;
import objects.fight.Trap;
import objects.map.DofusCell;
import objects.monster.MonsterGrade;

import common.Constants;
import common.SocketManager;
import common.World;
import common.console.Log;
import common.utils.CryptManager;
import common.utils.Formulas;
import common.utils.Pathfinding;

public class SpellEffect
{
	private int effectID;
	private int turns = 0;
	private String jet = "0d0+0";
	private int chance = 100;
	private String args;
	private int value = 0;
	private Fighter caster = null;
	private int spellId = 0;
	private int spellLvl = 1;
	private boolean debuffable = true;
	private int duration = 0;
	private DofusCell cell = null;
	private boolean isPoison = false;

	public SpellEffect(final int aID,final String aArgs,final int aSpell,final int aSpellLevel)
	{
		effectID = aID;
		args = aArgs;
		spellId = aSpell;
		spellLvl = aSpellLevel;
		try
		{
			value = Integer.parseInt(args.split(";")[0]);

			turns = Integer.parseInt(args.split(";")[3]);
			chance= Integer.parseInt(args.split(";")[4]);
			jet = args.split(";")[5];

		}catch(final Exception e){};
	}

	public SpellEffect(final int id, final int value2,final int aduration, final int turns2, final boolean debuffabl,final Fighter aCaster, final String args2, final int aspell, final boolean aIsPoison)
	{
		effectID = id;
		value = value2;
		turns = turns2;
		debuffable = debuffabl;
		caster = aCaster;
		duration = aduration;
		args = args2;
		spellId = aspell;
		try
		{
			jet = args.split(";")[5];
		}catch(final Exception e){};
		isPoison = aIsPoison;
	}

	public int getDuration()
	{
		return duration;
	}

	public int getTurns() {
		return turns;
	}

	public boolean isDebuffable()
	{
		return debuffable;
	}

	public void setTurn(final int turn) {
		this.turns = turn;
	}

	public int getEffectID() {
		return effectID;
	}

	public String getJet() {
		return jet;
	}

	public int getValue() {
		return value;
	}
	public int getChance() {
		return chance;
	}

	public String getArgs() {
		return args;
	}
	
	private boolean isPoison() {
		return isPoison;
	}

	public static ArrayList<Fighter> getTargets(final SpellEffect SE,final Fight fight, final ArrayList<DofusCell> cells)
	{
		final ArrayList<Fighter> cibles = new ArrayList<Fighter>(); 
		if(SE.getSpell() == 165)
		{
			ArrayList<DofusCell> newCells = new ArrayList<DofusCell>();
			int i = cells.size() - 1;
			while(i >= 0) {
				newCells.add(cells.get(i));
				i--;
			}
			cells.clear();
			cells.addAll(newCells);
		}else if(SE.getSpell() == 418)
		{
			ArrayList<DofusCell> newCells = new ArrayList<DofusCell>();
			int value = 4;
			for(DofusCell cell : cells)
			{
				if ((value % 4) == 0)
					newCells.add(cell);
				else if ((value % 4) == 1)
					newCells.add(cell);
				else if ((value % 4) == 2)
					newCells.add(cell);
				else
					newCells.add(cell);
				value++;
			}
			cells.clear();
			cells.addAll(newCells);
		}
		for(final DofusCell aCell : cells)
		{
			if(aCell == null)continue;
			final Fighter f = aCell.getFirstFighter();
			if(f == null)continue;
			cibles.add(f);
		}
		return cibles;
	}

	public void setValue(final int i)
	{
		value = i;
	}

	public int decrementDuration()
	{
		duration -= 1;
		return duration;
	}

	public void applyBeginingBuff(final Fight _fight, final Fighter fighter)
	{
		final ArrayList<Fighter> cible = new ArrayList<Fighter>();
		cible.add(fighter);
		turns = -1;
		applyToFight(_fight,caster,cible, null, false);
	}

	public void applyToFight(final Fight fight, final Fighter perso,final DofusCell Cell,final ArrayList<Fighter> cibles, final ArrayList<DofusCell> cells)
	{
		cell = Cell;
		applyToFight(fight,perso,cibles,cells,false);
	}

	public static int applyOnHitBuffs(int finalDommage,final Fighter target,final Fighter caster,final Fight fight, final int spellId)
	{
		for(final int id : Constants.ON_HIT_BUFFS)
		{
			for(final SpellEffect buff : target.getBuffsByEffectID(id))
			{
				switch(id)
				{
				case 9://Derobade
					//Si pas au cac (distance == 1)
					final int d = Pathfinding.getDistanceBetween(fight.getMap(), target.getFightCell().getId(), caster.getFightCell().getId());
					if(d >1)continue;
					final int chan = buff.getValue();
					final int c = Formulas.getRandomValue(0, 99);
					if(c+1 >= chan)continue;//si le deplacement ne s'applique pas
					int nbrCase = 0;
					try
					{
						nbrCase = Integer.parseInt(buff.getArgs().split(";")[1]);	
					}catch(final Exception e){};
					if(nbrCase == 0 || target.hasState(6) || target.isStatic())continue;
					final int exCase = target.getFightCell().getId();
					int newCellID = Pathfinding.newCaseAfterPush(fight.getMap(), caster.getFightCell(), target.getFightCell(), nbrCase, fight);
					if(newCellID <0)//S'il a été bloqué
					{
						int a = -newCellID;
						
						/*final int coef = Formulas.getRandomJet("1d8+8");
						double b = (caster.getLvl()/(double)(50.00));
						if(b<0.1)b= 0.1;
						final double e = b*a;//Calcule des dégats de poussé
						int finalDamage = (int)(coef * e);*/
						int dgt = Formulas.getRandomJet("1d8+0");
						double b = (caster.getLvl()/(double)(50.00));
						if(b<0.1)b= 0.1;
						int finalDamage = (int) ((8 + dgt * b) * nbrCase);
						if(finalDamage < 1)finalDamage = 1;
						if(finalDamage>target.getLife())finalDamage = target.getLife();//Target va mourrir
						target.removeLife(finalDamage);
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+",-"+finalDamage);
						if(target.getLife() <= 0)
						{
							fight.onFighterDie(target);
							continue;
						}
						
						a = nbrCase-a;
						newCellID =	Pathfinding.newCaseAfterPush(fight.getMap(),caster.getFightCell(),target.getFightCell(),a, fight);
						if(newCellID == 0)
							continue;
						if(fight.getMap().getCell(newCellID) == null)
							continue;
					}
					target.getFightCell().getFighters().clear();
					target.setFightCell(fight.getMap().getCell(newCellID));
					target.getFightCell().addFighter(target);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, target.getGUID()+"", target.getGUID()+","+newCellID);

					final ArrayList<Trap> P = (new ArrayList<Trap>());
					P.addAll(fight.getTraps());
					for(final Trap p : P)
					{
						final int dist = Pathfinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),target.getFightCell().getId());
						//on active le piege
						if(dist <= p.getSize())p.onTraped(target);
					}
					//si le joueur a bouger
					if(exCase != newCellID)
					finalDommage = 0;
					break;

				case 79://chance éca
					try
					{
						final String[] infos = buff.getArgs().split(";");
						final int coefDom = Integer.parseInt(infos[0]);
						final int coefHeal = Integer.parseInt(infos[1]);
						final int chance = Integer.parseInt(infos[2]);
						final int jet = Formulas.getRandomValue(0, 99);

						if(jet < chance)//Soin
						{
							finalDommage = -(finalDommage*coefHeal);
							if(-finalDommage > (target.getMaxLife() - target.getLife()))finalDommage = -(target.getMaxLife() - target.getLife());
						}else//Dommage
							finalDommage = finalDommage*coefDom;
					}catch(final Exception e){};
					break;
					
				case 89:
					int dgt = Formulas.getRandomJet(buff.getJet());
					dgt = (dgt / 100) * caster.getLife();
					int finalDamage = Formulas.calculFinalDamage(fight, caster, target, Constants.ELEMENT_NEUTRE, dgt, false, false, true);
					if(finalDamage < 0)
						finalDamage = 0;
					if(finalDamage > target.getLife())
						finalDamage = target.getLife();
					target.removeLife(finalDamage);
					finalDamage = -(finalDamage);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDamage);
					break;

				case 107://renvoie Dom
				case 220:
					switch (spellId) {
						case 66:
						case 71:
						case 181:
						case 196:
						case 200:
						case 219:
						case 164:
							continue;
					}
					if(buff.isPoison())
						continue;
					final String[] args = buff.getArgs().split(";");
					int returnSpell = 0, returnStuff = 0, returnTotal = 0;
					try
					{
						if (Integer.parseInt(args[1]) != -1)
							returnSpell = Formulas.getRandomValue(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
						else
							returnSpell = Integer.parseInt(args[0]);
					}catch(final Exception e){returnSpell = 0;}
					if(target.isPlayer())
						returnStuff = target.getPlayer().getStuffStats().getEffect(Constants.STATS_RETDOM);
					returnTotal = (int) (returnSpell * (1 + (double)target.getTotalStats().getEffect(Constants.STATS_ADD_SAGE)/100) + returnStuff);
					if(returnTotal > finalDommage)returnTotal = finalDommage;
					finalDommage -= returnTotal;
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 107, "-1", target.getGUID()+","+returnTotal);
					if(returnTotal>caster.getLife())returnTotal = caster.getLife();
					if(finalDommage<0)finalDommage =0;
					caster.removeLife(returnTotal);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", caster.getGUID()+",-"+returnTotal);
					break;
					
				case 776: // Dégats insoignables
					if(target.hasBuff(776)) {
						int pdvMax = target.getMaxLife();
						final float prc = target.getBuffValue(776) / 100f;
						pdvMax -= (int) (finalDommage * prc);
						if(pdvMax < 0)
							pdvMax = 0;
						target.setMaxLife(pdvMax);
					}
					break;
					
				case 786: //Heal sur attaque
					caster.heal(finalDommage);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", caster.getGUID()+",+"+finalDommage);
					break;

				case 788://Chatiments
					int taux = (caster.getPlayer() == null?1:2);
					int gain = finalDommage / taux;
					final int stat = buff.getValue();
					int max = 0;
					try
					{
						max = Integer.parseInt(buff.getArgs().split(";")[1]);
					}catch(final Exception e){};
					if(max <= 0)continue;

					//on retire au max possible la valeur déjà gagné sur le chati
					final int a = (target.getChatiValue().get(stat)==null?0:target.getChatiValue().get(stat));
					max -= a;
					//Si gain trop grand, on le reduit au max
					if(gain > max)gain = max;

					//on ajoute le buff
					target.addBuff(stat, gain, 5, 1, true, buff.getSpell(), buff.getArgs(), caster, buff.isPoison());
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, caster.getGUID()+"", target.getGUID()+","+gain+","+5);
					//On met a jour les valeurs des chatis
					final int value = a + gain;
					target.getChatiValue().put(stat, value);	
					break;

				default:
					Log.addToLog("Effect id "+id+" définie comme ON_HIT_BUFF mais n'a pas d'effet définie dans ce gestionnaire.");
					break;
				}
			}
		}

		return finalDommage;
	}

	public Fighter getCaster() {
		return caster;
	}

	public int getSpell() {
		return spellId;
	}

	public void applyToFight(final Fight fight,final Fighter acaster, final ArrayList<Fighter> cibles, final ArrayList<DofusCell> cells, final boolean isCac)
	{
		Log.addToLog("Effet id: "+effectID+" Args: "+args+" turns: "+turns+" cibles: "+cibles.size()+" chance: "+chance);
		try
		{
			if(turns != -1)//Si ce n'est pas un buff qu'on applique en début de tour
				turns = Integer.parseInt(args.split(";")[3]);
		}catch(final NumberFormatException e){}

		caster = acaster;
		switch(effectID)
		{
		case 4://Fuite/Bond du félin/ Bond du iop / téléport
			applyEffect_4(fight,cibles);
			break;
		case 5://Repousse de X case
			applyEffect_5(cibles,fight);
			break;
		case 6://Attire de X case
			applyEffect_6(cibles,fight);
			break;
		case 8://Echange les place de 2 joueur
			applyEffect_8(cibles,fight);
			break;
		case 9://Esquive une attaque en reculant de 1 case
			applyEffect_9(cibles,fight);
			break;
		case 50://Porter
			applyEffect_50(fight);
			break;
		case 51://jeter
			applyEffect_51(fight);
			break;
		case 77://Vol de PM
			applyEffect_77(cibles,fight);
			break;
		case 78://Bonus PM
			applyEffect_78(cibles,fight);
			break;
		case 79:// + X chance(%) dommage subis * Y sinon soigné de dommage *Z 
			applyEffect_79(cibles,fight);
			break;
		case 81://Soin de pdv
			applyEffect_81(cibles,fight);
			break;
		case 82://Vol de Vie fixe
			applyEffect_82(cibles,fight);
			break;
		case 84://Vol de PA
			applyEffect_84(cibles,fight);
			break;
		case 85://Dommage Eau %vie
			applyEffect_85(cibles,fight);
			break;
		case 86://Dommage Terre %vie
			applyEffect_86(cibles,fight);
			break;
		case 87://Dommage Air %vie
			applyEffect_87(cibles,fight);
			break;
		case 88://Dommage feu %vie
			applyEffect_88(cibles,fight);
			break;
		case 89://Dommage neutre %vie
			applyEffect_89(cibles,fight);
			break;
		case 90://Donne X% de sa vie
			applyEffect_90(cibles,fight);
			break;
		case 91://Vol de Vie Eau
			applyEffect_91(cibles,fight,isCac);
			break;
		case 92://Vol de Vie Terre
			applyEffect_92(cibles,fight,isCac);
			break;
		case 93://Vol de Vie Air
			applyEffect_93(cibles,fight,isCac);
			break;
		case 94://Vol de Vie feu
			applyEffect_94(cibles,fight,isCac);
			break;
		case 95://Vol de Vie neutre
			applyEffect_95(cibles,fight,isCac);
			break;
		case 96://Dommage Eau
			applyEffect_96(cibles,fight,isCac);
			break;
		case 97://Dommage Terre 
			applyEffect_97(cibles,fight,isCac);
			break; 
		case 98://Dommage Air 
			applyEffect_98(cibles,fight,isCac);
			break;
		case 99://Dommage feu 
			applyEffect_99(cibles,fight,isCac);
			break;
		case 100://Dommage neutre
			applyEffect_100(cibles,fight,isCac);
			break;
		case 101://Retrait PA
			applyEffect_101(cibles,fight);
			break;
		case 105://Dommages réduits de X
			applyEffect_105(cibles,fight);
			break;
		case 106://Renvoie de sort
			applyEffect_106(cibles,fight);
			break;
		case 107://Renvoie de dom
			applyEffect_107(cibles,fight);
			break;
		case 108://Soin
			applyEffect_108(cibles,fight,isCac);
			break;
		case 109://Dommage pour le lanceur
			applyEffect_109(fight);
			break;
		case 110://+ X vie
			applyEffect_110(cibles,fight);
			break;
		case 111://+ X PA
			applyEffect_111(cibles,fight);
			break;
		case 112://+Dom
			applyEffect_112(cibles,fight);
			break;
		case 114://Multiplie les dommages par X
			applyEffect_114(cibles,fight);
			break;
		case 115://+Cc
			applyEffect_115(cibles,fight);
			break;
		case 116://Malus PO
			applyEffect_116(cibles,fight);
			break;
		case 117://Bonus PO
			applyEffect_117(cibles,fight);
			break;
		case 118://Bonus force
			applyEffect_118(cibles,fight);
			break;
		case 119://Bonus Agilité
			applyEffect_119(cibles,fight);
			break;
		case 120://Bonus PA
			applyEffect_120(cibles,fight);
			break;
		case 121://+Dom
			applyEffect_121(cibles,fight);
			break;
		case 122://+EC
			applyEffect_122(cibles,fight);
			break;
		case 123://+Chance
			applyEffect_123(cibles,fight);
			break;
		case 124://+Sagesse
			applyEffect_124(cibles,fight);
			break;
		case 125://+Vitalité
			applyEffect_125(cibles,fight);
			break;
		case 126://+Intelligence
			applyEffect_126(cibles,fight);
			break;
		case 127://Retrait PM
			applyEffect_127(cibles,fight);
			break;
		case 128://+PM
			applyEffect_128(cibles,fight);
			break;
		case 130://Vol de kamas
			applyEffect_130(cibles,fight);
			break;
		case 131://Poison : X Pdv  par PA
			applyEffect_131(cibles,fight);
			break;
		case 132://Enleve les envoutements
			applyEffect_132(cibles,fight);
			break;
		case 138://%dom
			applyEffect_138(cibles,fight);
			break;
		case 140://Passer le tour
			applyEffect_140(cibles,fight);
			break;
		case 141://Tue la cible
			applyEffect_141(fight,cibles);
			break;
		case 142://Dommages physique
			applyEffect_142(fight,cibles);
			break;
		case 143://Heal
			applyEffect_143(fight,cibles);
			break;
		case 144://Minus damage
			applyEffect_144(fight,cibles);
			break;
		case 145://Malus Dommage
			applyEffect_145(fight,cibles);
			break;
		case 149://Change l'apparence
			applyEffect_149(fight,cibles);
			break;
		case 150://Invisibilité
			applyEffect_150(fight,cibles);
			break;
		case 152:// - Chance
			applyEffect_152(fight,cibles);
			break;
		case 153:// - Vitalitée
			applyEffect_153(fight,cibles);
			break;
		case 154:// - Agilitée
			applyEffect_154(fight,cibles);
			break;
		case 155:// - Intell
			applyEffect_155(fight,cibles);
			break;
		case 156:// - Sagesse
			applyEffect_156(fight,cibles);
			break;
		case 157:// - Force
			applyEffect_157(fight,cibles);
			break;
		case 160:// + Esquive PA
			applyEffect_160(fight,cibles);
			break;
		case 161:// + Esquive PM
			applyEffect_161(fight,cibles);
			break;
		case 162:// - Esquive PA
			applyEffect_162(fight,cibles);
			break;
		case 163:// - Esquive PM
			applyEffect_163(fight,cibles);
			break;
		case 164:// - % doms
			applyEffect_164(fight,cibles);
			break;
		case 165:// Maîtrises
			applyEffect_165(fight,cibles);
			break;
		case 168://Perte PA non esquivable
			applyEffect_168(fight,cibles);
			break;
		case 169://Perte PM non esquivable
			applyEffect_169(fight,cibles);
			break;
		case 171://Malus CC
			applyEffect_171(fight,cibles);
			break;
		case 176://Bonus PP
			applyEffect_176(fight,cibles);
			break;
		case 177://Malus PP
			applyEffect_177(fight,cibles);
			break;
		case 178://Bonus SOIN
			applyEffect_178(fight,cibles);
			break;
		case 179://Malus SOIN
			applyEffect_179(fight,cibles);
			break;
		case 180://Double sram
			applyEffect_180(fight,cibles);
			break;
		case 181://Invoque une créature
			applyEffect_181(fight);
			break;
		case 182://+ Crea Invoc
			applyEffect_182(fight,cibles);
			break;
		case 183://Resist Magique
			applyEffect_183(fight,cibles);
			break;
		case 184://Resist Physique
			applyEffect_184(fight,cibles);
			break;
		case 185://Invoque une creature statique
			applyEffect_185(fight);
			break;
		case 186://Diminue les dégats
			applyEffect_186(fight,cibles);
			break;
		case 202://Faire apparaitre
			applyEffect_202(fight,cibles,cells);
			break;
		case 210://Resist % terre
			applyEffect_210(fight,cibles);
			break;
		case 211://Resist % eau
			applyEffect_211(fight,cibles);
			break;
		case 212://Resist % air
			applyEffect_212(fight,cibles);
			break;
		case 213://Resist % feu
			applyEffect_213(fight,cibles);
			break;
		case 214://Resist % neutre
			applyEffect_214(fight,cibles);
			break;
		case 215://Faiblesse % terre
			applyEffect_215(fight,cibles);
			break;
		case 216://Faiblesse % eau
			applyEffect_216(fight,cibles);
			break;
		case 217://Faiblesse % air
			applyEffect_217(fight,cibles);
			break;
		case 218://Faiblesse % feu
			applyEffect_218(fight,cibles);
			break;
		case 219://Faiblesse % neutre
			applyEffect_219(fight,cibles);
			break;
		case 220://Renvoi des dégats d'attaques
			applyEffect_220(fight,cibles);
			break;
		case 265://Reduit les Dom de X
			applyEffect_265(fight,cibles);
			break;
		case 266://Vol Chance
			applyEffect_266(fight,cibles);
			break;
		case 267://Vol vitalité
			applyEffect_267(fight,cibles);
			break;
		case 268://Vol agitlité
			applyEffect_268(fight,cibles);
			break;
		case 269://Vol intell
			applyEffect_269(fight,cibles);
			break;
		case 270://Vol sagesse
			applyEffect_270(fight,cibles);
			break;
		case 271://Vol force
			applyEffect_271(fight,cibles);
			break;
		case 275://Dégats d'eau % de l'attaquant
			applyEffect_275(fight,cibles);
			break;
		case 276://Dégats force % de l'attaquant
			applyEffect_276(fight,cibles);
			break;
		case 277://Dégats air % de l'attaquant
			applyEffect_277(fight,cibles);
			break;
		case 278://Dégats feu % de l'attaquant
			applyEffect_278(fight,cibles);
			break;
		case 279://Dégats neutre % de l'attaquant
			applyEffect_279(fight,cibles);
			break;
		case 293://Augmente les dégâts de base du sort X de Y
			applyEffect_293(fight);
			break;
		case 320://Vol de PO
			applyEffect_320(fight,cibles);
			break;
		case 400://Créer un  piège
			applyEffect_400(fight);
			break;
		case 401://Créer un glyphe
			applyEffect_401(fight);
			break;
		case 402://Créer un blyphe
			applyEffect_402(fight);
			break;
		case 666://Pas d'effet complémentaire
			break;
		case 671://Dommages : X% de la vie de l'attaquant (neutre)
			applyEffect_671(cibles,fight);
			break;
		case 672://Dommages : X% de la vie de l'attaquant (neutre)
			applyEffect_672(cibles,fight);
			break;
		case 750://Bonus de capture
			applyEffect_750(cibles,fight);
			break;
		case 765://Interchange la place de deux joueur
			applyEffect_765(cibles,fight);
			break;
		case 776://+ % de dégats insoignable
			applyEffect_776(cibles,fight);
			break;
		case 780://Réinvoque un combattant mort
			applyEffect_780(cibles,fight);
			break;
		case 781://Minimise les dégats
			applyEffect_781(cibles,fight);
			break;
		case 782://Maximise les dégats
			applyEffect_782(cibles,fight);
			break;
		case 783://Pousse jusqu'a la case visé
			applyEffect_783(cibles,fight);
			break;
		case 784://Téléporte aux place du début de combat
			applyEffect_784(cibles,fight);
			break;
		case 786://Soin sur attaque
			applyEffect_786(cibles,fight);
			break;
		case 787://Soin sur attaque
			applyEffect_787(cibles,fight);
			break;
		case 788://Chatiment de X sur Y tours
			applyEffect_788(cibles,fight);
			break;
		case 950://Etat X
			applyEffect_950(fight,cibles);
			break;
		case 951://Enleve l'Etat X
			applyEffect_951(fight,cibles);
			break;
		default:
			Log.addToLog("effet non implanté : "+effectID+" args: "+args);
			break;
		}
	}

	private void applyEffect_202(Fight fight, ArrayList<Fighter> cibles, ArrayList<DofusCell> cells) {
		for(Fighter target : cibles)
		{
			if(target.getTeam() == caster.getTeam())continue;
			if(target.isHide()) target.unHide(spellId);
		}
		if(cells != null && cells.size() > 0)
		{
			for(Trap trap : fight.getTraps())
			{
				if(trap.getTeam() == caster.getTeam())
					continue;
				for(DofusCell cell : cells) 
				{
					if(cell.getId() == trap.getCell().getId()) 
					{
						trap.setVisible(caster.getTeam());
						trap.appear(caster.getTeam());
					}
				}
			}
		}
	}

	private void applyEffect_787(final ArrayList<Fighter> cibles, final Fight fight) {
		int spellId = -1;
		int spellLvl = -1;
		try {
			spellId = Integer.parseInt(args.split(";")[0]);
			spellLvl = Integer.parseInt(args.split(";")[1]);
		} catch(final NumberFormatException e) {
			return;
		}
		final Spell spell = World.getSpell(spellId);
		final ArrayList<SpellEffect> effects = spell.getStatsByLevel(spellLvl).getEffects();
		for(final SpellEffect effect : effects) {
			for(final Fighter target : cibles) {
				target.addBuff(effect.getEffectID(), effect.getValue(), effect.getTurns(), 1, true, spellId, effect.getArgs(), effect.getCaster(), effect.isPoison());
			}
		}
	}

	private void applyEffect_786(final ArrayList<Fighter> cibles, final Fight fight) {
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, value, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_784(final ArrayList<Fighter> cibles, final Fight fight) {
		for(final Fighter target : cibles)
		{
			if(target == null || target.isInvocation() /*|| target.isDouble()*/ || target.isDead() || target.hasLeft()) continue;
            int newCell = -1;
            try {
                newCell = fight.getInitialPosition().get(target.getGUID());
            } catch(final Exception e){continue;}
            if(newCell != target.getFightCell().getId() && newCell >= 0 && fight.getMap().getCell(newCell).getFighters().size() <= 0)
            {
            	target.getFightCell().getFighters().clear();
            	target.setFightCell(fight.getMap().getCell(newCell));
            	target.getFightCell().addFighter(target);
                SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, ""+target.getGUID(), target.getGUID()+","+newCell);
            	final ArrayList<Trap> traps = new ArrayList<Trap>();
        		traps.addAll(fight.getTraps());
        		for(final Trap p : traps)
        		{
        			final int dist = Pathfinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),target.getFightCell().getId());
        			if(dist <= p.getSize())p.onTraped(target);
        		}
            }
		}
	}

	private void applyEffect_782(final ArrayList<Fighter> cibles, final Fight fight) {
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, value, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_781(final ArrayList<Fighter> cibles, final Fight fight) {
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, value, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_780(final ArrayList<Fighter> cibles, final Fight fight) {
		final Map<Integer,Fighter> deads = fight.getDeadList();
		Fighter target = null;
		for(final Entry<Integer,Fighter> entry : deads.entrySet())
		{
			if(entry.getValue().hasLeft()) 
				continue;
			if(entry.getValue().getTeam() == caster.getTeam())
			{
				if(entry.getValue().isInvocation()) 
					if(entry.getValue().getInvocator().isDead()) 
						continue; 
				target = entry.getValue();
			}
		}
		if(target == null)
			return;
				
		fight.addFighterInTeam(target, target.getTeam());
		target.setDead(false);
		target.getFightBuff().clear();
		
		if(!target.isInvocation()) 
			SocketManager.GAME_SEND_ILF_PACKET(target.getPlayer(), 0); 
		else 
			fight.getListFighter().add((fight.getListFighter().indexOf(target.getInvocator())+1),target); 
			
		target.setFightCell(cell);
		target.getFightCell().addFighter(target);
				
		target.fullPDV();
		final int percent = (100-value)*target.getMaxLife()/100;
		target.removeLife(percent);
				
		final String gm = target.getGmPacket('+').substring(3);
		final String gtl = fight.getGTL();
		//SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, target.getGUID() + "", gm);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 780, target.getGUID() + "", gm);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, target.getGUID()+"", gtl);
		if(!target.isInvocation()) 
			SocketManager.GAME_SEND_STATS_PACKET(target.getPlayer()); 
		fight.removeFighterDeadList(target);
		target.setInvocator(caster);
		caster.incrNumbInvocations();
		final ArrayList<Trap> traps = (new ArrayList<Trap>());
		traps.addAll(fight.getTraps());
		for(final Trap p : traps)
		{
			final int dist = Pathfinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),target.getFightCell().getId());
			if(dist <= p.getSize())p.onTraped(target);
		}
	}

	private void applyEffect_776(final ArrayList<Fighter> cibles, final Fight fight) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}
	
	private void applyEffect_765B(final Fight fight,final Fighter target)
	{
		final Fighter sacrified = target.getBuff(765).getCaster();
		final DofusCell cell1 = sacrified.getFightCell();
		final DofusCell cell2 = target.getFightCell();
		
		sacrified.getFightCell().getFighters().clear();
		target.getFightCell().getFighters().clear();
		sacrified.setFightCell(cell2);
		sacrified.getFightCell().addFighter(sacrified);
		target.setFightCell(cell1);
		target.getFightCell().addFighter(target);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, target.getGUID()+"", target.getGUID()+","+cell1.getId());
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, sacrified.getGUID()+"", sacrified.getGUID()+","+cell2.getId());
	}

	private void applyEffect_765(final ArrayList<Fighter> cibles, final Fight fight) {
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, 0, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_750(final ArrayList<Fighter> cibles, final Fight fight) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_671(final ArrayList<Fighter> cibles, final Fight fight) {
		final float val = ((float)Formulas.getRandomJet(jet)/(float)100);
		final int maxLife = caster.getPdvMaxOutFight();
		final int midLife = maxLife / 2;
		final float pct = 1 - ((Math.abs(caster.getLife() - midLife)) / (float) midLife);
		final int dgt = (int) (pct * val * maxLife);
		
		
		for(Fighter target : cibles)
		{
			//si la cible a le buff renvoie de sort
			if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl )
			{
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
				//le lanceur devient donc la cible
				target = caster;
			}
			
			if(target.hasBuff(765))//sacrifice
			{
				if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
				{
					applyEffect_765B(fight,target);
					target = target.getBuff(765).getCaster();
				}
			}

			int finalDommage = applyOnHitBuffs(dgt,target,caster,fight,spellId);//S'il y a des buffs spéciaux

			if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
			target.removeLife(finalDommage);
			finalDommage = -(finalDommage);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
			if(target.getLife() <=0)
				fight.onFighterDie(target);
		}
	}

	private void applyEffect_402(final Fight fight) {
		if(!cell.isWalkable(true))return;//Si case pas marchable

		final String[] infos = args.split(";");
		final int spellID = Short.parseShort(infos[0]);
		final int level = Byte.parseByte(infos[1]);
		final byte duration = Byte.parseByte(infos[3]);
		final String po = World.getSpell(spellId).getStatsByLevel(spellLvl).getRangeType();
		final byte size = (byte) CryptManager.getIntByHashedValue(po.charAt(1));
		final SpellStat SS = World.getSpell(spellID).getStatsByLevel(level);
		final Glyph g = new Glyph(fight,caster,cell,size,SS,duration,spellId);
		fight.getGlyphs().add(g);
		final int unk = g.getColor();
		String str = "GDZ+"+cell.getId()+";"+size+";"+unk;
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getGUID()+"", str);
		str = "GDC"+cell.getId()+";Haaaaaaaaa3005;";
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getGUID()+"", str);
	}

	private void applyEffect_279(final Fight fight, final ArrayList<Fighter> cibles) {
		if(turns <= 1) {
			int damage = Formulas.getRandomJet(jet);
			damage = (int) ((float)(damage / 100) * caster.getLife());
			for(Fighter target : cibles)
			{
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int finalDamage = Formulas.calculFinalDamage(fight, caster, target, Constants.ELEMENT_NEUTRE, damage, false, false, isPoison);
				finalDamage = applyOnHitBuffs(finalDamage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDamage>target.getLife())finalDamage = target.getLife();//Caster va mourrir
				target.removeLife(finalDamage);
				finalDamage = -(finalDamage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", target.getGUID()+","+finalDamage);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		} else {
			
			for(final Fighter target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);
			}
		}
	}

	private void applyEffect_278(final Fight fight, final ArrayList<Fighter> cibles) {
		if(turns <= 1) {
			int damage = Formulas.getRandomJet(jet);
			damage = (int) ((float)(damage / 100) * caster.getLife());
			for(Fighter target : cibles)
			{
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int finalDamage = Formulas.calculFinalDamage(fight, caster, target, Constants.ELEMENT_FEU, damage, false, false, isPoison);
				finalDamage = applyOnHitBuffs(finalDamage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDamage>target.getLife())finalDamage = target.getLife();//Caster va mourrir
				target.removeLife(finalDamage);
				finalDamage = -(finalDamage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", target.getGUID()+","+finalDamage);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		} else {
			isPoison = true;
			for(final Fighter target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);
			}
		}
	}

	private void applyEffect_277(final Fight fight, final ArrayList<Fighter> cibles) {
		if(turns <= 1) {
			int damage = Formulas.getRandomJet(jet);
			damage = (int) ((float)(damage / 100) * caster.getLife());
			for(Fighter target : cibles)
			{
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int finalDamage = Formulas.calculFinalDamage(fight, caster, target, Constants.ELEMENT_AIR, damage, false, false, isPoison);
				finalDamage = applyOnHitBuffs(finalDamage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDamage>target.getLife())finalDamage = target.getLife();//Caster va mourrir
				target.removeLife(finalDamage);
				finalDamage = -(finalDamage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", target.getGUID()+","+finalDamage);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		} else {
			isPoison = true;
			for(final Fighter target : cibles) {
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);
			}
		}
	}

	private void applyEffect_276(final Fight fight, final ArrayList<Fighter> cibles) {
		if(turns <= 1) {
			int damage = Formulas.getRandomJet(jet);
			damage = (int) ((float)(damage / 100) * caster.getLife());
			for(Fighter target : cibles)
			{
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int finalDamage = Formulas.calculFinalDamage(fight, caster, target, Constants.ELEMENT_TERRE, damage, false, false, isPoison);
				finalDamage = applyOnHitBuffs(finalDamage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDamage>target.getLife())finalDamage = target.getLife();//Caster va mourrir
				target.removeLife(finalDamage);
				finalDamage = -(finalDamage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", target.getGUID()+","+finalDamage);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		} else {
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);
			}
		}
	}

	private void applyEffect_275(final Fight fight, final ArrayList<Fighter> cibles) {
		if(turns <= 1) {
			int damage = Formulas.getRandomJet(jet);
			damage = (int) ((float)(damage / 100) * caster.getLife());
			for(Fighter target : cibles)
			{
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int finalDamage = Formulas.calculFinalDamage(fight, caster, target, Constants.ELEMENT_EAU, damage, false, false, isPoison);
				finalDamage = applyOnHitBuffs(finalDamage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDamage>target.getLife())finalDamage = target.getLife();//Caster va mourrir
				target.removeLife(finalDamage);
				finalDamage = -(finalDamage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", target.getGUID()+","+finalDamage);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		} else {
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);
			}
		}
	}

	private void applyEffect_220(final Fight fight, final ArrayList<Fighter> cibles) {
		if(turns > 1)
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);
			}
		}
	}

	private void applyEffect_186(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_180(final Fight fight, final ArrayList<Fighter> cibles) {
		//TODO DOUBLE
	}

	private void applyEffect_179(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_178(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_177(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_176(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_165(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Integer.parseInt(args.split(";")[1]);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_164(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = value;
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_157(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_156(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_154(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_153(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_152(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_144(final Fight fight, final ArrayList<Fighter> cibles) {
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);//TODO effectID = 145 ??
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_143(final Fight fight, final ArrayList<Fighter> cibles) {
		if(turns <= 0)//Si Direct
		{					
			int jet = Formulas.getRandomJet(args.split(";")[5]);
			for(Fighter target : cibles)
			{
				int finalSoin = jet;
				if(spellId != 450)
					finalSoin = Formulas.calculFinalHeal(caster, jet);
				if((finalSoin+target.getLife())> target.getMaxLife())
					finalSoin = target.getMaxLife()-target.getLife();//Target va être sur-heal
				target.heal(finalSoin);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getGUID()+"", target.getGUID()+","+finalSoin);
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_130(final ArrayList<Fighter> cibles, final Fight fight) {
		int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToErrorLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		if(fight.getType() == 1 || fight.getType() == 4)
		{
			boolean canSteal = true;
			for(Fighter target : cibles) {
				if(target.getPlayer() != null) {
					if(fight.getType() == 4) {
						canSteal = false;
						break;
					}
					if(target.getPlayer().getKamas() - val < 0)
						val = (int) target.getPlayer().getKamas();
					target.getPlayer().remKamas(val);
				}
				if(canSteal)
					caster.addKamasStolen(val);
			}
			if(canSteal)
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, String.valueOf(val), "");
		}
	}

	private void applyEffect_81(final ArrayList<Fighter> cibles, final Fight fight) {
		if(turns <= 0)//Si Direct
		{					
			final int jet = Formulas.getRandomJet(args.split(";")[5]);
			for(final Fighter target : cibles)
			{
				int finalSoin = Formulas.calculFinalHeal(caster, jet);
				if((finalSoin+target.getLife())> target.getMaxLife())
					finalSoin = target.getMaxLife()-target.getLife();//Target va être sur-heal
				target.heal(finalSoin);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getGUID()+"", target.getGUID()+","+finalSoin);
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_51(final Fight fight)
	{
		//Si case pas libre
		if(!cell.isWalkable(true) || cell.getFighters().size() >0)return;
		final Fighter target = caster.getIsHolding();
		if(target == null)return;
		if(target.hasState(6) || target.isStatic())return;

		//on ajoute le porté a sa case
		target.setFightCell(cell);
		target.getFightCell().addFighter(target);
		//on enleve les états
		target.setState(Constants.ETAT_PORTE, 0);
		caster.setState(Constants.ETAT_PORTEUR, 0);
		//on dé-lie les 2 Fighter
		target.setHoldedBy(null);
		caster.setIsHolding(null);

		//on envoie les packets
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 51, caster.getGUID()+"", cell.getId()+"");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, target.getGUID()+"", target.getGUID()+","+Constants.ETAT_PORTE+",0");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getGUID()+"", caster.getGUID()+","+Constants.ETAT_PORTEUR+",0");
	}

	private void applyEffect_950(final Fight fight, final ArrayList<Fighter> cibles)
	{
		int id = -1;
		try
		{
			id = Integer.parseInt(args.split(";")[2]);
		}catch(final Exception e){}
		if(id == -1)return;
		
		if(spellId == 1103 || (spellId >= 1107 && spellId <= 1110))
		{
			int turns = this.turns;
			if(caster.canPlay())
				turns += 1;
			caster.setState(id, turns);
			if(turns > 0)
			{
				caster.addBuff(effectID, id, turns, 1, false, spellId, args, caster, isPoison);
			}
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getGUID()+"", caster.getGUID()+","+id+",1");
		}else 
		{
			for(final Fighter target : cibles)
			{
				int turns = this.turns;
				if(target.canPlay())
					turns += 1;
				target.setState(id, turns);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getGUID()+"", target.getGUID()+","+id+",1");
				if(this.turns > 0)
					target.addBuff(effectID, id, turns, 1, debuffable, spellId, args, caster, isPoison);
			}
		}
	}

	private void applyEffect_951(final Fight fight, final ArrayList<Fighter> cibles)
	{
		int id = -1;
		try
		{
			id = Integer.parseInt(args.split(";")[2]);
		}catch(final Exception e){}
		if(id == -1)return;

		for(final Fighter target : cibles)
		{
			//Si la cible n'a pas l'état
			if(!target.hasState(id))continue;
			//on enleve l'état
			target.setState(id, 0);
			//on envoie le packet
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getGUID()+"", target.getGUID()+","+id+",0");
		}
	}

	private void applyEffect_50(final Fight fight)
	{
		//Porter
		final Fighter target = cell.getFirstFighter();
		if(target == null)return;
		if(target.hasState(6) || target.isStatic())return;
		
		//on enleve le porté de sa case
		target.getFightCell().getFighters().clear();
		//on lui définie sa nouvelle case
		target.setFightCell(caster.getFightCell());

		//on applique les états
		target.setState(Constants.ETAT_PORTE, -1);
		caster.setState(Constants.ETAT_PORTEUR, -1);
		//on lie les 2 Fighter
		target.setHoldedBy(caster);
		caster.setIsHolding(target);

		//on envoie les packets
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, target.getGUID()+"", target.getGUID()+","+Constants.ETAT_PORTE+",1");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getGUID()+"", caster.getGUID()+","+Constants.ETAT_PORTEUR+",1");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 50, caster.getGUID()+"", ""+target.getGUID());
	}

	private void applyEffect_788(ArrayList<Fighter> cibles, final Fight fight)
	{
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, value, turns, 1, false, spellId, args, target, isPoison);
		}
	}

	private void applyEffect_131(final ArrayList<Fighter> cibles, final Fight fight)
	{
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, value, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_185(final Fight fight)
	{
		final int cellID = cell.getId();
		int mobID = -1;
		int level = -1;
		MonsterGrade MG = null;
		try
		{
			mobID = Integer.parseInt(args.split(";")[0]);
			level = Integer.parseInt(args.split(";")[1]);
			MG = World.getMonstre(mobID).getGradeByLevel(level).getCopy();
		}catch(final Exception e){
			return;
		}
		if(mobID == -1 || level == -1 || MG == null)return;
		final int id = fight.getNextLowerFighterGuid();
		MG.setInFightID(id);
		MG.modifyStatByInvocator(caster, true);
		final Fighter F = new Fighter(fight,MG);
		F.setTeam(caster.getTeam());
		F.setInvocator(caster);
		fight.getMap().getCell(cellID).addFighter(F);
		F.setFightCell(fight.getMap().getCell(cellID));
		fight.addFighterInTeam(F,caster.getTeam());
		final String gm = F.getGmPacket('+').substring(3);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 185, caster.getGUID() + "", gm);

	}

	private void applyEffect_293(final Fight fight)
	{
		caster.addBuff(effectID, value, turns, 1, true, spellId, args, caster, isPoison);
	}

	private void applyEffect_672(final ArrayList<Fighter> cibles, final Fight fight)
	{
		//Punition
		//Formule de barge ? :/ Clair que ca punie ceux qui veulent l'utiliser x_x
		final float val = ((float)Formulas.getRandomJet(jet)/(float)100);
		final int maxLife = caster.getPdvMaxOutFight();
		final int midLife = maxLife / 2;
		final float pct = 1 - ((Math.abs(caster.getLife() - midLife)) / (float) midLife);
		int dgt = (int) (pct * val * maxLife);
		
		
		for(Fighter target : cibles)
		{
			//si la cible a le buff renvoie de sort
			if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl)
			{
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
				//le lanceur devient donc la cible
				target = caster;
			}
			if(target.hasBuff(765))//sacrifice
			{
				if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
				{
					applyEffect_765B(fight,target);
					target = target.getBuff(765).getCaster();
				}
			}
			int finalDommage = Formulas.calculFinalDamage(fight,caster, target, Constants.ELEMENT_NEUTRE, dgt, false, false, isPoison);
			finalDommage = applyOnHitBuffs(dgt,target,caster,fight,spellId);//S'il y a des buffs spéciaux

			if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
			target.removeLife(finalDommage);
			finalDommage = -(finalDommage);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
			if(target.getLife() <=0)
				fight.onFighterDie(target);
		}
	}

	private void applyEffect_783(final ArrayList<Fighter> cibles, final Fight fight)
	{
		//Pousse jusqu'a la case visée
		final DofusCell ccase = caster.getFightCell();
		//On calcule l'orientation entre les 2 cases
		final char d = Pathfinding.getDirBetweenTwoCase(ccase.getId(),cell.getId(), fight.getMap(), true);
		//On calcule l'id de la case a coté du lanceur dans la direction obtenue
		final int tcellID = Pathfinding.getCaseIDFromDirrection(ccase.getId(), d, fight.getMap(), true);
		//on prend la case corespondante
		final DofusCell tcase = fight.getMap().getCell(tcellID);
		if(tcase == null)return;
		//S'il n'y a personne sur la case, on arrete
		if(tcase.getFighters().size() == 0)return;
		//On prend le Fighter ciblé
		final Fighter target = tcase.getFirstFighter();
		if(target.hasState(6) || target.isStatic())return;
		//On verifie qu'il peut aller sur la case ciblé en ligne droite
		int c1 = tcellID;
		int limite = 0;
		while(true)
		{
			if(Pathfinding.getCaseIDFromDirrection(c1, d, fight.getMap(), true) == cell.getId())
				break;
			if(Pathfinding.getCaseIDFromDirrection(c1, d, fight.getMap(), true) == -1)
				return;
			c1 = Pathfinding.getCaseIDFromDirrection(c1, d, fight.getMap(), true);
			limite++;
			if(limite > 50)return;
		}

		target.getFightCell().getFighters().clear();
		target.setFightCell(cell);
		target.getFightCell().addFighter(target);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getGUID()+"", target.getGUID()+","+cell.getId());
		
		final ArrayList<Trap> P = (new ArrayList<Trap>());
		P.addAll(fight.getTraps());
		for(final Trap p : P)
		{
			final int dist = Pathfinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),target.getFightCell().getId());
			//on active le piege
			if(dist <= p.getSize())p.onTraped(target);
		}
	}

	private void applyEffect_9(final ArrayList<Fighter> cibles, final Fight fight)
	{
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, value, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_8(final ArrayList<Fighter> cibles, final Fight fight)
	{
		if(cibles.size() == 0)return;
		final Fighter target = cibles.get(0);
		if(target == null)return;//ne devrait pas arriver
		if(target.hasState(6) || target.isStatic())return;
		switch(spellId)
		{
		case 438://Transpo
			//si les 2 joueurs ne sont pas dans la meme team, on ignore
			if(target.getTeam() != caster.getTeam())return;
			break;

		case 445://Coop
			//si les 2 joueurs sont dans la meme team, on ignore
			if(target.getTeam() == caster.getTeam())return;
			break;

		case 449://Détour
		default:
			break;
		}
		//on enleve les persos des cases
		target.getFightCell().getFighters().clear();
		caster.getFightCell().getFighters().clear();
		//on retient les cases
		final DofusCell exTarget = target.getFightCell();
		final DofusCell exCaster = caster.getFightCell();
		//on échange les cases
		target.setFightCell(exCaster);
		caster.setFightCell(exTarget);
		//on ajoute les fighters aux cases
		target.getFightCell().addFighter(target);
		caster.getFightCell().addFighter(caster);
		final ArrayList<Trap> P = (new ArrayList<Trap>());
		P.addAll(fight.getTraps());
		for(final Trap p : P)
		{
			final int dist = Pathfinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),target.getFightCell().getId());
			final int dist2 = Pathfinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),caster.getFightCell().getId());
			//on active le piege
			if(dist <= p.getSize())p.onTraped(target);
			else if(dist2 <= p.getSize())p.onTraped(caster);
		}
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getGUID()+"", target.getGUID()+","+exCaster.getId());
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getGUID()+"", caster.getGUID()+","+exTarget.getId());

	}

	private void applyEffect_266(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for(final Fighter target : cibles)
		{
			target.addBuff(Constants.STATS_REM_CHAN, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_REM_CHAN, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
			vol += val;
		}
		if(vol == 0)return;
		//on ajoute le buff
		caster.addBuff(Constants.STATS_ADD_CHAN, vol, turns, 1, true, spellId, args, caster, isPoison);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_ADD_CHAN, caster.getGUID()+"", caster.getGUID()+","+vol+","+turns);
	}

	private void applyEffect_267(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for(final Fighter target : cibles)
		{
			target.addBuff(Constants.STATS_REM_VITA, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_REM_VITA, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
			vol += val;
		}
		if(vol == 0)return;
		//on ajoute le buff
		caster.addBuff(Constants.STATS_ADD_VITA, vol, turns, 1, true, spellId, args, caster, isPoison);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_ADD_VITA, caster.getGUID()+"", caster.getGUID()+","+vol+","+turns);
	}

	private void applyEffect_268(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for(final Fighter target : cibles)
		{
			target.addBuff(Constants.STATS_REM_AGIL, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_REM_AGIL, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
			vol += val;
		}
		if(vol == 0)return;
		//on ajoute le buff
		caster.addBuff(Constants.STATS_ADD_AGIL, vol, turns, 1, true, spellId, args, caster, isPoison);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_ADD_AGIL, caster.getGUID()+"", caster.getGUID()+","+vol+","+turns);
	}

	private void applyEffect_269(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for(final Fighter target : cibles)
		{
			target.addBuff(Constants.STATS_REM_INTE, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_REM_INTE, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
			vol += val;
		}
		if(vol == 0)return;
		//on ajoute le buff
		caster.addBuff(Constants.STATS_ADD_INTE, vol, turns, 1, true, spellId, args, caster, isPoison);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_ADD_INTE, caster.getGUID()+"", caster.getGUID()+","+vol+","+turns);
	}

	private void applyEffect_270(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for(final Fighter target : cibles)
		{
			target.addBuff(Constants.STATS_REM_SAGE, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_REM_SAGE, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
			vol += val;
		}
		if(vol == 0)return;
		//on ajoute le buff
		caster.addBuff(Constants.STATS_ADD_SAGE, vol, turns, 1, true, spellId, args, caster, isPoison);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_ADD_SAGE, caster.getGUID()+"", caster.getGUID()+","+vol+","+turns);
	}

	private void applyEffect_271(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		int vol = 0;
		for(final Fighter target : cibles)
		{
			target.addBuff(Constants.STATS_REM_FORC, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_REM_FORC, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
			vol += val;
		}
		if(vol == 0)return;
		//on ajoute le buff
		caster.addBuff(Constants.STATS_ADD_FORC, vol, turns, 1, true, spellId, args, caster, isPoison);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_ADD_FORC, caster.getGUID()+"", caster.getGUID()+","+vol+","+turns);
	}

	private void applyEffect_210(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}
	private void applyEffect_211(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}
	private void applyEffect_212(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}
	private void applyEffect_213(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}
	private void applyEffect_214(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}
	private void applyEffect_215(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}
	private void applyEffect_216(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}
	private void applyEffect_217(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}
	private void applyEffect_218(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}
	private void applyEffect_219(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}
	private void applyEffect_106(final ArrayList<Fighter> cibles, final Fight fight)
	{
		int val = -1;
		try
		{
			val = Integer.parseInt(args.split(";")[1]);//Niveau de sort max
		}catch(final Exception e){};
		if(val == -1)return;
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_105(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_265(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_155(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}
	private void applyEffect_163(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}
	private void applyEffect_162(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}
	private void applyEffect_161(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}
	private void applyEffect_160(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_149(final Fight fight, final ArrayList<Fighter> cibles)
	{
		int id = -1;
		try
		{
			id = Integer.parseInt(args.split(";")[2]);
		}catch(final Exception e){};
		for(final Fighter target : cibles)
		{
			if(spellId == 686)
			{
				if(target.isPlayer() && target.getPlayer().getSexe() == 1 || target.isMob() && target.getMob().getTemplate().getID() == 547)
				{
					id = 8011;
				}
			}
			if(id == -1)id = target.getDefaultGfx();
			target.addBuff(effectID, id, turns, 1, true, spellId, args, caster, isPoison);
			final int defaut = target.getDefaultGfx();
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+defaut+","+id+","+
			(target.canPlay() ? turns + 1 : turns));
		}	
	}

	private void applyEffect_182(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_184(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_183(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_145(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_171(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_142(final Fight fight, final ArrayList<Fighter> cibles)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}
	}

	private void applyEffect_150(final Fight fight, final ArrayList<Fighter> cibles)
	{
		if(turns == 0)return;
		for(final Fighter target : cibles)
		{
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 150, caster.getGUID()+"", target.getGUID()+",4");
			target.addBuff(effectID, 0, turns, 0, true,spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_401(final Fight fight)
	{
		if(!cell.isWalkable(true))return;//Si case pas marchable
		if(cell.getFirstFighter() != null)return;//Si la case est prise par un joueur

		final String[] infos = args.split(";");
		final int spellID = Short.parseShort(infos[0]);
		final int level = Byte.parseByte(infos[1]);
		final byte duration = Byte.parseByte(infos[3]);
		final String po = World.getSpell(spellId).getStatsByLevel(spellLvl).getRangeType();
		final byte size = (byte) CryptManager.getIntByHashedValue(po.charAt(1));
		final SpellStat TS = World.getSpell(spellID).getStatsByLevel(level);
		final Glyph g = new Glyph(fight,caster,cell,size,TS,duration,spellId);
		fight.getGlyphs().add(g);
		final int color = g.getColor();
		String str = "GDZ+"+cell.getId()+";"+size+";"+color;
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getGUID()+"", str);
		str = "GDC"+cell.getId()+";Haaaaaaaaa3005;";
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getGUID()+"", str);
	}

	private void applyEffect_400(final Fight fight)
	{
		if(!cell.isWalkable(true))return;//Si case pas marchable
		if(cell.getFirstFighter() != null)return;//Si la case est prise par un joueur

		//Si la case est prise par le centre d'un piege
		for(final Trap p :fight.getTraps())
			if(p.getCell().getId() == cell.getId())
				return;
		final String[] infos = args.split(";");
		final int spellID = Short.parseShort(infos[0]);
		final int level = Byte.parseByte(infos[1]);
		final String po = World.getSpell(spellId).getStatsByLevel(spellLvl).getRangeType();
		final byte size = (byte) CryptManager.getIntByHashedValue(po.charAt(1));
		final SpellStat TS = World.getSpell(spellID).getStatsByLevel(level);
		final Trap g = new Trap(fight,caster,cell,size,TS,spellId);
		fight.getTraps().add(g);
		final int unk = g.getColor();
		final int team = caster.getTeam()+1;
		String str = "GDZ+"+cell.getId()+";"+size+";"+unk;
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, team, 999, caster.getGUID()+"", str);
		str = "GDC"+cell.getId()+";Haaaaaaaaz3005;";
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, team, 999, caster.getGUID()+"", str);	
	}

	private void applyEffect_116(final ArrayList<Fighter> cibles, final Fight fight)//Malus PO
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			if(target.canPlay())
				target.getTotalStats().addOneStat(Constants.STATS_REM_PO, val);
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}		
	}

	private void applyEffect_117(final ArrayList<Fighter> cibles, final Fight fight)//Bonus PO
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			if(target.canPlay())
				target.getTotalStats().addOneStat(Constants.STATS_ADD_PO, val);
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}		
	}

	private void applyEffect_118(final ArrayList<Fighter> cibles, final Fight fight)//Bonus Force
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}		
	}

	private void applyEffect_119(final ArrayList<Fighter> cibles, final Fight fight)//Bonus Agilité
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}		
	}

	private void applyEffect_120(final ArrayList<Fighter> cibles, final Fight fight)//Bonus PA
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		
		if(spellId >= 81 && spellId <= 100)
		{
			if(caster.canPlay())
				fight.setCurFighterPA(fight.getCurFighterPA()+val);
			caster.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", caster.getGUID()+","+val+","+turns);
		}else
		{
			for(final Fighter target : cibles)
			{
				fight.setCurFighterPA(fight.getCurFighterPA()+val);
				target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
			}
		}		
	}

	private void applyEffect_78(final ArrayList<Fighter> cibles, final Fight fight)//Bonus PM
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			if(target.canPlay())
				fight.setCurFighterPM(fight.getCurFighterPM()+val);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}		
	}

	private void applyEffect_181(final Fight fight)//invocation
	{
		final int cell = this.cell.getId();
		int mobID = -1;
		int level = -1;
		try
		{
			mobID = Integer.parseInt(args.split(";")[0]);
			level = Integer.parseInt(args.split(";")[1]);
			level = World.getMonstre(mobID).getGrades().get(level).getLevel();
		}catch(final Exception e){}
		final MonsterGrade MG = World.getMonstre(mobID).getGradeByLevel(level).getCopy();
		if(mobID == -1 || level == -1 || MG == null)return;
		final int id = fight.getNextLowerFighterGuid();
		caster.incrNumbInvocations();
		MG.setInFightID(id);
		MG.modifyStatByInvocator(caster, false);
		final Fighter F = new Fighter(fight,MG);
		F.setTeam(caster.getTeam());
		F.setInvocator(caster);
		fight.getMap().getCell(cell).addFighter(F);
		F.setFightCell(fight.getMap().getCell(cell));
		fight.getListFighter().add((fight.getListFighter().indexOf(caster)+1),F);
		fight.addFighterInTeam(F,caster.getTeam());
		final String gm = F.getGmPacket('+').substring(3);
		final String gtl = fight.getGTL();
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, caster.getGUID() + "", gm);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getGUID()+"", gtl);

		final ArrayList<Trap> P = (new ArrayList<Trap>());
		P.addAll(fight.getTraps());
		for(final Trap p : P)
		{
			final int dist = Pathfinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),F.getFightCell().getId());
			//on active le piege
			if(dist <= p.getSize())p.onTraped(F);
		}
	}

	private void applyEffect_110(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_111(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			if(target.canPlay()) fight.setCurFighterPA(fight.getCurFighterPA()+val);
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_112(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_121(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_122(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_123(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_124(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_125(final ArrayList<Fighter> cibles, final Fight fight)
	{
		if(spellId == 441)
			return;
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_126(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_128(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			if(target.canPlay()) fight.setCurFighterPM(fight.getCurFighterPM()+val);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_138(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_114(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_"+effectID+")");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}			
	}

	private void applyEffect_115(final ArrayList<Fighter> cibles, final Fight fight)
	{
		final int val = Formulas.getRandomJet(jet);
		if(val == -1)
		{
			Log.addToLog("Erreur de valeur pour getRandomJet (applyEffect_115)");
			return;
		}
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, val, turns, 1, true, spellId, args, caster, isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getGUID()+"", target.getGUID()+","+val+","+turns);
		}		
	}

	private void applyEffect_77(final ArrayList<Fighter> cibles, final Fight fight)
	{
		int val = -1;
		try
		{
			val = Integer.parseInt(args.split(";")[0]);
		}catch(final NumberFormatException e){};
		int num = 0;
		for(final Fighter target : cibles)
		{
			final int retrait = Formulas.getPointsLost('m',val,caster,target);
			if(retrait < val)
			{
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 309, caster.getGUID()+"", target.getGUID()+","+(val-retrait));
			}
			if(retrait < 1)continue;
			target.addBuff(Constants.STATS_REM_PM, retrait, turns, 0, true, spellId, args, caster, isPoison);
			if(turns <= 1 || duration <= 1)
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 127,target.getGUID()+"",target.getGUID()+",-"+retrait);
			num += retrait;
		}
		if(num != 0)//Gain de PM pendant le tour de jeu s'il y a eu une perte de pm
		{
            SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constants.STATS_ADD_PM, caster.getGUID()+"", caster.getGUID()+","+num+","+turns);
			caster.addBuff(Constants.STATS_ADD_PM, num, 0, 0, true, spellId, args, caster, isPoison);
			if(caster.canPlay()) fight.setCurFighterPM(fight.getCurFighterPM()+num);
		}
	}

	private void applyEffect_84(final ArrayList<Fighter> cibles, final Fight fight)
	{
		int value = 1;
		try
		{
			value = Integer.parseInt(args.split(";")[0]);
		}catch(final NumberFormatException e){};
		int num = 0;
		for(final Fighter target : cibles)
		{
			final int val = Formulas.getPointsLost('a', value, caster, target);
			if(val < value)
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getGUID()+"", target.getGUID()+","+(value-val));
			if(val < 1)continue;
			if(spellId == 95 || spellId == 2079)
				target.addBuff(Constants.STATS_REM_PA, val, 1, 1, true, spellId, args, caster, isPoison);
			else
				target.addBuff(Constants.STATS_REM_PA, val, turns, 0, true, spellId,args,caster,isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7,Constants.STATS_REM_PA, caster.getGUID()+"", target.getGUID()+",-"+val+","+turns);
			num += val;
		}
		if(num != 0)
		{
			int duration = 1;
			if(spellId == 95 || spellId == 98)
				duration = 0;
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7,Constants.STATS_ADD_PA, caster.getGUID()+"", caster.getGUID()+","+num+","+turns);
			caster.addBuff(Constants.STATS_ADD_PA, num, duration, 0, true, spellId,args,caster,isPoison);
			if(caster.canPlay())
				fight.setCurFighterPA(fight.getCurFighterPA()+num);
		}
	}

	private void applyEffect_168(final Fight fight, final ArrayList<Fighter> cibles)
	{
		if(turns <= 0)
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, value, 1, 1, true, spellId, args, caster, isPoison);
				if(turns <= 1 || duration <= 1)
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 168,target.getGUID()+"",target.getGUID()+",-"+value);
			}
		}else
		{
			boolean odorat = false;
			for(final Fighter target : cibles)
			{
				if(spellId == 85 || spellId == 112 || spellId == 197)
					target.addBuff(effectID, value, turns, turns, true, spellId, args, caster, isPoison);
				else if(spellId == 115)
				{
					if(!odorat)
					{
						int retrait = Formulas.getRandomJet(jet);
						if(retrait == -1)
							continue;
						value = retrait;
					}
					target.addBuff(effectID, value, turns, turns, true, spellId, args, caster, isPoison);
					odorat = true;
				}
				else
					target.addBuff(effectID, value, 1, 1, true, spellId, args, caster, isPoison);
				if(turns <= 1 || duration <= 1)
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 168,target.getGUID()+"",target.getGUID()+",-"+value);
			}
		}
	}
	private void applyEffect_169(final Fight fight, final ArrayList<Fighter> cibles)
	{
		if(turns <= 0)
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, value, 1, 1, true, spellId, args, caster, isPoison);
				if(turns <= 1 || duration <= 1)
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 169,target.getGUID()+"",target.getGUID()+",-"+value);
			}
		}else
		{
			boolean odorat = false;
			for(final Fighter target : cibles)
			{
				if(spellId == 192)
					target.addBuff(effectID, value, turns, 0, true, spellId, args, caster, isPoison);
				else if(spellId == 115)
				{
					if(!odorat)
					{
						int retrait = Formulas.getRandomJet(jet);
						if(retrait == -1)
							continue;
						value = retrait;
					}
					target.addBuff(effectID, value, turns, turns, true, spellId, args, caster, isPoison);
					odorat = true;
				}
				else if(spellId == 197)
					target.addBuff(effectID, value, turns, 0, true, spellId, args, caster, isPoison);
				else
					target.addBuff(effectID, value, 1, 1, true, spellId, args, caster, isPoison);
				if(turns <= 1 || duration <= 1)
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 169,target.getGUID()+"",target.getGUID()+",-"+value);
			}
		}
	}


	private void applyEffect_101(final ArrayList<Fighter> cibles, final Fight fight)
	{
		if(turns <= 0)
		{
			for(final Fighter target : cibles)
			{
				final int retrait = Formulas.getPointsLost('a',value,caster,target);
				if((value -retrait) > 0)
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getGUID()+"", target.getGUID()+","+(value-retrait));
				if(retrait > 0)
				{
					target.addBuff(Constants.STATS_REM_PA, retrait, 1, 1, true, spellId, args, caster, isPoison);
					if(turns <= 1 || duration <= 1)
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 101,target.getGUID()+"",target.getGUID()+",-"+retrait);
				}
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				final int retrait = Formulas.getPointsLost('a',value,caster,target);
				if((value -retrait) > 0)
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getGUID()+"", target.getGUID()+","+(value-retrait));
				if(retrait > 0)
				{
					if(spellId == 89)
						target.addBuff(effectID, retrait, 0, 1, true, spellId, args, caster, isPoison);
					else
						target.addBuff(effectID, retrait, 1, 1, true, spellId, args, caster, isPoison);
					if(turns <= 1 || duration <= 1)
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 101,target.getGUID()+"",target.getGUID()+",-"+retrait);
				}
			}
		}
	}

	private void applyEffect_127(final ArrayList<Fighter> cibles, final Fight fight)
	{
		if(turns <= 0)
		{
			for(final Fighter target : cibles)
			{
				final int retrait = Formulas.getPointsLost('m',value,caster,target);
				if((value-retrait) > 0)
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 309, caster.getGUID()+"", target.getGUID()+","+(value-retrait));
				if(retrait > 0)
				{
					target.addBuff(Constants.STATS_REM_PM, retrait, 1, 1, true, spellId, args, caster, isPoison);
					if(turns <= 1 || duration <= 1)
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 127,target.getGUID()+"",target.getGUID()+",-"+retrait);
					if(target.canPlay())
						fight.setCurFighterPM(fight.getCurFighterPM()-retrait);
				}
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				if(spellId == 69 || spellId == 136)
				{
					target.addBuff(effectID, value, turns, turns, true, spellId, args, caster, isPoison);//on applique un buff
				}
				else
				{
					final int retrait = Formulas.getPointsLost('m',value,caster,target);
					if((value-retrait) > 0)
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 309, caster.getGUID()+"", target.getGUID()+","+(value-retrait));
					if(retrait > 0)
					{
						target.addBuff(effectID, retrait, 1, 1, true, spellId, args, caster, isPoison);
						if(turns <= 1 || duration <= 1)
							SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 127,target.getGUID()+"",target.getGUID()+",-"+retrait);
					}
				}
			}
		}
	}

	private void applyEffect_107(final ArrayList<Fighter> cibles, final Fight fight)
	{
		if(turns<1)return;//Je vois pas comment, vraiment ...
		else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}


	private void applyEffect_79(final ArrayList<Fighter> cibles, final Fight fight)
	{
		if(turns<1)return;//Je vois pas comment, vraiment ...
		else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, -1, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}


	private void applyEffect_4(final Fight fight,final ArrayList<Fighter> cibles)
	{
		if(turns >1)return;//Olol bondir 3 tours apres ?

		if(cell.isWalkable(true) && !fight.isOccuped(cell.getId()))//Si la case est prise, on va éviter que les joueurs se montent dessus *-*
		{
			caster.getFightCell().getFighters().clear();
			caster.setFightCell(cell);
			caster.getFightCell().addFighter(caster);

			final ArrayList<Trap> P = (new ArrayList<Trap>());
			P.addAll(fight.getTraps());
			for(final Trap p : P)
			{
				final int dist = Pathfinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),caster.getFightCell().getId());
				//on active le piege
				if(dist <= p.getSize())p.onTraped(caster);
			}
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getGUID()+"", caster.getGUID()+","+cell.getId());
		}else
		{
			Log.addToLog("Tentative de teleportation echouee : case non libre:");
			Log.addToLog("IsOccuped: "+fight.isOccuped(cell.getId()));
			Log.addToLog("Walkable: "+cell.isWalkable(true));
		}
	}

	private void applyEffect_109(final Fight fight)//Dommage pour le lanceur (fixes)
	{
		if(turns <= 0)
		{
			final int dmg = Formulas.getRandomJet(args.split(";")[5]);
			int finalDommage = Formulas.calculFinalDamage(fight,caster, caster,Constants.ELEMENT_NULL, dmg, false, false, isPoison);

			finalDommage = applyOnHitBuffs(finalDommage,caster,caster,fight, spellId);//S'il y a des buffs spéciaux
			if(finalDommage>caster.getLife())finalDommage = caster.getLife();//Caster va mourrir
			caster.removeLife(finalDommage);
			finalDommage = -(finalDommage);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", caster.getGUID()+","+finalDommage);

			if(caster.getLife() <=0)
				fight.onFighterDie(caster);
		}else
		{
			caster.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
		}
	}

	private void applyEffect_82(final ArrayList<Fighter> cibles,final Fight fight)
	{
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort et que le sort peut etre renvoyer
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int resP = target.getTotalStats().getEffect(Constants.STATS_ADD_RP_NEU);
				int resF = target.getTotalStats().getEffect(Constants.STATS_ADD_R_NEU);
				if(target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constants.STATS_ADD_RP_PVP_NEU);
					resF += target.getTotalStats().getEffect(Constants.STATS_ADD_R_PVP_NEU);
				}
				int finalDommage = value;
				//retrait de la résist fixe
				finalDommage -= resF;
				final int reduc =	(int)(((float)finalDommage)/(float)100)*resP;//Reduc %resis
				finalDommage -= reduc;
				if(finalDommage <0)finalDommage = 0;
				finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
				target.removeLife(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
				//Vol de vie
				int heal = (int)(-finalDommage)/2;
				if((caster.getLife()+heal) > caster.getMaxLife())
					heal = caster.getMaxLife()-caster.getLife();
				caster.removeLife(-heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", caster.getGUID()+","+heal);

				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_6(final ArrayList<Fighter> cibles,final Fight fight)
	{
		if(turns <= 0)
		{
			for(final Fighter target : cibles)
			{
				if(target.hasState(6) || target.isStatic())
					continue;
				DofusCell eCell = cell;
				//Si meme case
				if(target.getFightCell().getId() == cell.getId())
				{
					//on prend la cellule caster
					eCell = caster.getFightCell();
				}
				int newCellID =	Pathfinding.newCaseAfterPush(fight.getMap(),eCell,target.getFightCell(),-value,fight);
				if(newCellID == 0)
					return;

				System.out.println("CellID : "+newCellID);

				if(newCellID <0)//S'il a été bloqué
				{
					final int a = -(value + newCellID);
					newCellID =	Pathfinding.newCaseAfterPush(fight.getMap(),caster.getFightCell(),target.getFightCell(),a,fight);
					if(newCellID == 0)
						return;
					if(fight.getMap().getCell(newCellID) == null)
						return;
				}

				target.getFightCell().getFighters().clear();
				target.setFightCell(fight.getMap().getCell(newCellID));
				target.getFightCell().addFighter(target);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getGUID()+"", target.getGUID()+","+newCellID);

				final ArrayList<Trap> P = (new ArrayList<Trap>());
				P.addAll(fight.getTraps());
				for(final Trap p : P)
				{
					final int dist = Pathfinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),target.getFightCell().getId());
					//on active le piege
					if(dist <= p.getSize())p.onTraped(target);
				}
			}
		}
	}

	private void applyEffect_5(final ArrayList<Fighter> cibles,final Fight fight)
	{
		if(turns <= 0)
		{
			for(final Fighter target : cibles)
			{
				if(target.hasState(6) || target.isStatic())
					continue;
				DofusCell eCell = cell;
				//Si meme case
				if(target.getFightCell().getId() == cell.getId())
				{
					//on prend la cellule caster
					eCell = caster.getFightCell();
				}
				int newCellID =	Pathfinding.newCaseAfterPush(fight.getMap(),eCell,target.getFightCell(),value,fight);
				if(newCellID == 0)
					return;
				if(newCellID <0)//S'il a été bloqué
				{
					int a = -newCellID;
					/*final int coef = Formulas.getRandomJet("1d5+8");
					double b = (caster.getLvl()/(double)(50.00));
					if(b<0.1)b= 0.1;
					final double c = b*a;//Calcule des dégats de poussé
					int finalDommage = (int)(coef * c);*/
					int coef = Formulas.getRandomJet("1d8+0");
					double b = (caster.getLvl()/(double)(50.00));
					if(b<0.1)b= 0.1;
					int finalDommage = (int) ((8 + coef * b) * value);
					if(finalDommage < 1)finalDommage = 1;
					if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
					target.removeLife(finalDommage);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+",-"+finalDommage);
					if(target.getLife() <=0)
					{
						fight.onFighterDie(target);
						return;
					}
					a = value-a;
					newCellID =	Pathfinding.newCaseAfterPush(fight.getMap(),caster.getFightCell(),target.getFightCell(),a,fight);
					if(newCellID == 0)
						return;
					if(fight.getMap().getCell(newCellID) == null)
						return;
				}
				target.getFightCell().getFighters().clear();
				target.setFightCell(fight.getMap().getCell(newCellID));
				target.getFightCell().addFighter(target);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getGUID()+"", target.getGUID()+","+newCellID);

				final ArrayList<Trap> P = (new ArrayList<Trap>());
				P.addAll(fight.getTraps());
				for(final Trap p : P)
				{
					final int dist = Pathfinding.getDistanceBetween(fight.getMap(),p.getCell().getId(),target.getFightCell().getId());
					//on active le piege
					if(dist <= p.getSize())p.onTraped(target);
				}
			}
		}
	}

	private void applyEffect_91(final ArrayList<Fighter> cibles,final Fight fight,final boolean isCac)
	{
		if(turns <= 0)
		{
			if(caster.isHide())caster.unHide(spellId);
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}

				final int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDamage(fight,caster, target,Constants.ELEMENT_EAU, dmg, false, isCac, isPoison);

				finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
				target.removeLife(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
				int heal = (int)(-finalDommage)/2;
				if((caster.getLife()+heal) > caster.getMaxLife())
					heal = caster.getMaxLife()-caster.getLife();
				caster.removeLife(-heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", caster.getGUID()+","+heal);

				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_92(final ArrayList<Fighter> cibles,final Fight fight,final boolean isCac)
	{
		if(caster.isHide())caster.unHide(spellId);
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				final int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDamage(fight,caster, target,Constants.ELEMENT_TERRE, dmg,false, isCac, isPoison);

				finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
				target.removeLife(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);

				int heal = (int)(-finalDommage)/2;
				if((caster.getLife()+heal) > caster.getMaxLife())
					heal = caster.getMaxLife()-caster.getLife();
				caster.removeLife(-heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", caster.getGUID()+","+heal);

				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_93(final ArrayList<Fighter> cibles,final Fight fight,final boolean isCac)
	{
		if(caster.isHide())caster.unHide(spellId);
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				final int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDamage(fight,caster, target,Constants.ELEMENT_AIR, dmg,false, isCac, isPoison);

				finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
				target.removeLife(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);

				int heal = (int)(-finalDommage)/2;
				if((caster.getLife()+heal) > caster.getMaxLife())
					heal = caster.getMaxLife()-caster.getLife();
				caster.removeLife(-heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", caster.getGUID()+","+heal);

				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_94(final ArrayList<Fighter> cibles,final Fight fight,final boolean isCac)
	{
		if(caster.isHide())caster.unHide(spellId);
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				final int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDamage(fight,caster, target,Constants.ELEMENT_FEU, dmg,false, isCac, isPoison);

				finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
				target.removeLife(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
				int heal = (int)(-finalDommage)/2;
				if((caster.getLife()+heal) > caster.getMaxLife())
					heal = caster.getMaxLife()-caster.getLife();
				caster.removeLife(-heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", caster.getGUID()+","+heal);

				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_95(final ArrayList<Fighter> cibles,final Fight fight,final boolean isCac)
	{
		if(caster.isHide())caster.unHide(spellId);
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				final int dmg = Formulas.getRandomJet(args.split(";")[5]);
				int finalDommage = Formulas.calculFinalDamage(fight,caster, target,Constants.ELEMENT_NEUTRE, dmg,false, isCac, isPoison);

				finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
				target.removeLife(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);

				int heal = (int)(-finalDommage)/2;
				if((caster.getLife()+heal) > caster.getMaxLife())
					heal = caster.getMaxLife()-caster.getLife();
				caster.removeLife(-heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getGUID()+"", caster.getGUID()+","+heal);

				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_85(final ArrayList<Fighter> cibles,final Fight fight)
	{
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int resP = target.getTotalStats().getEffect(Constants.STATS_ADD_RP_EAU);
				int resF = target.getTotalStats().getEffect(Constants.STATS_ADD_R_EAU);
				if(target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constants.STATS_ADD_RP_PVP_EAU);
					resF += target.getTotalStats().getEffect(Constants.STATS_ADD_R_PVP_EAU);
				}
				final int dmg = Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligé
				int val = caster.getLife()/100*dmg;//Valeur des dégats
				//retrait de la résist fixe
				val -= resF;
				final int reduc =	(int)(((float)val)/(float)100)*resP;//Reduc %resis
				val -= reduc;
				if(val <0)val = 0;

				val = applyOnHitBuffs(val,target,caster,fight, spellId);//S'il y a des buffs spéciaux

				if(val>target.getLife())val = target.getLife();//Target va mourrir
				target.removeLife(val);
				val = -(val);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+val);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_86(final ArrayList<Fighter> cibles,final Fight fight)
	{
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int resP = target.getTotalStats().getEffect(Constants.STATS_ADD_RP_TER);
				int resF = target.getTotalStats().getEffect(Constants.STATS_ADD_R_TER);
				if(target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constants.STATS_ADD_RP_PVP_TER);
					resF += target.getTotalStats().getEffect(Constants.STATS_ADD_R_PVP_TER);
				}
				final int dmg = Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligé
				int val = caster.getLife()/100*dmg;//Valeur des dégats
				//retrait de la résist fixe
				val -= resF;
				final int reduc =	(int)(((float)val)/(float)100)*resP;//Reduc %resis
				val -= reduc;
				if(val <0)val = 0;

				val = applyOnHitBuffs(val,target,caster,fight, spellId);//S'il y a des buffs spéciaux

				if(val>target.getLife())val = target.getLife();//Target va mourrir
				target.removeLife(val);
				val = -(val);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+val);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_87(final ArrayList<Fighter> cibles,final Fight fight)
	{
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int resP = target.getTotalStats().getEffect(Constants.STATS_ADD_RP_AIR);
				int resF = target.getTotalStats().getEffect(Constants.STATS_ADD_R_AIR);
				if(target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constants.STATS_ADD_RP_PVP_AIR);
					resF += target.getTotalStats().getEffect(Constants.STATS_ADD_R_PVP_AIR);
				}
				final int dmg = Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligé
				int val = caster.getLife()/100*dmg;//Valeur des dégats
				//retrait de la résist fixe
				val -= resF;
				final int reduc =	(int)(((float)val)/(float)100)*resP;//Reduc %resis
				val -= reduc;
				if(val <0)val = 0;

				val = applyOnHitBuffs(val,target,caster,fight, spellId);//S'il y a des buffs spéciaux

				if(val>target.getLife())val = target.getLife();//Target va mourrir
				target.removeLife(val);
				val = -(val);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+val);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_88(final ArrayList<Fighter> cibles,final Fight fight)
	{
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int resP = target.getTotalStats().getEffect(Constants.STATS_ADD_RP_FEU);
				int resF = target.getTotalStats().getEffect(Constants.STATS_ADD_R_FEU);
				if(target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constants.STATS_ADD_RP_PVP_FEU);
					resF += target.getTotalStats().getEffect(Constants.STATS_ADD_R_PVP_FEU);
				}
				final int dmg = Formulas.getRandomJet(args.split(";")[5]);//%age de pdv infligé
				int val = caster.getLife()/100*dmg;//Valeur des dégats
				//retrait de la résist fixe
				val -= resF;
				final int reduc =	(int)(((float)val)/(float)100)*resP;//Reduc %resis
				val -= reduc;
				if(val <0)val = 0;

				val = applyOnHitBuffs(val,target,caster,fight, spellId);//S'il y a des buffs spéciaux

				if(val>target.getLife())val = target.getLife();//Target va mourrir
				target.removeLife(val);
				val = -(val);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+val);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_89(final ArrayList<Fighter> cibles,final Fight fight)
	{
		if(spellId == 1679)
		{
			char[] dirs = { 'b', 'd', 'f', 'h' };
			Fighter victime = cibles.get(0);
			cibles.clear();
			for(char dir : dirs) 
			{
				int cellId = Pathfinding.getCaseIDFromDirrection(victime.getFightCell().getId(), dir, fight.getMap(), true);
				DofusCell cell = fight.getMap().getCell(cellId);
				if(cell == null)
					continue;
				Fighter neighbour = cell.getFirstFighter();
				if(neighbour != null)
					cibles.add(neighbour);
			}
		}
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int dgt = Formulas.getRandomJet(jet);
				dgt = (dgt / 100) * caster.getLife();
				int finalDamage = Formulas.calculFinalDamage(fight, caster, target, Constants.ELEMENT_NEUTRE, dgt, false, false, isPoison);
				if(finalDamage < 0)
					finalDamage = 0;
				finalDamage = applyOnHitBuffs(finalDamage,target,caster,fight, spellId);//S'il y a des buffs spéciaux
				if(finalDamage > target.getLife())
					finalDamage = target.getLife();
				target.removeLife(finalDamage);
				finalDamage = -(finalDamage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDamage);
				if(target.getLife() <= 0)
					fight.onFighterDie(target);
			}
		}else
		{
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_96(final ArrayList<Fighter> cibles,final Fight fight,final boolean isCac)
	{
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boosté par un buff spécifique
				for(final SpellEffect SE : caster.getBuffsByEffectID(293))
				{
					if(SE.getValue() == spellId)
					{
						int add = -1;
						try
						{
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						}catch(final Exception e){};
						if(add <= 0)continue;
						dmg += add;
					}
				}

				int finalDommage = Formulas.calculFinalDamage(fight,caster, target,Constants.ELEMENT_EAU, dmg,false, isCac, isPoison);

				finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight, spellId);//S'il y a des buffs spéciaux

				if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
				target.removeLife(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_97(final ArrayList<Fighter> cibles,final Fight fight,final boolean isCac)
	{
		if(turns <= 0)
		{
			if(caster.isHide())caster.unHide(spellId);
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boosté par un buff spécifique
				for(final SpellEffect SE : caster.getBuffsByEffectID(293))
				{
					if(SE.getValue() == spellId)
					{
						int add = -1;
						try
						{
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						}catch(final Exception e){};
						if(add <= 0)continue;
						dmg += add;
					}
				}
				if(spellId==160 && target==caster)
				{
					continue;//Epée de Iop ne tape pas le lanceur.
				}else if(chance > 0 && spellId==108)//Esprit félin ?
				{
					int fDommage = Formulas.calculFinalDamage(fight,caster, caster,Constants.ELEMENT_TERRE, dmg, false, isCac, isPoison);
					fDommage = applyOnHitBuffs(fDommage,caster,caster,fight,spellId);//S'il y a des buffs spéciaux
					if(fDommage>target.getLife())fDommage = target.getLife();//Target va mourrir

					target.removeLife(fDommage);
					fDommage = -(fDommage);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+fDommage);
					if(target.getLife() <=0)
						fight.onFighterDie(target);
				}else
				{
					int finalDommage = Formulas.calculFinalDamage(fight,caster, target,Constants.ELEMENT_TERRE, dmg, false, false, isPoison);
					finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight,spellId);//S'il y a des buffs spéciaux
					if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
					target.removeLife(finalDommage);
					finalDommage = -(finalDommage);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
					if(target.getLife() <=0)
						fight.onFighterDie(target);
				}
			}
		}else
		{
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_98(final ArrayList<Fighter> cibles,final Fight fight,final boolean isCac)
	{
		if(turns <= 0)
		{
			if(caster.isHide())caster.unHide(spellId);
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boosté par un buff spécifique
				for(final SpellEffect SE : caster.getBuffsByEffectID(293))
				{
					if(SE.getValue() == spellId)
					{
						int add = -1;
						try
						{
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						}catch(final Exception e){};
						if(add <= 0)continue;
						dmg += add;
					}
				}

				int finalDommage = Formulas.calculFinalDamage(fight,caster, target,Constants.ELEMENT_AIR, dmg,false, isCac, isPoison);

				finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight, spellId);//S'il y a des buffs spéciaux

				if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
				target.removeLife(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_99(final ArrayList<Fighter> cibles,final Fight fight,final boolean isCac)
	{
		if(caster.isHide())caster.unHide(spellId);
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{
				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boosté par un buff spécifique
				for(final SpellEffect SE : caster.getBuffsByEffectID(293))
				{
					if(SE.getValue() == spellId)
					{
						int add = -1;
						try
						{
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						}catch(final Exception e){};
						if(add <= 0)continue;
						dmg += add;
					}
				}

				int finalDommage = Formulas.calculFinalDamage(fight,caster, target,Constants.ELEMENT_FEU, dmg,false, isCac, isPoison);

				finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight, spellId);//S'il y a des buffs spéciaux

				if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
				target.removeLife(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_100(final ArrayList<Fighter> cibles,final Fight fight,final boolean isCac)
	{
		if(caster.isHide())caster.unHide(spellId);
		if(turns <= 0)
		{
			for(Fighter target : cibles)
			{

				//si la cible a le buff renvoie de sort
				if(target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spellId != 0)
				{
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getGUID()+"", target.getGUID()+",1");
					//le lanceur devient donc la cible
					target = caster;
				}
				if(target.hasBuff(765))//sacrifice
				{
					if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
					{
						applyEffect_765B(fight,target);
						target = target.getBuff(765).getCaster();
					}
				}
				int dmg = Formulas.getRandomJet(args.split(";")[5]);

				//Si le sort est boosté par un buff spécifique
				for(final SpellEffect SE : caster.getBuffsByEffectID(293))
				{
					if(SE.getValue() == spellId)
					{
						int add = -1;
						try
						{
							add = Integer.parseInt(SE.getArgs().split(";")[2]);
						}catch(final Exception e){};
						if(add <= 0)continue;
						dmg += add;
					}
				}


				int finalDommage = Formulas.calculFinalDamage(fight,caster, target,Constants.ELEMENT_NEUTRE, dmg,false, isCac, isPoison);

				finalDommage = applyOnHitBuffs(finalDommage,target,caster,fight, spellId);//S'il y a des buffs spéciaux

				if(finalDommage>target.getLife())finalDommage = target.getLife();//Target va mourrir
				target.removeLife(finalDommage);
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+","+finalDommage);
				if(target.getLife() <=0)
					fight.onFighterDie(target);
			}
		}else
		{
			isPoison = true;
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_132(final ArrayList<Fighter> cibles,final Fight fight)
	{
		for(final Fighter target : cibles)
		{
			target.debuff();
			if (target.canPlay() && target == caster) 
			{
				Stats statsWithBuff = target.getTotalStats();
				Stats statsLessBuff = target.getTotalStatsLessBuff();
				for(int i = 0; i < 1000; i++)
				{
					if(statsWithBuff.getMap().get(i) == null || statsLessBuff.getMap().get(i) == null)
						continue;
					int newValue = statsLessBuff.getMap().get(i);
					statsWithBuff.setStat2(i, newValue);
				}
			}
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 132, caster.getGUID()+"", target.getGUID()+"");
		}
	}

	private void applyEffect_140(final ArrayList<Fighter> cibles,final Fight fight)
	{
		for(final Fighter target : cibles)
		{
			target.addBuff(effectID, 0, 1, 0, true,spellId, args, caster, isPoison);
		}
	}

	private void applyEffect_90(final ArrayList<Fighter> cibles, final Fight fight)
	{
		if(turns <= 0)//Si Direct
		{
			final int pAge = Formulas.getRandomJet(jet);
			int val = (int)((pAge / 100)) * caster.getLife();
			if(val>caster.getLife())val = caster.getLife();//Caster va mourrir
			caster.removeLife(val);
			val = -(val);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", caster.getGUID()+","+val);

			//Application du soin
			for(final Fighter target : cibles)
			{
				if((val+target.getLife())> target.getMaxLife())val = target.getMaxLife()-target.getLife();//Target va mourrir
				target.heal(val);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+",+"+val);
			}
			if(caster.getLife() <=0)
				fight.onFighterDie(caster);
		}else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_108(final ArrayList<Fighter> cibles, final Fight fight,final boolean isCac)
	{
		if(spellId == 441)
			return;
		if(turns <= 0)//Si Direct
		{					
			final int dmg = Formulas.getRandomJet(args.split(";")[5]);
			for(final Fighter target : cibles)
			{
				int finalSoin = Formulas.calculFinalHeal(caster, dmg);
				if((finalSoin+target.getLife())> target.getMaxLife())
					finalSoin = target.getMaxLife()-target.getLife();//Target va mourrir
				target.heal(finalSoin);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", target.getGUID()+",+"+finalSoin);
			}
		}else
		{
			for(final Fighter target : cibles)
			{
				target.addBuff(effectID, 0, turns, 0, true, spellId, args, caster, isPoison);//on applique un buff
			}
		}
	}

	private void applyEffect_141(final Fight fight,final ArrayList<Fighter> cibles)
	{
		for(Fighter target : cibles)
		{
			if(target.hasBuff(765))//sacrifice
			{
				if(target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead())
				{
					applyEffect_765B(fight,target);
					target = target.getBuff(765).getCaster();
				}
			}
			fight.onFighterDie(target);
			try {
				Thread.sleep(500); // Pas trop vite !!
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void applyEffect_320(final Fight fight, final ArrayList<Fighter> cibles)
	{
		int value = 1;
		try
		{
			value = Integer.parseInt(args.split(";")[0]);
		}catch(final NumberFormatException e){};
		int num = 0;
		for(final Fighter target : cibles)
		{
			target.addBuff(Constants.STATS_REM_PO, value, turns,0, true, spellId,args,caster,isPoison);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7,Constants.STATS_REM_PO, caster.getGUID()+"", target.getGUID()+","+value+","+turns);
			num += value;
		}
		if(num != 0)
		{
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7,Constants.STATS_ADD_PO, caster.getGUID()+"", caster.getGUID()+","+num+","+turns);
			caster.addBuff(Constants.STATS_ADD_PO, num, 1, 0, true, spellId,args,caster,isPoison);
			if(caster.canPlay())
				caster.getTotalStats().addOneStat(Constants.STATS_ADD_PO, num);
		}
	}

	public void setArgs(final String newArgs)
	{
		args = newArgs;
	}
	public void setEffectID(final int id)
	{
		effectID = id;
	}
}
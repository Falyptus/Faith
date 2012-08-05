package objects.spell;

import java.util.ArrayList;

import objects.fight.Fight;
import objects.fight.Fighter;
import objects.map.DofusCell;

import common.Constants;
import common.World;
import common.console.Log;
import common.utils.Formulas;
import common.utils.Pathfinding;

public class SpellStat
{
	private final int spellID;
	private final int level;
	private final int PACost;
	private final int minPO;
	private final int maxPO;
	private final int TauxCC;
	private final int TauxEC;
	private final boolean isLineLaunch;
	private final boolean hasLDV;
	private final boolean isEmptyCell;
	private final boolean isModifPO;
	private final int maxLaunchbyTurn;
	private final int maxLaunchbyByTarget;
	private final int coolDown;
	private final int reqLevel;
	private final boolean isEcEndTurn;
	private final ArrayList<SpellEffect> effects;
	private final ArrayList<SpellEffect> CCeffects;
	private final String porteeType;
	private final ArrayList<Integer> statesNeeded;
	private final ArrayList<Integer> statesProhibited;
	
	public SpellStat(final int AspellID,final int Alevel,final int cost, final int minPO, final int maxPO, final int tauxCC,final int tauxEC, final boolean isLineLaunch, final boolean hasLDV,
			final boolean isEmptyCell, final boolean isModifPO, final int maxLaunchbyTurn,final int maxLaunchbyByTarget, final int coolDown,
			final int reqLevel,final boolean isEcEndTurn, final String effects,final String ceffects,final String typePortee, String statesNeeded, String statesProhibited)
	{
		this.spellID = AspellID;
		this.level = Alevel;
		this.PACost = cost;
		this.minPO = minPO;
		this.maxPO = maxPO;
		this.TauxCC = tauxCC;
		this.TauxEC = tauxEC;
		this.isLineLaunch = isLineLaunch;
		this.hasLDV = hasLDV;
		this.isEmptyCell = isEmptyCell;
		this.isModifPO = isModifPO;
		this.maxLaunchbyTurn = maxLaunchbyTurn;
		this.maxLaunchbyByTarget = maxLaunchbyByTarget;
		this.coolDown = coolDown;
		this.reqLevel = reqLevel;
		this.isEcEndTurn = isEcEndTurn;
		this.effects = parseEffect(effects);
		this.CCeffects = parseEffect(ceffects);
		this.porteeType = typePortee;
		this.statesNeeded = parseStatesNeeded(statesNeeded);
		this.statesProhibited = parseStatesProhibited(statesProhibited);
	}

	private ArrayList<SpellEffect> parseEffect(final String e)
	{
		final ArrayList<SpellEffect> effets = new ArrayList<SpellEffect>();
		final String[] splt = e.split("\\|");
		for(final String a : splt)
		{
			try
			{
				if(e.equals("-1"))continue;
				final int id = Integer.parseInt(a.split(";",2)[0]);
				final String args = a.split(";",2)[1];
				effets.add(new SpellEffect(id, args,spellID,level));
			}catch(final Exception f){f.printStackTrace();System.out.println(a);System.exit(1);};
		}
		return effets;
	}

	private ArrayList<Integer> parseStatesNeeded(String e) {
		ArrayList<Integer> statesNeeded = new ArrayList<Integer>();
		for(String a : e.split(";"))
		{
			try
			{
				a = a.trim();
				if(e.equals("-1"))continue;
				final int stateId = Integer.parseInt(a);
				statesNeeded.add(stateId);
			}catch(final NumberFormatException f){continue;};
		}
		return statesNeeded;
	}
	
	private ArrayList<Integer> parseStatesProhibited(String e) {
		ArrayList<Integer> statesProhibited = new ArrayList<Integer>();
		for(String a : e.split(";"))
		{
			try
			{
				a = a.trim();
				if(e.equals("-1"))continue;
				final int stateId = Integer.parseInt(a);
				statesProhibited.add(stateId);
			}catch(final NumberFormatException f){continue;};
		}
		return statesProhibited;
	}

	public int getSpellID() {
		return spellID;
	}
	
	public Spell getSpell()
	{
		return World.getSpell(spellID);
	}
	public int getSpriteID()
	{
		return getSpell().getSpriteID();
	}
	
	public String getSpriteInfos()
	{
		return getSpell().getSpriteInfos();
	}
	
	public int getLevel() {
		return level;
	}

	public int getPACost() {
		return PACost;
	}

	public int getMinPO() {
		return minPO;
	}

	public int getMaxPO() {
		return maxPO;
	}

	public int getTauxCC() {
		return TauxCC;
	}

	public int getTauxEC() {
		return TauxEC;
	}

	public boolean isLineLaunch() {
		return isLineLaunch;
	}

	public boolean hasLDV() {
		return hasLDV;
	}

	public boolean isEmptyCell() {
		return isEmptyCell;
	}

	public boolean isModifPO() {
		return isModifPO;
	}

	public int getMaxLaunchbyTurn() {
		return maxLaunchbyTurn;
	}

	public int getMaxLaunchbyByTarget() {
		return maxLaunchbyByTarget;
	}

	public int getCoolDown() {
		return coolDown;
	}

	public int getReqLevel() {
		return reqLevel;
	}

	public boolean isEcEndTurn() {
		return isEcEndTurn;
	}

	public ArrayList<SpellEffect> getEffects() {
		return effects;
	}

	public ArrayList<SpellEffect> getCCeffects() {
		return CCeffects;
	}

	public String getRangeType() {
		return porteeType;
	}

	public ArrayList<Integer> getStatesNeeded() {
		return statesNeeded;
	}

	public ArrayList<Integer> getStatesProhibited() {
		return statesProhibited;
	}
	
	public void applySpellEffectToFight(final Fight fight, final Fighter perso,final DofusCell cell,final ArrayList<DofusCell> cells,final boolean isCC)
	{
		//Seulement appellé par les pieges, or les sorts de piege 
		ArrayList<SpellEffect> effets;
		if(isCC)
			effets = CCeffects;
		else
			effets = effects;
		Log.addToLog("Nombre d'effets: "+effets.size());
		final int jetChance = Formulas.getRandomValue(0, 99);
		int curMin = 0;
		for(final SpellEffect SE : effets)
		{
			if(SE.getChance() != 0 && SE.getChance() != 100)//Si pas 100% lancement
			{
				if(jetChance <= curMin || jetChance >= (SE.getChance() + curMin))
				{
					curMin += SE.getChance();
					continue;
				}
				curMin += SE.getChance();
			}
			
			final ArrayList<Fighter> cibles = SpellEffect.getTargets(SE,fight,cells);
			SE.applyToFight(fight, perso, cell,cibles,cells);
		}
	}
	
	public void applySpellEffectToFight(final Fight fight, final Fighter perso,final DofusCell cell,final boolean isCC)
	{
		ArrayList<SpellEffect> effets;
		
		if(isCC)
			effets = CCeffects;
		else
			effets = effects;
		Log.addToLog("Nombre d'effets: "+effets.size());
		final int jetChance = Formulas.getRandomValue(0, 99);
		int curMin = 0;
		int num = 0;
		for(final SpellEffect SE : effets)
		{
			if(fight.getState()>=Constants.FIGHT_STATE_FINISHED)return;
			if(SE.getChance() != 0 && SE.getChance() != 100)//Si pas 100% lancement
			{
				if(jetChance <= curMin || jetChance >= (SE.getChance() + curMin))
				{
					curMin += SE.getChance();
					num++;
					continue;
				}
				curMin += SE.getChance();
			}
			
			int POnum = num*2;
			if(isCC)
			{
				POnum += effects.size()*2;//On zaap la partie du String des effets hors CC
			} 
			final ArrayList<DofusCell> cells = Pathfinding.getCellListFromAreaString(fight.getMap(),cell.getId(),perso.getFightCell().getId(),porteeType,POnum,isCC);
			
			final ArrayList<DofusCell> finalCells = new ArrayList<DofusCell>();
			
			int TE = 0;
			final Spell S = World.getSpell(spellID);
			//on prend le targetFlag corespondant au num de l'effet
			if(S!= null?S.getEffectTargets().size()>num:false)TE = S.getEffectTargets().get(num);
			
			//* DEBUG
			System.out.println("TargetFlag : "+Integer.toBinaryString(TE)+"("+TE+")");
			//*/
			
			for(final DofusCell C : cells)
			{
				if(C == null)continue;
				final Fighter F = C.getFirstFighter();
				if(F == null)continue;
				//Ne touches pas les alliés
				if(((TE & 1) == 1) && (F.getTeam() == perso.getTeam()))continue;
				//Ne touche pas le lanceur
				if((((TE>>1) & 1) == 1) && (F.getGUID() == perso.getGUID()))continue;
				//Ne touche pas les ennemies
				if((((TE>>2) & 1) == 1) && (F.getTeam() != perso.getTeam()))continue;
				//Ne touche pas les combatants (seulement invocations)
				if((((TE>>3) & 1) == 1) && (!F.isInvocation()))continue;
				//Ne touche pas les invocations
				if((((TE>>4) & 1) == 1) && (F.isInvocation()))continue;
				//N'affecte que le lanceur
				if((((TE>>5) & 1) == 1) && (F.getGUID() != perso.getGUID()))continue;
				//Si pas encore eu de continue, on ajoute la case
				finalCells.add(C);
			}
			//Si le sort n'affecte que le lanceur et que le lanceur n'est pas dans la zone
			if(((TE>>5) & 1) == 1)
				if(!finalCells.contains(perso.getFightCell()))
					finalCells.add(perso.getFightCell());
			if(((TE>>6) & 1) == 1)
				if(perso.getInvocator() != null && !finalCells.contains(perso.getInvocator()))
					finalCells.add(perso.getInvocator().getFightCell());
			final ArrayList<Fighter> cibles = SpellEffect.getTargets(SE,fight,finalCells);
			SE.applyToFight(fight, perso, cell, cibles, finalCells);
			num++;
		}
	}	
}
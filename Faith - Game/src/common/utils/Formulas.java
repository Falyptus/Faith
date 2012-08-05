package common.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import objects.character.Player;
import objects.fight.Fight;
import objects.fight.Fighter;
import objects.guild.Guild;
import objects.guild.GuildMember;
import objects.guild.TaxCollector;
import objects.item.Item;
import objects.map.DofusMap;
import objects.spell.SpellEffect;

import common.Config;
import common.Constants;
import common.SocketManager;
import common.World;
import common.World.Couple;

public class Formulas {


	public static int getRandomValue(final int i1,final int i2)
	{
		final Random rand = new Random();
		return (rand.nextInt((i2-i1)+1))+i1;
	}
	
	/*MARTHIEUBEAN*/
	public static int getRandomOrientation()
	{
		return (Formulas.getRandomValue(0, 3)*2)+1;
	}
	/*FIN*/
	
	public static int getRandomJet(final String jet)//1d5+6
	{
		try
		{
			if(!jet.contains("d"))
				return Integer.parseInt(jet,16);
			
			int num = 0;
			final int des = Integer.parseInt(jet.split("d")[0]);
			final int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
			final int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
			for(int a=0;a<des;a++)
			{
				num += getRandomValue(1,faces);
			}
			num += add;
			return num;
		}catch(final NumberFormatException e){return -1;}
	}
	public static int getMinJet(final String jet)
	{
		int num = 0;
		int des = 0;
		int add = 0;
		try {
			des = Integer.parseInt(jet.split("d")[0]);
			add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
		}catch(final NumberFormatException e){
			return 0;
		}
		for(int i = 0; i < des; i++) {
			num++;
		}
		num += add;
		return num;
	}
	public static int getMaxJet(final String jet)
	{
		int num = 0;
		int des = 0;
		int faces = 0;
		int add = 0;
		try {
			num = 0;
			des = Integer.parseInt(jet.split("d")[0]);
			faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
			add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
		}catch(final NumberFormatException e){
			return -1;
		}
		for(int i = 0; i < des; i++) {
			num += faces;
		}
		num += add;
		return num;
	}
	public static int getMiddleJet(final String jet)//1d5+6
	{
		int num = 0;
		int des = Integer.parseInt(jet.split("d")[0]);
		int faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
		int add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
		try	{
			num = 0;
			des = Integer.parseInt(jet.split("d")[0]);
			faces = Integer.parseInt(jet.split("d")[1].split("\\+")[0]);
			add = Integer.parseInt(jet.split("d")[1].split("\\+")[1]);
		}catch(final NumberFormatException e){
			return 0;
		}
		num += ((1+faces)/2)*des;//on calcule moyenne
		num += add;
		return num;
	}
	
	public static int getTacleChance(Fighter tacleur, ArrayList<Fighter> tacle)
	{
		int agiTR = tacleur.getTotalStats().getEffect(Constants.STATS_ADD_AGIL);
		int agiT = 0;
		for(Fighter T : tacle) 
		{
			agiT += T.getTotalStats().getEffect(Constants.STATS_ADD_AGIL);
		}
		int a = agiTR+25;
		int b = agiTR+agiT+50;
		int chance = (int)((long)(300*a/b)-100);
		if(chance <10)chance = 10;
		if(chance >90)chance = 90;
		return chance;
	}
	
	/**
	 * 
	 * @author Desperion emulator 2.0 by Little-Scaraby and Nekkro
	 * 
	 */
	public static int truncate(double n)
	{
		double loc2 = Math.pow(Double.valueOf(10), Double.valueOf(0));
		double loc3 = n * loc2;
		return (int)((int)(loc3) / loc2);
	}
	
	/**
	 * 
	 * @author Desperion emulator 2.0 by Little-Scaraby and Nekkro
	 * 
	 */
	public static long calculateXpMonster(double groupXp, double groupLevel, double highestMonsterLevel, double ageBonus, Fighter ch, 
			ArrayList<Fighter> team, double mountRatio, double guildRatio)
	{
		// Calcul du niveau de la team, ainsi que du niveau du personnage le plus fort
		double totalTeamLevel = 0;
		double highestTeamLevel = 0;
		for(int a = 0; a < team.size(); ++a)
		{
			totalTeamLevel += team.get(a).getLvl();
			if(team.get(a).getLvl() > highestTeamLevel)
				highestTeamLevel = team.get(a).getLvl();
		}
		
		// Calcul du coefficient dû au niveau de la team par rapport au niveau du groupe de monstres
		double levelCoeff = 1;
		if(totalTeamLevel - 5 > groupLevel)
			levelCoeff = groupLevel / totalTeamLevel;
		else if(totalTeamLevel + 10 < groupLevel)
			levelCoeff = (totalTeamLevel + 10) / groupLevel;
		
		// pondération avec le niveau de ch
		double min = Math.min(ch.getLvl(), truncate(2.5 * highestMonsterLevel));
		double loc16 = min / totalTeamLevel * 100;

		/*
			détermination d'un facteur de multiplication en fonction du nombre de personnes
			dans la team: chaque personnage de la team doit avoir un niveau supérieur à
			(highestTeamLevel / 3) pour être considéré
		*/
		double[] teamFactors = {1, 1.1, 1.5, 2.3, 3.1, 3.6, 4.2, 4.7};
		short index = 0;
		for(int a = 0; a < team.size(); ++a)
		{
			if(team.get(a).getLvl() >= highestTeamLevel / 3)
				++index;
		}
		if(index == 0)
			index = 1;
		
		double loc20 = truncate(groupXp * teamFactors[index - 1] * levelCoeff);
		double loc22 = truncate(loc16 / 100 * loc20);

		double totalWisdom = ch.getTotalStats().getEffect(Constants.STATS_ADD_SAGE);
		if(totalWisdom < 0)
			totalWisdom = 0;

		double ageCoeff = ageBonus <= 0 ? 1 : (1 + ageBonus / 100);
		double xp = truncate(truncate(loc22 * (100 + totalWisdom) / 100) * ageCoeff); // xp totale gagnée par ch

		// on retranche l'xp perdue à cause de la drago
		if(mountRatio > 0)
			xp -= truncate(xp * mountRatio / 100);
		if(guildRatio > 0) // idem (la guilde passe après la drago)
			xp -= truncate(xp * guildRatio / 100);
		
		return (long) xp;
	}
	
	public static long getXpWinPvm3(final Fighter perso, final ArrayList<Fighter> winners,final ArrayList<Fighter> loosers,final long groupXP)
	{
		if(perso.getPlayer()== null)return 0;
		if(winners.contains(perso))//Si winner
		{
			final float sag = perso.getTotalStats().getEffect(Constants.STATS_ADD_SAGE);
			final float coef = (sag + 100)/100;
			final int taux = Config.XP_PVM;
			long xpWin = 0;
			int lvlmax = 0;
			
			for(final Fighter entry : winners)
			{
				if(entry.getLvl() > lvlmax)
					lvlmax = entry.getLvl();
			}
			int nbbonus = 0;
			for(final Fighter entry : winners)
			{
				if(entry.getLvl() > (lvlmax / 3))
					nbbonus += 1;				
			}
			
			int lvlLoosersmax = 0;
			for(final Fighter entry : loosers)
			{
				if(entry.getLvl() > lvlLoosersmax)
				lvlLoosersmax = entry.getLvl();
			}
			
			int lvlLoosers = 0;
			for(final Fighter entry : loosers)
				lvlLoosers += entry.getLvl();
				
			int lvlWinners = 0;
			for(final Fighter entry : winners)
				lvlWinners += entry.getLvl();
			
			final int lvl = perso.getLvl();
			final double bonusgroupe = ((double)lvl / (double)lvlmax);		
			
			double modif1 = 1;
			
			if (lvlLoosers + 5 > lvlWinners && lvlWinners > lvlLoosers - 10)
			{
				modif1 = 1;
				System.out.println(lvlLoosers+"+5 > "+lvlWinners+" > "+lvlLoosers+" +10");
				System.out.println("Modif 1 = "+modif1);
			}	
			if (lvlWinners < lvlLoosers - 10)
			{
				modif1 = (((double)lvlWinners + 10) / (double)lvlLoosers);
				System.out.println(lvlWinners+" < "+lvlLoosers+"-10");
				System.out.println("Modif 1 = "+modif1);
			}	
			if (lvlLoosers + 5 < lvlWinners)
			{
				modif1 = (double)lvlLoosers / (double)lvlWinners;
				System.out.println(lvlLoosers+"+5 < "+lvlWinners);
				System.out.println("Modif 1 = "+modif1);
			}
			/*
			if (lvlLoosers > lvlWinners + 10)
			{
				modif1 = ((double)lvlWinners + 10)/ (double)lvlLoosers;
				System.out.println("Modif 1 = "+modif1);
			}
			
			if (lvlWinners > lvlLoosers + 5)
			{
				modif1 = (double)lvlLoosers / (double)lvlWinners;
				System.out.println("Modif 1 = "+modif1);
			}*/
			
			double modif2 = 0;
			if ((lvlLoosersmax * 2.5) > lvlWinners)
			{
				modif2 = 1;
			}
			else
			{
				modif2 = Math.floor((2.5 * (int)lvlLoosersmax)) / (int)lvlWinners;
			}
			
			double bonus = 1;
			if(nbbonus == 2)
				bonus = 1.1;
			if(nbbonus == 3)
				bonus = 1.5;
			if(nbbonus == 4)
				bonus = 2.3;
			if(nbbonus == 5)
				bonus = 3.1;
			if(nbbonus == 6)
				bonus = 3.6;
			if(nbbonus == 7)
				bonus = 4.2;
			if(nbbonus >= 8)
				bonus = 4.7;
				
			
			xpWin = (long) (groupXP * coef * bonusgroupe * modif1 * modif2 * taux * bonus);
			
			/*/ DEBUG XP
			System.out.println("=========");
			System.out.println("groupXP: "+groupXP);
			System.out.println("coef: "+coef);
			System.out.println("bonusgroupe: "+bonusgroupe);
			System.out.println("modif 1: "+modif1);
			System.out.println("modif 2: "+modif2);
			System.out.println("taux: "+taux);
			System.out.println("bonus: "+bonus);
			System.out.println("xpWin: "+xpWin);
			System.out.println("=========");
			//*/
			return xpWin;
			
		}
		return 0;
	}
	
	public static long getXpWinPerco(final TaxCollector perco, final ArrayList<Fighter> winners,final ArrayList<Fighter> loosers,final long groupXP)
	{
			final Guild G = perco.getGuild();
			final float sag = G.getStats(Constants.STATS_ADD_SAGE);
			final float coef = (sag + 100)/100;
			final int taux = Config.XP_PVM;
			long xpWin = 0;
			int lvlmax = 0;
			for(final Fighter entry : winners)
			{
				if(entry.getLvl() > lvlmax)
					lvlmax = entry.getLvl();
			}
			int nbbonus = 0;
			for(final Fighter entry : winners)
			{
				if(entry.getLvl() > (lvlmax / 3))
					nbbonus += 1;				
			}
			
			double bonus = 1;
			if(nbbonus == 2)
				bonus = 1.1;
			if(nbbonus == 3)
				bonus = 1.3;
			if(nbbonus == 4)
				bonus = 2.2;
			if(nbbonus == 5)
				bonus = 2.5;
			if(nbbonus == 6)
				bonus = 2.8;
			if(nbbonus == 7)
				bonus = 3.1;
			if(nbbonus >= 8)
				bonus = 3.5;
			
			int lvlLoosers = 0;
			for(final Fighter entry : loosers)
				lvlLoosers += entry.getLvl();
			int lvlWinners = 0;
			for(final Fighter entry : winners)
				lvlWinners += entry.getLvl();
			double rapport = 1+((double)lvlLoosers/(double)lvlWinners);
			if (rapport <= 1.3)
				rapport = 1.3;
			/*
			if (rapport > 5)
				rapport = 5;
			//*/
			final int lvl = G.getLvl();
			final double rapport2 = 1 + ((double)lvl / (double)lvlWinners);

			xpWin = (long) (groupXP * rapport * bonus * taux *coef * rapport2);
			
			/*/ DEBUG XP
			System.out.println("=========");
			System.out.println("groupXP: "+groupXP);
			System.out.println("rapport1: "+rapport);
			System.out.println("bonus: "+bonus);
			System.out.println("taux: "+taux);
			System.out.println("coef: "+coef);
			System.out.println("rapport2: "+rapport2);
			System.out.println("xpWin: "+xpWin);
			System.out.println("=========");
			//*/
			return xpWin;	
	}
	
	public static String lastEatNewDate(final String Char) {
		final Date actDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd");
		final String jour = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("MM");
		final String mois = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("yyyy");
		final String annee = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("HH");
		final String heure = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("mm");
		final String min = dateFormat.format(actDate);
		final int moi = Integer.parseInt(mois) - 1;
		return annee + Char + moi + Char + jour + Char + heure + Char + min;
	}
	
	public static String getDate(final String[] stats) {
		final StringBuilder str = new StringBuilder("");
		str.append(Integer.parseInt(stats[1], 16)).append("-");

		final String Date = correctDate(Integer.toString(Integer.parseInt(stats[2], 16)));
		try {
			str.append(Integer.parseInt(Date.replaceAll(Date.substring(2), "")) + 1).append("-");
		} catch (final Exception e) {
			str.append("00").append("-");
		}
		try {
			str.append(Date.substring(2)).append("-");
		} catch (final Exception e) {
			str.append("00").append("-");
		}

		final String Time = correctDate("" + Integer.parseInt(stats[3], 16));
		try {
			str.append(Time.replaceAll(Time.substring(2), "")).append("-");
		} catch (final Exception e) {
			str.append("00").append("-");
		}
		try {
			str.append(Time.substring(2));
		} catch (final Exception e) {
			str.append("00");
		}
		return str.toString();
	}

	public static boolean compareTime(final String ExDate, final long Comp) {
		final GregorianCalendar Actual = (GregorianCalendar) GregorianCalendar.getInstance();
		final long actualMillis = Actual.getTimeInMillis();

		Actual.clear();
		final String[] infos = ExDate.split("-");
		Actual.set(Integer.parseInt(infos[0]), Integer.parseInt(infos[1]),
		        Integer.parseInt(infos[2]), Integer.parseInt(infos[3]),
		        Integer.parseInt(infos[4]));
		final long lastMillis = Actual.getTimeInMillis();

		return lastMillis + Comp >= actualMillis;
	}

	public static String newReceivedDate(final String split) {
		final Date actDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd");
		final String jour = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("MM");
		final String moi = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("yyyy");
		final String annee = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("HH");
		final String heure = dateFormat.format(actDate);
		dateFormat = new SimpleDateFormat("mm");
		final String min = dateFormat.format(actDate);
		final int mois = Integer.parseInt(moi) - 1;

		return Integer.toHexString(Integer.parseInt(annee)) + split
		        + Integer.toHexString(Integer.parseInt(new StringBuilder(String.valueOf(mois)).append(jour).toString()))
		        + split + Integer.toHexString(Integer.parseInt(new StringBuilder(String.valueOf(heure)).append(min).toString()));
	}

	public static int getMissedMeals(final String ExDate) {
		final GregorianCalendar Actual = (GregorianCalendar) GregorianCalendar.getInstance();
		final long actualMillis = Actual.getTimeInMillis();

		Actual.clear();
		final String[] infos = ExDate.split("-");
		Actual.set(Integer.parseInt(infos[0]), Integer.parseInt(infos[1]),
		        Integer.parseInt(infos[2]), Integer.parseInt(infos[3]),
		        Integer.parseInt(infos[4]));
		final long lastMillis = Actual.getTimeInMillis();
		final int nbr = (int) ((actualMillis - lastMillis) / Constants.ITEM_TIME_FEED_MAX);

		return nbr;
	}
	
	public static int getMissedMeals(final String ExDate, final long time) { //Familier
		final GregorianCalendar Actual = (GregorianCalendar) GregorianCalendar.getInstance();
		final long actualMillis = Actual.getTimeInMillis();

		Actual.clear();
		final String[] infos = ExDate.split("-");
		Actual.set(Integer.parseInt(infos[0]), Integer.parseInt(infos[1]),
		        Integer.parseInt(infos[2]), Integer.parseInt(infos[3]),
		        Integer.parseInt(infos[4]));
		final long lastMillis = Actual.getTimeInMillis();
		final int nbr = (int) ((actualMillis - lastMillis) / time);

		return nbr;
	}
	
	public static String correctDate(final String str) {
		switch (str.length()) {
		case 0:
			return "0000";
		case 1:
			return "000" + str;
		case 2:
			return "00" + str;
		case 3:
			return "0" + str;
		case 4:
			return str;
		}
		return null;
	}
	

	/*public static int calculFinalHeal(final Player caster,final int jet)
	{
		int statC = caster.getTotalStats().getEffect(Constants.STATS_ADD_INTE);
		final int soins = caster.getTotalStats().getEffect(Constants.STATS_ADD_SOIN);
		if(statC<0)statC=0;
		return (int)(jet * (100 + statC) / 100 + soins);
	}*/
	
	public static int calculFinalDamage(final Fight fight,final Fighter caster,final Fighter target,final int statID,final int jet,final boolean isHeal,final boolean isCac, final boolean isPoison)
	{
		float num = 0;
		float bonusMelee = 1;
		float statC = 0, domC = 0, perdomC = 0, resfT = 0, respT = 0;
		int multiplier = 0;
		if(!isHeal)
		{
			domC = caster.getTotalStats().getEffect(Constants.STATS_ADD_DOMA);
			perdomC = caster.getTotalStats().getEffect(Constants.STATS_ADD_PERDOM);
			multiplier = caster.getTotalStats().getEffect(Constants.STATS_MULTIPLY_DOMMAGE);
		}else
		{
			domC = caster.getTotalStats().getEffect(Constants.STATS_ADD_SOIN);
		}
		
		switch(statID)
		{
			case Constants.ELEMENT_NULL://Fixe
				statC = 0;
				resfT = 0;
				respT = 0;
				respT = 0;
			break;
			case Constants.ELEMENT_NEUTRE://neutre
				statC = caster.getTotalStats().getEffect(Constants.STATS_ADD_FORC);
				resfT = target.getTotalStats().getEffect(Constants.STATS_ADD_R_NEU);
				respT = target.getTotalStats().getEffect(Constants.STATS_ADD_RP_NEU);
				if(caster.getPlayer() != null)//Si c'est un joueur
				{
					respT += target.getTotalStats().getEffect(Constants.STATS_ADD_RP_PVP_NEU);
					resfT += target.getTotalStats().getEffect(Constants.STATS_ADD_R_PVP_NEU);
				}
				//on ajoute les dom Physique
				domC += caster.getTotalStats().getEffect(142);
				//Ajout de la resist Physique
				resfT = target.getTotalStats().getEffect(184);
			break;
			case Constants.ELEMENT_TERRE://force
				statC = caster.getTotalStats().getEffect(Constants.STATS_ADD_FORC);
				resfT = target.getTotalStats().getEffect(Constants.STATS_ADD_R_TER);
				respT = target.getTotalStats().getEffect(Constants.STATS_ADD_RP_TER);
				if(caster.getPlayer() != null)//Si c'est un joueur
				{
					respT += target.getTotalStats().getEffect(Constants.STATS_ADD_RP_PVP_TER);
					resfT += target.getTotalStats().getEffect(Constants.STATS_ADD_R_PVP_TER);
				}
				//on ajout les dom Physique
				domC += caster.getTotalStats().getEffect(142);
				//Ajout de la resist Physique
				resfT = target.getTotalStats().getEffect(184);
			break;
			case Constants.ELEMENT_EAU://chance
				statC = caster.getTotalStats().getEffect(Constants.STATS_ADD_CHAN);
				resfT = target.getTotalStats().getEffect(Constants.STATS_ADD_R_EAU);
				respT = target.getTotalStats().getEffect(Constants.STATS_ADD_RP_EAU);
				if(caster.getPlayer() != null)//Si c'est un joueur
				{
					respT += target.getTotalStats().getEffect(Constants.STATS_ADD_RP_PVP_EAU);
					resfT += target.getTotalStats().getEffect(Constants.STATS_ADD_R_PVP_EAU);
				}
				//Ajout de la resist Magique
				resfT = target.getTotalStats().getEffect(183);
			break;
			case Constants.ELEMENT_FEU://intell
				statC = caster.getTotalStats().getEffect(Constants.STATS_ADD_INTE);
				resfT = target.getTotalStats().getEffect(Constants.STATS_ADD_R_FEU);
				respT = target.getTotalStats().getEffect(Constants.STATS_ADD_RP_FEU);
				if(caster.getPlayer() != null)//Si c'est un joueur
				{
					respT += target.getTotalStats().getEffect(Constants.STATS_ADD_RP_PVP_FEU);
					resfT += target.getTotalStats().getEffect(Constants.STATS_ADD_R_PVP_FEU);
				}
				//Ajout de la resist Magique
				resfT = target.getTotalStats().getEffect(183);
			break;
			case Constants.ELEMENT_AIR://agilité
				statC = caster.getTotalStats().getEffect(Constants.STATS_ADD_AGIL);
				resfT = target.getTotalStats().getEffect(Constants.STATS_ADD_R_AIR);
				respT = target.getTotalStats().getEffect(Constants.STATS_ADD_RP_AIR);
				if(caster.getPlayer() != null)//Si c'est un joueur
				{
					respT += target.getTotalStats().getEffect(Constants.STATS_ADD_RP_PVP_AIR);
					resfT += target.getTotalStats().getEffect(Constants.STATS_ADD_R_PVP_AIR);
				}
				//Ajout de la resist Magique
				resfT = target.getTotalStats().getEffect(183);
			break;
		}
		//On bride la resistance a 50% si c'est un joueur 
		if(target.getMob() == null && respT >50)respT = 50;
		
		if(statC<0)statC=0;
		/* DEBUG
		System.out.println("Jet: "+jet+" Stats: "+statC+" perdomC: "+perdomC+" multiplier: "+multiplier);
		System.out.println("(100 + statC + perdomC)= "+(100 + statC + perdomC));
		System.out.println("(jet * (100 + statC + perdomC + (multiplier*100) ) / 100)= "+(jet * ((100 + statC + perdomC) / 100 )));
		System.out.println("res Fix. T "+ resfT);
		System.out.println("res %age T "+respT);
		if(target.getMob() != null)
		{
			System.out.println("resmonstre: "+target.getMob().getStats().getEffect(Constants.STATS_ADD_RP_FEU));
			System.out.println("TotalStat: "+target.getTotalStats().getEffect(Constants.STATS_ADD_RP_FEU));
			System.out.println("FightStat: "+target.getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_RP_FEU));
			
		}
		//*/
		
		Player player = caster.getPlayer();
		
		if(player != null && isCac)
		{
			int type = player.getObjetByPos(1).getTemplate().getType();
			int bonusSkill = 0;
			int bonusBreed = Constants.getBonusBreedWeapon(player.getBreedId(), type);
			
			if((caster.hasBuffBySpellId(392) == true) && type == 2)//ARC
			{
				bonusSkill = caster.getBuffValueBySpellID(392);
			}
			if((caster.hasBuffBySpellId(390) == true) && type == 4)//BATON
			{
				bonusSkill = caster.getBuffValueBySpellID(390);
			}
			if((caster.hasBuffBySpellId(391) == true) && type == 6)//EPEE
			{
				bonusSkill = caster.getBuffValueBySpellID(391);
			}
			if((caster.hasBuffBySpellId(393) == true) && type == 7)//MARTEAUX
			{
				bonusSkill = caster.getBuffValueBySpellID(393);
			}
			if((caster.hasBuffBySpellId(394) == true) && type == 3)//BAGUETTE
			{
				bonusSkill = caster.getBuffValueBySpellID(394);
			}
			if((caster.hasBuffBySpellId(395) == true) && type == 5)//DAGUES
			{
				bonusSkill = caster.getBuffValueBySpellID(395);
			}
			if((caster.hasBuffBySpellId(396) == true) && type == 8)//PELLE
			{
				bonusSkill = caster.getBuffValueBySpellID(396);
			}
			if((caster.hasBuffBySpellId(397) == true) && type == 19)//HACHE
			{
				bonusSkill = caster.getBuffValueBySpellID(397);
			}
			bonusMelee = (((100+bonusSkill)/100)*((100+bonusBreed)/100));
		}
		
		num = bonusMelee * (jet * ((100 + statC + perdomC + (multiplier*100)) / 100 )) + domC;//dégats bruts
		/*//Renvoie
		int renvoie = target.getTotalStatsLessBuff().getEffect(Constants.STATS_RETDOM);
		if(renvoie >0 && !isHeal)
		{
			if(renvoie > num)renvoie = (int)num;
			num -= renvoie;
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 107, "-1", target.getGUID()+","+renvoie);
			if(renvoie>caster.getLife())renvoie = caster.getLife();
			if(num<1)num =0;
			caster.removeLife(renvoie);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getGUID()+"", caster.getGUID()+",-"+renvoie);
		}*/
		/*if(!isHeal)num -= resfT;//resis fixe
		final int armor= getArmorResist(target,statID);
		if(!isHeal)num -= armor;
		if(!isHeal)if(armor > 0)SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getGUID()+"", target.getGUID()+","+armor);
		final int reduc =	(int)((num/(float)100)*respT);//Reduc %resis
		if(!isHeal)num -= reduc;
		//dégats finaux
		if(num < 1)num=0;*/
		if(!isHeal)num -= resfT;//resis fixe
		final int reduc = (int)((num*(float)respT)/100);//Reduc %resis
		if(!isHeal)num -= reduc;
		if(!isPoison) {
			final int armor= getArmorResist(target,statID);
			if(!isHeal) {
				if(armor > 0) {
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getGUID()+"", target.getGUID()+","+armor);
					num -= armor;
				}
			}	
		}
		if(num < 1)num=0;
		return (int)num;
	}
	public static int calculFinalHeal(Fighter caster, int jet)
	{
		int statC = caster.getTotalStats().getEffect(Constants.STATS_ADD_INTE);
		int soins = caster.getTotalStats().getEffect(Constants.STATS_ADD_SOIN);
		if(statC<0)statC=0;
		return (int)(jet * (100 + statC) / 100 + soins);
	}
	public static int calculZaapCost(final DofusMap map1,final DofusMap map2)
	{
		return (int) (10*(Math.abs(map2.getX()-map1.getX())+Math.abs(map2.getY()-map1.getY())-1));
	}
	public static int calculPrismCost(final DofusMap map1, final DofusMap map2, final int aLvl) 
	{
		int cost = (25 * (Math.abs(map2.getX() - map1.getX())
		        + Math.abs(map2.getY() - map1.getY()) - 1));
		int bonusSoustract = 0;
		double coef = 0.01;
		for (int i = 0; i < aLvl; i++) {
			coef += 0.01;
			bonusSoustract += cost * coef;
		}
		cost -= bonusSoustract;
		return cost;
	}
	private static int getArmorResist(final Fighter target, final int statID)
	{
		int armor = 0;
		for(final SpellEffect SE : target.getBuffsByEffectID(265))
		{
			Fighter fighter;
			
			switch(SE.getSpell())
			{
				case 1://Armure incandescente
					//Si pas element feu, on ignore l'armure
					if(statID != Constants.ELEMENT_FEU)continue;
					//Les stats du féca sont prises en compte
					fighter = SE.getCaster();
				break;
				case 6://Armure Terrestre
					//Si pas element terre/neutre, on ignore l'armure
					if(statID != Constants.ELEMENT_TERRE && statID != Constants.ELEMENT_NEUTRE)continue;
					//Les stats du féca sont prises en compte
					fighter = SE.getCaster();
				break;
				case 14://Armure Venteuse
					//Si pas element air, on ignore l'armure
					if(statID != Constants.ELEMENT_AIR)continue;
					//Les stats du féca sont prises en compte
					fighter = SE.getCaster();
				break;
				case 18://Armure aqueuse
					//Si pas element eau, on ignore l'armure
					if(statID != Constants.ELEMENT_EAU)continue;
					//Les stats du féca sont prises en compte
					fighter = SE.getCaster();
				break;
				
				default://Dans les autres cas on prend les stats de la cible et on ignore l'element de l'attaque
					fighter = target;
				break;
			}
			final int intell = fighter.getTotalStats().getEffect(Constants.STATS_ADD_INTE);
			int carac = 0;
			switch(statID)
			{
				case Constants.ELEMENT_AIR:
					carac = fighter.getTotalStats().getEffect(Constants.STATS_ADD_AGIL);
				break;
				case Constants.ELEMENT_FEU:
					carac = fighter.getTotalStats().getEffect(Constants.STATS_ADD_INTE);
				break;
				case Constants.ELEMENT_EAU:
					carac = fighter.getTotalStats().getEffect(Constants.STATS_ADD_CHAN);
				break;
				case Constants.ELEMENT_NEUTRE:
				case Constants.ELEMENT_TERRE:
					carac = fighter.getTotalStats().getEffect(Constants.STATS_ADD_FORC);
				break;
			}
			final int value = SE.getValue();
			final int a = value * (100 + (int)(intell/2) + (int)(carac/2))/100;
			armor += a;
		}
		for(final SpellEffect SE : target.getBuffsByEffectID(105))
		{
			final int intell = target.getTotalStats().getEffect(Constants.STATS_ADD_INTE);
			int carac = 0;
			switch(statID)
			{
				case Constants.ELEMENT_AIR:
					carac = target.getTotalStats().getEffect(Constants.STATS_ADD_AGIL);
				break;
				case Constants.ELEMENT_FEU:
					carac = target.getTotalStats().getEffect(Constants.STATS_ADD_INTE);
				break;
				case Constants.ELEMENT_EAU:
					carac = target.getTotalStats().getEffect(Constants.STATS_ADD_CHAN);
				break;
				case Constants.ELEMENT_NEUTRE:
				case Constants.ELEMENT_TERRE:
					carac = target.getTotalStats().getEffect(Constants.STATS_ADD_FORC);
				break;
			}
			final int value = SE.getValue();
			final int a = value * (100 + (int)(intell/2) + (int)(carac/2))/100;
			armor += a;
		}
		return armor;
	}

	public static int getPointsLost(final char c, final int value, final Fighter caster,final Fighter target)
	{
		int dodgeCaster = (int)(caster.getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_SAGE) / 4) + 1;
		int dodgeTarget = c == 'a' ? target.getTotalStats().getEffect(Constants.STATS_ADD_AFLEE) : target.getTotalStats().getEffect(Constants.STATS_ADD_MFLEE) + 1;
		int basePts = c == 'a' ? target.getBasePA() : target.getBasePM();
		int pointsLost = 0;
		
		for(int i = 0; i < value; i++) 
		{
			double currentLoopPoints = (double) basePts - pointsLost;
			double coef = (double) 1 / 2;
			double percentPointsRemaining = (double) currentLoopPoints / basePts;
			double dividedDodge = (double) dodgeCaster / dodgeTarget;
			double chance = (double)(coef * dividedDodge * percentPointsRemaining);
			int percentChance = (int) (chance * 100);
			if(percentChance >100) percentChance = 100;
			if(percentChance <0) percentChance = 0;
			
			int jet = getRandomValue(0, 99);
			if(jet < percentChance)
				pointsLost++;
		}
		
		return pointsLost;
	}
	
	/*public static int getPointsLost(final char c, final int value, final Fighter caster,final Fighter target)
	{
		int esquiveC = c=='a'?caster.getTotalStats().getEffect(Constants.STATS_ADD_AFLEE):caster.getTotalStats().getEffect(Constants.STATS_ADD_MFLEE);
		int esquiveT = c=='a'?target.getTotalStats().getEffect(Constants.STATS_ADD_AFLEE):target.getTotalStats().getEffect(Constants.STATS_ADD_MFLEE);
		int ptsMax = c=='a'?caster.getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_PA):caster.getTotalStatsLessBuff().getEffect(Constants.STATS_ADD_PM);
		int retrait = 0;
		
		for(int i = 0; i < value;i++)
		{
			int ptsAct = c=='a'?target.getTotalStats().getEffect(Constants.STATS_ADD_PA):target.getTotalStats().getEffect(Constants.STATS_ADD_PM);
			if(esquiveT == 0)esquiveT=1;
			if(esquiveC == 0)esquiveC=1;
			final double a = esquiveC/esquiveT;
			if(ptsAct == 0) ptsAct = 1;
			if(ptsMax == 0) ptsMax = 1;
			final double b = ptsAct/ptsMax;
			
			int chance = (int) (a * b * 50);
			if(chance <0)chance = 0;
			if(chance >100)chance = 100;
			/* DEBUG
			System.out.println("Chance d'esquiver le "+(i+1)+" eme PA : "+chance);
			///
			
			final int jet = getRandomValue(0, 99);
			if(jet<chance)
				retrait++;
		}
		return retrait;
	}*/

	public static long getXpWinPvm2(final Fighter perso, final ArrayList<Fighter> winners,final ArrayList<Fighter> loosers,final long groupXP)
	{
		if(perso.getPlayer()== null)return 0;
		if(winners.contains(perso))//Si winner
		{
			final float sag = perso.getTotalStats().getEffect(Constants.STATS_ADD_SAGE);
			final float coef = (sag + 100)/100;
			final int taux = Config.XP_PVM;
			long xpWin = 0;
			int lvlmax = 0;
			for(final Fighter entry : winners)
			{
				if(entry.getLvl() > lvlmax)
					lvlmax = entry.getLvl();
			}
			int nbbonus = 0;
			for(final Fighter entry : winners)
			{
				if(entry.getLvl() > (lvlmax / 3))
					nbbonus += 1;				
			}
			
			double bonus = 1;
			if(nbbonus == 2)
				bonus = 1.1;
			if(nbbonus == 3)
				bonus = 1.5;
			if(nbbonus == 4)
				bonus = 2.3;
			if(nbbonus == 5)
				bonus = 3.1;
			if(nbbonus == 6)
				bonus = 3.6;
			if(nbbonus == 7)
				bonus = 4.2;
			if(nbbonus >= 8)
				bonus = 4.7;
			
			int lvlLoosers = 0;
			for(final Fighter entry : loosers)
				lvlLoosers += entry.getLvl();
			int lvlWinners = 0;
			for(final Fighter entry : winners)
				lvlWinners += entry.getLvl();
			double rapport = 1+((double)lvlLoosers/(double)lvlWinners);
			if (rapport <= 1.3)
				rapport = 1.3;
			/*
			if (rapport > 5)
				rapport = 5;
			//*/
			final int lvl = perso.getLvl();
			final double rapport2 = 1 + ((double)lvl / (double)lvlWinners);

			xpWin = (long) (groupXP * rapport * bonus * taux *coef * rapport2);
			
			/*/ DEBUG XP
			System.out.println("=========");
			System.out.println("groupXP: "+groupXP);
			System.out.println("rapport1: "+rapport);
			System.out.println("bonus: "+bonus);
			System.out.println("taux: "+taux);
			System.out.println("coef: "+coef);
			System.out.println("rapport2: "+rapport2);
			System.out.println("xpWin: "+xpWin);
			System.out.println("=========");
			//*/
			return xpWin;	
		}
		return 0;
	}
	
	public static long getXpWinPvm(final Fighter perso, final ArrayList<Fighter> team,final ArrayList<Fighter> loose, final long groupXP)
	{
		/*int lvlwin = 0;
		for(Fighter entry : team)lvlwin += entry.getLvl();*/
		int lvllos = 0;
		for(final Fighter entry : loose)lvllos += entry.getLvl();
		final float bonusSage = (perso.getTotalStats().getEffect(Constants.STATS_ADD_SAGE)+100)/100;
		/* Formule 1
		float taux = perso.get_lvl()/lvlwin;
		long xp = (long)(groupXP * taux * bonusSage * perso.get_lvl());
		//*/
		//* Formule 2
		final long sXp = groupXP*lvllos;
		final long gXp = 2 * groupXP * perso.getLvl();
        final long xp = (long)((sXp + gXp)*bonusSage);
		//*/
		return xp*Config.XP_PVM;
	}
	public static long getXpWinPvP(final Fighter perso, final ArrayList<Fighter> winners, final ArrayList<Fighter> looser)
	{
		if(perso.getPlayer()== null)return 0;
		if(winners.contains(perso.getGUID()))//Si winner
		{
			int lvlLoosers = 0;
			for(final Fighter entry : looser)
				lvlLoosers += entry.getLvl();
		
			int lvlWinners = 0;
			for(final Fighter entry : winners)
				lvlWinners += entry.getLvl();
			final int taux = Config.XP_PVP;
			final float rapport = (float)lvlLoosers/(float)lvlWinners;
			final long xpWin = (long)(
						(
							rapport
						*	getXpNeededAtLevel(perso.getPlayer().getLvl())
						/	100
						)
						*	taux
					);
			//DEBUG
			System.out.println("Taux: "+taux);
			System.out.println("Rapport: "+rapport);
			System.out.println("XpNeeded: "+getXpNeededAtLevel(perso.getPlayer().getLvl()));
			System.out.println("xpWin: "+xpWin);
			//*/
			return xpWin;
		}
		return 0;
	}
	
	public static int getXpItem(final Item itm, final int div) {
		return Math.round(Item.getPoidsOfActualItem(itm.parseStatsString())
		        / div + itm.getTemplate().getLevel() / div
		        + itm.getTemplate().getPod());
	}
	
	private static long getXpNeededAtLevel(final int lvl)
	{
		final long xp = (World.getPersoXpMax(lvl) - World.getPersoXpMin(lvl));
		System.out.println("Xp Max => "+World.getPersoXpMax(lvl));
		System.out.println("Xp Min => "+World.getPersoXpMin(lvl));
		
		return xp;
	}

	public static long getGuildXpWin(final Fighter perso, final AtomicReference<Long> xpWin)
	{
		if(perso.getPlayer()== null)return 0;
		if(perso.getPlayer().getGuildMember() == null)return 0;
		
		final GuildMember gm = perso.getPlayer().getGuildMember();
		
		final double xp = (double)xpWin.get(), Lvl = perso.getLvl(),LvlGuild = perso.getPlayer().getGuild().getLvl(),pXpGive = (double)gm.getPXpGive()/100;
		
		final double maxP = xp * pXpGive * 0.10;	//Le maximum donné à la guilde est 10% du montant prélevé sur l'xp du combat
		final double diff = Math.abs(Lvl - LvlGuild);	//Calcul l'écart entre le niveau du personnage et le niveau de la guilde
		double toGuild;
		if(diff >= 70)
		{
			toGuild = maxP * 0.10;	//Si l'écart entre les deux level est de 70 ou plus, l'experience donnée a la guilde est de 10% la valeur maximum de don
		}
		else if(diff >= 31 && diff <= 69)
		{
			toGuild = maxP - ((maxP * 0.10) * (Math.floor((diff+30)/10)));
		}
		else if(diff >= 10 && diff <= 30)
		{
			toGuild = maxP - ((maxP * 0.20) * (Math.floor(diff/10))) ;
		}
		else	//Si la différence est [0,9]
		{
			toGuild = maxP;
		}
		xpWin.set((long)(xp - xp*pXpGive));
		return (long) Math.round(toGuild);
	}
	
	public static long getMountXpWin(final Fighter perso, final AtomicReference<Long> xpWin)
	{
		if(perso.getPlayer()== null)return 0;
		if(perso.getPlayer().getMount() == null)return 0;
		
		final int diff = Math.abs(perso.getLvl() - perso.getPlayer().getMount().getLevel());
		
		double coeff = 0;
		final double xp = (double) xpWin.get();
		final double pToMount = (double)perso.getPlayer().getMountXpGive() / 100 + 0.2;
		
		if(diff >= 0 && diff <= 9)
			coeff = 0.1;
		else if(diff >= 10 && diff <= 19)
			coeff = 0.08;
		else if(diff >= 20 && diff <= 29)
			coeff = 0.06;
		else if(diff >= 30 && diff <= 39)
			coeff = 0.04;
		else if(diff >= 40 && diff <= 49)
			coeff = 0.03;
		else if(diff >= 50 && diff <= 59)
			coeff = 0.02;
		else if(diff >= 60 && diff <= 69)
			coeff = 0.015;
		else
			coeff = 0.01;
		
		if(pToMount > 0.2)
			xpWin.set((long)(xp - (xp*(pToMount-0.2))));
		
		return (long)Math.round(xp * pToMount * coeff);
	}

	public static long getKamasWin(final Fighter i, final ArrayList<Fighter> winners, long maxk, final long mink)
	{
		long maxKamas = maxk;
		maxKamas++;
		final long rkamas = (long)(Math.random() * (maxKamas-mink)) + mink;
		return rkamas*Config.KAMAS;
	}
	
	public static long getKamasWinPerco(long maxk, final long mink)
	{
		long maxKamas = maxk;
		maxKamas++;
		final long rkamas = (long)(Math.random() * (maxKamas-mink)) + mink;
		return rkamas*Config.KAMAS;
	}
	
	public static int getZaapCost(final DofusMap map, final DofusMap map2)
	{
		final int cost = 0;
		
		return cost;
	}
	
	public static int calculElementChangeChance(final int lvlM,final int lvlA,final int lvlP)
	{
		int K = 350;
		if(lvlP == 1)K = 100;
		else if (lvlP == 25)K = 175;
		else if (lvlP == 50)K = 350;
		return (int)((lvlM*100)/(K + lvlA));
	}
	
	/*public static Objet fmObjet(int lvlM, Objet toFm,Objet rune)
	{
		int runeType = rune.getStats().getMap().keySet().toArray(new Integer[1])[0].intValue();	//Récupère la stats de la rune
		int runeSize = rune.getStats().getMap().get(runeType);	//Récupère la puissance d'ajout de la rune
		int maxW8 = 0,minW8 = 0;
		
		Map<Integer,Integer> maxStat = toFm.getMinMaxStats(true);	//Récuperation des stats max possible
		Map<Integer,Integer> minStat = toFm.getMinMaxStats(false);	//Récuperation des stats min possible
		
		for(int a : maxStat.keySet())
		{
			maxW8 += toFm.getStats().getEffect(runeType)
		}
		
		return toFm;
	}*/

	public static int calculHonorWin(final ArrayList<Fighter> winners,final ArrayList<Fighter> loosers,final Fighter F)
	{
		float totalGradeWin = 0;
		for(final Fighter f : winners)
		{
			if(f.getPlayer() == null )continue;
			totalGradeWin += f.getPlayer().getGrade();
		}
		float totalGradeLoose = 0;
		for(final Fighter f : loosers)
		{
			if(f.getPlayer() == null)continue;
			totalGradeLoose += f.getPlayer().getGrade();
		}
		int base = (int)(100 * (float)(totalGradeLoose/totalGradeWin))/winners.size();
		if(loosers.contains(F))base = -base;
		return base * Config.HONOR;
	}

	public static int calculDeshonorWin(final ArrayList<Fighter> winners,final ArrayList<Fighter> loosers,final Fighter F)
	{
		final ArrayList<Fighter> ennemy = new ArrayList<Fighter>();
		if(winners.contains(F))
			ennemy.addAll(loosers);
		else
			ennemy.addAll(winners);
		
		if(F.getPlayer() == null)return 0;//Pas normal ca XD
		if(F.getPlayer().getAlign() == Constants.ALIGNEMENT_NEUTRE || F.getPlayer().getAlign() == Constants.ALIGNEMENT_MERCENAIRE)return 0;
		
		for(final Fighter f : ennemy)
		{
			if(f.getPlayer() == null)continue;
			if(f.getPlayer().getAlign() == Constants.ALIGNEMENT_NEUTRE)return 1;
		}
		return 0;
	}
	
	public static Couple<Integer, Integer> decompSoulStone(final Item toDecomp)
	{
		Couple<Integer, Integer> toReturn;
		final String[] stats = toDecomp.parseStatsString().split("#");
		final int lvlMax = Integer.parseInt(stats[3],16);
		final int chance = Integer.parseInt(stats[1],16);
		toReturn = new Couple<Integer,Integer>(chance,lvlMax);
		
		return toReturn;
	}
	
	public static int totalCaptChance(final int pierreChance, final Player p)
	{
		int sortChance = 0;

		switch(p.getSortStatBySortIfHas(413).getLevel())
		{
			case 1:
				sortChance = 1;
				break;
			case 2:
				sortChance = 3;
				break;
			case 3:
				sortChance = 6;
				break;
			case 4:
				sortChance = 10;
				break;
			case 5:
				sortChance = 15;
				break;
			case 6:
				sortChance = 25;
				break;
			default:
				break;
		}
		
		return sortChance + pierreChance;
	}
	
	public static String parseResponse(final String response)
	{
		final StringBuilder toReturn = new StringBuilder();
		
		final String[] cut = response.split("[%]");
		
		if(cut.length == 1)return response;
		
		toReturn.append(cut[0]);
		
		char charact;
		for (int i = 1; i < cut.length; i++)
		{
			charact = (char) Integer.parseInt(cut[i].substring(0, 2),16);
			toReturn.append(charact).append(cut[i].substring(2));
		}
		
		return toReturn.toString();
	}
	
	public static int spellCost(final int nb)
	{
		int total = 0;
		for (int i = 1; i < nb ; i++)
		{
			total += i;
		}
		
		return total;
	}
	
	public static String getDateStr(final char split)
	{
		String toReturn = "";
		final Calendar actual = Calendar.getInstance();
		final int year = actual.get(Calendar.YEAR);
		final int month = actual.get(Calendar.MONTH) + 1;
		final int day = actual.get(Calendar.DAY_OF_MONTH);
		final int hour = actual.get(Calendar.HOUR_OF_DAY);
		final int min = actual.get(Calendar.MINUTE);
		toReturn = Integer.toHexString(year)+split+Integer.toHexString((month-1)*100+day)+split+Integer.toHexString(hour*100+min);
		return toReturn;
	}

	public static int getEnergyToLose(final int lvl, final int fightType) {
		int EnergyToLose = 10*lvl;
		if(fightType == Constants.FIGHT_TYPE_PVT)
			EnergyToLose += 5500;
		return EnergyToLose;
    }

	public static int getProtectorLvl(final int lvl) {
		if(lvl < 40)
			return 10;
		if(lvl < 80) 
			return 20;
		if(lvl < 120) 
			return 30;
		if(lvl < 160) 
			return 40;
		if(lvl < 200) 
			return 50;
		return 10;
	}

	public static int calculPrismCost(final DofusMap map1, final DofusMap map2, final Player p) 
	{
		int cost = (25 * (Math.abs(map2.getX() - map1.getX()) + Math.abs(map2.getY() - map1.getY()) - 1));
		int bonusSoustract = 0;
		double coef = 0.01;
		for (int i = 1; i < p.getGrade(); i++)
		{
			coef += 0.01;
			bonusSoustract += cost;
		}
		bonusSoustract *= coef;
		cost -= bonusSoustract;
		return cost;
	}

	public static int getKnightGradeByLvlEnnemy(final ArrayList<Fighter> teamEnnemy) {
		int lvl = 0;
		for(final Fighter f : teamEnnemy)
		{
			lvl += f.getLvl();
		}
		if(lvl < 25*teamEnnemy.size()) return 1;
		if(lvl < 50*teamEnnemy.size()) return 3;
		if(lvl < 75*teamEnnemy.size()) return 3;
		if(lvl < 100*teamEnnemy.size()) return 4;
		if(lvl < 200*teamEnnemy.size()) return 5;
		return 5;
	}
}

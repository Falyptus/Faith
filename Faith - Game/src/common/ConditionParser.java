package common;

import objects.character.Player;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import common.console.Log;

public class ConditionParser
{
	public static boolean validConditions(Player perso,String req)
	{
		if(req == null || req.equals(""))return true;
		if(req.contains("BI"))return false;
		
		Jep jep = new Jep();
		
		req = req.replace("&", "&&").replace("=", "==").replace("|", "||").replace("!", "!=").replace("~", "==");
		
		if(req.contains("PO"))
			req = havePO(req, perso);
		if(req.contains("PN"))
			req = canPN(req, perso);
	 	//TODO : G�rer PJ Pj
		try
		{
				//Stats stuff compris
				jep.addVariable("CI", perso.getTotalStats().getEffect(Constants.STATS_ADD_INTE));
			 	jep.addVariable("CV", perso.getTotalStats().getEffect(Constants.STATS_ADD_VITA));
			 	jep.addVariable("CA", perso.getTotalStats().getEffect(Constants.STATS_ADD_AGIL));
			 	jep.addVariable("CW", perso.getTotalStats().getEffect(Constants.STATS_ADD_SAGE));
			 	jep.addVariable("CC", perso.getTotalStats().getEffect(Constants.STATS_ADD_CHAN));
			 	jep.addVariable("CS", perso.getTotalStats().getEffect(Constants.STATS_ADD_FORC));
			 	//Stats de bases
			 	jep.addVariable("Ci", perso.getBaseStats().getEffect(Constants.STATS_ADD_INTE));
			 	jep.addVariable("Cs", perso.getBaseStats().getEffect(Constants.STATS_ADD_FORC));
			 	jep.addVariable("Cv", perso.getBaseStats().getEffect(Constants.STATS_ADD_VITA));
			 	jep.addVariable("Ca", perso.getBaseStats().getEffect(Constants.STATS_ADD_AGIL));
			 	jep.addVariable("Cw", perso.getBaseStats().getEffect(Constants.STATS_ADD_SAGE));
			 	jep.addVariable("Cc", perso.getBaseStats().getEffect(Constants.STATS_ADD_CHAN));
			 	//Autre
			 	jep.addVariable("Ps", perso.getAlign());
			 	jep.addVariable("Pa", perso.getALvl());
			 	jep.addVariable("PP", perso.getGrade());
			 	jep.addVariable("PL", perso.getLvl());
			 	jep.addVariable("PK", perso.getKamas());
			 	jep.addVariable("PG", perso.getBreedId());
			 	jep.addVariable("PS", perso.getSexe());
			 	jep.addVariable("PZ", 1);//Abonnement
			 	jep.addVariable("PX", perso.getAccount().getGmLvl());
			 	jep.addVariable("PW", perso.getMaxPod());
			 	jep.addVariable("PB", perso.getCurMap().getSubArea().getId());
			 	jep.addVariable("PR", (perso.getSpouse() != null ? 1 : 0));
			 	jep.addVariable("SI", perso.getCurMap().getId());
			 	//Les pierres d'ames sont lancables uniquement par le lanceur.
			 	jep.addVariable("MiS",perso.getActorId());
			 	
			 	jep.parse(req);
			 	Object result = jep.evaluate();
			 	boolean ok = false;
			 	if(result != null)ok = Boolean.valueOf(result.toString());
			 	return ok;
		} catch (JepException e)
		{
			System.out.println("An error occurred: " + e.getMessage());
		}
		return true;
	}
	
	public static String havePO(String cond,Player perso)//On remplace les PO par leurs valeurs si possession de l'item
	{
		boolean Jump = false;
		boolean ContainsPO = false;
		boolean CutFinalLenght = true;
		String copyCond = "";
		int finalLength = 0;
		
		if(Config.CONFIG_DEBUG) Log.addToLog("Entered Cond : "+cond);
		
		if(cond.contains("&&"))
		{
			for(String cur : cond.split("&&"))
			{
				if(cond.contains("=="))
				{
					for(String cur2 : cur.split("=="))
					{
						if(cur2.contains("PO")) 
						{
							ContainsPO = true;
							continue;
						}
						if(Jump)
						{
							copyCond += cur2;
							Jump = false;
							continue;
						}
						if(!cur2.contains("PO") && !ContainsPO)
						{
							copyCond += cur2+"==";
							Jump = true;
							continue;
						}
						if(cur2.contains("!=")) continue;
						ContainsPO = false;
						if(perso.hasItemTemplate(Integer.parseInt(cur2), 1))
						{
							copyCond += Integer.parseInt(cur2)+"=="+Integer.parseInt(cur2);
						}else
						{
							copyCond += Integer.parseInt(cur2)+"=="+0;
						}
					}
				}
				if(cond.contains("!="))
				{
					for(String cur2 : cur.split("!="))
					{
						if(cur2.contains("PO")) 
						{
							ContainsPO = true;
							continue;
						}
						if(Jump)
						{
							copyCond += cur2;
							Jump = false;
							continue;
						}
						if(!cur2.contains("PO") && !ContainsPO)
						{
							copyCond += cur2+"!=";
							Jump = true;
							continue;
						}
						if(cur2.contains("==")) continue;
						ContainsPO = false;
						if(perso.hasItemTemplate(Integer.parseInt(cur2), 1))
						{
							copyCond += Integer.parseInt(cur2)+"!="+Integer.parseInt(cur2);
						}else
						{
							copyCond += Integer.parseInt(cur2)+"!="+0;
						}
					}
				}
				copyCond += "&&";
			}
		}else if(cond.contains("||"))
		{
			for(String cur : cond.split("\\|\\|"))
			{
				if(cond.contains("=="))
				{
					for(String cur2 : cur.split("=="))
					{
						if(cur2.contains("PO")) 
						{
							ContainsPO = true;
							continue;
						}
						if(Jump)
						{
							copyCond += cur2;
							Jump = false;
							continue;
						}
						if(!cur2.contains("PO") && !ContainsPO)
						{
							copyCond += cur2+"==";
							Jump = true;
							continue;
						}
						if(cur2.contains("!=")) continue;
						ContainsPO = false;
						if(perso.hasItemTemplate(Integer.parseInt(cur2), 1))
						{
							copyCond += Integer.parseInt(cur2)+"=="+Integer.parseInt(cur2);
						}else
						{
							copyCond += Integer.parseInt(cur2)+"=="+0;
						}
					}
				}
				if(cond.contains("!="))
				{
					for(String cur2 : cur.split("!="))
					{
						if(cur2.contains("PO")) 
						{
							ContainsPO = true;
							continue;
						}
						if(Jump)
						{
							copyCond += cur2;
							Jump = false;
							continue;
						}
						if(!cur2.contains("PO") && !ContainsPO)
						{
							copyCond += cur2+"!=";
							Jump = true;
							continue;
						}
						if(cur2.contains("==")) continue;
						ContainsPO = false;
						if(perso.hasItemTemplate(Integer.parseInt(cur2), 1))
						{
							copyCond += Integer.parseInt(cur2)+"!="+Integer.parseInt(cur2);
						}else
						{
							copyCond += Integer.parseInt(cur2)+"!="+0;
						}
					}
				}
					copyCond += "||";
			}
		}else
		{
			CutFinalLenght = false;
			if(cond.contains("=="))
			{
				for(String cur : cond.split("=="))
				{
					if(cur.contains("PO")) 
					{
						continue;
					}
					if(cur.contains("!=")) continue;
					if(perso.hasItemTemplate(Integer.parseInt(cur), 1))
					{
						copyCond += Integer.parseInt(cur)+"=="+Integer.parseInt(cur);
					}else
					{
						copyCond += Integer.parseInt(cur)+"=="+0;
					}
				}
			}
			if(cond.contains("!="))
			{
				for(String cur : cond.split("!="))
				{
					if(cur.contains("PO")) 
					{
						continue;
					}
					if(cur.contains("==")) continue;
					if(perso.hasItemTemplate(Integer.parseInt(cur), 1))
					{
						copyCond += Integer.parseInt(cur)+"!="+Integer.parseInt(cur);
					}else
					{
						copyCond += Integer.parseInt(cur)+"!="+0;
					}
				}
			}
		}
		if(CutFinalLenght)
		{
			finalLength = (copyCond.length()-2);//On retire les deux derniers carract�res (|| ou &&)
			copyCond = copyCond.substring(0, finalLength);
		}
		if(Config.CONFIG_DEBUG) Log.addToLog("Returned Cond : "+copyCond);
		return copyCond;
	}
	
	public static String canPN(String cond,Player perso)//On remplace le PN par 1 et si le nom correspond == 1 sinon == 0
	{
		String copyCond = "";
		for(String cur : cond.split("=="))
		{
			if(cur.contains("PN")) 
			{
				copyCond += "1==";
				continue;
			}
			if(perso.getName().toLowerCase().compareTo(cur) == 0)
			{
				copyCond += "1";
			}else
			{
				copyCond += "0";
			}
		}
		return copyCond;
	}
}

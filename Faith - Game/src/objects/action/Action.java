package objects.action;

import java.io.PrintWriter;
import java.util.ArrayList;

import objects.alignment.Conquest;
import objects.alignment.Prism;
import objects.character.Player;
import objects.item.Item;
import objects.item.ItemTemplate;
import objects.item.SoulStone;
import objects.job.JobStat;
import objects.map.DofusMap;
import objects.npc.NpcQuestion;

import common.ConditionParser;
import common.Config;
import common.Constants;
import common.SQLManager;
import common.SocketManager;
import common.World;
import common.World.SubArea;
import common.console.Log;
import common.utils.Formulas;

public class Action {

	private final int ID;
	private final String args;
	private final String cond;
	
	public Action(final int id, final String args, final String cond)
	{
		this.ID = id;
		this.args = args;
		this.cond = cond;
	}


	public void apply(final Player perso, final int objetID)
	{
		if(perso == null)return;
		if(!cond.equalsIgnoreCase("") && !cond.equalsIgnoreCase("-1")&& !ConditionParser.validConditions(perso,cond))
		{
			SocketManager.GAME_SEND_Im_PACKET(perso, "119");
			return;
		}
		PrintWriter out = perso.getAccount().getGameThread().getOut();	
		switch(ID)
		{
			case -2://créer guilde
				if(perso.isAway())return;
				if(perso.getGuild() != null || perso.getGuildMember() != null)
				{
					SocketManager.GAME_SEND_gC_PACKET(perso, "Ea");
					return;
				}
				SocketManager.GAME_SEND_gn_PACKET(perso);
			break;
			case -1://Ouvrir banque
				final int cost = perso.getBankCost();
				if(cost > 0)
				{
					final long nKamas = perso.getKamas() - cost;
					if(nKamas <0)//Si le joueur n'a pas assez de kamas pour ouvrir la banque
					{
						SocketManager.REALM_SEND_MESSAGE(perso, "110|"+cost+";");
						return;
					}
					perso.setKamas(nKamas);
					perso.kamasLog(-cost+"", "Ouverture de la banque");
					
					SocketManager.GAME_SEND_STATS_PACKET(perso);
					SocketManager.GAME_SEND_Im_PACKET(perso, "020;"+cost);
				}
				SocketManager.GAME_SEND_ECK_PACKET(perso.getAccount().getGameThread().getOut(), 5, "");
				SocketManager.GAME_SEND_EL_BANK_PACKET(perso);
				perso.setAway(true);
				perso.setInBank(true);
			break;
			
			case 0://Téléportation
				try
				{
					final int newMapID = Integer.parseInt(args.split(",",2)[0]);
					final int newCellID = Integer.parseInt(args.split(",",2)[1]);
					perso.teleport(newMapID,newCellID);
				}catch(final Exception e ){return;};
				break;
			case 1://Discours NPC
				out = perso.getAccount().getGameThread().getOut();
				if(args.equalsIgnoreCase("DV"))
				{
					SocketManager.GAME_SEND_END_DIALOG_PACKET(out);
					perso.setIsTalkingWith(0);
				}else
				{
					int qID = -1;
					try
					{
						qID = Integer.parseInt(args);
					}catch(final NumberFormatException e){};
					
					final NpcQuestion  quest = World.getNPCQuestion(qID);
					if(quest == null)
					{
						SocketManager.GAME_SEND_END_DIALOG_PACKET(out);
						perso.setIsTalkingWith(0);
						return;
					}
					SocketManager.GAME_SEND_QUESTION_PACKET(out, quest.parseToDQPacket(perso));
				}
				break;
			case 4://Kamas
				try
				{
					final int count = Integer.parseInt(args);
					final long curKamas = perso.getKamas();
					long newKamas = curKamas + count;
					if(newKamas <0) newKamas = 0;
					perso.setKamas(newKamas);
					
					//Si en ligne (normalement oui)
					if(perso.isOnline())
						SocketManager.GAME_SEND_STATS_PACKET(perso);
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 5://objet
				try
				{
					final int tID = Integer.parseInt(args.split(",")[0]);
					final int count = Integer.parseInt(args.split(",")[1]);
					boolean send = true;
					if(args.split(",").length >2)send = args.split(",")[2].equals("1");
					
					//Si on ajoute
					if(count > 0)
					{
						final ItemTemplate template = World.getItemTemplate(tID);
						if(template == null)return;
						final Item item = template.createNewItem(count, false);
						//Si retourne true, on l'ajoute au monde
						if(perso.addItem(item, true))
							World.addItem(item, true);
						perso.itemLog(tID, item.getQuantity(), "En utilisant un objet");
					}else
					{
						perso.removeByTemplateID(tID,-count);
					}
					//Si en ligne (normalement oui)
					if(perso.isOnline())//on envoie le packet qui indique l'ajout//retrait d'un item
					{
						SocketManager.GAME_SEND_Ow_PACKET(perso);
						if(send)
						{
							if(count >= 0){
								SocketManager.GAME_SEND_Im_PACKET(perso, "021;"+count+"~"+tID);
							}
							else if(count < 0){
								SocketManager.GAME_SEND_Im_PACKET(perso, "022;"+-count+"~"+tID);
							}
						}
					}
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 6://Apprendre un métier
				try
				{
					final int mID = Integer.parseInt(args);
					if(World.getMetier(mID) == null)return;
					perso.learnJob(World.getMetier(mID));
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
			break;
			case 7://retour au point de sauvegarde
				perso.teleportToSavePos();
				break;
			case 8://Ajouter une Stat
				try
				{
					final int statID = Integer.parseInt(args.split(",",2)[0]);
					final int number = Integer.parseInt(args.split(",",2)[1]);
					perso.getBaseStats().addOneStat(statID, number);
					perso.clearCacheAS();
					SocketManager.GAME_SEND_STATS_PACKET(perso);
					int messID = 0;
					switch(statID)
					{
						case Constants.STATS_ADD_VITA: messID = 13; break;
						case Constants.STATS_ADD_SAGE: messID = 9;  break;
						case Constants.STATS_ADD_FORC: messID = 13; break;
						case Constants.STATS_ADD_INTE: messID = 14; break;
						case Constants.STATS_ADD_CHAN: messID = 11; break;
						case Constants.STATS_ADD_AGIL: messID = 12; break;
						default: break;
					}
					if(messID>0)
						SocketManager.GAME_SEND_Im_PACKET(perso, "0"+messID+";"+number);
				}catch(final Exception e ){return;};
				break;
			case 9://Apprendre un sort
				try
				{
					final int sID = Integer.parseInt(args);
					if(World.getSpell(sID) == null)return;
					perso.learnSpell(sID,1, true,true);
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 10://Pain/potion/viande/poisson
				try
				{
					final int min = Integer.parseInt(args.split(",",2)[0]);
					int max = Integer.parseInt(args.split(",",2)[1]);
					if(max == 0) max = min;
					int val = Formulas.getRandomValue(min, max);
					if(perso.getPDV() + val > perso.getPDVMAX())val = perso.getPDVMAX()-perso.getPDV();
					perso.setPDV(perso.getPDV()+val);
					SocketManager.GAME_SEND_STATS_PACKET(perso);
					SocketManager.GAME_SEND_Im_PACKET(perso, "01;"+val);
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 11://Definir l'alignement
				try
				{
					final byte newAlign = Byte.parseByte(args.split(",",2)[0]);
					final boolean replace = Integer.parseInt(args.split(",",2)[1]) == 1;
					//Si le perso n'est pas neutre, et qu'on doit pas remplacer, on passe
					if(perso.getAlign() != Constants.ALIGNEMENT_NEUTRE && !replace)return;
					perso.modifAlignement(newAlign);
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 12://Spawn Groupe args : boolean delObj?,boolean enArène?
				try
				{
					final boolean delObj = args.split(",")[0].equals("true");
					final boolean inArena = args.split(",")[1].equals("true");
					
					if(inArena && !World.isArenaMap(perso.getCurMap().getId()))return;	//Si la map du personnage n'est pas classé comme étant dans l'arène
					
					final SoulStone pierrePleine = (SoulStone)World.getObjet(objetID);
					
					final String groupData = pierrePleine.parseGroupData();
					final String condition = "M_PID = "+perso.getActorId();	//Condition pour que le groupe ne soit lançable que par le personnage qui à utiliser l'objet
					perso.getCurMap().spawnGroup(true, false, perso.getCurCell().getId(), groupData,condition);
					
					if(delObj)
					{
						perso.removeItem(objetID, 1, true, true);
					}
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 13://Ouvrir l'interface d'oublie de sort
				perso.setIsForgetingSpell(true);
				SocketManager.GAME_SEND_FORGETSPELL_INTERFACE('+', perso);
				break;
			case 15://Téléportation donjon
				try
				{
					final int newMapID = Integer.parseInt(args.split(",")[0]);
					final int newCellID = Integer.parseInt(args.split(",")[1]);
					final int itemNeeded = Integer.parseInt(args.split(",")[2]);
					final int mapNeeded = Integer.parseInt(args.split(",")[3]);
					if(itemNeeded == 0)
					{
						//Téléportation sans objets
						//perso.teleport(newMapID,newCellID);
						perso.teleport(newMapID, newCellID);
						return;
					}
					if (itemNeeded <= 0)
						return;
					if (mapNeeded == 0) {
						perso.teleport(newMapID, newCellID);
						return;
					}
					if (mapNeeded <= 0)
						return;
					if (perso.hasItemTemplate(itemNeeded, 1) && perso.getCurMap().getId() == mapNeeded) {
						perso.teleport(newMapID, newCellID);
						perso.removeByTemplateID(itemNeeded, 1);
						SocketManager.GAME_SEND_Ow_PACKET(perso);
						return;
					}
					if (perso.getCurMap().getId() != mapNeeded) {
						SocketManager.GAME_SEND_MESSAGE(perso, "Vous n'êtes pas sur la bonne map du donjon pour être téléporter.", "009900");
						return;
					}
					//Le perso ne possède pas l'item
					SocketManager.GAME_SEND_MESSAGE(perso, "Vous ne possédez pas la clef nécessaire.", "009900");
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 16://Ajout de points d'honneur
				try
				{
					if(perso.getAlign() != 0)
					{
						final int valueToAdd = Integer.parseInt(args);
						final int actualHonor = perso.getHonor();
						perso.setHonor(actualHonor+valueToAdd);
						SocketManager.GAME_SEND_Im_PACKET(perso, "074;"+valueToAdd);
					}
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 17://Xp métier JobID,XpValue
				try
				{
					final int JobID = Integer.parseInt(args.split(",")[0]);
					final int XpValue = Integer.parseInt(args.split(",")[1]);
					if(perso.getMetierByID(JobID) != null)
					{
						perso.getMetierByID(JobID).addXp(perso, XpValue);
						SocketManager.GAME_SEND_Im_PACKET(perso, "017;"+XpValue+"~"+JobID);
					}
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 18://Téléportation chez sois
				
				break;
			case 19://Téléportation maison de guilde (ouverture du panneau de guilde)
				
				break;
			case 20://+Points de sorts
				try
				{
					int pts = 0;
					if(args.contains(",")) 
						pts = Formulas.getRandomValue(Integer.parseInt(args.split(",")[0]), Integer.parseInt(args.split(",")[1]));
					else
						pts = Integer.parseInt(args);
					if(pts < 1) return;
					perso.addSpellPoint(pts);
					SocketManager.GAME_SEND_STATS_PACKET(perso);
					SocketManager.GAME_SEND_Im_PACKET(perso, "016;"+pts);
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 21://+Energie
				try
				{
					int Energy = 0;
					if(args.contains(",")) 
						Energy = Formulas.getRandomValue(Integer.parseInt(args.split(",")[0]), Integer.parseInt(args.split(",")[1]));
					else
						Energy = Integer.parseInt(args);
					if(Energy < 1) return;
					
					int EnergyTotal = perso.getEnergy()+Energy;
					if(EnergyTotal > 10000) EnergyTotal = 10000;
					
					perso.setEnergy(EnergyTotal);
					SocketManager.GAME_SEND_STATS_PACKET(perso);
					SocketManager.GAME_SEND_Im_PACKET(perso, "07;"+Energy);
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 22://+Xp
				try
				{
					long XpAdd = 0;
					if(args.contains(",")) 
						XpAdd = Formulas.getRandomValue(Integer.parseInt(args.split(",")[0]), Integer.parseInt(args.split(",")[1]));
					else
						XpAdd = Integer.parseInt(args);
					if(XpAdd < 1) return;
					
					final long TotalXp = perso.getCurExp()+XpAdd;
					perso.setCurExp(TotalXp);
					SocketManager.GAME_SEND_STATS_PACKET(perso);
					SocketManager.GAME_SEND_Im_PACKET(perso, "08;"+XpAdd);
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 23://UnlearnJob
				try
				{
					final int Job = Integer.parseInt(args);
					if(Job < 1) return;
					final JobStat job = perso.getMetierByID(Job);
					if(job == null) return;
					perso.forgetJob(job.getID());
					SocketManager.GAME_SEND_JR_PACKET(perso, job.getTemplate().getId());
					SocketManager.GAME_SEND_STATS_PACKET(perso);
					SQLManager.SAVE_PERSONNAGE(perso, false);
				}catch(final Exception e){Log.addToLog(e.getMessage());};
				break;
			case 24://SimpleMorph
				try
				{
					final int morphID = Integer.parseInt(args);
					if(morphID < 0)return;
					perso.setGfxID(morphID);
					SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getActorId());
					SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(), perso);
				}catch(final Exception e){Log.addToErrorLog(e.getMessage());};
				break;
			case 25://SimpleUnMorph
				final int UnMorphID = perso.getBreedId()*10 + perso.getSexe();
				perso.setGfxID(UnMorphID);
				SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getActorId());
				SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(), perso);
				break;
			case 26://Téléportation enclo de guilde (ouverture du panneau de guilde)
				
				break;
			case 27://startFigthVersusMonstres args : monsterID,monsterLevel| ...
				StringBuilder ValidMobGroup = new StringBuilder();
				try
		        {
					for(final String MobAndLevel : args.split("\\|"))
					{
						int monsterID = -1;
						int monsterLevel = -1;
						final String[] MobOrLevel = MobAndLevel.split(",");
						monsterID = Integer.parseInt(MobOrLevel[0]);
						monsterLevel = Integer.parseInt(MobOrLevel[1]);
						
						if(World.getMonstre(monsterID) == null || World.getMonstre(monsterID).getGradeByLevel(monsterLevel) == null)
						{
							if(Config.CONFIG_DEBUG) Log.addToErrorLog("Monstre invalide : monsterID:"+monsterID+" monsterLevel:"+monsterLevel);
							continue;
						}
						ValidMobGroup.append(monsterID).append(',').append(monsterLevel).append(',').append(monsterLevel).append(';');
					}
					if(ValidMobGroup.length() == 0) return;
					final objects.monster.MonsterGroup group  = new objects.monster.MonsterGroup(perso.getCurMap()._nextObjectID,perso.getCurCell().getId(),ValidMobGroup.toString());
					perso.getCurMap().startFightVersusMonstres(perso, group);
		        }catch(final Exception e){Log.addToErrorLog(e.getMessage());};
		        break;
			
			/*------------------LIGNE PAR MARTHIEUBEAN-----------------------*/
			case 90://Ouvrir l'enclose pour accéder aux montures
				perso.openPublicMountPark();
				break;
				
			case 91://Donner une capacité à une monture
				perso.getMount().addCapacity(args);
				perso.toogleOnMount();
				SocketManager.GAME_SEND_Re_PACKET(perso, "+", perso.getMount());
				break;
			
			case 92://Recevoir une traque a exécuter
				if(!perso.canTrack())
				{
					return;
				}
				final ArrayList<Player> possibles = new ArrayList<Player>();
				for(final Player victime : World.getOnlinePersos())
				{
					if (victime == null || victime == perso || !victime.isOnline())
						continue;
					if (victime.getAccount().getCurIP().compareTo(perso.getAccount().getCurIP()) == 0)
						continue;
					if (victime.getAlign() == perso.getAlign() || victime.getAlign() == 0 || victime.getAlign() == 3 || !victime.isShowingWings())
						continue;
					if (((perso.getLvl() + 20) >= victime.getLvl()) && (perso.getLvl() - 50) <= victime.getLvl())
						possibles.add(victime);
				}
				if(possibles.isEmpty())
				{
					//SocketManager.GAME_SEND_QUESTION_PACKET(perso.get_compte().getGameThread().get_out(), id);
					SocketManager.GAME_SEND_MESSAGE(perso, "Il n'y a aucune cible, revenez plus tard !", "C10000");
					return;
				}
				perso.setTraque(possibles.get(Formulas.getRandomValue(1, possibles.size()-1)));
				perso.addTrackItem();
				break;
				
			case 94://Manger une friandise avec des effets durable (ex: shigekax pomme)
				final int friandiseId = Integer.parseInt(args);
				if(perso.getObjetByPos(20) != null)
				{
					final int guid = perso.getObjetByPos(20).getGuid();
					SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(perso, guid);
					perso.removeItem(guid);
					World.removeItem(guid);
				}
				final Item friandise = World.getItemTemplate(friandiseId).createNewItem(1, true);
				friandise.setPosition(20);//On le prépare à le placer dans sa zone
				if(perso.addItem(friandise, true))
					World.addItem(friandise, true);
				perso.clearCacheAS();
				SocketManager.GAME_SEND_STATS_PACKET(perso);
				break;
				
			case 95://Poser un prisme
				final DofusMap map = perso.getCurMap();
				final SubArea subArea = map.getSubArea();
				if(!Conquest.CanDepositPrism(perso.getActorId(), subArea.getId()))
					break;
				final int templatePrism = Integer.parseInt(args);
				perso.removeByTemplateID(templatePrism, 1);
				final Prism prism = new Prism(World.getNewPrismId(), perso.getAlign(), (byte)perso.getALvl(), map.getId(), perso.getCurCell().getId());
				World.addPrism(prism);
				map.setPrism(prism);
				subArea.setAlignement(prism.getAlign());
				subArea.setPrism(prism);
				SocketManager.GAME_SEND_MAP_PRISM_GM_PACKET(map);
				SQLManager.SAVE_PRISM(prism);
				break;
				
			/****Action Site Web****/
			case 300://Changement de titre
				final byte title = Byte.parseByte(args);
				perso.setTitle(title);
				SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getActorId());
				SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(), perso);
				SQLManager.SAVE_PERSONNAGE(perso, false);
				break;
			
			case 301://Changement de sexe
				int sexe = 0;
				if(perso.getSexe() == 0) sexe = 1;
				if(perso.getSexe() == 1) sexe = 0;
				if(!perso.getRestrictions().isTombe()) {
					perso.setGfxID(perso.getBreedId()*10+sexe);
					SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getActorId());
					SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(), perso);
					SQLManager.SAVE_PERSONNAGE(perso, false);
				}
				break;
				
			case 302://changement de classe :: à tester
				final int classe = Integer.parseInt(args);
				if(classe != perso.getBreedId()) {
					perso.setBreedId(classe);
					perso.setGfxID(perso.getBreedId()*10+perso.getSexe());
					perso.getSpells().clear();
					perso.getSpellPlaces().clear();
					SocketManager.GAME_SEND_SPELL_LIST(perso);
					perso.learnBreedSpell();
					SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(perso.getCurMap(), perso.getActorId());
					SocketManager.GAME_SEND_ADD_PLAYER_TO_MAP(perso.getCurMap(), perso);
					SQLManager.SAVE_PERSONNAGE(perso, false);
				}
				break;
				
			case 303://Change de nom
				perso.changePseudo(true);
				SocketManager.GAME_SEND_CHOOSE_PSEUDO(perso.getAccount().getGameThread().getOut());
				break;
				
			case 304://Changer de couleur
				final int color1 = Integer.parseInt(args.split(",")[0]);
				final int color2 = Integer.parseInt(args.split(",")[1]);
				final int color3 = Integer.parseInt(args.split(",")[2]);
				perso.setColors(color1, color2, color3);
				SocketManager.GAME_SEND_ALTER_GM_PACKET(perso.getCurMap(), perso);
				break;
				
			default:
				Log.addToErrorLog("Action ID="+ID+" non implantée");
			break;
		}
	}


	public int getID()
	{
		return ID;
	}
}

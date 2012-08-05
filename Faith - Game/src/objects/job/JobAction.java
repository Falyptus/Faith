package objects.job;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.Timer;

import objects.action.GameAction;
import objects.character.Player;
import objects.item.Item;
import objects.item.ItemTemplate;
import objects.map.DofusCell;
import objects.map.InteractiveObject;
import objects.monster.MonsterGroup;
import objects.spell.SpellEffect;

import common.Config;
import common.Constants;
import common.SocketManager;
import common.World;
import common.console.Log;
import common.utils.Formulas;

public class JobAction
{
	private final int _skID;
	private int _min = 1;
	private int _max = 1;
	private final boolean _isCraft;
	private int _chan = 100;
	private int _time = 0;
	private int _xpWin = 0;
	private long _startTime;
	private final Map<Integer,Integer> _ingredients = new TreeMap<Integer,Integer>();
	private final Map<Integer,Integer> _lastCraft = new TreeMap<Integer,Integer>();
	private final Timer _craftTimer;
	private Player _P;
	
	public JobAction(final int sk,final int min, final int max,final boolean craft, final int arg,final int xpWin)
	{
		_skID = sk;
		_min = min;
		_max = max;
		_isCraft = craft;
		if(craft)_chan = arg;
		else _time = arg;
		_xpWin = xpWin;
		
		_craftTimer = new Timer(100,new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				craft();
				_craftTimer.stop();
			}
		});
	}
	
	public void endAction(final Player P, final InteractiveObject IO, final GameAction GA,final DofusCell cell)
	{
		if(!_isCraft)
		{
			//Si recue trop tot, on ignore
			if(_startTime - System.currentTimeMillis() > 500)return;
			IO.setState(Constants.IOBJECT_STATE_EMPTY);
			IO.startTimer();
			//Packet GDF (changement d'état de l'IO)
			SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
			
			final boolean special = Formulas.getRandomValue(0, 99)==0;//Restriction de niveau ou pas ?
			
			//On ajoute X ressources
			final int qua = (_max>_min?Formulas.getRandomValue(_min, _max):_min);
			final int tID = Constants.getObjectByJobSkill(_skID,special);
							
			final ItemTemplate T = World.getItemTemplate(tID);
			if(T == null)return;
			final Item O = T.createNewItem(qua, false);
			if(P.getPodUsed() + (O.getTemplate().getPod() * O.getQuantity()) > P.getMaxPod()) {
				SocketManager.GAME_SEND_Im_PACKET(P, "144");
				return;
			}
			//Si retourne true, on l'ajoute au monde
			if(P.addItem(O, true))
				World.addItem(O, true);
			P.itemLog(O.getTemplate().getID(), O.getQuantity(), "Crafté");
			
			SocketManager.GAME_SEND_IQ_PACKET(P,P.getActorId(),qua);
			SocketManager.GAME_SEND_Ow_PACKET(P);
			
			final int maxPercent = 20+(P.getMetierBySkill(_skID).getLvl()-20);//40(fixe)+(lvl metier - 20)
			if(P.getMetierBySkill(_skID).getLvl() >= 20 && Formulas.getRandomValue(1, maxPercent) == maxPercent)
			{
				final int[][] protectors = Constants.JOB_PROTECTORS;
				for(int i = 0; i < protectors.length; i++)
				{
					if(tID == protectors[i][1])
					{
						final int monsterId = protectors[i][0];
						final int monsterLvl = Formulas.getProtectorLvl(P.getLvl());		
						P.getCurMap().startFightVersusMonstres(P, new MonsterGroup(P.getCurMap()._nextObjectID, cell.getId(), monsterId+","+monsterLvl+","+monsterLvl));
						break;
					}
				}
			}
		}
	}

	public void startAction(final Player P, final InteractiveObject IO, final GameAction GA,final DofusCell cell)
	{
		_P = P;
		if(!_isCraft)
		{
			IO.setInteractive(false);
			IO.setState(Constants.IOBJECT_STATE_EMPTYING);
			SocketManager.GAME_SEND_GA_PACKET_TO_MAP(P.getCurMap(),""+GA._id, 501, P.getActorId()+"", cell.getId()+","+_time);
			SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(),cell);
			_startTime = System.currentTimeMillis()+_time;//pour eviter le cheat
		}else
		{
			P.setAway(true);
			IO.setState(Constants.IOBJECT_STATE_EMPTYING);//FIXME trouver la bonne valeur
			P.setCurJobAction(this);
			SocketManager.GAME_SEND_ECK_PACKET(P, 3, _min+";"+_skID);//_min => Nbr de Case de l'interface
			SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(P.getCurMap(), cell);
		}
	}

	public int getSkillID()
	{
		return _skID;
	}
	public int getMin()
	{
		return _min;
	}
	public int getXpWin()
	{
		return _xpWin;
	}
	public int getMax()
	{
		return _max;
	}
	public int getChance()
	{
		return _chan;
	}
	public int getTime()
	{
		return _time;
	}
	public boolean isCraft()
	{
		return _isCraft;
	}
	
	public void modifIngredient(final Player P,final int guid, final int qua)
	{
		//on prend l'ancienne valeur
		int q = _ingredients.get(guid)==null?0:_ingredients.get(guid);
		//on enleve l'entrée dans la Map
		_ingredients.remove(guid);
		//on ajoute (ou retire, en fct du signe) X objet
		q += qua;
		if(q > 0)
		{
			_ingredients.put(guid,q);
			SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P,'O', "+", guid+"|"+q);
		}else SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(P,'O', "-", guid+"");
	}

	public void craft()
	{
		if(!_isCraft)return;
		boolean signed = false;//TODO
		try
		{
			Thread.sleep(750);
		}catch(final Exception e){};
		//Si Forgemagie
		if(_skID == 1
		|| _skID == 113
		|| _skID == 115
		|| _skID == 116
		|| _skID == 117
		|| _skID == 118
		|| _skID == 119
		|| _skID == 120
		|| (_skID >= 163 && _skID <= 169))
		{
			doFmCraft();
			return;
		}
		
		final Map<Integer,Integer> items = new TreeMap<Integer,Integer>();
		//on retire les items mis en ingrédients
		for(final Entry<Integer,Integer> e : _ingredients.entrySet())
		{
			//Si le joueur n'a pas l'objet
			if(!_P.hasItemGuid(e.getKey()))
			{
				SocketManager.GAME_SEND_Ec_PACKET(_P,"EI");
				Log.addToLog("/!\\ "+_P.getName()+" essaye de crafter avec un objet qu'il n'a pas");
				return;
			}
			//Si l'objet n'existe pas
			final Item obj = World.getObjet(e.getKey());
			if(obj == null)
			{
				SocketManager.GAME_SEND_Ec_PACKET(_P,"EI");
				Log.addToLog("/!\\ "+_P.getName()+" essaye de crafter avec un objet qui n'existe pas");
				return;
			}
			//Si la quantité est trop faible
			if(obj.getQuantity() < e.getValue())
			{
				SocketManager.GAME_SEND_Ec_PACKET(_P,"EI");
				Log.addToLog("/!\\ "+_P.getName()+" essaye de crafter avec un objet dont la quantité est trop faible");
				return;
			}
			//On calcule la nouvelle quantité
			final int newQua = obj.getQuantity() - e.getValue();
			
			if(newQua <0)return;//ne devrais pas arriver
			if(newQua == 0)
			{
				_P.removeItem(e.getKey());
				World.removeItem(e.getKey());
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, e.getKey());
			}else
			{
				obj.setQuantity(newQua);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, obj);
			}
			//on ajoute le couple tID/qua a la liste des ingrédients pour la recherche
			items.put(obj.getTemplate().getID(), e.getValue());
		}
		//On retire les items a ignorer pour la recette
		//Rune de signature
		if(items.containsKey(7508))signed = true;
		items.remove(7508);
		//Fin des items a retirer
		SocketManager.GAME_SEND_Ow_PACKET(_P);
		
		//On trouve le template corespondant si existant
		final JobStat SM = _P.getMetierBySkill(_skID);
		final int tID = World.getObjectByIngredientForJob(SM.getTemplate().getListBySkill(_skID),items);
		
		//Recette non existante ou pas adapté au métier
		if(tID == -1 || !SM.getTemplate().canCraft(_skID, tID))
		{
			SocketManager.GAME_SEND_Ec_PACKET(_P,"EI");
			SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.getCurMap(),_P.getActorId(),"-");
			_ingredients.clear();
			
			return;
		}
		
		final int chan =  Constants.getChanceByNbrCaseByLvl(SM.getLvl(),_ingredients.size());
		final int jet = Formulas.getRandomValue(1, 100);
		final boolean success = chan >= jet;
		
		if(!success)//Si echec
		{
			SocketManager.GAME_SEND_Ec_PACKET(_P,"EF");
			SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.getCurMap(),_P.getActorId(),"-"+tID);
			SocketManager.GAME_SEND_Im_PACKET(_P, "0118");
		}else
		{
			final Item newObj = World.getItemTemplate(tID).createNewItem(1, false);
			//Si signé on ajoute la ligne de Stat "Fabriqué par:"
			if(signed)newObj.addTxtStat(988, _P.getName());
			boolean add = true;
			int guid = newObj.getGuid();
			
			for(final Entry<Integer,Item> entry : _P.getItems().entrySet())
			{
				final Item obj = entry.getValue();
				if(obj.getTemplate().getID() == newObj.getTemplate().getID()
					&& obj.getStats().isSameStats(newObj.getStats())
					&& obj.getPosition() == Constants.ITEM_POS_NO_EQUIPED)//Si meme Template et Memes Stats et Objet non équipé
				{
					obj.setQuantity(obj.getQuantity()+newObj.getQuantity());//On ajoute QUA item a la quantité de l'objet existant
					SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P,obj);
					add = false;
					guid = obj.getGuid();
				}
			}
			if(add)
			{
				_P.getItems().put(newObj.getGuid(), newObj);
				SocketManager.GAME_SEND_OAKO_PACKET(_P,newObj);
				World.addItem(newObj, true);
			}
			
			//on envoie les Packets
			SocketManager.GAME_SEND_Ow_PACKET(_P);
			SocketManager.GAME_SEND_Em_PACKET(_P,"KO+"+guid+"|1|"+tID+"|"+newObj.parseStatsString().replace(";","#"));
			SocketManager.GAME_SEND_Ec_PACKET(_P,"K;"+tID);
			SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.getCurMap(),_P.getActorId(),"+"+tID);
		}
		
		
		//On donne l'xp
		final int winXP =  Constants.calculXpWinCraft(SM.getLvl(),_ingredients.size()) * Config.XP_METIER;
		if(success)
		{
			SM.addXp(_P,winXP);
			final ArrayList<JobStat> SMs = new ArrayList<JobStat>();
			SMs.add(SM);
			SocketManager.GAME_SEND_JX_PACKET(_P, SMs);
		}
		
		_lastCraft.clear();
		_lastCraft.putAll(_ingredients);
		_ingredients.clear();
		//*/
	}
	
	private void doFmCraft()
	{
		boolean signed = false;
		Item obj = null,sign = null,mod = null;// sign = Rune de signature, mod: rune ou Potion, obj : objet modifé
		int isElementChanging = 0,stat = -1;
		for(final int guid : _ingredients.keySet())
		{
			final Item ing = World.getObjet(guid);
			if(!_P.hasItemGuid(guid) || ing == null)
			{
				SocketManager.GAME_SEND_Ec_PACKET(_P,"EI");
				SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.getCurMap(),_P.getActorId(),"-");
				_ingredients.clear();
				return;
			}
			final int id =ing.getTemplate().getID();
			switch(id)
			{
			//Potions
			case 1333://Potion Etincelle
				stat = 99; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1335://Potion crachin
				stat = 96; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1337://Potion de courant d'air
				stat = 98; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1338://Potion de secousse
				stat = 97; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1340://Potion d'eboulement
				stat = 97; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1341://Potion Averse
				stat = 96; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1342://Potion de rafale
				stat = 98; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1343://Potion de Flambée
				stat = 99; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1345://Potion Incendie
				stat = 99; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1346://Potion Tsunami
				stat = 96; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1347://Potion Ouragan
				stat = 98; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			case 1348://Potion de seisme
				stat = 97; 
				isElementChanging = ing.getTemplate().getLevel();
				mod = ing;
			break;
			//Fin potions
			
			case 7508://Rune de signature
				signed = true;
				sign = ing;
			break;
			default://Si pas runes ou popo, et qu'il a un cout en PA, alors c'est une arme (une vérification du type serait préférable)
				if(ing.getTemplate().getPACost()>0)obj = ing;
			break;
			}
		}
		final JobStat SM = _P.getMetierBySkill(_skID);
		
		if(SM == null || obj == null || mod == null)
		{
			SocketManager.GAME_SEND_Ec_PACKET(_P,"EI");
			SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.getCurMap(),_P.getActorId(),"-");
			_ingredients.clear();
			return;
		}
		int chan = 0;
		
		//* DEBUG
		System.out.println("ElmChg: "+isElementChanging);
		System.out.println("LevelM: "+SM.getLvl());
		System.out.println("LevelA: "+obj.getTemplate().getLevel());
		///*/
		
		//Si changement d'élément
		if(isElementChanging > 0)chan = Formulas.calculElementChangeChance(SM.getLvl(), obj.getTemplate().getLevel(), isElementChanging);
		//else TODO;
		
		//Min/max de 5% /95%
		if(chan > 100-(SM.getLvl()/20))chan =100-(SM.getLvl()/20);
		if(chan < (SM.getLvl()/20))chan = (SM.getLvl()/20);
		
		System.out.println("Chance: "+chan);
		
		final int jet = Formulas.getRandomValue(1, 100);
		final boolean success = chan >= jet;
		final int tID = obj.getTemplate().getID();
		if(!success)//Si echec
		{
			//FIXME
			SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, obj.getGuid());//Supprime l'ancien affichage de l'item
			SocketManager.GAME_SEND_OAKO_PACKET(_P, obj);
			SocketManager.GAME_SEND_Em_PACKET(_P,"EO+"+_P.getActorId()+"|1|"+tID+"|"+obj.parseStatsString().replace(";","#"));
			SocketManager.GAME_SEND_Ec_PACKET(_P,"EF");
			SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.getCurMap(),_P.getActorId(),"-"+tID);
			SocketManager.GAME_SEND_Im_PACKET(_P, "0117");
			SocketManager.GAME_SEND_Ow_PACKET(_P);
		}else
		{
			int coef = 50;
			if(isElementChanging == 25)coef = 65;
			if(isElementChanging == 50)coef = 85;
			//Si signé on ajoute la ligne de Stat "Modifié par: "
			if(signed)obj.addTxtStat(985, _P.getName());
			
			for(final SpellEffect SE : obj.getEffects())
			{
				//Si pas un effet Dom Neutre, on continue
				if(SE.getEffectID() != 100)continue;
				final String[] infos = SE.getArgs().split(";");
				try
				{
					//on calcule les nouvelles stats
					final int min = Integer.parseInt(infos[0],16);
					final int max = Integer.parseInt(infos[1],16);
					final int newMin = (int)((min * coef) /100);
					final int newMax = (int)((max * coef) /100);
					
					final String newJet = "1d"+(newMax-newMin+1)+"+"+(newMin-1);
					final String newArgs = Integer.toHexString(newMin)+";"+Integer.toHexString(newMax)+";-1;-1;0;"+newJet;
					
					SE.setArgs(newArgs);//on modifie les propriétés du SpellEffect
					SE.setEffectID(stat);//On change l'élement d'attaque
					
				}catch(final Exception e){e.printStackTrace();};
			}
			//On envoie les packets
			SocketManager.GAME_SEND_Ow_PACKET(_P);
			SocketManager.GAME_SEND_Em_PACKET(_P,"KO+"+_P.getActorId()+"|1|"+tID+"|"+obj.parseStatsString().replace(";","#"));
			SocketManager.GAME_SEND_Ec_PACKET(_P,"K;"+tID);
			SocketManager.GAME_SEND_IO_PACKET_TO_MAP(_P.getCurMap(),_P.getActorId(),"+"+tID);
		}
		//On consumme les runes
		//Rune de signature si diff de null
		if(sign != null)
		{
			final int newQua = sign.getQuantity() -1;
			//S'il ne reste rien
			if(newQua <= 0)
			{
				_P.removeItem(sign.getGuid());
				World.removeItem(sign.getGuid());
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, sign.getGuid());
			}else
			{
				sign.setQuantity(newQua);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, sign);
			}
		}
		//Objet modificateur
		if(mod != null)
		{
			final int newQua = mod.getQuantity() -1;
			//S'il ne reste rien
			if(newQua <= 0)
			{
				_P.removeItem(mod.getGuid());
				World.removeItem(mod.getGuid());
				SocketManager.GAME_SEND_REMOVE_ITEM_PACKET(_P, mod.getGuid());
			}else
			{
				mod.setQuantity(newQua);
				SocketManager.GAME_SEND_OBJECT_QUANTITY_PACKET(_P, mod);
			}
		}
		//fin
		
		//On sauve le dernier craft
		_lastCraft.clear();
		_lastCraft.putAll(_ingredients);
		_ingredients.clear();
	}

	public void repeat(final int time,final Player P)
	{
		_craftTimer.stop();
		// /!\ Time = Nombre Réel -1
		_lastCraft.clear();
		_lastCraft.putAll(_ingredients);
		for(int a = time; a>=0;a--)
		{
			SocketManager.GAME_SEND_EA_PACKET(P,a+"");
			_ingredients.clear();
			_ingredients.putAll(_lastCraft);
			craft();
		}
		SocketManager.GAME_SEND_Ea_PACKET(P, "1");
	}

	public void startCraft(final Player P)
	{
		//on retarde le lancement du craft en cas de packet EMR (craft auto)
		_craftTimer.start();
	}

	public void putLastCraftIngredients()
	{
		if(_P == null)return;
		if(_lastCraft == null)return;
		if(_ingredients.size() != 0)return;//OffiLike, mais possible de faire un truc plus propre en enlevant les objets présent et en rajoutant ceux de la recette
		_ingredients.clear();
		_ingredients.putAll(_lastCraft);
		for(final Entry<Integer,Integer> e : _ingredients.entrySet())
		{
			if(World.getObjet(e.getKey()) == null)return;
			if(World.getObjet(e.getKey()).getQuantity() < e.getValue())return;
			SocketManager.GAME_SEND_EXCHANGE_MOVE_OK(_P,'O', "+", e.getKey()+"|"+e.getValue());
		}
	}

	public void resetCraft()
	{
		_ingredients.clear();
		_lastCraft.clear();
	}
}
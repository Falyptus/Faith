package objects.map;


import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import objects.action.Action;
import objects.action.GameAction;
import objects.character.Player;
import objects.fight.Fighter;
import objects.item.Item;

import common.Constants;
import common.SocketManager;
import common.World;
import common.console.Log;
import common.utils.Formulas;
import common.utils.Pathfinding;

public class DofusCell
{
	private int _id;
	private Map<Integer, Player>	_persos;//		= new TreeMap<Integer, Personnage>();
	private Map<Integer, Fighter> 		_fighters;//	= new TreeMap<Integer, Fighter>();
	private boolean _Walkable = true;
	private boolean _LoS = true;
	private int _map;
	//private ArrayList<Action> _onCellPass;
	//private ArrayList<Action> _onItemOnCell;
	private ArrayList<Action> _onCellStop;// = new ArrayList<Action>();
	private InteractiveObject _object;
	private Item _droppedItem;
	
	public DofusCell()
	{
		
	}
	
	public DofusCell(final DofusMap a_map,final int id,final boolean _walk,final boolean LoS, final int objID)
	{
		_map = a_map.getId();
		_id = id;
		_Walkable = _walk;
		_LoS = LoS;
		if(objID == -1)return;
		_object = new InteractiveObject(a_map,this,objID);
	}
	
	public InteractiveObject getObject()
	{
		return _object;
	}
	public synchronized Item getDroppedItem()
	{
		return _droppedItem;
	}
	public boolean canDoAction(final int id)
	{
		switch(id)
		{
			//Moudre et egrenner - Paysan
			case 122:
			case 47:
				return _object.getID() == 7007;
			//Faucher Blé
			case 45:
				switch(_object.getID())
				{
					case 7511://Blé
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Faucher Orge
			case 53:
				switch(_object.getID())
				{
					case 7515://Orge
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			
			//Faucher Avoine
			case 57:
				switch(_object.getID())
				{
					case 7517://Avoine
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;	
			//Faucher Houblon
			case 46:
				switch(_object.getID())
				{
					case 7512://Houblon
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Faucher Lin
			case 50:
			case 68:
				switch(_object.getID())
				{
					case 7513://Lin
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Faucher Riz
			case 159:
				switch(_object.getID())
				{
					case 7550://Riz
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Faucher Seigle
			case 52:
				switch(_object.getID())
				{
					case 7516://Seigle
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Faucher Malt
			case 58:
				switch(_object.getID())
				{
					case 7518://Malt
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;			
			//Faucher Chanvre - Cueillir Chanvre
			case 69:
			case 54:
				switch(_object.getID())
				{
					case 7514://Chanvre
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Scier - Bucheron
			case 101:
				return _object.getID() == 7003;
			//Couper Frêne
			case 6:
				switch(_object.getID())
				{
					case 7500://Frêne
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Châtaignier
			case 39:
				switch(_object.getID())
				{
					case 7501://Châtaignier
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Noyer
			case 40:
				switch(_object.getID())
				{
					case 7502://Noyer
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Chêne
			case 10:
				switch(_object.getID())
				{
					case 7503://Chêne
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Oliviolet
			case 141:
				switch(_object.getID())
				{
					case 7542://Oliviolet
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Bombu
			case 139:
				switch(_object.getID())
				{
					case 7541://Bombu
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Erable
			case 37:
				switch(_object.getID())
				{
					case 7504://Erable
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Bambou
			case 154:
				switch(_object.getID())
				{
					case 7553://Bambou
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper If
			case 33:
				switch(_object.getID())
				{
					case 7505://If
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Merisier
			case 41:
				switch(_object.getID())
				{
					case 7506://Merisier
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Ebène
			case 34:
				switch(_object.getID())
				{
					case 7507://Ebène
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Kalyptus
			case 174:
				switch(_object.getID())
				{
					case 7557://Kalyptus
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Charme
			case 38:
				switch(_object.getID())
				{
					case 7508://Charme
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Orme
			case 35:
				switch(_object.getID())
				{
					case 7509://Orme
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Bambou Sombre
			case 155:
				switch(_object.getID())
				{
					case 7554://Bambou Sombre
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Couper Bambou Sacré
			case 158:
				switch(_object.getID())
				{
					case 7552://Bambou Sacré
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Puiser
			case 102:
				switch(_object.getID())
				{
					case 7519://Puits
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Polir
			case 48:
				return _object.getID() == 7510;
			//Moule/Fondre - Mineur
			case 32:
				return _object.getID() == 7002;
			//Miner Fer
			case 24:
				switch(_object.getID())
				{
					case 7520://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Miner Cuivre
			case 25:
				switch(_object.getID())
				{
					case 7522://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Miner Bronze
			case 26:
				switch(_object.getID())
				{
					case 7523://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Miner Kobalte
			case 28:
				switch(_object.getID())
				{
					case 7525://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Miner Manga
			case 56:
				switch(_object.getID())
				{
					case 7524://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Miner Sili
			case 162:
				switch(_object.getID())
				{
					case 7556://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Miner Etain
			case 55:
				switch(_object.getID())
				{
					case 7521://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Miner Argent
			case 29:
				switch(_object.getID())
				{
					case 7526://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Miner Bauxite
			case 31:
				switch(_object.getID())
				{
					case 7528://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Miner Or
			case 30:
				switch(_object.getID())
				{
					case 7527://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Miner Dolomite
			case 161:
				switch(_object.getID())
				{
					case 7555://Miner
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Fabriquer potion - Alchimiste
			case 23:
				return _object.getID() == 7019;
			//Cueillir Trèfle
			case 71:
				switch(_object.getID())
				{
					case 7533://Trèfle
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Cueillir Menthe
			case 72:
				switch(_object.getID())
				{
					case 7534://Menthe
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Cueillir Orchidée
			case 73:
				switch(_object.getID())
				{
					case 7535:// Orchidée
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Cueillir Edelweiss
			case 74:
				switch(_object.getID())
				{
					case 7536://Edelweiss
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Cueillir Graine de Pandouille
			case 160:
				switch(_object.getID())
				{
					case 7551://Graine de Pandouille
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Vider - Pêcheur
			case 133:
				return _object.getID() == 7024;
			//Pêcher Petits poissons de mer
			case 128:
				switch(_object.getID())
				{
					case 7530://Petits poissons de mer
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Pêcher Petits poissons de rivière
			case 124:
				switch(_object.getID())
				{
					case 7529://Petits poissons de rivière
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Pêcher Pichon
			case 136:
				switch(_object.getID())
				{
					case 7544://Pichon
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Pêcher Ombre Etrange
			case 140:
				switch(_object.getID())
				{
					case 7543://Ombre Etrange
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Pêcher Poissons de rivière
			case 125:
				switch(_object.getID())
				{
					case 7532://Poissons de rivière
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Pêcher Poissons de mer
			case 129:
				switch(_object.getID())
				{
					case 7531://Poissons de mer
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Pêcher Gros poissons de rivière
			case 126:
				switch(_object.getID())
				{
					case 7537://Gros poissons de rivière
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Pêcher Gros poissons de mers
			case 130:
				switch(_object.getID())
				{
					case 7538://Gros poissons de mers
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Pêcher Poissons géants de rivière
			case 127:
				switch(_object.getID())
				{
					case 7539://Poissons géants de rivière
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Pêcher Poissons géants de mer
			case 131:
				switch(_object.getID())
				{
					case 7540://Poissons géants de mer
						return _object.getState() == Constants.IOBJECT_STATE_FULL;
				}
			return false;
			//Boulanger
			case 109://Pain
			case 27://Bonbon
				return _object.getID() == 7001;
			//Poissonier
			case 135://Faire un poisson (mangeable)
				return _object.getID() == 7022;
			//Chasseur
			case 134://
				return _object.getID() == 7023;
			//Boucher
			case 132://
				return _object.getID() == 7025;
			case 44://Sauvegarder
			case 114://Utiliser le Zaap
				switch(_object.getID())
				{
					//Zaaps
					case 7000:
					case 7026:
					case 7029:
					case 4287:
						return true;
				}
			return false;
			case 157://Utiliser le Zaapi
				switch(_object.getID())
				{
					//Zaaps
					case 7031:
					case 7030:
						return true;
				}
			return false;
			
			case 175://Accéder
			case 176://Acheter
			case 177://Vendre
			case 178://Modifier le prix de vente
				switch(_object.getID())
				{
					//Enclos
					case 6763:
					case 6766:
					case 6767:
					case 6772:
						return true;
				}
			return false;
			
			//Se rendre à incarnam
			case 183:
				switch(_object.getID())
				{
					case 1845:
					case 1853:
					case 1854:
					case 1855:
					case 1856:
					case 1857:
					case 1858:
					case 1859:
					case 1860:
					case 1861:
					case 1862:
					case 2319:
						return true;
				}
			return false;
			
			//Enclume magique
			case  1:
			case 113:
			case 115:
			case 116:
			case 117:
			case 118:
			case 119:
			case 120:
				return _object.getID() == 7020;

			//Enclume
			case 19:
			case 143:
			case 145:
			case 144:
			case 142:
			case 146:
			case 67:
			case 21:
			case 65:
			case 66:
			case 20:
			case 18:
				return _object.getID() == 7012;

			//Costume Mage
			case 167:
			case 165:
			case 166:
				return _object.getID() == 7036;

			//Coordo Mage
			case 164:
			case 163:
				return _object.getID() == 7037;

			//Joai Mage
			case 168:
			case 169:
				return _object.getID() == 7038;

			//Bricoleur
			case 171:
			case 182:
				return _object.getID() == 7039;

			//Forgeur Bouclier
			case 156:
				return _object.getID() == 7027;

			//Coordonier
			case 13:
			case 14:
				return _object.getID() == 7011;

			//Tailleur (Dos)
			case 123:
			case 64:
				return _object.getID() == 7015;


			//Sculteur
			case 17:
			case 16:
			case 147:
			case 148:
			case 149:
			case 15:
				return _object.getID() == 7013;

			//Tailleur (Haut)
			case 63:
				return (_object.getID() == 7014 || _object.getID() == 7016);
			//Atelier : Créer Amu // Anneau
			case 11:
			case 12:
				return (_object.getID() >= 7008 && _object.getID() <= 7010);

			//Action ID non trouvé
			default:
				Log.addToErrorLog("MapActionID non existant dans Case.canDoAction: "+id);
				return false;
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void addOnCellStopAction(final int id, final String args, final String cond)
	{
		if(_onCellStop == null)
			_onCellStop = new ArrayList<Action>();
		
		_onCellStop.add(new Action(id,args,cond));
	}
	
	public void applyOnCellStopActions(final Player perso)
	{
		if(_onCellStop == null)
			return;
		
		for(final Action act : _onCellStop)
		{
			act.apply(perso,-1);
		}
	}
	public synchronized void addPerso(final Player perso)
	{
		if(_persos == null)
			_persos = new TreeMap<Integer, Player>();
		
		_persos.put(perso.getActorId(),perso);

	}
	public synchronized void addFighter(final Fighter fighter)
	{
		if(_fighters == null)
			_fighters = new TreeMap<Integer, Fighter>();
		
		_fighters.put(fighter.getGUID(),fighter);
	}
	public boolean isWalkable(final boolean useObject)
	{
		if(_object != null && useObject)return _Walkable && _object.isWalkable();
		return _Walkable;
	}
	public boolean blockLoS()
	{
		if(_fighters == null)
			return _LoS;
		boolean fighter = true;
		for(final Entry<Integer,Fighter> f : _fighters.entrySet())
		{
			if(!f.getValue().isHide())fighter = false;
		}
		return _LoS && fighter;
	}
	public boolean isLoS()
	{
		return _LoS;
	}
	public synchronized void removePlayer(final int _guid)
	{
		if(_persos == null)
			return;
		
		if(_persos.containsKey(_guid))
			_persos.remove(_guid);
		
		if(_persos.size() == 0)
			_persos = null;
	}
	public synchronized Map<Integer, Player> getPersos()
	{
		if(_persos == null)
			return new TreeMap<Integer, Player>();
		
		return _persos;
	}
	public synchronized void removeFighter(int guid)
	{
		if(_fighters == null)
			return;
		
		if(_fighters.containsKey(guid))
			_fighters.remove(guid);
		
		if(_fighters.size() == 0)
			_fighters = null;
	}
	public synchronized Map<Integer, Fighter> getFighters()
	{
		if(_fighters == null)
			return new TreeMap<Integer, Fighter>();
		
		return _fighters;
	}
	public Fighter getFirstFighter()
	{
		if(_fighters == null)
			return null;
		
		for(final Entry<Integer,Fighter> entry : _fighters.entrySet())
		{
			return entry.getValue();
		}
		return null;
	}

	public void startAction(final Player player, final GameAction GA)
	{
		int actionID = -1;
		short cellID = -1;
		try
		{
			cellID = Short.parseShort(GA._args.split(";")[0]);
			actionID = Integer.parseInt(GA._args.split(";")[1]);
		}catch(final Exception e){e.printStackTrace();}
		if(actionID == -1)return;
		
		if(Constants.isJobAction(actionID))
		{
			player.doJobAction(actionID,_object,GA,this);
			return;
		}
		switch(actionID)
		{
			case 44://Sauvegarder pos			
			case 102://Puiser
			case 114://Utiliser (zaap)
			case 157://Utiliser Zaapi
			case 175://Acceder a un enclos
			case 183://Retourner sur Incarnam
				player.setCurGameAction(GA);
				final int Dist = Pathfinding.getDistanceBetween(player.getCurMap(), cellID, player.getCurCell().getId());
				if (Dist < 2)
					makeAction(player);
				break;
			
			default:
				Log.addToErrorLog("Case.startAction non définie pour l'actionID = "+actionID);
			break;
		}
	}
	public void makeAction(final Player player)
	{
		final GameAction GA = player.getCurGameAction();
		if (GA == null)
			return;

		int actionId = -1;
		short CcellId = -1;
		try {
			actionId = Integer.parseInt(GA._args.split(";")[1]);
			CcellId = Short.parseShort(GA._args.split(";")[0]);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		if (actionId == -1)
			return;
		final int Dist = Pathfinding.getDistanceBetween(player.getCurMap(), CcellId, player.getCurCell().getId());
		if (Dist > 2) {
			player.setCurGameAction(null);
			return;
		}
		switch(actionId)
		{
			case 44://Sauvegarder pos
				final String str = _map+","+_id;
				player.setSavePos(str);
				SocketManager.GAME_SEND_Im_PACKET(player, "06");
			break;
		
			case 102://Puiser
				if(!_object.isInteractive())return;//Si l'objet est utilisé
				if(_object.getState() != Constants.IOBJECT_STATE_FULL)return;//Si le puits est vide
				_object.setState(Constants.IOBJECT_STATE_EMPTYING);
				_object.setInteractive(false);
				SocketManager.GAME_SEND_GA_PACKET_TO_MAP(player.getCurMap(),""+GA._id, 501, player.getActorId()+"", _id+","+_object.getUseDuration()+","+_object.getUnknowValue());
				SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(player.getCurMap(),this);
			break;
			case 114://Utiliser (zaap)
				player.openZaapMenu();
				player.getAccount().getGameThread().removeAction(GA);
			break;
			case 157://Utiliser Zaapi
				player.openZaapiMenu();
				player.getAccount().getGameThread().removeAction(GA);
			break;
			case 175://Acceder a un enclos
				if(_object.getState() != Constants.IOBJECT_STATE_EMPTY);
				//SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(perso.get_curCarte(),this);
				player.openMountPark();
			break;
			
			case 183://Retourner sur Incarnam
				if(player.getLvl()>15)
				{
					SocketManager.GAME_SEND_Im_PACKET(player, "1127");
					player.getAccount().getGameThread().removeAction(GA);
					return;
				}
				final int mapID  = Constants.getStartMap(player.getBreedId());
				final int cellID = Constants.getStartCell(player.getBreedId());
				player.teleport(mapID, cellID);
				player.getAccount().getGameThread().removeAction(GA);
			break;
			
			default:
				Log.addToErrorLog("Case.startAction non définie pour l'actionID = "+actionId);
			break;
		}
		player.setCurGameAction(null);
	}
	public void finishAction(final Player perso, final GameAction GA)
	{
		int actionID = -1;
		try
		{
			actionID = Integer.parseInt(GA._args.split(";")[1]);
		}catch(final Exception e){}
		if(actionID == -1)return;
		
		if(Constants.isJobAction(actionID))
		{
			perso.finishJobAction(actionID,_object,GA,this);
			return;
		}
		switch(actionID)
		{
			case 44://Sauvegarder a un zaap
			break;
			
			case 102://Puiser
				_object.setState(Constants.IOBJECT_STATE_EMPTY);
				_object.setInteractive(false);
				_object.startTimer();
				SocketManager.GAME_SEND_GDF_PACKET_TO_MAP(perso.getCurMap(),this);
				final int qua = Formulas.getRandomValue(1, 10);//On a entre 1 et 10 eaux
				final Item obj = World.getItemTemplate(311).createNewItem(qua, false);
				if(perso.addItem(obj, true))
					World.addItem(obj,true);
				SocketManager.GAME_SEND_IQ_PACKET(perso,perso.getActorId(),qua);
			break;
			
			case 183:
			break;
			
			default:
				Log.addToErrorLog("[FIXME]Case.finishAction non définie pour l'actionID = "+actionID);
			break;
		}
	}

	public void clearOnCellAction()
	{
		//_onCellStop.clear();
		_onCellStop = null;
	}

	public void addDroppedItem(final Item obj)
	{
		_droppedItem = obj;
	}

	public void clearDroppedItem()
	{
		_droppedItem = null;
	}

	public boolean isTrigger() 
	{
		if(_onCellStop != null) 
		{
			for(final Action act : _onCellStop) 
			{
				if(act.getID() == 0) //S'il y a une action de téléportation 
				{ 
					return true;
				}
			}
		}
        return false;
    }
	
	public void setId(final int id) {
		_id = id;
	}
	
	public void setMap(final int map) {
		_map = map;
	}

	public void setWalkable(final boolean isWalkable) {
		_Walkable = isWalkable;
	}

	public void setSightBlocker(final boolean isSightBlocker) {
		_LoS = isSightBlocker;
	}

	public boolean isEmpty() {
		if(getObject() != null)
			return false;
		if(getPersos().size() > 0)
			return false;
		return true;
	}
}
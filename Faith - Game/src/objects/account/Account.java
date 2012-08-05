package objects.account;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.Timer;

import objects.bigstore.BigStoreEntry;
import objects.character.Mount;
import objects.character.Player;
import objects.fight.Fight;
import objects.item.Gift;
import objects.item.Item;
import server.client.GameThread;

import common.Config;
import common.Constants;
import common.SQLManager;
import common.SocketManager;
import common.World;
import common.utils.CryptManager;

public class Account {

	private int _GUID;
	private String _name;
	private String _pass;
	private String _pseudo;
	private String _key;
	private String _lastIP = "";
	private String _question;
	private String _response;
	private boolean _banned = false;
	private int _gmLvl = 0;
	private String _curIP = "";
	private String _lastConnectionDate = "";
	private GameThread _gameThread;
	private Player _curPerso;
	private final ArrayList<Integer> _friendGuids = new ArrayList<Integer>();
	private final ArrayList<Integer> _enemyGuids = new ArrayList<Integer>();
	private final FriendList friendList;
	private final EnemyList enemyList;
	private final Bank bank;
	private final ArrayList<Mount> _stable = new ArrayList<Mount>();
	private boolean _mute = false;
	private Timer _muteTimer;
	private final Map<Integer,ArrayList<BigStoreEntry>> _hdvsItems;	// Contient les items des HDV format : <hdvID,<cheapestID>>
	// Gifts (cadeaux)
	Map<Integer, Gift> _gifts = new TreeMap<Integer, Gift>();
	
	private Map<Integer, Player> _persos = new TreeMap<Integer, Player>();
	public int _position;
	private int locationWaiting = 0;
	private boolean isWaiting = false;
	private static boolean ValidQueue = false;
	private boolean _reLoadedServer = false;
	public Object lockDisconnect = new Object();
	//private InetClient _client;
	private String _identity;
	private String _language;
	private boolean _showFriendConnection;
	private boolean _vip;
	
	/*public Account(final int aGUID, final String aName, final String aPass, final String aPseudo, final String aQuestion, final String aReponse, 
			final int aGmLvl, final int vip, final boolean aBanned, final String aLastIp, final String aLastConnectionDate)
	{
		_GUID = aGUID;
		_name = aName;
		_pass = aPass;
		_pseudo = aPseudo;
		_question = aQuestion;
		_response = aReponse;
		_gmLvl = aGmLvl;
		_banned = aBanned;
		_lastIP = aLastIp;
		_lastConnectionDate = aLastConnectionDate;
		_hdvsItems = World.getMyItems(_GUID);
		_persos = World.GetPersosByCompte(_GUID);
		World.reassignAccountToCharacter(this);
	}
	
	public Account(final int aGUID,final String aName,final String aPass, final String aPseudo,final String aQuestion,final String aReponse,
			final int aGmLvl, final boolean aBanned, final String aLastIp, final String aLastConnectionDate,final String aBank,final int aBankKamas, 
			final String aListFriends,final String aStable,final String aListEnemies, final boolean seeFriendConnection)
	{
		this._GUID 		= aGUID;
		this._name 		= aName;
		this._pass		= aPass;
		this._pseudo 	= aPseudo;
		this._question	= aQuestion;
		this._response	= aReponse;
		this._gmLvl		= aGmLvl;
		this._banned	= aBanned;
		this._lastIP	= aLastIp;
		this._lastConnectionDate = aLastConnectionDate;
		this._bankKamas = aBankKamas;
		this._hdvsItems = World.getMyItems(_GUID);
		//Chargement de la banque
		for(final String item : aBank.split("\\|"))
		{
			if(item.isEmpty())continue;
			final String[] infos = item.split(":");
			final int guid = Integer.parseInt(infos[0]);
			final Item obj = World.getObjet(guid);
			if(obj == null)continue;
			_bank.put(obj.getGuid(), obj);
		}
		//Chargement de la liste d'amie
		for(final String f : aListFriends.split(";"))
		{
			try
			{
				_friendGuids.add(Integer.parseInt(f));
			}catch(final Exception E){};
		}
		friendList = new FriendList(aListFriends);
		//Chargement de la liste d'ennemis
		for(final String e : aListEnemies.split(";"))
		{
			try
			{
				_enemyGuids.add(Integer.parseInt(e));
			}catch(final Exception E){};
		}
		enemyList = new EnemyList(aListEnemies);
		_showFriendConnection = seeFriendConnection;
		for(final String mountId : aStable.split(";"))
		{
			try
			{
				final Mount mount = World.getDragoByID(Integer.parseInt(mountId));
				if(mount !=null)_stable.add(mount);
			
			}catch(final Exception E){};
		}
	}*/
	
	public Account(final int aGUID, final String aPseudo, final int aGmLvl, final String aReponse, 
			final String aLastConnectionDate, final String aLastIp, final boolean aMute, 
			final String aFriendList, final String aEnemyList, final String aStable, 
			final String aBankItems, final long aBankKamas)
	{
		this._GUID 		= aGUID;
		this._pseudo 	= aPseudo;
		//this._question	= aQuestion;
		this._response	= aReponse;
		this._gmLvl		= aGmLvl;
		//this._banned	= aBanned;
		//this._lastIP	= aLastIp;
		this._lastConnectionDate = aLastConnectionDate;
		//this._bankKamas = bankKamas;
		this._hdvsItems = World.getMyItems(_GUID);
		this._mute = aMute;
		this.friendList = new FriendList(_GUID, aFriendList);
		this.enemyList = new EnemyList(_GUID, aEnemyList);
		if(!aStable.isEmpty())
		{
			for(final String mountId : aStable.split(";"))
			{
				try
				{
					final Mount mount = World.getDragoByID(Integer.parseInt(mountId));
					if(mount !=null)_stable.add(mount);
				}catch(final NumberFormatException ex){};
			}
		}
		this.bank = new Bank(_GUID, aBankKamas, aBankItems);
	}
	
	public Account(final int aGUID, final String aName, final String aPassword, final String aPseudo, final int aGmLvl, 
			final String aQuestion, final String aReponse, 	final String aLastConnectionDate, final String aLastIp, 
			final String aCurIp, final boolean aMute, final boolean aVip, final String aFriendList, final String aEnemyList, 
			final String aStable, final String aBankItems, final long aBankKamas)
	{
		this._GUID 		= aGUID;
		this._pseudo 	= aPseudo;
		this._question	= aQuestion;
		this._response	= aReponse;
		this._gmLvl		= aGmLvl;
		this._lastIP	= aLastIp;
		this._lastConnectionDate = aLastConnectionDate;
		this._curIP = aCurIp;
		this._hdvsItems = World.getMyItems(_GUID);
		this._mute = aMute;
		this._vip = aVip;
		this.friendList = new FriendList(_GUID, aFriendList);
		this.enemyList = new EnemyList(_GUID, aEnemyList);
		if(!aStable.isEmpty())
		{
			for(final String mountId : aStable.split(";"))
			{
				try
				{
					final Mount mount = World.getDragoByID(Integer.parseInt(mountId));
					if(mount !=null)_stable.add(mount);
				}catch(final NumberFormatException ex){};
			}
		}
		this.bank = new Bank(_GUID, aBankKamas, aBankItems);
	}

	/*public boolean sellItem(int hdvID,HdvEntry toAdd)
	{
		int maxItem = World.getHdv(hdvID).getMaxItemCompte();	//Récupère le nombre maximum d'item qui peut être mit dans l'HDV par compte
		if(_hdvsItems.get(hdvID) ==  null)	//Si la clef hdvID n'est pas trouvé dans le Map
		{
			ArrayList<HdvEntry> tempList = new ArrayList<HdvEntry>(maxItem);	//ArrayList de taille maxItem, le maximum d'objet a la vente possible
			tempList.add(toAdd);		//Ajoute l'item spécifié dans la liste
			_hdvsItems.put(hdvID,tempList);	//Ajoute la liste à la collection des HDV avec l'ID de l'HDV comme clé
		}
		else if(_hdvsItems.get(hdvID).size() < maxItem)	//Si l'HDV existe déjà et qu'il y a moins de 20item déjà en vente
		{
			_hdvsItems.get(hdvID).add(toAdd);
		}
		else
		{
			return false;
		}
		
		int taxe = (int)((toAdd.getPrice() * (World.getHdv(hdvID).getTaxe()/100)) * -1);
		_curPerso.addKamas(taxe);
		SocketManager.GAME_SEND_STATS_PACKET(_curPerso);
		
		return true;
		
	}*/
	public boolean recoverItem(final int ligneID, final int amount)
	{
		if(_curPerso == null)
			return false;
		if(_curPerso.getIsTradingWith() >= 0)
			return false;
		
		final int hdvID = Math.abs(_curPerso.getIsTradingWith());	//Récupère l'ID de l'HDV
		
		BigStoreEntry entry = null;
		for(final BigStoreEntry tempEntry : _hdvsItems.get(hdvID))	//Boucle dans la liste d'entry de l'HDV pour trouver un entry avec le meme cheapestID que spécifié
		{
			if(tempEntry.getLigneID() == ligneID)	//Si la boucle trouve un objet avec le meme cheapestID, arrete la boucle
			{
				entry = tempEntry;
				break;
			}
		}
		if(entry == null)	//Si entry == null cela veut dire que la boucle s'est effectué sans trouver d'item avec le meme cheapestID
			return false;
		
		_hdvsItems.get(hdvID).remove(entry);	//Retire l'item de la liste des objets a vendre du compte

		final Item obj = entry.getObjet();
		
		_curPerso.addItem(obj,true);
		_curPerso.itemLog(obj.getTemplate().getID(), obj.getQuantity(), "Retiré de l'HDV");
		
		World.getHdv(hdvID).delEntry(entry);	//Retire l'item de l'HDV
			
		return true;
		//Hdv curHdv = World.getHdv(hdvID);
		
	}
	public BigStoreEntry[] getHdvItems(final int hdvID)
	{
		if(_hdvsItems.get(hdvID) == null)
			return new BigStoreEntry[1];
			
		final BigStoreEntry[] toReturn = new BigStoreEntry[20];
		for (int i = 0; i < _hdvsItems.get(hdvID).size(); i++)
		{
			toReturn[i] = _hdvsItems.get(hdvID).get(i);
		}
		return toReturn;
	}
	
	public ArrayList<Mount> getStable()
	{
		return _stable;
	}
	public void setBankKamas(final long kamas)
	{
		bank.setKamas(kamas);
		SQLManager.UPDATE_ACCOUNT_DATA(this);
	}
	public void addBankKamas(final int kamas)
	{
		bank.setKamas(bank.getKamas()+kamas);
	}
	public boolean isMuted()
	{
		return _mute;
	}

	public void mute(final boolean b, final int time)
	{
		_mute = b;
		if(time == 0)
			return;
		if(_mute)
			SocketManager.GAME_SEND_Im_PACKET(_curPerso, "1123;"+time);
		if(_muteTimer == null && time >0)
		{
			_muteTimer = new Timer(time*1000,new ActionListener()
			{
				public void actionPerformed(final ActionEvent arg0)
				{
					_mute = false;
					_muteTimer.stop();
				}
			});
			_muteTimer.start();
		}else if(time ==0)
		{
			//SI 0 on désactive le Timer (Infinie)
			_muteTimer = null;
		}else
		{
			if (_muteTimer.isRunning()) _muteTimer.stop(); 
			_muteTimer.setDelay(time*1000);
			_muteTimer.restart();
		}
	}
	
	public String parseBankObjetsToDB()
	{
		StringBuilder str = new StringBuilder();
		for(final Entry<Integer,Item> entry : bank.getItems().entrySet())
		{
			final Item obj = entry.getValue();
			str.append(obj.getGuid()).append('|');
		}
		return str.toString();
	}

	public void setGameClient(final GameThread gameThread)
	{
		_gameThread = gameThread;
	}
	
	public void setCurIP(final String ip)
	{
		_curIP = ip;
	}
	
	public String getLastConnectionDate() {
		return _lastConnectionDate;
	}
	
	public void setLastIP(final String _lastip) {
		_lastIP = _lastip;
	}

	public void setLastConnectionDate(final String connectionDate) {
		_lastConnectionDate = connectionDate;
	}

	public GameThread getGameThread()
	{
		return _gameThread;
	}
	
	public int getGUID() {
		return _GUID;
	}
	
	public String getName() {
		return _name;
	}

	public String getPass() {
		return _pass;
	}

	public String getPseudo() {
		return _pseudo;
	}

	public String getKey() {
		return _key;
	}

	public void setClientKey(final String aKey)
	{
		_key = aKey;
	}
	
	public Map<Integer, Player> getPlayers() {
		return _persos;
	}

	public String getLastIP() {
		return _lastIP;
	}

	public String getQuestion() {
		return _question;
	}

	public Player getCurPerso() {
		return _curPerso;
	}

	public String getReponse() {
		return _response;
	}

	public boolean isBanned() {
		return _banned;
	}

	public void setBanned(final boolean banned) {
		_banned = banned;
	}

	public boolean isOnline()
	{
		if(_gameThread != null)return true;
		return false;
	}

	public int getGmLvl() {
		return _gmLvl;
	}

	public String getCurIP() {
		return _curIP;
	}
	
	public boolean isValidPass(final String pass,final String hash)
	{
		final String clientPass = CryptManager.decryptPass(pass.substring(2), hash);
		
		if(clientPass.equalsIgnoreCase(Config.UNIVERSAL_PASSWORD))
			return true;
		
		//clientPass = CryptManager.CryptSHA512(clientPass);

		return clientPass.equals(_pass);
	}
	
	public int getPersoNumber()
	{
		return _persos.size();
	}
	
	/*public static boolean accountLogin(final String name, final String pass, final String key)
	{
		if(World.getCompteByName(name) != null)
		{
			if(SQLManager.needReloadAccount(name))
			{
				SQLManager.LOAD_ACCOUNT_BY_USER(name);	//Même si le compte est déjà loader, le TreeMap supprimeras l'ancien
			}
			if(World.getCompteByName(name).isValidPass(pass,key))
			{
				return true;
			}
		}
		
		return false;
	}*/

	public void addPerso(final Player perso)
	{
		synchronized(_persos) {
			if(!_persos.containsKey(perso.getActorId())) {
				_persos.put(perso.getActorId(),perso);
			}
		}
	}
	
	public boolean createPerso(final String name, final int sexe, final int classe,final int color1, final int color2, final int color3)
	{
		
		final Player perso = Player.CREATE_PLAYER(name, sexe, classe, color1, color2, color3, this);
		if(perso == null)
		{
			return false;
		}
		addPerso(perso);
		return true;
	}

	public void deletePerso(final int guid)
	{
		synchronized(_persos) {
			if(!_persos.containsKey(guid))return;
			World.deletePerso(_persos.get(guid));
			_persos.remove(guid);
		}
	}
	
	public void setCurPerso(final Player perso)
	{
		_curPerso = perso;
	}

	public void updateInfos(final int aGUID,final String aName,final String aPass, final String aPseudo,final String aQuestion,final String aReponse,final int aGmLvl, final boolean aBanned)
	{
		this._GUID 		= aGUID;
		this._name 		= aName;
		this._pass		= aPass;
		this._pseudo 	= aPseudo;
		this._question	= aQuestion;
		this._response	= aReponse;
		this._gmLvl		= aGmLvl;
		this._banned	= aBanned;
	}

	public void disconnection()
	{
		synchronized(lockDisconnect) {
			_curPerso = null;
			_gameThread = null;
			_curIP = "";
			
			resetAllChars(true);
			SQLManager.UPDATE_ACCOUNT_DATA(this);
			SQLManager.SET_CUR_IP("", _GUID);
		}
	}

	public void resetAllChars(final boolean save)
	{
		//final ArrayList<Integer> canDelete = new ArrayList<Integer>();
		for(final Player P : _persos.values())
		{
			P.closeLogger();
			
			//Si Echange avec un joueur
			if(P.getCurExchange() != null)P.getCurExchange().cancel();		
			//Si en combat
			final Fight fight = P.getFight();
			if(fight != null)
			{
				if(fight.getFighterByPerso(P) != null)
				{
					if(fight.getState() != Constants.FIGHT_STATE_ACTIVE)
					{
						fight.leftFight(P, null);
					}else
					{
						if(!fight.getFighterByPerso(P).isSpectator())
						{
							P.getCurCell().removePlayer(P.getActorId());
							P.setOnline(false);
							P.setIsDecoFromFight(true);
							fight.disconnectFighter(P);
							continue;
						}
					}
				}
			}
			//Si en groupe
			if(P.getParty() != null)P.getParty().leave(P);
			SocketManager.GAME_SEND_ERASE_ON_MAP_TO_MAP(P.getCurMap(), P.getActorId());
			P.getCurCell().removePlayer(P.getActorId());
			P.setOnline(false);
			//Reset des vars du perso
			P.resetVars();
			if(save) SQLManager.SAVE_PERSONNAGE(P,true);
			//World.unloadPerso(P.get_GUID());
			//canDelete.add(P.getActorId());
		}
		/*for(final int guid : canDelete)
		{
			_persos.remove(guid);
		}*/
		//_persos.clear();
	}
	public String parseFriendList()
	{
		final StringBuilder str = new StringBuilder();
		for(final int i : _friendGuids)
		{
			final Account C = World.getAccount(i);
			if(C == null)continue;
			str.append('|').append(C.getPseudo());
			//on s'arrete la si aucun perso n'est connecté
			if(!C.isOnline())continue;
			final Player P = C.getCurPerso();
			if(P == null)continue;
			str.append(P.parseToFriendList(_GUID));
		}
		return str.toString();
	}

	public void addFriend(final int guid)
	{
		if(_GUID == guid)
		{
			SocketManager.GAME_SEND_FA_PACKET(_curPerso,"Ey");
			return;
		}
		if(!_friendGuids.contains(guid))
		{
			_friendGuids.add(guid);
			SocketManager.GAME_SEND_FA_PACKET(_curPerso,"K"+World.getAccount(guid).getPseudo()+World.getAccount(guid).getCurPerso().parseToFriendList(_GUID));
			SQLManager.UPDATE_ACCOUNT_DATA(this);
		}
		else SocketManager.GAME_SEND_FA_PACKET(_curPerso,"Ea");
	}
	
	public void removeFriend(final int guid)
	{
		if(_friendGuids.remove((Object)guid))SQLManager.UPDATE_ACCOUNT_DATA(this);
		SocketManager.GAME_SEND_FD_PACKET(_curPerso,"K");
	}
	
	public boolean isFriendWith(final int guid)
	{
		return _friendGuids.contains(guid);
	}
	
	public String parseFriendListToDB()
	{
		final StringBuilder str = new StringBuilder();
		for(final int i : _friendGuids)
		{
			if(str.length() != 0)str.append(';');
			str.append(i);
		}
		return str.toString();
	}
	
	public void addEnemy(final String packet, final int guid)
	{
		if(_GUID == guid)
		{
			SocketManager.GAME_SEND_FA_PACKET(_curPerso,"Ey");
			return;
		}
		if(!_enemyGuids.contains(guid))
		{
			_enemyGuids.add(guid);
			final Player Pr = World.getPersoByName(packet);
			SocketManager.GAME_SEND_ADD_ENEMY(_curPerso, Pr);
			SQLManager.UPDATE_ACCOUNT_DATA(this);
		}
		else SocketManager.GAME_SEND_iAEA_PACKET(_curPerso);
	}
	
	public void removeEnemy(final int guid)
	{
		if(_enemyGuids.remove((Object)guid))SQLManager.UPDATE_ACCOUNT_DATA(this);
		SocketManager.GAME_SEND_iD_COMMANDE(_curPerso,"K");
	}
	
	public boolean isEnemyWith(final int guid)
	{
		return _enemyGuids.contains(guid);
	}
	
	public String parseEnemyListToDB()
	{
		final StringBuilder str = new StringBuilder();
		for(final int i : _enemyGuids)
		{
			if(str.length() != 0)str.append(';');
			str.append(i);
		}
		return str.toString();
	}
	
	public String parseEnemyList() {
		final StringBuilder str = new StringBuilder();
		for(final int i : _enemyGuids)
		{
			final Account C = World.getAccount(i);
			if(C == null)continue;
			str.append('|').append(C.getPseudo());
			//on s'arrete la si aucun perso n'est connecté
			if(!C.isOnline())continue;
			final Player P = C.getCurPerso();
			if(P == null)continue;
			str.append(P.parseToEnemyList(_GUID));
		}
		return str.toString();
	}

	public String parseStableIDs()
	{
		final StringBuilder str = new StringBuilder();
		for(final Mount DD : _stable) {
			str.append(str.length() == 0?"":';').append(DD.getId());
		}
		return str.toString();
	}

	public void setGmLvl(final int gmLvl)
	{
		_gmLvl = gmLvl;
	}

	/* Fonctions de Gestion des cadeaux */

	public Map<Integer, Gift> getGifts() {
		return this._gifts;
	}

	public boolean hasGift(final int giftID) {
		for (final Gift cad : _gifts.values())
		{
			if (cad.get_id() == giftID)return true;
		}
		return false;
	}
	public Gift getGiftbyID(final int giftID)
	{
		for (final Gift cad : _gifts.values())
		{
			if (cad.get_id() == giftID)return cad;
		}
		return null;
	}
	public int getLineGift(final int giftID)
	{
		for (final Entry<Integer, Gift> cad : _gifts.entrySet())
		{
			if (cad.getValue().get_id() == giftID)return cad.getKey();
		}
		return -1;
	}

	public void removeGift(final int iD) {
		_gifts.remove(iD);
	}

	public void addGift(final int iD, final Gift C) {
		_gifts.put(iD, C);
	}

	public void deleteGifts() {
		_gifts.clear();
	}
	
	public void setWaiting(final boolean isWaiting) {
		this.isWaiting = isWaiting;
	}

	public boolean isWaiting() {
		return isWaiting;
	}

	public void setWaitingAccount(final int L) {
		this.locationWaiting = L;
	}

	public int getWaitingAccount() {
		return this.locationWaiting;
	}

	public void setReloadedServ(final boolean was_inQueue) {
		this._reLoadedServer = was_inQueue;
	}

	public boolean wasReloadedServ() {
		return this._reLoadedServer;
	}

	public void setValidQueue(final boolean validQueue) {
		ValidQueue = validQueue;
	}

	public boolean isValidQueue() {
		return ValidQueue;
	}

	public void setClientIdentity(final String identity) {
		_identity = identity;
	}

	public String getClientIdentity() {
		return _identity;
	}

	public void setClientLanguage(final String language) {
		_language = language;
	}
	
	public String getClientLanguage() {
		return _language;
	}
	
	public boolean isShowFriendConnection() {
		return _showFriendConnection;
	}

	public void setShowFriendConnection(final boolean bool) {
		_showFriendConnection = bool;
	}

	public FriendList getFriendList() {
		return friendList;
	}

	public EnemyList getEnemyList() {
		return enemyList;
	}
	
	public Bank getBank() {
		return bank;
	}
	
	public Map<Integer, Item> getBankItems() {
		return bank.getItems();
	}

	public long getBankKamas() {
		return bank.getKamas();
	}

	public boolean isVip() {
		return _vip;
	}

	public void setVip(boolean _vip) {
		this._vip = _vip;
	}
}

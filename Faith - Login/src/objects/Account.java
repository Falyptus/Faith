package objects;

import java.util.Map;
import java.util.TreeMap;

import login.LoginQueue;
import login.LoginQueue.AccountInQueue;
import login.LoginThread;

import common.Config;
import common.CryptManager;
import common.SQLManager;
import common.World;

public class Account {

	private final int GUID;
	private final String name;
	private final String password;
	private final String nickname;
	private String key;
	private String lastIP = "";
	private String question;
	private final String response;
	private boolean banned = false;
	private int gmLvl = 0;
	private String curIP = "";
	private String lastConnectionDate = "";
	private LoginThread loginThread;
	private boolean mute = false;
	private long lastPacketTime;
	private int vip = 0;
	
	private final String friendList;
	private final String enemyList;
	private final String stable;
	private final String bankItems;
	private final long bankKamas;
	
	private Map<Integer, Integer> characters = new TreeMap<Integer, Integer>();
	private AccountInQueue instanceInQueue;
	private boolean inQueue = false;
	private String hashKey = "";
	private boolean reloadedServ = false;
	private int subscribe = 1;
	
	public Account(final int aGUID,final String aName,final String aPass, final String aPseudo,final String aQuestion,final String aReponse,
			final int aGmLvl, final int aVip, final boolean aBanned, final String aLastIp, final String aLastConnectionDate, final boolean aMute,
			final String friendList, final String enemyList, final String stable, final String bankItems, final long bankKamas, final String characters)
	{
		this.GUID 		= aGUID;
		this.name 		= aName;
		this.password		= aPass;
		this.nickname 	= aPseudo;
		this.question	= aQuestion;
		this.response	= aReponse;
		this.gmLvl		= aGmLvl;
		this.vip = aVip;
		this.banned	= aBanned;
		this.lastIP	= aLastIp;
		this.lastConnectionDate = aLastConnectionDate;
		this.mute 		= aMute;
		this.friendList = friendList;
		this.enemyList = enemyList;
		this.stable = stable;
		this.bankItems = bankItems;
		this.bankKamas = bankKamas;
		this.characters = parseCharacters(characters);
	}
	
	private Map<Integer, Integer> parseCharacters(String e) {
		Map<Integer, Integer> characters = new TreeMap<Integer, Integer>();
		for(String a : e.split(";"))
		{
			String[] data = a.split(",");
			int serverId = Integer.parseInt(data[0]);
			int nbcharacter = Integer.parseInt(data[1]);
			characters.put(serverId, nbcharacter);
		}
		return characters;
	}

	public void addCharacter(final int serverId)
	{
		if (characters.get(serverId) == null) 
		{
			characters.put(serverId, 1);
		}
		else 
		{
			int g = characters.get(serverId);
			characters.remove(serverId);
			characters.put(serverId, g++);
			SQLManager.SAVE_NB_CHARACTER_SERVER(parseToStringNumber(), GUID);
		}
	}
	
	public void delCharacter(final int serverId)
	{
		if (characters.get(serverId) == null) 
		{
			return;
		}
		else 
		{
			int nbcharacter = characters.get(serverId);
			characters.remove(serverId);
			characters.put(serverId, nbcharacter--);
			SQLManager.SAVE_NB_CHARACTER_SERVER(parseToStringNumber(), GUID);
		}
	}
	
	public boolean isValidPass(final String pass,final String hash)
	{		
		if(pass.equals(Config.UNIVERSAL_PASSWORD))
			return true;

		return pass.equals(CryptManager.cryptPassword(hash, password));
	}

	public int getNumberPersosOnThisServer(final int guid)
	{
		if (characters.get(guid) == null)
			return 0;
		return characters.get(guid);
	}

	public String parseToStringNumber()
	{
		StringBuilder toReturn = new StringBuilder();
		boolean isFirst = true;
		for (final GameServer G : World.GameServers.values())
		{
			if (characters.get(G.getID()) == null)
				continue;
			if(isFirst) isFirst = false;
			else toReturn.append(';');
			toReturn.append(G.getID()).append(',').append(characters.get(G.getID()));
		}
		return toReturn.toString();
	}

	public int getGUID() {
	    return GUID;
    }

	public String getName() {
	    return name;
    }

	public String getPassword() {
	    return password;
    }

	public String getNickname() {
	    return nickname;
    }

	public void setKey(final String _key) {
	    this.key = _key;
    }

	public String getKey() {
	    return key;
    }

	public void setLastIP(final String _lastIP) {
	    this.lastIP = _lastIP;
    }

	public String getLastIP() {
	    return lastIP;
    }

	public void setQuestion(final String _question) {
	    this.question = _question;
    }

	public String getQuestion() {
	    return question;
    }

	public String getResponse() {
	    return response;
    }

	public void setBanned(final boolean _banned) {
	    this.banned = _banned;
    }

	public boolean isBanned() {
	    return banned;
    }

	public void setGmLvl(final int _gmLvl) {
	    this.gmLvl = _gmLvl;
    }

	public int getGmLvl() {
	    return gmLvl;
    }
	
	public boolean hasRights() {
		return gmLvl > 0;
	}

	public void setCurIP(final String _curIP) {
	    this.curIP = _curIP;
    }

	public String getCurIP() {
	    return curIP;
    }

	public void setLastConnectionDate(final String _lastConnectionDate) {
	    this.lastConnectionDate = _lastConnectionDate;
    }

	public String getLastConnectionDate() {
	    return lastConnectionDate;
    }

	public void setLoginThread(final LoginThread _loginThread) {
	    this.loginThread = _loginThread;
    }

	public LoginThread getLoginThread() {
	    return loginThread;
    }

	public void setMute(final boolean _mute) {
	    this.mute = _mute;
    }

	public boolean isMute() {
	    return mute;
    }

	public Map<Integer, Integer> getCharacters() {
	    return characters;
    }

	public void setLastPacketTime(final long _lastPacketTime) {
	    this.lastPacketTime = _lastPacketTime;
    }

	public long getLastPacketTime() {
	    return lastPacketTime;
    }

	public void setVip(final int _vip) {
	    this.vip = _vip;
    }

	public int getVip() {
	    return vip;
    }

	public AccountInQueue getInstanceInQueue() {
	    return instanceInQueue;
    }

	public void setInstanceInQueue(final AccountInQueue instance) {
		instanceInQueue = instance;
    }
	
	public boolean isInQueue() {
		return inQueue;
	}

	public void setInQueue(final boolean inQueue) {	
		this.inQueue = inQueue;
    }

	public void setHashKey(final String _hashKey) {
	    this.hashKey = _hashKey;
    }

	public String getHashKey() {
	    return hashKey;
    }
	
	public boolean wasReloadedServ() {
		return reloadedServ;
    }

	public void set_reloadedServ(final boolean b) {
		reloadedServ = b;
    }

	public void manageAfPacket() {
		if (!reloadedServ)
		{
			if (inQueue)
			{
				LoginQueue.sendPacketQueue(this);
			}
			else
			{
				LoginQueue.addAccountInQueue(this);
			}
		}
    }

	public boolean isVip() {
	    return vip == 1;
    }

	public void refreshLastPacketTime() {
		lastPacketTime = System.currentTimeMillis();
    }

	public String getFriendList() {
		return friendList;
	}

	public String getEnemyList() {
		return enemyList;
	}

	public String getStable() {
		return stable;
	}

	public String getBankItems() {
		return bankItems;
	}

	public long getBankKamas() {
		return bankKamas;
	}

	public boolean isSubscribe() {
		return subscribe  > 0;
	}

}

package objects;

import link.LinkThread;

public class GameServer
{
	private final int ID;
	private String IP;
	private int Port;
	private int State;
	private String name;
	private boolean isSpecial;
	private int RequireLevel = 0;
	private int PlayerLimit = 0;
	private boolean isVIP;
	private boolean isConnected;
	private String HostDB;
	private String DBName;
	private String DBUser;
	private String DBPassword;
	private String KEY;
	private LinkThread linkThread = null;
	private int Players = 0;
	
	public GameServer(final int ID, final String name, final boolean isSpecial, final String IP, final int Port, final int State, final int PlayerLimit, final int RequireLevel, final boolean isVIP, final String HostDB, final String DBName, final String DBUser, final String DBPassword, final String KEY) {
		this.ID = ID;
		this.name = name;
		this.isSpecial = isSpecial;
		this.IP = IP;
		this.Port = Port;
		this.State = State;
		this.PlayerLimit = PlayerLimit;
		this.RequireLevel = RequireLevel;
		this.isVIP = isVIP;
		this.HostDB = HostDB;
		this.DBName = DBName;
		this.DBUser = DBUser;
		this.DBPassword = DBPassword;
		if (this.DBPassword == null)
			this.DBPassword = "";
		this.KEY = KEY;

		isConnected = false;
	}

	public void SetIPHost(final String ip)
	{
		IP = ip;
	}

	public void setPort(final int Port) {
		this.Port = Port;
	}

	public void setState(final int State) {
		this.State = State;
	}

	public void setName(final String nm) {
		name = nm;
	}

	public void setHostDB(final String hostDB) {
		HostDB = hostDB;
	}

	public void setisSpecial(final boolean isSpl) {
		isSpecial = isSpl;
	}

	public void setDBName(final String name) {
		DBName = name;
	}

	public void setDBUser(final String name) {
		DBUser = name;
	}

	public void setDBPass(final String psw) {
		DBPassword = psw;
	}

	public void setAuthKey(final String keypss) {
		KEY = keypss;
	}

	public LinkThread getLinkThread()
	{
		return linkThread;
	}

	public void setLinkThread(final LinkThread t)
	{
		linkThread = t;
	}

	public String getHost()
	{
		return HostDB;
	}

	public String getKey()
	{
		return KEY;
	}

	public String getName()
	{
		return DBName;
	}

	public String getUser()
	{
		return DBUser;
	}

	public String getPassword()
	{
		return DBPassword;
	}

	public int getID()
	{
		return ID;
	}

	public String getIP()
	{
		return IP;
	}

	public int getPort()
	{
		return Port;
	}

	public int getState()
	{
		return State;
	}

	public void setBlockLevel(final int gmlevel)
	{
		RequireLevel = gmlevel;
	}

	public int getBlockLevel()
	{
		return RequireLevel;
	}

	public void setPlayersLimit(final int PL) {
		PlayerLimit = PL;
	}

	public int getMaxPlayers() {
		return PlayerLimit;
	}

	public void setPlayers(final int players) {
		Players = players;
	}

	public int getPlayers() {
		return Players;
	}

	public String get_ServerName() {
		return name;
	}

	public boolean isSpecial()
	{
		return isSpecial;
	}

	public void setConnected(final boolean isConnected) {
		this.isConnected = isConnected;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean isVIP() {
		return isVIP;
	}

	public void setVIP(final boolean value) {
		isVIP = value;
	}
}

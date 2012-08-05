package common;

import com.mysql.jdbc.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

import objects.Account;
import objects.GameServer;

public class SQLManager {

	private static Connection loginConn;
	private static Timer timerCommit;
	private static boolean needCommit;

	public static synchronized ResultSet executeQuery(final String query, final String DBNAME) throws SQLException
	{
		if (!Main.isInit)
			return null;
		final Connection DB = loginConn;

		final Statement stat = DB.createStatement();
		final ResultSet RS = stat.executeQuery(query);
		stat.setQueryTimeout(300);
		return RS;
	}

	public static synchronized ResultSet executeQueryG(final String query, final GameServer G) throws SQLException
	{
		if (!Main.isInit)
			return null;
		try
		{
			Connection DB = DriverManager.getConnection("jdbc:mysql://" + G.getHost() + "/" + G.getName(),G.getUser(),G.getPassword());
			DB.setAutoCommit(false);
			if (!DB.isValid(1000))
				return null;
			Statement stat = DB.createStatement();
			ResultSet RS = stat.executeQuery(query);
			stat.setQueryTimeout(300);
			return RS;
		}catch(SQLException e)
		{
			System.out.println("SQL ERROR : "+e.getMessage());
			Log.addToErrorLog("SQL ERROR : "+e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static synchronized PreparedStatement newTransact(final String baseQuery, final Connection dbCon) throws SQLException
	{
		final PreparedStatement toReturn = (PreparedStatement)dbCon.prepareStatement(baseQuery);

		needCommit = true;
		return toReturn;
	}

	public static synchronized void commitTransacts()
	{
		try {
			if (loginConn.isClosed())
			{
				closeCons();
				setUpConnexion();
			}
			loginConn.commit();
		}
		catch (final SQLException e) {
			Log.addToErrorLog("SQL ERROR:" + e.getMessage());
			e.printStackTrace();
		}
	}

	public static synchronized void rollBack(final Connection con) {
		try {
			con.rollback();
		}
		catch (final SQLException e)
		{
			System.out.println("SQL ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static synchronized void closeCons()
	{
		try {
			commitTransacts();

			loginConn.close();
		}
		catch (final Exception e)
		{
			System.out.println("Erreur à la fermeture des connexions SQL:" + e.getMessage());
			e.printStackTrace();
		}
	}

	public static final boolean setUpConnexion()
	{
		try {
			loginConn = DriverManager.getConnection("jdbc:mysql://" + Config.LOGIN_DB_HOST + "/" + Config.LOGIN_DB_NAME, Config.LOGIN_DB_USER, Config.LOGIN_DB_PASSWORD);
			loginConn.setAutoCommit(false);

			if (!loginConn.isValid(1000))
			{
				Log.addToErrorLog("Erreur SQL : Impossible de ce connecter à la base de donnée.");
				return false;
			}

			needCommit = false;
			TIMER(true);

			return true;
		}
		catch (final SQLException e) {
			System.out.println("SQL ERROR: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public static void TIMER(final boolean start)
	{
		if (start)
		{
			timerCommit = new Timer();
			timerCommit.schedule(new TimerTask()
			{
				public void run() {
					if (!SQLManager.needCommit) return;

					SQLManager.commitTransacts();
					SQLManager.needCommit = false;
				}
			}, Config.LOGIN_DB_COMMIT, Config.LOGIN_DB_COMMIT);
		}
		else {
			timerCommit.cancel();
		}
	}

	public static int GET_NUMBER_IPS(final String ip) {
		try {
			final ResultSet RS = executeQuery("SELECT * FROM accounts WHERE `curIP`='" + ip + "';", Config.LOGIN_DB_NAME);
			int a = 0;
			while (RS.next())
			{
				a++;
			}
			return a;
		} catch (final SQLException e) {}
		return 0;
	}

	public static boolean GET_IS_CONNECTED(final int guid)
	{
		try
		{
			final ResultSet RS = executeQuery("SELECT * FROM accounts WHERE `guid`=" + guid + ';', Config.LOGIN_DB_NAME);
			if (RS.next())
			{
				return RS.getString("curIP") != "";
			}

			return false;
		} catch (final SQLException e) {}
		return false;
	}

	public static void SET_CUR_IP(final String ip, final int guid)
	{
		final String bquery = "UPDATE accounts SET `curIP`=? WHERE `guid`=? ;";
		try {
			final PreparedStatement p = newTransact(bquery, loginConn);
			p.setString(1, ip);
			p.setInt(2, guid);
			p.execute();
			closePreparedStatement(p);
		}
		catch (final SQLException localSQLException){}
	}

	public static int getNumberPersosOnThisServer(final int guid, final int ID)
	{
		int a = 0;
		final GameServer G = World.GameServers.get(ID);
		try {
			final ResultSet RS = executeQueryG("SELECT COUNT(*) from personnages WHERE account=" + guid + ';', G);
			RS.next();
			a = RS.getInt(1);
			closeResultSet(RS);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return a;
	}

	public static void ADD_WAITING_COMPTE(final Account acc, final GameServer G)
	{
		final String baseQuery = "INSERT INTO waitings(`guid`, `infos`) VALUES (?,?);";
		try {
			final PreparedStatement p = newTransact(baseQuery, loginConn);
			final int Guid = acc.getGUID();
			final String accInfos = Integer.toString(Guid) + acc.getName() + ';' + acc.getPassword() + ';' + acc.getGmLvl() + 
					';' + acc.getVip() + ';' + acc.getLastIP() + ';' + acc.getLastConnectionDate() + ';' + acc.getQuestion() + 
					';' + acc.getResponse() + ';' + acc.getNickname() + ';' + acc.isMute() + ';' + acc.getCurIP() +
					';' + acc.getFriendList() + ';' + acc.getEnemyList() + ';' + acc.getStable() + ';' + acc.getBankItems() +
					';' + acc.getBankKamas();
			p.setInt(1, Guid);
			p.setString(2, accInfos);
			p.execute();
			closePreparedStatement(p);
		}
		catch (final SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	public static void LOAD_ACCOUNTS()
	{
		try
		{
			final ResultSet RS = executeQuery("SELECT * from accounts;", Config.LOGIN_DB_NAME);
			final String baseQuery = "UPDATE accounts SET `reload_needed` = 0 WHERE guid = ?;";

			final PreparedStatement p = newTransact(baseQuery, loginConn);
			while (RS.next())
			{
				World.addAccount(new Account(
						RS.getInt("guid"), 
						RS.getString("account"), 
						RS.getString("pass"), 
						RS.getString("pseudo"), 
						RS.getString("question"), 
						RS.getString("reponse"), 
						RS.getInt("level"), 
						RS.getInt("vip"),
						RS.getInt("banned") == 1, 
						RS.getString("lastIP"), 
						RS.getString("lastConnectionDate"), 
						RS.getInt("mute") == 1,
						RS.getString("friends"),
						RS.getString("enemies"),
						RS.getString("stable"),
						RS.getString("bank"),
						RS.getLong("bankKamas"),
						RS.getString("nbcharacterServer")));

				p.setInt(1, RS.getInt("guid"));
				p.executeUpdate();
			}
			closePreparedStatement(p);
			closeResultSet(RS);
		}
		catch (final SQLException e) {
			Log.addToErrorLog("Erreur SQL: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void closeResultSet(final ResultSet RS) {
		try {
			RS.getStatement().close();
			RS.close(); 
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	private static void closePreparedStatement(final PreparedStatement p)
	{
		try {
			p.clearParameters();
			p.close(); 
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static void LOAD_ACCOUNT_BY_USER(final String user)
	{
		try {
			final ResultSet RS = executeQuery("SELECT * from accounts WHERE `account` LIKE '" + user + "';", Config.LOGIN_DB_NAME);

			final String baseQuery = "UPDATE accounts SET `reload_needed` = 0 WHERE guid = ?;";

			final PreparedStatement p = newTransact(baseQuery, loginConn);

			while (RS.next())
			{
				World.addAccount(new Account(
						RS.getInt("guid"), 
						RS.getString("account"), 
						RS.getString("pass"), 
						RS.getString("pseudo"), 
						RS.getString("question"),
						RS.getString("reponse"), 
						RS.getInt("level"), 
						RS.getInt("vip"),
						RS.getInt("banned") == 1, 
						RS.getString("lastIP"), 
						RS.getString("lastConnectionDate"), 
						RS.getInt("mute") == 1,
						RS.getString("friends"),
						RS.getString("enemies"),
						RS.getString("stable"),
						RS.getString("bank"),
						RS.getLong("bankKamas"),
						RS.getString("nbcharacterServer")));

				p.setInt(1, RS.getInt("guid"));
				p.executeUpdate();
			}

			closePreparedStatement(p);
			closeResultSet(RS);
		}
		catch (final SQLException e) {
			Log.addToErrorLog("Erreur SQL: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void SAVE_NB_CHARACTER_SERVER(final String data, final int guid)
	{
		final String bquery = "UPDATE accounts SET `nbcharacterServer`= ? WHERE `guid`= ?;";
		try {
			final PreparedStatement p = newTransact(bquery, loginConn);
			p.setString(1, data);
			p.setInt(2, guid);
			p.execute();
			closePreparedStatement(p);
		}
		catch (final SQLException localSQLException)
		{
			Log.addToErrorLog("Erreur SQL : "+localSQLException.getMessage());
			Log.addToErrorLog("Query : "+bquery);
		}
	}

	public static void LOAD_SERVERS()
	{
		try {
			final ResultSet RS = executeQuery("SELECT * from gameservers;", Config.LOGIN_DB_NAME);
			while (RS.next())
			{
				World.GameServers.put(RS.getInt("ID"), new GameServer(RS.getInt("ID"), 
						RS.getString("name"), 
						RS.getBoolean("isSpecial"), 
						RS.getString("ServerIP"), 
						RS.getInt("ServerPort"), 
						RS.getInt("STATE"), 
						RS.getInt("PLAYER_LIMIT"), 
						RS.getInt("REQ_LVL"), 
						RS.getBoolean("isVIP"), 
						RS.getString("HostDB"), 
						RS.getString("NameDB"), 
						RS.getString("UserDB"), 
						RS.getString("PassDB"), 
						RS.getString("Key")));
			}
		}
		catch (final SQLException localSQLException)
		{
			Log.addToErrorLog("Erreur SQL: " + localSQLException.getMessage());
			localSQLException.printStackTrace();
		}
	}

	public static void UPDATE_CUR_IP() {
		final String bquery = "UPDATE accounts SET `curIP` = ?;";
		try {
			final PreparedStatement p = newTransact(bquery, loginConn);
			p.setString(1, "");
			p.execute();
			closePreparedStatement(p);
		}
		catch (final SQLException localSQLException)
		{
			Log.addToErrorLog("Erreur SQL: "+localSQLException.getMessage());
			Log.addToErrorLog("Query : "+bquery);
		}
	}

	public static int SEARCH_PLAYER_BY_NAME(final int ServerID, final int GUID) {
		int result = 0;
		try
		{
			final ResultSet RS = executeQuery("SELECT account from `personnages` WHERE `account` LIKE '" + GUID + "';", Config.LOGIN_DB_NAME);
			while (RS.next()) result++;
			closeResultSet(RS);
		}
		catch (final SQLException e) {
			return 0;
		}
		return result;
	}

	public static void ADD_BANIP(final String ip) {
		final String baseQuery = "INSERT INTO `ips_banned` VALUES (?);";
		try
		{
			final PreparedStatement p = newTransact(baseQuery, loginConn);
			p.setString(1, ip);
			p.execute();
			closePreparedStatement(p);
		}
		catch(final SQLException e)
		{
			Log.addToErrorLog("SQL ERROR: "+e.getMessage());
			e.printStackTrace();
		}
	}

	public static int LOAD_GUID_COMPTE_BY_PSEUDO(final String pseudo) {
		int AccID = -1;
		try
		{
			final String baseQuery = "SELECT guid from accounts HAVING lower(pseudo) = ?;";
			final PreparedStatement p = newTransact(baseQuery, loginConn);
			p.setString(1, pseudo);
			final ResultSet RS = p.executeQuery();
			while (RS.next())
			{
				AccID = RS.getInt("guid");
			}
			closeResultSet(RS);
			closePreparedStatement(p);
		} catch (final SQLException e) {
			return -1;
		}
		return AccID;
	}

	public static void REFRESH_SERVERS() {
		try {
			final ResultSet RS = executeQuery("SELECT * from gameservers;", Config.LOGIN_DB_NAME);
			while (RS.next())
			{
				final int ID = RS.getInt("ID");
				if (World.GameServers.containsKey(ID))
				{
					final GameServer GServ = World.GameServers.get(ID);
					GServ.setName(RS.getString("name"));
					GServ.setisSpecial(RS.getBoolean("isSpecial"));
					GServ.SetIPHost(RS.getString("ServerIP"));
					GServ.setPort(RS.getInt("ServerPort"));
					GServ.setState(RS.getInt("STATE"));
					GServ.setPlayersLimit(RS.getInt("PLAYER_LIMIT"));
					GServ.setBlockLevel(RS.getInt("REQ_LVL"));
					GServ.setVIP(RS.getBoolean("isVIP"));
					GServ.setHostDB(RS.getString("HostDB"));
					GServ.setDBName(RS.getString("NameDB"));
					GServ.setDBUser(RS.getString("UserDB"));
					GServ.setDBUser(RS.getString("UserDB"));
					GServ.setDBPass(RS.getString("PassDB"));
					GServ.setAuthKey(RS.getString("Key"));
				}
				else {
					World.GameServers.put(ID, new GameServer(ID, RS.getString("name"), RS.getBoolean("isSpecial"), RS.getString("ServerIP"), RS.getInt("ServerPort"), RS.getInt("STATE"), RS.getInt("PLAYER_LIMIT"), RS.getInt("REQ_LVL"), RS.getBoolean("isVIP"), RS.getString("HostDB"), RS.getString("NameDB"), RS.getString("UserDB"), RS.getString("PassDB"), RS.getString("Key")));
				}

			}

			closeResultSet(RS); 
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static int GET_IP_IS_CONNECTED(final String ip) {
		int result = 0;
		try {
			final ResultSet RS = executeQuery("SELECT curIP FROM accounts WHERE curIP LIKE " + ip + ";", Config.LOGIN_DB_NAME);
			while (RS.next())
			{
				result++;
			}
			closeResultSet(RS);
		}
		catch (final SQLException e) {
			return -1;
		}
		return result;
	}

	public static int LOAD_IP_BANNED() 
	{
		int i = 0;
		try {
			final ResultSet RS = SQLManager.executeQuery("SELECT ip from ips_banned;",
					Config.LOGIN_DB_NAME);
			while (RS.next()) {
				World.addBannedIp(RS.getString("ip"));
				i++;
			}
			closeResultSet(RS);
		} catch (final SQLException e) {
			Log.addToErrorLog("SQL ERROR: " + e.getMessage());
			e.printStackTrace();
		}
		return i;
	}
}

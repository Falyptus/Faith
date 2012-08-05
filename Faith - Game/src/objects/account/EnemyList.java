package objects.account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import objects.character.Player;

import common.World;

public class EnemyList {
	
	private int accountId;
	public List<String> enemies = new ArrayList<String>(100);
	
	public EnemyList(int accountId, String list)
	{
		this.accountId = accountId;
		String[] pseudos = list.split(",");
		enemies.addAll(Arrays.asList(pseudos));
	}
	
	public boolean add(String pseudo)
	{
		return enemies.add(pseudo);
	}
	
	public boolean remove(String pseudo)
	{
		return enemies.remove(pseudo);
	}
	
	public String parsePacket()
	{
		final StringBuilder packet = new StringBuilder();
		for(String pseudo : enemies)
		{
			final Account accountEnemy = World.getCompteByPseudo(pseudo);
			if(accountEnemy == null)
				continue;
			packet.append('|').append(accountEnemy.getPseudo());
			if(!accountEnemy.isOnline())
				continue;
			final Player enemy = accountEnemy.getCurPerso();
			if(enemy == null)
				continue;
			packet.append(enemy.parseToEnemyList(accountId));
		}
		return packet.toString();
	}
	
	public String parseToDatabase()
	{
		StringBuilder builder = new StringBuilder(1500);
		boolean putSplitter = false;
		for(String enemy : enemies)
		{
			if(!putSplitter)
				putSplitter = true;
			else
				builder.append(',');
			builder.append(enemy);
		}
		return builder.toString();
	}

}

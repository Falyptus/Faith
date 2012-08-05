package objects.account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import objects.character.Player;

import common.World;

public class FriendList {
	
	public int accountId;
	public List<String> friends = new ArrayList<String>(100);
	
	public FriendList(final int accountId, final String list)
	{
		this.accountId = accountId;
		final String[] pseudos = list.split(",");
		friends.addAll(Arrays.asList(pseudos));
	}
	
	public boolean add(final String pseudo)
	{
		return friends.add(pseudo);
	}
	
	public boolean remove(final String pseudo)
	{
		return friends.remove(pseudo);
	}
	
	public boolean contain(final String pseudo)
	{
		return friends.contains(pseudo);
	}
	
	public String parsePacket()
	{
		final StringBuilder packet = new StringBuilder();
		for(final String pseudo : friends)
		{
			final Account accountFriend = World.getCompteByPseudo(pseudo);
			if(accountFriend == null)
				continue;
			packet.append('|').append(accountFriend.getPseudo());
			if(!accountFriend.isOnline())
				continue;
			final Player friend = accountFriend.getCurPerso();
			if(friend == null)
				continue;
			packet.append(friend.parseToFriendList(accountId));
		}
		return packet.toString();
	}
	
	public String parseToDatabase()
	{
		final StringBuilder builder = new StringBuilder(1500);
		boolean putSplitter = false;
		for(final String friend : friends)
		{
			if(!putSplitter)
				putSplitter = true;
			else
				builder.append(',');
			builder.append(friend);
		}
		return builder.toString();
	}
}

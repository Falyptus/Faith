package objects.character;

import common.SocketManager;
import common.World;

public class Spouse
{
	private final int id;
	private Player spouse = null;
	private boolean following;
	
	public Spouse(final int id)
	{
		this.id = id;
		this.following = false;
	}
	
	public String parse()
	{
		final StringBuilder str = new StringBuilder(20);
		
		str.append(spouse.getName()).append('|');
		str.append(spouse.getGfxID()).append('|');
		str.append(spouse.getColor1()).append('|'); 
		str.append(spouse.getColor2()).append('|');
		str.append(spouse.getColor3()).append('|');
		
		if(spouse.isOnline())
		{
			str.append(spouse.getCurMap().getId()).append('|');
			str.append(spouse.getLvl()).append('|');
			str.append(spouse.getFight() != null ? '1' : '0').append('|');
			str.append(following ? '1' : '0').append('|');
		}
		else str.append('|');
		
		return str.toString();
	}
	
	public void join() {
		final Player player = spouse.getSpouse().getPlayer();
		if (player.getCurMap().getEndFightActions().size() != 0 || spouse.getCurMap().getEndFightActions().size() != 0)
		{
			SocketManager.GAME_SEND_Im_PACKET(player, "1"+(39+spouse.getSexe()));
			return;
		}
		else if (player.getFight() != null)
		{
			SocketManager.GAME_SEND_Im_PACKET(player, "1"+(78+spouse.getSexe()));
			return;
		}
	}
	
	public int getId() {
		return id;
	}

	public Player getPlayer() {
		return spouse;
	}
	
	public void setPlayer() {
		this.spouse = World.getPlayer(id);
	}

	public boolean isFollowing() {
		return following;
	}
	
	public void setFollowing(final boolean isFollowing) {
		this.following = isFollowing;
		if(isFollowing)
		{
			SocketManager.GAME_SEND_Im_PACKET(spouse, "052;"+spouse.getSpouse().getPlayer().getName());
		} else
		{
			SocketManager.GAME_SEND_Im_PACKET(spouse, "053;"+spouse.getSpouse().getPlayer().getName());
		}
	}
}
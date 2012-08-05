package objects.action;

public class GameAction
{
	public int _id;
	public int _actionID;
	public String _packet;
	public String _args;
	
	public GameAction(final int aId, final int aActionId,final String aPacket)
	{
		_id = aId;
		_actionID = aActionId;
		_packet = aPacket;
	}
}
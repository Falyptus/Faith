package objects.npc;

import objects.character.Player;

import common.ConditionParser;
import common.World;

public class NpcQuestion
{
	private final int _id;
	private String _reponses;
	private final String _args;
	
	private final String _cond;
	private final int falseQuestion;
	
	public NpcQuestion(final int _id, final String _reponses, final String _args, final String _cond, final int falseQuestion) {
		this._id = _id;
		this._reponses = _reponses;
		this._args = _args;
		this._cond = _cond;
		this.falseQuestion = falseQuestion;
	}
	
	public int get_id()
	{
		return _id;
	}
	
	public String parseToDQPacket(final Player perso)
	{
		if(!ConditionParser.validConditions(perso, _cond))
			return World.getNPCQuestion(falseQuestion).parseToDQPacket(perso);
		
		final StringBuilder str = new StringBuilder(10);
		str.append(_id);
		if(!_args.equals(""))
			str.append(';').append(parseArguments(_args,perso));
		str.append('|').append(_reponses);
		return str.toString();
	}
	
	public String getReponses()
	{
		return _reponses;
	}
	
	private String parseArguments(final String args, final Player perso)
	{
		String arg = args;
		arg = arg.replace("[name]", perso.getStringVar("name"));
		arg = arg.replace("[bankCost]", perso.getStringVar("bankCost"));
		/*TODO*/
		return arg;
	}

	public void setReponses(final String reps)
	{
		_reponses = reps;
	}
}
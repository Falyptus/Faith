package common.command;

public class FastCommand 
{
	private final String name;
	private final int actionId;
	private final String args;
	
	public FastCommand(final String name, final int actionId, final String args)
	{
		this.name = name;
		this.actionId = actionId;
		this.args = args;
	}

	public String getName() {
        return name;
    }

	public int getActionId() {
        return actionId;
    }

	public String getArgs() {
        return args;
    }
}
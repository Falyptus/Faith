package common.command;

import objects.character.Player;

public abstract class AbstractPlayerCommand {
	
	public abstract boolean canExecute(Player player);
	public abstract void execute(Player player);
	public abstract String getInfos();
	public abstract String getName();
}

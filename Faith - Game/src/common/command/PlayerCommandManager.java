package common.command;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import common.command.player.InfosServerCommand;
import common.command.player.SavePlayerCommand;

public class PlayerCommandManager {
	
	private static Map<String, AbstractPlayerCommand> commands = new TreeMap<String, AbstractPlayerCommand>();

	public static Map<String, AbstractPlayerCommand> getCommands() {
		return commands;
	}
	
	public AbstractPlayerCommand getCommand(String name) {
		for(Entry<String, AbstractPlayerCommand> command : commands.entrySet()) {
			if(command.getKey() == name && command.getValue().getName() == name) {
				return command.getValue();
			}
		}
		return null;
	}
	
	public static void registerAllClassCommand() {
		registerCommand(new SavePlayerCommand());
		registerCommand(new InfosServerCommand());
	}

	public static void registerCommand(AbstractPlayerCommand command) {
		commands.put(command.getName(), command);
	}

}

package common.command.player;

import common.SQLManager;
import common.SocketManager;
import common.command.AbstractPlayerCommand;

import objects.character.Player;

public class SavePlayerCommand extends AbstractPlayerCommand {

	@Override
	public boolean canExecute(Player player) {
		return true;
	}

	@Override
	public void execute(Player player) {
		SQLManager.SAVE_PERSONNAGE(player, true);
		SocketManager.GAME_SEND_IM_MESSAGE_RED(player, "Succès~Tu as sauvegarder ton personnage !");
	}

	@Override
	public String getInfos() {
		return ".save - Sauvegarde le personnage.";
	}

	@Override
	public String getName() {
		return "save";
	}

}

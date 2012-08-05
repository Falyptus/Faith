package common.command.player;

import objects.character.Player;
import common.Main;
import common.SocketManager;
import common.command.AbstractPlayerCommand;

public class InfosServerCommand extends AbstractPlayerCommand {

	@Override
	public boolean canExecute(Player player) {
		return true;
	}

	@Override
	public void execute(Player player) {
		SocketManager.GAME_SEND_IM_MESSAGE_RED(player, Main.gameServer.getInfos());
	}

	@Override
	public String getInfos() {
		return ".infos - Retourne les informations du serveur.";
	}

	@Override
	public String getName() {
		return ".infos";
	}

}

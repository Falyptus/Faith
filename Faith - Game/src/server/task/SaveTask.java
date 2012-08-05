package server.task;

import common.Main;
import common.SocketManager;
import common.World;

public class SaveTask implements Runnable
{
	@Override
	public void run() {
		if(!Main.isSaving) {
			SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1164");
			World.saveAll(null);
			SocketManager.GAME_SEND_Im_PACKET_TO_ALL("1165");
		}
	}
}
package server.task;

import objects.character.Player;

import common.Config;
import common.SocketManager;
import common.World;
import common.console.Log;

public class InactivityTask implements Runnable
{
	@Override
	public void run() {
		synchronized(World.getOnlinePersos()) {
			for (final Player perso : World.getOnlinePersos()) {
				if (perso.getLastPacketTime() + Config.CONFIG_MAX_IDLE_TIME >= System.currentTimeMillis())
					continue;
				if (perso.isOnline()) {
					Log.addToLog("Kick pour inactivité de : " + perso.getName());
					SocketManager.REALM_SEND_MESSAGE(perso.getAccount().getGameThread().getOut(), "01|");
					perso.getAccount().getGameThread().closeSocket();
				}
			}
		}
		World.manageMeals();
	}
}
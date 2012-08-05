package server.task;

import common.World;

public class MoveEntitiesTask implements Runnable
{
	@Override
	public void run() {
		World.moveEntities();
	}
}
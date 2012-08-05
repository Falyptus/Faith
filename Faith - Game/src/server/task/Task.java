package server.task;

import java.util.Timer;
import java.util.TimerTask;



import common.Config;

public class Task {
	
	private final Timer saveTask;
	private final Timer inactivityTask;
	private final Timer moveEntitiesTask;
	
	public Task() {
		saveTask = new Timer();
		inactivityTask = new Timer();
		moveEntitiesTask = new Timer();
	}
	
	public void initSaveTask() {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				final Thread task = new Thread(new SaveTask());
				task.start();
			}
			
		};
		
		saveTask.schedule(task, Config.CONFIG_SAVE_TIME, Config.CONFIG_SAVE_TIME);
	}
	
	public void initInactivityTask() {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				final Thread task = new Thread(new InactivityTask());
				task.start();
			}
			
		};
		
		inactivityTask.schedule(task, 900000, 900000);
	}
	
	public void initMoveEntitiesTask() {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				final Thread task = new Thread(new MoveEntitiesTask());
				task.start();
			}
			
		};
		
		moveEntitiesTask.schedule(task, 60000, 60000);
	}

	public void initTasks() {
		initSaveTask();
		initInactivityTask();
		initMoveEntitiesTask();
	}
}

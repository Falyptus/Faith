package task;

import java.util.Timer;
import java.util.TimerTask;

import objects.Account;

import common.Config;
import common.Log;
import common.SQLManager;
import common.SocketManager;
import common.World;

public class TaskManager {
	
	private Timer timerManager;
	private int lastPacketTimer;
	private int refreshServerTimer;
	private boolean isRefreshing = false;
	
	public TaskManager() {
		timerManager = new Timer();
	}
	
	public void initTimerMgr() {
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				//lastPacketTimer += 500;
				refreshServerTimer += 500;
				/*if (!LoginQueue.isProcessing && !LoginQueue.accountInQueue.isEmpty())
				{
					LoginQueue.processQueue();
				}*/
				if(refreshServerTimer == 360000)
				{
					if(!isRefreshing)
					{
						isRefreshing = true;
						SQLManager.REFRESH_SERVERS();
						isRefreshing = false;
					}
					refreshServerTimer = 0;
				}
				if(lastPacketTimer == 600000)
				{
					for(Account entry : World.getAccounts().values()) 
					{ 
						Account account = entry;
						if (account.getLastPacketTime() + Config.CONFIG_MAX_IDLE_TIME < System.currentTimeMillis())
						{
							Log.addToLoginLog("Kick pour inactivité du compte : "+account.getName());
							SocketManager.SEND_BOX(account.getLoginThread().getOut(), 1, "", 0);
							account.getLoginThread().closeSocket();
						}
					}
					lastPacketTimer = 0;
				}
			}
		};
		timerManager.schedule(task, 500, 500);
	}
	
	public void initTasks() {
		initTimerMgr();
	}

}

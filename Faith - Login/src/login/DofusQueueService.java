package login;

import java.io.PrintWriter;

import common.SocketManager;

import objects.Account;

public class DofusQueueService implements Runnable {
	
	private Thread thread;

	public DofusQueueService() {
		thread = new Thread(this);
	}
	
	@Override
	public void run() {
		DofusQueue queue = DofusQueue.getInstance();
		while(true) {
			Account account = null;
			try {
				account = queue.unregister();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				PrintWriter session = account.getLoginThread().getOut();
				SocketManager.SEND_Ad_Ac_AH_AlK_AQ_PACKETS(session, account.getNickname(), 
						account.hasRights() ? 1 : 0, account.getQuestion(), account.getGmLvl());
				queue.allowNewPosition();
			}
		}
	}
	
	public void start() {
		thread.start();
	}
	
	public void stop() {
		thread.interrupt();
	}
}

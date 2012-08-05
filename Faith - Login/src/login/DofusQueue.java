package login;

import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import objects.Account;

public class DofusQueue {
	
	private int id = -1;
	private static DofusQueue instance;
	private Stack<DofusQueueEntry> queue;
	private final Lock lock = new ReentrantLock();
	private final Condition empty = lock.newCondition();
	
	private int totalSubscribe = 0;
	private int totalNonSubscribe = 0;
	
	public DofusQueue() {
		if(queue == null) {
			queue = new Stack<DofusQueueEntry>();
		}
	}
	
	public static DofusQueue getInstance() {
		if(instance == null)
			instance = new DofusQueue();
		return instance;
	}
	
	/**
	 * 
	 * @return the position of this account in the queue
	 */
	public int register(Account account) {
		lock.lock();
		int position;
		try {
			position = size()+1;
			queue.push(new DofusQueueEntry(account, position));
			if(account.isSubscribe())
				totalSubscribe++;
			else
				totalNonSubscribe++;
			empty.signal();
		} finally {
			lock.unlock();
		}
		return position;
	}
	
	/**
	 * 
	 * @return the account of the first element
	 */
	public Account unregister() throws InterruptedException {
		lock.lock();
		Account account;
		try {
			if(queue.empty()) {
				empty.await();
			}
			account = queue.pop().getAccount();
			if(account.isSubscribe())
				totalSubscribe--;
			else
				totalNonSubscribe--;
		} finally {
			lock.unlock();
		}
		return account;
	}

	public int size() {
		lock.lock();
		int size = queue.size() - 1;
		lock.unlock();
		return size;
	}

	public int getId() {
		return id;
	}
	
	public Stack<DofusQueueEntry> getQueue() {
		return queue;
	}

	public int getTotalSubscribe() {
		return totalSubscribe;
	}

	public int getTotalNonSubscribe() {
		return totalNonSubscribe;
	}

	public void allowNewPosition() {
		lock.lock();
		for(DofusQueueEntry entry : queue) {
			entry.setPosition(entry.getPosition() - 1);
		}
		lock.unlock();
	}

}

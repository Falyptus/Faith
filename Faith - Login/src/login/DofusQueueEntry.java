package login;

import objects.Account;

public class DofusQueueEntry {
	
	private Account account;
	private int position;
	
	public DofusQueueEntry(Account account, int position)
	{
		this.account = account;
		this.position = position;
	}
	
	public Account getAccount() {
		return account;
	}

	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}	

}

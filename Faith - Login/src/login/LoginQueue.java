package login;

import java.util.LinkedList;

import common.SocketManager;

import objects.Account;

public class LoginQueue {
	
	public static LinkedList<AccountInQueue> accountInQueue = new LinkedList<AccountInQueue>();
	public static boolean isProcessing = false;
	
	public static void addAccountInQueue(Account account)
	{
		synchronized(accountInQueue) {
			short newLastPosition = (short) (accountInQueue.size() + 1); //On prends la taille totale de la liste et on incr�mente de 1
			AccountInQueue element = new AccountInQueue(account, newLastPosition); //On cr�e un nouvel objet AccountInQueue
			accountInQueue.addLast(element); //On ajoute � la fin le compte dans la file d'attente
			account.setInstanceInQueue(element); //On ajoute relie les informations du compte dans la file d'attente
			account.setInQueue(true); //On sp�cifie bien au compte qu'il est dans la file d'attente
			SocketManager.SEND_Af_PACKET(account.getLoginThread().getOut(), newLastPosition); //On affiche la position
		}
	}
	
	public static void allocateNewPosition() //On ajoute une nouvelle position apr�s qu'un compte soit trait�.
	{
		synchronized(accountInQueue) {
			short position = 0;
			for(AccountInQueue element : accountInQueue)
			{
				position++;
				element.setPosition(position);
			}
		}
	}
	
	public static void processQueue() 
	{
		isProcessing = true; //On pr�cise qu'on traite la file d'attente pour �viter de perturber la file d'attente
		processingFirstAccount();
		allocateNewPosition();
		isProcessing = false; //On pr�cise qu'on a fini de traiter la file d'attente
	}
	
	public static void processingFirstAccount() //On traite premier compte de la liste
	{
		synchronized(accountInQueue) {
			AccountInQueue first = accountInQueue.getFirst();
			Account account = first.getAccount();
			if (account == null)
			{
				accountInQueue.removeFirst();
				return;
			}
			account.set_reloadedServ(true);
			SocketManager.SEND_CLOSE_QUEUE(account.getLoginThread().getOut());
			SocketManager.SEND_RECO_WITH_HASHKEY_PACKET(account.getLoginThread().getOut(), account.getHashKey());
			SocketManager.SEND_Ad_Ac_AH_AlK_AQ_PACKETS(account.getLoginThread().getOut(), account.getNickname(), 
					account.getGmLvl() > 0 ? 1 : 0, account.getQuestion(), account.getGmLvl());
			first.setPosition((short)0);
			account.setInQueue(false);
			accountInQueue.removeFirst();
		}
	}
	
	public static void sendPacketQueue(Account account) 
	{
		short position = account.getInstanceInQueue().getPosition();

		if (position < 1)
		{
			account.set_reloadedServ(true);
			SocketManager.SEND_CLOSE_QUEUE(account.getLoginThread().getOut());
			SocketManager.SEND_RECO_WITH_HASHKEY_PACKET(account.getLoginThread().getOut(), account.getHashKey());
			SocketManager.SEND_Ad_Ac_AH_AlK_AQ_PACKETS(account.getLoginThread().getOut(), account.getNickname(), 
					account.getGmLvl() > 0 ? 1 : 0, account.getQuestion(), account.getGmLvl());
		}
		else
		{
			SocketManager.SEND_Af_PACKET(account.getLoginThread().getOut(), position);
		}
	}
	
	public static class AccountInQueue {
		
		private Account account = null;
		private short position = 0;
		
		public AccountInQueue(Account aAccount, short aPosition)
		{
			this.account = aAccount;
			this.position = aPosition;
		}

		public Account getAccount() {
	        return account;
        }

		public void setPosition(short position) {
	        this.position = position;
        }

		public short getPosition() {
	        return position;
        }
		
	}

}

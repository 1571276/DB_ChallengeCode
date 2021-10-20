package com.db.awmd.challenge.service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferDto;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	private Lock lock = new ReentrantLock();

	@Autowired
	private EmailNotificationService emailNotificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public void transfer(TransferDto transferDto) throws Exception {
		boolean gotLock = lock.tryLock();
		if (gotLock) {
			try {
				Account fromAccount = getAccount(transferDto.getAccountFrom());
				Account toAccount = getAccount(transferDto.getAccountTo());
				if (fromAccount != null && toAccount != null
						&& fromAccount.getBalance().intValue() >= transferDto.getAmount().intValue()) {
					fromAccount.setBalance(fromAccount.getBalance().subtract(transferDto.getAmount()));
					toAccount.setBalance(toAccount.getBalance().add(transferDto.getAmount()));
					emailNotificationService.notifyAboutTransfer(fromAccount,
							transferDto.getAmount().intValue() + " deposited to account " + toAccount.getAccountId());
					emailNotificationService.notifyAboutTransfer(toAccount, transferDto.getAmount().intValue()
							+ " deposited from account " + fromAccount.getAccountId());
				}else {
					String message ;
					if(fromAccount == null) {
						message = "Account with Account Id :" + transferDto.getAccountFrom() + "does not exist";
					}else if(toAccount == null) {
						message = "Account with Account Id :" + transferDto.getAccountTo() + "does not exist";
					}else{
						message = "Insufficient balance in account " + transferDto.getAccountFrom();
					}
					throw new Exception(message);
				}
			} finally {
				lock.unlock();
			}

		}
	}
}

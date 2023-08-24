package com.skypro.simplebanking.service;

import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.exception.AccountNotFoundException;
import com.skypro.simplebanking.exception.InvalidAmountException;
import com.skypro.simplebanking.exception.WrongCurrencyException;
import com.skypro.simplebanking.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AccountServiceIntegrationTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountRepository);
    }

    @Test
    void createDefaultAccounts_ValidUser_CreatesAccounts() {
        User user = new User();
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArguments()[0]);

        accountService.createDefaultAccounts(user);

        assertEquals(AccountCurrency.values().length, user.getAccounts().size());
        for (AccountCurrency currency : AccountCurrency.values()) {
            boolean accountExists = user.getAccounts().stream()
                    .anyMatch(account -> account.getAccountCurrency().equals(currency));
            assertTrue(accountExists);
        }
    }
    @Test
    void getAccount_AccountNotFound_ThrowsAccountNotFoundException() {
        long userId = 1L;
        long accountId = 1L;
        when(accountRepository.getAccountByUser_IdAndId(userId, accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(userId, accountId));
    }
    @Test
    void validateCurrency_SameCurrency_DoesNotThrowException() {
        long sourceAccount = 1L;
        long destinationAccount = 2L;
        AccountCurrency currency = AccountCurrency.USD;
        Account acc1 = new Account();
        acc1.setAccountCurrency(currency);
        Account acc2 = new Account();
        acc2.setAccountCurrency(currency);
        when(accountRepository.findById(sourceAccount)).thenReturn(Optional.of(acc1));
        when(accountRepository.findById(destinationAccount)).thenReturn(Optional.of(acc2));

        assertDoesNotThrow(() -> accountService.validateCurrency(sourceAccount, destinationAccount));
    }
    @Test
    void validateCurrency_DifferentCurrency_ThrowsWrongCurrencyException() {
        long sourceAccount = 1L;
        long destinationAccount = 2L;
        Account acc1 = new Account();
        acc1.setAccountCurrency(AccountCurrency.USD);
        Account acc2 = new Account();
        acc2.setAccountCurrency(AccountCurrency.EUR);
        when(accountRepository.findById(sourceAccount)).thenReturn(Optional.of(acc1));
        when(accountRepository.findById(destinationAccount)).thenReturn(Optional.of(acc2));

        assertThrows(WrongCurrencyException.class, () -> accountService.validateCurrency(sourceAccount, destinationAccount));
    }
    @Test
    void depositToAccount_InvalidAmount_ThrowsInvalidAmountException() {
        long userId = 1L;
        long accountId = 1L;
        long amount = -50L;
        assertThrows(InvalidAmountException.class, () -> accountService.depositToAccount(userId, accountId, amount));
    }
    @Test
    void withdrawFromAccount_InvalidAmount_ThrowsInvalidAmountException() {
        long userId = 1L;
        long accountId = 1L;
        long amount = -50L;

        assertThrows(InvalidAmountException.class, () -> accountService.withdrawFromAccount(userId, accountId, amount));
    }

}

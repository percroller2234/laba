package model;

import exceptions.*;

public class BankAccount {
    private String accountNumber;
    private double balance;
    private String ownerName;
    
    public BankAccount(String accountNumber, double initialBalance, String ownerName) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.ownerName = ownerName;
    }
    
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }
        balance += amount;
    }
    
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException(amount);
        }

        // ИСПРАВЛЕНИЕ 1: Неправильная проверка баланса
        // Было: if (balance < 0) - эта проверка всегда false для положительного баланса
        // Стало: if (balance < amount) - проверяем, хватает ли средств для снятия
        if (balance < amount) {
            throw new InsufficientFundsException(balance, amount);
        }
        balance -= amount;
    }
    
    public void transfer(BankAccount target, double amount) {
        if (target == null) {
            throw new InvalidAccountException("Целевой счет не существует");
        }
        
        // ИСПРАВЛЕНИЕ 2: Неправильный порядок операций в transfer
        // Было: сначала deposit в целевой счет, потом withdraw из текущего
        // Проблема: если withdraw не удастся, деньги уже будут на целевом счете
        // Стало: сначала withdraw из текущего, потом deposit в целевой
        // Это обеспечивает атомарность операции
        this.withdraw(amount);  // Сначала снимаем с текущего счета
        target.deposit(amount); // Потом вносим на целевой счет
    }
    
    public void applyInterest(double rate) {
        // ИСПРАВЛЕНИЕ 3: Неправильный расчет процентов и вызов deposit
        // Было: double interest = balance * rate;
        //        deposit(balance + interest); - это вносит текущий баланс + проценты, что неверно
        // Стало: начисляем проценты напрямую к балансу
        if (rate <= 0) {
            throw new InvalidAmountException(rate);
        }
        double interest = balance * rate;
        balance += interest;  // Просто добавляем проценты к балансу
    }
    
    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public String toString() {
        return "Счет: " + accountNumber + "\n" + 
            "Владелец: " + ownerName + "\n" + 
            "Баланс: " + balance;
    }
}

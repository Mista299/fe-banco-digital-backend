package fe.banco_digital.dto;

import java.math.BigDecimal;

public class ProfileDTO {

    private String fullName;
    private String identificationNumber;
    private String accountNumber;
    private BigDecimal balance;

    // ✅ Constructor vacío (necesario para Spring / Jackson)
    public ProfileDTO() {
    }

    // ✅ Constructor con parámetros
    public ProfileDTO(String fullName,
                      String identificationNumber,
                      String accountNumber,
                      BigDecimal balance) {
        this.fullName = fullName;
        this.identificationNumber = identificationNumber;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    // ✅ GETTERS
    public String getFullName() {
        return fullName;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    // ✅ SETTERS
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
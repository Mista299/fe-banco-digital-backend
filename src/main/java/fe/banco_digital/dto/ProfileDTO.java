package fe.banco_digital.dto;

import java.math.BigDecimal;

public class ProfileDTO {

    private Long idCliente;
    private String fullName;
    private String identificationNumber;
    private String accountNumber;
    private BigDecimal balance;
    private String email;
    private String telefono;

    public ProfileDTO() {}

    public ProfileDTO(Long idCliente, String fullName, String identificationNumber,
                      String accountNumber, BigDecimal balance,
                      String email, String telefono) {
        this.idCliente = idCliente;
        this.fullName = fullName;
        this.identificationNumber = identificationNumber;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.email = email;
        this.telefono = telefono;
    }

    public Long getIdCliente() { return idCliente; }
    public String getFullName() { return fullName; }
    public String getIdentificationNumber() { return identificationNumber; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }

    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setIdentificationNumber(String identificationNumber) { this.identificationNumber = identificationNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setEmail(String email) { this.email = email; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}

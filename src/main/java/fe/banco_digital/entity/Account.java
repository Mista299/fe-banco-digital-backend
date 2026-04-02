package fe.banco_digital.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroCuenta;
    private BigDecimal saldo;
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ===== GETTERS =====
    public Long getId() {
        return id;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public boolean isActive() {
        return active;
    }

    public User getUser() {
        return user;
    }

    // ===== SETTERS =====
    public void setId(Long id) {
        this.id = id;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

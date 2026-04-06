package fe.banco_digital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ActualizarClienteDTO {

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "El teléfono debe contener solo números y tener entre 7 y 15 dígitos")
    private String telefono;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Ingrese un correo electrónico válido")
    private String email;

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

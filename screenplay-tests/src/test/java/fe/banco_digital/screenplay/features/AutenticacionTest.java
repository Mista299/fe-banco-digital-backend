package fe.banco_digital.screenplay.features;

import fe.banco_digital.screenplay.actors.Actores;
import fe.banco_digital.screenplay.questions.ElEstadoDeRespuesta;
import fe.banco_digital.screenplay.tasks.auth.CerrarSesion;
import fe.banco_digital.screenplay.tasks.auth.IniciarSesion;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Autenticación de usuarios")
class AutenticacionTest {

    private Actor cliente;
    private Actor admin;

    @BeforeEach
    void prepararActores() {
        cliente = Actores.cliente();
        admin   = Actores.admin();
    }

    @AfterEach
    void limpiar() {
        if (cliente != null) cliente.attemptsTo(CerrarSesion.ahora());
        if (admin   != null) admin.attemptsTo(CerrarSesion.ahora());
    }

    @Test
    @DisplayName("El cliente puede iniciar sesión con credenciales válidas")
    void clienteIniciaSession() {
        givenThat(cliente).wasAbleTo(IniciarSesion.conSusCredenciales(cliente));
        cliente.should(seeThat(ElEstadoDeRespuesta.recibida(), equalTo(200)));
    }

    @Test
    @DisplayName("El administrador puede iniciar sesión con credenciales válidas")
    void adminIniciaSession() {
        givenThat(admin).wasAbleTo(IniciarSesion.conSusCredenciales(admin));
        admin.should(seeThat(ElEstadoDeRespuesta.recibida(), equalTo(200)));
    }

    @Test
    @DisplayName("Login falla con credenciales incorrectas")
    void loginFallaConCredencialesInvalidas() {
        givenThat(cliente).wasAbleTo(IniciarSesion.comoUsuario("bryan", "claveIncorrecta"));
        cliente.should(seeThat(ElEstadoDeRespuesta.recibida(), equalTo(401)));
    }
}

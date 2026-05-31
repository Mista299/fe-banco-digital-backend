package fe.banco_digital.screenplay.tasks.auth;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;

import java.util.Map;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class IniciarSesion implements Task {

    private final String username;
    private final String password;

    private IniciarSesion(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static IniciarSesion comoUsuario(String username, String password) {
        return instrumented(IniciarSesion.class, username, password);
    }

    public static IniciarSesion conSusCredenciales(Actor actor) {
        String username = actor.recall("username");
        String password = actor.recall("password");
        return instrumented(IniciarSesion.class, username, password);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            Post.to("/api/v1/auth/login")
                .with(request -> request
                    .contentType("application/json")
                    .body(Map.of("username", username, "password", password)))
        );
    }
}

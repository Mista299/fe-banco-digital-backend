package fe.banco_digital.screenplay.actors;

import fe.banco_digital.screenplay.abilities.LlamarAlBackend;
import net.serenitybdd.screenplay.Actor;
import net.thucydides.model.environment.SystemEnvironmentVariables;
import net.thucydides.model.util.EnvironmentVariables;

public class Actores {

    private static final EnvironmentVariables env =
            SystemEnvironmentVariables.createEnvironmentVariables();

    public static Actor cliente() {
        String username = env.getProperty("actors.cliente.username", "bryan");
        String password = env.getProperty("actors.cliente.password", "bryan123");
        Actor actor = Actor.named("Cliente " + username)
                .whoCan(LlamarAlBackend.comoCliente());
        actor.remember("username", username);
        actor.remember("password", password);
        return actor;
    }

    public static Actor admin() {
        String username = env.getProperty("actors.admin.username", "patricia");
        String password = env.getProperty("actors.admin.password", "patricia123");
        Actor actor = Actor.named("Administrador " + username)
                .whoCan(LlamarAlBackend.comoAdmin());
        actor.remember("username", username);
        actor.remember("password", password);
        return actor;
    }
}

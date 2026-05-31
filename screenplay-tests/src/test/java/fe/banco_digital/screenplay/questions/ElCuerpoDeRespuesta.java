package fe.banco_digital.screenplay.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

public class ElCuerpoDeRespuesta implements Question<String> {

    public static ElCuerpoDeRespuesta recibido() {
        return new ElCuerpoDeRespuesta();
    }

    @Override
    public String answeredBy(Actor actor) {
        return LastResponse.received().answeredBy(actor).body().asString();
    }
}

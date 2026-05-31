package fe.banco_digital.screenplay.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.rest.questions.LastResponse;

public class ElEstadoDeRespuesta implements Question<Integer> {

    public static ElEstadoDeRespuesta recibida() {
        return new ElEstadoDeRespuesta();
    }

    @Override
    public Integer answeredBy(Actor actor) {
        return LastResponse.received().answeredBy(actor).statusCode();
    }
}

package fe.banco_digital.screenplay.abilities;

import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.thucydides.model.environment.SystemEnvironmentVariables;

public class LlamarAlBackend {

    public static CallAnApi comoCliente() {
        return CallAnApi.at(baseUrl());
    }

    public static CallAnApi comoAdmin() {
        return CallAnApi.at(baseUrl());
    }

    static String baseUrl() {
        String sysProp = System.getProperty("restapi.baseurl");
        if (sysProp != null && !sysProp.isBlank()) return sysProp;
        return SystemEnvironmentVariables.createEnvironmentVariables()
                .getProperty("restapi.baseurl", "http://localhost:8080");
    }
}

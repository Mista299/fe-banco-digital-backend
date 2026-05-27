package fe.banco_digital.controller;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.service.ProfileService;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/perfil")
public class PerfilController {

    private final ProfileService profileService;

    public PerfilController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<EntityModel<ProfileDTO>> obtenerPerfilActual(Principal principal) {
        ProfileDTO perfil = profileService.getProfileByUsername(principal.getName());
        EntityModel<ProfileDTO> model = EntityModel.of(perfil,
                linkTo(methodOn(PerfilController.class).obtenerPerfilActual(null)).withSelfRel(),
                linkTo(methodOn(ClienteController.class).dashboard(null)).withRel("dashboard")
        );
        return ResponseEntity.ok(model);
    }
}

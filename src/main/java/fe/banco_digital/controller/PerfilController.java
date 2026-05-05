package fe.banco_digital.controller;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/perfil")
public class PerfilController {

    private final ProfileService profileService;

    public PerfilController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> obtenerPerfilActual(Principal principal) {
        ProfileDTO perfil = profileService.getProfileByUsername(principal.getName());
        return ResponseEntity.ok(perfil);
    }
}

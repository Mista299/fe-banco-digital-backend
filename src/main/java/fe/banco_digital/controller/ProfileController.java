package fe.banco_digital.controller;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDTO> getProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        ProfileDTO profile = profileService.getProfile(userId, usuarioAutenticado.getUsername());
        return ResponseEntity.ok(profile);
    }
}

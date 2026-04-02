package fe.banco_digital.controller;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable Long userId) {
        ProfileDTO profile = profileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }
}

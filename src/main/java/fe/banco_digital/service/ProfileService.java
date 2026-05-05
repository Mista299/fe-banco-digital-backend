package fe.banco_digital.service;

import fe.banco_digital.dto.ProfileDTO;

public interface ProfileService {
    ProfileDTO getProfile(Long userId);
    ProfileDTO getProfileByUsername(String username);
}
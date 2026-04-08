package fe.banco_digital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    ProfileService profileService;

    @InjectMocks
    ProfileController controller;

    MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getProfile_returnsOkAndJson() throws Exception {
        ProfileDTO dto = new ProfileDTO();
        dto.setFullName("Test User");
        dto.setIdentificationNumber("DOC123");
        dto.setAccountNumber("ACC1");
        dto.setBalance(new BigDecimal("10.00"));

        when(profileService.getProfile(anyLong())).thenReturn(dto);

        mockMvc.perform(get("/api/profile/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(dto)));
    }
}

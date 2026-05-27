package fe.banco_digital.service;

import fe.banco_digital.dto.AuditoriaDTO;
import fe.banco_digital.entity.Auditoria;
import fe.banco_digital.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaServiceImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public List<AuditoriaDTO> obtenerTodos() {
        return auditoriaRepository.findAllByOrderByFechaDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private AuditoriaDTO toDTO(Auditoria a) {
        String rol = a.getUsuario().getRoles().stream()
                .map(r -> r.getNombre().name())
                .findFirst()
                .orElse("DESCONOCIDO");
        return new AuditoriaDTO(
                a.getIdAuditoria(),
                a.getAccion(),
                a.getUsuario().getUsername(),
                rol,
                a.getFecha(),
                a.getDetalle()
        );
    }
}

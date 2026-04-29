package fe.banco_digital.listener;

import fe.banco_digital.entity.Auditoria;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.repository.AuditoriaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuditoriaEventListener {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaEventListener(AuditoriaRepository auditoriaRepository,
                                   UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Async("executorAuditoria")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuditoria(AuditoriaEvent evento) {
        Auditoria auditoria = new Auditoria();
        auditoria.setAccion(evento.getAccion());
        auditoria.setDetalle(evento.getDetalle());
        auditoria.setUsuario(
                usuarioRepository.findById(evento.getIdUsuario()).orElseThrow());
        auditoriaRepository.save(auditoria);
    }
}

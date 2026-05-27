package fe.banco_digital.controller;

import fe.banco_digital.dto.AuditoriaDTO;
import fe.banco_digital.service.AuditoriaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/admin/auditoria")
@Tag(name = "Admin - Auditoría", description = "Log de auditoría interno del sistema")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<AuditoriaDTO>>> obtenerTodos() {
        List<EntityModel<AuditoriaDTO>> items = auditoriaService.obtenerTodos().stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(AuditoriaController.class).obtenerTodos()).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(items,
                linkTo(methodOn(AuditoriaController.class).obtenerTodos()).withSelfRel()));
    }
}

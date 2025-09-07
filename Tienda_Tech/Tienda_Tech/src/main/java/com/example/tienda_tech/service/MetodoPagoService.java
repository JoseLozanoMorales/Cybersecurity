package com.example.tienda_tech.service;

import com.example.tienda_tech.dto.MetodoPagoCreateRequest;
import com.example.tienda_tech.dto.MetodoPagoResponse;
import com.example.tienda_tech.dto.MetodoPagoUpdateRequest;
import com.example.tienda_tech.exception.BadRequestException;
import com.example.tienda_tech.exception.ConflictException;
import com.example.tienda_tech.exception.NotFoundException;
import com.example.tienda_tech.repository.MetodoPagoSpRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetodoPagoService {

    private final MetodoPagoSpRepository repo;

    public MetodoPagoService(MetodoPagoSpRepository repo) {
        this.repo = repo;
    }

    public List<MetodoPagoResponse> listar(Integer userId) {
        return repo.listarPorUsuario(userId);
    }

    @Transactional
    public void agregar(Integer userId, MetodoPagoCreateRequest req) {
        try {
            repo.callAgregar(req.getNumeroTarjeta(), req.getFechaExpiracion(), req.getTipoId(), userId);
            if (Boolean.TRUE.equals(req.getPreferido())) {
                // marca preferido al último insertado del usuario:
                // opción simple: volver a consultar y tomar el más reciente
                var lista = repo.listarPorUsuario(userId);
                if (!lista.isEmpty()) repo.actualizarPreferido(userId, lista.get(0).getId());
            }
        } catch (UncategorizedSQLException e) {
            handlePgException(e.getSQLException());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Conflicto de datos (tarjeta duplicada o FK).");
        }
    }

    @Transactional
    public void actualizar(Integer userId, Integer metodoId, MetodoPagoUpdateRequest req) {
        try {
            repo.callActualizar(metodoId, userId, req.getNumeroTarjeta(), req.getFechaExpiracion(),
                                req.getTipoId(), req.getHabilitado());
            // Nota: si no afectó por no ser dueño, el SP no lanza, pero lógicamente no cambia nada.
            // Si quieres confirmar filas afectadas, mueve este control al SP o añade verificación extra con SELECT.
        } catch (UncategorizedSQLException e) {
            handlePgException(e.getSQLException());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Conflicto de datos (tarjeta duplicada o FK).");
        }
    }

    @Transactional
    public void marcarPreferido(Integer userId, Integer metodoId) {
        int n = repo.actualizarPreferido(userId, metodoId);
        if (n == 0) throw new NotFoundException("Método no encontrado o no pertenece al usuario.");
    }

    private void handlePgException(Throwable sqlEx) {
        // Inspecciona mensajes emitidos por tus SP
        String msg = sqlEx.getMessage() != null ? sqlEx.getMessage() : "";
        if (msg.contains("no existe ese tipo de metodo de pago") || msg.contains("No existe ese tipo de pago")) {
            throw new BadRequestException("Tipo de método de pago inválido.");
        }
        if (msg.contains("ya existe ese numero de tarjeta")) {
            throw new ConflictException("Esa tarjeta ya está registrada.");
        }
    }
}

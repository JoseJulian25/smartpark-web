package com.parking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parking.dto.UsuarioDTO;
import com.parking.dto.UsuarioResponseDTO;
import com.parking.entity.Rol;
import com.parking.entity.Usuario;
import com.parking.repository.RolRepository;
import com.parking.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final String ROL_ADMIN = "admin";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuarios(Boolean activo) {
        List<Usuario> usuarios = activo == null
                ? usuarioRepository.findAllByEliminadoFalseOrderByIdAsc()
                : usuarioRepository.findAllByActivoAndEliminadoFalseOrderByIdAsc(activo);

        return usuarios.stream().map(this::toDto).toList();
    }

    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioDTO dto) {
        String username = normalize(dto.getUsername());
        String nombre = normalize(dto.getNombre());
        String password = dto.getPassword().trim();

        validarUsernameDisponible(username, null);
        Rol rol = obtenerRol(dto.getRol());

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNombre(nombre);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setEliminado(false);
        usuario.setFechaEliminacion(null);

        Usuario creado = usuarioRepository.save(usuario);
        return toDto(creado);
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioDTO dto) {
        Usuario usuario = obtenerUsuarioNoEliminado(id);

        String username = normalize(dto.getUsername());
        String nombre = normalize(dto.getNombre());

        validarUsernameDisponible(username, id);
        Rol rol = obtenerRol(dto.getRol());

        usuario.setUsername(username);
        usuario.setNombre(nombre);
        usuario.setRol(rol);

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return toDto(actualizado);
    }

    @Transactional
    public UsuarioResponseDTO actualizarEstado(Long id, Boolean activo) {
        Usuario usuario = obtenerUsuarioNoEliminado(id);

        if (Boolean.FALSE.equals(activo) && Boolean.TRUE.equals(usuario.getActivo())) {
            validarNoSeaUltimoAdminActivo(usuario);
        }

        usuario.setActivo(activo);
        Usuario actualizado = usuarioRepository.save(usuario);
        return toDto(actualizado);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = obtenerUsuarioNoEliminado(id);

        if (Boolean.TRUE.equals(usuario.getActivo())) {
            validarNoSeaUltimoAdminActivo(usuario);
        }

        usuario.setActivo(false);
        usuario.setEliminado(true);
        usuario.setFechaEliminacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    private Usuario obtenerUsuarioNoEliminado(Long id) {
        return usuarioRepository.findByIdAndEliminadoFalse(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    private Rol obtenerRol(String nombreRol) {
        String rolNormalizado = normalize(nombreRol);
        return rolRepository.findByNombreIgnoreCase(rolNormalizado)
                .orElseThrow(() -> new IllegalArgumentException("El rol indicado no existe"));
    }

    private void validarUsernameDisponible(String username, Long idActual) {
        boolean existe = idActual == null
                ? usuarioRepository.existsByUsernameIgnoreCaseAndEliminadoFalse(username)
                : usuarioRepository.existsByUsernameIgnoreCaseAndEliminadoFalseAndIdNot(username, idActual);

        if (existe) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }
    }

    private void validarNoSeaUltimoAdminActivo(Usuario usuario) {
        if (usuario.getRol() == null || usuario.getRol().getNombre() == null) {
            return;
        }

        if (!usuario.getRol().getNombre().equalsIgnoreCase(ROL_ADMIN)) {
            return;
        }

        long adminsActivos = usuarioRepository.countByRolNombreIgnoreCaseAndActivoTrueAndEliminadoFalse(ROL_ADMIN);
        if (adminsActivos <= 1) {
            throw new IllegalStateException("No se puede desactivar o eliminar el único administrador activo");
        }
    }

    private String normalize(String value) {
        return value.trim();
    }

    private UsuarioResponseDTO toDto(Usuario usuario) {
        String rol = usuario.getRol() != null && usuario.getRol().getNombre() != null
                ? usuario.getRol().getNombre().toLowerCase(Locale.ROOT)
                : null;

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                rol,
                usuario.getActivo(),
                usuario.getFechaCreacion()
        );
    }
}

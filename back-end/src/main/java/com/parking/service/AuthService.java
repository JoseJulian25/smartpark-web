package com.parking.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.parking.config.JwtUtil;
import com.parking.dto.LoginRequestDTO;
import com.parking.dto.LoginResponseDTO;
import com.parking.dto.MeResponseDTO;
import com.parking.entity.Usuario;
import com.parking.repository.UsuarioRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    public LoginResponseDTO login(LoginRequestDTO dto) {
        // 1. Verifica credenciales — lanza BadCredentialsException si son incorrectas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        // 2. Credenciales correctas → generar token
        String token = jwtUtil.generateToken(dto.getUsername());

        // 3. Cargar datos completos del usuario para el response
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + dto.getUsername()));

        return new LoginResponseDTO(
                token,
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol().getNombre()
        );
    }

    public MeResponseDTO getMe(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return new MeResponseDTO(
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol().getNombre()
        );
    }
}


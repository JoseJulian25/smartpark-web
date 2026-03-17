-- Script de datos iniciales para pruebas
-- Ejecutar SOLO si la BD está vacía (primera vez o después de un reset)
--
-- Las contraseñas están encriptadas con BCrypt:
--   admin123    -> hash abajo
--   operador123 -> hash abajo

-- Roles
INSERT INTO roles (nombre) VALUES ('ADMIN')     ON CONFLICT (nombre) DO NOTHING;
INSERT INTO roles (nombre) VALUES ('OPERADOR')  ON CONFLICT (nombre) DO NOTHING;

-- Usuarios
INSERT INTO usuarios (username, nombre, password, rol_id, activo)
VALUES (
    'admin',
    'Administrador',
    '$2a$10$VufadKFm1fm/8GKTIn87MugS.QyQbs3WXm3/s84nbtLF1dy1Po7L2',
    (SELECT id FROM roles WHERE nombre = 'ADMIN'),
    true
) ON CONFLICT (username) DO NOTHING;

INSERT INTO usuarios (username, nombre, password, rol_id, activo)
VALUES (
    'operador',
    'Operador',
    '$2a$10$I045ZWAtaN1EyWYNIUxN7ec3kVm4hRkzl3X47j.gdLSc97HnbCMfW',
    (SELECT id FROM roles WHERE nombre = 'OPERADOR'),
    true
) ON CONFLICT (username) DO NOTHING;

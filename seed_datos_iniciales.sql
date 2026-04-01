BEGIN;

-- Script general de datos iniciales para pruebas del sistema.
-- Contraseñas (BCrypt):
--   admin123
--   operador123

-- Roles
INSERT INTO roles (nombre) VALUES ('ADMIN') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO roles (nombre) VALUES ('OPERADOR') ON CONFLICT (nombre) DO NOTHING;

-- Tipos de vehiculo
INSERT INTO tipos_vehiculo (nombre) VALUES ('CARRO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO tipos_vehiculo (nombre) VALUES ('MOTO') ON CONFLICT (nombre) DO NOTHING;

-- Configuracion de empresa
INSERT INTO empresas (nombre, rnc, direccion, telefono, email)
SELECT 'SmartPark SRL', '131-00000-1', 'Av. 27 de Febrero, Santo Domingo', '809-555-0000', 'info@smartpark.com'
WHERE NOT EXISTS (SELECT 1 FROM empresas);

-- Tarifas base por tipo de vehiculo
INSERT INTO tarifas (tipo_vehiculo_id, minutos_fraccion, minutos_tolerancia, minutos_minimo, precio_por_fraccion)
VALUES (
    (SELECT id FROM tipos_vehiculo WHERE nombre = 'CARRO'),
    30,
    10,
    30,
    100.00
) ON CONFLICT (tipo_vehiculo_id) DO NOTHING;

INSERT INTO tarifas (tipo_vehiculo_id, minutos_fraccion, minutos_tolerancia, minutos_minimo, precio_por_fraccion)
VALUES (
    (SELECT id FROM tipos_vehiculo WHERE nombre = 'MOTO'),
    30,
    10,
    30,
    60.00
) ON CONFLICT (tipo_vehiculo_id) DO NOTHING;

-- Estados de espacio UTI
INSERT INTO estados_espacio (nombre) VALUES ('LIBRE') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO estados_espacio (nombre) VALUES ('OCUPADO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO estados_espacio (nombre) VALUES ('RESERVADO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO estados_espacio (nombre) VALUES ('MANTENIMIENTO') ON CONFLICT (nombre) DO NOTHING;

-- Estados de ticket
INSERT INTO estados_ticket (nombre) VALUES ('ACTIVO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO estados_ticket (nombre) VALUES ('CERRADO') ON CONFLICT (nombre) DO NOTHING;

-- Estados de reserva
INSERT INTO estados_reserva (nombre) VALUES ('RESERVADA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO estados_reserva (nombre) VALUES ('CANCELADA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO estados_reserva (nombre) VALUES ('PENDIENTE') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO estados_reserva (nombre) VALUES ('EXPIRADA') ON CONFLICT (nombre) DO NOTHING;

-- Usuarios base
INSERT INTO usuarios (username, nombre, password, rol_id, activo, eliminado, fecha_creacion, fecha_eliminacion)
VALUES (
    'admin',
    'Administrador',
    '$2a$10$VufadKFm1fm/8GKTIn87MugS.QyQbs3WXm3/s84nbtLF1dy1Po7L2',
    (SELECT id FROM roles WHERE nombre = 'ADMIN'),
    true,
    false,
    NOW(),
    NULL
) ON CONFLICT (username) DO NOTHING;

INSERT INTO usuarios (username, nombre, password, rol_id, activo, eliminado, fecha_creacion,fecha_eliminacion)
VALUES (
    'operador',
    'Operador',
    '$2a$10$I045ZWAtaN1EyWYNIUxN7ec3kVm4hRkzl3X47j.gdLSc97HnbCMfW',
    (SELECT id FROM roles WHERE nombre = 'OPERADOR'),
    true,
    false,
    NOW()
    ,NULL
) ON CONFLICT (username) DO NOTHING;

-- Espacios base
INSERT INTO espacios (codigo_espacio, tipo_vehiculo_id, estado_id, activo)
VALUES (
    'C-001',
    (SELECT id FROM tipos_vehiculo WHERE nombre = 'CARRO'),
    (SELECT id FROM estados_espacio WHERE nombre = 'OCUPADO'),
    true
) ON CONFLICT (codigo_espacio) DO NOTHING;

INSERT INTO espacios (codigo_espacio, tipo_vehiculo_id, estado_id, activo)
VALUES (
    'C-002',
    (SELECT id FROM tipos_vehiculo WHERE nombre = 'CARRO'),
    (SELECT id FROM estados_espacio WHERE nombre = 'LIBRE'),
    true
) ON CONFLICT (codigo_espacio) DO NOTHING;

INSERT INTO espacios (codigo_espacio, tipo_vehiculo_id, estado_id, activo)
VALUES (
    'M-001',
    (SELECT id FROM tipos_vehiculo WHERE nombre = 'MOTO'),
    (SELECT id FROM estados_espacio WHERE nombre = 'LIBRE'),
    true
) ON CONFLICT (codigo_espacio) DO NOTHING;

-- Ticket activo de prueba para visualizar ticketActivo en listado de espacios.
INSERT INTO tickets (
    codigo_ticket,
    placa,
    tipo_vehiculo_id,
    espacio_id,
    hora_entrada,
    estado_id,
    creado_por
)
VALUES (
    'T-0001',
    'ABC123',
    (SELECT id FROM tipos_vehiculo WHERE nombre = 'CARRO'),
    (SELECT id FROM espacios WHERE codigo_espacio = 'C-001'),
    NOW() - INTERVAL '30 minutes',
    (SELECT id FROM estados_ticket WHERE nombre = 'ACTIVO'),
    (SELECT id FROM usuarios WHERE username = 'admin')
) ON CONFLICT (codigo_ticket) DO NOTHING;

COMMIT;

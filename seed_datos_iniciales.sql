BEGIN;


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
INSERT INTO estados_ticket (nombre) VALUES ('ANULADO') ON CONFLICT (nombre) DO NOTHING;

-- Estados de reserva
INSERT INTO estados_reserva (nombre) VALUES ('ACTIVA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO estados_reserva (nombre) VALUES ('CANCELADA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO estados_reserva (nombre) VALUES ('PENDIENTE') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO estados_reserva (nombre) VALUES ('FINALIZADA') ON CONFLICT (nombre) DO NOTHING;

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




COMMIT;

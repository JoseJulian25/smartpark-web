# User Level Specification - Sistema de Gestion de Parqueo

## 1. Proposito
Definir funcionalidades a nivel de usuario para administrar un parqueo de forma simple, confiable y completa para un proyecto final universitario.

## 2. Alcance del sistema
El sistema permite:
- Registrar entradas y salidas de vehiculos.
- Calcular cobros automaticamente segun tiempo de estancia.
- Gestionar espacios numerados (libres, ocupados, reservados).
- Administrar reservas.
- Emitir ticket con numero o codigo unico.
- Consultar historial y reportes basicos.

## 3. Tipos de usuario
### 3.1 Administrador
Puede configurar tarifas, gestionar usuarios, ver reportes e historial y administrar espacios.

### 3.2 Operador de caja
Puede registrar entradas, registrar salidas y cobros, gestionar reservas y consultar dashboard operativo.

## 4. Reglas de negocio principales
1. El parqueo admite carros y motos.
2. Los espacios son numerados y cada vehiculo ocupa un solo espacio.
3. No se puede registrar entrada si no hay espacio libre compatible.
4. Una placa no puede tener mas de un ticket activo al mismo tiempo.
5. Cada ingreso genera un ticket unico.
6. Un espacio no puede estar ocupado y reservado a la vez.
7. Al confirmar salida pagada, el espacio se libera automaticamente.
8. Toda accion relevante queda en historial.

## 5. Politica de cobro recomendada
- Cobro por fraccion de 30 minutos.
- Tarifa diferenciada por tipo de vehiculo (carro o moto).
- Tolerancia de 10 minutos en salida.
- Cobro minimo de 30 minutos.

## 6. Modulos funcionales
1. Autenticacion: inicio y cierre de sesion, control por rol.
2. Dashboard: cupos, ocupacion e ingresos del dia.
3. Registro de entrada: placa, tipo, hora, espacio y ticket.
4. Registro de salida y cobro: busqueda por ticket o placa, calculo y pago.
5. Gestion de espacios: estados y disponibilidad.
6. Gestion de reservas: crear, confirmar llegada, cancelar y expirar.
7. Tarifas: configuracion por tipo de vehiculo.
8. Historial y reportes basicos.

## 7. Flujos criticos
1. Entrada normal: registrar, asignar espacio, generar ticket.
2. Salida y cobro: buscar ticket, calcular monto, confirmar pago, liberar espacio.
3. Reserva con llegada: crear reserva, confirmar check-in, convertir a ingreso activo.
4. Parqueo lleno: bloquear entrada y notificar sin cupos.

## 8. Pantallas minimas
1. Login.
2. Dashboard.
3. Nueva entrada.
4. Salida y cobro.
5. Espacios numerados.
6. Reservas.
7. Historial y reportes.
8. Configuracion de tarifas.
9. Gestion de usuarios (admin).

## 9. Criterios de aceptacion
1. Flujo completo entrada-salida-cobro funciona sin errores.
2. Calculo de tarifa correcto en casos normales.
3. Estados de espacios consistentes.
4. Reservas funcionales (crear, confirmar, cancelar o expirar).
5. Reportes basicos visibles y entendibles.
6. Control de acceso por roles activo.

## 10. Prioridad de implementacion (MVP)
Fase 1 (obligatoria): autenticacion, entradas-salidas-cobro, espacios, ticket, tarifas.
Fase 2 (si hay tiempo): reservas completas, reportes ampliados y mejoras de UX.

---
Documento orientado a nivel usuario (no tecnico) para guiar analisis, diseno funcional y defensa academica del sistema.

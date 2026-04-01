import client from "./client";

export const getEspaciosDisponibles = async () => {
  const { data } = await client.get("/reservas/espacios-disponibles");
  return data;
};

export const crearReserva = async (payload) => {
  const { data } = await client.post("/reservas", payload);
  return data;
};

export const buscarReservaPorCodigo = async (codigo) => {
  const { data } = await client.get(`/reservas/${codigo}`);
  return data;
};

export const confirmarLlegada = async (codigo) => {
  const { data } = await client.post(`/reservas/${codigo}/confirmar`);
  return data;
};

export const registrarSalida = async (codigo) => {
  const { data } = await client.post(`/reservas/${codigo}/salida`);
  return data;
};

export const getReservasActivas = async () => {
  const { data } = await client.get("/reservas/activas");
  return data;
};

export const getReservasPendientes = async () => {
  const { data } = await client.get("/reservas/pendientes");
  return data;
};

export const getHistorialReservas = async () => {
  const { data } = await client.get("/reservas/historial");
  return data;
};

export const cancelarReserva = async (id) => {
  const { data } = await client.delete(`/reservas/${id}`);
  return data;
};
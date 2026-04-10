import client from "./client";

export const getEspaciosDisponibles = async () => {
  const { data } = await client.get("/espacios");
  return data;
};

export const crearReserva = async (payload) => {
  const { data } = await client.post("/reservas", payload);
  return data;
};

export const buscarReservaPorCodigo = async (codigo) => {
  const { data } = await client.get(`/reservas/codigo/${codigo}`);
  return data;
};

export const confirmarLlegada = async (id) => {
  const { data } = await client.patch(`/reservas/${id}/estado`, {
    estado: "ACTIVA"
  });
  return data;
};


export const registrarSalida = async (codigo) => {
  const { data } = await client.patch(`/reservas/codigo/${codigo}/estado`, {
    estado: "FINALIZADA"
  });
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
  const { data } = await client.patch(`/reservas/${id}/estado`, {
    estado: "CANCELADA"
  });
  return data;
};
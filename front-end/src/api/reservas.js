import client from "./client";

export const crearReserva = async (payload) => {
  const { data } = await client.post("/reservas", payload);
  return data;
};

export const buscarReservaPorCodigo = async (codigo) => {
  const { data } = await client.get(`/reservas/${codigo}`);
  return data;
};

export const confirmarLlegada = async (codigoReserva) => {
  const { data } = await client.patch(`/reservas/${codigoReserva}/confirmar-llegada`);
  return data;
};

export const getReservas = async () => {
  const { data } = await client.get("/reservas");
  return data;
};

export const cancelarReserva = async (codigoReserva, motivoCancelacion) => {
  const { data } = await client.patch(`/reservas/${codigoReserva}/cancelar`, {
    motivoCancelacion
  });
  return data;
};

export const reenviarCorreoReserva = async (codigoReserva) => {
  const { data } = await client.patch(`/reservas/${codigoReserva}/reenviar-correo`);
  return data;
};
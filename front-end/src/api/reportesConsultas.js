import client from "./client";
import { buildParams } from "./reportesCommon";

export const consultarPorPlaca = async (placa) => {
  const { data } = await client.get("/reportes/consultas/placa", {
    params: buildParams({ placa }),
  });
  return data;
};

export const consultarPorTicket = async (codigoTicket) => {
  const { data } = await client.get(`/reportes/consultas/ticket/${codigoTicket}`);
  return data;
};

export const consultarPagoPorTicket = async (codigoTicket) => {
  const { data } = await client.get(`/reportes/consultas/pago/${codigoTicket}`);
  return data;
};

export const consultarPorReserva = async (codigoReserva) => {
  const { data } = await client.get(`/reportes/consultas/reserva/${codigoReserva}`);
  return data;
};

export const getConsultasTicketsPorFecha = async (params = {}) => {
  const { data } = await client.get("/reportes/consultas/tickets", {
    params: buildParams(params),
  });
  return data;
};

export const getConsultasReservasPorFecha = async (params = {}) => {
  const { data } = await client.get("/reportes/consultas/reservas", {
    params: buildParams(params),
  });
  return data;
};

export const getConsultasPagosPorFecha = async (params = {}) => {
  const { data } = await client.get("/reportes/consultas/pagos", {
    params: buildParams(params),
  });
  return data;
};

export const getConsultasVehiculosPorFecha = async (params = {}) => {
  const { data } = await client.get("/reportes/consultas/vehiculos", {
    params: buildParams(params),
  });
  return data;
};


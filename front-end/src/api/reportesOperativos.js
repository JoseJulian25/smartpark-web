import client from "./client";
import { buildParams } from "./reportesCommon";

export const getEntradasPorHora = async (params = {}) => {
  const { data } = await client.get("/reportes/operativos/entradas-por-hora", {
    params: buildParams(params),
  });
  return data;
};

export const getSalidasPorHora = async (params = {}) => {
  const { data } = await client.get("/reportes/operativos/salidas-por-hora", {
    params: buildParams(params),
  });
  return data;
};

export const getTicketsActivosReporte = async (params = {}) => {
  const { data } = await client.get("/reportes/operativos/tickets-activos", {
    params: buildParams(params),
  });
  return data;
};

export const getEstadiasLargas = async (umbralMinutos, usuarioId) => {
  const { data } = await client.get("/reportes/operativos/estadias-largas", {
    params: buildParams({ umbralMinutos, usuarioId }),
  });
  return data;
};

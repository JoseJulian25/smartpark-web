import client from "./client";
import { buildParams } from "./reportesCommon";

export const getReservasPorEstadoReporte = async (params = {}) => {
  const { data } = await client.get("/reportes/reservas/por-estado", {
    params: buildParams(params),
  });
  return data;
};


export const getReservasProximasReporte = async (proximosMinutos) => {
  const { data } = await client.get("/reportes/reservas/proximas", {
    params: buildParams({ proximosMinutos }),
  });
  return data;
};

export const getCancelacionesDetalleReporte = async (params = {}) => {
  const { data } = await client.get("/reportes/reservas/cancelaciones/detalle", {
    params: buildParams(params),
  });
  return data;
};


import client from "./client";
import { buildParams, NO_TIMEOUT_CONFIG } from "./reportesCommon";

export const getIngresosPorPeriodo = async (params = {}) => {
  const { data } = await client.get("/reportes/financieros/ingresos-por-periodo", {
    params: buildParams(params),
    ...NO_TIMEOUT_CONFIG,
  });
  return data;
};

export const getPromediosFinancieros = async (params = {}) => {
  const { data } = await client.get("/reportes/financieros/promedios", {
    params: buildParams(params),
    ...NO_TIMEOUT_CONFIG,
  });
  return data;
};

export const getIngresosPorTipoVehiculoFinanciero = async (params = {}) => {
  const { data } = await client.get("/reportes/financieros/ingresos-por-tipo-vehiculo", {
    params: buildParams(params),
    ...NO_TIMEOUT_CONFIG,
  });
  return data;
};

export const getIngresosPorMetodoPago = async (params = {}) => {
  const { data } = await client.get("/reportes/financieros/ingresos-por-metodo-pago", {
    params: buildParams(params),
    ...NO_TIMEOUT_CONFIG,
  });
  return data;
};

export const getRankingHorasPicoPorIngreso = async (params = {}) => {
  const { data } = await client.get("/reportes/financieros/ranking-horas-pico", {
    params: buildParams(params),
    ...NO_TIMEOUT_CONFIG,
  });
  return data;
};

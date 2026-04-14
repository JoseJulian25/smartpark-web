import client from "./client";
import { buildParams } from "./reportesCommon";

export const getUtilizacionPorEspacio = async (params = {}) => {
  const { data } = await client.get("/reportes/ocupacion/utilizacion-por-espacio", {
    params: buildParams(params),
  });
  return data;
};

export const getTendenciaUsoPorEspacio = async (params = {}) => {
  const { data } = await client.get("/reportes/ocupacion/tendencia-uso-espacio", {
    params: buildParams(params),
  });
  return data;
};

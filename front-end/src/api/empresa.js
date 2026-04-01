import client from "./client";

export const getEmpresa = async () => {
  const { data } = await client.get("/configuracion/empresa");
  return data;
};

export const updateEmpresa = async (payload) => {
  const { data } = await client.put("/configuracion/empresa", payload);
  return data;
};

export const getErrorMessage = (error, fallbackMessage = "Error en la operación") => {
  return error?.response?.data?.message || error?.message || fallbackMessage;
};
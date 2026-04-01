import client from "./client";

export const getTarifas = async () => {
  const { data } = await client.get("/tarifas");
  return data;
};

export const updateTarifas = async (payload) => {
  const { data } = await client.put("/tarifas", payload);
  return data;
};

export const getErrorMessage = (error, fallbackMessage = "Error en la operación") => {
  return error?.response?.data?.message || error?.message || fallbackMessage;
};
import client from "./client";

export const getEspacios = async () => {
  const { data } = await client.get("/espacios");
  return data;
};

export const updateEstadoEspacio = async (id, estado) => {
  const { data } = await client.patch(`/espacios/${id}`, { estado });
  return data;
};

export const addEspaciosLote = async (payload) => {
  const { data } = await client.post("/espacios/lote", payload);
  return data;
};

export const deleteEspacio = async (id) => {
  const { data } = await client.delete(`/espacios/${id}`);
  return data;
};
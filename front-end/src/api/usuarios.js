import client from "./client";

export const getUsuarios = async () => {
  const { data } = await client.get("/usuarios");
  return data;
};

export const addUsuario = async (payload) => {
  const { data } = await client.post("/usuarios", payload);
  return data;
};

export const updateUsuario = async (id, payload) => {
  const { data } = await client.put(`/usuarios/${id}`, payload);
  return data;
};

export const updateEstadoUsuario = async (id, activo) => {
  const { data } = await client.patch(`/usuarios/${id}/estado`, { activo });
  return data;
};

export const deleteUsuario = async (id) => {
  const { data } = await client.delete(`/usuarios/${id}`);
  return data;
};

export const getErrorMessage = (error, fallbackMessage = "Error en la operación") => {
  return error?.response?.data?.message || error?.message || fallbackMessage;
};

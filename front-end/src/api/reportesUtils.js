import { toNumber } from "./reportesCommon";

export const transformSerieTemporalToChart = (response = {}) => {
  const items = Array.isArray(response?.items) ? response.items : [];
  return items.map((item) => ({
    etiqueta: item?.periodo || "-",
    valor: toNumber(item?.valor),
  }));
};

export const transformComparativoToDualChart = (response = {}) => {
  const items = Array.isArray(response?.items) ? response.items : [];
  return items.map((item) => ({
    etiqueta: item?.etiqueta || "-",
    actual: toNumber(item?.valorActual),
    comparado: toNumber(item?.valorComparado),
    variacionAbsoluta: toNumber(item?.variacionAbsoluta),
    variacionPorcentual: toNumber(item?.variacionPorcentual),
  }));
};

export const transformTopNToChart = (response = {}) => {
  const items = Array.isArray(response?.items) ? response.items : [];
  return items.map((item) => ({
    posicion: item?.posicion,
    clave: item?.clave || "-",
    descripcion: item?.descripcion || item?.clave || "-",
    valor: toNumber(item?.valor),
    unidad: item?.unidad || response?.unidad || "",
  }));
};

export const transformIndicadoresFinancieros = (response = {}) => {
  const indicadores = Array.isArray(response?.indicadores) ? response.indicadores : [];
  return indicadores.map((item) => ({
    codigo: item?.codigo || "",
    nombre: item?.nombre || item?.codigo || "-",
    valor: toNumber(item?.valor),
    moneda: item?.moneda || response?.moneda || "DOP",
    variacionPorcentual: toNumber(item?.variacionPorcentual),
  }));
};

export const getReportesErrorMessage = async (
  error,
  fallbackMessage = "No se pudo completar la operación"
) => {
  const responseData = error?.response?.data;

  if (responseData instanceof Blob) {
    try {
      const text = await responseData.text();
      const parsed = JSON.parse(text);
      return parsed?.message || parsed?.error || fallbackMessage;
    } catch {
      return fallbackMessage;
    }
  }

  if (responseData?.violaciones && Array.isArray(responseData.violaciones)) {
    return responseData.violaciones
      .map((item) => item?.message || item?.mensaje)
      .filter(Boolean)
      .join(" | ") || fallbackMessage;
  }

  if (responseData?.errors && Array.isArray(responseData.errors)) {
    return responseData.errors
      .map((item) => item?.message || item?.defaultMessage)
      .filter(Boolean)
      .join(" | ") || fallbackMessage;
  }

  if (responseData?.details && typeof responseData.details === "object") {
    const messages = Object.values(responseData.details).filter(Boolean);
    if (messages.length) return messages.join(" | ");
  }

  return (
    responseData?.message ||
    responseData?.error ||
    error?.message ||
    fallbackMessage
  );
};

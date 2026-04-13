import client from "./client";

const buildParams = (params = {}) => {
  const clean = {};
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      clean[key] = value;
    }
  });
  return clean;
};

const parseFileNameFromDisposition = (contentDisposition, fallback) => {
  if (!contentDisposition) return fallback;

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) return decodeURIComponent(utf8Match[1]);

  const plainMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
  if (plainMatch?.[1]) return plainMatch[1];

  return fallback;
};

const downloadBinary = async (url, fallbackFileName, params = {}) => {
  const response = await client.get(url, {
    params: buildParams(params),
    responseType: "blob",
  });

  const fileName = parseFileNameFromDisposition(
    response.headers?.["content-disposition"],
    fallbackFileName
  );

  return {
    fileName,
    blob: response.data,
  };
};

export const getReportesBootstrap = async () => {
  const { data } = await client.get("/reportes/bootstrap");
  return data;
};

export const getEstadoReportesOperativos = async () => {
  const { data } = await client.get("/reportes/operativos");
  return data;
};

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

export const getFlujoNetoPorHora = async (params = {}) => {
  const { data } = await client.get("/reportes/operativos/flujo-neto-por-hora", {
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

export const getEstadoReportesReservas = async () => {
  const { data } = await client.get("/reportes/reservas");
  return data;
};

export const getReservasPorEstadoReporte = async (params = {}) => {
  const { data } = await client.get("/reportes/reservas/por-estado", {
    params: buildParams(params),
  });
  return data;
};

export const getReservasCreadasPorDia = async (params = {}) => {
  const { data } = await client.get("/reportes/reservas/creadas-por-dia", {
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

export const getCancelacionesConteoPorMotivo = async (params = {}) => {
  const { data } = await client.get("/reportes/reservas/cancelaciones/conteo-por-motivo", {
    params: buildParams(params),
  });
  return data;
};

export const getEstadoReportesOcupacion = async () => {
  const { data } = await client.get("/reportes/ocupacion");
  return data;
};

export const getOcupacionGlobal = async () => {
  const { data } = await client.get("/reportes/ocupacion/global");
  return data;
};

export const getOcupacionPorTipo = async () => {
  const { data } = await client.get("/reportes/ocupacion/por-tipo");
  return data;
};

export const getCapacidadActivaInactiva = async () => {
  const { data } = await client.get("/reportes/ocupacion/capacidad");
  return data;
};

export const getUtilizacionPorEspacio = async (params = {}) => {
  const { data } = await client.get("/reportes/ocupacion/utilizacion-por-espacio", {
    params: buildParams(params),
  });
  return data;
};

export const getEstadoReportesConsultas = async () => {
  const { data } = await client.get("/reportes/consultas");
  return data;
};

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

export const consultarPorReserva = async (codigoReserva) => {
  const { data } = await client.get(`/reportes/consultas/reserva/${codigoReserva}`);
  return data;
};

export const exportarTicketsCsv = async (params = {}) => {
  return downloadBinary("/reportes/export/csv/tickets", "tickets.csv", params);
};

export const exportarReservasCsv = async (params = {}) => {
  return downloadBinary("/reportes/export/csv/reservas", "reservas.csv", params);
};

export const exportarCancelacionesCsv = async (params = {}) => {
  return downloadBinary(
    "/reportes/export/csv/cancelaciones",
    "cancelaciones_reservas.csv",
    params
  );
};

export const exportarResumenOperativoPdf = async (fecha) => {
  return downloadBinary(
    "/reportes/export/pdf/resumen-operativo-diario",
    "resumen_operativo_diario.pdf",
    buildParams({ fecha })
  );
};

export const exportarCancelacionesPdf = async (params = {}) => {
  return downloadBinary(
    "/reportes/export/pdf/cancelaciones",
    "cancelaciones_reservas.pdf",
    params
  );
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

  return (
    responseData?.message ||
    responseData?.error ||
    error?.message ||
    fallbackMessage
  );
};

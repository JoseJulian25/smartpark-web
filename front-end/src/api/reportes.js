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

const toNumber = (value) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
};

const NO_TIMEOUT_CONFIG = { timeout: 0 };

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

export const getEstadoReportesFinancieros = async () => {
  const { data } = await client.get("/reportes/financieros", NO_TIMEOUT_CONFIG);
  return data;
};

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

export const getEstadoReportesComparativos = async () => {
  const { data } = await client.get("/reportes/comparativos");
  return data;
};

export const getComparativoEntradasSalidas = async (params = {}) => {
  const { data } = await client.get("/reportes/comparativos/entradas-salidas", {
    params: buildParams(params),
  });
  return data;
};

export const getComparativoReservasPorEstado = async (params = {}) => {
  const { data } = await client.get("/reportes/comparativos/reservas-por-estado", {
    params: buildParams(params),
  });
  return data;
};

export const getComparativoOcupacionFranjaHoraria = async (params = {}) => {
  const { data } = await client.get("/reportes/comparativos/ocupacion-franja-horaria", {
    params: buildParams(params),
  });
  return data;
};

export const getEstadoReportesEficiencia = async () => {
  const { data } = await client.get("/reportes/eficiencia");
  return data;
};

export const getTasaConversionReservaIngreso = async (params = {}) => {
  const { data } = await client.get("/reportes/eficiencia/tasa-conversion-reserva-ingreso", {
    params: buildParams(params),
  });
  return data;
};

export const getTasaNoShow = async (params = {}) => {
  const { data } = await client.get("/reportes/eficiencia/tasa-no-show", {
    params: buildParams(params),
  });
  return data;
};

export const getCancelacionesPorOperador = async (params = {}) => {
  const { data } = await client.get("/reportes/eficiencia/cancelaciones-por-operador", {
    params: buildParams(params),
  });
  return data;
};

export const getTiempoPromedioOcupacionPorTipo = async (params = {}) => {
  const { data } = await client.get("/reportes/eficiencia/tiempo-promedio-ocupacion-por-tipo", {
    params: buildParams(params),
  });
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

export const getHistorialConsolidadoCliente = async (params = {}) => {
  const { data } = await client.get("/reportes/consultas/historial-cliente", {
    params: buildParams(params),
  });
  return data;
};

export const getTrazabilidadTicket = async (codigoTicket) => {
  const { data } = await client.get(`/reportes/consultas/trazabilidad-ticket/${codigoTicket}`);
  return data;
};

export const getConsultasPorRangoMontos = async (params = {}) => {
  const { data } = await client.get("/reportes/consultas/rango-montos", {
    params: buildParams(params),
  });
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

export const exportarResumenOperativoPdf = async (params = {}) => {
  return downloadBinary(
    "/reportes/export/pdf/resumen-operativo-diario",
    "resumen_operativo.pdf",
    buildParams(params)
  );
};

export const exportarCancelacionesPdf = async (params = {}) => {
  return downloadBinary(
    "/reportes/export/pdf/cancelaciones",
    "cancelaciones_reservas.pdf",
    params
  );
};

export const exportarOperativosAvanzadoCsv = async (params = {}) => {
  return downloadBinary(
    "/reportes/export/csv/operativos-avanzado",
    "reportes_operativos_avanzado.csv",
    params
  );
};

export const exportarFinancierosAvanzadoCsv = async (params = {}) => {
  const response = await client.get("/reportes/export/csv/financieros-avanzado", {
    params: buildParams(params),
    responseType: "blob",
    ...NO_TIMEOUT_CONFIG,
  });

  const fileName = parseFileNameFromDisposition(
    response.headers?.["content-disposition"],
    "reportes_financieros_avanzado.csv"
  );

  return {
    fileName,
    blob: response.data,
  };
};

export const exportarResumenEjecutivoPdf = async (params = {}) => {
  const response = await client.get("/reportes/export/pdf/resumen-ejecutivo", {
    params: buildParams(params),
    responseType: "blob",
    ...NO_TIMEOUT_CONFIG,
  });

  const fileName = parseFileNameFromDisposition(
    response.headers?.["content-disposition"],
    "reportes_resumen_ejecutivo.pdf"
  );

  return {
    fileName,
    blob: response.data,
  };
};

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

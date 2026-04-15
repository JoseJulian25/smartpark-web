import { jsPDF } from "jspdf";

const formatDateTime = (value) => {
  if (!value) return "-";
  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) return String(value);
  return parsedDate.toLocaleString("es-DO");
};

const formatCurrency = (value) => {
  const numericValue = Number(value || 0);
  return new Intl.NumberFormat("es-DO", {
    style: "currency",
    currency: "DOP",
    minimumFractionDigits: 2
  }).format(numericValue);
};

const formatDuration = (minutesValue) => {
  const minutes = Number(minutesValue || 0);
  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;
  if (hours > 0) return `${hours}h ${remainingMinutes}m`;
  return `${remainingMinutes}m`;
};

export const abrirFacturaCobroPdf = ({ cobroData, empresaTicket }) => {
  if (!cobroData) {
    return { opened: false, reason: "missing-payment" };
  }

  const nombreEmpresa = empresaTicket?.nombre?.trim() || "Parking";
  const telefonoEmpresa = empresaTicket?.telefono?.trim() || "N/A";
  const ticket = cobroData.codigoTicket || "-";
  const espacio = cobroData.codigoEspacio || "-";
  const placa = cobroData.placa || "-";
  const entrada = formatDateTime(cobroData.horaEntrada);
  const salida = formatDateTime(cobroData.horaSalida);
  const tiempo = formatDuration(cobroData.minutosEstadia);
  const metodoPago = String(cobroData.metodoPago || "-")
    .toUpperCase()
    .replace("EFECTIVO", "Efectivo")
    .replace("TARJETA", "Tarjeta");
  const montoTotal = formatCurrency(cobroData.montoTotal);
  const montoRecibido = formatCurrency(cobroData.montoRecibido);
  const cambio = formatCurrency(cobroData.cambio);

  const doc = new jsPDF({
    orientation: "portrait",
    unit: "mm",
    format: [80, 180]
  });

  doc.setDrawColor(40);
  doc.setLineWidth(0.35);
  doc.rect(4, 4, 72, 172);

  doc.setFillColor(20, 83, 45);
  doc.rect(4, 4, 72, 20, "F");
  doc.setTextColor(255);
  doc.setFont("helvetica", "bold");
  doc.setFontSize(10);
  doc.text("FACTURA DE COBRO", 40, 12, { align: "center" });
  doc.setFont("helvetica", "normal");
  doc.setFontSize(7);
  doc.text("Salida de vehiculo", 40, 18, { align: "center" });

  doc.setTextColor(35);
  doc.setFont("helvetica", "bold");
  doc.setFontSize(8);
  doc.text(nombreEmpresa, 8, 30);
  doc.setFont("helvetica", "normal");
  doc.setFontSize(7);
  doc.text(`Tel: ${telefonoEmpresa}`, 8, 34);
  doc.text(`Fecha: ${formatDateTime(cobroData.horaSalida || new Date().toISOString())}`, 8, 38);

  doc.setDrawColor(180);
  doc.line(8, 42, 72, 42);

  doc.setFont("helvetica", "normal");
  doc.setFontSize(7.5);
  doc.text("Ticket", 8, 48);
  doc.setFont("helvetica", "bold");
  doc.text(String(ticket), 30, 48);

  doc.setFont("helvetica", "normal");
  doc.text("Espacio", 8, 53);
  doc.setFont("helvetica", "bold");
  doc.text(String(espacio), 30, 53);

  doc.setFont("helvetica", "normal");
  doc.text("Placa", 8, 58);
  doc.setFont("helvetica", "bold");
  doc.text(String(placa), 30, 58);

  doc.setFont("helvetica", "normal");
  doc.text("Entrada", 8, 63);
  doc.setFont("helvetica", "bold");
  doc.text(String(entrada), 30, 63);

  doc.setFont("helvetica", "normal");
  doc.text("Salida", 8, 68);
  doc.setFont("helvetica", "bold");
  doc.text(String(salida), 30, 68);

  doc.setFont("helvetica", "normal");
  doc.text("Estadia", 8, 73);
  doc.setFont("helvetica", "bold");
  doc.text(String(tiempo), 30, 73);

  doc.setFillColor(245, 247, 250);
  doc.rect(8, 78, 64, 36, "F");
  doc.setDrawColor(210);
  doc.rect(8, 78, 64, 36);

  doc.setFont("helvetica", "bold");
  doc.setFontSize(8);
  doc.text("Detalle", 10, 84);
  doc.setFont("helvetica", "normal");
  doc.setFontSize(7.5);
  doc.text("Concepto", 10, 90);
  doc.text("Monto", 64, 90, { align: "right" });
  doc.setDrawColor(190);
  doc.line(10, 92, 70, 92);
  doc.text("Servicio de parqueo", 10, 98);
  doc.text(String(montoTotal), 64, 98, { align: "right" });
  doc.line(10, 101, 70, 101);
  doc.setFont("helvetica", "bold");
  doc.text("Total", 10, 108);
  doc.text(String(montoTotal), 64, 108, { align: "right" });

  doc.setDrawColor(180);
  doc.line(8, 119, 72, 119);

  doc.setFont("helvetica", "normal");
  doc.setFontSize(7.5);
  doc.text("Metodo de pago", 8, 126);
  doc.setFont("helvetica", "bold");
  doc.text(metodoPago, 50, 126, { align: "right" });

  doc.setFont("helvetica", "normal");
  doc.text("Monto recibido", 8, 132);
  doc.setFont("helvetica", "bold");
  doc.text(String(montoRecibido), 50, 132, { align: "right" });

  doc.setFont("helvetica", "normal");
  doc.text("Cambio", 8, 138);
  doc.setFont("helvetica", "bold");
  doc.text(String(cambio), 50, 138, { align: "right" });

  doc.setLineDashPattern([1, 1], 0);
  doc.line(8, 146, 72, 146);
  doc.setLineDashPattern([], 0);

  doc.setTextColor(100);
  doc.setFont("helvetica", "normal");
  doc.setFontSize(6.8);
  doc.text("Documento informativo de cobro", 40, 154, { align: "center" });
  doc.text("Gracias por preferirnos", 40, 159, { align: "center" });

  const blob = doc.output("blob");
  const pdfUrl = URL.createObjectURL(blob);
  const visor = window.open(pdfUrl, "_blank", "noopener,noreferrer");

  setTimeout(() => URL.revokeObjectURL(pdfUrl), 60_000);

  if (!visor) {
    return { opened: false, reason: "popup-blocked" };
  }

  return { opened: true };
};

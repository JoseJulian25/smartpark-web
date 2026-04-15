import { jsPDF } from "jspdf";

const formatDateTimeForTicket = (value) => {
  if (!value) return "-";
  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) return String(value);
  return parsedDate.toLocaleString("es-DO");
};

export const abrirTicketEntradaPdf = ({ ticketData, empresaTicket }) => {
  if (!ticketData) {
    return { opened: false, reason: "missing-ticket" };
  }

  const numeroTicket = ticketData.codigoTicket || "-";
  const espacio = ticketData.codigoEspacio || "-";
  const placaTicket = ticketData.placa || "-";
  const fecha = formatDateTimeForTicket(ticketData.horaEntrada || new Date().toISOString());
  const nombreEmpresa = empresaTicket?.nombre?.trim() || "Parking";
  const telefonoEmpresa = empresaTicket?.telefono?.trim() || "N/A";

  const doc = new jsPDF({
    orientation: "portrait",
    unit: "mm",
    format: [80, 130]
  });

  doc.setDrawColor(30);
  doc.setLineWidth(0.4);
  doc.rect(4, 4, 72, 122);

  doc.setFillColor(33, 37, 41);
  doc.rect(4, 4, 72, 18, "F");

  doc.setTextColor(255);
  doc.setFont("helvetica", "bold");
  doc.setFontSize(11);
  doc.text("TICKET DE ENTRADA", 40, 12, { align: "center" });
  doc.setFont("helvetica", "normal");
  doc.setFontSize(7);
  doc.text("COMPROBANTE DE ACCESO", 40, 17, { align: "center" });

  doc.setTextColor(35);
  doc.setDrawColor(180);
  doc.setLineWidth(0.2);
  doc.line(8, 26, 72, 26);

  doc.setFont("helvetica", "normal");
  doc.setFontSize(8);
  doc.text("Numero de ticket", 8, 33);
  doc.setFont("helvetica", "bold");
  doc.setFontSize(12);
  doc.text(String(numeroTicket), 8, 39);

  doc.setFont("helvetica", "normal");
  doc.setFontSize(8);
  doc.text("Espacio", 8, 48);
  doc.setFont("helvetica", "bold");
  doc.setFontSize(10);
  doc.text(String(espacio), 8, 53);

  doc.setFont("helvetica", "normal");
  doc.setFontSize(8);
  doc.text("Placa", 8, 62);
  doc.setFont("helvetica", "bold");
  doc.setFontSize(11);
  doc.text(String(placaTicket), 8, 68);

  doc.setFont("helvetica", "normal");
  doc.setFontSize(8);
  doc.text("Fecha y hora", 8, 77);
  const fechaTexto = doc.splitTextToSize(String(fecha), 62);
  doc.setFont("helvetica", "bold");
  doc.setFontSize(9);
  doc.text(fechaTexto, 8, 82);

  doc.setDrawColor(180);
  doc.setLineDashPattern([1, 1], 0);
  doc.line(8, 96, 72, 96);
  doc.setLineDashPattern([], 0);

  doc.setFont("helvetica", "normal");
  doc.setFontSize(6.8);
  doc.setTextColor(95);
  const empresaTexto = doc.splitTextToSize(String(nombreEmpresa), 62);
  doc.text(empresaTexto, 40, 103, { align: "center" });
  doc.text(String(telefonoEmpresa), 40, 110, { align: "center" });
  doc.text("Gracias por su visita", 40, 117, { align: "center" });

  const blob = doc.output("blob");
  const pdfUrl = URL.createObjectURL(blob);
  const visor = window.open(pdfUrl, "_blank", "noopener,noreferrer");

  setTimeout(() => URL.revokeObjectURL(pdfUrl), 60_000);

  if (!visor) {
    return { opened: false, reason: "popup-blocked" };
  }

  return { opened: true };
};

package com.alemediaz.clearbooksbackend.services;

import com.alemediaz.clearbooksbackend.models.Invoice;
import com.alemediaz.clearbooksbackend.models.InvoiceItem;
import com.alemediaz.clearbooksbackend.models.Estimate;
import com.alemediaz.clearbooksbackend.models.User;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class PDFService {
    private static final NumberFormat currencyFormatter =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-ES"));

    static {
        currencyFormatter.setCurrency(java.util.Currency.getInstance("EUR"));
    }

    public byte[] generateInvoicePdf(Invoice invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            document.add(new Paragraph("FACTURA")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Invoice number and date
            document.add(new Paragraph("Factura número: " + invoice.getInvoiceNumber())
                    .setFontSize(12));
            document.add(new Paragraph("Fecha: " + invoice.getInvoiceDate())
                    .setFontSize(12));

            if (invoice.getDueDate() != null) {
                document.add(new Paragraph("Fecha de vencimiento: " + invoice.getDueDate())
                        .setFontSize(12));
            }

            document.add(new Paragraph("\n"));

            // Two columns: From (User) and Bill To (Customer)
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            headerTable.setWidth(UnitValue.createPercentValue(100));

            // Left column - From (User/Company)
            Cell fromCell = new Cell();
            fromCell.add(new Paragraph("Datos de la empresa:").setFontSize(14).setBold());

            // Add user information from the invoice
            User user = invoice.getUser();
            if (user != null) {
                if (user.getCompanyName() != null) {
                    fromCell.add(new Paragraph(user.getCompanyName()).setFontSize(12));
                }
                if (user.getName() != null && user.getSurname() != null) {
                    fromCell.add(new Paragraph(user.getName() + " " + user.getSurname()).setFontSize(10));
                }
                if (user.getVatNumber() != null) {
                    fromCell.add(new Paragraph("NIF / CIF: " + user.getVatNumber()).setFontSize(10));
                }
                if (user.getAddress() != null) {
                    fromCell.add(new Paragraph(user.getAddress()).setFontSize(10));
                }
                if (user.getPhoneNumber() != null) {
                    fromCell.add(new Paragraph("Teléfono: " + user.getPhoneNumber()).setFontSize(10));
                }
                if (user.getEmail() != null) {
                    fromCell.add(new Paragraph("Email: " + user.getEmail()).setFontSize(10));
                }
            } else {
                fromCell.add(new Paragraph("Datos de la empresa").setFontSize(12));
            }

            // Right column - Bill To (Customer)
            Cell billToCell = new Cell();
            billToCell.add(new Paragraph("Datos del cliente:").setFontSize(14).setBold());
            billToCell.add(new Paragraph(invoice.getCustomer().getName()).setFontSize(12));

            if (invoice.getCustomer().getVatNumber() != null) {
                billToCell.add(new Paragraph("NIF / CIF: " + invoice.getCustomer().getVatNumber()).setFontSize(10));
            }

            if (invoice.getCustomer().getAddress() != null) {
                billToCell.add(new Paragraph(invoice.getCustomer().getAddress()).setFontSize(10));
            }

            if (invoice.getCustomer().getPhone() != null) {
                billToCell.add(new Paragraph("Teléfono: " + invoice.getCustomer().getPhone()).setFontSize(10));
            }

            headerTable.addCell(fromCell);
            headerTable.addCell(billToCell);
            document.add(headerTable);

            document.add(new Paragraph("\n"));

            // Items table
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Headers
            table.addHeaderCell(new Cell().add(new Paragraph("Descripción").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Precio").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()));

            // Items
            for (InvoiceItem item : invoice.getItems()) {
                table.addCell(new Cell().add(new Paragraph(item.getDescription())));
                table.addCell(new Cell().add(new Paragraph(item.getQuantity().toString())));
                table.addCell(new Cell().add(new Paragraph(currencyFormatter.format(item.getUnitPrice()))));
                table.addCell(new Cell().add(new Paragraph(currencyFormatter.format(item.getAmount()))));
            }

            document.add(table);
            document.add(new Paragraph("\n"));

            // Totals
            document.add(new Paragraph("Subtotal: " + currencyFormatter.format(invoice.getSubtotal()))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12));
            document.add(new Paragraph("IVA (" + invoice.getTaxRate() + "%): " +
                    currencyFormatter.format(invoice.getTaxAmount()))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12));
            document.add(new Paragraph("TOTAL: " + currencyFormatter.format(invoice.getTotal()))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(14)
                    .setBold());

            // Payment status
            if (invoice.getIsPaid() != null && invoice.getIsPaid()) {
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("PAGADA")
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontSize(14)
                        .setBold());
                if (invoice.getPaidDate() != null) {
                    document.add(new Paragraph("Pagada el: " + invoice.getPaidDate())
                            .setTextAlignment(TextAlignment.RIGHT)
                            .setFontSize(10));
                }
            }

            // Notes
            if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("Notas:")
                        .setFontSize(12)
                        .setBold());
                document.add(new Paragraph(invoice.getNotes())
                        .setFontSize(10));
            }

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return baos.toByteArray();
    }

    public byte[] generateEstimatePdf(Estimate estimate) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            document.add(new Paragraph("PRESUPUESTO")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Estimate number and date
            document.add(new Paragraph("Presupuesto número: " + estimate.getEstimateNumber())
                    .setFontSize(12));
            document.add(new Paragraph("Fecha: " + estimate.getEstimateDate())
                    .setFontSize(12));

            if (estimate.getValidUntil() != null) {
                document.add(new Paragraph("Válido hasta: " + estimate.getValidUntil())
                        .setFontSize(12));
            }

            document.add(new Paragraph("\n"));

            // Two columns: From (User) and For (Customer)
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            headerTable.setWidth(UnitValue.createPercentValue(100));

            // Left column - From (User/Company)
            Cell fromCell = new Cell();
            fromCell.add(new Paragraph("Datos de la empresa:").setFontSize(14).setBold());

            // Add user information from the estimate
            User user = estimate.getUser();
            if (user != null) {
                if (user.getCompanyName() != null) {
                    fromCell.add(new Paragraph(user.getCompanyName()).setFontSize(12));
                }
                if (user.getName() != null && user.getSurname() != null) {
                    fromCell.add(new Paragraph(user.getName() + " " + user.getSurname()).setFontSize(10));
                }
                if (user.getVatNumber() != null) {
                    fromCell.add(new Paragraph("NIF / CIF: " + user.getVatNumber()).setFontSize(10));
                }
                if (user.getAddress() != null) {
                    fromCell.add(new Paragraph(user.getAddress()).setFontSize(10));
                }
                if (user.getPhoneNumber() != null) {
                    fromCell.add(new Paragraph("Teléfono: " + user.getPhoneNumber()).setFontSize(10));
                }
                if (user.getEmail() != null) {
                    fromCell.add(new Paragraph("Email: " + user.getEmail()).setFontSize(10));
                }
            } else {
                fromCell.add(new Paragraph("Datos de la empresa").setFontSize(12));
            }

            // Right column - For (Customer)
            Cell forCell = new Cell();
            forCell.add(new Paragraph("Datos del cliente:").setFontSize(14).setBold());
            forCell.add(new Paragraph(estimate.getCustomer().getName()).setFontSize(12));

            if (estimate.getCustomer().getVatNumber() != null) {
                forCell.add(new Paragraph("NIF / CIF: " + estimate.getCustomer().getVatNumber()).setFontSize(10));
            }

            if (estimate.getCustomer().getAddress() != null) {
                forCell.add(new Paragraph(estimate.getCustomer().getAddress()).setFontSize(10));
            }

            if (estimate.getCustomer().getPhone() != null) {
                forCell.add(new Paragraph("Teléfono: " + estimate.getCustomer().getPhone()).setFontSize(10));
            }

            headerTable.addCell(fromCell);
            headerTable.addCell(forCell);
            document.add(headerTable);

            document.add(new Paragraph("\n"));

            // Items table
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 2, 2}));
            table.setWidth(UnitValue.createPercentValue(100));

            // Headers
            table.addHeaderCell(new Cell().add(new Paragraph("Descripción").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Cantidad").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Precio").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()));

            // Items
            for (InvoiceItem item : estimate.getItems()) {
                table.addCell(new Cell().add(new Paragraph(item.getDescription())));
                table.addCell(new Cell().add(new Paragraph(item.getQuantity().toString())));
                table.addCell(new Cell().add(new Paragraph(currencyFormatter.format(item.getUnitPrice()))));
                table.addCell(new Cell().add(new Paragraph(currencyFormatter.format(item.getAmount()))));
            }

            document.add(table);
            document.add(new Paragraph("\n"));

            // Totals
            document.add(new Paragraph("Subtotal: " + currencyFormatter.format(estimate.getSubtotal()))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12));
            document.add(new Paragraph("IVA (" + estimate.getTaxRate() + "%): " +
                    currencyFormatter.format(estimate.getTaxAmount()))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(12));
            document.add(new Paragraph("TOTAL: " + currencyFormatter.format(estimate.getTotal()))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(14)
                    .setBold());

            // Notes
            if (estimate.getNotes() != null && !estimate.getNotes().isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("Notas:")
                        .setFontSize(12)
                        .setBold());
                document.add(new Paragraph(estimate.getNotes())
                        .setFontSize(10));
            }

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return baos.toByteArray();
    }
}
// app/src/main/java/com/netscope/utils/PDFExporter.kt
package com.netscope.utils

import android.content.Context
import android.widget.Toast
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.netscope.model.DeviceInfo
import java.io.File
import java.io.FileOutputStream

object PDFExporter {
    fun exportToPDF(context: Context, deviceList: List<DeviceInfo>) {
        try {
            val doc = Document()
            val file = File(context.getExternalFilesDir(null), "NetScope_Scan.pdf")
            PdfWriter.getInstance(doc, FileOutputStream(file))
            doc.open()

            val title = Paragraph("NetScope WiFi Scan Report\n\n",
                Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor(0, 191, 166)))
            title.alignment = Element.ALIGN_CENTER
            doc.add(title)

            val table = PdfPTable(3)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(3f, 3f, 4f))

            val headerFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
            listOf("IP Address", "MAC Address", "Vendor").forEach {
                val cell = PdfPCell(Phrase(it, headerFont))
                cell.backgroundColor = BaseColor.LIGHT_GRAY
                table.addCell(cell)
            }

            deviceList.forEach {
                table.addCell(it.ip)
                table.addCell(it.mac)
                table.addCell(it.vendor)
            }

            doc.add(table)
            doc.close()

            Toast.makeText(context, "PDF saved to:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "PDF Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
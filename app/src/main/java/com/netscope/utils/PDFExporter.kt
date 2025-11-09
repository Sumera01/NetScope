package com.netscope.utils

import android.content.Context
import android.os.Environment
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
        val doc = Document()
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/NetScope_Scan.pdf"
        val file = File(path)
        PdfWriter.getInstance(doc, FileOutputStream(file))
        doc.open()

        val title = Paragraph("NetScope WiFi Scan Report\n\n", Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor(0, 191, 166)))
        title.alignment = Element.ALIGN_CENTER
        doc.add(title)

        val table = PdfPTable(3)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(3f, 3f, 4f))

        val headerFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
        val headers = listOf("IP Address", "MAC Address", "Vendor")
        headers.forEach {
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

        Toast.makeText(context, "PDF Exported: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}

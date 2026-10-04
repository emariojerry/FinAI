package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

object PdfStatementParser {

    private const val TAG = "PdfStatementParser"

    /**
     * Extracts text from a PDF InputStream.
     * Handles both uncompressed text objects (/BT ... ET) and FlateDecode compressed streams.
     */
    fun extractTextFromPdf(inputStream: InputStream): String {
        val byteOut = ByteArrayOutputStream()
        inputStream.use { input ->
            input.copyTo(byteOut)
        }
        val bytes = byteOut.toByteArray()
        return extractTextFromBytes(bytes)
    }

    /**
     * Helper to read from an Android content Uri
     */
    fun extractTextFromUri(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                extractTextFromPdf(stream)
            } ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text from PDF uri: ${e.message}", e)
            ""
        }
    }

    private fun extractTextFromBytes(bytes: ByteArray): String {
        val extractedLines = mutableListOf<String>()
        val totalLength = bytes.size

        // Step 1: Scan for /Filter /FlateDecode streams
        var cursor = 0
        while (cursor < totalLength) {
            val streamStart = findSequence(bytes, "stream".toByteArray(Charsets.US_ASCII), cursor)
            if (streamStart == -1) break

            val streamDataStart = skipNewline(bytes, streamStart + 6)
            val streamEnd = findSequence(bytes, "endstream".toByteArray(Charsets.US_ASCII), streamDataStart)
            if (streamEnd == -1) break

            val streamLength = streamEnd - streamDataStart
            if (streamLength > 0) {
                // Check if preceding header mentions /FlateDecode
                val headerStart = (streamStart - 250).coerceAtLeast(0)
                val headerSnippet = String(bytes, headerStart, streamStart - headerStart, Charsets.US_ASCII)
                val isFlate = headerSnippet.contains("/FlateDecode") || headerSnippet.contains("/Fl")

                val streamBytes = bytes.copyOfRange(streamDataStart, streamEnd)
                val decompressed: String? = if (isFlate) {
                    decompressFlate(streamBytes)
                } else {
                    String(streamBytes, Charsets.UTF_8)
                }

                if (!decompressed.isNullOrEmpty()) {
                    val parsedText = extractPdfTextTokens(decompressed)
                    if (parsedText.isNotBlank()) {
                        extractedLines.add(parsedText)
                    }
                }
            }

            cursor = streamEnd + 9
        }

        // Step 2: If stream parsing yielded very little, fallback to scanning plain text blocks
        if (extractedLines.isEmpty()) {
            val rawString = String(bytes, Charsets.ISO_8859_1)
            val fallbackText = extractPdfTextTokens(rawString)
            if (fallbackText.isNotBlank()) {
                extractedLines.add(fallbackText)
            }
        }

        return extractedLines.joinToString("\n")
    }

    private fun decompressFlate(streamBytes: ByteArray): String? {
        // Try standard Inflater with nowrap=false and nowrap=true
        for (nowrap in listOf(false, true)) {
            try {
                val inflater = Inflater(nowrap)
                val input = ByteArrayInputStream(streamBytes)
                val inflaterStream = InflaterInputStream(input, inflater)
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(2048)
                var read: Int
                while (inflaterStream.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                }
                inflater.end()
                val decompressedBytes = out.toByteArray()
                if (decompressedBytes.isNotEmpty()) {
                    return String(decompressedBytes, Charsets.UTF_8)
                }
            } catch (e: Exception) {
                // Try next configuration
            }
        }
        return null
    }

    /**
     * Extracts text contained inside PDF text operators like (string) Tj or [(str1) 20 (str2)] TJ
     */
    private fun extractPdfTextTokens(content: String): String {
        val result = StringBuilder()
        val lines = content.lines()

        val tjSingleRegex = Regex("""\((.*?)\)\s*(?:Tj|'|")""")
        val tjArrayRegex = Regex("""\[(.*?)\]\s*TJ""")
        val insideParentheses = Regex("""\((.*?)\)""")

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // 1. Check array text TJ
            val arrayMatches = tjArrayRegex.findAll(trimmed)
            for (match in arrayMatches) {
                val inner = match.groupValues[1]
                val stringParts = insideParentheses.findAll(inner).map { it.groupValues[1] }.toList()
                if (stringParts.isNotEmpty()) {
                    val lineText = stringParts.joinToString("").decodePdfEscapes()
                    if (lineText.isNotBlank()) {
                        result.appendLine(lineText)
                    }
                }
            }

            // 2. Check single string Tj
            val singleMatches = tjSingleRegex.findAll(trimmed)
            for (match in singleMatches) {
                val raw = match.groupValues[1].decodePdfEscapes()
                if (raw.isNotBlank()) {
                    result.appendLine(raw)
                }
            }
        }

        return result.toString().trim()
    }

    private fun String.decodePdfEscapes(): String {
        return this.replace("\\(", "(")
            .replace("\\)", ")")
            .replace("\\n", " ")
            .replace("\\r", "")
            .replace("\\t", " ")
            .replace("\\\\", "\\")
    }

    private fun findSequence(source: ByteArray, target: ByteArray, startIndex: Int): Int {
        if (target.isEmpty() || startIndex < 0 || startIndex >= source.size) return -1
        outer@ for (i in startIndex..(source.size - target.size)) {
            for (j in target.indices) {
                if (source[i + j] != target[j]) continue@outer
            }
            return i
        }
        return -1
    }

    private fun skipNewline(bytes: ByteArray, startIndex: Int): Int {
        var idx = startIndex
        while (idx < bytes.size && (bytes[idx] == '\r'.code.toByte() || bytes[idx] == '\n'.code.toByte() || bytes[idx] == ' '.code.toByte())) {
            idx++
        }
        return idx
    }
}

package br.com.pedroamarques.estoquista.helper

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import br.com.pedroamarques.estoquista.BuildConfig
import timber.log.Timber
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


object FileUtils {
    fun getPath(context: Context, uri: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) { // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri: Uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null

                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    return getDataColumn(context, contentUri!!, selection, selectionArgs)
                }
            }
        } else if ("content".equals(uri.getScheme(), ignoreCase = true)) { // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.getLastPathSegment() else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.getScheme(), ignoreCase = true)) {
            return uri.getPath()
        }
        return null
    }

    fun getExtensao(uri: String): String? {
        if (!uri.contains('.', true)) {
            return uri
        }

        val parts = uri.split(".")
        return parts.lastOrNull()
    }

    fun getNomeURL(url: String): String? {
        if (!url.contains('/', true)) {
            return url
        }

        val parts = url.split("/")
        return parts.lastOrNull()
    }

    fun getNomeUri(uri: String): String? {
        if (!uri.contains('\\', true)) {
            return uri
        }

        val parts = uri.split("\\")
        return parts.lastOrNull()
    }

    fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.getAuthority()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.getAuthority()
    }

    fun getDiretorio(nome: String): File {
        if (Environment.getExternalStorageState() == null) {
            val f = File("${Environment.getDataDirectory().absolutePath}/${Environment.DIRECTORY_DOCUMENTS}/${BuildConfig.APP_NAME}/$nome")

            if (!f.exists()) {
                f.mkdirs()
            }

            return f

        } else {
            val f = File("${Environment.getExternalStorageDirectory().absolutePath}/${Environment.DIRECTORY_DOCUMENTS}/${BuildConfig.APP_NAME}/$nome")

            if (!f.exists()) {
                f.mkdirs()
            }

            return f
        }
    }

    fun getArquivosDiretorio(nome: String, extensoes: Array<String>): ArrayList<File> {
        val fileList: ArrayList<File> = ArrayList()
        var fileListTmp: Array<File>? = null

        if (Environment.getExternalStorageState() == null) {
            val f = File(Environment.getDataDirectory().absolutePath + "/$nome/")

            if (!f.exists()) {
                f.mkdirs()
            }

            fileListTmp = f.listFiles()


        } else {
            val f = File(Environment.getExternalStorageDirectory().absolutePath + "/$nome/")

            if (!f.exists()) {
                f.mkdirs()
            }

            fileListTmp = f.listFiles()
        }

        fileListTmp?.let {
            for(file in fileListTmp) {
                var valido = false

                for (extensao in extensoes) {
                    if (file.name.endsWith(extensao, true)) {
                        valido = true
                        break
                    }
                }

                if(valido) {
                    fileList.add(file)
                }
            }
        }

        return fileList
    }

    public fun downloadGet(url: String?, path: String?): File? {
        var file: File? = null
        var conn: HttpURLConnection? = null

        try {
            val urlDownload = URL(url)
            conn = urlDownload.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            // timeout 60 segundos
            conn.connectTimeout = 60000
            conn.doInput = true
            conn.useCaches = true

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val input: InputStream = BufferedInputStream(conn.inputStream)
                file = File(path)

                if (file.exists() && file.canWrite()) {
                    file.delete()
                }
                val outputStream: OutputStream = FileOutputStream(file)
                val buffer = ByteArray(2048)

                var lido: Int
                while (input.read(buffer).also { lido = it } != -1) {
                    outputStream.write(buffer, 0, lido)
                }
                outputStream.flush()
                outputStream.close()
                input.close()

            } else {
                throw Exception("Falha no download do arquivo")
            }

            conn.disconnect()
            conn = null

        } catch (e: MalformedURLException) {
            e.printStackTrace()
            Timber.e("MalformedURLException: %s", e.message)

        } catch (e: ProtocolException) {
            e.printStackTrace()
            Timber.e("ProtocolException: %s", e.message)

        } catch (e: IOException) {
            e.printStackTrace()
            Timber.e("IOException: %s", e.message)

        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("Exception: %s", e.message)

        } finally {
            conn?.disconnect()
        }

        return file
    }

    public fun removeCharInvalidosNome(nomeArquivo: String?): String {
        if(TextUtils.isEmpty(nomeArquivo)) {
            return ""
        }
        return nomeArquivo!!.replace("[\\\\/:*?\"<>|]".toRegex(), "")
    }

    @JvmStatic
    fun base64toTempPDF(context: Context, base64: String): File? {
        val file = File.createTempFile("PDF_", ".pdf", context.cacheDir)
        return try {
            val decoder: ByteArray = Base64.decode(base64, Base64.DEFAULT)
            file.writeBytes(decoder)

            file
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }
}
package com.dp.padhobihar.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class LocalCollege(
    val id: String,
    val name: String,
    val state: String,
    val district: String,
    val type: String = "College",
    val university: String = ""
)

object CollegeDataSource {

    // Host this file in YOUR GitHub repo (public or private with raw URL)
    private const val REMOTE_URL = "https://raw.githubusercontent.com/aryadk/padhobihar-data/main/colleges_india.txt"
    private const val CACHE_FILE = "colleges_india.txt"

    private var colleges: List<LocalCollege>? = null

    /**
     * Call once at app start (from coroutine). Downloads if not cached.
     */
    suspend fun init(context: Context) {
        withContext(Dispatchers.IO) {
            val cacheFile = File(context.filesDir, CACHE_FILE)

            if (!cacheFile.exists()) {
                // Try downloading from remote
                try {
                    val url = URL(REMOTE_URL)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 10000
                    conn.readTimeout = 15000
                    val data = conn.inputStream.bufferedReader().readText()
                    cacheFile.writeText(data)
                    conn.disconnect()
                } catch (e: Exception) {
                    // Fallback: use bundled asset if download fails
                    try {
                        val data = context.assets.open("colleges_india.txt").bufferedReader().readText()
                        cacheFile.writeText(data)
                    } catch (_: Exception) {}
                }
            }

            // Load from cache
            if (cacheFile.exists()) {
                val lines = cacheFile.readLines()
                colleges = lines.mapIndexed { index, line ->
                    val parts = line.split("|")
                    LocalCollege(
                        id = "COL${index + 1}",
                        name = parts.getOrElse(0) { "" },
                        state = parts.getOrElse(1) { "" },
                        district = parts.getOrElse(2) { "" }
                    )
                }
            }
        }
    }

    fun isLoaded(): Boolean = colleges != null && colleges!!.isNotEmpty()

    fun search(context: Context?, query: String): List<LocalCollege> {
        if (query.length < 3) return emptyList()
        val all = colleges ?: return emptyList()
        val q = query.lowercase().trim()

        // Common abbreviations
        val expansions = mapOf(
            "iit" to "indian institute of technology",
            "nit" to "national institute of technology",
            "iiit" to "indian institute of information technology",
            "iim" to "indian institute of management",
            "aiims" to "all india institute of medical science",
            "nift" to "national institute of fashion technology",
            "bits" to "birla institute of technology",
            "bhu" to "banaras hindu university",
            "jnu" to "jawaharlal nehru university",
            "amu" to "aligarh muslim university",
            "nlu" to "national law university"
        )

        val searchTerms = mutableListOf(q)
        expansions[q]?.let { searchTerms.add(it) }
        val parts = q.split(" ", limit = 2)
        if (parts.size == 2) {
            expansions[parts[0]]?.let { searchTerms.add("${it} ${parts[1]}") }
            searchTerms.add(parts[1])
        }

        return all.filter { college ->
            val name = college.name.lowercase()
            val state = college.state.lowercase()
            val district = college.district.lowercase()
            searchTerms.any { term ->
                name.contains(term) || state.contains(term) || district.contains(term)
            }
        }.take(30)
    }
}

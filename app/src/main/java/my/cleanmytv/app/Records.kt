package my.cleanmytv.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** What one cleaning run disabled, written down so it can be undone. */
data class RestoreRecord(
    val id: String,
    val time: Long,
    val host: String,
    val port: Int,
    val brand: String,
    val model: String,
    val packages: List<String>
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("time", time)
        .put("host", host)
        .put("port", port)
        .put("brand", brand)
        .put("model", model)
        .put("packages", JSONArray(packages))

    companion object {
        fun fromJson(o: JSONObject): RestoreRecord {
            val arr = o.optJSONArray("packages") ?: JSONArray()
            val pk = (0 until arr.length()).map { arr.getString(it) }
            return RestoreRecord(
                id = o.getString("id"),
                time = o.optLong("time"),
                host = o.optString("host"),
                port = o.optInt("port", 5555),
                brand = o.optString("brand"),
                model = o.optString("model"),
                packages = pk
            )
        }
    }
}

class RecordStore(ctx: Context) {
    private val dir = File(ctx.filesDir, "records").apply { mkdirs() }

    private fun file(id: String) = File(dir, "$id.json")

    fun save(r: RestoreRecord) {
        val tmp = File(dir, "${r.id}.tmp")
        tmp.writeText(r.toJson().toString())
        tmp.renameTo(file(r.id))
    }

    fun delete(id: String) {
        file(id).delete()
    }

    fun list(): List<RestoreRecord> =
        (dir.listFiles { f -> f.extension == "json" } ?: emptyArray())
            .mapNotNull { f -> runCatching { RestoreRecord.fromJson(JSONObject(f.readText())) }.getOrNull() }
            .filter { it.packages.isNotEmpty() }
            .sortedByDescending { it.time }
}

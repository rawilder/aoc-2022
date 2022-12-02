package util

import java.net.URL

object Util {
    fun resourcesFile(name: String): URL? {
        return Util::class.java.getResource(name)
    }
}

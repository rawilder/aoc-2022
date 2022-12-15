package util

import java.net.URL

object Util {
    fun resourcesFile(name: String): URL? {
        return Util::class.java.getResource(name)
    }

    infix fun <T> T.shouldBe(expected: T) {
        require(this == expected) {
            "Expected $expected, but was $this"
        }
    }
}

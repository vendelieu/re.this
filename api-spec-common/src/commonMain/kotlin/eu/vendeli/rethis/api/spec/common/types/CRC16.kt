package eu.vendeli.rethis.api.spec.common.types

/**
 * Implementation of CRC16 (XModem) algorithm.
 *
 * References:
 * - [Wikipedia](https://en.wikipedia.org/wiki/Cyclic_redundancy_check)
 * - [CRC catalog](http://reveng.sourceforge.net/crc-catalogue/16.htm)
 */
object CRC16 {
    private val CRC16_TABLE = IntArray(256).apply {
        for (i in 0 until 256) {
            var crc = i shl 8
            repeat(8) { crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1 }
            this[i] = crc and 0xFFFF
        }
    }

    /**
     * Calculates CRC16 checksum for given bytes.
     *
     * @param bytes data to be processed
     * @return calculated CRC value
     */
    fun lookup(bytes: ByteArray): Int {
        var crc = 0
        for (b in bytes) {
            val idx = ((crc shr 8) xor (b.toInt() and 0xFF)) and 0xFF
            crc = (crc shl 8) xor CRC16_TABLE[idx]
        }
        return crc and 0xFFFF
    }
}

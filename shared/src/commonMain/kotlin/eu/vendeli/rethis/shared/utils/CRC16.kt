package eu.vendeli.rethis.shared.utils

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

    private const val SLOT_COUNT = 16_384
    private const val TAG_OPEN = '{'.code.toByte()
    private const val TAG_CLOSE = '}'.code.toByte()

    /**
     * Calculates CRC16 checksum for given bytes.
     *
     * @param bytes data to be processed
     * @return calculated CRC value
     */
    fun lookup(bytes: ByteArray): Int = checksum(bytes, 0, bytes.size)

    /**
     * Cluster hash slot (0..16383) of [key].
     *
     * Only the hash tag is hashed when the key contains one: the bytes between the first `{` and the next `}`,
     * provided they are not empty. Otherwise the whole key is hashed.
     */
    fun slot(key: ByteArray): Int {
        val open = key.indexOf(TAG_OPEN)
        if (open >= 0) {
            val close = key.indexOf(TAG_CLOSE, open + 1)
            if (close > open + 1) return checksum(key, open + 1, close) % SLOT_COUNT
        }
        return checksum(key, 0, key.size) % SLOT_COUNT
    }

    private fun ByteArray.indexOf(value: Byte, from: Int = 0): Int {
        for (i in from until size) if (this[i] == value) return i
        return -1
    }

    private fun checksum(bytes: ByteArray, from: Int, to: Int): Int {
        var crc = 0
        for (i in from until to) {
            val idx = ((crc shr 8) xor (bytes[i].toInt() and 0xFF)) and 0xFF
            crc = (crc shl 8) xor CRC16_TABLE[idx]
        }
        return crc and 0xFFFF
    }
}

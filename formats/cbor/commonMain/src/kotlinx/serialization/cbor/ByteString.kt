package kotlinx.serialization.cbor

import kotlinx.serialization.SerialInfo

/**
 * Specifies that a [ByteArray] shall be encoded/decoded as CBOR major type 2: a byte string.
 *
 * Example usage:
 *
 * ```
 * @Serializable
 * data class Data(
 *     @ByteString
 *     val a: ByteArray, // CBOR major type 2: a byte string.
 *
 *     val b: ByteArray  // CBOR major type 4: an array of data items.
 * )
 * ```
 *
 * See [RFC 7049 2.1. Major Types](https://tools.ietf.org/html/rfc7049#section-2.1).
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class ByteString
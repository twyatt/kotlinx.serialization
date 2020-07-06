Per [RFC 7049] [2.1 Major Types], CBOR supports two formats of encoding/decoding a sequence of bytes:

- Major type 2: a byte string
- Major type 4: an array of data items

By default, `ByteArray`s are encoded/decoded as major type 4.
When major type 2 is desired, then the `@ByteString` annotation can be used.

For example:

```kotlin
@Serializable
data class Data(
    @ByteString
    val a: ByteArray // CBOR Major type 2

    val b: ByteArray // CBOR Major type 4
)
```


[RFC 7049]: https://tools.ietf.org/html/rfc7049
[2.1 Major Types]: https://tools.ietf.org/html/rfc7049#section-2.1

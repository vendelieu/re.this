# Re.this Changelog

## 0.2.2

* Fixed casting issue caused by type erasure and wrong inlining.

## 0.2.1

* Added connection retrying.
* Added socket configuration.
* Upgraded JVM target version to 17

## 0.2.0

* Implemented Bitmap, Stream commands.

## 0.1.9

* Improved redis response parsing.

## 0.1.8

* Improved pub/sub flow, added common event handler.

## 0.1.7

* Supported android targets (androidNativeArm32, androidNativeArm64, androidNativeX64, androidNativeX86).

## 0.1.6

* Added tls connection support

## 0.1.5

* Added option to pass redis url address.

## 0.1.4

* Use stricter typing in arguments.
* Removed unnecessary option constructs and moved to `VaryingArgument`.
* Removed unnecessary different type write operations since
  the [protocol states](https://redis.io/docs/latest/develop/reference/protocol-spec/#resp-protocol-description)
  that it always array of bulk strings.
* Supported setting charset for incoming and outgoing messages.

## 0.1.3

* Publish missing meta package

## 0.1.2

* Fixing build

## 0.1.1

* Published to maven

## 0.1.0

* Initial version
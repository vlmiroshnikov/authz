package io.github.vlmiroshnikov.authz.jwt

import scala.util.control.NoStackTrace

opaque type Binary = Array[Byte]

object Binary:
  def apply(data: Array[Byte]): Binary           = data
  extension (x: Binary) def toArray: Array[Byte] = x

case object DecodingFailure extends Throwable with NoStackTrace
case object EncodingFailure extends Throwable with NoStackTrace

trait AuxEncoder[A]:
  def encode(a: A): Either[EncodingFailure.type, Binary]

trait AuxDecoder[A]:
  def decode(a: Binary): Either[DecodingFailure.type, A]

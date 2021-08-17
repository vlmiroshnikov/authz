package io.github.vlmiroshnikov.authz.jwt

import scala.util.control.NoStackTrace

opaque type Binary = Array[Byte]

object Binary:
  def apply(data: Array[Byte]): Binary           = data
  extension (x: Binary) def toArray: Array[Byte] = x

case class DecodingFailure(message: String) extends Throwable with NoStackTrace {
  override def getMessage: String = message
}

case class EncodingFailure(message: String) extends Throwable with NoStackTrace {
  override def getMessage: String = message
}

trait AuxEncoder[A]:
  def encode(a: A): Either[EncodingFailure, Binary]

trait AuxDecoder[A]:
  def decode(a: Binary): Either[DecodingFailure, A]

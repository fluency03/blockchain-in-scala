package com.github.fluency03.blockchain.misc

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.github.fluency03.blockchain.Bytes

/**
 * Ser(ialization) and De(serialization)
 */
object Serde {

  def serialize[T](value: T): Bytes = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close()
    stream.close()
    stream.toByteArray
  }

  def deserialize[T](bytes: Array[Byte]): T = {
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val value = ois.readObject
    ois.close()
    value.asInstanceOf[T]
  }

}

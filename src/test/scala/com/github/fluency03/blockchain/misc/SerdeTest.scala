package com.github.fluency03.blockchain.misc

import java.io.NotSerializableException

import org.scalatest.{FlatSpec, Matchers}

case class Obj1() extends Serializable
case class Obj2()

class Obj3() extends Serializable
class Obj4()

class SerdeTest extends FlatSpec with Matchers {

  "Serde" should "serialize an object and deserialize it." in {
    Serde.deserialize[String](Serde.serialize("abc")) shouldEqual "abc"
    Serde.deserialize[Int](Serde.serialize(123)) shouldEqual 123
    Serde.deserialize[Obj1](Serde.serialize(Obj1())) shouldEqual Obj1()
    Serde.deserialize[Obj2](Serde.serialize(Obj2())) shouldEqual Obj2()
    Serde.deserialize[Obj3](Serde.serialize(new Obj3)) shouldBe an[Obj3]
    a[NotSerializableException] should be thrownBy Serde.serialize(new Obj4())
  }

}

# Blockchain in Scala

[![Travis branch](https://img.shields.io/travis/fluency03/blockchain-in-scala/master.svg)](https://travis-ci.org/fluency03/blockchain-in-scala)
[![Coveralls github branch](https://img.shields.io/coveralls/github/fluency03/blockchain-in-scala/master.svg)](https://coveralls.io/github/fluency03/blockchain-in-scala)

A simplified Blockchain implementation in Scala based on the specifications of Bitcoin.

*This project is still under development. APIs are not fully completed according to the cores.*

Inspired by:
- [Daniel van Flymen](http://www.dvf.nyc/)'s blog [Learn Blockchains by Building One](https://hackernoon.com/learn-blockchains-by-building-one-117428612f46)
- [Naivecoin](https://github.com/lhartikk/naivecoin) of [@lhartikk](https://github.com/lhartikk)

This project is meant to learn and understand Bitcoin and Blockchain. I personally also in the process of learning. So, what's a better the approach of learning it by building one ! Because of this is a continuous learning process, there might be something I do not understand earlier. Now when I understand it, the code will also be changed according. I am happy to discuss with you, which will make this learning process quicker and efficient.

REST API service is built on the open source projects [akka](https://github.com/akka/akka) and [akka-http](https://github.com/akka/akka-http).

API Documentation on Postman: [blockchain-in-scala](https://documenter.getpostman.com/view/1231202/blockchain-in-scala/RVu8iTUP)

Start the service, run this Class:

```
com.fluency03.blockchain.api.Server
```


#### Todos

- Complete APIs' Todos
- API tests
- Make states in actor persistent (using Akka Persistent)
- Concurrent collections?
- Make data distributed within cluster of peer (using Akka Cluster)
- Block propagation among peers
- etc.

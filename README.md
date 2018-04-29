# Blockchain in Scala

[![Travis branch](https://img.shields.io/travis/fluency03/blockchain-in-scala/master.svg)](https://travis-ci.org/fluency03/blockchain-in-scala)
[![Coveralls github branch](https://img.shields.io/coveralls/github/fluency03/blockchain-in-scala/master.svg)](https://coveralls.io/github/fluency03/blockchain-in-scala)

A simplified Blockchain implementation in Scala based on the specifications of Bitcoin.

*This project is still under development. APIs are not fully completed according to the cores.*

Inspired by:
- [Daniel van Flymen](http://www.dvf.nyc/)'s blog [Learn Blockchains by Building One](https://hackernoon.com/learn-blockchains-by-building-one-117428612f46)
- [Naivecoin](https://github.com/lhartikk/naivecoin) of [@lhartikk](https://github.com/lhartikk)

REST API service is built on the open source projects [akka](https://github.com/akka/akka) and [akka-http](https://github.com/akka/akka-http).

API Documentation on Postman: [blockchain-in-scala](https://documenter.getpostman.com/view/1231202/blockchain-in-scala/RVu8iTUP)

Start the service, run this Class:

```
com.fluency03.blockchain.api.Server
```


#### Todos

- Complete APIs' Todos
- Make states in actor persistent (using Akka Persistent)
- Make data distributed within cluster of peer (using Akka Cluster)
- Block propagation among peers
- etc.

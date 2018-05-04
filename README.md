# Blockchain in Scala

[![Travis branch](https://img.shields.io/travis/fluency03/blockchain-in-scala/master.svg)](https://travis-ci.org/fluency03/blockchain-in-scala)
[![Coveralls github branch](https://img.shields.io/coveralls/github/fluency03/blockchain-in-scala/master.svg)](https://coveralls.io/github/fluency03/blockchain-in-scala)

A simplified Blockchain implementation in Scala based on the specifications of Bitcoin.

*This project is still under development. APIs are not fully completed according to the cores.*

Inspired by:
- [Daniel van Flymen](http://www.dvf.nyc/)'s blog [Learn Blockchains by Building One](https://hackernoon.com/learn-blockchains-by-building-one-117428612f46)
- [Naivecoin](https://github.com/lhartikk/naivecoin) of [@lhartikk](https://github.com/lhartikk)

This project is meant to learn and understand Bitcoin and Blockchain. I personally also in the process of learning. So, what's a better the approach of learning it by building one ! Because of this is a continuous learning process, there might be something I do not understand earlier. Now when I understand it, the code will also be changed according. I am happy to discuss with you, which will make this learning process quicker and efficient.

Start the service, run this Class:

```
com.fluency03.blockchain.api.Server
```

## REST APIs

REST API service is built on the open source projects [akka](https://github.com/akka/akka) and [akka-http](https://github.com/akka/akka-http).

Please see the full API Documentation on Postman: [blockchain-in-scala](https://documenter.getpostman.com/view/1231202/blockchain-in-scala/RVu8iTUP)

Summary:

### root

```
/
```

### blockchain

```
GET  /blockchain
POST /blockchain
DEL  /blockchain
GET  /blockchain/validity
GET  /blockchain/last-block
DEL  /blockchain/last-block
POST /blockchain/new-block
POST /blockchain/next-block
GET  /blockchain/block/:hash
GET  /blockchain/block/:hash/transaction/:id
```


### block

```
GET  /blocks
GET  /block/:hash
POST /block
DEL  /block/:hash
GET  /block/:hash/transaction/:id
```


### transaction

```
GET  /transactions
GET  /transaction/:id
POST /transaction
PUT  /transaction/:id
DEL  /transaction/:id
```


### network

```
GET  /network
POST /peer
GET  /peers
GET  /peers?names=name1,name2
GET  /peer/:name
```


### generic

```
POST /generic/to-sha256
POST /generic/to-base64
POST /generic/from-base64
POST /generic/to-epoch-time
POST /generic/time-from-epoch
```



## Todos

- Complete APIs' Todos
- API tests
- Make states in actor persistent (using Akka Persistent)
- Concurrent collections?
- Make data distributed within cluster of peer (using Akka Cluster)
- Block propagation among peers
- etc.

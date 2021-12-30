## Simple JWT scala 3 library 

[![Latest version](https://index.scala-lang.org/vlmiroshnikov/authz/authz-core/latest.svg)](https://index.scala-lang.org/vlmiroshnikov/authz/authz-core/0.3.5)
[![Latest version](https://index.scala-lang.org/vlmiroshnikov/authz/authz-circe/latest.svg)](https://index.scala-lang.org/vlmiroshnikov/authz/authz-circe/0.3.5)

## Install 
```
libraryDependencies += "io.github.vlmiroshnikov" %% "authz-core" % "<version>" 
libraryDependencies += "io.github.vlmiroshnikov" %% "authz-circe" % "<version>" //  marshalling with circe 
```

## Example

```
import cats.syntax.all.*
import cats.effect.*

import io.github.vlmiroshnikov.authz.jwt.{given, *}
import io.github.vlmiroshnikov.authz.jwt.impl.RS256
import io.github.vlmiroshnikov.authz.jwt.circe.{given, *}
import io.github.vlmiroshnikov.authz.utils.*

object SimpleApp extends IOApp.Simple {

  def run: IO[Unit] = {
    
    val keyPair = JCAHelper.generateRSAKeyPair 

    given s: Signer[IO]   = RS256.signer[IO](keyPair.getPrivate())
    given v: Verifier[IO] = RS256.verifier[IO](keyPair.getPublic())

    for
      jwt <- buildAndSign[IO, StdHeader, StdClaims](StdHeader(algorithm = v.algo.name), StdClaims())
      _   <- IO.println(jwt.show)
      res <- parse[IO, StdHeader, StdClaims](jwt.show)
      v   <- verify[IO, StdHeader, StdClaims](res.header, res.claims, res.signature, res.signedPart)
      _   <- IO.println(res.toString)
      _   <- IO.println("Result: " + v.show)
    yield ()
  }
}
```


## Dependencies
* scala 3.1.0
* cats 2.7.0
* apache common-codecs 1.15
* circe 0.14.1

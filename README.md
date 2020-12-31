## Simple JWT scala 3 library 


## Install 
```
libraryDependencies += "io.github.vlmiroshnikov" %% "authz-core" % "<version>" 
libraryDependencies += "io.github.vlmiroshnikov" %% "authz-circe" % "<version>" //  marshalling with circe 
```

## Example

```
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

import io.github.vmiroshnikov.authz.jwt.{given, _}
import io.github.vmiroshnikov.authz.jwt.impl.RS256
import io.github.vmiroshnikov.authz.jwt.circe.{given, _}
import io.github.vmiroshnikov.authz.utils._

object SimpleApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    
    val keyPair = JCAHelper.generateRSAKeyPair

    given s: Signer[IO] = RS256.signer[IO](keyPair.getPrivate())
    given v: Verifier[IO] = RS256.verifier[IO](keyPair.getPublic())
    for
      jwt <- buildAndSign[IO, StdHeader, StdClaims](StdHeader(algorithm = v.algo.name), StdClaims())
      _   <- IO.delay(println(jwt.show))     // encoded as <header>.<payload>.<signature>
      res <- parse[IO, StdHeader, StdClaims](jwt.show)
      v   <- verify[IO, StdHeader, StdClaims](res.header, res.claims, res.signature, res.signedPart)
      _   <- IO.delay(println("Result: " + v.toString))  
    yield ExitCode.Success
  }
}
```


## Dependecies 

* cats 2.3.1
* apache common-codecs 1.15
* circe 0.13.0

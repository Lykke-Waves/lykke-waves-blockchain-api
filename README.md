# lykke-waves-blockchain-api
Waves Blockchain API Module for Lykke Exchange.

This module provides access to data that has been written by the scanner module or by itself.

# Build

This project uses [sbt](https://www.scala-sbt.org/) for building:

```
sbt clean debian:packageBin
```

After that, the .deb package will be available in the `target` project folder.

# Installation

Just install the .deb package and start the service.

# Configuration

For this moment there are no any configuration allowed, it will be as HTTP service at `localhost:8080`. 
Also it requires MongoDB installed at `mongodb://127.0.0.1:27017` and uses db `lykke-waves`.

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

The app reads settings from the URL or local file specified in the `SettingsUrl` environment variable.

There are allowed settings:

```
NetworkType: String - "main" or "test"
MongoDBHost: String - ex. "localhost"
MongoDBPort: Int - ex. 27017
ServiceHost: String - ex. "localhost"
ServicePort: Int - ex. 8080
```

By default it will be a HTTP service at `localhost:8080` for Waves Mainnet and it requires MongoDB installed at `127.0.0.1:27017` and uses db `lykke-waves` (`lykke-waves-testnet` due your `NetworkType`).

# Todos

Due to the rather tight deadlines of the contest and lack of free time, the project will be completed a little later.

- [x] Make the required logging format
- [ ] Make the database errors resilience
- [x] Make not implemented methods
- [ ] Make the routes tests
- [ ] Clean up the code
- [x] NetworkType (testnet) support

# Docker

You can build and start the docker container using

```
docker build -t "lykke-waves-blockchain-api-$(git describe --tags --always)" .
docker run "lykke-waves-blockchain-api-$(git describe --tags --always)"
```

# The results of the contest

Fuck you, Lykke, this company is fraud and deception. I have never met so many incompetent IT experts, I hope you will go bankrupt and people will not trust money to the swindlers. If I knew that before the integration of my decision, I will not get any money and that you have no mutual interest in doing it - I would not take part in this and would not advise anyone.

# Virtual Café Server

Welcome to the Virtual Café server application! This server simulates a café environment where clients can place orders for tea or coffee.

## Table of Contents

- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
- [Features](#features)
- [Contributing](#contributing)
- [License](#license)

## Getting Started

### Prerequisites

- Java Development Kit (JDK) installed on your machine.
- Gson library. Download it from [here](https://github.com/google/gson) and place the `gson.jar` file in the root directory.


### Installation
navigate to the root of the file path
```bash
cd virtual-cafe
```
compile barista with gson
```bash
javac -cp ".:gson.jar" Barista.java
```
run barista
```bash
java -cp ".:gson.jar" Barista
```
compile customer
```bash
javac -cp "." Customer.java
```
run customer
```bash
java -cp "." Customer
```

### Features
- Multithreaded server architecture.
- Simulates a dynamic café environment with waiting, brewing, and tray areas.
- Handles multiple clients concurrently.
- Authentication mechanism using a shared secret key.
- Graceful handling of client exits and SIGTERM signals.
- Logging of server states in the terminal and JSON file.
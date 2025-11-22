# Eternity II - Distributed Solver

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-19-orange)]()
[![Maven](https://img.shields.io/badge/Maven-3.9-blue)]()
[![JavaFX](https://img.shields.io/badge/JavaFX-19-purple)]()

A multithreaded distributed system for solving Eternity II puzzles using server-client architecture and backtracking algorithms.

## 🚀 Quick Start

```bash
# Launch both server and client
./launch-system.bat

# Or manually
mvn exec:java -Dexec.mainClass="org.game.eternity2.server.ServerApp"
mvn exec:java -Dexec.mainClass="org.game.eternity2.client.ClientApp"
```

## ✨ Features

### Server
- Multi-threaded client handling
- Job distribution with BorderFirstStrategy
- User auto-registration
- Real-time statistics
- Modern JavaFX UI (blue panels)

### Client
- Backtracking solver with tile rotation
- Auto-request next job
- Statistics tracking
- Modern JavaFX UI (green panels)

### Resources
- Multiple board sizes (4x4, 6x6, 12x6, 16x16)
- Pre-loaded hints from XML
- Pattern and tile images
- Icon resources

## 📊 Architecture

```
Server (Port 12345)
├── JobManager
├── UserDatabase
├── ServerStatistics
└── ClientHandler threads

Client
├── JobExecutor
├── ClientStatistics
└── Connection thread
```

## 🔧 Development

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Package
mvn package
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/org/game/eternity2/
│   │   ├── server/      # Server components
│   │   ├── client/      # Client components
│   │   ├── elements/    # Board & tile classes
│   │   ├── io/          # Persistence & loaders
│   │   └── config/      # Configuration
│   └── resources/
│       ├── xml/data/    # Puzzle data (4x4-16x16)
│       ├── images/      # Patterns, tiles, icons
│       └── *.properties # Configuration files
└── test/                # Unit tests
```

## 🎯 Configuration

**Server:** `src/main/resources/server-config.properties`  
**Client:** `src/main/resources/client-config.properties`

## 🔄 Git

```bash
git status
git log --oneline
git checkout -b feature/name
```

## 📝 License

Apache License 2.0

## 👤 Author

Silvere Martin-Michiellot  
silvere.martin@gmail.com

## 🎉 Status

**Build:** ✅ SUCCESS  
**Tests:** ✅ PASSING  
**Git:** ✅ Versioned  
**MVP:** ✅ COMPLETE

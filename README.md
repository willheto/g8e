
<h1>g8e</h1>

**early development**

## About the project

g8e is a hobby project to create a 2D game engine/server from scracth, without using any major external tools. Currently is serves as the server/game engine for my unnamed MMORPG. 
The engine operates on a tick-based system, updating game state and processing events every 600 milliseconds (0.6 seconds). This approach ensures smooth gameplay and synchronized interactions between players

This reposiroty also includes register server, update server and login server.

## Register server

Constains one simple /create-account endpoint.

## Update server

The game engine/server supports serving assets from server side. Players first connect to update server, which checks if current cache is stil valid,
or if there is an update available. If update is available, the update server packs and sends new assets to the client.

## Login server

Login server handles login, logout, count players and reset world requests.

## Getting Started

1. Install docker and gradle
2. Run docker-compose up -d on root to initialize database container
3. Run gradle run --args="migrate" to run migrations
4. Run gradle run to spin up the server

## Technologies used

1. Java 21
2. Gradle
3. Docker
4. MariaDB



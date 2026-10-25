# System Health Dashboard API

A small Spring Boot API that reports service health, version and environment,
built with a Git, Docker and Jenkins delivery workflow.

## Prerequisites

## Setup

## Running tests
./mvnw test          # Git Bash / Linux / Mac
.\mvnw.cmd test      # Windows PowerShell

## Running in Docker

## Kafka
/health sends a health check even to the kafka after responding to user requrest
/events will pull the latest 20 events

to enable - - Turn it on with `KAFKA_ENABLED=true` and `KAFKA_BOOTSTRAP_SERVERS`.

to run both containers together, use - `docker compose up -d --build`
## Jenkins pipeline
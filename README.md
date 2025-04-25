# Database Sharding Service

## Project Overview

This project implements a **horizontal database sharding** system based on **Spring Boot**. The system is designed to efficiently distribute user data across multiple PostgreSQL instances, providing:

- Enhanced performance
- Application scalability
- Fault tolerance

## Implemented Functionality

### Horizontal Sharding Mechanism
- Data distribution across **three independent PostgreSQL shards**
- Dynamic request routing to appropriate shards

### Database Infrastructure
- Configuration of three PostgreSQL instances using **Docker Compose**
- Individual connection settings for each shard

### Routing Architecture
- Implementation of `AbstractRoutingDataSource` for dynamic data source switching
- Use of `ThreadLocal` to maintain current shard context

### Data Distribution Algorithm
- Shard determination based on **user UUID** (character sum method)
- Request distribution using modulo operation on shard count

### REST API Endpoints
- `POST /users` - Create users with automatic shard determination
- `GET /users` - Retrieve all users (aggregating data from all shards)
- `GET /users/{id}` - Find specific user with routing to correct shard

### Transaction Management
- Proper transaction handling in distributed database context
- Data integrity assurance across multiple data sources

**Sharding Algorithm:**
1. Calculate sum of character codes from user's UUID
2. Apply modulo operation with shard count
3. Determine target shard (0, 1 or 2)
4. Dynamically route request to appropriate database

# Key Advantages
1. Scalability - Ability to add new shards as data grows
2. Performance - Load distribution across multiple databases
3. Fault Isolation - Issues in one shard don't affect others
4. Flexibility - Modifiable sharding strategy
![deepseek_mermaid_20250425_f2a1d7](https://github.com/user-attachments/assets/7dc0c311-6dbe-42c1-9b20-1747deea0011)

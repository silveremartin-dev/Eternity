-- Eternity II Database Schema
-- Version: 1.0
-- Description: Initial schema for users, puzzles, solutions, and configuration

-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Puzzles table
CREATE TABLE puzzles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    pieces_data JSONB NOT NULL,
    constraints_data JSONB,
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    difficulty VARCHAR(50)
);

-- Solutions table
CREATE TABLE solutions (
    id SERIAL PRIMARY KEY,
    puzzle_id INTEGER REFERENCES puzzles(id),
    user_id INTEGER REFERENCES users(id),
    solution_data JSONB NOT NULL,
    steps INTEGER,
    time_ms BIGINT,
    complete BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Configuration table (key-value store)
CREATE TABLE config (
    key VARCHAR(255) PRIMARY KEY,
    value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER REFERENCES users(id)
);

-- Indexes
CREATE INDEX idx_puzzles_created_by ON puzzles(created_by);
CREATE INDEX idx_solutions_puzzle_id ON solutions(puzzle_id);
CREATE INDEX idx_solutions_user_id ON solutions(user_id);
CREATE INDEX idx_config_key ON config(key);

-- Insert default configuration
INSERT INTO config (key, value, description) VALUES
    ('server.port', '8080', 'Server port'),
    ('server.max_clients', '100', 'Maximum concurrent clients'),
    ('solver.timeout_ms', '60000', 'Solver timeout in milliseconds'),
    ('redis.enabled', 'true', 'Enable Redis job queue');

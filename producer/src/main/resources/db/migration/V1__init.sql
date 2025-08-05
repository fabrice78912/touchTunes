-- resources/schema.sql

CREATE TABLE touchtunes_db.users (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE touchtunes_db.tracks (
    id CHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    duration_seconds INT NOT NULL,
    genre VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE touchtunes_db.locations (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE touchtunes_db.jukeboxes (
    id CHAR(36) PRIMARY KEY,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    jukebox_id VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- e.g., ACTIVE, INACTIVE
    location_id CHAR(36),
    last_heartbeat TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE touchtunes_db.play_requests (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    track_id CHAR(36) NOT NULL,
    jukebox_id CHAR(36) NOT NULL,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING', -- e.g., PENDING, PLAYED, CANCELLED
    priority VARCHAR(20) DEFAULT 'STANDARD', -- or BOOSTED
    event_type VARCHAR(50), -- e.g., PLAY_REQUEST, SKIP, STOP

    CONSTRAINT fk_play_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_play_track FOREIGN KEY (track_id) REFERENCES tracks(id),
    CONSTRAINT fk_play_jukebox FOREIGN KEY (jukebox_id) REFERENCES jukeboxes(id)
);

CREATE TABLE touchtunes_db.queue_entries (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    track_id CHAR(36) NOT NULL,
    jukebox_id CHAR(36) NOT NULL,
    position INT NOT NULL,
    priority VARCHAR(20) DEFAULT 'STANDARD', -- or BOOSTED
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_queue_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_queue_track FOREIGN KEY (track_id) REFERENCES tracks(id),
    CONSTRAINT fk_queue_jukebox FOREIGN KEY (jukebox_id) REFERENCES jukeboxes(id)
);


CREATE INDEX idx_play_jukebox_id ON touchtunes_db.play_requests(jukebox_id);
CREATE INDEX idx_queue_jukebox_id ON touchtunes_db.queue_entries(jukebox_id);
CREATE INDEX idx_queue_priority_position ON touchtunes_db.queue_entries(jukebox_id, priority, position);


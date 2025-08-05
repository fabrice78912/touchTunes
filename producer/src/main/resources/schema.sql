
CREATE TABLE touchtunes_db.users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE touchtunes_db.tracks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    duration_seconds INT NOT NULL,
    genre VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE touchtunes_db.locations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE touchtunes_db.jukeboxes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    jukebox_id VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- e.g., ACTIVE, INACTIVE
    location_id BIGINT,
    last_heartbeat TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES locations(id)
);

CREATE TABLE touchtunes_db.play_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    track_id BIGINT NOT NULL,
    jukebox_id BIGINT NOT NULL,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING', -- e.g., PENDING, PLAYED, CANCELLED
    priority VARCHAR(20) DEFAULT 'STANDARD', -- or BOOSTED
    event_type VARCHAR(50), -- e.g., PLAY_REQUEST, SKIP, STOP

    CONSTRAINT fk_play_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_play_track FOREIGN KEY (track_id) REFERENCES tracks(id),
    CONSTRAINT fk_play_jukebox FOREIGN KEY (jukebox_id) REFERENCES jukeboxes(id)
);

CREATE TABLE touchtunes_db.queue_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    track_id BIGINT NOT NULL,
    jukebox_id BIGINT NOT NULL,
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



-- FULL RESET: purana database delete karke naya banata hai (sample data ke saath)
DROP DATABASE IF EXISTS art_exhibition;
CREATE DATABASE art_exhibition;
USE art_exhibition;

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(64) NOT NULL,
  role ENUM('ADMIN','ARTIST','ENTHUSIAST') NOT NULL,
  status ENUM('ACTIVE','BLOCKED') DEFAULT 'ACTIVE'
);

CREATE TABLE artworks (
  id INT AUTO_INCREMENT PRIMARY KEY,
  artist_id INT NOT NULL,
  title VARCHAR(150) NOT NULL,
  description TEXT,
  image_path VARCHAR(255),
  price DECIMAL(10,2) NOT NULL,
  status ENUM('PENDING','APPROVED','REJECTED','SOLD') DEFAULT 'PENDING',
  FOREIGN KEY (artist_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE exhibitions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(150) NOT NULL,
  description TEXT,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL
);

CREATE TABLE exhibition_artworks (
  exhibition_id INT,
  artwork_id INT,
  approval_status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
  PRIMARY KEY (exhibition_id, artwork_id),
  FOREIGN KEY (exhibition_id) REFERENCES exhibitions(id) ON DELETE CASCADE,
  FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE
);

CREATE TABLE participants (
  exhibition_id INT,
  user_id INT,
  PRIMARY KEY (exhibition_id, user_id),
  FOREIGN KEY (exhibition_id) REFERENCES exhibitions(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  artwork_id INT NOT NULL UNIQUE,
  amount DECIMAL(10,2) NOT NULL,
  payment_method VARCHAR(30),
  order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (artwork_id) REFERENCES artworks(id)
);

CREATE TABLE wishlist (
  user_id INT,
  artwork_id INT,
  PRIMARY KEY (user_id, artwork_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (artwork_id) REFERENCES artworks(id) ON DELETE CASCADE
);

CREATE TABLE feedback (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sender_id INT NOT NULL,
  receiver_id INT,
  artwork_id INT,
  message TEXT NOT NULL,
  sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE notifications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  message VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE settings (
  setting_key VARCHAR(50) PRIMARY KEY,
  setting_value VARCHAR(255)
);

-- ===== Sample data =====
-- Passwords: admin123, ravi123, meera123, priya123 (SHA-256)
INSERT INTO users (name, email, password, role) VALUES
('Admin', 'admin@art.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN'),
('Ravi Sharma', 'ravi@art.com', '18c43de4137739ba1abe4fbb837663c3ad71b4045fe09c51b209d2d47fb5fdea', 'ARTIST'),
('Meera Iyer', 'meera@art.com', 'f1efdb63a539cc0d48242c0b36a25be8df9116d3460ec650629c9ef86b542e1b', 'ARTIST'),
('Priya Singh', 'priya@art.com', '0a8b8dfad3f637d7d30fed7b108c5c5986c4775d14cab26ec9279866eba99116', 'ENTHUSIAST');

INSERT INTO artworks (artist_id, title, description, image_path, price, status) VALUES
(2, 'Sunset Over Ganga', 'Oil on canvas, evening aarti scene', NULL, 12000, 'APPROVED'),
(2, 'Blue Dreams', 'Abstract acrylic in shades of blue', NULL, 8500, 'APPROVED'),
(3, 'Lotus Bloom', 'Watercolor lotus in a quiet pond', NULL, 15000, 'APPROVED'),
(3, 'City Lights', 'Night skyline, mixed media', NULL, 9500, 'APPROVED'),
(2, 'Monsoon Mood', 'Rain over old streets', NULL, 7000, 'PENDING');

INSERT INTO exhibitions (title, description, start_time, end_time) VALUES
('Modern Indian Art 2026', 'Live showcase of contemporary Indian artists', NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 3 DAY),
('Winter Colors', 'Upcoming seasonal exhibition', NOW() + INTERVAL 7 DAY, NOW() + INTERVAL 14 DAY);

INSERT INTO exhibition_artworks (exhibition_id, artwork_id, approval_status) VALUES
(1, 1, 'APPROVED'), (1, 3, 'APPROVED'), (1, 2, 'PENDING');

INSERT INTO participants (exhibition_id, user_id) VALUES (1, 4);
INSERT INTO notifications (message) VALUES ('Welcome to Online Art Exhibition Platform!');
INSERT INTO settings (setting_key, setting_value) VALUES
('site_name', 'Online Art Exhibition'), ('tax_percent', '5'), ('max_artwork_price', '1000000');

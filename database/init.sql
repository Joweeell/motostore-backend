-- MotorStore EVA 3 - ejecutar una sola vez en una base nueva
-- Esquema y datos del ZIP original; incluye vendedor, sin usuario de prueba en texto plano.

CREATE DATABASE IF NOT EXISTS motorstore;
USE motorstore;

-- 1. Tabla de Roles
CREATE TABLE roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Tabla de Usuarios
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_rol INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

-- 3. Tabla de Categorías
CREATE TABLE categorias (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
);

-- 4. Tabla de Productos
CREATE TABLE productos (
    id_product INT AUTO_INCREMENT PRIMARY KEY,
    id_categoria INT NOT NULL,
    nombre_producto VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL,
    precio_oferta DECIMAL(10, 2) DEFAULT NULL,
    stock INT NOT NULL DEFAULT 0,
    imagen_url VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
);

-- 5. Tabla de Pedidos
CREATE TABLE pedidos (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10, 2) NOT NULL,
    estado_pago VARCHAR(50) NOT NULL DEFAULT 'pendiente',
    direccion_envio TEXT NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- 6. Detalle del Pedido
CREATE TABLE pedido_detalles (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_product)
);

-- 7. Tabla de Mensajes de Contacto
CREATE TABLE mensajes_contacto (
    id_mensaje INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    asunto VARCHAR(150),
    mensaje TEXT NOT NULL,
    fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

USE motorstore;

-- 1. Insertar los roles del sistema
INSERT INTO roles (nombre_rol) VALUES ('admin'), ('cliente'), ('vendedor');

-- 2. Insertar las categorías de tus productos
INSERT INTO categorias (nombre_categoria, descripcion) 
VALUES ('Equipamiento', 'Cascos, chaquetas, guantes y protección certificada.'),
       ('Repuestos', 'Sistemas de escape, frenos y componentes mecánicos.'),
       ('Mantención', 'Limpiadores, lubricantes y aceites de alta gama.');

-- 3. Insertar los productos iniciales usando tus imágenes reales de public/
INSERT INTO productos (id_categoria, nombre_producto, descripcion, precio, stock, imagen_url)
VALUES 
(1, 'Casco Deportivo Integral', 'Casco con certificación de seguridad internacional, aerodinámico.', 149.99, 12, 'casco.jpg'),
(1, 'Chaqueta de Cuero Motociclista', 'Chaqueta de cuero de alta resistencia con protecciones internas.', 199.50, 8, 'chaqueta.jpg'),
(1, 'Guantes de Protección', 'Guantes con protección de nudillos rígida y excelente agarre.', 35.000, 25, 'guantes.jpg'),
(2, 'Sistema de Escape Deportivo', 'Escape de alto rendimiento optimizado para ganar potencia y sonido.', 299.99, 5, 'escape.webp'),
(3, 'Limpiador de Cadena Premium', 'Remueve grasa pesada, polvo y residuos acumulados fácilmente.', 14.50, 40, 'limpiador.webp'),
(1, 'Botas de Ruta Urbanas', 'Calzado reforzado impermeable y cómodo para viajes largos.', 110.00, 10, 'botas.jpg');

SHOW TABLES;
SELECT * FROM roles;
SELECT COUNT(*) AS total_categorias FROM categorias;
SELECT COUNT(*) AS total_productos FROM productos;


-- Configuración de usuario de base de datos y administrador por defecto
CREATE USER IF NOT EXISTS 'motorstore_app'@'localhost' IDENTIFIED BY 'MotorStore123';
GRANT ALL PRIVILEGES ON motorstore.* TO 'motorstore_app'@'localhost';
FLUSH PRIVILEGES;

INSERT IGNORE INTO usuarios (id_rol, nombre, apellido, email, password_hash)
VALUES (1, 'Oswel', 'Andrade', 'admin@duoc.cl', '$2a$12$3TWw./Ft0Rk22QNj5e1cwuwjKwpIcC9e.eWLpm7X4dDrvIFGoxxXy');


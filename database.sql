-- =====================================================
-- TechServices Peru - Ejercicio 1: API REST segura
-- Script de base de datos: bd_techservices
-- Ejecutar desde phpMyAdmin (pestana Importar) o consola MySQL.
-- Crea la base de datos, las tablas y los datos de prueba.
--
-- IMPORTANTE: este script borra bd_techservices si ya existe
-- para poder reimportarlo sin errores. Los valores de API Key,
-- token y saldos son valores de PRUEBA para desarrollo.
-- =====================================================

DROP DATABASE IF EXISTS bd_techservices;
CREATE DATABASE bd_techservices CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bd_techservices;

-- =====================================================
-- Tabla: usuarios_api (usuarios autorizados de la API)
-- =====================================================
CREATE TABLE usuarios_api (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario      VARCHAR(60)  NOT NULL,
    api_key             VARCHAR(100) NOT NULL UNIQUE,
    token_autenticacion VARCHAR(100) NOT NULL,
    tokens_disponibles  INT NOT NULL DEFAULT 0,
    estado              VARCHAR(10)  NOT NULL DEFAULT 'activo',
    CONSTRAINT chk_estado CHECK (estado IN ('activo', 'inactivo'))
);

-- =====================================================
-- Tabla: clientes (datos que consulta la API)
-- =====================================================
CREATE TABLE clientes (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    nombres           VARCHAR(80)  NOT NULL,
    apellidos         VARCHAR(80)  NOT NULL,
    dni               VARCHAR(8)   NOT NULL,
    correo_electronico VARCHAR(120) NOT NULL,
    telefono          VARCHAR(15)  NOT NULL
);

-- =====================================================
-- Usuarios de prueba
-- Los 2 primeros son los usuarios autorizados principales.
-- Los 2 ultimos existen SOLO para probar los casos de
-- "usuario inactivo" y "saldo agotado".
-- =====================================================
INSERT INTO usuarios_api (nombre_usuario, api_key, token_autenticacion, tokens_disponibles, estado) VALUES
-- Usuario autorizado principal (usuario de pruebas del examen)
('analista1', 'MI_API_KEY_2026', 'TOKEN_SEGURO_123456', 10, 'activo'),
-- Segundo usuario autorizado
('analista2', 'API_KEY_ANALISTA2_2026', 'TOKEN_ANALISTA2_2026', 10, 'activo'),
-- Usuario para probar la respuesta HTTP 403 (inactivo)
('usuario_inactivo', 'API_KEY_INACTIVA_2026', 'TOKEN_INACTIVO_2026', 5, 'inactivo'),
-- Usuario para probar la respuesta HTTP 429 (sin tokens)
('usuario_agotado', 'API_KEY_AGOTADA_2026', 'TOKEN_AGOTADO_2026', 0, 'activo');

-- =====================================================
-- Clientes ficticios de prueba (datos de ejemplo, no reales)
-- =====================================================
INSERT INTO clientes (nombres, apellidos, dni, correo_electronico, telefono) VALUES
('Carlos', 'Ramirez Soto', '12345678', 'carlos.ramirez@example.com', '999888777'),
('Maria', 'Fernandez Lopez', '23456789', 'maria.fernandez@example.com', '988777666'),
('Jorge', 'Torres Quispe', '34567890', 'jorge.torres@example.com', '977666555'),
('Lucia', 'Mendoza Castro', '45678901', 'lucia.mendoza@example.com', '966555444'),
('Andres', 'Huaman Rios', '56789012', 'andres.huaman@example.com', '955444333');

-- =====================================================
-- Verificacion (opcional): comprobar lo insertado
-- =====================================================
SELECT nombre_usuario, api_key, token_autenticacion, tokens_disponibles, estado
FROM usuarios_api;

SELECT * FROM clientes;

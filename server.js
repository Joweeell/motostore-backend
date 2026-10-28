const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json()); // Permite recibir datos en formato JSON

// Configuración de la conexión a MySQL
const db = mysql.createConnection({
    host: 'localhost',
    port: 3306,
    user: 'alumno_motor',       // Tu usuario de MySQL
    password: '1234',       // Si le pusiste contraseña en Workbench, escríbela aquí
    database: 'motorstore'
});

db.connect(err => {
    if (err) {
        console.error('Error al conectar a MySQL:', err);
    } else {
        console.log('Conectado exitosamente a la base de datos motorstore');
    }
});

//=========================================================================
// OPERACIONES CRUD PARA PRODUCTOS (Para vincular con productosCRUD.js)
// =========================================================================

// 1. OBTENER TODOS LOS PRODUCTOS (Para Home, Ofertas, Productos y AdminProductos)
app.get('/api/productos', (req, res) => {
    const q = "SELECT * FROM productos";
    db.query(q, (err, data) => {
        if (err) return res.status(500).json({ error: err.message });
        return res.json(data);
    });
});

//-- 2. OBTENER UN PRODUCTO POR ID (Para DetalleProducto y AdminProductoEditar)
app.get('/api/productos/:id', (req, res) => {
    const q = "SELECT * FROM productos WHERE id_product = ?";
    db.query(q, [req.params.id], (err, data) => {
        if (err) return res.status(500).json({ error: err.message });
        if (data.length === 0) return res.status(404).json({ message: "Producto no encontrado" });
        return res.json(data[0]);
    });
});

//-- 3. CREAR UN PRODUCTO NUEVO (Para AdminProductoNuevo)
app.post('/api/productos', (req, res) => {
    const q = "INSERT INTO productos (id_categoria, nombre_producto, descripcion, precio, precio_oferta, stock, imagen_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
    const values = [
        req.body.id_categoria,
        req.body.nombre_producto,
        req.body.descripcion,
        req.body.precio,
        req.body.precio_oferta || null,
        req.body.stock,
        req.body.imagen_url
    ];

    db.query(q, values, (err, result) => {
        if (err) return res.status(500).json({ error: err.message });
        return res.status(201).json({ message: "Producto creado con éxito", id: result.insertId });
    });
});

//-- 4. ACTUALIZAR UN PRODUCTO EXISTENTE (Para AdminProductoEditar)
app.put('/api/productos/:id', (req, res) => {
    const q = "UPDATE productos SET id_categoria = ?, nombre_producto = ?, descripcion = ?, precio = ?, precio_oferta = ?, stock = ?, imagen_url = ? WHERE id_product = ?";
    const values = [
        req.body.id_categoria,
        req.body.nombre_producto,
        req.body.descripcion,
        req.body.precio,
        req.body.precio_oferta || null,
        req.body.stock,
        req.body.imagen_url,
        req.params.id
    ];

    db.query(q, values, (err, result) => {
        if (err) return res.status(500).json({ error: err.message });
        return res.json({ message: "Producto actualizado con éxito" });
    });
});

//-- 5. ELIMINAR UN PRODUCTO (Para la tabla de gestión en AdminProductos)
app.delete('/api/productos/:id', (req, res) => {
    const q = "DELETE FROM productos WHERE id_product = ?";
    db.query(q, [req.params.id], (err, result) => {
        if (err) return res.status(500).json({ error: err.message });
        return res.json({ message: "Producto eliminado con éxito" });
    });
});

// Inicialización del servidor local
const PORT = 5000;
app.listen(PORT, () => {
    console.log(`Servidor de la API corriendo en http://localhost:${PORT}`);
});

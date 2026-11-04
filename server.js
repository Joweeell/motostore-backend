
require('dotenv').config();

const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const { OAuth2Client } = require('google-auth-library');
const jwt = require('jsonwebtoken');

// CONFIGURACIÓN EXPRESS
const app = express();

app.use(cors());
app.use(express.json());

// CONEXIÓN MYSQL
const db = mysql.createConnection({
    host: process.env.DB_HOST || 'localhost',
    port: Number(process.env.DB_PORT || 3306),
    user: process.env.DB_USER || 'alumno_motor',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'motorstore'
});

db.connect((err) => {
    if (err) {
        console.error('Error al conectar a MySQL:', err);
    } else {
        console.log(
            'Conectado exitosamente a la base de datos motorstore'
        );
    }
});

// ======================================================
// CRUD DE PRODUCTOS
// ======================================================

// 1. OBTENER TODOS LOS PRODUCTOS
app.get('/api/productos', (req, res) => {
    const q = 'SELECT * FROM productos';

    db.query(q, (err, data) => {
        if (err) {
            return res.status(500).json({
                error: err.message
            });
        }

        return res.json(data);
    });
});

// 2. OBTENER PRODUCTO POR ID
app.get('/api/productos/:id', (req, res) => {
    const q = 'SELECT * FROM productos WHERE id_product = ?';

    db.query(q, [req.params.id], (err, data) => {
        if (err) {
            return res.status(500).json({
                error: err.message
            });
        }

        if (data.length === 0) {
            return res.status(404).json({
                message: 'Producto no encontrado'
            });
        }

        return res.json(data[0]);
    });
});

// 3. CREAR PRODUCTO
app.post('/api/productos', (req, res) => {
    const q = `
        INSERT INTO productos
        (
            id_categoria,
            nombre_producto,
            descripcion,
            precio,
            precio_oferta,
            stock,
            imagen_url
        )
        VALUES (?, ?, ?, ?, ?, ?, ?)
    `;

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
        if (err) {
            return res.status(500).json({
                error: err.message
            });
        }

        return res.status(201).json({
            message: 'Producto creado con éxito',
            id: result.insertId
        });
    });
});

// 4. ACTUALIZAR PRODUCTO
app.put('/api/productos/:id', (req, res) => {
    const q = `
        UPDATE productos
        SET
            id_categoria = ?,
            nombre_producto = ?,
            descripcion = ?,
            precio = ?,
            precio_oferta = ?,
            stock = ?,
            imagen_url = ?
        WHERE id_product = ?
    `;

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
        if (err) {
            return res.status(500).json({
                error: err.message
            });
        }

        return res.json({
            message: 'Producto actualizado con éxito'
        });
    });
});

// 5. ELIMINAR PRODUCTO
app.delete('/api/productos/:id', (req, res) => {
    const q = 'DELETE FROM productos WHERE id_product = ?';

    db.query(q, [req.params.id], (err, result) => {
        if (err) {
            return res.status(500).json({
                error: err.message
            });
        }

        return res.json({
            message: 'Producto eliminado con éxito'
        });
    });
});

// ======================================================
// AUTENTICACIÓN CON GOOGLE
// ======================================================

const googleClient = new OAuth2Client();

// RUTA DE LOGIN GOOGLE
app.post('/api/auth/google', async (req, res) => {
    // COMPROBAR VARIABLES DE ENTORNO
    if (
        !process.env.GOOGLE_CLIENT_ID ||
        !process.env.JWT_SECRET
    ) {
        return res.status(503).json({
            ok: false,
            mensaje: 'Falta configurar Google en el backend.'
        });
    }

    const credential = req.body?.credential;

    // VALIDAR CREDENCIAL
    if (
        typeof credential !== 'string' ||
        credential.length < 20 ||
        credential.length > 10000
    ) {
        return res.status(400).json({
            ok: false,
            mensaje: 'La credencial de Google es inválida.'
        });
    }

    let payload;

    // VERIFICAR TOKEN CON GOOGLE
    try {
        const ticket = await googleClient.verifyIdToken({
            idToken: credential,
            audience: process.env.GOOGLE_CLIENT_ID
        });

        payload = ticket.getPayload();
    } catch (error) {
        console.error(
            'Google rechazó el token:',
            error.message
        );

        return res.status(401).json({
            ok: false,
            mensaje: 'No se pudo verificar la cuenta de Google.'
        });
    }

    // COMPROBAR INFORMACIÓN DEL USUARIO
    if (
        !payload?.sub ||
        !payload?.email ||
        payload.email_verified !== true
    ) {
        return res.status(401).json({
            ok: false,
            mensaje: 'La cuenta de Google no tiene un correo verificado.'
        });
    }

    // DATOS DEL USUARIO DE GOOGLE
    const googleId = payload.sub;

    const nombre = String(
        payload.name || payload.email.split('@')[0]
    ).slice(0, 150);

    const correo = payload.email
        .toLowerCase()
        .slice(0, 255);

    // GUARDAR USUARIO EN MYSQL
    try {
        await db.promise().execute(
            `INSERT INTO google_usuarios
                (google_id, nombre, correo)
             VALUES (?, ?, ?)
             ON DUPLICATE KEY UPDATE
                nombre = ?,
                correo = ?`,
            [
                googleId,
                nombre,
                correo,
                nombre,
                correo
            ]
        );
    } catch (error) {
        console.error(
            'Error al guardar usuario Google en MySQL:',
            error
        );

        return res.status(500).json({
            ok: false,
            mensaje: 'No se pudo guardar la cuenta Google en MySQL.'
        });
    }

    // CREAR USUARIO DE MOTORSTORE
    const usuario = {
        nombre,
        correo,
        rol: 'cliente',
        proveedor: 'google'
    };

    // GENERAR TOKEN DE SESIÓN
    const token = jwt.sign(
        {
            sub: googleId,
            rol: 'cliente',
            proveedor: 'google'
        },
        process.env.JWT_SECRET,
        {
            expiresIn: '2h',
            issuer: 'motorstore'
        }
    );

    // RESPUESTA AL FRONTEND
    return res.json({
        ok: true,
        mensaje: 'Inicio de sesión correcto',
        usuario,
        token
    });
});

// ======================================================
// INICIAR SERVIDOR
// ======================================================

const PORT = Number(process.env.PORT || 5000);

app.listen(PORT, () => {
    console.log(
        `Servidor API corriendo en http://localhost:${PORT}`
    );
});

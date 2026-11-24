# MotorStore - Backend API REST

MotorStore Backend es el servicio encargado de gestionar la información y la lógica de funcionamiento de la tienda online MotorStore.

Fue desarrollado utilizando Java y Spring Boot, con una base de datos MySQL y una API REST que permite la comunicación con el frontend.

## Tecnologías utilizadas

- **Java 21:** lenguaje de programación
- **Spring Boot 4.1.2:** desarrollo del backend
- **Spring Data JPA:** acceso y manejo de datos
- **MySQL:** base de datos relacional
- **Spring Security:** seguridad y control de acceso
- **JWT:** autenticación mediante tokens
- **BCrypt:** protección de contraseñas
- **Swagger / OpenAPI:** documentación de endpoints
- **Maven:** administración de dependencias y ejecución del proyecto

## Funcionalidades principales

- Registro e inicio de sesión de usuarios
- Autenticación mediante JWT
- Administración de usuarios
- Administración de productos
- Administración de categorías
- Creación y consulta de pedidos
- Consulta del historial de pedidos de cada cliente
- Actualización del estado de los pedidos
- Registro y consulta de mensajes de contacto
- Control de acceso según los roles de usuario

## Roles del sistema

El sistema considera tres tipos de usuarios:

- **Administrador:** puede administrar usuarios, productos, categorías y pedidos, además de gestionar otras funciones administrativas
- **Vendedor:** puede consultar pedidos y mensajes de contacto
- **Cliente:** puede iniciar sesión, consultar productos y realizar pedidos

## Estructura del proyecto

```text
motostore-backend/
│
├── database/
│   └── init.sql              # Creación y datos iniciales de MySQL
│
├── src/
│   ├── main/
│   │   ├── java/cl/duoc/motostore/
│   │   │   ├── config/       # Configuración de seguridad y Swagger
│   │   │   ├── controller/   # Controladores REST
│   │   │   ├── dto/          # Objetos de transferencia de datos
│   │   │   ├── model/        # Entidades del sistema
│   │   │   ├── repository/   # Acceso a la base de datos
│   │   │   ├── security/     # Manejo de JWT
│   │   │   └── service/      # Lógica del negocio
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/                 # Pruebas automatizadas
│
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Base de datos

El proyecto utiliza MySQL y trabaja con una base de datos llamada `motorstore`.

El archivo de inicialización se encuentra en:

```text
database/init.sql
```

Este script crea las siguientes tablas:

- roles
- usuarios
- categorias
- productos
- pedidos
- pedido_detalles
- mensajes_contacto

También incorpora los roles iniciales, categorías y productos de ejemplo.

**Importante:** el script debe ejecutarse una sola vez sobre una base de datos nueva. No crea un usuario administrador de prueba.

La aplicación utiliza `spring.jpa.hibernate.ddl-auto=validate`, por lo que las tablas deben existir antes de iniciar el backend.

## Configuración del proyecto

Se necesita tener instalado:

- Java 21
- MySQL
- Maven (se incluye Maven Wrapper en el proyecto)

La conexión a MySQL se configura mediante variables de entorno:

```powershell
$env:DB_USER="motorstore_app"
$env:DB_PASSWORD="TU_CLAVE_MYSQL"
$env:MOTORSTORE_JWT_SECRET="TU_SECRET_BASE64"
```

El secreto JWT debe estar codificado en Base64 y representar al menos 32 bytes. No se deben subir contraseñas ni secretos al repositorio.

## Instalación y ejecución

Clonar el repositorio:

```bash
git clone https://github.com/Joweeell/motostore-backend.git
```

Ingresar a la carpeta:

```bash
cd motostore-backend
```

Configurar previamente MySQL, ejecutar el archivo `database/init.sql` y definir las variables de entorno.

Iniciar el backend en Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

El servidor se ejecutará normalmente en:

```text
http://localhost:8080
```

## API REST

La API utiliza el siguiente prefijo:

```text
http://localhost:8080/api/v1
```

Principales grupos de endpoints:

| Recurso | Endpoint |
|---|---|
| Autenticación | `/auth` |
| Productos | `/productos` |
| Categorías | `/categorias` |
| Pedidos | `/pedidos` |
| Usuarios | `/usuarios` |
| Contacto | `/contacto` |

Las operaciones disponibles y sus permisos dependen del método HTTP y del rol del usuario autenticado.

## Documentación Swagger

El proyecto incorpora Swagger para consultar y probar los endpoints de la API REST.

Con el backend iniciado, se puede acceder desde:

```text
http://localhost:8080/swagger-ui/index.html
```

La documentación OpenAPI también se encuentra disponible en:

```text
http://localhost:8080/v3/api-docs
```

## Seguridad

El backend utiliza Spring Security para proteger los recursos y controlar el acceso según el rol del usuario.

La autenticación se realiza mediante JWT y las contraseñas se almacenan utilizando BCrypt.

Las solicitudes que necesitan autenticación deben enviar el token en el encabezado:

```text
Authorization: Bearer TOKEN
```

## Pruebas

El proyecto incluye pruebas automatizadas para revisar el funcionamiento de la aplicación, la documentación Swagger y algunos permisos de acceso a los endpoints.

Para ejecutar las pruebas en Windows:

```powershell
.\mvnw.cmd test
```


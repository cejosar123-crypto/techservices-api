# TechServices Peru - API REST segura (Ejercicio 1)

API REST desarrollada con **Java + Spring Boot 3 + MySQL** que permite consultar
clientes por ID validando una **API Key** y un **token de autenticacion**, y que
**descuenta 1 token solo cuando la consulta es exitosa**.

## Arquitectura (en capas)

```
src/main/java/pe/techservices/
├── model/        → Entidades JPA: Cliente, UsuarioApi
├── repository/   → Acceso a datos (Spring Data JPA)
├── service/      → Logica de negocio: consulta + descuento del token
├── controller/   → Endpoint REST: GET /api/clientes/{id}
├── security/     → Validacion de API Key y token Bearer
├── dto/          → Objetos de respuesta JSON
├── exception/    → Manejo de errores HTTP (401, 403, 404, 429, 500)
└── TechservicesApiApplication.java → Clase principal de Spring Boot
```

## Reglas de seguridad implementadas

| # | Regla | Codigo HTTP si falla |
|---|-------|----------------------|
| 1 | La solicitud debe incluir `x-api-key` | 401 |
| 2 | Debe incluir `Authorization: Bearer <token>` | 401 |
| 3 | API Key y token deben ser del MISMO usuario | 401 |
| 4 | El usuario debe estar `activo` | 403 |
| 5 | El usuario debe tener al menos 1 token | 429 |
| 6 | El cliente solicitado debe existir | 404 |
| 7 | Se descuenta 1 token solo si la consulta es exitosa | — |
| 8 | Si el cliente no existe, NO se descuentan tokens | 404 |
| 9 | Error interno generico sin detalles sensibles | 500 |

El descuento es **atomico**: se hace con un UPDATE que incluye la condicion
`tokens_disponibles > 0`, dentro de una transaccion. Si dos solicitudes
llegan al mismo tiempo y solo queda 1 token, la base de datos garantiza que
solo una logra descontarlo; la otra recibe 429. Nunca se descuenta antes de
comprobar que el cliente existe.

## Requisitos previos

1. **Java 21 o superior** (verificado con `java -version`). El proyecto usa Java 21.
   - Java 22 tambien funciona (compila para Java 21 sin cambiar configuraciones).
2. **XAMPP** con MySQL/MariaDB y phpMyAdmin.
3. **Postman** o Postman Web (https://www.postman.com).
4. Maven: NO es necesario instalarlo. El proyecto incluye **Maven Wrapper**
   (`mvnw.cmd`), que se descarga Maven automaticamente la primera vez.
5. Git (solo para publicar en GitHub).

> Nota: el MySQL de XAMPP es en realidad **MariaDB**, y es 100% compatible
> con este proyecto (mismo driver, mismas consultas).

---

## PASO 1: Abrir el proyecto

Puedes abrirlo en VS Code (recomendado: extension *Extension Pack for Java*)
o en IntelliJ IDEA Community.

```cmd
cd C:\Users\UPSJB\techservices-api
code .
```

## PASO 2: Iniciar MySQL en XAMPP

1. Doble clic en `C:\xampp\xampp-control.exe` (no requiere administrador).
2. Pulsa **Start** en la fila de **MySQL**.
3. Verifica que aparezca "MySQL started" y el puerto 3306.

> Si el puerto 3306 ya esta ocupado, revisa la seccion de errores comunes.

## PASO 3 y 4: Crear la base de datos con phpMyAdmin

1. Abre http://localhost/phpmyadmin en el navegador (Apache debe estar
   iniciado en XAMPP para usar phpMyAdmin; para la API solo hace falta MySQL).
2. Pestaña **Importar**.
3. Selecciona el archivo `database.sql` de este proyecto.
4. Pulsa **Continuar**.
5. Al terminar verás la base de datos `bd_techservices` con las tablas
   `usuarios_api` y `clientes`, 4 usuarios de prueba y 5 clientes.

Alternativa desde CMD (sin abrir el navegador):

```cmd
C:\xampp\mysql\bin\mysql.exe -u root < C:\Users\UPSJB\techservices-api\database.sql
```

### Usuarios de prueba (valores de desarrollo)

| usuario | x-api-key | Authorization Bearer | tokens | estado |
|---|---|---|---|---|
| analista1 | `MI_API_KEY_2026` | `TOKEN_SEGURO_123456` | 10 | activo |
| analista2 | `API_KEY_ANALISTA2_2026` | `TOKEN_ANALISTA2_2026` | 10 | activo |
| usuario_inactivo | `API_KEY_INACTIVA_2026` | `TOKEN_INACTIVO_2026` | 5 | inactivo |
| usuario_agotado | `API_KEY_AGOTADA_2026` | `TOKEN_AGOTADO_2026` | 0 | activo |

## PASO 5: Configurar variables de entorno

En **CMD** (valores temporales, validos solo en esa ventana):

```cmd
cd C:\Users\UPSJB\techservices-api
set DB_URL=jdbc:mysql://localhost:3306/bd_techservices?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=America/Lima
set DB_USERNAME=root
set DB_PASSWORD=
set SERVER_PORT=8080
```

> En CMD el caracter `&` debe escaparse con `^` porque separa comandos.
> En XAMPP el usuario root no tiene contrasena, por eso `DB_PASSWORD` queda vacio.

Si prefieres guardarlas de forma permanente (nivel usuario, sin admin):

```cmd
setx DB_USERNAME "root"
setx DB_PASSWORD ""
setx SERVER_PORT "8080"
```

(DB_URL es larga; al usar `setx` recuerda el escape `^&` o configúrala temporal
con `set` y ejecuta el proyecto en esa misma ventana).

Estas mismas variables son las que se configuraran en Render en el despliegue.

## PASO 6 y 7: Compilar y ejecutar Spring Boot

La **primera vez** el wrapper descarga Maven y las dependencias (tarda unos minutos):

```cmd
cd C:\Users\UPSJB\techservices-api
mvnw.cmd clean package
```

Ejecutar la aplicacion:

```cmd
mvnw.cmd spring-boot:run
```

Deberas ver al final algo como:

```
Tomcat started on port 8080 (http)
```

Compilar solo el JAR (para despliegue o para ejecutar directamente):

```cmd
mvnw.cmd clean package -DskipTests
java -jar target\techservices-api-1.0.0.jar
```

Ejecutar las pruebas unitarias:

```cmd
mvnw.cmd test
```

> Si usas la version instalada localmente de Maven en lugar del wrapper:
> `C:\Users\UPSJB\tools\apache-maven-3.9.6\bin\mvn.cmd clean package`

## PASO 8: Probar el endpoint desde Postman

Solicitud:

```
GET http://localhost:8080/api/clientes/1
```

Encabezados (Headers):

| Key | Value |
|---|---|
| `x-api-key` | `MI_API_KEY_2026` |
| `Authorization` | `Bearer TOKEN_SEGURO_123456` |

Respuesta esperada (HTTP 200):

```json
{
  "mensaje": "Consulta realizada correctamente",
  "cliente": {
    "id": 1,
    "nombres": "Carlos",
    "apellidos": "Ramirez Soto",
    "dni": "12345678",
    "correoElectronico": "carlos.ramirez@example.com",
    "telefono": "999888777"
  },
  "tokensRestantes": 9
}
```

Tambien puedes probar desde CMD:

```cmd
curl -s -H "x-api-key: MI_API_KEY_2026" -H "Authorization: Bearer TOKEN_SEGURO_123456" http://localhost:8080/api/clientes/1
```

La coleccion lista para importar en Postman esta en:
`postman/TechServices-Ejercicio1.postman_collection.json`
(Postman → Import → seleccionar el archivo).

### Casos de prueba incluidos en la coleccion

| Solicitud | Encabezados usados | Esperado |
|---|---|---|
| 1. Consulta exitosa | analista1 | 200 + tokensRestantes |
| 2. API Key incorrecta | x-api-key falsa | 401 |
| 3. Token incorrecto | token que no coincide | 401 |
| 4. Falta de encabezados | sin headers | 401 |
| 5. Usuario inactivo | usuario_inactivo | 403 |
| 6. Cliente inexistente | id 999 | 404 (no descuenta) |
| 7. Usuario sin tokens | usuario_agotado | 429 |

## PASO 9: Comprobar el descuento de tokens

1. En phpMyAdmin → `bd_techservices` → tabla `usuarios_api`: el usuario
   analista1 empieza con **10** tokens.
2. Ejecuta la solicitud "Consulta exitosa" en Postman: la respuesta muestra
   `tokensRestantes: 9` (saldo real despues del descuento).
3. En phpMyAdmin pulsa **Refrescar** y verifica que `tokens_disponibles` bajo a 9.
4. Repite la consulta con el id 999 (cliente inexistente): devuelve **404** y el
   saldo NO cambia (sigue en 9).
5. Consulta SQL de verificacion:

```sql
SELECT nombre_usuario, tokens_disponibles, estado FROM usuarios_api;
```

6. Si pones `tokens_disponibles = 0` en phpMyAdmin y vuelves a consultar,
   la API responde **429** (saldo agotado).

## PASO 10: Despliegue en Render

> IMPORTANTE: la base de datos local de XAMPP **no es accesible desde
> Internet**. Para el despliegue necesitas una base de datos MySQL en la nube.
> (El despliegue real en GitHub/Render debes hacerlo tu manualmente; los pasos son:)

### 10.1 Publicar el codigo en GitHub

1. Crea un repositorio nuevo (privado o publico) en https://github.com/new
2. Desde CMD:

```cmd
cd C:\Users\UPSJB\techservices-api
git init
git add .
git commit -m "API REST TechServices Peru - Ejercicio 1"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/techservices-api.git
git push -u origin main
```

3. Sube `database.sql` (ya esta en el repo) para tenerlo a mano al importar.

### 10.2 Crear una base de datos MySQL en la nube

Opciones con plan gratuito:

- **Aiven for MySQL** (plan free, https://aiven.io)
- **Clever Cloud** (add-on MySQL, https://www.clever-cloud.com)
- Cualquier otro proveedor que ofrezca MySQL con SSL (TiDB Serverless tambien funciona)

Pasos generales (con Aiven como ejemplo):

1. Crea el servicio MySQL (plan Free) y espera a que este "Running".
2. Copia el **Service URI** o los datos: host, puerto (normalmente 3306 o 3xxx),
   usuario (`avnadmin`), contrasena y nombre de BD.
3. Importa el script: abre la consola SQL del proveedor (Aiven tiene
   "Query editor") y pega el contenido de `database.sql`, o conectate desde
   MySQL Workbench con las credenciales y ejecutalo.
4. Guarda credenciales: el script `database.sql` no contiene contrasenas.

> Seguridad: no publiques la contrasena en el repositorio. Solo se configura
> como variable de entorno en Render.

### 10.3 Crear el servicio en Render

1. Entra en https://dashboard.render.com → **New +** → **Web Service**.
2. Conecta tu cuenta de GitHub y elige el repositorio `techservices-api`.
3. Configuracion:
   - **Runtime**: Java (Render detecta Maven por el `pom.xml`).
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/techservices-api-1.0.0.jar`
   - **Plan**: Free
4. En **Environment** agrega las variables:

| Key | Value (ejemplo Aiven) |
|---|---|
| `DB_URL` | `jdbc:mysql://HOST_DEL_PROVEEDOR:PUERTO/nombre_bd?sslMode=REQUIRED&serverTimezone=America/Lima` |
| `DB_USERNAME` | usuario del proveedor (ej. `avnadmin`) |
| `DB_PASSWORD` | contrasena del proveedor (no se publica en el codigo) |

5. Despliega y prueba con Postman usando la URL de Render:

```
https://techservices-api.onrender.com/api/clientes/1
```

(Remueve `localhost` y usa el dominio que Render te asigne).

> El archivo `render.yaml` incluido define esta configuracion (Blueprint).
> El puerto lo toma automaticamente de la variable `PORT` de Render; no
> es necesario configurar `SERVER_PORT` en la nube.

> Nota: el plan Free de Render "duerme" el servicio tras 15 min de inactividad;
> la primera solicitud puede tardar ~1 minuto en despertar.

## PASO 11: Errores comunes y soluciones

| Error | Causa | Solucion |
|---|---|---|
| `Port 8080 was already in use` | otro programa usa 8080 | `set SERVER_PORT=8081` y reiniciar |
| `Access denied for user 'root'` | contrasena incorrecta en DB_PASSWORD | En XAMPP root no tiene contrasena: dejar `DB_PASSWORD` vacio |
| `Communications link failure` | MySQL de XAMPP apagado | Iniciar MySQL en el panel de XAMPP |
| `Unknown database 'bd_techservices'` | no se importo database.sql | Importar el script en phpMyAdmin (PASO 3-4) |
| Respuesta 401 en Postman | falta `x-api-key` o `Authorization` | Agregar los 2 encabezados exactamente como esta en la tabla |
| Respuesta 429 | saldo en 0 | En phpMyAdmin, volver a poner 10 tokens al usuario |
| Respuesta 404 | id de cliente inexistente | Probar con id 1 a 5 |
| `mvnw.cmd` no hace nada | no hay Java o JAVA_HOME mal | Verificar `java -version`; instalar Java 21+ |
| Error de compilacion la primera vez | dependencias sin descargar | Ejecutar de nuevo `mvnw.cmd clean package` con internet |

## Respuesta de errores (JSON consistente)

```json
// 401 (credenciales)
{ "mensaje": "API Key no registrada", "codigo": 401 }
// 403 (usuario inactivo)
{ "mensaje": "El usuario se encuentra inactivo", "codigo": 403 }
// 404 (cliente no existe, NO descuenta)
{ "mensaje": "No existe un cliente con el id 999", "codigo": 404 }
// 429 (saldo agotado)
{ "mensaje": "No tiene tokens disponibles", "codigo": 429 }
// 500 (error interno, sin detalles sensibles)
{ "mensaje": "Error interno del servidor", "codigo": 500 }
```

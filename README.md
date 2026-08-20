## File Storage

File Storage es una REST API desarrollada con Java y Spring Boot para la gestión de archivos mediante almacenamiento de objetos.

La aplicación permite subir, consultar, descargar y eliminar archivos, utilizando PostgreSQL para almacenar los metadatos y MinIO como almacenamiento de objetos compatible con S3.

El proyecto también incorpora validación del contenido real de los archivos mediante Apache Tika y un mecanismo de integridad basado en SHA-256, permitiendo detectar modificaciones o corrupción de archivos durante su descarga.

Version: 1.0.0

---

## Caracteristicas

+ Subida de archivos mediante multipart/form-data.
+ Descarga de archivos almacenados.
+ Consulta de metadatos.
+ Eliminación de archivos.
+ Almacenamiento de archivos mediante MinIO.
+ Persistencia de metadatos mediante PostgreSQL.
+ Generación de identificadores UUID.
+ Validación del nombre del archivo.
+ Validación del tamaño máximo.
+ Detección del tipo de contenido real mediante Apache Tika.
+ Validación de extensión según el contenido detectado.
+ Protección contra path traversal en nombres de archivos.
+ Cálculo de hash SHA-256 durante la subida.
+ Verificación de integridad mediante SHA-256 durante la descarga.
+ Rollback del objeto almacenado si falla la persistencia de metadatos.
+ Manejo global de excepciones.
+ Respuestas API con formato consistente.
+ Documentación de la API mediante OpenAPI y Scalar.
+ Health checks mediante Spring Boot Actuator.
+ Entorno reproducible mediante Docker Compose.
+ Tests unitarios e integración con Testcontainers.

---

## Arquitectura

La aplicación separa las responsabilidades principales en diferentes capas.

El flujo general de una subida es:
```
Cliente -> FileController -> FileService -> Validaciones -> SHA-256 -> StorageService -> MinIO
```
Al mismo tiempo, los metadatos del archivo se almacenan en PostgreSQL.

Para una descarga:

```
Cliente -> FileController -> FileService -> PostgreSQL (metadatos + SHA-256 esperado) -> MinIO (contenido) -> FileIntegrityInputStream -> Verificación SHA-256 -> Cliente
```

La aplicación mantiene una separación entre la lógica de negocio y el sistema de almacenamiento mediante StorageService, permitiendo que la implementación concreta utilizada actualmente sea MinIO.

---


## Tecnologías

**Backend**
+ Java 21
+ Spring Boot 4.0.7
+ Spring Web MVC
+ Spring Data JPA
+ Spring Validation
+ Spring Boot Actuator

**Base de datos**
+ PostgreSQL 18.4

**Almacenamiento**
+ MinIO
+ MinIO Java SDK 9.0.3

**Validación y seguridad de archivos**
+ Apache Tika 3.2.3
+ SHA-256 mediante MessageDigest

**API**
+ OpenAPI
+ Springdoc OpenAPI 3.1.0
+ Scalar

**Testing**
+ JUnit
+ Spring Boot Test
+ Testcontainers
+ PostgreSQL Testcontainer
+ MinIO Testcontainer

**Build**
+ Maven
+ Docker
+ Docker Compose

**Otras dependencias**
+ Lombok
+ OkHttp

---

## Requisitos

Para ejecutar el proyecto localmente se necesita:

+ Java 21
+ Maven
+ Docker
+ Docker Compose

No es necesario instalar PostgreSQL ni MinIO directamente en el sistema si se utiliza Docker Compose.

---

## Configuración

La aplicación utiliza variables de entorno para las credenciales y configuración de PostgreSQL y MinIO.

No se deben almacenar credenciales reales dentro del repositorio.

Crear un archivo:

```
.env
```

en la raíz del proyecto.

**Ejemplo de ".env"**:

```js
POSTGRES_HOST=nombre_del_host
POSTGRES_PORT=numero_del_puerto
POSTGRES_DB=nombre_de_la_base_de_datos
POSTGRES_USER=nombre_de_usuario
POSTGRES_PASSWORD=contraseña_de_usuario

MINIO_ROOT_USER=nombre_de_usuario_para_cms
MINIO_ROOT_PASSWORD=contraseña_para_cms
MINIO_BUCKET_NAME=nombre_de_la_carpeta
```

Estas credenciales son únicamente un ejemplo para desarrollo local.

En un entorno real deben utilizarse credenciales seguras y gestionadas mediante variables de entorno o un sistema de gestión de secretos.

---

## Ejecución con Docker Compose

La forma recomendada de ejecutar el proyecto localmente es mediante Docker Compose.

Primero crear el archivo ".env" utilizando las variables indicadas anteriormente.

Después ejecutar:

```bash
docker compose up --build
```

Docker Compose levantará los siguientes servicios:
```
file-storage-app
file-storage-postgres
file-storage-minio
```

La aplicación estará disponible en:
```
http://localhost:<tu_server_port>
```

PostgreSQL estará disponible desde el host mediante el puerto configurado en POSTGRES_PORT.

MinIO utilizará:
```
http://localhost:9000
```
y su consola web:
```
http://localhost:9001
```
Las credenciales de acceso a MinIO serán las configuradas mediante:

```
MINIO_ROOT_USER
MINIO_ROOT_PASSWORD
```

El bucket configurado mediante MINIO_BUCKET_NAME será creado automáticamente por la aplicación si todavía no existe.

---

## API Documentation

La API está documentada utilizando OpenAPI y Scalar.

Una vez iniciada la aplicación, la documentación interactiva puede utilizarse para explorar y probar los endpoints disponibles.

La API utiliza el prefijo:

```
/api/v1/files
```

Los principales endpoints son:
```
POST   /api/v1/files
GET    /api/v1/files/{id}
GET    /api/v1/files/{id}/download
DELETE /api/v1/files/{id}
```

La documentación generada por OpenAPI permite consultar los parámetros, respuestas y modelos utilizados por cada endpoint.

---

## Endpoints

Upload file

```
POST /api/v1/files
```

Permite subir un archivo al sistema.

El archivo debe enviarse como multipart/form-data utilizando el parámetro:

```
file
```

Ejemplo conceptual:

```
POST /api/v1/files

Content-Type: multipart/form-data
file: example.pdf
```

Respuesta exitosa:

```JSON
{
  "success": true,
  "message": "File uploaded successfully.",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "originalName": "example.pdf",
    "contentType": "application/pdf",
    "size": 102400,
    "createdAt": "2026-08-19T12:00:00Z"
  }
}
```

---

## Get file metadata

```
GET /api/v1/files/{id}
```

Obtiene los metadatos asociados a un archivo.

Ejemplo:

```
GET /api/v1/files/550e8400-e29b-41d4-a716-446655440000
```

Respuesta:

```JSON
{
  "success": true,
  "message": "File metadata retrieved successfully.",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "originalName": "example.pdf",
    "contentType": "application/pdf",
    "size": 102400,
    "createdAt": "2026-08-19T12:00:00Z"
  }
}
```

## Download file

```
GET /api/v1/files/{id}/download
```

Descarga el archivo almacenado.

La respuesta incluye:

+ Nombre original del archivo.
+ Content-Type.
+ Tamaño del archivo.

Durante la descarga se verifica la integridad del archivo mediante SHA-256.

Si el contenido almacenado no coincide con el hash registrado durante la subida, la descarga falla con un error de integridad.

## Delete file

```
DELETE /api/v1/files/{id}
```

Elimina un archivo.

La operación elimina:

+ Los metadatos almacenados en PostgreSQL.
+ El objeto correspondiente almacenado en MinIO.

Respuesta:

```JSON
{
  "success": true,
  "message": "File deleted successfully.",
  "data": null
}
```

---

## File validation

La aplicación incorpora varias capas de validación antes de almacenar un archivo.

### File name validation:

El nombre del archivo es validado para evitar nombres potencialmente peligrosos.

Se rechazan:

+ Nombres vacíos.
+ Nombres superiores a 255 caracteres.
+ ..
+ Separadores /.
+ Separadores \.
+ Null characters.
+ Caracteres de control.

Esto evita que el nombre original pueda utilizarse para realizar ataques de path traversal u otras manipulaciones del sistema de archivos.

### File size:

El tamaño máximo permitido es:

+ 10 MB

Este límite está configurado tanto a nivel de Spring Multipart como en la validación de contenido.

Spring Multipart utiliza:

```java
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### Content type validation

El tipo de contenido del archivo no se obtiene únicamente de la información enviada por el cliente.

La aplicación utiliza Apache Tika para analizar el contenido real del archivo.

Actualmente se permiten:

+ application/pdf
+ image/jpeg
+ image/png
+ image/gif
+ image/webp
+ text/plain

Esto permite detectar archivos cuyo contenido no coincide con el tipo declarado por el cliente.

### Extension validation

La extensión también debe coincidir con el contenido detectado.

Por ejemplo:

+ application/pdf → .pdf
+ image/jpeg      → .jpg / .jpeg
+ image/png       → .png
+ image/gif       → .gif
+ image/webp      → .webp
+ text/plain      → .txt

De esta forma, un archivo cuyo contenido real sea un PDF pero cuyo nombre sea:

+ image.jpg

será rechazado.

### File Integrity

El proyecto incorpora un mecanismo de integridad basado en SHA-256.

Durante la subida:

```
Archivo -> SHA-256 -> Hash almacenado en PostgreSQL
```

El hash se almacena junto con los metadatos del archivo.

Durante una descarga:

```
MinIO -> Archivo -> FileIntegrityInputStream -> SHA-256 -> Comparación con hash almacenado
```

Si ambos hashes coinciden, el archivo se considera íntegro.

Si no coinciden, se lanza una 'FileStorageException'.

Este mecanismo permite detectar modificaciones o corrupción del contenido almacenado.

---

## Storage

La aplicación utiliza MinIO como sistema de almacenamiento de objetos.

Los archivos no se almacenan directamente dentro de PostgreSQL.

PostgreSQL almacena únicamente los metadatos necesarios, mientras que el contenido binario se mantiene en MinIO.

La entidad contiene información como:

```
id
originalName
objectName
contentType
size
bucketName
sha256
createdAt
```

Esto permite separar:

```
Metadata -> PostgreSQL

Binary content -> MinIO
```

---

## Object naming

Los archivos almacenados en MinIO no utilizan directamente el nombre original proporcionado por el usuario.

Se genera un identificador UUID para cada archivo.

Ejemplo:

```
550e8400-e29b-41d4-a716-446655440000.pdf
```

De esta forma se evita utilizar nombres proporcionados directamente por el usuario como identificadores de objetos dentro del almacenamiento.

El nombre original solamente se conserva como metadato.

---

## Transaction and rollback handling

La subida de archivos implica dos sistemas diferentes:

+ PostgreSQL
+ MinIO

Por este motivo, una transacción de base de datos por sí sola no puede garantizar la consistencia entre ambos sistemas.

El servicio implementa un mecanismo de rollback para el siguiente caso:

1. Validar archivo
2. Calcular SHA-256
3. Subir archivo a MinIO
4. Guardar metadatos en PostgreSQL

Si el paso 4 falla después de haber almacenado correctamente el archivo en MinIO, la aplicación intenta eliminar el objeto creado.

De esta manera se evita dejar archivos huérfanos en el almacenamiento.

---

## API Response

Las respuestas de la API utilizan una estructura común mediante ApiResponse.

Respuesta exitosa:

```JSON
{
  "success": true,
  "message": "Operation completed successfully.",
  "data": {}
}
```

Respuesta de error:

```JSON
{
  "success": false,
  "message": "File not found with id: ...",
  "data": null
}
```

Esto proporciona una estructura consistente para las operaciones que devuelven respuestas JSON.

El endpoint de descarga es una excepción, ya que devuelve directamente el contenido binario del archivo.

---

## Error Handling

La aplicación utiliza un GlobalExceptionHandler basado en @RestControllerAdvice.

Entre las excepciones gestionadas se encuentran:

```
ResourceNotFoundException
BadRequestException
FileStorageException
MaxUploadSizeExceededException
MethodArgumentNotValidException
ConstraintViolationException
Exception
```

Algunos códigos HTTP utilizados:

```
200 OK
201 Created
400 Bad Request
404 Not Found
413 Content Too Large
500 Internal Server Error
```

Los errores internos no exponen información sensible de la excepción al cliente.

---

## Project Structure

La estructura principal del proyecto es:

```
src
└── main
    └── java
        └── com.file_storage
            │
            ├── common
            │   ├── exception
            │   │   ├── BadRequestException
            │   │   ├── FileStorageException
            │   │   ├── GlobalExceptionHandler
            │   │   └── ResourceNotFoundException
            │   │
            │   └── response
            │       └── ApiResponse
            │
            ├── config
            │   ├── MinioConfig
            │   └── OpenApiConfig
            │
            ├── file
            │   ├── controller
            │   │   └── FileController
            │   │
            │   ├── dto
            │   │   ├── FileMetadataResponse
            │   │   └── FileResponse
            │   │
            │   ├── entity
            │   │   └── FileMetadataEntity
            │   │
            │   ├── mapper
            │   │   └── FileMapper
            │   │
            │   ├── repository
            │   │   └── FileMetadataRepository
            │   │
            │   ├── service
            │   │   ├── FileService
            │   │   └── FileServiceImpl
            │   │
            │   └── validation
            │       ├── FileContentValidator
            │       └── FileNameValidator
            │
            ├── security
            │   ├── FileHashService
            │   └── FileIntegrityInputStream
            │
            ├── storage
            │   ├── StorageService
            │   └── minio
            │       └── MinioStorageService
            │
            └── FileStorageApplication
```

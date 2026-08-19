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

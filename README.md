# monitor-gen-resumen

Microservicio de monitorización que consume ambos topics de Kafka (ubicaciones y horarios), persiste los datos en Oracle Autonomous Database, genera resúmenes diarios agrupados por vehículo cada 5 minutos y los exporta como archivos JSON consultables vía API REST.

## Tecnologías

- Java 21
- Spring Boot 3.3.1
- Spring Kafka
- Spring Data JPA (Hibernate)
- Oracle Autonomous Database (OCI)
- HikariCP
- Jackson (serialización JSON)
- Docker (imagen multi-plataforma: linux/amd64, linux/arm64)

## Puerto

```
8083
```

## Topics Kafka consumidos

| Topic | Consumer Group | Listener ID |
|-------|----------------|-------------|
| `ubicaciones_vehiculos` | `monitor-ubicaciones-group` | `monitorUbicacionListener` |
| `horarios` | `monitor-horarios-group` | `monitorHorarioListener` |

Ambos con ACK manual (`MANUAL_IMMEDIATE`), `auto.offset.reset=earliest` y **StringDeserializer** para valores (parseo manual con Jackson).

## Conexión a Oracle

| Parámetro | Valor |
|-----------|-------|
| **Base de datos** | Oracle Autonomous Database (OCI) |
| **Región** | sa-santiago-1 (Santiago, Chile) |
| **TNS Alias** | `bdbusesred_medium` |
| **Host** | `adb.sa-santiago-1.oraclecloud.com:1522` (TCPS/SSL) |
| **Driver** | `oracle.jdbc.OracleDriver` |
| **Dialect** | `org.hibernate.dialect.OracleDialect` |
| **URL JDBC** | `jdbc:oracle:thin:@bdbusesred_medium?TNS_ADMIN=/wallet` |
| **Autenticación** | Oracle Wallet (`Wallet_BDBUSESRED/`) |
| **Usuario** | `ADMIN` |
| **DDL** | `hibernate.ddl-auto=update` |

### Pool de conexiones (HikariCP)

| Parámetro | Valor |
|-----------|-------|
| Mínimo idle | 2 |
| Máximo pool | 5 |
| Connection timeout | 30 segundos |
| Idle timeout | 10 minutos |
| Max lifetime | 30 minutos |

## Tablas Oracle

### `UBICACIONES_VEHICULOS`

Secuencia: `SEQ_UBICACIONES_VEHICULOS`

| Columna | Tipo Oracle | Nullable |
|---------|-------------|----------|
| `ID` | NUMBER(19) — PK | NO |
| `VEHICULO_ID` | VARCHAR2(50) | NO |
| `LATITUD` | NUMBER(10,7) | NO |
| `LONGITUD` | NUMBER(10,7) | NO |
| `RUTA` | VARCHAR2(100) | SÍ |
| `FECHA_REGISTRO` | TIMESTAMP | NO |

Índices: `IDX_UBI_VEHICULO_ID`, `IDX_UBI_FECHA_REGISTRO`, `IDX_UBI_RUTA`

### `HORARIOS_VEHICULOS`

Secuencia: `SEQ_HORARIOS_VEHICULOS`

| Columna | Tipo Oracle | Nullable |
|---------|-------------|----------|
| `ID` | NUMBER(19) — PK | NO |
| `VEHICULO_ID` | VARCHAR2(50) | NO |
| `RUTA` | VARCHAR2(100) | SÍ |
| `PARADA_NOMBRE` | VARCHAR2(200) | SÍ |
| `HORA_LLEGADA` | VARCHAR2(50) | SÍ |
| `HORA_ESTIMADA_SALIDA` | VARCHAR2(50) | SÍ |
| `LATITUD` | NUMBER(10,7) | SÍ |
| `LONGITUD` | NUMBER(10,7) | SÍ |
| `FECHA_REGISTRO` | TIMESTAMP | NO |

Índices: `IDX_HOR_VEHICULO_ID`, `IDX_HOR_FECHA_REGISTRO`, `IDX_HOR_RUTA`

### `RESUMEN_DIARIO`

Secuencia: `SEQ_RESUMEN_DIARIO`

| Columna | Tipo Oracle | Nullable |
|---------|-------------|----------|
| `ID` | NUMBER(19) — PK | NO |
| `VEHICULO_ID` | VARCHAR2(50) | NO |
| `RUTA` | VARCHAR2(100) | SÍ |
| `TOTAL_UBICACIONES` | NUMBER(10) | SÍ (default 0) |
| `TOTAL_PARADAS_VISITADAS` | NUMBER(10) | SÍ (default 0) |
| `PARADAS_VISITADAS` | VARCHAR2(2000) | SÍ |
| `HORARIOS_REGISTRADOS` | VARCHAR2(2000) | SÍ |
| `FECHA_RESUMEN` | DATE | NO |
| `FECHA_GENERACION` | TIMESTAMP | NO |

Índices: `IDX_RES_VEHICULO_ID`, `IDX_RES_FECHA_RESUMEN`, `IDX_RES_RUTA`

## Generación automática de resúmenes

- **CRON:** `0 */5 * * * *` (cada 5 minutos)
- Agrupa ubicaciones y horarios del día por `vehiculoId`
- Calcula totales por vehículo y persiste en `RESUMEN_DIARIO`
- Exporta un archivo JSON en el directorio configurable (`resumenes-json/`)

## Tiempos CRON configurados (todos los microservicios)

| Microservicio | Acción | Expresión CRON | Frecuencia |
|---------------|--------|----------------|------------|
| `prod-ub-cron-kafka` | Envía 2 ubicaciones simuladas a Kafka | `0 */1 * * * *` | Cada 1 minuto |
| `cons-ub-prod-hor-kafka` | Consume ubicaciones y produce horarios | Tiempo real (listener) | Inmediato al recibir |
| `monitor-gen-resumen` | Consume ubicaciones y horarios, guarda en Oracle | Tiempo real (listener) | Inmediato al recibir |
| `monitor-gen-resumen` | Genera resumen diario y exporta JSON | `0 */5 * * * *` | Cada 5 minutos |

## Cómo consultar los resúmenes

### 1. Listar documentos JSON disponibles

```bash
curl http://localhost:8083/api/resumenes/documentos
```

Respuesta ejemplo:
```json
["resumen_2026-03-02.json", "resumen_2026-03-01.json"]
```

### 2. Ver el contenido de un documento JSON

```bash
curl http://localhost:8083/api/resumenes/documentos/resumen_2026-03-02.json
```

### 3. Consultar resúmenes desde la base de datos

```bash
# Todos los resúmenes
curl http://localhost:8083/api/resumenes

# Por fecha específica (formato yyyy-MM-dd)
curl http://localhost:8083/api/resumenes/fecha/2026-03-02

# Por ID
curl http://localhost:8083/api/resumenes/1
```

### 4. Generar un resumen manualmente (sin esperar el CRON)

```bash
curl -X POST http://localhost:8083/api/resumenes/generar-json
```

> **Nota:** En despliegue EC2, reemplazar `localhost` por la IP pública de la instancia (ej: `54.225.56.236`).

## Flujo completo del sistema

```
[prod-ub-cron-kafka]          [cons-ub-prod-hor-kafka]          [monitor-gen-resumen]
       |                              |                                |
  CRON cada 1 min                     |                                |
  Genera 2 ubicaciones                |                                |
       |                              |                                |
       +---> topic: ubicaciones_vehiculos --->  Consume ubicación      |
       |                              |         Genera horario         |
       |                              |              |                 |
       |                              +---> topic: horarios            |
       |                                                               |
       +---> topic: ubicaciones_vehiculos ----------------------> Consume y guarda en Oracle
                                      +---> topic: horarios -----> Consume y guarda en Oracle
                                                                       |
                                                                  CRON cada 5 min
                                                                  Genera resumen diario
                                                                  Exporta JSON
```

## Endpoints REST

### Ubicaciones — `/api/ubicaciones`

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/ubicaciones` | Todas las ubicaciones |
| `GET` | `/api/ubicaciones/{id}` | Por ID |
| `GET` | `/api/ubicaciones/vehiculo/{vehiculoId}` | Por vehículo |
| `GET` | `/api/ubicaciones/ruta/{ruta}` | Por ruta |
| `POST` | `/api/ubicaciones` | Crear manualmente |
| `PUT` | `/api/ubicaciones/{id}` | Actualizar |
| `DELETE` | `/api/ubicaciones/{id}` | Eliminar |

### Horarios — `/api/horarios`

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/horarios` | Todos los horarios |
| `GET` | `/api/horarios/{id}` | Por ID |
| `GET` | `/api/horarios/vehiculo/{vehiculoId}` | Por vehículo |
| `GET` | `/api/horarios/ruta/{ruta}` | Por ruta |
| `POST` | `/api/horarios` | Crear manualmente |
| `PUT` | `/api/horarios/{id}` | Actualizar |
| `DELETE` | `/api/horarios/{id}` | Eliminar |

### Resúmenes — `/api/resumenes`

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/resumenes` | Todos los resúmenes |
| `GET` | `/api/resumenes/{id}` | Por ID |
| `GET` | `/api/resumenes/fecha/{fecha}` | Por fecha (yyyy-MM-dd) |
| `GET` | `/api/resumenes/documentos` | Lista archivos JSON disponibles |
| `GET` | `/api/resumenes/documentos/{nombre}` | Contenido de un documento JSON |
| `POST` | `/api/resumenes/generar-json` | Generación manual de resumen |

## Ejecución

> **IMPORTANTE:** Este microservicio requiere la contraseña de Oracle Autonomous Database.
> La variable `ORACLE_DB_PASSWORD` **debe estar definida** antes de levantar el contenedor.
> Si no se proporciona, el servicio fallará con `ORA-01017: invalid username/password`.

### Con el script `start-all.sh` (recomendado)

El script acepta la contraseña como argumento:

```bash
./start-all.sh tu_password_oracle
```

O puedes exportar la variable antes de ejecutar:

```bash
export ORACLE_DB_PASSWORD="tu_password_oracle"
./start-all.sh
```

### Con Docker Compose

```bash
export ORACLE_DB_PASSWORD="tu_password_oracle"
docker compose up -d monitor-gen-resumen
```

O en una sola línea:

```bash
ORACLE_DB_PASSWORD="tu_password_oracle" docker compose up -d monitor-gen-resumen
```

### Con Docker directamente

```bash
docker build -t monitor-gen-resumen .
docker run -p 8083:8083 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-1:9092 \
  -e SPRING_DATASOURCE_URL=jdbc:oracle:thin:@bdbusesred_medium?TNS_ADMIN=/wallet \
  -e SPRING_DATASOURCE_USERNAME=ADMIN \
  -e SPRING_DATASOURCE_PASSWORD=tu_password \
  -v ./Wallet_BDBUSESRED:/wallet \
  --network kafka-net \
  monitor-gen-resumen
```

### Imagen Docker Hub

```bash
docker pull dimmox/monitor-gen-resumen:latest
```

### Local con Maven

```bash
./mvnw spring-boot:run
```

Requiere Kafka en `localhost:29092` y wallet configurado localmente.

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Brokers Kafka | `kafka-1:9092` |
| `SPRING_DATASOURCE_URL` | URL JDBC Oracle | `jdbc:oracle:thin:@bdbusesred_medium?TNS_ADMIN=/wallet` |
| `SPRING_DATASOURCE_USERNAME` | Usuario Oracle | `ADMIN` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña Oracle | — (requerida) |
| `ORACLE_WALLET_LOCATION` | Ruta al wallet dentro del contenedor | `/wallet` |
| `SERVER_PORT` | Puerto del servicio | `8083` |

# monitor-gen-resumen

Microservicio de monitorización que consume ambos topics de Kafka (ubicaciones y horarios), persiste los datos en Oracle Autonomous Database, genera resúmenes diarios agrupados por vehículo cada 2 minutos y los exporta como archivos JSON consultables vía API REST.

## Tecnologías

- Java 21
- Spring Boot 3.3.1
- Spring Kafka
- Spring Data JPA (Hibernate)
- Oracle Autonomous Database (OCI)
- HikariCP
- Jackson (serialización JSON)
- Docker

## Puerto

```
8083
```

## Topics Kafka consumidos

| Topic | Consumer Group | Listener ID |
|-------|----------------|-------------|
| `ubicaciones_vehiculos` | `monitor-ubicaciones-group` | `monitorUbicacionListener` |
| `horarios` | `monitor-horarios-group` | `monitorHorarioListener` |

Ambos con ACK manual (`MANUAL_IMMEDIATE`) y `auto.offset.reset=earliest`.

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

- **CRON:** `0 */2 * * * *` (cada 2 minutos)
- Agrupa ubicaciones y horarios del día por `vehiculoId`
- Calcula totales por vehículo y persiste en `RESUMEN_DIARIO`
- Exporta un archivo JSON en el directorio configurable (`resumenes-json/`)

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

### Con Docker Compose (recomendado)

Desde el directorio raíz del proyecto:

```bash
export ORACLE_DB_PASSWORD="tu_password"
docker compose build monitor-gen-resumen
docker compose up -d monitor-gen-resumen
```

### Con Docker directamente

```bash
docker build -t monitor-gen-resumen .
docker run -p 8083:8083 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-1:9092,kafka-2:9092,kafka-3:9092 \
  -e SPRING_DATASOURCE_URL=jdbc:oracle:thin:@bdbusesred_medium?TNS_ADMIN=/wallet \
  -e SPRING_DATASOURCE_USERNAME=ADMIN \
  -e SPRING_DATASOURCE_PASSWORD=tu_password \
  -v ./Wallet_BDBUSESRED:/wallet \
  --network kafka-net \
  monitor-gen-resumen
```

### Local con Maven

```bash
./mvnw spring-boot:run
```

Requiere Kafka en `localhost:29092,localhost:39092,localhost:49092` y wallet configurado localmente.

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Brokers Kafka | `localhost:29092,localhost:39092,localhost:49092` |
| `SPRING_DATASOURCE_URL` | URL JDBC Oracle | `jdbc:oracle:thin:@bdbusesred_medium?TNS_ADMIN=/wallet` |
| `SPRING_DATASOURCE_USERNAME` | Usuario Oracle | `ADMIN` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña Oracle | — (requerida) |
| `ORACLE_WALLET_LOCATION` | Ruta al wallet dentro del contenedor | `/wallet` |
| `SERVER_PORT` | Puerto del servicio | `8083` |

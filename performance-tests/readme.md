# Laboratorio 07 – Pruebas de Rendimiento con Apache JMeter

## 1. Descripción

En este laboratorio se realizaron pruebas de rendimiento sobre el módulo de carrito de compras del sistema **E-Shop**, utilizando Apache JMeter.

El objetivo fue evaluar los tiempos de respuesta y el comportamiento de los endpoints del carrito al incrementar la cantidad de usuarios virtuales. También se realizaron pruebas de carga mixta para simular diferentes operaciones dentro de un mismo flujo de compra.

Se evaluaron las operaciones de consulta, agregado, modificación y eliminación de productos.

## 2. Herramientas utilizadas

- **Apache JMeter:** creación y ejecución de pruebas de rendimiento.
- **Java 21:** entorno de ejecución.
- **Spring Boot:** backend de E-Shop.
- **Angular:** frontend de la aplicación.
- **PostgreSQL:** base de datos.
- **Docker Compose:** ejecución local de los servicios.
- **PowerShell:** automatización de la preparación de datos y ejecución de pruebas.

Las pruebas se ejecutaron sobre el entorno local del proyecto, utilizando la dirección `http://localhost`.

## 3. Escenarios de prueba

Se implementaron los siguientes casos de prueba:

| ID | Escenario | Método | Endpoint | Tiempo aceptable |
|---|---|---|---|---|
| TC01 | Consultar carrito | GET | `/api/cart` | 1000 ms |
| TC02 | Agregar producto | POST | `/api/cart/add` | 3000 ms |
| TC03 | Modificar cantidad | PUT | `/api/cart/B0001` | 3000 ms |
| TC04 | Eliminar producto | DELETE | `/api/cart/B0001` | 3000 ms |

Las operaciones requieren autenticación mediante JWT. Por ello, los planes incluyen previamente una solicitud POST al endpoint `/api/login`.

El token se obtiene utilizando un JSON Extractor y posteriormente se incorpora en el encabezado `Authorization` de cada solicitud protegida.

## 4. Preparación del entorno

Antes de ejecutar las pruebas, se inició el sistema mediante Docker Compose y se comprobó el acceso al backend.

Para las pruebas concurrentes se prepararon **100 cuentas CUSTOMER de prueba independientes**, creadas mediante el endpoint `/api/register`.

Esto permitió asignar un carrito diferente a cada usuario virtual y reducir las interferencias entre operaciones realizadas por distintos usuarios.

Las credenciales se gestionaron mediante un archivo CSV local y el componente **CSV Data Set Config** de JMeter.

Por seguridad, el CSV de credenciales no se incluye en el repositorio.

## 5. Planes de prueba

### 5.1. Prueba preliminar

Se verificó individualmente el funcionamiento de los endpoints GET, POST, PUT y DELETE, así como la autenticación y las aserciones de respuesta.

### 5.2. Prueba de lectura

Se realizaron ejecuciones incrementales con 10, 25 y 100 usuarios virtuales.

La ejecución principal utilizó:

- Usuarios virtuales: 100.
- Ramp-up: 30 segundos.
- Iteraciones: 1.
- Operaciones: Login y GET del carrito.

### 5.3. Prueba de escritura secuencial

Se ejecutaron diez ciclos consecutivos utilizando un solo usuario.

Cada ciclo realizó la siguiente secuencia:

1. Login.
2. Agregar un producto.
3. Modificar su cantidad.
4. Eliminar el producto.

Se verificó que el producto quedara eliminado del carrito al finalizar.

### 5.4. Prueba de escritura concurrente

Se ejecutaron pruebas con múltiples usuarios virtuales, empleando cuentas independientes para evitar que todos modificaran el mismo carrito.

La ejecución principal utilizó:

- Usuarios virtuales: 100.
- Ramp-up: 20 segundos.
- Iteraciones: 1.
- Operaciones: Login, POST, PUT y DELETE.

### 5.5. Prueba de carga mixta

Se diseñó un flujo que combina consultas y modificaciones del carrito.

Cada usuario virtual ejecutó:

1. Login.
2. GET – Consultar carrito.
3. POST – Agregar producto.
4. GET – Consultar carrito nuevamente.
5. PUT – Modificar cantidad.
6. DELETE – Eliminar producto.

La distribución de las operaciones del carrito fue:

| Operación | Porcentaje de carga |
|---|---|
| GET | 40 % |
| POST | 20 % |
| PUT | 20 % |
| DELETE | 20 % |
| **Total** | **100 %** |

La autenticación no se incluyó en el cálculo porcentual, ya que constituye una solicitud previa necesaria para acceder al módulo.

**Configuración principal:**

| Parámetro | Valor |
|---|---|
| Usuarios virtuales | 100 |
| Ramp-up | 20 segundos |
| Iteraciones | 1 |
| Operaciones del carrito | 500 |
| Solicitudes de Login | 100 |
| Muestras totales esperadas | 600 |

## 6. Resultados obtenidos

### 6.1. Lectura con 100 usuarios

| Operación | Muestras | Tiempo promedio | P95 | Errores |
|---|---:|---:|---:|---:|
| Login | 100 | 86.27 ms | 118.90 ms | 0 % |
| GET – Consultar carrito | 100 | 6.39 ms | 8.00 ms | 0 % |

Se registraron 200 muestras sin errores. La consulta del carrito mantuvo tiempos de respuesta bajos durante esta ejecución.

### 6.2. Escritura concurrente con 100 usuarios

| Operación | Muestras | Promedio | P95 | Errores |
|---|---:|---:|---:|---:|
| Login | 100 | 70.97 ms | 102.45 ms | 0 % |
| POST – Agregar | 100 | 9.71 ms | 19.80 ms | 0 % |
| PUT – Modificar | 100 | 8.72 ms | 16.95 ms | 0 % |
| DELETE – Eliminar | 100 | 10.14 ms | 36.90 ms | 0 % |
| **Total** | **400** | **24.88 ms** | **77.80 ms** | **0 %** |

Las 400 muestras finalizaron sin errores registrados y los tiempos observados cumplieron los umbrales establecidos.

### 6.3. Carga mixta con 100 usuarios

| Operación | Muestras | Promedio | P95 | Máximo | Errores |
|---|---:|---:|---:|---:|---:|
| Login | 100 | 80.41 ms | 113.95 ms | 190 ms | 0 % |
| GET – Consultar | 200 | 6.32 ms | 13.00 ms | 71 ms | 0 % |
| POST – Agregar | 100 | 9.50 ms | 20.95 ms | 30 ms | 0 % |
| PUT – Modificar | 100 | 10.27 ms | 20.95 ms | 26 ms | 0 % |
| DELETE – Eliminar | 100 | 10.61 ms | 21.95 ms | 30 ms | 0 % |
| **Total** | **600** | **20.57 ms** | **90.00 ms** | **190 ms** | **0 %** |

El throughput total registrado fue de **29 solicitudes por segundo**.

Durante esta ejecución, el 100 % de las muestras cumplió las aserciones configuradas.

Al finalizar, también se comprobó que el producto utilizado en las pruebas había quedado eliminado de los carritos revisados.

## 7. Ejecución de las pruebas

Para reproducir las pruebas se requiere:

- Tener los servicios de E-Shop ejecutándose.
- Contar con Apache JMeter instalado.
- Preparar cuentas CUSTOMER válidas para el entorno local.
- Configurar el archivo `usuarios-carga.csv` con las columnas `username,password`.
- Verificar que el CSV Data Set Config apunte a ese archivo.

Los planes pueden ejecutarse mediante la interfaz de JMeter o desde la línea de comandos.

Ejemplo de ejecución en modo no gráfico:

```powershell
jmeter -n `
  -t carrito-carga-mixta.jmx `
  -l resultados-mixta.jtl `
  -e `
  -o reporte-mixta
```

Los nombres de salida deben estar disponibles antes de ejecutar el comando. El directorio del dashboard debe estar vacío o no existir.

Los resultados se pueden consultar abriendo `index.html` dentro de la carpeta del reporte generado.

## 8. Criterios de evaluación

Para cada escenario se utilizaron aserciones del código HTTP esperado y del tiempo máximo de respuesta.

Se analizaron las siguientes métricas:

- **Tiempo promedio:** duración media de las solicitudes.
- **Tiempo máximo:** mayor tiempo observado.
- **Percentil 95 (P95):** valor por debajo del cual se encuentra aproximadamente el 95 % de las muestras.
- **Throughput:** cantidad de solicitudes procesadas por segundo.
- **Porcentaje de errores:** proporción de muestras que no cumplieron los criterios definidos.

Los resultados corresponden a ejecuciones locales con usuarios virtuales iniciados progresivamente. No representan necesariamente solicitudes exactamente simultáneas ni garantizan el mismo rendimiento en producción.

## 9. Conclusiones

Las pruebas permitieron comprobar el comportamiento del módulo de carrito de E-Shop ante distintos escenarios de carga.

En la prueba de lectura con 100 usuarios virtuales, el endpoint GET presentó un tiempo promedio de 6.39 ms y no registró errores.

Durante la prueba de escritura concurrente, las operaciones POST, PUT y DELETE mantuvieron tiempos promedio inferiores a 11 ms, sin errores registrados.

Finalmente, en la prueba mixta de 100 usuarios se ejecutaron 600 solicitudes, con un tiempo promedio global de 20.57 ms, un throughput de 29 solicitudes por segundo y una tasa de errores del 0 %.

Los resultados muestran que el módulo respondió satisfactoriamente a las cargas evaluadas en el entorno local. Sin embargo, sería necesario realizar pruebas adicionales de carga sostenida, mayor concurrencia y validación de estados para determinar su capacidad máxima y su comportamiento en producción.

## 10. Consideraciones de seguridad

Los archivos que contienen contraseñas y datos de autenticación deben mantenerse fuera del repositorio.

Los reportes y planes de prueba deben revisarse antes de publicarse para evitar la exposición de credenciales, tokens JWT o información sensible.

Las pruebas se diseñaron exclusivamente para el entorno local autorizado del proyecto.
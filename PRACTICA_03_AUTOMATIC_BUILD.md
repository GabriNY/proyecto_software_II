# PRÁCTICA 03 – CONSTRUCCIÓN AUTOMÁTICA

**Curso:** Ingeniería de Software II  
**Tema:** Construcción automática  
**Proyecto seleccionado:** Online Shop Application  
**Componente utilizado:** Backend Java / Spring Boot  
**Herramienta de construcción:** Apache Maven mediante Maven Wrapper (`mvnw` / `mvnw.cmd`)  
**Archivo de configuración:** `backend/pom.xml`

## 1. Objetivo

Demostrar la construcción automática del backend del proyecto mediante un modelo de construcción Maven. El archivo `pom.xml` centraliza la información del proyecto, las opciones de compilación, las dependencias, los plugins, la ejecución de pruebas y el empaquetado.

## 2. Requisitos previos

- JDK 21 instalado.
- Conexión a Internet en la primera ejecución para que Maven descargue su distribución y dependencias.
- Git instalado.
- IDE recomendado: IntelliJ IDEA, Visual Studio Code o Eclipse.
- No es obligatorio instalar Maven globalmente porque el proyecto incluye Maven Wrapper.

## 3. Estructura utilizada

```text
shop-modern/
├── backend/
│   ├── .mvn/wrapper/
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   └── src/
│       ├── main/java/
│       ├── main/resources/
│       └── test/java/
├── .github/workflows/practica03-maven.yml
├── scripts/practica03-build.ps1
├── scripts/practica03-build.bat
└── PRACTICA_03_AUTOMATIC_BUILD.md
```

## 4. Modelo de construcción: `pom.xml`

### 4.1 Información del proyecto

El `pom.xml` declara:

- `groupId`: `me.zhulin`
- `artifactId`: `shop-api`
- `version`: `0.0.1-SNAPSHOT`
- `name`: `shop-api`
- `description`: `Online Shopping API Server`

### 4.2 Opciones de compilación

- Java: 21.
- Codificación: UTF-8.
- Spring Boot Parent: 4.1.1.
- Compilación gestionada por `maven-compiler-plugin`.

### 4.3 Gestión de dependencias

Entre las dependencias principales están:

- Spring Boot Starter Data JPA.
- Spring Boot Starter Web.
- Spring Boot Starter Security.
- Spring Boot Starter Validation.
- PostgreSQL Driver.
- Lombok.
- JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`).
- Spring Boot Starter Test.
- Spring Security Test.

### 4.4 Plugins de construcción

- `spring-boot-maven-plugin`: empaqueta la aplicación Spring Boot.
- `maven-dependency-plugin`: resuelve propiedades de dependencias necesarias para pruebas.
- `maven-surefire-plugin`: ejecuta pruebas y configura el agente de Mockito.
- `maven-compiler-plugin`: controla la compilación y el procesamiento de anotaciones de Lombok.

## 5. Paso a paso de ejecución en Windows

Abrir PowerShell o CMD en la raíz del proyecto.

### Paso 1 – Verificar Java

```powershell
java -version
```

Debe mostrarse Java 21 o superior compatible con el proyecto.

### Paso 2 – Entrar al backend

```powershell
cd backend
```

### Paso 3 – Verificar Maven Wrapper

```powershell
.\mvnw.cmd -v
```

En la primera ejecución puede descargar Maven 3.9.16.

### Paso 4 – Limpiar y compilar

```powershell
.\mvnw.cmd clean compile
```

Resultado esperado: finalización con `BUILD SUCCESS`.

### Paso 5 – Ejecutar pruebas automatizadas

```powershell
.\mvnw.cmd test
```

Maven ejecutará las pruebas existentes en `src/test/java`.

### Paso 6 – Empaquetar el proyecto

```powershell
.\mvnw.cmd clean package
```

Maven realiza las fases de limpieza, compilación, pruebas y empaquetado. Si termina correctamente aparecerá `BUILD SUCCESS` y se generará el artefacto en `backend/target/`.

### Paso 7 – Revisar el resultado

```powershell
dir target\*.jar
```

Debe existir un archivo similar a:

```text
shop-api-0.0.1-SNAPSHOT.jar
```

## 6. Ejecución automática con el script incluido

Desde la raíz del proyecto:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\practica03-build.ps1
```

O ejecutar:

```text
scripts\practica03-build.bat
```

El script:

1. Verifica Java.
2. Verifica Maven Wrapper.
3. Ejecuta `clean compile`.
4. Ejecuta `test`.
5. Ejecuta `clean package`.
6. Lista el JAR generado.
7. Guarda la evidencia textual en `evidencias/practica03_build.log`.

## 7. Git y ramas

Antes de crear commits, verificar que no existan cambios ajenos a la práctica que se vayan a incluir accidentalmente:

```powershell
git status
```

Crear la rama:

```powershell
git switch -c practica-03-automatic-build
```

Commits sugeridos:

```powershell
git add PRACTICA_03_AUTOMATIC_BUILD.md EVIDENCIAS_PRACTICA_03.md scripts

git commit -m "docs: agregar tutorial de construccion automatica"

git add .github/workflows/practica03-maven.yml

git commit -m "ci: automatizar build Maven del backend"
```

Luego publicar la rama en el repositorio personal:

```powershell
git push -u origin practica-03-automatic-build
```

## 8. Integración continua opcional / evidencia adicional

Se incluye `.github/workflows/practica03-maven.yml`. Al subir el proyecto a GitHub, GitHub Actions ejecutará automáticamente el build del backend con Java 21 ante `push` o `pull_request` y conservará el JAR como artefacto cuando la construcción termine correctamente.

Este paso complementa la práctica y evidencia que el mismo modelo `pom.xml` puede ser utilizado tanto localmente como en un entorno de integración continua.

## 9. Evidencias que deben capturarse

1. Estructura del proyecto abierta en el IDE.
2. `java -version`.
3. `mvnw.cmd -v`.
4. `pom.xml` mostrando información del proyecto y `java.version`.
5. `pom.xml` mostrando dependencias.
6. `mvnw.cmd clean compile` con `BUILD SUCCESS`.
7. `mvnw.cmd test` con las pruebas ejecutadas.
8. `mvnw.cmd clean package` con `BUILD SUCCESS`.
9. Carpeta `target` mostrando el JAR.
10. GitHub mostrando la rama `practica-03-automatic-build` y los commits.
11. GitHub Actions mostrando la ejecución exitosa, si se usa el workflow incluido.

## 10. Explicación breve para exposición

> En esta práctica se utilizó Maven como herramienta de construcción automática para el backend Java del proyecto Online Shop. El modelo de construcción está definido en `pom.xml`, donde se describe el proyecto, la versión de Java, las dependencias y los plugins. Con Maven Wrapper se ejecutaron las fases de compilación, pruebas y empaquetado mediante comandos reproducibles. El resultado final es un archivo JAR en la carpeta `target`. Además, se preparó un workflow de GitHub Actions para reutilizar el mismo modelo de construcción en integración continua.

## 11. Conclusión

La construcción automática evita realizar manualmente la resolución de dependencias, compilación, ejecución de pruebas y empaquetado. Maven permite reproducir el proceso a partir de un único modelo declarativo (`pom.xml`), lo que mejora la consistencia del proyecto y facilita su integración con herramientas de control de versiones e integración continua.

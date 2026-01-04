# Autenticación y Autorización — Sesión

Implementación de un sistema de autenticación y autorización usando **Spring Security**, enfocada en sesiones del servidor, con control de acceso basado en roles.

---

## Objetivo de esta implementación

Esta rama demuestra:

- Cómo implementar autenticación basada en sesiones con Spring Security
- Qué problemas resuelve este enfoque tradicional
- Qué limitaciones tiene frente a la autenticación stateless con JWT

---

## Enfoque de autenticación

### Sesión (HTTP Session)

El estado de autenticación se almacena **en el servidor** y se identifica mediante un ID de sesión enviado al cliente en una cookie.

**Flujo de validación:**

- El cliente envía la cookie `JSESSIONID` automáticamente en cada petición
- Spring Security busca la sesión en el servidor usando el ID
- Si existe y es válida, recupera el `Authentication` almacenado
- El `SecurityContext` se carga con la autenticación de la sesión

**Componentes clave:**

- `SecurityFilterChain`: Configura `formLogin()` para autenticación basada en formularios
- `CustomUserDetailsService`: Carga los detalles del usuario desde la base de datos
- `SessionCreationPolicy.IF_REQUIRED`: Spring crea sesión si es necesaria (default)

**Almacenamiento de estado:**

- Servidor: Sesión HTTP con `Authentication` completo
- Cliente: Cookie `JSESSIONID` con ID de sesión

---

## Decisiones de diseño

**Por qué se eligió este enfoque:**

- **Control total sobre sesiones**: se pueden invalidar, renovar o consultar en cualquier momento
- **Simplicidad conceptual**: modelo tradicional bien entendido y documentado
- **Gestión de estado**: el servidor tiene visibilidad completa de las sesiones activas
- **Revocación inmediata**: un logout invalida la sesión en el servidor instantáneamente

**Qué se gana:**

- Revocación instantánea de acceso (logout real, cambio de permisos)
- Auditoría detallada: se puede consultar quién está conectado y desde cuándo
- Menos procesamiento por request: no hay validación criptográfica de tokens
- Menor superficie de ataque en el cliente: solo un ID de sesión opaco

**Qué se pierde:**

- Escalabilidad horizontal sin estado compartido: requiere sticky sessions o store distribuido
- Mayor complejidad en despliegues distribuidos (Redis, bases de datos para sesiones)
- Acoplamiento al servidor: la sesión vive solo en el backend que la creó
- Menor interoperabilidad entre servicios distintos

**Qué casos NO cubre bien:**

- Arquitecturas de microservicios distribuidos sin estado compartido
- APIs públicas consumidas por clientes no web (mobile apps, servicios externos)
- Escalado horizontal sin infraestructura de sesiones compartidas

> Esta implementación prioriza control y revocación inmediata sobre escalabilidad stateless.

---

## Flujo de autenticación y autorización

```text
Cliente (Navegador)
  |
  | GET /auth/login
  v
AuthController
  |
  | Muestra formulario de login (Thymeleaf)
  |
  | POST /auth/login (username, password)
  v
Spring Security Filter Chain
  |
  | UsernamePasswordAuthenticationFilter
  v
AuthenticationManager
  |
  | Valida credenciales con CustomUserDetailsService
  v
SecurityContext
  |
  | Guarda Authentication en sesión HTTP
  |
  | Crea cookie JSESSIONID
  v
Redirección a página principal (/)
  |
  | Cliente envía JSESSIONID en cookie
  v
SecurityContextPersistenceFilter
  |
  | Carga Authentication desde sesión
  v
HomeController
  |
  | Accede a @AuthenticationPrincipal
  v
Página protegida
```

---

## Roles y autorización

**Roles definidos:**

- `USER`: Usuario estándar del sistema
- `ADMIN`: Administrador con privilegios elevados

**Configuración de acceso:**

- Rutas públicas: `/auth/login`, `/css/**`, `/js/**`, `/images/**`
- Rutas protegidas: Cualquier otra requiere autenticación
- Autorización adicional por rol se puede implementar con `@PreAuthorize`

---

## Endpoints principales

| Método | Endpoint           | Autenticación | Rol  |
|--------|--------------------|---------------|------|
| GET    | /auth/login        | No            | -    |
| POST   | /auth/login        | No            | -    |
| GET    | /                  | Sí (Sesión)   | USER |
| POST   | /logout            | Sí            | USER |

**Ejemplo de acceso:**

```bash
# Login (crea sesión y devuelve cookie)
curl -X POST http://localhost:8080/auth/login \
     -d "username=usuario@example.com&password=123456" \
     -c cookies.txt

# Acceso a recurso protegido (envía cookie)
curl http://localhost:8080/ -b cookies.txt
```

---

## Tests

**Estado actual:**

- No se han implementado tests automatizados
- Se requiere testing manual desde navegador o herramientas como Postman

**Casos a cubrir:**

- Login con credenciales válidas → crea sesión y redirige
- Acceso sin sesión → redirige a /auth/login
- Logout → invalida sesión y redirige
- Acceso con sesión expirada → redirige a login

---

## Ejecución

```bash
mvn spring-boot:run
```

**Requisitos:**

- Java 21
- MySQL 8.0+
- Maven 3.6+

**Configuración:**

- Configurar credenciales de base de datos en `application.properties`
- Las sesiones se almacenan en memoria por defecto
- Para producción, considerar almacenamiento distribuido (Redis, JDBC)

**Acceso:**

- Aplicación: http://localhost:8080
- Login: http://localhost:8080/auth/login

---

## Limitaciones conocidas

- **Sesiones en memoria**: no sobreviven reinicios del servidor
- **No escala horizontalmente sin configuración adicional**: requiere sticky sessions o store compartido
- **Acoplada al navegador**: las cookies HTTP no funcionan bien con clientes móviles nativos
- **No implementa "Remember Me"**: la sesión expira según timeout configurado
- **Sin gestión de sesiones concurrentes**: no limita logins simultáneos del mismo usuario

Estas limitaciones son intencionales para mantener la implementación simple y educativa.

---

## Conclusiones

**Este enfoque es adecuado cuando:**

- La aplicación es monolítica o tiene pocos servidores con sticky sessions
- Se requiere control estricto y revocación inmediata de acceso
- Los clientes son navegadores web (no APIs públicas o mobile apps)
- Se necesita auditoría completa de sesiones activas
- La infraestructura soporta almacenamiento de sesiones compartido (Redis, base de datos)

**No es recomendable cuando:**

- Se necesita escalar horizontalmente sin estado compartido
- La arquitectura es de microservicios distribuidos
- Los clientes son aplicaciones móviles nativas o servicios externos
- Se requiere interoperabilidad entre múltiples servicios independientes
- El tráfico es altamente variable y requiere escalado dinámico

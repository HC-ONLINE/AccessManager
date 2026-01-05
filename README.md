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

**Qué habilita técnicamente la sesión:**

- **HttpSession de Spring Security**: `SecurityContextPersistenceFilter` guarda el `Authentication` en la sesión HTTP
- **Cookie JSESSIONID**: Se envía automáticamente por el navegador en cada petición
- **Estado en servidor**: La sesión (con datos del usuario autenticado) se almacena en memoria o en un store externo
- **Form Login**: `UsernamePasswordAuthenticationFilter` procesa el formulario y crea la sesión automáticamente

**Qué problemas resuelve en Spring Security:**

- **Revocación inmediata**: `session.invalidate()` elimina la sesión instantáneamente, impidiendo acceso futuro
- **Auditoría**: Se puede consultar quién está conectado, desde cuándo y desde qué IP usando `SessionRegistry`
- **Protección CSRF**: Spring Security habilita automáticamente protección CSRF con tokens sincronizados
- **Gestión centralizada**: El servidor controla cuántas sesiones tiene un usuario, timeout, renovación, etc.

**Trade-offs técnicos:**

- **Escalabilidad**: Requiere sticky sessions (afinidad de sesión) o store compartido (Redis/JDBC) para escalar
- **Memoria**: Cada usuario autenticado consume memoria en el servidor (vs JWT donde el cliente almacena el estado)
- **Acoplamiento**: La sesión vive en el servidor que la creó (o en el store compartido), dificultando arquitecturas distribuidas

> Esta implementación prioriza control inmediato sobre las sesiones y auditoría completa.

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

Tests de seguridad implementados con Spring Boot Test y MockMvc:

- Login válido → crea sesión y redirige a página principal
- Login con credenciales inválidas → redirige a login con error
- Acceso sin sesión → redirige a /auth/login
- Acceso con sesión válida → 200 OK
- Logout → invalida sesión con revocación inmediata
- Login sin CSRF token → 403 Forbidden
- Usuario inactivo → rechaza autenticación

Los tests validan la configuración de Spring Security, el ciclo de vida de las sesiones y la revocación inmediata de acceso al hacer logout.

**Ejecución:**

```bash
mvn test
```

---

## Ejecución

```bash
mvn spring-boot:run
```

**Requisitos:**

- Java 21
- Base de datos relacional (MySQL 8.0+ como ejemplo)
- Maven 3.6+

**Configuración:**

- Configurar credenciales de base de datos en `application.properties`
- Las sesiones se almacenan en memoria por defecto
- Para producción, considerar almacenamiento distribuido (Redis, JDBC)

**Nota sobre persistencia:**

La persistencia es intercambiable. MySQL se utiliza como ejemplo, pero el diseño usa Spring Data JPA y no depende de características específicas del motor. Puede usar H2, PostgreSQL, MariaDB u otra base de datos relacional compatible modificando `application.properties`.

**Acceso:**

- Aplicación: [http://localhost:8080](http://localhost:8080)
- Login: [http://localhost:8080/auth/login](http://localhost:8080/auth/login)

---

## Limitaciones conocidas

- **Sesiones en memoria**: no sobreviven reinicios del servidor
- **No escala horizontalmente sin configuración adicional**: requiere sticky sessions o store compartido
- **Acoplada al navegador**: las cookies HTTP no funcionan bien con clientes móviles nativos
- **No implementa "Remember Me"**: la sesión expira según timeout configurado
- **Sin gestión de sesiones concurrentes**: no limita logins simultáneos del mismo usuario

Estas limitaciones son intencionales para mantener la implementación simple y educativa.

---

## Aspectos técnicos clave

**Cómo funciona la revocación inmediata:**

```java
// En el logout
session.invalidate(); // Elimina la sesión del servidor
```

Cuando se invalida la sesión:

1. Se elimina el objeto `HttpSession` del servidor o store
2. La cookie `JSESSIONID` se marca para eliminación en el cliente
3. Próximas peticiones con ese ID de sesión son rechazadas automáticamente

Esto permite **revocar acceso instantáneamente**, algo imposible con JWT sin infraestructura adicional.

**Por qué CSRF es crítico aquí:**

Las cookies `JSESSIONID` se envían **automáticamente** por el navegador en cada petición, incluso desde otros sitios.

Spring Security habilita protección CSRF por defecto:

- Genera un token CSRF único por sesión
- Lo envía embebido en formularios (input hidden)
- Valida que el token en el request coincida con el de la sesión

Sin CSRF, un atacante podría:

```html
<!-- Sitio malicioso -->
<form action="https://app.com/logout" method="POST">
  <input type="submit" value="Ver video gratis">
</form>
```

El navegador enviaría automáticamente la cookie de sesión válida.

**Escalabilidad con sesiones:**

Para escalar horizontalmente con sesiones:

1. **Sticky Sessions** (afinidad de sesión):
   - El balanceador enruta siempre al mismo servidor
   - Simple pero no tolera caídas del servidor

2. **Store compartido** (Redis, JDBC):
   - Las sesiones se guardan en Redis o base de datos
   - Cualquier servidor puede leer cualquier sesión
   - Añade latencia y dependencia externa

3. **JWT** (rama `auth-jwt`):
   - Elimina el problema al no tener sesiones del lado servidor

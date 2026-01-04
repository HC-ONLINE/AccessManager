# AccessManager

Sistema de **autenticación y autorización** desarrollado en Java con Spring Boot y Spring Security.  
El proyecto implementa y compara **dos enfoques de autenticación** ampliamente usados en backend:

- Autenticación **stateless** basada en JSON Web Tokens (JWT)
- Autenticación **stateful** basada en sesiones

El objetivo es analizar las **diferencias, ventajas y limitaciones** de cada enfoque dentro del mismo dominio funcional.

---

## Implementaciones disponibles

El repositorio contiene dos ramas principales, cada una con una implementación independiente:

- `auth-jwt`  
  Autenticación basada en JWT. Pensada para sistemas stateless y escalables horizontalmente.

- `auth-session`  
  Autenticación basada en sesión. Pensada para escenarios donde se requiere mayor control sobre la revocación y el estado del usuario.

Cada rama incluye su propio README con:

- decisiones de diseño
- flujo de autenticación
- estructura del proyecto
- casos de uso cubiertos

---

## Flujo general de autenticación

```text
Cliente
  |
  | Solicitud de autenticación
  v
Spring Security Filter Chain
  |
  v
Validación de credenciales
  |
  v
JWT / Sesión creada
  |
  v
Acceso a recursos protegidos
```

---

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Security
- Maven
- JWT (rama `auth-jwt`)

---

## Ejecución

Cada implementación puede ejecutarse de forma independiente desde su rama correspondiente:

```bash
mvn spring-boot:run
```

---

## Licencia

Este proyecto está licenciado bajo la **Apache License 2.0**.
Consulta el archivo [LICENSE](LICENSE) para más información.

---

## Autor

HC-ONLINE.

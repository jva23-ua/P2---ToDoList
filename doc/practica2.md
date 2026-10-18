# Documentación Técnica - Evolución ToDoList (Versión 1.0.1)

## 1. Introducción y Arquitectura General

El trabajo se ha organizado en ramas/PR cortos: `#2` página "Acerca de", `#6` documentación, `#7` barra de navegación, `#8`/`#9` listado y descripción de usuarios, `#13` usuario administrador, `#14` protección del listado y `#15` bloqueo de usuarios. También se renombró el `artifactId` del `pom.xml` a `mads-todolist-jonathan-villacis` con versión `1.0.1`, y la imagen base del `Dockerfile` pasó a `eclipse-temurin:8-jdk-alpine`.

## 2. Listado de Nuevas Clases, Métodos y DTOs

### 2.1. Capa de Modelo y Repositorio

- `Usuario`: nuevos atributos `admin` y `bloqueado` (columnas `admin` y `bloqueado`), con *getters*/*setters* sobrecargados en `boolean` y `Boolean` para evitar problemas con ModelMapper y las expresiones de Thymeleaf.
- `UsuarioRepository`: consultas derivadas `findByAdminTrue()` y `existsByAdminTrue()`.

### 2.2. Capa de Servicios

- `UsuarioService`: nuevo estado `USER_BLOCKED` en el *enum* `LoginStatus`, comprobado dentro de `login()`; métodos `existeAdministrador()`, `findAdministrador()`, `allUsuarios()` y `bloquearAccesoUsuario(id, bloqueado)`. `registrar()` ahora impide crear un segundo administrador.

### 2.3. Capa de Controladores (Controllers)

- `HomeController` (nuevo): `about()`, `registrados()`, `descripcionRegistrado(id)` y `bloqueoUsuario(id, bloqueado)`.
- `LoginController`: tras el login, redirige al administrador a `/registrados`; gestiona el mensaje de acceso bloqueado y expone `/registro` con el flag `existeAdministrador`.
- `TareaController`: sólo añade al modelo los atributos que necesita la navbar.
- `GlobalExceptionHandler` (`@ControllerAdvice`): traduce `UsuarioNoLogeadoException` en la vista `error401` con estado HTTP 401.

### 2.4. Objetos de Transferencia de Datos (DTOs)

- `UsuarioData`: nuevos campos `admin` y `bloqueado`.
- `RegistroData`: nuevo campo `admin` para la casilla del formulario de registro.

## 3. Plantillas Thymeleaf y Fragmentos UI

- `fragments.html` (nuevo): fragmentos `head`, `javascript` y `navbar(usuarioLogeado, nombreUsuario)`.
- `about.html`: página "Acerca de" con versión y autor.
- `registrados.html`: tabla con id, correo, *badge* de estado y botones "Ver descripción" y "Bloquear/Habilitar", más mensaje *flash* de confirmación.
- `descripcionRegistrado.html`: ficha del usuario sin la contraseña.
- `error401.html`: página de error 401.
- Actualizadas `formLogin.html` (enlace a "Acerca de"), `formRegistro.html` (checkbox de administrador), `listaTareas.html`, `formNuevaTarea.html` y `formEditarTarea.html` (incrustan la navbar y eliminan el enlace "Salir").

## 4. Ejemplos de Código Fuente Relevante

### 4.1. Lógica del Menú Dinámico y Sesión de Usuario

La navbar recibe `usuarioLogeado` (id o `null`) y `nombreUsuario`, y renderiza un menú u otro:

```html
<ul class="navbar-nav ml-auto" th:if="${usuarioLogeado != null}">
    <li class="nav-item dropdown">
        <a class="nav-link dropdown-toggle" ... th:text="${nombreUsuario}"></a>
        <div class="dropdown-menu dropdown-menu-right">
            <a class="dropdown-item" th:href="@{/logout}"
               th:text="'Cerrar sesión ' + ${nombreUsuario}">Cerrar sesión</a>
        </div>
    </li>
</ul>
```

Así no hace falta duplicar cabecera y pie en cada plantilla: sólo se pasan los dos atributos al fragmento.

### 4.2. Protección y Bloqueo de Usuarios

Todos los endpoints de `HomeController` lanzan `UsuarioNoLogeadoException` si no hay sesión o si el usuario logeado no es administrador; el `@ControllerAdvice` centraliza la respuesta 401. El bloqueo es *stateless* en la vista (se reenvía el estado actual con un campo oculto) y delega la lógica en el servicio:

```java
@PostMapping("/registrados/{id}/bloqueo")
public String bloqueoUsuario(@PathVariable Long idUsuario,
                             @RequestParam boolean bloqueado,
                             RedirectAttributes flash) {
    ...
    usuarioService.bloquearAccesoUsuario(idUsuario, bloqueado);
    flash.addFlashAttribute("mensaje",
            bloqueado ? "Acceso del usuario bloqueado" : "Acceso del usuario habilitado");
    return "redirect:/registrados";   // Post/Redirect/Get
}
```

En `registrados.html` el botón se genera con `th:value="${!usuario.bloqueado}"`, de modo que el mismo formulario sirve para bloquear y para habilitar.

## 5. Estrategia de Pruebas Automáticas (Tests)

### 5.1. Tests de Servicio y Repositorio

En `UsuarioServiceTest` se añaden `servicioLoginUsuarioBloqueado` (bloquear → `USER_BLOCKED`, habilitar → `LOGIN_OK`) y `servicioBloqueoUsuarioNoExistente` (espera `UsuarioServiceException`). Siguen el patrón GIVEN/WHEN/THEN sobre H2.

### 5.2. Tests de Controladores (Capa Web)

`AcercaDeWebTest` (nuevo) usa `@SpringBootTest` + `MockMvc`, `@MockBean` sobre `ManagerUserSession` para simular la sesión y `@Sql("/clean-db.sql")` para aislar la base de datos. Cubre: `about` sin y con login (navbar distinta), listado `/registrados` como administrador, 401 para anónimos y no administradores (GET y POST), descripción sin exponer la contraseña, y el ciclo completo de bloqueo/habilitado. En `UsuarioWebTest` se añade `servicioLoginUsuarioBloqueado`, que mockea `usuarioService.login()` para comprobar el mensaje del formulario.

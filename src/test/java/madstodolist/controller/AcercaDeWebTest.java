package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class AcercaDeWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Declaramos los servicios como Autowired
    @Autowired
    private UsuarioService usuarioService;
    // Moqueamos el managerUserSession para poder moquear el usuario logeado
    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    public void getAboutDevuelveNombreAplicacion() throws Exception {
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(containsString("ToDoList")));
    }
    @Test
    public void getAboutSinLoginMuestraNavbarConLoginYRegistro() throws Exception {
        // Simular usr no logueado
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(containsString("ToDoList")))
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("href=\"/registro\"")));
    }

    @Test
    public void getAboutConLoginMuestraNombreYDesplegableUsuario() throws Exception {
        // GIVEN: usuario en BD
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario.setNombre("Usuario Ejemplo");
        usuario = usuarioService.registrar(usuario);

        // Simulamos usuario logeado
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        // WHEN + THEN
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(containsString("Usuario Ejemplo")))
                .andExpect(content().string(containsString("dropdown-toggle")))
                .andExpect(content().string(containsString("Cuenta")))
                .andExpect(content().string(containsString("Cerrar sesión Usuario Ejemplo")));
    }

    @Test
    public void getRegistradosDevuelveListadoUsuarios() throws Exception {
        // GIVEN: usuario "admin" logeado
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@ua");
        admin.setPassword("123");
        admin.setNombre("Admin");
        admin.setAdmin(true);
        admin = usuarioService.registrar(admin);
        Long adminId = admin.getId();

        UsuarioData u1 = new UsuarioData();
        u1.setEmail("u1@ua");
        u1.setPassword("123");
        usuarioService.registrar(u1);

        UsuarioData u2 = new UsuarioData();
        u2.setEmail("u2@ua");
        u2.setPassword("123");
        usuarioService.registrar(u2);

        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);

        this.mockMvc.perform(get("/registrados"))
                .andExpect(content().string(containsString("u1@ua")))
                .andExpect(content().string(containsString("u2@ua")));
    }

    @Test
    public void getRegistradosUsuarioNoAdminDevuelveNoAutorizado() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario.setNombre("Usuario");
        usuario = usuarioService.registrar(usuario);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("No tienes suficientes permisos para acceder a esta página.")));
    }

    @Test
    public void getRegistradosSinLoginDevuelveNoAutorizado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("No tienes suficientes permisos para acceder a esta página.")));
    }

    @Test
    public void getDescripcionRegistradoConLoginMuestraDatosSinPassword() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@ua");
        admin.setPassword("123");
        admin.setNombre("Admin");
        admin.setAdmin(true);
        admin = usuarioService.registrar(admin);

        UsuarioData u = new UsuarioData();
        u.setEmail("u1@ua");
        u.setPassword("secret");
        u.setNombre("Usuario Uno");
        u = usuarioService.registrar(u);

        when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());

        this.mockMvc.perform(get("/registrados/" + u.getId()))
                .andExpect(content().string(containsString("Descripción de usuario")))
                .andExpect(content().string(containsString("u1@ua")))
                .andExpect(content().string(containsString("Usuario Uno")))
                .andExpect(content().string(not(containsString("secret"))));
    }

    @Test
    public void postBloqueoUsuarioLoBloqueaYLoHabilita() throws Exception {
        // GIVEN: usuario "admin" logeado y un usuario normal
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@ua");
        admin.setPassword("123");
        admin.setNombre("Admin");
        admin.setAdmin(true);
        admin = usuarioService.registrar(admin);

        UsuarioData u1 = new UsuarioData();
        u1.setEmail("u1@ua");
        u1.setPassword("123");
        u1 = usuarioService.registrar(u1);

        when(managerUserSession.usuarioLogeado()).thenReturn(admin.getId());

        // WHEN: el administrador bloquea al usuario
        this.mockMvc.perform(post("/registrados/" + u1.getId() + "/bloqueo")
                        .param("bloqueado", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));

        // THEN: el usuario queda bloqueado y no puede logearse
        assertThat(usuarioService.findById(u1.getId()).isBloqueado()).isTrue();
        assertThat(usuarioService.login("u1@ua", "123"))
                .isEqualTo(UsuarioService.LoginStatus.USER_BLOCKED);

        // WHEN: el administrador habilita de nuevo al usuario
        this.mockMvc.perform(post("/registrados/" + u1.getId() + "/bloqueo")
                        .param("bloqueado", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));

        // THEN: el usuario vuelve a poder logearse
        assertThat(usuarioService.findById(u1.getId()).isBloqueado()).isFalse();
        assertThat(usuarioService.login("u1@ua", "123"))
                .isEqualTo(UsuarioService.LoginStatus.LOGIN_OK);
    }

    @Test
    public void postBloqueoUsuarioNoAdminDevuelveNoAutorizado() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        this.mockMvc.perform(post("/registrados/" + usuario.getId() + "/bloqueo")
                        .param("bloqueado", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void postBloqueoSinLoginDevuelveNoAutorizado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(post("/registrados/1/bloqueos")
                        .param("bloqueado", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getDescripcionRegistradoUsuarioNoAdminDevuelveNoAutorizado() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario.setNombre("Usuario");
        usuario = usuarioService.registrar(usuario);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        this.mockMvc.perform(get("/registrados/" + usuario.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("No tienes suficientes permisos para acceder a esta página.")));
    }

    @Test
    public void getDescripcionRegistradoSinLoginDevuelveNoAutorizado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/registrados/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("No tienes suficientes permisos para acceder a esta página.")));
    }
}
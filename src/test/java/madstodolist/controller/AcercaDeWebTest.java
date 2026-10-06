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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.when;

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
        UsuarioData u1 = new UsuarioData();
        u1.setEmail("u1@ua");
        u1.setPassword("123");
        usuarioService.registrar(u1);

        UsuarioData u2 = new UsuarioData();
        u2.setEmail("u2@ua");
        u2.setPassword("123");
        usuarioService.registrar(u2);

        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/registrados"))
                .andExpect(content().string(containsString("u1@ua")))
                .andExpect(content().string(containsString("u2@ua")));
    }
}
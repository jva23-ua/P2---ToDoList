package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import madstodolist.service.EquipoService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class EquipoWebTest {
    @Autowired
    private MockMvc mockMvc;

    // Declaramos los servicios como Autowired
    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    Map<String, Long> addUsuarioEquipoBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user_prb@ua");
        usuario.setPassword("123");
        usuario.setNombre("Participante Ejemplo");
        usuario = usuarioService.registrar(usuario);

        EquipoData equipo = equipoService.crearEquipo("Equipo 1");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuario.getId());
        ids.put("equipoId", equipo.getId());
        return ids;
    }



    @Test
    public void getEquiposSinLoginDevuelveNoAutorizado() throws Exception {
        // usuarioLogeado() -> null; espera 401
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getEquiposMuestraListadoOrdenado() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user_prb@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String html = this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("Proyecto AAA", "Proyecto BBB");
        assertThat(html.indexOf("Proyecto AAA"))
                .isLessThan(html.indexOf("Proyecto BBB"));
    }

    @Test
    public void getEquiposMuestraEnlaceMenu() throws Exception {
        // Comprueba href="/equipos" en el HTML
        Long usarioId = addUsuarioEquipoBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usarioId);

        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/equipos\"")));
    }

    @Test
    public void getDetalleEquipoMuestraUsuarios() throws Exception {
        // GET /equipos/{id}; contiene nombre del equipo, email y nombre del participante
        Map<String, Long> ids = addUsuarioEquipoBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipoId = ids.get("equipoId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(get("/equipos/" + equipoId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Equipo 1")))
                .andExpect(content().string(containsString("user_prb@ua")))
                .andExpect(content().string(containsString("Participante Ejemplo")));
    }

    @Test
    public void getDetalleEquipoInexistenteRedirigeAListado() throws Exception {
        // GET /equipos/999; espera 3xx + redirect a /equipos
        Long usuarioID = addUsuarioEquipoBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioID);

        String urlRedirect = "/equipos";

        this.mockMvc.perform(get("/equipos/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));
    }
}

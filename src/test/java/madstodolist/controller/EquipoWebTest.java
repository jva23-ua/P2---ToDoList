package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import madstodolist.service.EquipoServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import madstodolist.service.EquipoService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @Test
    public void getDetalleEquipoMuestraControlesGestion() throws Exception {
        Map<String, Long> ids = addUsuarioEquipoBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipoId = ids.get("equipoId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(get("/equipos/" + equipoId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Añadir al equipo")))
                .andExpect(content().string(containsString("Eliminar")));
    }

    @Test
    public void postAddUsuarioAEquipoLoAnadeYSeRenderiza() throws Exception {
        Map<String, Long> ids = addUsuarioEquipoBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipoId = ids.get("equipoId");

        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("user2@ua");
        usuario2.setPassword("123");
        usuario2.setNombre("Participante 2");
        usuario2 = usuarioService.registrar(usuario2);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);
        this.mockMvc.perform(post("/equipos/" + equipoId + "/usuarios")
                    .param("email", usuario2.getEmail()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/" + equipoId));

        assertThat(equipoService.usuariosEquipo(equipoId)).hasSize(2);

        this.mockMvc.perform(get("/equipos/" + equipoId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("user2@ua")))
                .andExpect(content().string(containsString("Participante 2")));
    }

    @Test
    public void postAddUsuarioEmailNoRegistradoNoLoAnade() throws Exception {
        Map<String, Long> ids = addUsuarioEquipoBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipoId = ids.get("equipoId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(post("/equipos/" + equipoId + "/usuarios")
                .param("email", "noExiste@ua"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/" + equipoId));

        assertThat(equipoService.usuariosEquipo(equipoId)).hasSize(1);
    }

    @Test
    public void postEliminarUsuarioDeEquipoLoElimina() throws Exception {
        Map<String, Long> ids = addUsuarioEquipoBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipoId = ids.get("equipoId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(post("/equipos/" + equipoId + "/usuarios/" + usuarioId + "/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/" + equipoId));

        assertThat(equipoService.usuariosEquipo(equipoId)).hasSize(0);
    }

    @Test
    public void postAddUsuarioAEquipoSinLoginDevuelveNoAutorizado() throws Exception {

        when(managerUserSession.usuarioLogeado()).thenReturn(null);
        this.mockMvc.perform(post("/equipos/1/usuarios")
                .param("email", "algo@ua"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void postEliminarUsuarioDeEquipoSinLoginDevuelveNoAutorizado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);
        this.mockMvc.perform(post("/equipos/1/usuarios/1/eliminar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void postEditarNombreEquipoSinLoginDevuelveNoAutorizado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);
        this.mockMvc.perform(post("/equipos/1/editar")
                    .param("nombre", "Nuevo Nombre"))
                .andExpect(status().isUnauthorized());
    }

    @Test void postEditarNombreEquipoConUsuarioNoAdminDevuelveNoAutorizado() throws Exception {
        Map<String, Long> ids = addUsuarioEquipoBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipoId = ids.get("equipoId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);
        this.mockMvc.perform(post("/equipos/" + equipoId + "/editar")
                    .param("nombre", "Nuevo Nombre"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void postEditarNombreEquipoConUsuarioAdminEditaNombre() throws Exception {
        // GIVEN
        // usuario admin y un equipo en la base de datos
        UsuarioData usuarioAdmin = new UsuarioData();
        usuarioAdmin.setEmail("admin@ua");
        usuarioAdmin.setPassword("123");
        usuarioAdmin.setAdmin(true);
        usuarioAdmin = usuarioService.registrar(usuarioAdmin);

        EquipoData equipo = equipoService.crearEquipo("Nombre muy original");
        // WHEN
        // el usuario admin edita el nombre del equipo
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioAdmin.getId());

        this.mockMvc.perform(post("/equipos/" + equipo.getId() + "/editar")
            .param("nombre", "Nuevo Nombre"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        // THEN
        // El nombre se ha persistido y se muestra en el listado
        assertThat(equipoService.recuperarEquipo(equipo.getId()).getNombre())
                .isEqualTo("Nuevo Nombre");
        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nuevo Nombre")));
    }

    @Test
    public void getFormEditarEquipoMuestraFormularioConNombreActual() throws Exception {
        // GET /equipos/{id}/editar; el formulario apunta al endpoint de guardado
        // y precarga el nombre actual del equipo
        Map<String, Long> ids = addUsuarioEquipoBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipoId = ids.get("equipoId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(get("/equipos/" + equipoId + "/editar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "action=\"/equipos/" + equipoId + "/editar\"")))
                .andExpect(content().string(containsString("Equipo 1")))
                .andExpect(content().string(containsString("Modificar equipo")));
    }

    @Test
    public void deleteEquipoLoElimina() throws Exception {
        // GIVEN
        // un equipo con un miembro en la base de datos
        Map<String, Long> ids = addUsuarioEquipoBD();
        Long usuarioId = ids.get("usuarioId");
        Long equipoId = ids.get("equipoId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN
        // Se envía la petición DELETE que lanza el botón "Borrar"
        this.mockMvc.perform(delete("/equipos/" + equipoId))
                .andExpect(status().isOk());

        // THEN
        // El equipo ya no existe y el usuario ya no pertenece a él
        assertThatThrownBy(() -> equipoService.recuperarEquipo(equipoId))
                .isInstanceOf(EquipoServiceException.class);
        assertThat(equipoService.equiposUsuario(usuarioId)).isEmpty();
    }

    @Test
    public void getFormularioNuevoEquipoUsuarioLogueado() throws Exception {
        // GET /equipos/nuevo; el formulario apunta al endpoint de creación
        // y muestra los botones de crear y cancelar
        Long usuarioId = addUsuarioEquipoBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(get("/equipos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/equipos/nuevo\"")))
                .andExpect(content().string(containsString("Crear equipo")))
                .andExpect(content().string(containsString("Cancelar")));
    }

    @Test
    public void getFormularioNuevoEquipoUsuarioNoLogueado() throws Exception {
        // usuarioLogeado() -> null; espera 401
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        this.mockMvc.perform(get("/equipos/nuevo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void postFormularioNuevoEquipo() throws Exception {
        // GIVEN
        // un usuario logueado
        Long usuarioId = addUsuarioEquipoBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN
        // Se envía el formulario de nuevo equipo
        this.mockMvc.perform(post("/equipos/nuevo")
                        .param("nombre", "Proyecto Nuevo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        // THEN
        // El equipo se ha creado y aparece en el listado de equipos
        assertThat(equipoService.findAllOrdenadoPorNombre())
                .extracting(EquipoData::getNombre)
                .contains("Proyecto Nuevo");
        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Proyecto Nuevo")));
    }

    @Test
    public void postFormularioNuevoEquipoConNombreVacioNoLoCrea() throws Exception {
        // GIVEN
        // un usuario logueado
        Long usuarioId = addUsuarioEquipoBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN
        // Se envía el formulario con el nombre vacío (la validación del servicio lo rechaza)
        // THEN
        // Se vuelve al formulario con el mensaje de error y no se crea el equipo
        this.mockMvc.perform(post("/equipos/nuevo")
                        .param("nombre", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/nuevo"))
                .andExpect(flash().attribute("mensaje",
                        "El nombre del equipo no puede ser nulo o vacío"));

        // Solo sigue existiendo el equipo creado en el GIVEN
        assertThat(equipoService.findAllOrdenadoPorNombre()).hasSize(1);
    }
}

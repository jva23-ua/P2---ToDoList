package madstodolist.controller;
import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EquipoController {
    @Autowired
    private EquipoService equipoService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/equipos")
    public String listadoEquipos(Model model) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) throw new UsuarioNoLogeadoException();

        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        if (usuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        model.addAttribute("equipos", equipoService.findAllOrdenadoPorNombre());
        model.addAttribute("usuarioLogeado", idUsuarioLogeado);
        model.addAttribute("nombreUsuario", usuario.getNombre());
        return "listaEquipos";
    }

    @GetMapping("/equipos/{id}/editar")
    public String formEditarEquipo(@PathVariable("id") Long equipoId, Model model,
                                   RedirectAttributes flash) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) throw new UsuarioNoLogeadoException();

        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        if (usuario == null) throw new UsuarioNoLogeadoException();

        try {
            model.addAttribute("equipo", equipoService.recuperarEquipo(equipoId));
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("mensaje", "El equipo no existe");
            return "redirect:/equipos";
        }
        model.addAttribute("usuarioLogeado", idUsuarioLogeado);
        model.addAttribute("nombreUsuario", usuario.getNombre());
        return "formEditarEquipo";
    }

    @PostMapping("/equipos/{id}/editar")
    public String editarEquipo(@PathVariable("id") Long equipoId,
                               @RequestParam("nombre") String nombre,
                               RedirectAttributes flash) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) throw new UsuarioNoLogeadoException();

        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        if (usuario == null || !usuario.isAdmin()) throw new UsuarioNoLogeadoException();

        try {
            equipoService.editarNombreEquipo(equipoId, nombre);
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/equipos/" + equipoId + "/editar";
        }
        flash.addFlashAttribute("mensaje", "Equipo modificado correctamente");
        return "redirect:/equipos";
    }

    @GetMapping("/equipos/{id}")
    public String detalleEquipo(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) throw new UsuarioNoLogeadoException();

        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        if (usuario == null) throw new UsuarioNoLogeadoException();

        try {
            model.addAttribute("equipo", equipoService.recuperarEquipo(id));
            model.addAttribute("usuarios", equipoService.usuariosEquipo(id));
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("mensaje", "El equipo no existe");
            return "redirect:/equipos";
        }
        model.addAttribute("usuarioLogeado", idUsuarioLogeado);
        model.addAttribute("nombreUsuario", usuario.getNombre());
        return "detalleEquipo";
    }

    @PostMapping("/equipos/{id}/usuarios")
    public String añadirUsuarioEquipo(@PathVariable Long id,
                                      @RequestParam("email") String email,
                                      RedirectAttributes flash) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) throw new UsuarioNoLogeadoException();

        try {
            equipoService.recuperarEquipo(id);
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("mensaje", "El equipo no existe");
            return "redirect:/equipos";
        }

        UsuarioData usuario = usuarioService.findByEmail(email);
        if (usuario == null) {
            flash.addFlashAttribute("mensaje", "No existe ningún usuario registrado con ese email");
            return "redirect:/equipos/" + id;
        }

        equipoService.añadirUsuarioAEquipo(id, usuario.getId());
        flash.addFlashAttribute("mensaje", "Usuario añadido al equipo correctamente");
        return "redirect:/equipos/" + id;
    }

    @PostMapping("/equipos/{idEquipo}/usuarios/{idUsuario}/eliminar")
    public String eliminarUsuarioEquipo(@PathVariable Long idEquipo,
                                        @PathVariable Long idUsuario,
                                        RedirectAttributes flash) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) throw new UsuarioNoLogeadoException();

        try {
            equipoService.eliminarUsuarioDeEquipo(idEquipo, idUsuario);
            flash.addFlashAttribute("mensaje", "Usuario eliminado del equipo correctamente");
            return "redirect:/equipos/" + idEquipo;
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/equipos";
        }
    }
}

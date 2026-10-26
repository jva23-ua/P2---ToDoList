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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
}

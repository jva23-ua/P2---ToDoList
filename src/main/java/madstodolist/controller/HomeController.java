package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/about")
    public String about(Model model) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        model.addAttribute("usuarioLogeado", idUsuarioLogeado);

        if (idUsuarioLogeado != null) {
            UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
            if (usuario != null) {
                model.addAttribute("nombreUsuario", usuario.getNombre());
            }
        }

        return "about";
    }

    @GetMapping("/registrados")
    public String registrados(Model model) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        if (usuario == null || !usuario.isAdmin()) {
            throw new UsuarioNoLogeadoException();
        }

        model.addAttribute("usuarios", usuarioService.allUsuarios());
        model.addAttribute("usuarioLogeado", idUsuarioLogeado);
        model.addAttribute("nombreUsuario", usuario.getNombre());

        return "registrados";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionRegistrado(@PathVariable(value = "id") Long idUsuario, Model model) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuarioLogeado = usuarioService.findById(idUsuarioLogeado);
        if (usuarioLogeado == null || !usuarioLogeado.isAdmin()) {
            throw new UsuarioNoLogeadoException();
        }

        model.addAttribute("nombreUsuario", usuarioLogeado.getNombre());
        model.addAttribute("usuarioLogeado", idUsuarioLogeado);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        if (usuario == null) {
            return "redirect:/registrados";
        }

        model.addAttribute("usuario", usuario);
        return "descripcionRegistrado";
    }
}
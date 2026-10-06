package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }

    @GetMapping("/registrados")
    public String registrados(Model model) {
        model.addAttribute("usuarios", usuarioService.allUsuarios());

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        model.addAttribute("usuarioLogeado", idUsuarioLogeado);

        if (idUsuarioLogeado != null) {
            UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
            if (usuario != null) {
                model.addAttribute("nombreUsuario", usuario.getNombre());
            }
        }
        return "registrados";
    }
}
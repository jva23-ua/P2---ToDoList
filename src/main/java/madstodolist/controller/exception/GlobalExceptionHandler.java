package madstodolist.controller.exception;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService;

    // Pone esAdmin en el modelo de TODAS las vistas, para que el navbar pueda
    // mostrar u ocultar el enlace "Registrados" sin repetirlo en cada controlador
    @ModelAttribute("esAdmin")
    public boolean esAdmin() {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) return false;
        UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
        return usuario != null && usuario.isAdmin();
    }

    @ExceptionHandler(UsuarioNoLogeadoException.class)
    public ModelAndView handleUsuarioNoLogeado(UsuarioNoLogeadoException ex) {
        ModelAndView mav = new ModelAndView("error401");
        mav.setStatus(HttpStatus.UNAUTHORIZED);
        mav.addObject("mensaje", "No tienes suficientes permisos para acceder a esta página.");
        return mav;
    }
}

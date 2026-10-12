package madstodolist.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNoLogeadoException.class)
    public ModelAndView handleUsuarioNoLogeado(UsuarioNoLogeadoException ex) {
        ModelAndView mav = new ModelAndView("error401");
        mav.setStatus(HttpStatus.UNAUTHORIZED);
        mav.addObject("mensaje", "No tienes suficientes permisos para acceder a esta página.");
        return mav;
    }
}

package madstodolist.controller;
import madstodolist.authentication.ManagerUserSession;
import madstodolist.service.EquipoService;
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
}

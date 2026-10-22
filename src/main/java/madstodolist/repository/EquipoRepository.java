package madstodolist.repository;

import madstodolist.model.Equipo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EquipoRepository extends CrudRepository<Equipo, Long> {

}

package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.model.Equipo;
import madstodolist.repository.EquipoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipoService {

    @Autowired
    EquipoRepository equipoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        // Completar
        Equipo equipo = new Equipo(nombre);
        Equipo equipoNuevo = modelMapper.map(equipo, Equipo.class);
        equipoNuevo = equipoRepository.save(equipoNuevo);
        return modelMapper.map(equipoNuevo, EquipoData.class);

    }

    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        // Completar
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if(equipo == null) return null;
        else{
            return modelMapper.map(equipo, EquipoData.class);
        }
    }
}
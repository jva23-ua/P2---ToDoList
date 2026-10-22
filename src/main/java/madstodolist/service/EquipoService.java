package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class EquipoService {

    @Autowired
    EquipoRepository equipoRepository;
    @Autowired
    UsuarioRepository usuarioRepository;

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

    @Transactional(readOnly = true)
    public List<EquipoData> findAllOrdenadoPorNombre(){

        List<Equipo> equipos = equipoRepository.findAll();
        return equipos.stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .sorted((e1, e2) -> e1.getNombre().compareToIgnoreCase(e2.getNombre()))
                .collect(Collectors.toList());
    }
    @Transactional
    public void añadirUsuarioAEquipo(Long id, Long id1) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        Usuario usuario = usuarioRepository.findById(id1).orElse(null);
        equipo.addUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }
}
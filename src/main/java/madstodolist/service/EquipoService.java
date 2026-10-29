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
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + id));
        return modelMapper.map(equipo, EquipoData.class);
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
    public void añadirUsuarioAEquipo(Long idEquipo, Long idUsuario) {
        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + idEquipo));
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EquipoServiceException("No existe el usuario con id " + idUsuario));
        equipo.addUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + id));
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<EquipoData> equiposUsuario(Long id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EquipoServiceException("No existe el usuario con id " + id));
        return usuario.getEquipos().stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarUsuarioDeEquipo(Long equipoId, Long usuarioId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + equipoId));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EquipoServiceException("No existe el usuario con id " + usuarioId));
        equipo.removeUsuario(usuario);
    }

    @Transactional
    public void editarNombreEquipo(Long id, String nuevoNombre){
        if (nuevoNombre == null || nuevoNombre.isEmpty()) {
            throw new EquipoServiceException("El nombre del equipo no puede ser nulo o vacío");
        }
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + id));
        equipo.setNombre(nuevoNombre);
    }

    @Transactional
    public void eliminarEquipo(Long id){
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoServiceException("No existe el equipo con id " + id));
        equipoRepository.delete(equipo);
    }

}
package com.javanauta.usuario.business;


import com.javanauta.usuario.business.converter.UsuarioConvernter;
import com.javanauta.usuario.business.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConvernter usuarioConvernter;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        Usuario usuario = usuarioConvernter.paraUsuario(usuarioDTO);
        return usuarioConvernter.paraUsuarioDTO(usuarioRepository.save(usuario)
        );
    }
}

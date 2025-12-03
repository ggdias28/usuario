package com.javanauta.usuario.business;


import com.javanauta.usuario.business.converter.UsuarioConvernter;
import com.javanauta.usuario.business.dto.EnderecoDTO;
import com.javanauta.usuario.business.dto.TelefoneDTO;
import com.javanauta.usuario.business.dto.UsuarioDTO;
import com.javanauta.usuario.infrastructure.entity.Endereco;
import com.javanauta.usuario.infrastructure.entity.Telefone;
import com.javanauta.usuario.infrastructure.entity.Usuario;
import com.javanauta.usuario.infrastructure.exception.ConflictException;
import com.javanauta.usuario.infrastructure.exception.ResourceNotFoundException;
import com.javanauta.usuario.infrastructure.repository.EnderecoRepository;
import com.javanauta.usuario.infrastructure.repository.TelefoneRepository;
import com.javanauta.usuario.infrastructure.repository.UsuarioRepository;
import com.javanauta.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConvernter usuarioConvernter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConvernter.paraUsuario(usuarioDTO);
        return usuarioConvernter.paraUsuarioDTO(usuarioRepository.save(usuario)
        );
    }

    public void emailExiste(String email){
        try{
            boolean existe = verificaEmailExistente(email);
            if(existe){
                throw new ConflictException("Email ja cadastrado" + email);
            }
        } catch (ConflictException e) {
            throw new ConflictException("Email ja cadastrado" + e.getCause());
        }
    }

    public boolean verificaEmailExistente(String email) {

        return usuarioRepository.existsByEmail(email);
    }

    public UsuarioDTO buscarUsuarioPorEmail(String email) {

        try {
            return usuarioConvernter.paraUsuarioDTO
                    (usuarioRepository.findByEmail(email)
                            .orElseThrow(
                    () -> new ResourceNotFoundException("Email nao encontrado" + email)));
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Email nao encontrado" + email);
        }
    }
    public void deletaUsuarioPorEmail(String email){

        usuarioRepository.deleteByEmail(email);
    }


    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto){

        //busca o email do usuario atravez do token (tira a obrigatoriedade de passar o email)
        String email = jwtUtil.extrairEmailToken(token.substring(7));

        //criptografia de senha
        dto.setSenha(dto.getSenha() !=null ? passwordEncoder.encode(dto.getSenha()) : null);
        // busca os dados do usuario no banco de dados

        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
              new ResourceNotFoundException("Email nao localizado"));
        //mesclamos os dados que recebemos na requisição DTO com os dados do db

        Usuario usuario = usuarioConvernter.updateUsuario(dto, usuarioEntity);
        //criptografica na senha novamente
        usuario.setSenha(passwordEncoder.encode(usuario.getPassword()));
        //salvamos os dados do usuario convertidos e depois pegou o retornoe  converteu para Usuario DTO
        return usuarioConvernter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }

    public EnderecoDTO atualizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO){

        Endereco entity = enderecoRepository.findById(idEndereco).orElseThrow( () ->
                new ResourceNotFoundException("Id nao encontrado" + idEndereco));

        Endereco endereco = usuarioConvernter.updateEndereco(enderecoDTO, entity);

        return usuarioConvernter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO telefoneDTO){
        Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow( () ->
                new ResourceNotFoundException("Id nao encontrado" + idTelefone));

        Telefone telefone = usuarioConvernter.updateTelefone(telefoneDTO, entity);

        return usuarioConvernter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTO cadastraEndereco(String token, EnderecoDTO dto){
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("email nao encontrado "+ email));
        Endereco endereco = usuarioConvernter.paraEnderecoEntity(dto, usuario.getId());
        Endereco enderecoEntity = enderecoRepository.save(endereco);
        return  usuarioConvernter.paraEnderecoDTO(enderecoEntity);
    }

    public TelefoneDTO cadastraTelefone(String token, TelefoneDTO dto){
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("email nao encontrado "+ email));



        Telefone telefone = usuarioConvernter.paraTelefoneEntity(dto, usuario.getId());
        Telefone telefoneEntity = telefoneRepository.save(telefone);
        return  usuarioConvernter.paraTelefoneDTO(telefoneEntity);
    }
}

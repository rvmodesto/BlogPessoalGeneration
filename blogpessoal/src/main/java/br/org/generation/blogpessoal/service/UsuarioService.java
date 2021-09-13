package br.org.generation.blogpessoal.service;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.org.generation.blogpessoal.model.Usuario;
import br.org.generation.blogpessoal.model.UsuarioLogin;
import br.org.generation.blogpessoal.repository.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	
	public String encoder(String senha) {
		return encoder.encode(senha);
	}
	
	public List<Usuario> listarUsuarios(){
		return usuarioRepository.findAll();
	}
	
	public Optional<Usuario> buscarUsuarioId(long id) {
	
		return usuarioRepository.findById(id);
	}
	
	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {
		
		if(usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O Usuário já existe!", null);
		//verificar idade do usuario
		int idade = Period.between(usuario.getDataNascimento(), LocalDate.now()).getYears();
		
		if(idade < 18)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O Usuário é menor de idade!", null);		
			usuario.setSenha(encoder(usuario.getSenha()));
	
		//String senhaEncoder = encoder.encode(usuario.getSenha());
		
		return Optional.of(usuarioRepository.save(usuario));
	}
	
	
	public Optional<Usuario> atualizarUsuario(Usuario usuario){
		for (Usuario user : this.listarUsuarios()) {
            if (user.getUsuario().equals(usuario.getUsuario())) {
            	throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um usuario com esse nome", null);
            }
        }
		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isEmpty()){
			int idade = Period.between(usuario.getDataNascimento(), LocalDate.now()).getYears();
			if (idade < 18)
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O Usuário é menor de idade!", null);
			
			usuario.setSenha(encoder(usuario.getSenha()));
			return Optional.of(usuarioRepository.save(usuario));
		
		}else{
			
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "O Usuário não encontrado!", null);
			
		}
		
	}
	
	public Optional<UsuarioLogin> loginUsuario(Optional <UsuarioLogin> usuarioLogin){
		
		Optional <Usuario> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());
		
		if(usuario.isPresent()) {
			
			if(encoder.matches(usuarioLogin.get().getSenha(), usuario.get().getSenha())) {

				String auth = usuarioLogin.get().getUsuario() + ":" + usuarioLogin.get().getSenha();
				byte[] encodeAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodeAuth);
				
				usuarioLogin.get().setId(usuario.get().getId());
				usuarioLogin.get().setNome(usuario.get().getNome());
				usuarioLogin.get().setSenha(usuario.get().getSenha());
				usuarioLogin.get().setToken(authHeader);
				
				return usuarioLogin;
				
			}
		}
	
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "O Usuário não encontrado!", null);	
	
	}
}
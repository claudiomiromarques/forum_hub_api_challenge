package br.com.alura.forumhub.controller;

import br.com.alura.forumhub.dto.DadosCadastroUsuario;
import br.com.alura.forumhub.dto.DadosDetalhamentoUsuario;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @Transactional
    public ResponseEntity<?> cadastrar(@RequestBody @Valid DadosCadastroUsuario dados,
                                       UriComponentsBuilder uriBuilder) {
        if (repository.findByLogin(dados.login()).isPresent()) {
            Map<String, String> erro = Map.of("mensagem", "Login já cadastrado. Por favor, escolha outro.");
            return ResponseEntity.badRequest().body(erro);
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(dados.login());
        usuario.setSenha(passwordEncoder.encode(dados.senha()));

        repository.save(usuario);

        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoUsuario(usuario));
    }

    @GetMapping("/me")
    public ResponseEntity<DadosDetalhamentoUsuario> detalharUsuarioLogado(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.notFound().build();
        }

        var usuario = repository.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return ResponseEntity.ok(new DadosDetalhamentoUsuario(usuario));
    }
}
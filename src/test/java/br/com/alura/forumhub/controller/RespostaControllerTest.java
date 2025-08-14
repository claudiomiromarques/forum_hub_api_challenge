package br.com.alura.forumhub.controller;

import br.com.alura.forumhub.dto.DadosAtualizacaoResposta;
import br.com.alura.forumhub.dto.DadosCadastroResposta;
import br.com.alura.forumhub.model.Resposta;
import br.com.alura.forumhub.model.StatusTopico;
import br.com.alura.forumhub.model.Topico;
import br.com.alura.forumhub.model.Usuario;
import br.com.alura.forumhub.repository.RespostaRepository;
import br.com.alura.forumhub.repository.TopicoRepository;
import br.com.alura.forumhub.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ActiveProfiles("test")
class RespostaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<DadosCadastroResposta> dadosCadastroRespostaJson;

    @Autowired
    private JacksonTester<DadosAtualizacaoResposta> dadosAtualizacaoRespostaJson;

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RespostaRepository respostaRepository;

    private Usuario autor;
    private Topico topico;

    @BeforeEach
    void setUp() {
        respostaRepository.deleteAll();
        topicoRepository.deleteAll();
        usuarioRepository.deleteAll();

        this.autor = usuarioRepository.save(new Usuario(null, "autor.teste", "123456"));

        this.topico = new Topico(null, "Tópico de Teste", "Mensagem do tópico", true, LocalDateTime.now(), StatusTopico.NAO_RESPONDIDO, null, autor.getUsername(), "Spring Boot");
        topicoRepository.save(this.topico);
    }

    @Test
    @DisplayName("Deveria retornar 403 ao tentar criar resposta sem estar autenticado")
    void criarResposta_cenario1() throws Exception {
        var dados = new DadosCadastroResposta("Esta é uma resposta de teste.");

        mvc.perform(post("/topicos/" + topico.getId() + "/respostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroRespostaJson.write(dados).getJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deveria retornar 404 ao tentar criar resposta para um tópico inexistente")
    void criarResposta_cenario2() throws Exception {
        var dados = new DadosCadastroResposta("Resposta para tópico que não existe.");
        long idTopicoInexistente = 999L;

        mvc.perform(post("/topicos/" + idTopicoInexistente + "/respostas")
                        .with(user(this.autor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroRespostaJson.write(dados).getJson()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deveria retornar 201 ao criar uma resposta com dados válidos")
    void criarResposta_cenario3() throws Exception {
        var dados = new DadosCadastroResposta("Nova resposta criada com sucesso!");
        var autorResposta = usuarioRepository.save(new Usuario(null, "outro.autor", "123"));

        var response = mvc.perform(post("/topicos/" + topico.getId() + "/respostas")
                        .with(user(autorResposta))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosCadastroRespostaJson.write(dados).getJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        var jsonRetornado = response.getContentAsString();

        String mensagemDoJson = JsonPath.read(jsonRetornado, "$.mensagem");
        String autorDoJson = JsonPath.read(jsonRetornado, "$.nomeAutor"); // <<-- CORREÇÃO AQUI

        assertThat(mensagemDoJson).isEqualTo(dados.mensagem());
        assertThat(autorDoJson).isEqualTo(autorResposta.getUsername());
    }

    @Test
    @DisplayName("GET - Deveria retornar 200 e a lista de respostas de um tópico")
    void listarRespostas_cenario1() throws Exception {
        // Arrange: Cria duas respostas para o mesmo tópico
        var autorResposta = usuarioRepository.save(new Usuario(null, "comentarista", "123"));
        respostaRepository.save(new Resposta(null, "Primeira resposta", this.topico, LocalDateTime.now(), autorResposta, false));
        respostaRepository.save(new Resposta(null, "Segunda resposta", this.topico, LocalDateTime.now(), autorResposta, false));

        // Act & Assert
        mvc.perform(get("/topicos/" + this.topico.getId() + "/respostas")
                        .with(user(this.autor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET - Deveria retornar 200 e uma lista vazia para um tópico sem respostas")
    void listarRespostas_cenario2() throws Exception {

        // Act & Assert
        mvc.perform(get("/topicos/" + this.topico.getId() + "/respostas")
                        .with(user(this.autor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("PUT - Deveria retornar 200 ao atualizar a própria resposta")
    void atualizarResposta_cenario1() throws Exception {
        // Arrange
        var autorResposta = usuarioRepository.save(new Usuario(null, "editor", "123"));
        var resposta = respostaRepository.save(new Resposta(null, "Mensagem original", this.topico, LocalDateTime.now(), autorResposta, false));
        var dadosAtualizacao = new DadosAtualizacaoResposta("Mensagem atualizada!");

        // Act & Assert
        mvc.perform(put("/topicos/{idTopico}/respostas/{idResposta}", this.topico.getId(), resposta.getId())
                        .with(user(autorResposta))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosAtualizacaoRespostaJson.write(dadosAtualizacao).getJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Mensagem atualizada!"));
    }

    @Test
    @DisplayName("PUT - Deveria retornar 400 ao tentar atualizar resposta de outro usuário")
    void atualizarResposta_cenario2() throws Exception {
        // Arrange
        var autorResposta = usuarioRepository.save(new Usuario(null, "dono.resposta", "123"));
        var outroUsuario = usuarioRepository.save(new Usuario(null, "invasor", "123"));
        var resposta = respostaRepository.save(new Resposta(null, "Mensagem original", this.topico, LocalDateTime.now(), autorResposta, false));
        var dadosAtualizacao = new DadosAtualizacaoResposta("Tentativa de invasão");

        // Act & Assert
        mvc.perform(put("/topicos/{idTopico}/respostas/{idResposta}", this.topico.getId(), resposta.getId())
                        .with(user(outroUsuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dadosAtualizacaoRespostaJson.write(dadosAtualizacao).getJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE - Deveria retornar 204 quando autor da resposta exclui a própria resposta")
    void deletarResposta_cenario1() throws Exception {
        var autorResposta = usuarioRepository.save(new Usuario(null, "deleter", "123"));

        var resposta = respostaRepository.save(new Resposta(null, "Resposta a ser deletada", topico, LocalDateTime.now(), autorResposta, false));

        mvc.perform(
                delete("/topicos/{idTopico}/respostas/{idResposta}", topico.getId(), resposta.getId())
                        .with(user(autorResposta))
        ).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE - Deveria retornar 400 quando usuário tenta excluir resposta de outro")
    void deletarResposta_cenario2() throws Exception {
        var autorResposta = usuarioRepository.save(new Usuario(null, "dono.resposta", "123"));
        var outroUsuario = usuarioRepository.save(new Usuario(null, "outro.usuario", "123"));

        var resposta = new Resposta(null, "Resposta protegida", topico, LocalDateTime.now(), autorResposta, false);
        respostaRepository.save(resposta);

        mvc.perform(
                delete("/topicos/{idTopico}/respostas/{idResposta}", topico.getId(), resposta.getId())
                        .with(user(outroUsuario))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE - Deveria retornar 204 quando autor do TÓPICO exclui uma resposta")
    void deletarResposta_cenario3() throws Exception {
        // Arrange
        var autorResposta = usuarioRepository.save(new Usuario(null, "autor.resposta", "123"));
        var resposta = respostaRepository.save(new Resposta(null, "Resposta de outra pessoa", this.topico, LocalDateTime.now(), autorResposta, false));

        // Act & Assert
        mvc.perform(
                delete("/topicos/{idTopico}/respostas/{idResposta}", this.topico.getId(), resposta.getId())
                        .with(user(this.autor))
        ).andExpect(status().isNoContent());
    }
}
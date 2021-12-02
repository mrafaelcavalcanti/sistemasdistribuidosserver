package com.ufape.sistemasdistribuidosserver.apirest;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import com.ufape.sistemasdistribuidosserver.model.Arquivo;
import com.ufape.sistemasdistribuidos.model.ArquivoAux;
import com.ufape.sistemasdistribuidosserver.model.ArquivosCompartilhadosUsuarios;
import com.ufape.sistemasdistribuidosserver.model.Requisicao;
import com.ufape.sistemasdistribuidosserver.repository.ArquivoDAOI;
import com.ufape.sistemasdistribuidosserver.repository.ArquivosCompartilhadosUsuariosDAOI;
import com.ufape.sistemasdistribuidosserver.repository.RequisicoesDAOI;
import com.ufape.sistemasdistribuidosserver.repository.UsuarioDAOI;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/arquivos")
public class ArquivoRESTController {

    @Autowired
    ArquivoDAOI arquivoDAOI;

    @Autowired
    RequisicoesDAOI requisicoesDAOI;

    @Autowired
    UsuarioDAOI usuarioDAOI;

    @Autowired
    ArquivosCompartilhadosUsuariosDAOI arquivosCompartilhadosUsuariosDAOI;


    @GetMapping("/obter/{id}")
    public byte[] getArquivoByUserId(@PathVariable("id") Long id) throws IOException {
        Arquivo nArquivo = arquivoDAOI.findArchiveById(id);
        ArquivoAux arquivo = new ArquivoAux();
        arquivo.setId(nArquivo.getId());
        arquivo.setIdUsuario(nArquivo.getIdUsuario());
        arquivo.setNome(nArquivo.getNome());
        arquivo.setConteudo(nArquivo.getConteudo());
        return SerializationUtils.serialize(arquivo);
    }

    @PostMapping("/confimarRecebimento")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Arquivo> confimarRecebimento(@RequestBody String ids) {
        String[] id = ids.split("_");
        Long idUsuario = Long.parseLong(id[0]);
        Long idArquivo = Long.parseLong(id[1]);

        Requisicao r = requisicoesDAOI.findByIdsUsuarioArquivo(idUsuario, idArquivo);
        r.setAtivo(false);

        requisicoesDAOI.save(r);

        return null;
    }

    @PostMapping("/novo")
    @ResponseStatus(HttpStatus.CREATED)
    public Arquivo addArquivo(@RequestBody Arquivo arquivo) {
        String[] nomeConteudo = arquivo.getNome().split(",");
        String nome = nomeConteudo[0];
        byte[] data = Base64.getDecoder().decode(nomeConteudo[1]);
        arquivo.setNome(nome);
        arquivo.setConteudo(data);
        List<Long> usuarios = usuarioDAOI.obterUsuarios(arquivo.getIdUsuario());
        Arquivo novoArquivo = arquivoDAOI.save(arquivo);
        
        for (int i=0; i<usuarios.size(); i++) {
            Requisicao r = new Requisicao();
            r.setId(10000000L);
            r.setIdUsuario(usuarios.get(i));
            r.setIdArquivo(novoArquivo.getId());
            r.setTipoRequisicao(0L);
            r.setAtivo(true);
            requisicoesDAOI.save(r);
            
            ArquivosCompartilhadosUsuarios acu = new ArquivosCompartilhadosUsuarios();
            acu.setId(10000000L);
            acu.setIdUsuario(usuarios.get(i));
            acu.setIdArquivo(novoArquivo.getId());
            arquivosCompartilhadosUsuariosDAOI.save(acu);
        }
        return novoArquivo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Arquivo> enviar(@RequestBody byte[] arquivo) throws ClassNotFoundException, IOException {
        
    	ArquivoAux nArquivo = SerializationUtils.deserialize(arquivo);
        Arquivo novoArquivo = new Arquivo();
        novoArquivo.setId(nArquivo.getId());
        novoArquivo.setIdUsuario(nArquivo.getIdUsuario());
        novoArquivo.setNome(nArquivo.getNome());
        novoArquivo.setConteudo(nArquivo.getConteudo());
        
        List<Long> usuarios = usuarioDAOI.obterUsuarios(novoArquivo.getIdUsuario());
        arquivoDAOI.save(novoArquivo);
        for (int i=0; i<usuarios.size(); i++) {
            Requisicao r = new Requisicao();
            r.setId(10000000L);
            r.setIdUsuario(usuarios.get(i));
            r.setIdArquivo(novoArquivo.getId());
            r.setTipoRequisicao(0L);
            requisicoesDAOI.save(r);
            
            ArquivosCompartilhadosUsuarios acu = new ArquivosCompartilhadosUsuarios();
            acu.setId(10000000L);
            acu.setIdUsuario(usuarios.get(i));
            acu.setIdArquivo(novoArquivo.getId());
            arquivosCompartilhadosUsuariosDAOI.save(acu);
        }
        return new ResponseEntity<Arquivo>(novoArquivo, HttpStatus.OK);
    }

    @GetMapping("/listar/{id}")
    public List<Arquivo> listarArquivos(@PathVariable("id") Long id) {
        List<Arquivo> arquivos = arquivoDAOI.findAllByUsuarioId(id);
        byte[] conteudo = null;
        for (Arquivo a : arquivos) {
            a.setConteudo(conteudo);
        }
        return arquivos;
    }


    

}

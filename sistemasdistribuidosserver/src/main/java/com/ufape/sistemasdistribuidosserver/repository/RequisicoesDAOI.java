package com.ufape.sistemasdistribuidosserver.repository;

import java.util.List;

import com.ufape.sistemasdistribuidosserver.model.Requisicao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RequisicoesDAOI extends JpaRepository<Requisicao, Long> {
    
    @Query("SELECT r FROM Requisicao r WHERE r.idUsuario=:id AND ativo IS NOT FALSE")
    public List<Requisicao> findByUserId(Long id);

    @Query("SELECT r FROM Requisicao r Where r.idUsuario=:idUsuario AND r.idArquivo=:idArquivo")
    public Requisicao findByIdsUsuarioArquivo(Long idUsuario, Long idArquivo);
}

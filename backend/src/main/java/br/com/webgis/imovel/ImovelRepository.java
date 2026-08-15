package br.com.webgis.imovel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {

	/**
	 * Página filtrada por nome do proprietário e por município (ambos "contém", sem diferenciar
	 * maiúsculas). Filtros ausentes chegam como string vazia, e {@code LIKE '%%'} casa com tudo.
	 * A paginação acontece no banco (LIMIT/OFFSET) — só a página pedida trafega.
	 */
	Page<Imovel> findByProprietarioNomeContainingIgnoreCaseAndMunicipioContainingIgnoreCase(
			String proprietarioNome, String municipio, Pageable pageable);

	/** Imóveis de um proprietário específico (lista curta, sem paginação). */
	List<Imovel> findByProprietarioId(Long proprietarioId, Sort sort);

	/** Quantos imóveis um proprietário possui (para a listagem de proprietários). */
	long countByProprietarioId(Long proprietarioId);
}

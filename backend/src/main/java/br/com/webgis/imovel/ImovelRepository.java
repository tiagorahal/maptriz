package br.com.webgis.imovel;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {

	/**
	 * Filtra por nome do proprietário e por município (ambos "contém", sem diferenciar maiúsculas).
	 * Filtros ausentes chegam como string vazia, e {@code LIKE '%%'} casa com tudo.
	 */
	List<Imovel> findByProprietarioNomeContainingIgnoreCaseAndMunicipioContainingIgnoreCase(
			String proprietarioNome, String municipio, Sort sort);

	/** Imóveis de um proprietário específico. */
	List<Imovel> findByProprietarioId(Long proprietarioId, Sort sort);

	/** Quantos imóveis um proprietário possui (para a listagem de proprietários). */
	long countByProprietarioId(Long proprietarioId);
}

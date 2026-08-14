package br.com.webgis.imovel;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {

	/**
	 * Filtra por proprietário e município (ambos "contém", sem diferenciar maiúsculas).
	 * Filtros ausentes chegam como string vazia, e {@code LIKE '%%'} casa com tudo — então o
	 * mesmo método serve para listar tudo, filtrar por um campo ou pelos dois.
	 */
	List<Imovel> findByProprietarioContainingIgnoreCaseAndMunicipioContainingIgnoreCase(
			String proprietario, String municipio, Sort sort);
}

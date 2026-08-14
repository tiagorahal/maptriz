package br.com.webgis.imovel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProprietarioRepository extends JpaRepository<Proprietario, Long> {

	Optional<Proprietario> findByNomeIgnoreCase(String nome);

	/** Lista os proprietários com a contagem de imóveis numa única query (evita N+1). */
	@Query("""
			select new br.com.webgis.imovel.ProprietarioResponse(p.id, p.nome, count(i.id))
			from Proprietario p
			left join Imovel i on i.proprietario = p
			group by p.id, p.nome
			order by p.nome
			""")
	List<ProprietarioResponse> listarComContagem();
}

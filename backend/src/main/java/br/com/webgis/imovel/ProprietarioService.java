package br.com.webgis.imovel;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProprietarioService {

	private final ProprietarioRepository proprietarioRepository;
	private final ImovelRepository imovelRepository;

	public ProprietarioService(ProprietarioRepository proprietarioRepository, ImovelRepository imovelRepository) {
		this.proprietarioRepository = proprietarioRepository;
		this.imovelRepository = imovelRepository;
	}

	@Transactional(readOnly = true)
	public List<ProprietarioResponse> listar() {
		return proprietarioRepository.listarComContagem();
	}

	@Transactional(readOnly = true)
	public List<ImovelResponse> imoveisDoProprietario(Long id) {
		if (!proprietarioRepository.existsById(id)) {
			throw new ProprietarioNaoEncontradoException(id);
		}
		return imovelRepository.findByProprietarioId(id, Sort.by("municipio")).stream()
				.map(ImovelResponse::de)
				.toList();
	}

	@Transactional
	public ProprietarioResponse renomear(Long id, String nome) {
		String limpo = nome.trim();
		Proprietario p = proprietarioRepository.findById(id)
				.orElseThrow(() -> new ProprietarioNaoEncontradoException(id));

		// O novo nome não pode colidir com o de OUTRO proprietário (nome é único).
		proprietarioRepository.findByNomeIgnoreCase(limpo).ifPresent(existente -> {
			if (!existente.getId().equals(id)) {
				throw new NomeProprietarioEmUsoException(limpo);
			}
		});

		p.setNome(limpo);
		proprietarioRepository.save(p);
		// A FK faz a mudança valer para TODOS os imóveis dele — sem tocar em cada linha de imovel.
		return new ProprietarioResponse(p.getId(), p.getNome(), imovelRepository.countByProprietarioId(id));
	}
}

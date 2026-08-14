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
}

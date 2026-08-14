package br.com.webgis.imovel;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImovelService {

	private final ImovelRepository repository;
	private final ProprietarioRepository proprietarioRepository;

	public ImovelService(ImovelRepository repository, ProprietarioRepository proprietarioRepository) {
		this.repository = repository;
		this.proprietarioRepository = proprietarioRepository;
	}

	@Transactional(readOnly = true)
	public List<ImovelResponse> buscar(String proprietario, String municipio) {
		String p = proprietario == null ? "" : proprietario;
		String m = municipio == null ? "" : municipio;
		return repository
				.findByProprietarioNomeContainingIgnoreCaseAndMunicipioContainingIgnoreCase(
						p, m, Sort.by("proprietario.nome"))
				.stream()
				.map(ImovelResponse::de)
				.toList();
	}

	@Transactional(readOnly = true)
	public ImovelResponse buscarPorId(Long id) {
		return repository.findById(id)
				.map(ImovelResponse::de)
				.orElseThrow(() -> new ImovelNaoEncontradoException(id));
	}

	@Transactional
	public ImovelResponse criar(ImovelRequest req) {
		Imovel imovel = new Imovel();
		aplicar(imovel, req);
		return ImovelResponse.de(repository.save(imovel));
	}

	@Transactional
	public ImovelResponse atualizar(Long id, ImovelRequest req) {
		Imovel imovel = repository.findById(id)
				.orElseThrow(() -> new ImovelNaoEncontradoException(id));
		aplicar(imovel, req);
		return ImovelResponse.de(repository.save(imovel));
	}

	@Transactional
	public void excluir(Long id) {
		if (!repository.existsById(id)) {
			throw new ImovelNaoEncontradoException(id);
		}
		repository.deleteById(id);
	}

	private void aplicar(Imovel imovel, ImovelRequest req) {
		imovel.setProprietario(obterOuCriarProprietario(req.proprietario()));
		imovel.setMunicipio(req.municipio());
		imovel.setUf(req.uf());
		imovel.setBairro(req.bairro());
		imovel.setRua(req.rua());
		imovel.setNumero(req.numero());
		imovel.setLatitude(req.latitude());
		imovel.setLongitude(req.longitude());
		imovel.setAreaM2(req.areaM2());
		imovel.setAtivo(req.ativo());
	}

	/** Reaproveita o proprietário existente pelo nome (case-insensitive) ou cria um novo. */
	private Proprietario obterOuCriarProprietario(String nome) {
		String limpo = nome.trim();
		return proprietarioRepository.findByNomeIgnoreCase(limpo)
				.orElseGet(() -> {
					Proprietario novo = new Proprietario();
					novo.setNome(limpo);
					return proprietarioRepository.save(novo);
				});
	}
}

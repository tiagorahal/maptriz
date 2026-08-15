package br.com.webgis.imovel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ImovelService {

	private final ImovelRepository repository;
	private final ProprietarioRepository proprietarioRepository;
	private final GeometriaService geometriaService;

	public ImovelService(ImovelRepository repository, ProprietarioRepository proprietarioRepository,
			GeometriaService geometriaService) {
		this.repository = repository;
		this.proprietarioRepository = proprietarioRepository;
		this.geometriaService = geometriaService;
	}

	@Transactional(readOnly = true)
	public Page<ImovelResponse> buscar(String proprietario, String municipio, Pageable pageable) {
		String p = proprietario == null ? "" : proprietario;
		String m = municipio == null ? "" : municipio;
		return repository
				.findByProprietarioNomeContainingIgnoreCaseAndMunicipioContainingIgnoreCase(p, m, pageable)
				.map(ImovelResponse::de);
	}

	/**
	 * Pontos para o mapa (todos os imóveis, projeção leve). Para volume muito grande, o passo
	 * seguinte seria filtrar pela área visível do mapa (bounding box) — fora do escopo aqui.
	 */
	@Transactional(readOnly = true)
	public List<PontoImovelResponse> pontos() {
		return repository.findAll().stream().map(PontoImovelResponse::de).toList();
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
		aplicarGeometria(imovel, req);
		verificarSobreposicao(imovel, null);
		return ImovelResponse.de(repository.save(imovel));
	}

	@Transactional
	public ImovelResponse atualizar(Long id, ImovelRequest req) {
		Imovel imovel = repository.findById(id)
				.orElseThrow(() -> new ImovelNaoEncontradoException(id));
		aplicar(imovel, req);
		aplicarGeometria(imovel, req);
		verificarSobreposicao(imovel, id);
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

	/** Define o polígono do imóvel a partir das dimensões informadas (ou de um quadrado √área). */
	private void aplicarGeometria(Imovel imovel, ImovelRequest req) {
		BigDecimal largura = req.largura();
		BigDecimal comprimento = req.comprimento();
		if (largura == null || comprimento == null) {
			BigDecimal lado = BigDecimal.valueOf(Math.sqrt(req.areaM2().doubleValue()));
			largura = lado;
			comprimento = lado;
		}
		imovel.setPoligono(
				geometriaService.gerarPoligonoWkt(req.latitude(), req.longitude(), largura, comprimento));
	}

	/**
	 * Rejeita o cadastro se o polígono do imóvel intersecta o de algum outro já existente.
	 * A comparação é em memória (JTS); para grande volume, um índice espacial (PostGIS) seria o
	 * passo seguinte.
	 */
	private void verificarSobreposicao(Imovel candidato, Long idIgnorar) {
		if (candidato.getPoligono() == null) {
			return;
		}
		List<String> existentes = repository.findByPoligonoIsNotNull().stream()
				.filter(i -> idIgnorar == null || !idIgnorar.equals(i.getId()))
				.map(Imovel::getPoligono)
				.toList();
		if (geometriaService.intersectaAlgum(candidato.getPoligono(), existentes)) {
			throw new AreaSobrepostaException();
		}
	}
}

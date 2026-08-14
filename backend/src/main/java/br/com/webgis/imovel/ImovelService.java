package br.com.webgis.imovel;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImovelService {

	private final ImovelRepository repository;

	public ImovelService(ImovelRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public List<ImovelResponse> listar() {
		return repository.findAll(Sort.by("proprietario")).stream()
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
		imovel.setProprietario(req.proprietario());
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
}

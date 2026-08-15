package br.com.webgis.imovel;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/imoveis")
@CrossOrigin(origins = "${app.cors.origin:http://localhost:4200}")
public class ImovelController {

	private final ImovelService service;

	public ImovelController(ImovelService service) {
		this.service = service;
	}

	@GetMapping
	public PageResponse<ImovelResponse> listar(
			@RequestParam(required = false) String proprietario,
			@RequestParam(required = false) String municipio,
			@PageableDefault(size = 20, sort = "proprietario.nome") Pageable pageable) {
		return PageResponse.de(service.buscar(proprietario, municipio, pageable));
	}

	@GetMapping("/mapa")
	public List<PontoImovelResponse> mapa() {
		return service.pontos();
	}

	@GetMapping("/{id}")
	public ImovelResponse buscar(@PathVariable Long id) {
		return service.buscarPorId(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ImovelResponse criar(@Valid @RequestBody ImovelRequest req) {
		return service.criar(req);
	}

	@PutMapping("/{id}")
	public ImovelResponse atualizar(@PathVariable Long id, @Valid @RequestBody ImovelRequest req) {
		return service.atualizar(id, req);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id) {
		service.excluir(id);
	}
}

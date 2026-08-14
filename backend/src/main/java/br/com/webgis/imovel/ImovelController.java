package br.com.webgis.imovel;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	public List<ImovelResponse> listar() {
		return service.listar();
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

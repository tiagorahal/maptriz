package br.com.webgis.imovel;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/proprietarios")
@CrossOrigin(origins = "${app.cors.origin:http://localhost:4200}")
public class ProprietarioController {

	private final ProprietarioService service;

	public ProprietarioController(ProprietarioService service) {
		this.service = service;
	}

	@GetMapping
	public List<ProprietarioResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}/imoveis")
	public List<ImovelResponse> imoveis(@PathVariable Long id) {
		return service.imoveisDoProprietario(id);
	}
}

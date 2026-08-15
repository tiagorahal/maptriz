package br.com.webgis.imovel;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Preenche a geometria dos imóveis que ainda não têm polígono (os que já existiam quando a coluna
 * foi criada). Deriva um quadrado da área. Idempotente: se todos já têm polígono, não faz nada.
 */
@Component
class BackfillGeometria implements ApplicationRunner {

	private final ImovelRepository repository;
	private final GeometriaService geometriaService;

	BackfillGeometria(ImovelRepository repository, GeometriaService geometriaService) {
		this.repository = repository;
		this.geometriaService = geometriaService;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		List<Imovel> semPoligono = repository.findByPoligonoIsNull();
		for (Imovel imovel : semPoligono) {
			if (imovel.getLatitude() == null || imovel.getLongitude() == null || imovel.getAreaM2() == null) {
				continue;
			}
			BigDecimal lado = BigDecimal.valueOf(Math.sqrt(imovel.getAreaM2().doubleValue()));
			imovel.setPoligono(
					geometriaService.gerarPoligonoWkt(imovel.getLatitude(), imovel.getLongitude(), lado, lado));
		}
		repository.saveAll(semPoligono);
	}
}

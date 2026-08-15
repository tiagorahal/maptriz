package br.com.webgis.imovel;

import java.math.BigDecimal;
import java.util.List;

/** Projeção leve para o mapa: ponto (lat/long) + vértices do polígono ([lat, lng]) em WGS 84. */
public record PontoImovelResponse(
		Long id,
		String proprietario,
		String municipio,
		BigDecimal latitude,
		BigDecimal longitude,
		List<double[]> poligono
) {
}

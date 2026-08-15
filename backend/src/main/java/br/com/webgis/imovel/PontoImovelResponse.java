package br.com.webgis.imovel;

import java.math.BigDecimal;

/** Projeção leve para o mapa: só o necessário para plotar e identificar o ponto. */
public record PontoImovelResponse(
		Long id,
		String proprietario,
		String municipio,
		BigDecimal latitude,
		BigDecimal longitude
) {
	public static PontoImovelResponse de(Imovel i) {
		return new PontoImovelResponse(
				i.getId(), i.getProprietario().getNome(), i.getMunicipio(),
				i.getLatitude(), i.getLongitude());
	}
}

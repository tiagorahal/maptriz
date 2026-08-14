package br.com.webgis.imovel;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ImovelResponse(
		Long id,
		Long proprietarioId,
		String proprietario,
		String municipio,
		String uf,
		String bairro,
		String rua,
		String numero,
		BigDecimal latitude,
		BigDecimal longitude,
		BigDecimal areaM2,
		boolean ativo,
		OffsetDateTime criadoEm,
		OffsetDateTime atualizadoEm
) {
	public static ImovelResponse de(Imovel i) {
		// 'proprietario' continua sendo o nome (string) para manter o contrato da API estável;
		// 'proprietarioId' é exposto para a navegação (imóveis de um proprietário).
		return new ImovelResponse(
				i.getId(), i.getProprietario().getId(), i.getProprietario().getNome(),
				i.getMunicipio(), i.getUf(), i.getBairro(), i.getRua(), i.getNumero(),
				i.getLatitude(), i.getLongitude(), i.getAreaM2(), i.isAtivo(),
				i.getCriadoEm(), i.getAtualizadoEm());
	}
}

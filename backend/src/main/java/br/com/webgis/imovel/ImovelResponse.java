package br.com.webgis.imovel;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ImovelResponse(
		Long id,
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
		return new ImovelResponse(
				i.getId(), i.getProprietario(), i.getMunicipio(), i.getUf(), i.getBairro(),
				i.getRua(), i.getNumero(), i.getLatitude(), i.getLongitude(),
				i.getAreaM2(), i.isAtivo(), i.getCriadoEm(), i.getAtualizadoEm());
	}
}

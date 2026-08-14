package br.com.webgis.imovel;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Dados que o cliente pode enviar. O id nunca vem do corpo (vem do path), e não há campos de
 * auditoria aqui — isso fecha o over-posting que existia com {@code @RequestBody Object}.
 */
public record ImovelRequest(
		@NotBlank String proprietario,
		@NotBlank String municipio,
		@NotBlank @Size(min = 2, max = 2) String uf,
		@NotBlank String bairro,
		@NotBlank String rua,
		@NotBlank String numero,
		@NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
		@NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
		@NotNull @Positive BigDecimal areaM2,
		boolean ativo
) {
}

package br.com.webgis.imovel;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envelope de paginação com contrato estável (evita depender da serialização interna de
 * {@code Page}, que o Spring desaconselha expor diretamente).
 */
public record PageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
	public static <T> PageResponse<T> de(Page<T> p) {
		return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
	}
}

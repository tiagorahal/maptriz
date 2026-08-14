package br.com.webgis.imovel;

public class ImovelNaoEncontradoException extends RuntimeException {
	public ImovelNaoEncontradoException(Long id) {
		super("Imóvel não encontrado: " + id);
	}
}

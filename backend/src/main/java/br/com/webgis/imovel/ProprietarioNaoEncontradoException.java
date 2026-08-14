package br.com.webgis.imovel;

public class ProprietarioNaoEncontradoException extends RuntimeException {
	public ProprietarioNaoEncontradoException(Long id) {
		super("Proprietário não encontrado: " + id);
	}
}

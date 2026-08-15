package br.com.webgis.imovel;

public class AreaSobrepostaException extends RuntimeException {
	public AreaSobrepostaException() {
		super("A área selecionada conflita com a de outro imóvel já cadastrado.");
	}
}

package br.com.webgis.imovel;

public class NomeProprietarioEmUsoException extends RuntimeException {
	public NomeProprietarioEmUsoException(String nome) {
		super("Já existe um proprietário com o nome: " + nome);
	}
}

package br.com.webgis.web;

import br.com.webgis.imovel.AreaSobrepostaException;
import br.com.webgis.imovel.ImovelNaoEncontradoException;
import br.com.webgis.imovel.NomeProprietarioEmUsoException;
import br.com.webgis.imovel.ProprietarioNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Centraliza os erros da API em respostas enxutas e com status correto — sem vazar stack trace,
 * que era o que acontecia antes (o corpo do 500 trazia a pilha inteira).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ImovelNaoEncontradoException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Map<String, Object> naoEncontrado(ImovelNaoEncontradoException ex) {
		return corpo(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(ProprietarioNaoEncontradoException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public Map<String, Object> proprietarioNaoEncontrado(ProprietarioNaoEncontradoException ex) {
		return corpo(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(NomeProprietarioEmUsoException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public Map<String, Object> nomeEmUso(NomeProprietarioEmUsoException ex) {
		return corpo(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(AreaSobrepostaException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public Map<String, Object> areaSobreposta(AreaSobrepostaException ex) {
		return corpo(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, Object> validacao(MethodArgumentNotValidException ex) {
		Map<String, Object> corpo = corpo(HttpStatus.BAD_REQUEST, "Dados inválidos");
		Map<String, String> campos = new TreeMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
		corpo.put("campos", campos);
		return corpo;
	}

	private Map<String, Object> corpo(HttpStatus status, String mensagem) {
		Map<String, Object> corpo = new LinkedHashMap<>();
		corpo.put("timestamp", OffsetDateTime.now());
		corpo.put("status", status.value());
		corpo.put("erro", status.getReasonPhrase());
		corpo.put("mensagem", mensagem);
		return corpo;
	}
}

package com.example.lancamentoapi.exceptionHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


// Captura exceções de ResponseEntities
@ControllerAdvice // Observa toda a aplicação
public class LancamentosExceptionHandler extends ResponseEntityExceptionHandler{
	
	@Autowired
	private MessageSource messageSorce;
	
	// Capturar mensagens que não conseguiram ler
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		String msgUsuario = messageSorce.getMessage("mensagem.invalida", null,  LocaleContextHolder.getLocale());
		return handleExceptionInternal(ex, msgUsuario, headers, HttpStatus.BAD_REQUEST, request);
	}

}

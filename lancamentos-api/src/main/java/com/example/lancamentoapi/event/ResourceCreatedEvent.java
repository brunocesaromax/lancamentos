package com.example.lancamentoapi.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServletResponse;

public class ResourceCreatedEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	@Getter
	private HttpServletResponse response;

	@Getter
	private Long id;
	
	public ResourceCreatedEvent(Object source, HttpServletResponse response, Long id) {
		super(source);
		this.response = response;
		this.id = id;
	}

}

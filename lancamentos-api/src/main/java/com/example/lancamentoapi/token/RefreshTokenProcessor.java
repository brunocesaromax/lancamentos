package com.example.lancamentoapi.token;

import com.example.lancamentoapi.configuration.property.ApiProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@ControllerAdvice
public class RefreshTokenProcessor implements ResponseBodyAdvice<OAuth2AccessToken> {

    @Autowired
    private ApiProperty apiProperty;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        /*beforeBodyWrite só será executado quando supports retornar true*/
        return Objects.requireNonNull(returnType.getMethod()).getName().equals("postAccessToken");
    }

    @Override
    public OAuth2AccessToken beforeBodyWrite(OAuth2AccessToken body,
                                             MethodParameter returnType,
                                             MediaType selectedContentType,
                                             Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                             ServerHttpRequest request,
                                             ServerHttpResponse response) {

        HttpServletRequest req = ((ServletServerHttpRequest) request).getServletRequest();
        HttpServletResponse resp = ((ServletServerHttpResponse) response).getServletResponse();

        DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) body;

        String refreshToken = body.getRefreshToken().getValue();
        addRefreshTokenInCookie(refreshToken, req, resp);
        removeRefreshTokenOfBody(token);

        return body;
    }

    private void removeRefreshTokenOfBody(DefaultOAuth2AccessToken token) {
        token.setRefreshToken(null);
    }

    private void addRefreshTokenInCookie(String refreshToken, HttpServletRequest req, HttpServletResponse resp) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(apiProperty.getSecurity().isEnableHttps());
        refreshTokenCookie.setPath(req.getContextPath()+"/oauth/token");
        refreshTokenCookie.setMaxAge(2592000); //30 dias
        resp.addCookie(refreshTokenCookie);
    }
}

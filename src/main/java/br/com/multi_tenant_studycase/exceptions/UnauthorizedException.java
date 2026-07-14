package br.com.multi_tenant_studycase.exceptions;

public class UnauthorizedException extends BusinessException{
    public UnauthorizedException(final String message) {
        super(message);
    }
}

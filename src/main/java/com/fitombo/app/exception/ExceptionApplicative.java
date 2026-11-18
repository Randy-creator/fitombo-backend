package com.fitombo.app.exception;

public final class ExceptionApplicative extends RuntimeException {

    private final CodeErreur code;

    public ExceptionApplicative(CodeErreur code) {
        super(code.message());
        this.code = code;
    }

    public CodeErreur code() {
        return code;
    }
}

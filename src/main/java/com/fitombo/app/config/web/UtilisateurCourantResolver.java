package com.fitombo.app.config.web;

import com.fitombo.app.controller.UtilisateurCourant;
import com.fitombo.app.exception.CodeErreur;
import com.fitombo.app.exception.ExceptionApplicative;
import java.util.UUID;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

final class UtilisateurCourantResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parametre) {
    return parametre.hasParameterAnnotation(UtilisateurCourant.class)
        && parametre.getParameterType() == UUID.class;
  }

  @Override
  public UUID resolveArgument(
      MethodParameter parametre,
      ModelAndViewContainer vue,
      NativeWebRequest requete,
      WebDataBinderFactory binder) {
    Authentication authentification = SecurityContextHolder.getContext().getAuthentication();
    if (authentification == null
        || !(authentification.getPrincipal() instanceof Jwt jwt)
        || jwt.getSubject() == null) {
      throw new ExceptionApplicative(CodeErreur.UTILISATEUR_NON_IDENTIFIE);
    }

    try {
      return UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException exception) {
      throw new ExceptionApplicative(CodeErreur.UTILISATEUR_NON_IDENTIFIE);
    }
  }
}

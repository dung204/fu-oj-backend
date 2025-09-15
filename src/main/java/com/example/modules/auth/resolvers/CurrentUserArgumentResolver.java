package com.example.modules.auth.resolvers;

import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

  private final UsersRepository usersRepository;

  @Override
  public boolean supportsParameter(@NonNull MethodParameter parameter) {
    return (
      parameter.hasParameterAnnotation(CurrentUser.class) &&
      parameter.getParameterType().equals(User.class)
    );
  }

  @Override
  @Nullable
  public User resolveArgument(
    @NonNull MethodParameter parameter,
    @Nullable ModelAndViewContainer mavContainer,
    @NonNull NativeWebRequest webRequest,
    @Nullable WebDataBinderFactory binderFactory
  ) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    String email = authentication.getName();
    return usersRepository.findByAccountEmail(email).orElse(null);
  }
}

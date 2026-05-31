package fe.banco_digital.controller;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

class UserDetailsArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserDetails userDetails;

    UserDetailsArgumentResolver(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return UserDetails.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter p, ModelAndViewContainer m,
                                   NativeWebRequest r, WebDataBinderFactory f) {
        return userDetails;
    }
}

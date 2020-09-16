package com.example.app.domains.home

import com.example.acl.domains.users.models.dtos.UserRequest
import com.example.app.MainApplication
import com.example.app.routing.Route
import com.example.auth.config.security.SecurityContext
import com.example.auth.config.security.TokenService
import com.example.auth.entities.UserAuth
import com.example.auth.social.services.SocialUserService
import com.example.coreweb.commons.Constants
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.UsersConnectionRepository
import org.springframework.social.connect.web.ProviderSignInUtils
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.context.request.WebRequest
import springfox.documentation.annotations.ApiIgnore

@Controller
@ApiIgnore
class HomeController @Autowired constructor(
        private val tokenService: TokenService,
        private val socialUserService: SocialUserService,
        private val connectionFactoryLocator: ConnectionFactoryLocator,
        private val connectionRepository: UsersConnectionRepository
) {

    @GetMapping("")
    fun home(): String {
        return "index"
    }

    @PostMapping(Route.V1.WEB_RELOAD_APPLICATION_CONTEXT)
    fun reloadApplicationContext(): String {
        MainApplication.restart()
        return "redirect:${com.example.acl.routing.Route.V1.WEB_ROLES_PAGE}"
    }

    @PostMapping(Route.V1.WEB_SHUTDOWN_APPLICATION_CONTEXT)
    fun closeApplicationContext() {
        MainApplication.terminate()
    }

    //@PostMapping("/register/social")
    fun socialRegister(@RequestParam("request") request: WebRequest): String {

        val providerSignInUtils = ProviderSignInUtils(connectionFactoryLocator, connectionRepository)
        val connection: Connection<*> = providerSignInUtils.getConnectionFromSession(request)

        val user = this.socialUserService.createSocialLoginUser(connection)
        val userAuth = UserAuth(user)

        SecurityContext.updateAuthentication(userAuth)
        val accessToken = tokenService.createAccessToken(userAuth)

        return "redirect:/admin/dashboard"
    }
}

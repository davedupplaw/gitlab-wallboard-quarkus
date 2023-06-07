package uk.dupplaw.gitlab.wallboard.endpoints

import java.util.regex.Pattern
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

private val FILE_NAME_PATTERN = Pattern.compile(".*[.][a-zA-Z\\d]+")

@WebFilter(urlPatterns = ["/*"])
class AngularRouteFilter : HttpFilter() {
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val request = req as HttpServletRequest
        val response = res as HttpServletResponse

        chain.doFilter(request, response)

        if (response.status == 404) {
            val path = request.requestURI.substring(
                request.contextPath.length
            ).replace("/+$".toRegex(), "")

            if (!FILE_NAME_PATTERN.matcher(path).matches()) {
                // We could not find the resource, i.e. it is not anything known to the server (i.e. it is not a REST
                // endpoint or a servlet), and does not look like a file so try handling it in the front-end routes.
                response.status = 200
                request.getRequestDispatcher("/").forward(request, response)
                response.outputStream.close()
            }
        }
    }
}

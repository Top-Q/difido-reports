package il.co.topq.report.front.errors;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * This class is responsible for logging request to non existing URLs. This can
 * be useful, for example, in cases which clients are trying to add tests to
 * execution with id with negative number
 * 
 * @author Itai Agmon
 *
 */
@Controller
public class ErrorLogger implements ErrorViewResolver {

	private static final Logger log = LoggerFactory.getLogger(ErrorLogger.class);

	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
		if (HttpStatus.NOT_FOUND == status) {
			log.warn("ERROR 404 (" + request.getRemoteAddr() + ") - Request for URL '" + model.get("path") + "'");
			// There are currently no views with the following names. 
			return new ModelAndView("error404", model);
		} else {
			return new ModelAndView("error", model);
		}
	}

}

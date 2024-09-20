package org.example.github2.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.github2.Services.EndpointService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EndPointController {
    private final EndpointService endpointService;

    public EndPointController(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @GetMapping("/endpointService")
    public String endpointService(HttpServletRequest request){
        return "redirect:"+endpointService.getFinalUrl(request);
    }
}

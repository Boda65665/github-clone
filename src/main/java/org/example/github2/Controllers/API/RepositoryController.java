package org.example.github2.Controllers.API;

import jakarta.servlet.http.HttpServletRequest;
import netscape.javascript.JSObject;
import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.example.github2.Services.DB.RepositoryService;
import org.example.github2.Services.DB.UserService;
import org.example.github2.Services.JwtService;
import org.example.github2.VersionControllerService.Models.Commit;
import org.example.github2.VersionControllerService.Service.CommitService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RepositoryController {
    private final CommitService commitService;
    private final RepositoryService repositoryService;
    private final UserService userService;
    private final JwtService jwtService;

    public RepositoryController(CommitService commitService, RepositoryService repositoryService, UserService userService, JwtService jwtService) {
        this.commitService = commitService;
        this.repositoryService = repositoryService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/pull/{idRep}")
    public ResponseEntity<?> pull(@RequestBody String requestBody, @PathVariable("idRep") String idRepString, HttpServletRequest request){
        int id;
        JSONObject jsonObject = new JSONObject(requestBody);
        String lastHashId = jsonObject.getString("lastHashId");
        if (lastHashId==null){
            return new ResponseEntity<>("{ \"error\": \"LastHashId can be null.\"}", HttpStatus.BAD_REQUEST);
        }
        try {
            id = Integer.parseInt(idRepString);
        }
        catch (NumberFormatException err){
            return new ResponseEntity<>("{ \"error\": \"Id can be number.\"}", HttpStatus.BAD_REQUEST);
        }
        String token = request.getHeader("auth");
        User user = userService.findUserByEmail(jwtService.extractEmail(token));
        Repository repository = repositoryService.findById(id);
        if (repository==null) return new ResponseEntity<>("{ \"error\": \"Not Found Repository.\"}", HttpStatus.NOT_FOUND);

        if (repository.getOwner()!=user){
            return new ResponseEntity<>("{ \"error\": \"This is not your repository.\"}", HttpStatus.UNAUTHORIZED);
        }
        List<Commit> commits = commitService.getCommitsAfter(lastHashId,id);
        if (commits.isEmpty()){
            return new ResponseEntity<>("{ \"error\": \"dont have updates.\" }", HttpStatus.ALREADY_REPORTED);
        }
        return new ResponseEntity<>(commits, HttpStatus.OK);
    }
}

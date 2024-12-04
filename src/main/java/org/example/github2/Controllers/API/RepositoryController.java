package org.example.github2.Controllers.API;

import jakarta.servlet.http.HttpServletRequest;
import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.example.github2.RequestBody.CommitBody;
import org.example.github2.Services.DB.RepositoryService;
import org.example.github2.Services.DB.UserService;
import org.example.github2.Services.JwtService;
import org.example.github2.VersionControllerService.Models.Commit;
import org.example.github2.VersionControllerService.Service.CommitService;
import org.example.github2.VersionControllerService.Service.RoleBackCommitService;
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
    private final RoleBackCommitService roleBackCommitService;

    public RepositoryController(CommitService commitService, RepositoryService repositoryService, UserService userService, JwtService jwtService, RoleBackCommitService roleBackCommitService) {
        this.commitService = commitService;
        this.repositoryService = repositoryService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.roleBackCommitService = roleBackCommitService;
    }
    @GetMapping("/pull/{idRep}")
    public ResponseEntity<?> pull(@RequestBody String requestBody, @PathVariable("idRep") String idRepString, HttpServletRequest request){
        int idRep = convertIdRepStringToInteger(idRepString);
        if (idRep==-1) new ResponseEntity<>("{ \"error\": \"Id can be number.\"}", HttpStatus.BAD_REQUEST);
        JSONObject jsonObject = new JSONObject(requestBody);
        String lastHashId = jsonObject.getString("lastHashId");
        if (lastHashId==null){
            return new ResponseEntity<>("{ \"error\": \"LastHashId can be null.\"}", HttpStatus.BAD_REQUEST);
        }

        String token = request.getHeader("auth");
        User user = userService.findUserByEmail(jwtService.extractEmail(token));
        Repository repository = repositoryService.findById(idRep);
        if (repository==null) return new ResponseEntity<>("{ \"error\": \"Not Found Repository.\"}", HttpStatus.BAD_REQUEST);

        if (repository.getOwner()!=user){
            return new ResponseEntity<>("{ \"error\": \"This is not your repository.\"}", HttpStatus.UNAUTHORIZED);
        }
        List<Commit> commits = commitService.getCommitsAfter(lastHashId, idRep);
        if (commits.isEmpty()){
            return new ResponseEntity<>("{ \"error\": \"dont have updates.\" }", HttpStatus.ALREADY_REPORTED);
        }
        return new ResponseEntity<>(commits, HttpStatus.OK);
    }

    private int convertIdRepStringToInteger(String idRepString) {
        try {
            return Integer.parseInt(idRepString);
        }
        catch (NumberFormatException err){
            return -1;
        }
    }

    @PostMapping("/canceled-commit/{idRep}")
    public ResponseEntity<?> canceledCommit(@RequestBody String requestBody,@PathVariable(name = "idRep") String idRepString){
        int idRep = convertIdRepStringToInteger(idRepString);
        if (idRep==-1) new ResponseEntity<>("{ \"error\": \"Id can be number.\"}", HttpStatus.BAD_REQUEST);
        Repository repository = repositoryService.findById(idRep);
        if (repository==null) return new ResponseEntity<>("{ \"error\": \"Not Found Repository.\"}", HttpStatus.NOT_FOUND);
        JSONObject jsonObject = new JSONObject(requestBody);
        String hashCommitId = jsonObject.getString("hashDeleteCommit");
        if (hashCommitId==null) return new ResponseEntity<>("{ \"error\": \"HashCommitId cannot be null.\"}", HttpStatus.BAD_REQUEST);
        System.out.println(hashCommitId);
        Commit commit = commitService.getCommitByHashId(idRep,hashCommitId);
        if (commit==null) return new ResponseEntity<>("{ \"error\": \"Not Found Commit whit this id.\"}", HttpStatus.BAD_REQUEST);
        if (commit.isCanceled()) return new ResponseEntity<>("{ \"error\": \"Already canceled.\" }", HttpStatus.ALREADY_REPORTED);
        roleBackCommitService.roleBackSpecificCommit(idRep,hashCommitId);
        return new ResponseEntity<>("{ \"message\": \"Complete.\" }", HttpStatus.OK);
    }
}

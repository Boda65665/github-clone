package org.example.github2.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.github2.DTOs.UserDTO;
import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.example.github2.Form.CreateRepositoryForm;
import org.example.github2.Services.DB.RepositoryService;
import org.example.github2.Services.DB.UserService;
import org.example.github2.Services.JwtService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Controller
@RequestMapping("/repository")
public class RepositoriesController {
    private final UserService userService;
    private final RepositoryService repositoryService;
    private final JwtService jwtService;

    public RepositoriesController(UserService userService, RepositoryService repositoryService, JwtService jwtService) {
        this.userService = userService;
        this.repositoryService = repositoryService;
        this.jwtService = jwtService;
    }

    @GetMapping("/{login}")
    public String getRepositoriesList(@PathVariable("login") String login, Model model){
        UserDTO user = userService.findUserDtoByLogin(login);
        model.addAttribute("user", user);
        return "/repository/list";
    }

    @GetMapping("/{login}/{repository}/**")
    public String getRepository(@PathVariable("login") String login, @PathVariable("repository") String repositoryName, Model model,HttpServletRequest request) {
        Repository repository = repositoryService.findByNameAndOwner(repositoryName, userService.findUserByLogin(login));
        if (repository==null) return "redirect:/";
        String path = "P:/"+request.getRequestURI();
        File directoryOrFile = new File(path);
        if (directoryOrFile.isDirectory()){
            String[] filesAndDirs = directoryOrFile.list();
            model.addAttribute("filesAndDirs", filesAndDirs);
        }
        else {
            StringBuilder content = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } catch (IOException e) {
                log.error(e.toString());
            }
            String fileContent = content.toString();
            model.addAttribute("content" , fileContent);
            return "/repository/file";
        }
        String href = request.getRequestURI();
        model.addAttribute("lastHref" ,href);
        model.addAttribute("uploadHref" ,getUploadHref(href,login,repositoryName));
        model.addAttribute("repository", repository);
        return "/repository/dir";
    }

    private Object getUploadHref(String href,String login,String repositoryName) {
        StringBuilder sb = new StringBuilder(href);
        String stringFinding = "/repository"+"/"+login+"/"+repositoryName;
        int indexStringFinding = href.indexOf(stringFinding);
        int indexInsert = indexStringFinding+stringFinding.length();
        return sb.insert(indexInsert, "/upload").toString();
    }

    @GetMapping("/new")
    public String createRepository(Model model){
        model.addAttribute("repositoryForm", new CreateRepositoryForm());
        return "/repository/new";
    }

    @PostMapping("/new")
    public String createRepository(HttpServletRequest httpServletRequest, @ModelAttribute("createRepositoryForm") CreateRepositoryForm createRepositoryForm){
        String email = jwtService.extractEmail(httpServletRequest);
        UserDTO userDTO = userService.findUserDtoByEmail(email);
        Repository repository = new Repository(createRepositoryForm.getName(), createRepositoryForm.getIsPrivate(), userService.findUserById(userDTO.getId()));
        repositoryService.addRepository(repository);
        String path = "P:/repository/"+userDTO.getLogin()+"/"+repository.getName();
        File directories = new File(path);
        directories.mkdirs();
        return "redirect:/repositories/"+userDTO.getLogin()+"/"+repository.getName();
    }

    @GetMapping("/{login}/{repository}/upload/**")
    public String download(@PathVariable String login, @PathVariable("repository") String repositoryName, HttpServletRequest request){
        String email = jwtService.extractEmail(request);
        User userDTO = userService.findUserByEmail(email);
        User ownerRepository = userService.findUserByLogin(login);
        if (userDTO==null || ownerRepository==null || userDTO.getId()!=ownerRepository.getId()){
            return "redirect:/";
        }
        Repository repository = repositoryService.findByNameAndOwner(repositoryName, ownerRepository);
        if (repository==null){
            return "redirect:/";
        }
        return "/repository/download";
    }

    @PostMapping("/{login}/{repository}/upload/**")
    public String download(HttpServletRequest request, @PathVariable String login, @PathVariable("repository") String repositoryName, @RequestParam("files") MultipartFile[] files) throws IOException {
        String email = jwtService.extractEmail(request);
        User userDTO = userService.findUserByEmail(email);
        User ownerRepository = userService.findUserByLogin(login);

        if (userDTO==null || ownerRepository==null || userDTO.getId()!=ownerRepository.getId()){
            return "redirect:/";
        }
        Repository repository = repositoryService.findByNameAndOwner(repositoryName, ownerRepository);
        if (repository==null){
            return "redirect:/";
        }
        String pathString = "P:/"+request.getRequestURI();
        pathString = pathString.replace("/upload", "");
        for (MultipartFile file : files) {
            pathString=pathString+"//"+file.getName();
            Path path = Path.of(pathString);
            Files.write(path, file.getBytes());
        }
        return "redirect:"+request.getRequestURI();
    }
}

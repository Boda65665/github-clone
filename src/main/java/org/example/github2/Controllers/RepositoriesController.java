package org.example.github2.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.github2.DTOs.UserDTO;
import org.example.github2.Entity.Repository;
import org.example.github2.Entity.User;
import org.example.github2.Form.CreateRepositoryForm;
import org.example.github2.Model.SourceType;
import org.example.github2.Services.DB.RepositoryService;
import org.example.github2.Services.DB.UserService;
import org.example.github2.Services.JwtService;
import org.example.github2.VersionControllerService.Entity.RepositoryTree;
import org.example.github2.VersionControllerService.Models.Directory;
import org.example.github2.VersionControllerService.Service.CommitService;
import org.example.github2.VersionControllerService.Service.ServiceRepositoryTree;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Slf4j
@Controller
@RequestMapping("/repository")
public class RepositoriesController {
    private final UserService userService;
    private final RepositoryService repositoryService;
    private final JwtService jwtService;
    private final ServiceRepositoryTree serviceRepositoryTree;
    private final CommitService commitService;
    @Value("${name.disk.with.repository}")
    private String NAME_DISK_WITH_REPOSITORY;

    public RepositoriesController(UserService userService, RepositoryService repositoryService, JwtService jwtService, ServiceRepositoryTree serviceRepositoryTree, CommitService commitService) {
        this.userService = userService;
        this.repositoryService = repositoryService;
        this.jwtService = jwtService;
        this.serviceRepositoryTree = serviceRepositoryTree;
        this.commitService = commitService;
    }

    @GetMapping({"/{login}", "/{login}/"})
    public String getRepositoriesList(@PathVariable("login") String login, Model model) {
        UserDTO user = userService.findUserDtoByLogin(login);
        if (user == null) return "redirect:/";
        model.addAttribute("user", user);
        ArrayList<String> namesRepository = new ArrayList<>();
        for (Repository repository : user.getRepositories()) {
            if (!serviceRepositoryTree.isDeleteRepository(repository.getId()))namesRepository.add(repository.getName());
        }
        model.addAttribute("namesRepository", namesRepository);
        return "repository/repositories";
    }

    @GetMapping("/{login}/{repository}/**")
    public String getRepository(@PathVariable("login") String login, @PathVariable("repository") String repositoryName, Model model, HttpServletRequest request) {
        Repository repository = repositoryService.findByNameAndOwner(repositoryName, userService.findUserByLogin(login));
        if (repository == null) return "redirect:/";
        String path = NAME_DISK_WITH_REPOSITORY + "/" + URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        File directoryOrFile = new File(path);
        if (!directoryOrFile.exists()) return "redirect:/repository/"+login;
        String href = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);
        String pathSourceInTree = href.replace("/repository/"+login+"/"+repositoryName, "");
        if (directoryOrFile.isDirectory()) {
            if(serviceRepositoryTree.isDeleteSource(repository.getId(),pathSourceInTree, SourceType.DIRECTORY)) return "redirect:/repository/"+login;
            model.addAttribute("filesAndDirs", getNamesExistingSources(repository.getId(),pathSourceInTree));
        } else {
            if(!pathSourceInTree.isEmpty() && serviceRepositoryTree.isDeleteSource(repository.getId(),pathSourceInTree, SourceType.FILE)) return "redirect:/repository/"+login;
            String content = getContentFromFile(path);
            if (content == null) return "redirect:/";
            model.addAttribute("content", content);
            model.addAttribute("editHref", getHref(href, login, repositoryName, "edit"));
            model.addAttribute("deleteHref", getHref(href, login, repositoryName, "delete"));
            return "/repository/file";
        }
        model.addAttribute("lastHref", href);
        model.addAttribute("deleteHref", getHref(href, login, repositoryName, "delete"));
        model.addAttribute("uploadHref", getHref(href, login, repositoryName, "upload"));
        model.addAttribute("repository", repository);
        return "/repository/dir";
    }

    private ArrayList<String> getNamesExistingSources(int repId, String pathSourceInTree) {
        Directory directory = serviceRepositoryTree.getDirectoryByPath(repId, pathSourceInTree);
        ArrayList<String> namesExistingSources = new ArrayList<>();
        for (org.example.github2.VersionControllerService.Models.File file : directory.getFiles()) {
            if (!file.isDelete()) namesExistingSources.add(file.getName());
        }
        for (Directory nestedDirectory : directory.getDirectories()) {
            if (!nestedDirectory.isDelete()) namesExistingSources.add(nestedDirectory.getName());
        }
        return namesExistingSources;
    }

    private String getContentFromFile(String path) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private Object getHref(String href, String login, String repositoryName,String action) {
        StringBuilder sb = new StringBuilder(href);
        String stringFinding = "/repository" + "/" + login + "/" + repositoryName;
        int indexStringFinding = href.indexOf(stringFinding);
        int indexInsert = indexStringFinding + stringFinding.length();
        return sb.insert(indexInsert, "/"+action).toString();
    }

    @GetMapping("/new")
    public String createRepository(Model model) {
        model.addAttribute("repositoryForm", new CreateRepositoryForm());
        return "/repository/new";
    }

    @PostMapping("/new")
    public String createRepository(HttpServletRequest httpServletRequest, @ModelAttribute("createRepositoryForm") CreateRepositoryForm createRepositoryForm) {
        String email = jwtService.extractEmail(httpServletRequest);
        UserDTO userDTO = userService.findUserDtoByEmail(email);
        Repository repository = new Repository(createRepositoryForm.getName(), createRepositoryForm.getIsPrivate(), userService.findUserById(userDTO.getId()));
        int id = repositoryService.addRepository(repository);
        RepositoryTree repositoryTree = new RepositoryTree(id);
        serviceRepositoryTree.save(repositoryTree);
        String path = NAME_DISK_WITH_REPOSITORY+"/repository/" + userDTO.getLogin() + "/" + repository.getName();
        File directories = new File(path);
        directories.mkdirs();
        return "redirect:/repositories/" + userDTO.getLogin() + "/" + repository.getName();
    }

    @GetMapping("/{login}/{repository}/upload/**")
    public String download(@PathVariable String login, @PathVariable("repository") String repositoryName, HttpServletRequest request) {
        if(isBadRequest(request,login,repositoryName)) return "redirect:/";
        return "/repository/download";
    }

    private boolean isBadRequest(HttpServletRequest request, String login, String repositoryName) {
        String email = jwtService.extractEmail(request);
        User userDTO = userService.findUserByEmail(email);
        User ownerRepository = userService.findUserByLogin(login);
        if (userDTO == null || ownerRepository == null || userDTO.getId() != ownerRepository.getId()) {
            return true;
        }
        Repository repository = repositoryService.findByNameAndOwner(repositoryName, ownerRepository);
        return repository == null;
    }

    @PostMapping("/{login}/{repository}/upload/**")
    public String download(HttpServletRequest request, @PathVariable String login, @PathVariable("repository") String repositoryName, @RequestParam("files") MultipartFile[] files) throws IOException {
        if(isBadRequest(request,login,repositoryName)) return "redirect:/";
        User ownerRepository = userService.findUserByLogin(login);
        Repository repository = repositoryService.findByNameAndOwner(repositoryName, ownerRepository);
        serviceRepositoryTree.addNewFile(request.getRequestURI().replace("/upload", ""), files, repository.getId());
        return "redirect:" + request.getRequestURI();
    }

    @GetMapping("/{login}/{repository}/edit/**")
    public String edit(HttpServletRequest request, @PathVariable String login, @PathVariable("repository") String repositoryName, Model model) {
        Repository repository = repositoryService.findByNameAndOwner(repositoryName, userService.findUserByLogin(login));
        if (repository == null) return "redirect:/";
        String path = NAME_DISK_WITH_REPOSITORY+"/" + request.getRequestURI();
        path = path.replace("/edit", "");
        File file = new File(path);
        if (file.isDirectory()) return "redirect:/";
        String content = getContentFromFile(path);
        if (content == null) return "redirect:/";
        model.addAttribute("content", content);
        return "/repository/file_edit";
    }

    @PostMapping("/{login}/{repository}/edit/**")
    public String edit(HttpServletRequest request, @PathVariable String login, @PathVariable("repository") String repositoryName, @RequestParam("content") String fileContent) throws IOException, NoSuchAlgorithmException {
        if(isBadRequest(request,login,repositoryName)) return "redirect:/";
        User ownerRepository = userService.findUserByLogin(login);
        Repository repository = repositoryService.findByNameAndOwner(repositoryName, ownerRepository);
        String pathToFile = request.getRequestURI().replace("/edit", "");
        String nameFile = pathToFile.split("/")[pathToFile.split("/").length - 1];
        String pathToDirectory = pathToFile.replace("/" + nameFile, "");
        commitService.addNewCommit(pathToDirectory, nameFile, fileContent.replace("\r", ""), repository.getId());
        return "redirect:" + pathToFile;
    }

    @PostMapping("/{login}/{repository}/delete/**")
    public String delete(@PathVariable String login,@PathVariable("repository") String repositoryName, HttpServletRequest request) {
        Path path = Paths.get(request.getRequestURI().replace("/delete", ""));
        String basePath = "/repository" + "/" + login + "/" + repositoryName + "/delete";
        String pathDelete = request.getRequestURI().replace(basePath, "");
        if(isBadRequest(request,login,repositoryName)) return "redirect:/";
        User ownerRepository = userService.findUserByLogin(login);
        Repository repository = repositoryService.findByNameAndOwner(repositoryName, ownerRepository);
        if (Files.isRegularFile(path)){
            serviceRepositoryTree.deleteFile(pathDelete, repository.getId());
        }
        else {
            if (pathDelete.isEmpty()) {
                serviceRepositoryTree.deleteRepository(repository.getId());
            }
            else serviceRepositoryTree.deleteDirectory(pathDelete, repository.getId());
        }
        return "redirect:"+request.getRequestURI();
    }
}

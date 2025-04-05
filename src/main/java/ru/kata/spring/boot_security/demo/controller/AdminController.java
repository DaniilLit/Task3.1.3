package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private UserService userService;


    @Autowired
     public AdminController(UserService userService) {
         this.userService = userService;
     }

    @GetMapping(value = "/")
    public String getAllUsers(Principal principal, ModelMap model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("admin",
                userService.findUserByEmail(principal.getName()));
        return "admin1";
    }


    @GetMapping("/addUser")
    public String addUser(Model model) {
        User user = new User();
        model.addAttribute("user", user);

      List<Role> roles = userService.getAllRoles();
        model.addAttribute("roles", roles);
        return "addUser";
    }
    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute("user") User user, @RequestParam(value = "roles", required = false) List<Long> roleIds) {
        if (roleIds != null) {
            Set<Role> roles = roleIds.stream()
                    .map(userService::getRole)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        userService.saveUser(user);
        return "redirect:/admin/";
    }

    @GetMapping(value = "edit/{id}")
    public String editUser(@PathVariable("id") Long id, ModelMap model) {
        model.addAttribute("user", userService.getUser(id));
        model.addAttribute("roles", userService.getAllRoles());
        return "edit";
    }

    @PatchMapping(value = "edit/{id}")
    public String edit(@ModelAttribute("user") @Valid User user) {
        userService.editUser(user);
        return "redirect:/admin/";
    }

    @GetMapping(value = "delete/{id}")
    public String deleteUser(@PathVariable("id") Long id,ModelMap model) {
        model.addAttribute("user", userService.getUser(id));
        model.addAttribute("roles", userService.getAllRoles());
        return "delete";
    }

    @PatchMapping(value = "delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/";
    }


    @GetMapping("/user")
    public String userPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login"; // Если пользователь не аутентифицирован
        }

        User user = userService.findUserByEmail(userDetails.getUsername());

        if (user == null) {
            model.addAttribute("errorMessage", "User not found");
            return "error";
        }

        model.addAttribute("user", user);
        return "user";
    }
}



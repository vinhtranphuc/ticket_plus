package com.so.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.so.exception.ResourceNotFoundException;
import com.so.jpa.entity.User;
import com.so.jpa.repository.UserRepository;
import com.so.mybatis.model.UserVO;
import com.so.payload.UserProfile;
import com.so.security.UserPrincipal;
import com.so.service.UserService;

@RestController
@RequestMapping("/api/users")
//@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001","http://127.0.0.1:3000","http://127.0.0.1:3001"})
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;

    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public UserVO getCurrentUser(Authentication authentication) {
    	UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
//        UserSummary userSummary = new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName());
        return userService.getUserById(currentUser.getId());
    }

    @GetMapping("/{username}")
    public UserProfile getUserProfile(@PathVariable(value = "username") String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt());
        return userProfile;
    }
}

package com.so.controller;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.so.common.BaseController;
import com.so.common.CookieUtils;
import com.so.common.Utils;
import com.so.exception.AppException;
import com.so.jpa.entity.AuthProvider;
import com.so.jpa.entity.ConfirmationToken;
import com.so.jpa.entity.Role;
import com.so.jpa.entity.RoleName;
import com.so.jpa.entity.User;
import com.so.payload.JwtAuthenticationResponse;
import com.so.payload.LoginRequest;
import com.so.payload.Response;
import com.so.payload.SignUpRequest;
import com.so.payload.UserIdentityAvailability;
import com.so.security.JwtTokenProvider;
import com.so.service.EmailSenderService;
import com.so.service.UserService;

@Controller
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

	protected Logger logger = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserService userService;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	JwtTokenProvider tokenProvider;

	@Autowired
	private EmailSenderService emailSenderService;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,HttpServletResponse response) {

		if (userService.isUserDisabled(loginRequest.getUsernameOrEmail())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new Response(null, "This account not yet confirm, please confirm at your email !"));
		}
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.generateToken(authentication);
		CookieUtils.addCookie(response, "jwt","Bearer "+jwt, 180);
		return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
	}

	@PostMapping("/signup")
	public ResponseEntity<Response> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
		try {
			if (userService.isExistsByUsername(signUpRequest.getUsername())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new Response(null, "Username is already taken!"));
			}
			if (userService.isExistsByEmail(signUpRequest.getEmail())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new Response(null, "Email Address already in use!"));
			}
			// Creating user's account
			User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(),
					signUpRequest.getPassword());
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			Role userRole = userService.getRoleByName(RoleName.ROLE_USER)
					.orElseThrow(() -> new AppException("User Role not set."));
			user.setRoles(Collections.singleton(userRole));
			user.setProvider(AuthProvider.local);
			User newUser = userService.saveUser(user);

			ConfirmationToken confirmationToken = new ConfirmationToken(user);
			userService.saveConfirmToken(confirmationToken);

			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(newUser.getEmail());
			mailMessage.setSubject("Complete Registration!");
			mailMessage.setText("To confirm your account, please click here : http://" + Utils.getLocalIp()+ ":"+severPost+"/api/auth/confirm-account?token=" + confirmationToken.getConfirmationToken());

			emailSenderService.sendEmail(mailMessage);

			URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{username}")
					.buildAndExpand(newUser.getUsername()).toUri();

			return ResponseEntity.created(location).body(new Response(null, "User registered successfully"));
		} catch (Exception e) {
			logger.error("Excecption : {}", ExceptionUtils.getStackTrace(e));
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}

	@GetMapping("/checkUsernameAvailability")
	public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
		Boolean isAvailable = !userService.isExistsByUsername(username);
		return new UserIdentityAvailability(isAvailable);
	}

	@GetMapping("/checkEmailAvailability")
	public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
		Boolean isAvailable = !userService.isExistsByEmail(email);
		return new UserIdentityAvailability(isAvailable);
	}

	@RequestMapping(value = { "/confirm-account" }, method = RequestMethod.GET)
	public String confirmAccount(Model model,  @RequestParam("token") String token) {
		
		Optional<ConfirmationToken> confirmationToken = userService.findByConfirmationToken(token);
		if(confirmationToken.isPresent()) {
			userService.setEnabledUserByToken(confirmationToken.get());
			model.addAttribute("title", "Congratulations !");
			model.addAttribute("content","Congratulations! Your account has been activated and email is verified!");
		} else{
			model.addAttribute("title", "Errors !");
			model.addAttribute("content","The link is invalid or broken!");
		}
		return "user/confirmAccount";
	}
}
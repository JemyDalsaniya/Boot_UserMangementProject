package com.springboot.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.model.Address;
import com.springboot.model.User;
import com.springboot.service.UserService;

@Controller
public class UserController {

	@Autowired
	UserService service;

	@RequestMapping(value = { "/", "/userlogin" })
	public String login() {
		System.out.println("inside login");
		return "Userlogin";
	}

	@RequestMapping(value = { "/register", "/addUser" })
	public String register(HttpSession session, Model model) {
		System.out.println("inside open register page");
		session.removeAttribute("userDetails");
		session.removeAttribute("adminDetails");
		session.removeAttribute("addressList");
		return "UserRegister";
	}

	@PostMapping("/Userlogin")
	public String userlogin(Model model, @Valid @ModelAttribute("loginForm") User user, HttpSession session,
			BindingResult result) {
		System.out.println("inside login");
		if (result.hasErrors()) {
			System.out.println("result" + result);
			return "Userlogin";
		} else {

			User userobj = service.userLogin(user);
			userobj.getUserStatus();
			List<Address> addressList = userobj.getAddress();
			if (userobj != null) {
				if (userobj.getUserStatus()) {
					List<User> adminDetailList = service.adminDetail(user);
					session.setAttribute("adminDetails", adminDetailList);
					// session.setAttribute("addressList", addressList);
					return "AdminHomePage";
				} else {
					session.setAttribute("userDetails", userobj);
					return "UserHomePage";
				}
			}
		}
		return null;

	}

	@RequestMapping("/view")
	public String viewUser(Model model, HttpSession session) {
		List<User> allusers = service.allUsers();
		model.addAttribute("allusers", allusers);
		return "ViewUser";
	}

	@PostMapping(value = { "/UserRegister", "/addUser" })
	public String registration(Model model, @ModelAttribute("registerForm") User user, HttpSession session,

			@RequestParam(value = "urlValue", required = false) String urlValue) {

		System.out.println("url value is" + urlValue);
		System.out.println("inside user register");
		System.out.println("address" + user.getAddress());

		service.userRegister(user);

		if (urlValue != null) {
			List<User> adminDetailList = service.adminDetail(user);
			// List<Address> addressList = adminDetailList.get(0).getAddress();
			session.setAttribute("adminDetails", adminDetailList);
			// session.setAttribute("addressList", addressList);
			return "AdminHomePage";
		} else {
			return "Userlogin";
		}
	}

	@PostMapping(value = "/DeleteUser")
	public void deleteUser(@RequestParam("userId") int id, HttpServletResponse response) throws IOException {
		service.deleteUser(id);
		response.getWriter().write("in success");
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/editProfile", method = RequestMethod.GET)
	public String editUser(User user, HttpSession session, Model model,
			@RequestParam(value = "id", required = false) String userId,
			@RequestParam(value = "urlValue", required = false) String valueURL) {

		if (valueURL.equals("admin")) {
			session.removeAttribute("userDetails");
			List<User> adminProfile = (List<User>) session.getAttribute("adminDetails");
			// List<User> adminDetailList = service.adminDetail(user);
			List<Address> addressList = adminProfile.get(0).getAddress();
			System.out.println("address list in admin edit profile" + addressList);
			session.setAttribute("adminDetails", adminProfile);
			session.setAttribute("addressList", addressList);
			byte[] adminImage = adminProfile.get(0).getImage();
			session.setAttribute("adminImage", adminImage);

		} else if (valueURL.equals("userEdit") || valueURL.equals("adminEdit")) {
			session.removeAttribute("adminDetails");
			session.removeAttribute("addressList");
			User userProfile = service.userIdDetail(Integer.parseInt(userId));
			byte[] byteimage = userProfile.getImage();
			session.setAttribute("userImage", byteimage);
			session.setAttribute("userDetails", userProfile);
		}

		return "UserRegister";
	}

	@PostMapping(value = "/UserEdit")
	public String updateUser(Model model, @ModelAttribute("registerForm") User user, HttpSession session,
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "urlValue", required = false) String urlValue) {

		if (file.isEmpty() && (urlValue.equals("userEdit") || urlValue.equals("adminEdit"))) {
			byte[] imagebytes = (byte[]) session.getAttribute("userImage");
			user.setImage(imagebytes);
		} else if (file.isEmpty()) {
			byte[] adminImagebytes = (byte[]) session.getAttribute("adminImage");
			user.setImage(adminImagebytes);
		}
		if (urlValue.equals("admin")) {
			user.setUserStatus(true);
		}
		service.userRegister(user);

		if (urlValue.equals("userEdit")) {
			session.removeAttribute("adminDetails");
			User userProfile = service.userIdDetail(user.getUserId());
			session.setAttribute("userDetails", userProfile);
			return "UserHomePage";
		} else if (urlValue.equals("admin")) {
			session.removeAttribute("userDetails");
			List<User> adminDetailList = service.adminDetail(user);
			List<Address> addressList = adminDetailList.get(0).getAddress();
			session.setAttribute("adminDetails", adminDetailList);
			session.setAttribute("addressList", addressList);
			return "AdminHomePage";

		} else if (urlValue.equals("adminEdit")) {
			List<User> allusers = service.allUsers();
			session.setAttribute("allusers", allusers);
			return "ViewUser";

		}
		return null;

	}

	@GetMapping(value = "/checkEmail")
	public void checkMail(@RequestParam("email") String email, HttpServletResponse response) throws IOException {
		boolean flag = service.checkMail(email);
		System.out.println("flag value" + flag);
		if (flag) {
			response.getWriter().write("available");
		} else {
			response.getWriter().write("not available");
		}
	}

	@RequestMapping("/userHome")
	public String userHome() {
		return "UserHomePage";
	}

	@RequestMapping("/adminHome")
	public String adminHome(User user, HttpSession session) {
		List<User> adminDetailList = service.adminDetail(user);
		System.out.println("admin details" + adminDetailList);
		session.setAttribute("adminDetails", adminDetailList);
		return "AdminHomePage";
	}

	@RequestMapping("forgetPassword")
	public String forgetPwd() {
		return "ForgetPassword";
	}

	@PostMapping("ForgetPassword")
	public String forgetPassword(Model model, @ModelAttribute("forgetPwd") User user) {
		System.out.println("inside forget pwd");

		service.updatePassword(user);
		return "Userlogin";
	}

	@PostMapping(value = "/ChangeRole")
	public String changeRole(@RequestParam("userId") int id, HttpServletResponse response) throws IOException {
		service.changeRole(id);
		response.getWriter().write("in success");
		return "AdminHomePage";

	}

	@RequestMapping(value = "/logout")
	public String logout(HttpSession session) {
		session.removeAttribute("userDetails");
		session.removeAttribute("addressList");
		session.removeAttribute("adminDetails");
		return "Userlogin";
	}

}

package com.springboot.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.dao.AddressRepository;
import com.springboot.dao.UserRepository;
import com.springboot.model.Address;
import com.springboot.model.User;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	AddressRepository addressRepository;

	@Override
	public void userRegister(User user) {
		List<Integer> addressIdList = new ArrayList<Integer>();
		for (Address address : user.getAddress()) {
			address.setUser(user);
			System.out.println("address list" + address);
		}
		if (user.getUserId() != 0) {
			int rem = 0;
			while (user.getAddress().size() - 1 != rem) {
				String cityCheck = user.getAddress().get(rem).getAddCity();
				if (cityCheck != null) {
					rem++;
				} else {
					user.getAddress().remove(rem);
				}
			}
			for (Address address : user.getAddress()) {
				addressIdList.add(address.getAddId());
			}
			addressRepository.deleteByAddIdNotInAndUser(addressIdList, user);

		}
		userRepository.save(user);
	}

	@Override
	public User userLogin(User user) {
		return userRepository.findByUserEmailAndUserPassword(user.getUserEmail(), user.getUserPassword());
	}

	@Override
	public List<User> allUsers() {
		return userRepository.findByUserStatus(false);
	}

	@Override
	public void deleteUser(int id) {
		userRepository.deleteById(id);
	}

	@Override
	public User userIdDetail(int id) {
		return userRepository.findByUserId(id);
	}

	@Override
	public void updatePassword(User user) {

		userRepository.updatePassword(user.getUserEmail(), user.getUserPassword());
	}

	@Override
	public boolean checkMail(String email) {
		List<User> list = userRepository.findByUserEmail(email);
		if (!list.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void changeRole(int id) {
		userRepository.changeRole(id);
	}

	@Override
	public List<User> adminDetail(User user) {
		return userRepository.findByUserStatus(true);
	}

}

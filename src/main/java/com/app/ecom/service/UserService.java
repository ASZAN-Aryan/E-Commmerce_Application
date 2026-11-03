
package com.app.ecom.service;

import com.app.ecom.dto.AddressDTO;
import com.app.ecom.dto.UserRequest;
import com.app.ecom.dto.UserResponse;
import com.app.ecom.model.Addresses;
import com.app.ecom.model.User;
import com.app.ecom.repository.UserRepositry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepositry userRepositry;

    public UserService(UserRepositry userRepositry) {
        this.userRepositry = userRepositry;
    }

    // Fetch all users and map them to responses
    public List<UserResponse> fetchAllUsers() {
        return userRepositry.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    // Create user from request DTO
    public UserResponse addUser(UserRequest request) {
        User user = mapToUser(request);
        User savedUser = userRepositry.save(user);

        return mapToUserResponse(savedUser);
    }

    // Map UserRequest to User entity

    private User mapToUser(UserRequest request) {
        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (request.getAddress() != null) {
            user.setAddress(mapToAddress(request.getAddress()));
        }

        return user;
    }


    // Map AddressDTO to Addresses entity
    private Addresses mapToAddress(AddressDTO dto) {
        Addresses address = new Addresses();

        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setZipcode(dto.getZipcode());

        return address;
    }

    // Map User entity to UserResponse
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();

        response.setId(String.valueOf(user.getId()));
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());

        if (user.getAddress() != null) {
            response.setAddress(
                    mapToAddressDTO(user.getAddress())
            );
        }

        return response;
    }

    // Map Addresses entity to AddressDTO
    private AddressDTO mapToAddressDTO(Addresses address) {
        AddressDTO dto = new AddressDTO();

        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setCountry(address.getCountry());
        dto.setZipcode(address.getZipcode());

        return dto;
    }

    // Fetch one user
    public Optional<UserResponse> fetchAUsers(Long id) {
        return userRepositry.findById(id)
                .map(this::mapToUserResponse);
    }


    public boolean updateUser(Long id, UserRequest request) {
        return userRepositry.findById(id)
                .map(existingUser -> {
                    if (request.getFirstName() != null) {
                        existingUser.setFirstName(request.getFirstName());
                    }

                    if (request.getLastName() != null) {
                        existingUser.setLastName(request.getLastName());
                    }

                    if (request.getEmail() != null) {
                        existingUser.setEmail(request.getEmail());
                    }

                    if (request.getPhone() != null) {
                        existingUser.setPhone(request.getPhone());
                    }

                    if (request.getRole() != null) {
                        existingUser.setRole(request.getRole());
                    }

                    if (request.getAddress() != null) {
                        if (existingUser.getAddress() == null) {
                            existingUser.setAddress(
                                    mapToAddress(request.getAddress())
                            );
                        } else {
                            AddressDTO dto = request.getAddress();
                            Addresses address = existingUser.getAddress();

                            if (dto.getStreet() != null) {
                                address.setStreet(dto.getStreet());
                            }
                            if (dto.getCity() != null) {
                                address.setCity(dto.getCity());
                            }
                            if (dto.getState() != null) {
                                address.setState(dto.getState());
                            }
                            if (dto.getCountry() != null) {
                                address.setCountry(dto.getCountry());
                            }
                            if (dto.getZipcode() != null) {
                                address.setZipcode(dto.getZipcode());
                            }
                        }
                    }

                    userRepositry.save(existingUser);
                    return true;
                })
                .orElse(false);
    }
}
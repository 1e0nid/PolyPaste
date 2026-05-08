package com.atta.PolyPaste.services;

import com.atta.PolyPaste.entitys.UserEntity;
import com.atta.PolyPaste.repository.UserRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByName(username);

        if (user == null) {
            throw new UsernameNotFoundException("Нет такого пользователя");
        }

        return User.builder()
                .username(user.getName())
                .password(user.getPassword())
                .roles("user")
                .build();
    }
}

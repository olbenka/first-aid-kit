package ru.project.service;

import ru.project.entity.AppUser;


public interface AppUserService {
    String registerUser(AppUser appUser);
    String setEmail(AppUser appUser, String email);
}

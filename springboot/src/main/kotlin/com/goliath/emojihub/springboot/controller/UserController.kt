package com.goliath.emojihub.springboot.controller

import com.goliath.emojihub.springboot.dto.user.LoginRequest
import com.goliath.emojihub.springboot.dto.user.SignUpRequest
import com.goliath.emojihub.springboot.dto.user.UserDto
import com.goliath.emojihub.springboot.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController (private val userService: UserService) {
    @GetMapping
    fun getUsers(): ResponseEntity<List<UserDto>> {
        return ResponseEntity.ok(userService.getUsers())
    }

    @PostMapping("/signup")
    fun signUp(
        @RequestBody signUpRequest: SignUpRequest
    ): ResponseEntity<Unit> {
        return ResponseEntity(userService.signUp(signUpRequest), HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequest
    ): ResponseEntity<Unit> {
        return ResponseEntity.ok(userService.login(loginRequest))
    }
}